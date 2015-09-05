package net.drewke.tdme.audio.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.drewke.tdme.os.FileSystem;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * JOrbis/Ogg Vorbis decoder
 * 	uses: JOrbis-0.0.17, LGPL.
 * 	based on: JOrbisPlayer from JOrbis-0.0.17, GPL.
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class JOrbisDecoder extends AudioDecoder {

	private enum OggReadState {NONE, PAGE, PACKET, SYNTHESIS, READBITSTREAM};

	private final static int BUFSIZE = 4096 * 2;
	private static final int RETRY = 3;

	private boolean initiated;
	private String pathName;
	private String fileName;
	InputStream bitStream;
	private SyncState oggSynchState;
	private StreamState oggStreamState;
	private Page oggPage;
	private Packet oggPacket;
	private Info oggInfo;
	private Comment oggComment;
	private DspState oggDspState;
	private Block oggBlock;
	private byte[] buffer;
	private int bytes;
	private boolean chained;
	int retry;
	int endOfStream;
	float[][][] _pcmf;
	int[] _index;
	int samples;
	int sampleAt;
	OggReadState oggReadState; 

	/**
	 * Public constructor
	 */
	public JOrbisDecoder() {
		initiated = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.drewke.tdme.audio.AudioDecoder#openFile(java.lang.String,
	 * java.lang.String)
	 */
	public void openFile(String pathName, String fileName) throws IOException, AudioDecoderException {
		this.pathName = pathName;
		this.fileName = fileName;

		//
		oggSynchState = new SyncState();
		oggStreamState = new StreamState();
		oggPage = new Page();
		oggPacket = new Packet();

		oggInfo = new Info();
		oggComment = new Comment();
		oggDspState = new DspState();
		oggBlock = new Block(oggDspState);

		buffer = null;
		bytes = 0;
		retry = RETRY;

		oggSynchState.init();

		retry = RETRY;
		endOfStream = 0;

		_pcmf = new float[1][][];
		_index = null;

		//
		samples = 0;
		sampleAt = 0;
		oggReadState = OggReadState.PAGE;

		try {
			int index = oggSynchState.buffer(BUFSIZE);

			// open input stream
			bitStream = FileSystem.getInstance().getInputStream(pathName, fileName);

			buffer = oggSynchState.data;
			bytes = bitStream.read(buffer, index, BUFSIZE);
			oggSynchState.wrote(bytes);

			if (chained) { //
				chained = false; //
			} else { //
				if (oggSynchState.pageout(oggPage) != 1) {
					throw new AudioDecoderException("Input does not appear to be an Ogg bitstream.");
				}
			}

			//
			oggStreamState.init(oggPage.serialno());
			oggStreamState.reset();

			//
			oggInfo.init();
			oggComment.init();

			if (oggStreamState.pagein(oggPage) < 0) {
				throw new AudioDecoderException("Error reading first page of Ogg bitstream data.");
			}

			retry = RETRY;

			//
			if (oggStreamState.packetout(oggPacket) != 1) {
				// no page? must not be vorbis
				throw new AudioDecoderException("Error reading initial header packet.");
			}

			//
			if (oggInfo.synthesis_headerin(oggComment, oggPacket) < 0) {
				throw new AudioDecoderException("This Ogg bitstream does not contain Vorbis audio data.");
			}

			//
			int i = 0;
			while (i < 2) {
				while (i < 2) {
					int result = oggSynchState.pageout(oggPage);
					if (result == 0) break; // Need more data
					if (result == 1) {
						oggStreamState.pagein(oggPage);
						while (i < 2) {
							result = oggStreamState.packetout(oggPacket);
							if (result == 0) break;
							if (result == -1) {
								throw new AudioDecoderException("JOrbisDecoder::Corrupt secondary header. Exiting.");
							}
							oggInfo.synthesis_headerin(oggComment, oggPacket);
							i++;
						}
					}
				}

				//
				index = oggSynchState.buffer(BUFSIZE);
				buffer = oggSynchState.data;
				bytes = bitStream.read(buffer, index, BUFSIZE);
				if (bytes == 0 && i < 2) {
					throw new AudioDecoderException("End of file before finding all Vorbis headers!");
				}
				oggSynchState.wrote(bytes);
			}

			{
				byte[][] ptr = oggComment.user_comments;
				StringBuffer sb = new StringBuffer();

				for (int j = 0; j < ptr.length; j++) {
					if (ptr[j] == null)
						break;
					System.err.println("Comment: "
							+ new String(ptr[j], 0, ptr[j].length - 1));
					if (sb != null)
						sb.append(" "
								+ new String(ptr[j], 0, ptr[j].length - 1));
				}
				sampleRate = oggInfo.rate;
				channels = oggInfo.channels;
				bitsPerSample = 16;
			}

			oggDspState.synthesis_init(oggInfo);
			oggBlock.init(oggDspState);
			_index = new int[oggInfo.channels];

			//
			initiated = true;
			oggReadState = OggReadState.PAGE; 
		} catch (IOException ioException) {
			try { bitStream.close(); } catch (IOException ioe) {}
			throw ioException;
		} finally {
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioDecoder#reset()
	 */
	public void reset() throws IOException, AudioDecoderException {
		// do nothing if not initiated
		if (initiated == false) return;

		// otherwise close
		close();
		//	and reopen
		openFile(pathName, fileName);
		//	this can be improved for sure!
	}

	/**
	 * Reads from bit stream until next samples are available
	 * @throws IOException
	 */
	private void readNextSamples() throws IOException {
		while (endOfStream == 0) {
			switch(oggReadState) {
				case NONE: {
					return;
				}
				case PAGE: {
					int result = oggSynchState.pageout(oggPage);
					if (result == 0) {
						oggReadState = OggReadState.READBITSTREAM;
					} else
					if (result == -1) {
						// redo PAGE
					} else {
						oggStreamState.pagein(oggPage);
						if (oggPage.granulepos() == 0) { //
							chained = true; //
							endOfStream = 1; //
							oggReadState = OggReadState.NONE;
							// redo PAGE
						} else {
							oggReadState = OggReadState.PACKET;
						}
					}
					break;
				}
				case PACKET: {
					int result = oggStreamState.packetout(oggPacket);
//						System.out.println("packet out");
					if (result == 0) {
						// need more data
						if (oggPage.eos() != 0) {
							oggReadState = OggReadState.NONE;
							endOfStream = 1;
						} else {
							oggReadState = OggReadState.PAGE;
						}
					} else
					if (result == -1) {
						// missing or corrupt data
						// at this page position
						// no reason to complain; already complained
						// above
						// redo PACKET
					} else {
						// we have a packet. Decode it
						if (oggBlock.synthesis(oggPacket) == 0) {
							// success!
							oggDspState.synthesis_blockin(oggBlock);
//								System.out.println("synthesis");
						}
						oggReadState = OggReadState.SYNTHESIS;
					}
					break;
				}
				case SYNTHESIS: {
					if (samples > 0) oggDspState.synthesis_read(samples);
					if ((samples = oggDspState.synthesis_pcmout(_pcmf, _index)) > 0) {
						sampleAt = 0;
						return;
					} else {
						oggReadState = OggReadState.PACKET;
					}
					break;
				}
				case READBITSTREAM: {
					int index = oggSynchState.buffer(BUFSIZE);
					buffer = oggSynchState.data;
					bytes = bitStream.read(buffer, index, BUFSIZE);
					if (bytes == -1) {
						endOfStream = 1;
						oggReadState = OggReadState.NONE;
					} else
					if (bytes == 0) {
						endOfStream = 1;
						oggReadState = OggReadState.NONE;
					} else {
						oggSynchState.wrote(bytes);
						oggReadState = OggReadState.PAGE;
					}
					break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioDecoder#readFromStream(java.nio.ByteBuffer)
	 */
	public int readFromStream(ByteBuffer data) throws IOException, AudioDecoderException {
		if (initiated == false) return 0;

		//
		int bytesDecoded = 0;
		do {
			// read until audio data can be decoded, if we dont have any yet
			if (sampleAt >= samples) {
				readNextSamples();
			}

			// no more data, so return
			if (samples == 0) {
				data.flip();
				return bytesDecoded;
			}

			// decode audio data
			float[][] pcmf = _pcmf[0];
			for (; sampleAt < samples; sampleAt++) {
				// exit if buffer is full
				if (data.remaining() == 0) {
					data.flip();
					return bytesDecoded;
				}
				// otherwise read into buffer
				for (int channelAt = 0; channelAt < oggInfo.channels; channelAt++) {
					int mono = _index[channelAt];
					int val = (int) (pcmf[channelAt][mono + sampleAt] * 32767.);
					if (val > 32767) val = 32767;
					if (val < -32768) val = -32768;
					if (val < 0) val = val | 0x8000;
					data.put((byte) (val));
					data.put((byte) (val >>> 8));
					bytesDecoded+= 2;
				}
			}
		} while(endOfStream == 0);

		//
		data.flip();
		return bytesDecoded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.drewke.tdme.audio.AudioDecoder#close()
	 */
	public void close() {
		if (initiated == false) return;
		try { bitStream.close(); } catch (IOException ioe) {}
		initiated = false;
		bitStream = null;
		oggSynchState = null;
		oggStreamState = null;
		oggPage = null;
		oggPacket = null;
		oggInfo = null;
		oggComment = null;
		oggDspState = null;
		oggBlock = null;
		buffer = null;
		bytes = 0;
		chained = false;
		retry = 0;
		endOfStream = 0;
		_pcmf = null;
		_index = null;
		samples = 0;
		sampleAt = 0;
		oggReadState = OggReadState.NONE; 
	}

}

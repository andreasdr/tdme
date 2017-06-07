package net.drewke.tdme.audio;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.drewke.tdme.audio.decoder.AudioDecoder;
import net.drewke.tdme.audio.decoder.AudioDecoderException;
import net.drewke.tdme.audio.decoder.JOrbisDecoder;
import net.drewke.tdme.utils.Console;

import com.jogamp.openal.AL;

/**
 * Audio Stream 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class AudioStream extends AudioEntity {

	private static final int BUFFER_COUNT = 3;
	private static final int BUFFER_SIZE = 8192 * 4;

	private boolean initiated = false;
	private String pathName;
	private String fileName;
	private int[] alBufferIds;
	private int alSourceId;
	private AudioDecoder decoder;
	int format;
	int frequency;
	ByteBuffer data;

	/**
	 * Protected constructor
	 * @param id
	 * @param path name
	 * @param file name
	 */
	protected AudioStream(String id, String pathName, String fileName) {
		super(id);
		this.pathName = pathName;
		this.fileName = fileName;
		alBufferIds = null;
		alSourceId = Audio.ALSOURCEID_NONE;
		decoder = null;
		format = -1;
		frequency = -1;
		data = null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#isPlaying()
	 */
	public boolean isPlaying() {
		int[] state = new int[1];
		Audio.al.alGetSourcei(alSourceId, AL.AL_SOURCE_STATE, state, 0);
		return (state[0] == AL.AL_PLAYING);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#rewind()
	 */
	public void rewind() {
		if (initiated == false) return;

		try {
			//
			decoder.reset();
		} catch (IOException ioe) {
			Console.println("Audio stream: '" + id + "': " + ioe.getMessage());
		} catch (AudioDecoderException ade) {
			Console.println("Audio stream: '" + id + "': " + ade.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#play()
	 */
	public void play() {
		if (initiated == false) return;

		if (isPlaying() == true) stop();

		// update AL properties
		updateProperties();

		//
		int buffersToPlay = 0;
		for (int i = 0; i < alBufferIds.length; i++) {
			data.clear();
			try {
				//
				int bytesDecoded = decoder.readFromStream(data);

				// skip if no more data is available
				if (bytesDecoded == 0) break;
			} catch (IOException ioe) {
				Console.println("Audio stream: '" + id + "': " + ioe.getMessage());
			} catch (AudioDecoderException ade) {
				Console.println("Audio stream: '" + id + "': " + ade.getMessage());
			}
			Audio.al.alBufferData(alBufferIds[i], format, data, data.remaining(), frequency);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				Console.println("Audio stream: '" + id + "': Could not upload buffer");
			}
			buffersToPlay++;
		}

		//
		Audio.al.alSourceQueueBuffers(alSourceId, buffersToPlay, alBufferIds, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not queue buffers");
		}
		Audio.al.alSourcePlay(alSourceId);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not play source");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#pause()
	 */
	public void pause() {
		if (initiated == false) return;
		Audio.al.alSourcePause(alSourceId);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio sound: '" + id + "': Could not pause");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#stop()
	 */
	public void stop() {
		if (initiated == false) return;
		Audio.al.alSourceStop(alSourceId);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio sound: '" + id + "': Could not stop");
		}

		// determine queued buffers
		int[] queuedBuffersArray = new int[1];
		Audio.al.alGetSourcei(alSourceId, AL.AL_BUFFERS_QUEUED, queuedBuffersArray, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not determine queued buffers");
		}
		int queuedBuffers = queuedBuffersArray[0];

		// unqueue buffers
		if (queuedBuffers > 0) {
			int[] removedBuffers = new int[queuedBuffers];
			Audio.al.alSourceUnqueueBuffers(alSourceId, queuedBuffers, removedBuffers, 0);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				Console.println("Audio stream: '" + id + "': Could not unqueue buffers");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#initialize()
	 */
	protected boolean initialize() {
		// decode audio stream
		decoder = new JOrbisDecoder();
		try {
			// decode ogg vorbis
			decoder.openFile(pathName, fileName);
			Console.println(
				"Audio stream: '" +
				id + "' with " +
				decoder.getBitsPerSample() +" bits per sample, " +
				decoder.getChannels() +" channels, " +
				decoder.getSampleRate() +" samplerate"
			);
			frequency = decoder.getSampleRate();
			switch (decoder.getChannels()) {
				case(1): format = AL.AL_FORMAT_MONO16; break;
				case(2): format = AL.AL_FORMAT_STEREO16; break;
				default:
					Console.println("Audio stream: '" + id + "': Unsupported number of channels");
			}
		} catch (IOException ioe) {
			Console.println("Audio stream: '" + id + "': " + ioe.getMessage());
			decoder.close();
			return false;
		} catch (AudioDecoderException ade) {
			Console.println("Audio stream: '" + id + "': " + ade.getMessage());
			decoder.close();
			return false;
		}

		alBufferIds = new int[BUFFER_COUNT];
		Audio.al.alGenBuffers(alBufferIds.length, alBufferIds, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not generate buffer");
			return false;
		}

		// create source
		int[] sourceIds = new int[1];
		Audio.al.alGenSources(1, sourceIds, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not generate source");
			dispose();
			return false;			
		}

		// have our source id
		alSourceId = sourceIds[0];

		// initiate sound properties
		updateProperties();

		//
		data = ByteBuffer.allocateDirect(BUFFER_SIZE);
		
		//
		initiated = true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#update()
	 */
	protected void update() {
		if (initiated == false) return;

		// determine processed buffers
		int[] processedBuffersArray = new int[1];
		Audio.al.alGetSourcei(alSourceId, AL.AL_BUFFERS_PROCESSED, processedBuffersArray, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			Console.println("Audio stream: '" + id + "': Could not determine processed buffers");
		}
		int processedBuffers = processedBuffersArray[0];

		//
		if (processedBuffers > 0) {
			while (processedBuffers > 0) {
				// get a processed buffer id and unqueue it
				int[] processedBufferIdArray = new int[1];
				Audio.al.alSourceUnqueueBuffers(alSourceId, 1, processedBufferIdArray, 0);
				if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
					Console.println("Audio stream: '" + id + "': Could not unqueue buffers");
				}
				int processedBufferId = processedBufferIdArray[0];
	
				// fill processed buffer again
				data.clear();
				int bytesDecoded = 0;
				try {
					//
					bytesDecoded = decoder.readFromStream(data);
					if (looping == true && bytesDecoded < BUFFER_SIZE) {
						decoder.reset();
						bytesDecoded+= decoder.readFromStream(data);
					}
				} catch (IOException ioe) {
					Console.println("Audio stream: '" + id + "': " + ioe.getMessage());
				} catch (AudioDecoderException ade) {
					Console.println("Audio stream: '" + id + "': " + ade.getMessage());
				}

				// new buffer if we have any data left
				if (bytesDecoded > 0) {
					// upload buffer data
					Audio.al.alBufferData(processedBufferId, format, data, data.remaining(), frequency);
					if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
						Console.println("Audio stream: '" + id + "': Could not upload buffer");
					}
	
					// queue it
					Audio.al.alSourceQueueBuffers(alSourceId, 1, processedBufferIdArray, 0);
					if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
						Console.println("Audio stream: '" + id + "': Could not queue buffer");
					}
				}

				// processed it
				processedBuffers--;
			}
		}

		// update AL properties
		updateProperties();
	}

	/**
	 * Updates properties to Open AL
	 */
	private void updateProperties() {
		// update sound properties
		Audio.al.alSourcef (alSourceId, AL.AL_PITCH, pitch);
		Audio.al.alSourcef (alSourceId, AL.AL_GAIN, gain);
		Audio.al.alSourcefv (alSourceId, AL.AL_POSITION, sourcePosition.getArray(), 0);
		Audio.al.alSourcefv (alSourceId, AL.AL_DIRECTION, sourceDirection.getArray(), 0);
		Audio.al.alSourcefv (alSourceId, AL.AL_VELOCITY, sourceVelocity.getArray(), 0);
		if (fixed == true) {
			Audio.al.alSourcef(alSourceId, AL.AL_ROLLOFF_FACTOR,  0.0f);
			Audio.al.alSourcei(alSourceId, AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);
		} else {
			Audio.al.alSourcef(alSourceId, AL.AL_ROLLOFF_FACTOR,  1.0f);
			Audio.al.alSourcei(alSourceId, AL.AL_SOURCE_RELATIVE, AL.AL_FALSE);			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#dispose()
	 */
	protected void dispose() {
		if (alSourceId != Audio.ALSOURCEID_NONE) {
			Audio.al.alDeleteSources(1, new int[]{alSourceId}, 0);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				Console.println("Audio sound: '" + id + "': Could not delete source");
			}
			alSourceId = Audio.ALSOURCEID_NONE;
		}
		if (alBufferIds != null) {
			//
			Audio.al.alDeleteBuffers(alBufferIds.length, alBufferIds, 0);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				Console.println("Audio sound: '" + id + "': Could not delete buffers");
			}
			alBufferIds = null;
		}
		if (decoder != null) decoder.close();
		initiated = false;
	}

}

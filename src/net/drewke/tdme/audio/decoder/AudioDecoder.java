package net.drewke.tdme.audio.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Audio decoder base class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class AudioDecoder {

	public final static int CHANNELS_NONE = -1;
	public final static int SAMPLERATE_NONE = -1;
	public final static int BITSPERSAMPLES_NONE = -1;

	protected int channels = CHANNELS_NONE;
	protected int sampleRate = SAMPLERATE_NONE;
	protected int bitsPerSample = BITSPERSAMPLES_NONE;

	/**
	 * Open a local file
	 * @param path name
	 * @param file name
	 */
	public abstract void openFile(String pathName, String fileName) throws IOException, AudioDecoderException;

	/**
	 * Resets this audio decoder, if a stream was open it will be rewinded
	 */
	public abstract void reset() throws IOException, AudioDecoderException;

	/**
	 * @return number of channels or CHANNELS_NONE
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @return sample rate in hz or SAMPLERATE_NONE
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return bits per sample or BITSPERSAMPLES_NONE
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Read raw PCM data from stream 
	 * @param byte buffer
	 * @return number of bytes read
	 */
	public abstract int readFromStream(ByteBuffer data) throws IOException, AudioDecoderException;

	/**
	 * Closes the audio file
	 */
	public abstract void close();

}

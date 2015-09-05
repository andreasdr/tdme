package net.drewke.tdme.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.drewke.tdme.audio.decoder.AudioDecoderException;
import net.drewke.tdme.audio.decoder.JOrbisDecoder;

import com.jogamp.openal.AL;

/**
 * Simple sound implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Sound extends AudioEntity {

	private boolean initiated = false;
	private String pathName;
	private String fileName;
	private String bufferId;
	private int alBufferId;
	private int alSourceId;

	/**
	 * Protected constructor
	 * @param id
	 * @param path name
	 * @param file name
	 */
	protected Sound(String id, String pathName, String fileName) {
		super(id);
		this.bufferId = pathName + File.separator + fileName;
		this.pathName = pathName;
		this.fileName = fileName;
		alBufferId = Audio.ALBUFFERID_NONE;
		alSourceId = Audio.ALSOURCEID_NONE;
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
		Audio.al.alSourceRewind(alSourceId);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			System.out.println("Audio sound: '" + id + "': Could not rewind");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#play()
	 */
	public void play() {
		if (initiated == false) return;

		// update
		update();

		// play
		Audio.al.alSourcePlay(alSourceId);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			System.out.println("Audio sound: '" + id + "': Could not play");
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
			System.out.println("Audio sound: '" + id + "': Could not pause");
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
			System.out.println("Audio sound: '" + id + "': Could not stop");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#init()
	 */
	protected boolean init() {
		// check if we already have this buffer
		AudioBufferManager.AudioBufferManaged audioBufferManaged = Audio.instance.audioBufferManager.addAudioBuffer(bufferId);
		if (audioBufferManaged.alId == Audio.ALBUFFERID_NONE) {
	        // nope, generate al buffer
			int[] bufferIdArray = new int[1];
			Audio.al.alGenBuffers(1, bufferIdArray, 0);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				System.out.println("Audio sound: '" + id + "': Could not generate buffer");
				return false;
			}
			alBufferId = bufferIdArray[0];

			// set up al id in audio buffer managed
			audioBufferManaged.setAlId(alBufferId);

			// decode Audio sound
			int format = -1;
			int frequency = -1;

			// sounds should have a maximum length of 1 min, everything else should be handled as stream
			ByteBuffer data = ByteBuffer.allocate(2 * 2 * 10 * 60 * 44100);
			JOrbisDecoder decoder = new JOrbisDecoder();
			try {
				// decode ogg vorbis
				decoder.openFile(pathName, fileName);
				System.out.println(
					"Audio sound: '" +
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
						System.out.println("Audio sound: '" + id + "': Unsupported number of channels");
				}
				if (decoder.readFromStream(data) == 0) throw new AudioDecoderException("no audio data was decoded");
				System.out.println(
					"Audio sound: '" +
					id + "' with length " +
					(float)data.remaining() / 2.0f / (float)decoder.getChannels() / (float)decoder.getSampleRate() +
					" seconds"
				);
			} catch (IOException ioe) {
				System.out.println("Audio sound: '" + id + "': " + ioe.getMessage());
				dispose();
				return false;
			} catch (AudioDecoderException ade) {
				System.out.println("Audio sound: '" + id + "': " + ade.getMessage());
				dispose();
				return false;
			} finally {
				decoder.close();
			}

			// check for valid format and frequency
			if (format == -1 || frequency == -1) {
				System.out.println("Audio sound: '" + id + "': Format or frequency invalid");
				dispose();
				return false;
			}

			// upload to al
			Audio.al.alBufferData(alBufferId, format, data, data.remaining(), frequency);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				System.out.println("Audio sound: '" + id + "': Could not upload buffer data");
				dispose();
				return false;			
			}
		} else {
			alBufferId = audioBufferManaged.alId;
		}

		// create source
		int[] sourceIds = new int[1];
		Audio.al.alGenSources(1, sourceIds, 0);
		if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
			System.out.println("Audio sound: '" + id + "': Could not generate source");
			dispose();
			return false;			
		}

		// have our source id
		alSourceId = sourceIds[0];

		// initiate sound properties
		Audio.al.alSourcei (alSourceId, AL.AL_BUFFER, alBufferId);
		update();

		initiated = true;

		// yay
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.audio.AudioEntity#pulse()
	 */
	protected void update() {
		// update sound properties
		Audio.al.alSourcef(alSourceId, AL.AL_PITCH, pitch);
		Audio.al.alSourcef(alSourceId, AL.AL_GAIN, gain);
		Audio.al.alSourcefv(alSourceId, AL.AL_POSITION, sourcePosition.getArray(), 0);
		Audio.al.alSourcefv(alSourceId, AL.AL_DIRECTION, sourceDirection.getArray(), 0);
		Audio.al.alSourcefv(alSourceId, AL.AL_VELOCITY, sourceVelocity.getArray(), 0);
		Audio.al.alSourcei(alSourceId, AL.AL_LOOPING, looping?AL.AL_TRUE:AL.AL_FALSE);
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
				System.out.println("Audio sound: '" + id + "': Could not delete source");
			}
			alSourceId = Audio.ALSOURCEID_NONE;
		}
		if (alBufferId != Audio.ALBUFFERID_NONE &&
			Audio.instance.audioBufferManager.removeAudioBuffer(bufferId) == true) {
			//
			Audio.al.alDeleteBuffers(1, new int[]{alBufferId}, 0);
			if (Audio.al.alGetError() != AL.AL_NO_ERROR) {
				System.out.println("Audio sound: '" + id + "': Could not delete buffers");
			}
			alBufferId = Audio.ALBUFFERID_NONE;
		}
		initiated = false;
	}

}

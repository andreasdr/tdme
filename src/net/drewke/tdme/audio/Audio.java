package net.drewke.tdme.audio;

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

/**
 * Interface to TDME audio methods
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Audio {

	protected final static int ALBUFFERID_NONE = -1;
	protected final static int ALSOURCEID_NONE = -1;

	protected static Audio instance = null;
	private static HashMap<String, AudioEntity> audioEntities = new HashMap<String, AudioEntity>();
	protected AudioBufferManager audioBufferManager = new AudioBufferManager();

	protected static AL al = null;

	protected Vector3 listenerPosition = new Vector3();
	protected Vector3 listenerVelocity = new Vector3();
	protected Vector3 listenerOrientationAt = new Vector3(0.0f, 0.0f, -1.0f);
	protected Vector3 listenerOrientationUp = new Vector3(0.0f, 1.0f, 0.0f);

	/**
	 * @return audio singleton instance
	 */
	public static Audio getInstance() {
		if (Audio.instance == null) {
			Audio.instance = new Audio();
		}
		return Audio.instance;
	}

	/**
	 * Private constructor
	 */
	private Audio() {
		//
		ALut.alutInit();
		al = ALFactory.getAL();

		// init listener position
		update();
	}

	/**
	 * @return listener position
	 */
	public Vector3 getListenerPosition() {
		return listenerPosition;
	}

	/**
	 * @return listener velocity
	 */
	public Vector3 getListenerVelocity() {
		return listenerVelocity;
	}

	/**
	 * @return listener orientation at
	 */
	public Vector3 getListenerOrientationAt() {
		return listenerOrientationAt;
	}

	/**
	 * @return listener orientation up
	 */
	public Vector3 getListenerOrientationUp() {
		return listenerOrientationUp;
	}

	/**
	 * Returns an audio entity identified by given id
	 * @param id
	 * @return audio entity
	 */
	public AudioEntity getEntity(String id) {
		return audioEntities.get(id);
	}

	/**
	 * Adds an stream
	 * 	the only format supported by now is ogg vorbis
	 * @param id
	 * @param path name
	 * @param file name
	 * @return audio entity
	 */
	public AudioEntity addStream(String id, String pathName, String fileName) {
		AudioEntity stream = new AudioStream(
			id,
			pathName,
			fileName
		);
		if (stream.init() == true) {
			removeEntity(id);
			audioEntities.put(id, stream);
			return stream;
		} else {
			Console.println(
				"Audio stream: '" +
				id + "' failed"
			);
			return null;
		}
	}

	/**
	 * Adds an sound
	 * 	the only format supported by now is ogg vorbis
	 * @param id
	 * @param path name
	 * @param file name
	 * @return audio entity
	 */
	public AudioEntity addSound(String id, String pathName, String fileName) {
		AudioEntity sound = new Sound(
			id,
			pathName,
			fileName
		);
		if (sound.init() == true) {
			removeEntity(id);
			audioEntities.put(id, sound);
			return sound;
		} else {
			Console.println(
				"Audio sound: '" +
				id + "' failed"
			);
			return null;
		}
	}

	/**
	 * Removes an audio entity
	 * @param id
	 */
	public void removeEntity(String id) {
		AudioEntity audioEntity = audioEntities.get(id);
		if (audioEntity != null) {
			audioEntity.stop();
			audioEntity.dispose();
			audioEntities.remove(audioEntity.getId());
		}
	}

	/**
	 * Clears all audio entities
	 */
	public void reset() {
		// determine keys to remove
		ArrayList<String> keys = new ArrayList<String>();
		for(String key: audioEntities.getKeysIterator()) {
			keys.add(key);
		}

		// remove entities
		for (String key: keys) {
			removeEntity(key);
		}
	}

	/**
	 * Shuts the audio down
	 */
	public void shutdown() {
		reset();
		ALut.alutExit();
	}

	/**
	 * Update and transfer audio entity states to open AL
	 */
	public void update() {
		// update audio entities
		for (AudioEntity audioEntity: audioEntities.getValuesIterator()) {
			audioEntity.update();
		}

		// update listener position
		al.alListenerfv (AL.AL_POSITION, listenerPosition.getArray(), 0);
		al.alListenerfv (AL.AL_VELOCITY, listenerVelocity.getArray(), 0);
		float[] listenerOrientationAtArray = listenerOrientationAt.getArray();
		float[] listenerOrientationUpArray = listenerOrientationUp.getArray();
		float[] listenerOrientation = new float[] {
			listenerOrientationAtArray[0],
			listenerOrientationAtArray[1],
			listenerOrientationAtArray[2],
			listenerOrientationUpArray[0],
			listenerOrientationUpArray[1],
			listenerOrientationUpArray[2]
		};
		al.alListenerfv (AL.AL_ORIENTATION, listenerOrientation, 0);
	}

}

package net.drewke.tdme.audio;

import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;

/**
 * Audio buffer manager
 * @author Andreas Drewke
 * @version $Id$
 */
public final class AudioBufferManager {

	/**
	 * Managed audio buffer entity
	 * @author Andreas Drewke
	 */
	protected class AudioBufferManaged {
		String id;
		int alId;
		int referenceCounter;

		/**
		 * Protected constructor
		 * @param id
		 * @param Open AL id
		 * @param referenceCounter
		 */
		private AudioBufferManaged(String id, int alId) {
			this.id = id;
			this.alId = alId;
			this.referenceCounter = 0;
		}

		/**
		 * @return audio buffer id
		 */
		protected String getId() {
			return id;
		}

		/**
		 * @return Open AL id
		 */
		protected int getAlId() {
			return alId;
		}

		/**
		 * Set up Open AL audio buffer id
		 * @param al Id
		 */
		protected void setAlId(int alId) {
			this.alId = alId;
		}

		/**
		 * @return reference counter
		 */
		protected int getReferenceCounter() {
			return referenceCounter;
		}

		/**
		 * decrement reference counter
		 * @return if reference counter = 0
		 */
		private boolean decrementReferenceCounter() {
			referenceCounter--;
			return referenceCounter == 0;
		}

		/**
		 * increment reference counter
		 */
		private void incrementReferenceCounter() {
			referenceCounter++;
		}

	}

	private HashMap<String, AudioBufferManaged> audioBuffers;

	/**
	 * Protected constructor
	 */
	protected AudioBufferManager() {
		audioBuffers = new HashMap<String, AudioBufferManaged>(); 
	}

	/**
	 * Adds a audio buffer to manager / open al stack
	 * @param id
	 * @param int audio buffer al id
	 * @return audio buffer managed
	 */
	protected AudioBufferManaged addAudioBuffer(String id) {
		// check if we already manage this texture
		AudioBufferManaged audioBufferManaged = audioBuffers.get(id);
		if (audioBufferManaged != null) {
			//
			audioBufferManaged.incrementReferenceCounter();

			// yep, return open gl id
			return audioBufferManaged;
		}

		// not yet, create managed audio buffer with no AL id attached yet
		audioBufferManaged = new AudioBufferManaged(
			id,
			Audio.ALBUFFERID_NONE
		);
		audioBufferManaged.incrementReferenceCounter();

		// add it to our audioBuffers
		audioBuffers.put(
			audioBufferManaged.getId(),
			audioBufferManaged
		);

		// return open gl id
		return audioBufferManaged;
	}

	/**
	 * Removes a texture from manager / open gl stack
	 * @param id
	 * @return true if caller has to remove the audio buffer from open AL
	 */
	protected boolean removeAudioBuffer(String id) {
		AudioBufferManaged audioBufferManaged = audioBuffers.get(id);
		if (audioBufferManaged != null) {
			if (audioBufferManaged.decrementReferenceCounter()) {
				// remove from our list
				audioBuffers.remove(id);
				// report to called that this audio buffer can be removed
				return true;
			} else {
				return false;
			}
		}
		// should never happen
		Console.println("Warning: audio buffer not loaded by audio buffer manager");
		return false;
	}

}

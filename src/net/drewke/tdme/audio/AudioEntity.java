package net.drewke.tdme.audio;

import net.drewke.tdme.math.Vector3;

/**
 * Audio Entity Class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class AudioEntity {

	protected String id;
	protected boolean looping = false;
	protected boolean fixed = false;
	protected float pitch = 1.0f;
	protected float gain = 1.0f;
	protected Vector3 sourcePosition = new Vector3();
	protected Vector3 sourceDirection = new Vector3();
	protected Vector3 sourceVelocity = new Vector3();

	/**
	 * Protected constructor
	 * @param id
	 */
	protected AudioEntity(String id) {
		this.id = id;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return if sound will be looped
	 */
	public boolean isLooping() {
		return looping;
	}

	/**
	 * @return if sound will be looped
	 */
	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	/**
	 * @return fixed, means the sound will always played no matter where the position and listener is located
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * Set this entity fixed, means the sound will always played no matter where the position and listener is located
	 * @param fixed
	 */
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	/**
	 * @return pitch
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * Set up pitch
	 * @param pitch
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return gain
	 */
	public float getGain() {
		return gain;
	}

	/**
	 * Set up gain
	 * @param gain
	 */
	public void setGain(float gain) {
		this.gain = gain;
	}

	/**
	 * @return source position
	 */
	public Vector3 getSourcePosition() {
		return sourcePosition;
	}

	/**
	 * @return source direction
	 */
	public Vector3 getSourceDirection() {
		return sourceDirection;
	}

	/**
	 * @return source velocity
	 */
	public Vector3 getSourceVelocity() {
		return sourceVelocity;
	}

	/**
	 * @return if stream is playing
	 */
	public abstract boolean isPlaying();

	/**
	 * Rewinds this audio entity
	 */
	public abstract void rewind();

	/**
	 * Plays this audio entity
	 */
	public abstract void play();

	/**
	 * Pauses this audio entity
	 */
	public abstract void pause();

	/**
	 * Stops this audio entity
	 */
	public abstract void stop();

	/**
	 * Initiates this OpenAL entity to OpenAl
	 */
	protected abstract boolean initialize();

	/**
	 * Commits properties to OpenAl
	 */
	protected abstract void update();

	/**
	 * Dispose this entity from OpenAL
	 */
	protected abstract void dispose();

}

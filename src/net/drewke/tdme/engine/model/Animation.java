package net.drewke.tdme.engine.model;

import net.drewke.tdme.math.Matrix4x4;

/**
 * AnimationSetup
 * @author andreas.drewke
 * @version $Id$
 */
public final class Animation {

	private int frames;
	private Matrix4x4[] transformationsMatrices;

	/**
	 * Public constructor
	 * @param frames
	 */
	public Animation(int frames) {
		this.frames = frames;
		transformationsMatrices = new Matrix4x4[this.frames];
		for(int i = 0; i < frames; i++) {
			transformationsMatrices[i] = new Matrix4x4().identity();
		}
	}

	/**
	 * @return number of frames
	 */
	public int getFrames() {
		return frames;
	}

	/**
	 * Returns transformation matrices
	 * @return transformation matrices
	 */
	public Matrix4x4[] getTransformationsMatrices() {
		return transformationsMatrices;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "AnimationSetup [frames=" + frames + ", transformationsMatrices=" + transformationsMatrices + "]";
	}

}
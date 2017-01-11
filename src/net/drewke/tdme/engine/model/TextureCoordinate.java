package net.drewke.tdme.engine.model;

import java.util.Arrays;

import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;

/**
 * Class representing a UV data
 * @author andreas.drewke
 * @version $Id$
 */
public final class TextureCoordinate {

	private float data[];

	/**
	 * Public constructor
	 * @param texture coordinate
	 */
	public TextureCoordinate(TextureCoordinate textureCoordinate) {
		data = new float[2];
		data[0] = textureCoordinate.data[0];
		data[1] = textureCoordinate.data[1];
	}

	/**
	 * Public constructor
	 * @param float array containing u, v values
	 */
	public TextureCoordinate(float[] uv) {
		data = new float[2];
		System.arraycopy(uv, 0, data, 0, Math.min(uv.length, data.length));
	}

	/**
	 * Public constructor
	 * @param u
	 * @param v
	 */
	public TextureCoordinate(float u, float v) {
		data = new float[2];
		data[0] = u;
		data[1] = 1.0f - v;
	}

	/**
	 * @return U
	 */
	public float getU() {
		return data[0];
	}

	/**
	 * @return V
	 */
	public float getV() {
		return data[1];
	}

	/**
	 * @return texture data as array
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * Clones the texture coordinate
	 * @return new texture coordinate
	 */
	public TextureCoordinate clone() {
		return new TextureCoordinate(
			data[0],
			1.0f - data[1] 
		);
	}

	/**
	 * Compares this texture coordinate with given texture coordinate
	 * @return equality
	 */
	public boolean equals(TextureCoordinate textureCoordinate) {
		return
			(this == textureCoordinate) ||
			(Math.abs(data[0] - textureCoordinate.data[0]) < MathTools.EPSILON &&
			Math.abs(data[1] - textureCoordinate.data[1]) < MathTools.EPSILON);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return Arrays.toString(data);
	}

}
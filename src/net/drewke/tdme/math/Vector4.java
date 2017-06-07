package net.drewke.tdme.math;

import java.util.Arrays;

/**
 * Vector 4 class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Vector4 {

	protected float data[];

	/**
	 * Public constructor
	 */
	public Vector4() {
		data = new float[4];
		data[0] = 0f;
		data[1] = 0f;
		data[2] = 0f;
		data[3] = 0f;
	}

	/**
	 * Public constructor
	 * @param vector v
	 * @param weight
	 */
	public Vector4(Vector3 v, float w) {
		data = new float[4];
		data[0] = v.data[0];
		data[1] = v.data[1];
		data[2] = v.data[2];
		data[3] = w;
	}

	/**
	 * Public constructor
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Vector4(float x, float y, float z, float w) {
		data = new float[4];
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
	}

	/**
	 * Public constructor
	 * @param float array containing x,y,z,w values
	 */
	public Vector4(float[] v) {
		data = new float[4];
		data[0] = v[0];
		data[1] = v[1];
		data[2] = v[2];
		data[3] = v[3];
	}

	/**
	 * Set up vector
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 * @return this vector
	 */
	public Vector4 set(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
		return this;
	}

	/**
	 * Set up vector
	 * @param float array containing x,y,z,w values
	 * @return this vector
	 */
	public Vector4 set(float[] v) {
		data[0] = v[0];
		data[1] = v[1];
		data[2] = v[2];
		data[3] = v[3];
		return this;
	}

	/**
	 * Set up vector
	 * @param v
	 * @return this vector
	 */
	public Vector4 set(Vector4 v) {
		data[0] = v.data[0];
		data[1] = v.data[1];
		data[2] = v.data[2];
		data[3] = v.data[3];
		return this;
	}

	/**
	 * Set up vector
	 * @param vector 3
	 * @param w 
	 * @return this vector
	 */
	public Vector4 set(Vector3 v, float w) {
		data[0] = v.data[0];
		data[1] = v.data[1];
		data[2] = v.data[2];
		data[3] = w;
		return this;
	}

	/**
	 * @return x
	 */
	public float getX() {
		return data[0];
	}

	/**
	 * set X
	 * @param x
	 */
	public void setX(float x) {
		data[0] = x;
	}

	/**
	 * @return y
	 */
	public float getY() {
		return data[1];
	}

	/**
	 * set Y
	 * @param y
	 */
	public void setY(float y) {
		data[1] = y;
	}

	/**
	 * @return z
	 */
	public float getZ() {
		return data[2];
	}

	/**
	 * Set Z
	 * @param z
	 */
	public void setZ(float z) {
		data[2] = z;
	}

	/**
	 * @return w
	 */
	public float getW() {
		return data[3];
	}

	/**
	 * Set W
	 * @param w
	 */
	public void setW(float w) {
		data[3] = w;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector4 scale(float scale) {
		data[0]*= scale;
		data[1]*= scale;
		data[2]*= scale;
		data[3]*= scale;
		return this;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector4 scale(Vector4 scale) {
		data[0]*= scale.data[0];
		data[1]*= scale.data[1];
		data[2]*= scale.data[2];
		data[3]*= scale.data[3];
		return this;
	}

	/**
	 * @return vector as array
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * Clones the vector
	 * @return new cloned vector
	 */
	public Vector4 clone() {
		return new Vector4(
			data
		);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return Arrays.toString(data);
	}

}

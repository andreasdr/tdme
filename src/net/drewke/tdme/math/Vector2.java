package net.drewke.tdme.math;

import java.util.Arrays;

/**
 * Vector 2 class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Vector2 {

	protected float data[];

	/**
	 * Public constructor
	 */
	public Vector2() {
		data = new float[2];
		data[0] = 0f;
		data[1] = 0f;
	}

	/**
	 * Public constructor
	 * @param x
	 * @param y
	 */
	public Vector2(float x, float y) {
		data = new float[2];
		data[0] = x;
		data[1] = y;
	}

	/**
	 * Public constructor
	 * @param float array containing x,y values
	 */
	public Vector2(float[] v) {
		data = new float[2];
		data[0] = v[0];
		data[1] = v[1];
	}

	/**
	 * Public constructor
	 * @param vector 2
	 */
	public Vector2(Vector2 v) {
		data = new float[2];
		data[0] = v.data[0];
		data[1] = v.data[1];
	}

	/**
	 * Set up vector
	 * @param x
	 * @param y
	 * @return this vector
	 */
	public Vector2 set(float x, float y) {
		data[0] = x;
		data[1] = y;
		return this;
	}

	/**
	 * Set up vector
	 * @param float array containing x,y values
	 * @return this vector
	 */
	public Vector2 set(float[] v) {
		data[0] = v[0];
		data[1] = v[1];
		return this;
	}

	/**
	 * Set up vector
	 * @param v
	 * @return this vector
	 */
	public Vector2 set(Vector2 v) {
		data[0] = v.data[0];
		data[1] = v.data[1];
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
	 * Adds a vector
	 * @param v
	 * @return this vector
	 */
	public Vector2 add(Vector2 v) {
		data[0]+= v.data[0];
		data[1]+= v.data[1];
		return this;
	}

	/**
	 * Subtracts a vector
	 * @param v
	 * @return this vector
	 */
	public Vector2 sub(Vector2 v) {
		data[0]-= v.data[0];
		data[1]-= v.data[1];
		return this;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector2 scale(float scale) {
		data[0]*= scale;
		data[1]*= scale;
		return this;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector2 scale(Vector2 scale) {
		data[0]*= scale.data[0];
		data[1]*= scale.data[1];
		return this;
	}

	/**
	 * @return vector as array
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return Arrays.toString(data);
	}

}
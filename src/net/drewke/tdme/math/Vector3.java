package net.drewke.tdme.math;

import java.util.Arrays;

/**
 * Vector3 class
 * @author andreas.drewke
 * @version $Id$
 */
public final class Vector3 {

	protected float data[];

	/**
	 * Public constructor
	 */
	public Vector3() {
		data = new float[3];
		Arrays.fill(data, 0.0f);
	}

	/**
	 * Public constructor
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3(float x, float y, float z) {
		data = new float[3];
		data[0] = x;
		data[1] = y;
		data[2] = z;
	}

	/**
	 * Public constructor
	 * @param float array containing x,y,z values
	 */
	public Vector3(float[] v) {
		data = new float[3];
		System.arraycopy(v, 0, data, 0, Math.min(v.length, data.length));
	}

	/**
	 * Public constructor
	 * @param float array containing x,y,z values
	 */
	public Vector3(Vector3 v) {
		data = new float[3];
		System.arraycopy(v.data, 0, data, 0, Math.min(v.data.length, data.length));
	}

	/**
	 * Set up vector
	 * @param x
	 * @return this vector
	 */
	public Vector3 set(float x, float y, float z) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		return this;
	}

	/**
	 * Set up vector
	 * @param float array containing x,y,z values
	 * @return this vector
	 */
	public Vector3 set(float[] v) {
		System.arraycopy(v, 0, data, 0, Math.min(v.length, data.length));
		return this;
	}

	/**
	 * Set up vector
	 * @param v
	 * @return this vector
	 */
	public Vector3 set(Vector3 v) {
		System.arraycopy(v.data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * @return x
	 */
	public float getX() {
		return data[0];
	}

	/**
	 * Set X
	 * @param x
	 */
	public void setX(float x) {
		data[0] = x;
	}

	/**
	 * add to x component
	 * @param x
	 * @return this vector
	 */
	public Vector3 addX(float x) {
		data[0]+= x;
		return this;
	}

	/**
	 * sub from x component
	 * @param x
	 * @return this vector
	 */
	public Vector3 subX(float x) {
		data[0]-= x;
		return this;
	}

	/**
	 * @return y
	 */
	public float getY() {
		return data[1];
	}

	/**
	 * Set Y
	 * @param y
	 * @return this vector
	 */
	public Vector3 setY(float y) {
		data[1] = y;
		return this;
	}

	/**
	 * add to y component
	 * @param y
	 * @return this vector
	 */
	public Vector3 addY(float y) {
		data[1]+= y;
		return this;
	}

	/**
	 * sub from y component
	 * @param y
	 * @return this vector
	 */
	public Vector3 subY(float y) {
		data[1]-= y;
		return this;
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
	 * @return this vector
	 */
	public Vector3 setZ(float z) {
		data[2] = z;
		return this;
	}

	/**
	 * add to z component
	 * @param z
	 * @return this vector
	 */
	public Vector3 addZ(float z) {
		data[2]+= z;
		return this;
	}

	/**
	 * sub from z component
	 * @param z
	 * @return this vector
	 */
	public Vector3 subZ(float z) {
		data[2]-= z;
		return this;
	}

	/**
	 * @return vector as array
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * Compute the cross product of vector v1 and v2
	 * @param v1
	 * @param v2
	 * @return cross product vector of v1 and v2
	 */
	public static Vector3 computeCrossProduct(Vector3 v1, Vector3 v2) {
		return new Vector3(
			(v1.data[1] * v2.data[2]) - (v1.data[2] * v2.data[1]),
			(v1.data[2] * v2.data[0]) - (v1.data[0] * v2.data[2]),
			(v1.data[0] * v2.data[1]) - (v1.data[1] * v2.data[0])
		);
	}

	/**
	 * Compute the cross product of vector v1 and v2
	 * @param v1
	 * @param v2
	 * @param destination vector
	 * @return destination vector
	 */
	public static Vector3 computeCrossProduct(Vector3 v1, Vector3 v2, Vector3 dest) {
		dest.set(
			(v1.data[1] * v2.data[2]) - (v1.data[2] * v2.data[1]),
			(v1.data[2] * v2.data[0]) - (v1.data[0] * v2.data[2]),
			(v1.data[0] * v2.data[1]) - (v1.data[1] * v2.data[0])
		);
		return dest;
	}

	/**
	 * Compute the dot product of vector v1 and v2
	 * @param v1
	 * @param v2
	 * @return Vector3
	 */
	public static float computeDotProduct(Vector3 v1, Vector3 v2) {
		return 
			(v1.data[0] * v2.data[0]) +
			(v1.data[1] * v2.data[1]) +
			(v1.data[2] * v2.data[2]);
	}

	/**
	 * @return the vectors length
	 */
	public float computeLength() {
		return (float)Math.sqrt(
			(data[0] * data[0])	+ (data[1] * data[1]) + (data[2] * data[2])
		);
	}

	/**
	 * @return the vectors length squared
	 */
	public float computeLengthSquared() {
		return
			(data[0] * data[0])	+ (data[1] * data[1]) + (data[2] * data[2]);
	}

	/**
	 * Computes angle between a and b from 0..180
	 * @param vector a, must be normalized
	 * @param vector b, must be normalized
	 * @return
	 */
	public static float computeAngle(Vector3 a, Vector3 b) {
		return (float)(
			180d /
			Math.PI *
			Math.acos(
				Vector3.computeDotProduct(a, b)
			)
		);
	}

	/**
	 * Computes angle between a and b 
	 * @param vector a, must be normalized
	 * @param vector b, must be normalized
	 * @param plane n normal where a and b live in, must be normalized
	 * @return
	 */
	public static float computeAngle(Vector3 a, Vector3 b, Vector3 n) {
		float angle = Vector3.computeAngle(a, b);
		float sign = MathTools.sign(Vector3.computeDotProduct(n, Vector3.computeCrossProduct(a, b)));
		if (Float.isNaN(sign) == true) sign = 1.0f;
		return ((angle * sign) + 360.0f) % 360.0f;
	}

	/**
	 * Normalize the vector
	 * @return this vector
	 */
	public Vector3 normalize() {
		float length = computeLength();
		data[0] /= length;
		data[1] /= length;
		data[2] /= length;
		return this;
	}

	/**
	 * Computes a orthogonal vector from this vector
	 * @param destination vector
	 * @return destination vector
	 */
	public Vector3 computeOrthogonalVector(Vector3 dest)  {
		if (Math.abs(data[0]) > MathTools.EPSILON) {
			dest.data[1] = data[0];
			dest.data[2] = ((-2 * data[0] * data[1] * data[2] + 2 * data[0] * data[2]) / (2 * (data[2] * data[2] + data[0] * data[0])));
			dest.data[0] = ((-data[0] * data[1] - data[2] * dest.data[2]) / data[0]);
		} else
		if (Math.abs(data[1]) > MathTools.EPSILON) {
			dest.data[2] = data[1];
			dest.data[0] = ((-2 * data[0] * data[1] * data[2] + 2 * data[0] * data[1]) / (2 * (data[1] * data[1] + data[0] * data[0])));
			dest.data[1] = ((-data[2] * data[1] - data[0] * dest.data[0]) / data[1]);
		} else
		if (Math.abs(data[2]) > MathTools.EPSILON) {
			dest.data[0] = data[2];
			dest.data[1] = ((-2 * data[0] * data[1] * data[2] + 2 * data[1] * data[2]) / (2 * (data[2] * data[2] + data[1] * data[1])));
			dest.data[2] = ((-data[0] * data[2] - data[1] * dest.data[1]) / data[2]);
		}
		return dest;
	}

	/**
	 * Adds a vector
	 * @param v
	 * @return this vector
	 */
	public Vector3 add(Vector3 v) {
		data[0]+= v.data[0];
		data[1]+= v.data[1];
		data[2]+= v.data[2];
		return this;
	}

	/**
	 * Adds a float to each vector component
	 * @param v
	 * @return this vector
	 */
	public Vector3 add(float value) {
		data[0]+= value;
		data[1]+= value;
		data[2]+= value;
		return this;
	}

	/**
	 * Adds a vector
	 * @param v
	 * @return this vector 
	 */
	public Vector3 sub(Vector3 v) {
		data[0]-= v.data[0];
		data[1]-= v.data[1];
		data[2]-= v.data[2];
		return this;
	}

	/**
	 * Subtracts a float from each vector component
	 * @param v
	 * @return this vector
	 */
	public Vector3 sub(float value) {
		data[0]-= value;
		data[1]-= value;
		data[2]-= value;
		return this;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector3 scale(float scale) {
		data[0]*= scale;
		data[1]*= scale;
		data[2]*= scale;
		return this;
	}

	/**
	 * Scale this vector
	 * @param scale
	 * @return this vector 
	 */
	public Vector3 scale(Vector3 scale) {
		data[0]*= scale.data[0];
		data[1]*= scale.data[1];
		data[2]*= scale.data[2];
		return this;
	}

	/**
	 * Clones the vector
	 * @return new cloned vector
	 */
	public Vector3 clone() {
		return new Vector3(
			data
		);
	}

	/**
	 * Compares this vector with given vector
	 * @param vector v
	 * @return equality
	 */
	public boolean equals(Vector3 v) {
		return
			(this == v) ||
			(Math.abs(data[0] - v.data[0]) < MathTools.EPSILON &&
			Math.abs(data[1] - v.data[1]) < MathTools.EPSILON &&
			Math.abs(data[2] - v.data[2]) < MathTools.EPSILON);
	}

	/**
	 * Compares this vector with given vector
	 * @param vector v
	 * @param tolerance
	 * @return equality
	 * 
	 */
	public boolean equals(Vector3 v, float tolerance) {
		return
			(this == v) ||
			(Math.abs(data[0] - v.data[0]) < tolerance &&
			Math.abs(data[1] - v.data[1]) < tolerance &&
			Math.abs(data[2] - v.data[2]) < tolerance);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return Arrays.toString(data);
	}

}
package net.drewke.tdme.math;

import java.util.Arrays;

/**
 * Quaternion
 * @author Andreas Drewke
 * @see http://db-in.com/blog/2011/04/cameras-on-opengl-es-2-x/
 */
public final class Quaternion {

	protected float data[];
	protected float _data[];

	private Vector3 t;
	private Vector3 q;
	private Vector3 qxt;

	/**
	 * Public constructor
	 */
	public Quaternion() {
		data = new float[4];
		Arrays.fill(data, 0.0f);
		_data = new float[4];
		t = new Vector3();
		q = new Vector3();
		qxt = new Vector3();
	}

	/**
	 * P
	 * @param q
	 * @return
	 */
	public Quaternion(Quaternion q) {
		data = new float[4];
		_data = new float[4];
		t = new Vector3();
		this.q = new Vector3();
		qxt = new Vector3();
		System.arraycopy(q.data, 0, data, 0, data.length);
	}

	/**
	 * Public constructor
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion(float x, float y, float z, float w) {
		data = new float[4];
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
	}

	/**
	 * Set up this quaternion by components
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion set(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
		return this;
	}

	/**
	 * Public constructor
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion(Vector3 v, float w) {
		float[] vectorXYZ = v.getArray();
		data = new float[4];
		data[0] = vectorXYZ[0];
		data[1] = vectorXYZ[1];
		data[2] = vectorXYZ[2];
		data[3] = w;
	}

	/**
	 * Sets up this quaternion by quaternion q
	 * @param q
	 * @return
	 */
	public Quaternion set(Quaternion q) {
		System.arraycopy(q.data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * Set quaternion
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion set(Vector3 v, float w) {
		float[] vectorXYZ = v.getArray();
		data[0] = vectorXYZ[0];
		data[1] = vectorXYZ[1];
		data[2] = vectorXYZ[2];
		data[3] = w;
		return this;
	}

	/**
	 * Set up quaternion identity
	 * @return this quaternion
	 */
	public Quaternion identity() {
		data[0] = 0.0f;
		data[1] = 0.0f;
		data[2] = 0.0f;
		data[3] = 1.0f;
		return this;
	}

	/**
	 * Inverts this quaternion
	 * @return this quaternion
	 */
	public Quaternion invert() {
		data[0] *= -1.0f;
		data[1] *= -1.0f;
		data[2] *= -1.0f;
		return this;
	}

	/**
	 * Creates a rotation quaternion
	 * 
	 * @param angle
	 * @param axis
	 * @return this quaternion
	 */
	public Quaternion rotate(float angle, Vector3 v) {
		// converts the angle in degrees to radians.
		float radians = angle * 3.14159265f / 180.0f;

		// finds the Sin and Cosin for the half angle.
		float sin = (float)Math.sin(radians * 0.5);
		float cos = (float)Math.cos(radians * 0.5);
		
		// formula to construct a new quaternion based on direction and angle.
		float[] axisXYZ = v.getArray();
		data[0] = axisXYZ[0] * sin;
		data[1] = axisXYZ[1] * sin;
		data[2] = axisXYZ[2] * sin;
		data[3] = cos;

		//
		return this;
	}

	/**
	 * Normalize quaternion
	 */
	public Quaternion normalize() {
		float magnitude =
			(float)Math.sqrt(
				data[0] * data[0] +
				data[1] * data[1] +
				data[2] * data[2] +
				data[3] * data[3]
			);
		data[0] = data[0] / magnitude;
		data[1] = data[1] / magnitude;
		data[2] = data[2] / magnitude;
		data[3] = data[3] / magnitude;
		return this;
	}

	/**
	 * Multiplies this quaternion with quaternion q
	 * @param quaterion q
	 * @return this quaternion
	 */
	public Quaternion multiply(Quaternion q) {
		_data[0] = data[3] * q.data[0] + data[0] * q.data[3] + data[1] * q.data[2] - data[2] * q.data[1];
		_data[1] = data[3] * q.data[1] - data[0] * q.data[2] + data[1] * q.data[3] + data[2] * q.data[0];
		_data[2] = data[3] * q.data[2] + data[0] * q.data[1] - data[1] * q.data[0] + data[2] * q.data[3];
		_data[3] = data[3] * q.data[3] - data[0] * q.data[0] - data[1] * q.data[1] - data[2] * q.data[2];
		System.arraycopy(_data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * Multiplies this quaternion with quaternion q
	 * @param quaterion q
	 * @return this quaternion
	 */
	public Quaternion add(Quaternion q) {
		data[0]+= q.data[0];
		data[1]+= q.data[1];
		data[2]+= q.data[2];
		data[3]+= q.data[3];
		return this;
	}

	/**
	 * Scales this quaternion with given value
	 * @param value
	 * @return this quaternion
	 */
	public Quaternion scale(float value) {
		data[0]*= value;
		data[1]*= value;
		data[2]*= value;
		data[3]*= value;
		return this;
	}

	/**
	 * Multiplies a quaternion with given vector v
	 * @param vector v
	 * @param dest
	 * @return dest
	 */
	public Vector3 multiply(Vector3 v, Vector3 dest) {
		// t = 2 * cross(q.xyz, v)
		q.set(data);
		Vector3.computeCrossProduct(
			q,
			v,
			t
		).scale(2.0f);

		// v' = v + q.w * t + cross(q.xyz, t)
		Vector3.computeCrossProduct(q, t, qxt);
		dest.set(v);
		dest.add(qxt);
		dest.add(t.scale(data[3]));

		//
		return dest;
	}

	/**
	 * Computes a matrix from given
	 * @param destination matrix
	 * @return destination matrix  
	 */
	public Matrix4x4 computeMatrix(Matrix4x4 matrix) {
		matrix.set(
			1.0f - 2.0f * (data[1] * data[1] + data[2] * data[2]),
			2.0f * (data[0] * data[1] + data[2] * data[3]),
			2.0f * (data[0] * data[2] - data[1] * data[3]),
			0.0f,
			2.0f * (data[0] * data[1] - data[2] * data[3]),
			1.0f - 2.0f * (data[0] * data[0] + data[2] * data[2]),
			2.0f * (data[2] * data[1] + data[0] * data[3]),
			0.0f,
			2.0f * (data[0] * data[2] + data[1] * data[3]),
			2.0f * (data[1] * data[2] - data[0] * data[3]),
			1.0f - 2.0f * (data[0] * data[0] + data[1] * data[1]),
			0.0f,
			0.0f,
			0.0f,
			0.0f,
			1.0f
		);
		return matrix;
	}

	/**
	 * Returns array data
	 * @return array data
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

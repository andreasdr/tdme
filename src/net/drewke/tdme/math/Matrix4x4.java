package net.drewke.tdme.math;

import java.util.Arrays;

/**
 * 4x4 Matrix class
 * @author Andreas Drewke, Song Ho Ahn <song.ahn@gmail.com>
 * @version $Id$
 */
public final class Matrix4x4 {

	protected float data[];
	protected float _data[];
	protected Vector3 tmpVector3;

	/**
	 * Public constructor
	 */
	public Matrix4x4() {
		data = new float[16];
		Arrays.fill(data, 0.0f);
		_data = new float[16];
		tmpVector3 = new Vector3();
	}

	/**
	 * Public constructor
	 * @param float array m
	 */
	public Matrix4x4(float[] m) {
		data = new float[16];
		System.arraycopy(m, 0, data, 0, Math.min(m.length, data.length));
		_data = new float[16];
		tmpVector3 = new Vector3();
	}

	/**
	 * Public constructor
	 * @param matrix
	 */
	public Matrix4x4(Matrix4x4 matrix) {
		data = new float[16];
		System.arraycopy(matrix.data, 0, data, 0, data.length);
		_data = new float[16];
		tmpVector3 = new Vector3();
	}

	/**
	 * Public constructor
	 * @param row 0, column 0
	 * @param row 1, column 0
	 * @param row 2, column 0
	 * @param row 3, column 0
	 * @param row 0, column 1
	 * @param row 1, column 1
	 * @param row 2, column 1
	 * @param row 3, column 1
	 * @param row 0, column 2
	 * @param row 1, column 2
	 * @param row 2, column 2
	 * @param row 3, column 2
	 * @param row 0, column 3
	 * @param row 1, column 3
	 * @param row 2, column 3
	 * @param row 3, column 3
	 */
	public Matrix4x4(
		float r0c0, float r1c0, float r2c0, float r3c0,
		float r0c1, float r1c1, float r2c1, float r3c1,
		float r0c2, float r1c2, float r2c2, float r3c2,
		float r0c3, float r1c3, float r2c3, float r3c3) {
		//
		data = new float[16];
		_data = new float[16];
		tmpVector3 = new Vector3();
		set(
			r0c0, r1c0, r2c0, r3c0,
			r0c1, r1c1, r2c1, r3c1,
			r0c2, r1c2, r2c2, r3c2,
			r0c3, r1c3, r2c3, r3c3
		);
	}

	/**
	 * Set up matrix by values
	 * @param row 0, column 0
	 * @param row 1, column 0
	 * @param row 2, column 0
	 * @param row 3, column 0
	 * @param row 0, column 1
	 * @param row 1, column 1
	 * @param row 2, column 1
	 * @param row 3, column 1
	 * @param row 0, column 2
	 * @param row 1, column 2
	 * @param row 2, column 2
	 * @param row 3, column 2
	 * @param row 0, column 3
	 * @param row 1, column 3
	 * @param row 2, column 3
	 * @param row 3, column 3
	 * @return this matrix
	 */
	public Matrix4x4 set(
		float r0c0, float r1c0, float r2c0, float r3c0,
		float r0c1, float r1c1, float r2c1, float r3c1,
		float r0c2, float r1c2, float r2c2, float r3c2,
		float r0c3, float r1c3, float r2c3, float r3c3) {
		data[0] = r0c0;
		data[1] = r1c0;
		data[2] = r2c0;
		data[3] = r3c0;
		data[4] = r0c1; 
		data[5] = r1c1;
		data[6] = r2c1;
		data[7] = r3c1;
		data[8] = r0c2;
		data[9] = r1c2;
		data[10] = r2c2;
		data[11] = r3c2;
		data[12] = r0c3;
		data[13] = r1c3;
		data[14] = r2c3;
		data[15] = r3c3;
		return this;
	}

	/**
	 * Sets up this matrix by matrix m
	 * @param m
	 * @return this matrix
	 */
	public Matrix4x4 set(float[] m) {
		System.arraycopy(m, 0, data, 0, Math.min(m.length, data.length));
		return this;
	}

	/**
	 * Sets up this matrix by matrix m
	 * @param m
	 * @return
	 */
	public Matrix4x4 set(Matrix4x4 m) {
		System.arraycopy(m.data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * Get coordinate system axes
	 * @param x axis
	 * @param y axis
	 * @param z axis
	 * @return this matrix
	 */
	public Matrix4x4 getAxes(Vector3 xAxis, Vector3 yAxis, Vector3 zAxis) {
		// extract coordinate axes
		xAxis.set(data[0], data[1], data[2]);
		yAxis.set(data[4], data[5], data[6]);
		zAxis.set(data[8], data[9], data[10]);
		return this;
	}

	/**
	 * Set coordinate system axes
	 * @param x axis
	 * @param y axis
	 * @param z axis
	 * @return this matrix
	 */
	public Matrix4x4 setAxes(Vector3 xAxis, Vector3 yAxis, Vector3 zAxis) {
		data[0] = xAxis.data[0];
		data[1] = xAxis.data[1];
		data[2] = xAxis.data[2];
		data[3] = 0f;
		data[4] = yAxis.data[0];
		data[5] = yAxis.data[1];
		data[6] = yAxis.data[2];
		data[7] = 0f;
		data[8] = zAxis.data[0];
		data[9] = zAxis.data[1];
		data[10] = zAxis.data[2];
		data[11] = 0f;
		return this;
	}

	/**
	 * Get translation
	 * @param translation
	 * @return this matrix
	 */
	public Matrix4x4 getTranslation(Vector3 translation) {
		translation.set(
			data[12],
			data[13],
			data[14]
		);
		return this;
	}

	/**
	 * Set translation
	 * @param translation
	 * @return this matrix
	 */
	public Matrix4x4 setTranslation(Vector3 translation) {
		data[12] = translation.data[0];
		data[13] = translation.data[1];
		data[14] = translation.data[2];
		return this;
	}

	/**
	 * Get scale
	 * @param scale
	 * @return this matrix
	 */
	public Matrix4x4 getScale(Vector3 scale) {
		tmpVector3.set(data[0], data[1], data[2]);
		scale.data[0] = tmpVector3.computeLength();
		tmpVector3.set(data[4], data[5], data[6]);
		scale.data[1] = tmpVector3.computeLength();
		tmpVector3.set(data[8], data[9], data[10]);
		scale.data[2] = tmpVector3.computeLength();
		return this;
	}

	/**
	 * Get scale
	 * @param scale
	 * @return this matrix
	 */
	public Matrix4x4 setScale(Vector3 scale) {
		// x axis
		tmpVector3.set(data[0], data[1], data[2]);
		tmpVector3.normalize();
		tmpVector3.scale(scale.data[0]);
		data[0] = tmpVector3.data[0];
		data[1] = tmpVector3.data[1];
		data[2] = tmpVector3.data[2];
		// y axis
		tmpVector3.set(data[4], data[5], data[6]);
		tmpVector3.normalize();
		tmpVector3.scale(scale.data[1]);
		data[4] = tmpVector3.data[0];
		data[5] = tmpVector3.data[1];
		data[6] = tmpVector3.data[2];
		// z axis
		tmpVector3.set(data[8], data[9], data[10]);
		tmpVector3.normalize();
		tmpVector3.scale(scale.data[2]);
		data[8] = tmpVector3.data[0];
		data[9] = tmpVector3.data[1];
		data[10] = tmpVector3.data[2];
		// done
		return this;
	}

	/**
	 * Setup identity matrix
	 * @return this matrix
	 */
	public Matrix4x4 identity() {
	    data[0] = 1.0f;
	    data[1] = 0.0f;
	    data[2] = 0.0f;
	    data[3] = 0.0f;
	    data[4] = 0.0f;
	    data[5] = 1.0f;
	    data[6] = 0.0f;
	    data[7] = 0.0f;
	    data[8] = 0.0f;
	    data[9] = 0.0f;
	    data[10] = 1.0f;
	    data[11] = 0.0f;
	    data[12] = 0.0f;
	    data[13] = 0.0f;
	    data[14] = 0.0f;
	    data[15] = 1.0f;
	    return this;
	}

	/**
	 * Multiplies a vector3 with this matrix into destination vector
	 * @param vector 3
	 * @param destination vector 3
	 * @return vector 3
	 */
	public Vector3 multiply(Vector3 v, Vector3 dest) {
		return dest.set(
			v.data[0] * data[0] + v.data[1] * data[4] + v.data[2] * data[8] + data[12],
			v.data[0] * data[1] + v.data[1] * data[5] + v.data[2] * data[9] + data[13],
			v.data[0] * data[2] + v.data[1] * data[6] + v.data[2] * data[10] + data[14]
	    );
	}

	/**
	 * Multiplies a vector3 with this matrix ignoring translation
	 * @param vector 3
	 * @param destination vector 3
	 * @return vector 3 dest
	 */
	public Vector3 multiplyNoTranslation(Vector3 v, Vector3 dest) {
		return dest.set(
			v.data[0] * data[0] + v.data[1] * data[4] + v.data[2] * data[8],
			v.data[0] * data[1] + v.data[1] * data[5] + v.data[2] * data[9],
			v.data[0] * data[2] + v.data[1] * data[6] + v.data[2] * data[10]
	    );
	}

	/**
	 * Multiplies a vector4 with this matrix into destination vector
	 * @param vector 4
	 * @param destination vector4
	 * @return destination vector 4
	 */
	public Vector4 multiply(Vector4 v, Vector4 dest) {
		dest.set(
			v.data[0] * data[0] + v.data[1] * data[4] + v.data[2] * data[8] + v.data[3] * data[12],
			v.data[0] * data[1] + v.data[1] * data[5] + v.data[2] * data[9] + v.data[3] * data[13],
			v.data[0] * data[2] + v.data[1] * data[6] + v.data[2] * data[10] + v.data[3] * data[14],
			v.data[0] * data[3] + v.data[1] * data[7] + v.data[2] * data[11] + v.data[3] * data[15]
	    );
		return dest;
	}

	/**
	 * Multiplies this matrix with another matrix
	 * @param m
	 * @return this matrix
	 */
	public Matrix4x4 multiply(Matrix4x4 m) {
		_data[0] = data[0] * m.data[0]  + data[1] * m.data[4]  + data[2] * m.data[8]  + data[3] * m.data[12];
		_data[1] = data[0] * m.data[1]  + data[1] * m.data[5]  + data[2] * m.data[9]  + data[3] * m.data[13];
		_data[2] = data[0] * m.data[2]  + data[1] * m.data[6]  + data[2] * m.data[10] + data[3] * m.data[14];
		_data[3] = data[0] * m.data[3]  + data[1] * m.data[7]  + data[2] * m.data[11] + data[3] * m.data[15];
		_data[4] = data[4] * m.data[0]  + data[5] * m.data[4]  + data[6] * m.data[8]  + data[7] * m.data[12];
		_data[5] = data[4] * m.data[1]  + data[5] * m.data[5]  + data[6] * m.data[9]  + data[7] * m.data[13];
		_data[6] = data[4] * m.data[2]  + data[5] * m.data[6]  + data[6] * m.data[10]  + data[7] * m.data[14];
		_data[7] = data[4] * m.data[3]  + data[5] * m.data[7]  + data[6] * m.data[11]  + data[7] * m.data[15];
		_data[8] = data[8] * m.data[0]  + data[9] * m.data[4]  + data[10] * m.data[8] + data[11] * m.data[12];
		_data[9] = data[8] * m.data[1]  + data[9] * m.data[5]  + data[10] * m.data[9] + data[11] * m.data[13];
		_data[10] = data[8] * m.data[2]  + data[9] * m.data[6]  + data[10] * m.data[10] + data[11] * m.data[14];
		_data[11] = data[8] * m.data[3]  + data[9] * m.data[7]  + data[10] * m.data[11] + data[11] * m.data[15];
		_data[12] = data[12] * m.data[0] + data[13] * m.data[4] + data[14] * m.data[8] + data[15] * m.data[12];
		_data[13] = data[12] * m.data[1] + data[13] * m.data[5] + data[14] * m.data[9] + data[15] * m.data[13];
		_data[14] = data[12] * m.data[2] + data[13] * m.data[6] + data[14] * m.data[10] + data[15] * m.data[14];
		_data[15] = data[12] * m.data[3] + data[13] * m.data[7] + data[14] * m.data[11] + data[15] * m.data[15];

		// set up new matrix data
		System.arraycopy(_data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * Scales this matrix
	 * @param s
	 * @returns this matrix
	 */
	public Matrix4x4 scale(float s) {
		data[0]*= s;
		data[1]*= s;
		data[2]*= s;
		data[3]*= s;
		data[4]*= s;
		data[5]*= s;
		data[6]*= s;
		data[7]*= s;
		data[8]*= s;
		data[9]*= s;
		data[10]*= s;
		data[11]*= s;
		return this;
	}

	/**
	 * Scales this matrix by given vector
	 * @param v
	 * @return this matrix
	 */
	public Matrix4x4 scale(Vector3 v) {
		data[0] *= v.data[0];
		data[1] *= v.data[0];
		data[2] *= v.data[0];
		data[3] *= v.data[0];
		data[4] *= v.data[1];
		data[5] *= v.data[1];
		data[6] *= v.data[1];
		data[7] *= v.data[1];
		data[8] *= v.data[2];
		data[9] *= v.data[2];
		data[10] *= v.data[2];
		data[11] *= v.data[2];
		return this;
	}

	/**
	 * Sets up a translation matrix
	 * @param v
	 * @return this matrix
	 */
	public Matrix4x4 translate(Vector3 v) {
		data[12] += v.data[0] * data[0] + v.data[1] * data[4] + v.data[2] * data[8];
		data[13] += v.data[0] * data[1] + v.data[1] * data[5] + v.data[2] * data[9];
		data[14] += v.data[0] * data[2] + v.data[1] * data[6] + v.data[2] * data[10];
		return this;
	}

	/**
	 * Creates a rotation matrix
	 * @param angle
	 * @param vector v
	 * @return this matrix
	 */
	public Matrix4x4 rotate(float angle, Vector3 v) {
		set(
			new Quaternion().
			identity().
			rotate(angle, v).
			computeMatrix(this)
		);
		return this;
	}

	/**
	 * Transposes this matrix
	 * @return this matrix
	 */
	public Matrix4x4 transpose() {
		_data[0] = data[0];
		_data[1] = data[4];
		_data[2] = data[8];
		_data[3] = data[12];
		_data[4] = data[1];
		_data[5] = data[5];
		_data[6] = data[9];
		_data[7] = data[13];
		_data[8] = data[2];
		_data[9] = data[6];
		_data[10] = data[10];
		_data[11] = data[14];
		_data[12] = data[3];
		_data[13] = data[7];
		_data[14] = data[11];
		_data[15] = data[15];
		// set up new matrix data
		System.arraycopy(_data, 0, data, 0, data.length);
		return this;
	}

	/**
	 * Inverts the matrix
	 * 
	 * 	Taken from MESA GLU implementation
	 * 
	 *		Copyright (C) 1999-2007  Brian Paul   All Rights Reserved. 
	 * 		Permission is hereby granted, free of charge, to any person obtaining a
	 * 		copy of this software and associated documentation files (the "Software"),
	 * 		to 	deal in the Software without restriction, including without limitation
	 * 		the rights to use, copy, modify, merge, publish, distribute, sublicense,
	 * 		and/or sell copies of the Software, and to permit persons to whom the
	 * 		Software is furnished to do so, subject to the following conditions:
	 * 
	 * 		The above copyright notice and this permission notice shall be included
	 * 		in all copies or substantial portions of the Software.
	 * 
	 * 		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	 * 		OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	 * 		FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
	 * 		BRIAN PAUL BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
	 * 		AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	 * 		CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 *
	 * @return this matrix
	 */
	public Matrix4x4 invert() {
		_data[0] = data[5] * data[10] * data[15] - data[5] * data[11]
				* data[14] - data[9] * data[6] * data[15] + data[9] * data[7]
				* data[14] + data[13] * data[6] * data[11] - data[13] * data[7]
				* data[10];
		_data[4] = -data[4] * data[10] * data[15] + data[4] * data[11]
				* data[14] + data[8] * data[6] * data[15] - data[8] * data[7]
				* data[14] - data[12] * data[6] * data[11] + data[12] * data[7]
				* data[10];
		_data[8] = data[4] * data[9] * data[15] - data[4] * data[11] * data[13]
				- data[8] * data[5] * data[15] + data[8] * data[7] * data[13]
				+ data[12] * data[5] * data[11] - data[12] * data[7] * data[9];
		_data[12] = -data[4] * data[9] * data[14] + data[4] * data[10]
				* data[13] + data[8] * data[5] * data[14] - data[8] * data[6]
				* data[13] - data[12] * data[5] * data[10] + data[12] * data[6]
				* data[9];
		_data[1] = -data[1] * data[10] * data[15] + data[1] * data[11]
				* data[14] + data[9] * data[2] * data[15] - data[9] * data[3]
				* data[14] - data[13] * data[2] * data[11] + data[13] * data[3]
				* data[10];
		_data[5] = data[0] * data[10] * data[15] - data[0] * data[11]
				* data[14] - data[8] * data[2] * data[15] + data[8] * data[3]
				* data[14] + data[12] * data[2] * data[11] - data[12] * data[3]
				* data[10];
		_data[9] = -data[0] * data[9] * data[15] + data[0] * data[11]
				* data[13] + data[8] * data[1] * data[15] - data[8] * data[3]
				* data[13] - data[12] * data[1] * data[11] + data[12] * data[3]
				* data[9];
		_data[13] = data[0] * data[9] * data[14] - data[0] * data[10]
				* data[13] - data[8] * data[1] * data[14] + data[8] * data[2]
				* data[13] + data[12] * data[1] * data[10] - data[12] * data[2]
				* data[9];
		_data[2] = data[1] * data[6] * data[15] - data[1] * data[7] * data[14]
				- data[5] * data[2] * data[15] + data[5] * data[3] * data[14]
				+ data[13] * data[2] * data[7] - data[13] * data[3] * data[6];
		_data[6] = -data[0] * data[6] * data[15] + data[0] * data[7] * data[14]
				+ data[4] * data[2] * data[15] - data[4] * data[3] * data[14]
				- data[12] * data[2] * data[7] + data[12] * data[3] * data[6];
		_data[10] = data[0] * data[5] * data[15] - data[0] * data[7] * data[13]
				- data[4] * data[1] * data[15] + data[4] * data[3] * data[13]
				+ data[12] * data[1] * data[7] - data[12] * data[3] * data[5];
		_data[14] = -data[0] * data[5] * data[14] + data[0] * data[6]
				* data[13] + data[4] * data[1] * data[14] - data[4] * data[2]
				* data[13] - data[12] * data[1] * data[6] + data[12] * data[2]
				* data[5];
		_data[3] = -data[1] * data[6] * data[11] + data[1] * data[7] * data[10]
				+ data[5] * data[2] * data[11] - data[5] * data[3] * data[10]
				- data[9] * data[2] * data[7] + data[9] * data[3] * data[6];
		_data[7] = data[0] * data[6] * data[11] - data[0] * data[7] * data[10]
				- data[4] * data[2] * data[11] + data[4] * data[3] * data[10]
				+ data[8] * data[2] * data[7] - data[8] * data[3] * data[6];
		_data[11] = -data[0] * data[5] * data[11] + data[0] * data[7] * data[9]
				+ data[4] * data[1] * data[11] - data[4] * data[3] * data[9]
				- data[8] * data[1] * data[7] + data[8] * data[3] * data[5];
		_data[15] = data[0] * data[5] * data[10] - data[0] * data[6] * data[9]
				- data[4] * data[1] * data[10] + data[4] * data[2] * data[9]
				+ data[8] * data[1] * data[6] - data[8] * data[2] * data[5];

		//
		float determinant = data[0] * _data[0] + data[1] * _data[4] + data[2]
				* _data[8] + data[3] * _data[12];

		//
		if (determinant == 0f) {
			identity();
			return this;
		}

		//
		determinant = 1.0f / determinant;
		for (int i = 0; i < _data.length; i++)
			_data[i] = _data[i] * determinant;

		// set up new matrix data
		System.arraycopy(_data, 0, data, 0, data.length);

		// done
		return this;
	}

	/**
	 * Returns if this matrix equals m
	 * @param m
	 * @return equals
	 */
	public boolean equals(Matrix4x4 m) {
		return
			Math.abs(data[0] - m.data[0]) < MathTools.EPSILON &&
			Math.abs(data[1] - m.data[1]) < MathTools.EPSILON &&
			Math.abs(data[2] - m.data[2]) < MathTools.EPSILON &&
			Math.abs(data[3] - m.data[3]) < MathTools.EPSILON &&
			Math.abs(data[4] - m.data[4]) < MathTools.EPSILON &&
			Math.abs(data[5] - m.data[5]) < MathTools.EPSILON &&
			Math.abs(data[6] - m.data[6]) < MathTools.EPSILON &&
			Math.abs(data[7] - m.data[7]) < MathTools.EPSILON &&
			Math.abs(data[8] - m.data[8]) < MathTools.EPSILON &&
			Math.abs(data[9] - m.data[9]) < MathTools.EPSILON &&
			Math.abs(data[10] - m.data[10]) < MathTools.EPSILON &&
			Math.abs(data[11] - m.data[11]) < MathTools.EPSILON &&
			Math.abs(data[12] - m.data[12]) < MathTools.EPSILON &&
			Math.abs(data[13] - m.data[13]) < MathTools.EPSILON &&
			Math.abs(data[14] - m.data[14]) < MathTools.EPSILON &&
			Math.abs(data[15] - m.data[15]) < MathTools.EPSILON;
	}

	/**
	 * Returns array data
	 * @return array data
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * Clones this matrix
	 * @return new cloned matrix
	 */
	public Matrix4x4 clone() {
		return new Matrix4x4(
			data
		);
	}

	/**
	 * Interpolates between matrix 1 and matrix 2 by 0f<=t<=1f linearly 
	 * @param matrix 1
	 * @param matrix 2
	 * @param t
	 * @param destination matrix
	 * @return interpolated matrix
	 */
	public static Matrix4x4 interpolateLinear(Matrix4x4 m1, Matrix4x4 m2, float t, Matrix4x4 dest) {
		return dest.set(
			(m2.data[0] * t) + ((1f - t) * m1.data[0]),
			(m2.data[1] * t) + ((1f - t) * m1.data[1]),
			(m2.data[2] * t) + ((1f - t) * m1.data[2]),
			(m2.data[3] * t) + ((1f - t) * m1.data[3]),
			(m2.data[4] * t) + ((1f - t) * m1.data[4]),
			(m2.data[5] * t) + ((1f - t) * m1.data[5]),
			(m2.data[6] * t) + ((1f - t) * m1.data[6]),
			(m2.data[7] * t) + ((1f - t) * m1.data[7]),
			(m2.data[8] * t) + ((1f - t) * m1.data[8]),
			(m2.data[9] * t) + ((1f - t) * m1.data[9]),
			(m2.data[10] * t) + ((1f - t) * m1.data[10]),
			(m2.data[11] * t) + ((1f - t) * m1.data[11]),
			(m2.data[12] * t) + ((1f - t) * m1.data[12]),
			(m2.data[13] * t) + ((1f - t) * m1.data[13]),
			(m2.data[14] * t) + ((1f - t) * m1.data[14]),
			(m2.data[15] * t) + ((1f - t) * m1.data[15])
		);		
	}

	/**
	 * Compute Euler angles (rotation around x, y, z axes)
	 * @see https://github.com/erich666/GraphicsGems/tree/master/gemsiv/euler_angle
	 * 
	 * 		This code repository predates the concept of Open Source, and predates most licenses along such lines. 
	 * 		As such, the official license truly is:
	 * 
	 * 		EULA: The Graphics Gems code is copyright-protected. 
	 * 		In other words, you cannot claim the text of the code as your own and resell it. 
	 * 		Using the code is permitted in any program, product, or library, non-commercial or commercial. 
	 * 		Giving credit is not required, though is a nice gesture. 
	 * 		The code comes as-is, and if there are any flaws or problems with any Gems code, 
	 * 		nobody involved with Gems - authors, editors, publishers, or webmasters - are to be held responsible. 
	 * 		Basically, don't be a jerk, and remember that anything free comes with no guarantee.
	 * 
	 * @param euler
	 */
	public void computeEulerAngles(Vector3 euler) {
		float[] eulerXYZ = euler.getArray();

		// axes
		int axis0 = 0;
		int axis1 = 1;
		int axis2 = 2;

		// compute euler angles in radians
		float cy = (float)Math.sqrt(data[axis0 + 4 * axis0] * data[axis0 + 4 * axis0] + data[axis1 + 4 * axis0] * data[axis1 + 4 * axis0]);
		if (cy > 16f * MathTools.EPSILON) {
			eulerXYZ[0] = (float)(Math.atan2(data[axis2 + 4 * axis1], data[axis2 + 4 * axis2]));
			eulerXYZ[1] = (float)(Math.atan2(-data[axis2 + 4 * axis0], cy));
			eulerXYZ[2] = (float)(Math.atan2(data[axis1 + 4 * axis0], data[axis0 + 4 * axis0]));
		} else {
			eulerXYZ[0] = (float)(Math.atan2(-data[axis1 + 4 * axis2], data[axis1 + 4 * axis1]));
			eulerXYZ[1] = (float)(Math.atan2(-data[axis2 + 4 * axis0], cy));
			eulerXYZ[2] = 0f;
		}

	    // convert to degrees
	    euler.scale((float)(180d / Math.PI));
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return Arrays.toString(data);
	}

}
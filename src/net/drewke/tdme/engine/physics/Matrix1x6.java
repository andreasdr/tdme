/*********************************************************************************
 * This source code is based on                                                  *
 * ReactPhysics3D physics library, http://www.reactphysics3d.com                 *
 * Copyright (c) 2010-2015 Daniel Chappuis                                       *
 *********************************************************************************
 *                                                                               *
 * This software is provided 'as-is', without any express or implied warranty.   *
 * In no event will the authors be held liable for any damages arising from the  *
 * use of this software.                                                         *
 *                                                                               *
 * Permission is granted to anyone to use this software for any purpose,         *
 * including commercial applications, and to alter it and redistribute it        *
 * freely, subject to the following restrictions:                                *
 *                                                                               *
 * 1. The origin of this software must not be misrepresented; you must not claim *
 *    that you wrote the original software. If you use this software in a        *
 *    product, an acknowledgment in the product documentation would be           *
 *    appreciated but is not required.                                           *
 *                                                                               *
 * 2. Altered source versions must be plainly marked as such, and must not be    *
 *    misrepresented as being the original software.                             *
 *                                                                               *
 * 3. This notice may not be removed or altered from any source distribution.    *
 *                                                                               *
 ********************************************************************************/

package net.drewke.tdme.engine.physics;

import java.util.Arrays;

import net.drewke.tdme.math.Vector3;

/**
 * Matrix 1x6
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Matrix1x6 {

	// data
	protected float data[] = {0f, 0f, 0f, 0f, 0f, 0f};

	/**
	 * Protected constructor
	 */
	protected Matrix1x6() {}

	/**
	 * Set up matrix values
	 * @param value0
	 * @param value1
	 * @param value2
	 * @param value3
	 * @param value4
	 * @param value5
	 */
	protected void setValue(float value0, float value1, float value2, float value3, float value4, float value5) {
		this.data[0] = value0;
		this.data[1] = value1;
		this.data[2] = value2;
		this.data[3] = value3;
		this.data[4] = value4;
		this.data[5] = value5;
	}

	/**
	 * Set up matrix value at given idx
	 * @param idx
	 * @param value
	 */
	protected void setValue(int idx, float value) {
		data[idx] = value;
	}

	/**
	 * Set up vector as value
	 * @param this matrix start idx
	 * @param vector 3
	 */
	protected void setValue(int startIdx, Vector3 vector3) {
		System.arraycopy(vector3.getArray(), 0, data, startIdx, 3);
	}

	/**
	 * Multiplies this matrix 1x6 with given vector 6
	 * @param vector
	 * @return scalar product
	 */
	protected float multiply(Vector6 vector) {
		float[] vector6Array = vector.getArray();
		return
			data[0] * vector6Array[0] + data[1] * vector6Array[1] + data[2] * vector6Array[2] +
			data[3] * vector6Array[3] + data[4] * vector6Array[4] + data[5] * vector6Array[5];
	}

	/**
	 * Multiplies this 1x6 matrix with given matrix 6x6 into destination matrix
	 * @param matrix6x6 6x6
	 * @param destination
	 * @return destination matrix 1x6
	 */
	protected Matrix1x6 multiply(Matrix6x6 matrix6x6, Matrix1x6 dest) {
		float[] matrix6x6Data = matrix6x6.getArray();
		dest.data[0] =
			data[0] * matrix6x6Data[0 * 6 + 0] + data[1] * matrix6x6Data[1 * 6 + 0] +
			data[2] * matrix6x6Data[2 * 6 + 0] + data[3] * matrix6x6Data[3 * 6 + 0] +
			data[4] * matrix6x6Data[4 * 6 + 0] + data[5] * matrix6x6Data[5 * 6 + 0];
		dest.data[1] = 
			data[0] * matrix6x6Data[0 * 6 + 1] + data[1] * matrix6x6Data[1 * 6 + 1] +
			data[2] * matrix6x6Data[2 * 6 + 1] + data[3] * matrix6x6Data[3 * 6 + 1] +
			data[4] * matrix6x6Data[4 * 6 + 1] + data[5] * matrix6x6Data[5 * 6 + 1];
		dest.data[2] =
			data[0] * matrix6x6Data[0 * 6 + 2] + data[1] * matrix6x6Data[1 * 6 + 2] +
			data[2] * matrix6x6Data[2 * 6 + 2] + data[3] * matrix6x6Data[3 * 6 + 2] +
			data[4] * matrix6x6Data[4 * 6 + 2] + data[5] * matrix6x6Data[5 * 6 + 2];
		dest.data[3] =
			data[0] * matrix6x6Data[0 * 6 + 3] + data[1] * matrix6x6Data[1 * 6 + 3] +
			data[2] * matrix6x6Data[2 * 6 + 3] + data[3] * matrix6x6Data[3 * 6 + 3] +
			data[4] * matrix6x6Data[4 * 6 + 3] + data[5] * matrix6x6Data[5 * 6 + 3];
		dest.data[4] =
			data[0] * matrix6x6Data[0 * 6 + 4] + data[1] * matrix6x6Data[1 * 6 + 4] +
			data[2] * matrix6x6Data[2 * 6 + 4] + data[3] * matrix6x6Data[3 * 6 + 4] +
			data[4] * matrix6x6Data[4 * 6 + 4] + data[5] * matrix6x6Data[5 * 6 + 4];
		dest.data[5] =
			data[0] * matrix6x6Data[0 * 6 + 5] + data[1] * matrix6x6Data[1 * 6 + 5] +
			data[2] * matrix6x6Data[2 * 6 + 5] + data[3] * matrix6x6Data[3 * 6 + 5] +
			data[4] * matrix6x6Data[4 * 6 + 5] + data[5] * matrix6x6Data[5 * 6 + 5];
		return dest;
	}

	/**
	 * Get transpose
	 * @param destination vector 6
	 * @return destination vector 6
	 */
	protected Vector6 getTranspose(Vector6 dest) {
		System.arraycopy(data, 0, dest.data, 0, 6);
		return dest;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Matrix1x6 [" + Arrays.toString(data) + "]";
	}

}

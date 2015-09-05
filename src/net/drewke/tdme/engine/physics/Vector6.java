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
 * Vector6
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Vector6 {

	protected float data[] = {0f, 0f, 0f, 0f, 0f, 0f};

	/**
	 * Constructor
	 */
	protected Vector6() {
	}

	/**
	 * Fills the Vector with given value
	 * @param value
	 */
	protected void fill(float value) {
		Arrays.fill(data, value);
	}

	/**
	 * Set up vector from vector
	 * @param vector 6
	 * @return this vector
	 */
	protected Vector6 set(Vector6 vector6) {
		System.arraycopy(vector6.getArray(), 0, data, 0, data.length);
		return this;
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
	 * Adds this vector to given vector6 into dest vector6
	 * @param vector 6
	 * @param this vector 6
	 */
	protected Vector6 add(Vector6 vector6) {
		data[0]+= vector6.data[0];
		data[1]+= vector6.data[1];
		data[2]+= vector6.data[2];
		data[3]+= vector6.data[3];
		data[4]+= vector6.data[4];
		data[5]+= vector6.data[5];
		return this;
	}
	
	/**
	 * Subtracts given vector 6 from this vector 6
	 * @param vector 6
	 * @param this vector 6
	 */
	protected Vector6 sub(Vector6 vector6) {
		data[0]-= vector6.data[0];
		data[1]-= vector6.data[1];
		data[2]-= vector6.data[2];
		data[3]-= vector6.data[3];
		data[4]-= vector6.data[4];
		data[5]-= vector6.data[5];
		return this;
	}

	/**
	 * Scales this vector6 with given float value
	 * @param value
	 * @return this vector 6 
	 */
	protected Vector6 scale(float value) {
		data[0]*= value;
		data[1]*= value;
		data[2]*= value;
		data[3]*= value;
		data[4]*= value;
		data[5]*= value;
		return this;
	}

	/**
	 * @return data array
	 */
	protected float[] getArray() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Vector6 [data=" + Arrays.toString(data) + "]";
	}

}

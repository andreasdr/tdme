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

/**
 * Dynamic Vector
 * @author Andreas Drewke
 * @versioN $Id$
 */
public final class DynamicVector {

	protected float data[];

	/**
	 * Protected constructor
	 */
	protected DynamicVector(int size) {
		data = new float[size];
	}

	/**
	 * Change size
	 * @param size
	 */
	protected void setSize(int size) {
		data = new float[size];
	}

	/**
	 * Set value
	 * @param idx
	 * @param value
	 */
	protected void setValue(int idx, float value) {
		data[idx] = value;
	}

	/**
	 * Retrieves value
	 * @param idx
	 * @return value
	 */
	protected float getValue(int idx) {
		return data[idx];
	}

	/**
	 * Scales this vector with given value into dest vector
	 * @param value
	 * @param dest
	 */
	protected void scale(float value, DynamicVector dest) {
		if (data.length != dest.data.length) {
			dest.setSize(data.length);
		}
		for (int i = 0; i < data.length; i++) {
			dest.data[i] = data[i] * value;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "DynamicVector [data=" + Arrays.toString(data) + "]";
	}

}

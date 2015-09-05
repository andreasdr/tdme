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

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;
import net.drewke.tdme.utils.Pool;

/**
 * Contact cache manager
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ContactCache {

	/**
	 * Contact cache info
	 */
	protected static class ContactCacheInfo {
		protected Key key;
		protected RigidBody rb1;
		protected RigidBody rb2;
		protected int hitPointCount;
		protected Vector3[] hitPoints;
		protected float[] lamdas;
	}

	private Key key = new Key();
	private int keyPoolIdx = 0;
	private Key[] keyPool = null;
	private int contactCacheInfoPoolIdx = 0;
	private ContactCacheInfo[] contactCacheInfoPool = null;
	private HashMap<Key, ContactCacheInfo> contactCache = new HashMap<Key, ContactCacheInfo>();
	private Vector3 tmpVector3 = new Vector3();

	/**
	 * Constructor
	 */
	protected ContactCache() {
		keyPool = new Key[ConstraintsSolver.CONSTRAINTS_MAX];
		contactCacheInfoPool = new ContactCacheInfo[ConstraintsSolver.CONSTRAINTS_MAX];
		for (int i = 0; i < ConstraintsSolver.CONSTRAINTS_MAX; i++) {
			keyPool[i] = new Key();
			contactCacheInfoPool[i] = new ContactCacheInfo();
			contactCacheInfoPool[i].hitPoints = new Vector3[CollisionResponse.HITPOINT_COUNT];
			contactCacheInfoPool[i].lamdas = new float[CollisionResponse.HITPOINT_COUNT * 3];
			for (int j = 0; j < CollisionResponse.HITPOINT_COUNT; j++) {
				contactCacheInfoPool[i].hitPoints[j] = new Vector3();
			}
		}
	}

	/**
	 * Clear contact cache
	 */
	protected void clear() {
		keyPoolIdx = 0;
		contactCacheInfoPoolIdx = 0;
		contactCache.clear();
	}

	/**
	 * 
	 * @param rb1
	 * @param rb2
	 * @param collision
	 * @param lamdaValues
	 */
	protected void add(RigidBody rb1, RigidBody rb2, CollisionResponse collision, float[] lamdaValues) {
		// construct contact cache
		ContactCacheInfo contactCacheInfo = contactCacheInfoPool[contactCacheInfoPoolIdx++];
		contactCacheInfo.rb1 = rb1;
		contactCacheInfo.rb2 = rb2;
		contactCacheInfo.hitPointCount = collision.getHitPointsCount();
		for (int i = 0; i < contactCacheInfo.hitPointCount; i++) {
			contactCacheInfo.hitPoints[i].set(collision.getHitPointAt(i));
			contactCacheInfo.lamdas[i] = lamdaValues[i];
		}

		// generate key
		Key key = keyPool[keyPoolIdx++];
		key.reset();
		key.append(rb1.id);
		key.append(",");
		key.append(rb2.id);

		// construct
		ContactCacheInfo oldContactCacheInfo = contactCache.put(key, contactCacheInfo);
	}

	/**
	 * Retrieve contact cache info
	 * @param rigid body1
	 * @param rigid body2
	 * @param collision response
	 * @return contact cache info
	 */
	protected ContactCacheInfo get(RigidBody rb1, RigidBody rb2, CollisionResponse collision) {
		// generate key
		key.reset();
		key.append(rb1.id);
		key.append(",");
		key.append(rb2.id);

		// retrieve
		ContactCacheInfo contactCacheInfo = contactCache.get(key);
		if (contactCacheInfo != null) {
			// validate
			//	hit point count
			if (collision.getHitPointsCount() != contactCacheInfo.hitPointCount) return null;

			// hit points
			for (int i = 0; i < contactCacheInfo.hitPointCount; i++) {
				tmpVector3.set(
					collision.getHitPointAt(i)
				).sub(
					contactCacheInfo.hitPoints[i]
				);
				if (tmpVector3.computeLength() > 0.1f) return null;
			}

			// valid
			return contactCacheInfo;
		}

		//
		return null;
	}

}

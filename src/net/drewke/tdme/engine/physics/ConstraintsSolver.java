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

import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;

/**
 * Constraints solver
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ConstraintsSolver {

	protected final static int BODIES_MAX = 4096;
	protected final static int CONSTRAINTS_MAX = BODIES_MAX * 3;

	private int constraintsEntityCount = 0;
	private int collisionsCount = 0;
	private int constraintsCount = 0;
	private int keyCount = 0;

	private ArrayList<RigidBody> rigidBodies = null;
	private HashMap<String, RigidBody> constrainedBodies = new HashMap<String, RigidBody>();
	private ContactCache contactCache = new ContactCache();

	// constraints
	private int[][] constraintsBodyIdxMap = new int[CONSTRAINTS_MAX][];
	private Matrix1x6[][] jacobianMatrices = new Matrix1x6[CONSTRAINTS_MAX][];
	private Vector6[][] bVectors = new Vector6[CONSTRAINTS_MAX][];
	private DynamicVector lambda = new DynamicVector(CONSTRAINTS_MAX);
	private DynamicVector lambdaInit = new DynamicVector(CONSTRAINTS_MAX);
	private DynamicVector errorValues = new DynamicVector(CONSTRAINTS_MAX);
	private DynamicVector b = new DynamicVector(CONSTRAINTS_MAX);
	private DynamicVector lowerBounds = new DynamicVector(CONSTRAINTS_MAX);
	private DynamicVector upperBounds = new DynamicVector(CONSTRAINTS_MAX);
	private float d[] = new float[CONSTRAINTS_MAX];

	// bodies
	private ConstraintsEntity[] constraintsEntities = new ConstraintsEntity[BODIES_MAX];
	private CollisionResponse[] collisions = new CollisionResponse[BODIES_MAX];
	private Key[] keys = new Key[BODIES_MAX * 2];
	private Matrix6x6[] invInertiaMatrices = new Matrix6x6[BODIES_MAX];
	private Vector6[] velocityVectors = new Vector6[BODIES_MAX];
	private Vector6[] constrainedVelocityVectors = new Vector6[BODIES_MAX];
	private Vector6[] forcesVectors = new Vector6[BODIES_MAX];
	private Vector6[] a = new Vector6[BODIES_MAX];

	// user velocity constraints
	private ArrayList<RigidBody> rigidBodiesVelocityChange = new ArrayList<RigidBody>();
	private ArrayList<RigidBody> rigidBodiesCurrentChain = new ArrayList<RigidBody>();
	private ArrayList<RigidBody> rigidBodiesChainsResult = new ArrayList<RigidBody>();

	// tmp
	private float tmpLamdaValues[] = new float[CollisionResponse.HITPOINT_COUNT * 3];
	private Matrix1x6 tmpMatrix1x6 = new Matrix1x6();
	private Vector6 tmpVector6 = new Vector6();

	// update
	private Vector3 newLinearVelocity = new Vector3();
	private Vector3 newAngularVelocity = new Vector3();
	private Vector3 force = new Vector3();
	private Vector3 torque = new Vector3();

	/**
	 * Protected constructor
	 * @param rigid bodies
	 */
	protected ConstraintsSolver(ArrayList<RigidBody> rigidBodies) {
		//
		this.rigidBodies = rigidBodies;

		// constraints related initalizations
		for (int i = 0; i < CONSTRAINTS_MAX; i++) {
			constraintsBodyIdxMap[i] = new int[2];
			jacobianMatrices[i] = new Matrix1x6[2];
			jacobianMatrices[i][0] = new Matrix1x6();
			jacobianMatrices[i][1] = new Matrix1x6();
			bVectors[i] = new Vector6[2];
			bVectors[i][0] = new Vector6();
			bVectors[i][1] = new Vector6();
		}

		// body related initalizations
		for (int i = 0; i < BODIES_MAX; i++) {
			invInertiaMatrices[i] = new Matrix6x6();
			velocityVectors[i] = new Vector6();
			constrainedVelocityVectors[i] = new Vector6();
			forcesVectors[i] = new Vector6();
			a[i] = new Vector6();
			constraintsEntities[i] = new ConstraintsEntity();
			collisions[i] = new CollisionResponse();
		}

		// keys
		for (int i = 0; i < BODIES_MAX * 2; i++) {
			keys[i] = new Key();
		}

		// 
		lambda.setSize(CONSTRAINTS_MAX);
		lambdaInit.setSize(CONSTRAINTS_MAX);
		errorValues.setSize(CONSTRAINTS_MAX);
		b.setSize(CONSTRAINTS_MAX);
		lowerBounds.setSize(CONSTRAINTS_MAX);
		upperBounds.setSize(CONSTRAINTS_MAX);
	}

	/**
	 * Reset
	 */
	protected void reset() {
		constraintsEntityCount = 0;
		collisionsCount = 0;
		constraintsCount = 0;
		keyCount = 0;
		constrainedBodies.clear();
	}

	/**
	 * @return constraints entity
	 */
	protected ConstraintsEntity allocateConstraintsEntity() {
		return constraintsEntities[constraintsEntityCount++];
	}

	/**
	 * @return collision response
	 */
	protected CollisionResponse allocateCollision() {
		return collisions[collisionsCount++];
	}

	/**
	 * @return key
	 */
	protected Key allocateKey() {
		return keys[keyCount++];
	}

	/**
	 * @return key
	 */
	protected void releaseKey() {
		keyCount--;
	}

	/**
	 * Init method
	 * @param delta time
	 * @param constraints
	 * @param rigid bodies
	 */
	private void init(float dt) {
		//
		constraintsCount = 0;
		for (int i = 0; i < constraintsEntityCount; i++) {
			ConstraintsEntity constraintedBody = constraintsEntities[i];

			constrainedBodies.put(constraintedBody.rb1.id, constraintedBody.rb1);
			constrainedBodies.put(constraintedBody.rb2.id, constraintedBody.rb2);

			constraintsCount+= constraintedBody.collision.getHitPointsCount() * 3;
		}

		//
		int currentConstraint = 0;
		for (int i = 0; i < constraintsEntityCount; i++) {
			ConstraintsEntity constraintedBody = constraintsEntities[i];
			int hitPointsCount = constraintedBody.collision.getHitPointsCount();

			//
			for (int j = 0; j < hitPointsCount * 3; j++) {
				constraintsBodyIdxMap[currentConstraint + j][0] = constraintedBody.rb1.idx;
				constraintsBodyIdxMap[currentConstraint + j][1] = constraintedBody.rb2.idx;
			}
			constraintedBody.computeJacobian(currentConstraint, jacobianMatrices);
			constraintedBody.computeLowerBound(currentConstraint, lowerBounds);
			constraintedBody.computeUpperBound(currentConstraint, upperBounds);
			constraintedBody.computeBaumgarte(currentConstraint, errorValues);

			// contact cache
			ContactCache.ContactCacheInfo contactCacheInfo = contactCache.get(
				constraintedBody.rb1,
				constraintedBody.rb2,
				constraintedBody.collision
			);
			if (contactCacheInfo != null) {
				for (int j = 0; j < hitPointsCount * 3; j++) {
					lambdaInit.setValue(currentConstraint + j, contactCacheInfo.lamdas[j]);
				}				
			} else {
				for (int j = 0; j < hitPointsCount * 3; j++) {
					lambdaInit.setValue(currentConstraint + j, 0.0f);
				}
			}

			//
			currentConstraint+= hitPointsCount * 3;
		}
	}

	/**
	 * Fill matrices
	 */
	private void fillMatrices() {
		for (RigidBody rb: constrainedBodies.getValuesIterator()) {
			int bodyIdx = rb.idx;

			Vector6 velocityVector = velocityVectors[bodyIdx];
			velocityVector.setValue(0, rb.linearVelocity);
			velocityVector.setValue(3, rb.angularVelocity);

			Vector6 constainedVelocityVector = constrainedVelocityVectors[bodyIdx];
			constainedVelocityVector.fill(0.0f);

			Vector6 forcesVector = forcesVectors[bodyIdx];
			forcesVector.setValue(0, rb.force);
			forcesVector.setValue(3, rb.torque);

			Matrix6x6 invInertiaMatrix = invInertiaMatrices[bodyIdx];
			invInertiaMatrix.fill(0.0f);
			float[] worldInverseInertiaArray = rb.worldInverseInertia.getArray();
			if (rb.isStatic == false) {
				invInertiaMatrix.setValue(0, 0, rb.inverseMass);
				invInertiaMatrix.setValue(1, 1, rb.inverseMass);
				invInertiaMatrix.setValue(2, 2, rb.inverseMass);
				invInertiaMatrix.setValue(3, 3, worldInverseInertiaArray[0 + 0]);
				invInertiaMatrix.setValue(3, 4, worldInverseInertiaArray[0 + 1]);
				invInertiaMatrix.setValue(3, 5, worldInverseInertiaArray[0 + 2]);
				invInertiaMatrix.setValue(4, 3, worldInverseInertiaArray[4 + 0]);
				invInertiaMatrix.setValue(4, 4, worldInverseInertiaArray[4 + 1]);
				invInertiaMatrix.setValue(4, 5, worldInverseInertiaArray[4 + 2]);
				invInertiaMatrix.setValue(5, 3, worldInverseInertiaArray[8 + 0]);
				invInertiaMatrix.setValue(5, 4, worldInverseInertiaArray[8 + 1]);
				invInertiaMatrix.setValue(5, 5, worldInverseInertiaArray[8 + 2]);
			}
		}
	}

	/**
	 * Compute vector b
	 * @param delta time
	 */
	private void computeVectorB(float dt) {
		float oneOverDT = 1.0f / dt;
		errorValues.scale(oneOverDT, b);
		for (int i = 0; i < constraintsCount; i++) {
			int body1Idx = constraintsBodyIdxMap[i][0];
			int body2Idx = constraintsBodyIdxMap[i][1];

			// 1.0 / dt * J * V
			float t1 =
				jacobianMatrices[i][0].multiply(velocityVectors[body1Idx]) +
				jacobianMatrices[i][1].multiply(velocityVectors[body2Idx]) *
				oneOverDT;

			// J*M^-1*F_ext 
			float t2 =
				jacobianMatrices[i][0].multiply(invInertiaMatrices[body1Idx], tmpMatrix1x6).multiply(forcesVectors[body1Idx]) +
				jacobianMatrices[i][1].multiply(invInertiaMatrices[body2Idx], tmpMatrix1x6).multiply(forcesVectors[body2Idx]);

			//
			float result = b.getValue(i) + t1 + t2;
			b.setValue(i, result);
		}
	}

	/**
	 * Computes matrix b
	 */
	private void computeMatrixB() {
		for (int i = 0; i < constraintsCount; i++) {
			int body1Idx = constraintsBodyIdxMap[i][0];
			int body2Idx = constraintsBodyIdxMap[i][1];
			invInertiaMatrices[body1Idx].multiply(jacobianMatrices[i][0].getTranspose(tmpVector6), bVectors[i][0]);
			invInertiaMatrices[body2Idx].multiply(jacobianMatrices[i][1].getTranspose(tmpVector6), bVectors[i][1]);
		}
	}

	/**
	 * Compute vector a
	 * @param a
	 */
	private void computeVectorA() {
		for (RigidBody rb: constrainedBodies.getValuesIterator()) {
			a[rb.idx].fill(0.0f);
		}
		for (int i = 0; i < constraintsCount; i++) {
			int body1Idx = constraintsBodyIdxMap[i][0];
			int body2Idx = constraintsBodyIdxMap[i][1];
			a[body1Idx].add(tmpVector6.set(bVectors[i][0]).scale(lambda.getValue(i)));
			a[body2Idx].add(tmpVector6.set(bVectors[i][1]).scale(lambda.getValue(i)));
		}
	}

	/**
	 * PGLCP
	 */
	private void PGLCP() {
		for (int i = 0; i < constraintsCount; i++) {
			lambda.setValue(i, lambdaInit.getValue(i));			
		}

		//
		computeVectorA();

		//
		for (int i = 0; i < constraintsCount; i++) {
			// d[i] = (J_sp[i][0] * B_sp[0][i] + J_sp[i][1] * B_sp[1][i]);
			d[i] =
				jacobianMatrices[i][0].multiply(bVectors[i][0]) +
				jacobianMatrices[i][1].multiply(bVectors[i][1]);
		}

		//
		for (int iteration = 0; iteration < 20; iteration++) {
			//
			for (int i = 0; i < constraintsCount; i++) {
				int body1Idx = constraintsBodyIdxMap[i][0];
				int body2Idx = constraintsBodyIdxMap[i][1];

				float xDelta =
					(
						b.getValue(i) -
						jacobianMatrices[i][0].multiply(a[body1Idx]) -
						jacobianMatrices[i][1].multiply(a[body2Idx])
					) / d[i];
				float xTemp = lambda.getValue(i);

				float min = Math.min(xTemp + xDelta, upperBounds.getValue(i));
				float max = Math.max(lowerBounds.getValue(i), min);
				lambda.setValue(i,max);

				xDelta = lambda.getValue(i) - xTemp;
				a[body1Idx].add(tmpVector6.set(bVectors[i][0]).scale(xDelta));
				a[body2Idx].add(tmpVector6.set(bVectors[i][1]).scale(xDelta));
		
			}
		}
	}

	/**
	 * Compute vector velocity constraints
	 */
	private void computeVectorVelocityConstraints(float dt) {
		for (int i = 0; i < constraintsCount; i++) {
			int body1Idx = constraintsBodyIdxMap[i][0];
			int body2Idx = constraintsBodyIdxMap[i][1];
			constrainedVelocityVectors[body1Idx].sub(tmpVector6.set(bVectors[i][0]).scale(lambda.getValue(i) * dt));
			constrainedVelocityVectors[body2Idx].sub(tmpVector6.set(bVectors[i][1]).scale(lambda.getValue(i) * dt));

		}
	}

	/**
	 * Update contact cache
	 */
	private void updateContactCache() {
		//
		contactCache.clear();

		//
		int constraintsIdx = 0;
		for (int i = 0; i < constraintsEntityCount; i++) {
			ConstraintsEntity constraintsEntity = constraintsEntities[i];
			int hitPoints = constraintsEntity.collision.getHitPointsCount();
			for (int j = 0; j < hitPoints * 3; j++) {
				tmpLamdaValues[j] = lambda.getValue(constraintsIdx + j);
			}
			contactCache.add(
				constraintsEntity.rb1,
				constraintsEntity.rb2,
				constraintsEntity.collision,
				tmpLamdaValues
			);
			constraintsIdx+= hitPoints * 3;
		}
	}

	/**
	 * Finds rigid body successors in a direction for given rigid body src 
	 * @param rigid body src
	 * @param normal last
	 * @param rigid bodies current chain
	 * @param rigid bodies current chain result
	 * @param calls
	 */
	private void checkChainSuccessor(RigidBody rigidBodySrc, Vector3 normalLast, ArrayList<RigidBody> rigidBodiesCurrentChain) {
		rigidBodiesCurrentChain.add(rigidBodySrc);
		for (int i = 0; i < constraintsEntityCount; i++) {
			ConstraintsEntity constraintEntity = constraintsEntities[i];

			// rigid body to check
			RigidBody rigidBodyCheck = null;

			// check if rigid body is another rigid body velocity change rigid
			if (constraintEntity.rb1 == rigidBodySrc) {
				rigidBodyCheck = constraintEntity.rb2;
			} else
			if (constraintEntity.rb2 == rigidBodySrc) {
				rigidBodyCheck = constraintEntity.rb1;
			} else {
				continue;
			}

			// do not check static rigids
			if (rigidBodyCheck.isStatic == true) {
				continue;
			}

			// skip on disabled rigid bodies
			if (rigidBodyCheck.enabled == false) continue;

			// check if we checked this node already
			boolean haveRigidBodyCheck = false;
			for (int j = 0; j < rigidBodiesCurrentChain.size(); j++) {
				if (rigidBodiesCurrentChain.get(j) == rigidBodyCheck) {
					haveRigidBodyCheck = true;
					break;
				}
			}
			if (haveRigidBodyCheck == true) {
				continue;
			}

			// check if normal have same direction
			Vector3 normalCurrent = constraintEntity.collision.getNormal();
			if (normalLast != null) {
				if (Math.abs(Vector3.computeDotProduct(normalLast, normalCurrent)) < 0.75f) {
					continue;
				}
			}

			// check next
			checkChainSuccessor(rigidBodyCheck, normalCurrent, rigidBodiesCurrentChain);
		}
	}

	/**
	 * Process rigid body chain
	 * @param idx
	 * @param rigid bodies current chain
	 * @return new idx to process
	 */
	protected int processRigidBodyChain(int idx,  ArrayList<RigidBody> rigidBodiesCurrentChain) {
		// compute speed of A
		int rigidBodyAIdx = -1;
		for (int j = idx; j < rigidBodiesCurrentChain.size(); j++) {
			RigidBody rigidBody = rigidBodiesCurrentChain.get(j);

			// check if rigid body had a velocity change
			boolean isVelocityChangeRigidBody = false;
			for (int k = 0; k < rigidBodiesVelocityChange.size(); k++) {
				RigidBody rigidBodyVC = rigidBodiesVelocityChange.get(k);
				if (rigidBodyVC == rigidBody) {
					isVelocityChangeRigidBody = true;
					break;
				}
			}
			if (isVelocityChangeRigidBody == true) {
				rigidBodyAIdx = j;
				break;
			} else {
				continue;
			}
		}

		// skip if we have no rigid body A
		if (rigidBodyAIdx == -1) return -1;

		// get rigid body A, speed
		RigidBody rigidBodyA = rigidBodiesCurrentChain.get(rigidBodyAIdx);
		float rigidBodyASpeed = rigidBodyA.linearVelocity.computeLength();

		// compute max speed of B in chain
		int rigidBodyBIdx = -1;
		float rigidBodyBSpeed = 0f;
		for (int j = idx + 1; j < rigidBodiesCurrentChain.size(); j++) {
			RigidBody rigidBody = rigidBodiesCurrentChain.get(j);

			// check if rigid body had a velocity change
			boolean isVelocityChangeRigidBody = false;
			for (int k = 0; k < rigidBodiesVelocityChange.size(); k++) {
				RigidBody rigidBodyVC = rigidBodiesVelocityChange.get(k);
				if (rigidBodyVC == rigidBody) {
					isVelocityChangeRigidBody = true;
					break;
				}
			}
			if (isVelocityChangeRigidBody == false) continue; 

			// determine a on b
			float ab = Vector3.computeDotProduct(
				rigidBodiesCurrentChain.get(rigidBodyAIdx).linearVelocity, 
				rigidBodiesCurrentChain.get(j).linearVelocity
			);

			// skip if A has same direction like B
			if (ab > 0f) continue;

			// otherwise compute speed
			float _speed = rigidBody.linearVelocity.computeLength();

			// we have a candidate
			if (_speed > rigidBodyBSpeed) {
				rigidBodyBIdx = j;
				rigidBodyBSpeed = _speed;
			}
		}

		// get out if we have no B
		if (rigidBodyBIdx == -1) {
			return -1;
		}

		// set up rigid body A
		if (rigidBodyA.linearVelocity.computeLength() > MathTools.EPSILON) {
			float y = rigidBodyA.linearVelocity.getY(); 
			rigidBodyA.linearVelocity.normalize();
			rigidBodyA.linearVelocity.scale(
				rigidBodyASpeed - rigidBodyBSpeed > 0f?
				rigidBodyASpeed - rigidBodyBSpeed:
				0f
			);
			rigidBodyA.linearVelocity.setY(y);
		}

		// set up rigid body B
		RigidBody rigidBodyB = rigidBodiesCurrentChain.get(rigidBodyBIdx);
		if (rigidBodyB.linearVelocity.computeLength() > MathTools.EPSILON) {
			float y = rigidBodyB.linearVelocity.getY();
			rigidBodyB.linearVelocity.normalize();
			rigidBodyB.linearVelocity.scale(
				rigidBodyBSpeed - rigidBodyASpeed > 0f?
				rigidBodyBSpeed - rigidBodyASpeed:
				0f
			);
			rigidBodyB.linearVelocity.setY(y);
		}

		// set up rigid bodies between A and B
		for (int rigidBodyIdx = rigidBodyAIdx + 1; rigidBodyIdx < rigidBodyBIdx; rigidBodyIdx++) {
			RigidBody rigidBody = rigidBodiesCurrentChain.get(rigidBodyIdx);
			float y = rigidBody.linearVelocity.getY();
			rigidBody.linearVelocity.scale(0f);
			rigidBody.linearVelocity.setY(y);
		}

		return rigidBodyBIdx + 1;
	}

	/**
	 * Check if we have any user velocity rigids
	 * 	which have opposite velocity and do collide directly or via other objects
	 */
	protected void checkVelocityConstraint() {
		for (int i = 0; i < rigidBodies.size(); i++) {
			RigidBody rigidBodyVelocityChange = rigidBodies.get(i);
			if (rigidBodyVelocityChange.enabled == false) continue;
			if (rigidBodyVelocityChange.checkVelocityChange() == true) {
				rigidBodiesVelocityChange.add(rigidBodyVelocityChange);
			}
		}

		// determine rigid bodies with velocity change
		for (int i = 0; i < rigidBodiesVelocityChange.size(); i++) {
			RigidBody rigidBodySrc = rigidBodiesVelocityChange.get(i);

			// skip on rigid bodies that have been processed
			boolean rigidBodyProcessed = false;
			for (int j = 0; j < rigidBodiesChainsResult.size(); j++) {
				if (rigidBodiesChainsResult.get(j) == rigidBodySrc) {
					rigidBodyProcessed = true;
					break;
				}
			}
			if (rigidBodyProcessed == true) continue;

			// yep, we have a rigid with velocity change, find a path to another one 
			checkChainSuccessor(rigidBodySrc, null, rigidBodiesCurrentChain);

			// mark as processed
			for (int j = 0; j < rigidBodiesCurrentChain.size(); j++) {
				RigidBody rigidBody = rigidBodiesCurrentChain.get(j);

				// mark as processed
				rigidBodiesChainsResult.add(rigidBody);
			}

			// skip if we have no chain
			if (rigidBodiesCurrentChain.size() < 2) {
				rigidBodiesCurrentChain.clear();
				continue;
			}

			// process rigid body chain
			int idx = 0;
			while (true == true) {
				idx = processRigidBodyChain(idx, rigidBodiesCurrentChain);
				if (idx == -1 || idx >= rigidBodiesCurrentChain.size()) break;
			}

			// clean up
			rigidBodiesCurrentChain.clear();
		}

		// clean up
		rigidBodiesChainsResult.clear();
		rigidBodiesVelocityChange.clear();
	}

	/**
	 * Compute 
	 * @param delta time
	 * @param constraints
	 * @param rigid bodies
	 */
	protected void compute(float dt) {
		if (constraintsEntityCount == 0) return;
		checkVelocityConstraint();
		init(dt);
		fillMatrices();
		computeVectorB(dt);
		computeMatrixB();
		PGLCP();
		computeVectorVelocityConstraints(dt);
		updateContactCache();
	}

	/**
	 * Set constrained linear and angular velocity for given body into dest vector
	 * @param body
	 * @param dest
	 */
	private void getConstrainedVelocity(RigidBody body, Vector3 linearVelocity, Vector3 angularVelocity) {
		Vector6 vector6 = constrainedVelocityVectors[body.idx];
		float[] vector6Array = vector6.data;
		linearVelocity.set(
			vector6Array[0],
			vector6Array[1],
			vector6Array[2]
		);
		angularVelocity.set(
			vector6Array[3],
			vector6Array[4],
			vector6Array[5]
		);
	}

	/**
	 * Updates all bodies 
	 * @param delta time
	 */
	protected void updateAllBodies(float deltaTime) {
		for (int i = 0; i < rigidBodies.size(); i++) {
			RigidBody body = rigidBodies.get(i);

			// skip on static or sleeping or disabled
			if (body.isStatic == true ||
				body.isSleeping == true ||
				body.enabled == false) {
				continue;
			}

			//
			newLinearVelocity.set(0f, 0f, 0f);
			newAngularVelocity.set(0f, 0f, 0f);

			// if constrained retrieve constrained velocities
			if (constrainedBodies.get(body.id) != null) {
				getConstrainedVelocity(body, newLinearVelocity, newAngularVelocity);
			}

			//
			force.set(body.force).scale(body.inverseMass * deltaTime);
			body.worldInverseInertia.multiply(body.torque, torque).scale(deltaTime);

			// add forces, old velocities
			newLinearVelocity.add(force);
			newAngularVelocity.add(torque);
			newLinearVelocity.add(body.linearVelocity);
			newAngularVelocity.add(body.angularVelocity);

			// set up new velocities
			body.linearVelocity.set(newLinearVelocity);
			body.angularVelocity.set(newAngularVelocity);


			// update rigid body
			body.update(deltaTime);
		}
	}
	
}

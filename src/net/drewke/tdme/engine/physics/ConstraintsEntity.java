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

/**
 * Physics constraints entity
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ConstraintsEntity {

	private final static float HITPOINT_TOLERANCE = 0.3f;

	protected RigidBody rb1;
	protected RigidBody rb2;

	protected CollisionResponse collision;

	private Vector3[] frictionVectors;
	private float muMg;

	private Vector3 tmpVector3 = new Vector3();
	private Vector3 r1 = new Vector3();
	private Vector3 r2 = new Vector3();
	private Vector3 r1CrossN = new Vector3();
	private Vector3 r2CrossN = new Vector3();
	private Vector3 r1CrossU1 = new Vector3();
	private Vector3 r2CrossU1 = new Vector3();
	private Vector3 r1CrossU2 = new Vector3();
	private Vector3 r2CrossU2 = new Vector3();

	/**
	 * Compute cross product a x b = dest
	 * @param a
	 * @param b
	 * @param dest
	 */
	private static void computeCrossProduct(Vector3 a, Vector3 b, Vector3 dest) {
		float[] aXYZ = a.getArray();
		float[] bXYZ = b.getArray();
		dest.set(
			aXYZ[1] * bXYZ[2] - aXYZ[2] * bXYZ[1],
			aXYZ[2] * bXYZ[0] - aXYZ[0] * bXYZ[2],
			aXYZ[0] * bXYZ[1] - aXYZ[1] * bXYZ[0]
		);		
	}

	/**
	 * Protected constructor
	 */
	protected ConstraintsEntity() {
		this.frictionVectors = new Vector3[2];
		this.frictionVectors[0] = new Vector3();
		this.frictionVectors[1] = new Vector3();
	}

	/**
	 * Protected constructor
	 * @param rb1
	 * @param rb2
	 * @param collision
	 */
	protected void set(RigidBody rb1, RigidBody rb2, CollisionResponse collision) {
		this.rb1 = rb1;
		this.rb2 = rb2;
		this.collision = collision;

		//	see http://en.wikipedia.org/wiki/Friction#Coefficient_of_friction
		muMg = ((rb1.friction + rb2.friction) / 2f) * ((rb1.mass + rb2.mass) / 2f) * MathTools.g;

		// our collision normal is the vector object a collides with object b
		collision.getNormal().scale(-1f);

		// compute first friction vector
		collision.getNormal().computeOrthogonalVector(this.frictionVectors[0]);

		// compute second friction vector
		computeCrossProduct(collision.getNormal(), this.frictionVectors[0], this.frictionVectors[1]);

		// work around falling stacks
		// 	check if to compact hit points
		if (rb2.angularVelocity.computeLength() < HITPOINT_TOLERANCE) {
			CollisionDetection.getInstance().compactHitPoints(collision);
		}
	}

	/**
	 * Compute jacobian
	 * @param constraint idx
	 * @param jacobian matrices
	 */
	protected void computeJacobian(int constraintIdx, Matrix1x6[][] jacobianMatrices) {
		Vector3 body1Position = rb1.getPosition();
		Vector3 body2Position = rb2.getPosition();
		Vector3 n = collision.getNormal();
		Vector3 t1 = frictionVectors[0];
		Vector3 t2 = frictionVectors[1];
		Matrix1x6 jacobianMatrix;
		int currentConstraintIdx = constraintIdx;
		for (int hitPointIdx = 0; hitPointIdx < collision.getHitPointsCount(); hitPointIdx++) {
			Vector3 point = collision.getHitPointAt(hitPointIdx);

			//
			r1.set(point).sub(body1Position);
			r2.set(point).sub(body2Position);
			computeCrossProduct(r1, n, r1CrossN);
			computeCrossProduct(r2, n, r2CrossN);

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][0];
			jacobianMatrix.setValue(0, tmpVector3.set(n).scale(-1f));
			jacobianMatrix.setValue(3, tmpVector3.set(r1CrossN).scale(-1f));

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][1];
			jacobianMatrix.setValue(0, n);
			jacobianMatrix.setValue(3, r2CrossN);

			//
			currentConstraintIdx++;
			computeCrossProduct(r1, t1, r1CrossU1);
			computeCrossProduct(r2, t1, r2CrossU1);
			computeCrossProduct(r1, t2, r1CrossU2);
			computeCrossProduct(r2, t2, r2CrossU2);

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][0];
			jacobianMatrix.setValue(0, tmpVector3.set(t1).scale(-1f));
			jacobianMatrix.setValue(3, tmpVector3.set(r1CrossU1).scale(-1f));

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][1];
			jacobianMatrix.setValue(0, t1);
			jacobianMatrix.setValue(3, r2CrossU1);

			//
			currentConstraintIdx++;

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][0];
			jacobianMatrix.setValue(0, tmpVector3.set(t2).scale(-1f));
			jacobianMatrix.setValue(3, tmpVector3.set(r1CrossU2).scale(-1f));

			jacobianMatrix = jacobianMatrices[currentConstraintIdx][1];
			jacobianMatrix.setValue(0, t2);
			jacobianMatrix.setValue(3, r2CrossU2);

			currentConstraintIdx++;
		}
	}

	/**
	 * Compute lower bounds
	 * @param constraint idx
	 * @param lower bounds
	 */
	protected void computeLowerBound(int constraintIdx, DynamicVector lowerBounds) {
		int currentConstraintIdx = constraintIdx;
		for (int hitPointIdx = 0; hitPointIdx < collision.getHitPointsCount(); hitPointIdx++) {
			lowerBounds.setValue(currentConstraintIdx++, 0f);		// Lower bound for the contact constraint
			lowerBounds.setValue(currentConstraintIdx++, -muMg);	// Lower bound for the first friction constraint
			lowerBounds.setValue(currentConstraintIdx++, -muMg);	// Lower bound for the second friction constraint
		}
	}

	/**
	 * Create upper bounds
	 * @param constraint idx
	 * @param upper bounds
	 */
	protected void computeUpperBound(int constraintIdx, DynamicVector upperBounds) {
		int currentConstraintIdx = constraintIdx;
		for (int hitPointIdx = 0; hitPointIdx < collision.getHitPointsCount(); hitPointIdx++) {
			upperBounds.setValue(currentConstraintIdx++, Float.POSITIVE_INFINITY);	// Upper bound for the contact constraint
			upperBounds.setValue(currentConstraintIdx++, +muMg);			// Upper bound for the first friction constraint
			upperBounds.setValue(currentConstraintIdx++, +muMg);			// Upper bound for the second friction constraint
		}
	}

	/**
	 * Compute baumgarte
	 * @param constraint idx
	 * @param error values
	 */
	protected void computeBaumgarte(int constraintIdx, DynamicVector errorValues) {
		int currentConstraintIdx = constraintIdx;
		
		float restitutionCoeff = rb1.restitution + rb2.restitution;
		float penetration = collision.getPenetration();
		float errorValue =
			Math.abs(
				restitutionCoeff *
				(Vector3.computeDotProduct(collision.getNormal(), rb1.angularVelocity) -
				 Vector3.computeDotProduct(collision.getNormal(), rb2.angularVelocity))
			) +
			(0.4f * penetration);
		for (int hitPointIdx = 0; hitPointIdx < collision.getHitPointsCount(); hitPointIdx++) {
			errorValues.setValue(currentConstraintIdx++, errorValue);	// Error value for contact constraint
			errorValues.setValue(currentConstraintIdx++, 0f);			// Error value for friction constraint
			errorValues.setValue(currentConstraintIdx++, 0f);			// Error value for friction constraint
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Constraints [rb1=" + rb1 + ", rb2=" + rb2 + "]";
	}

}

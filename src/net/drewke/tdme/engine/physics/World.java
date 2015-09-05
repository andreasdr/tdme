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

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Rotations;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.LineSegment;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIterator;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;

/**
 * Physics
 * @author Andreas Drewke
 * @version $Id$
 */
public final class World {

	// rigid bodies, pool
	private ArrayList<RigidBody> rigidBodies = new ArrayList<RigidBody>();
	private ArrayList<RigidBody> rigidBodiesDynamic = new ArrayList<RigidBody>();
	private HashMap<String, RigidBody> rigidBodiesById = new HashMap<String, RigidBody>();

	private HashMap<Key, Key> rigidBodyTestedCollisions = new HashMap<Key, Key>();

	private Partition partition = new Partition();

	//
	private Vector3 collisionMovement = new Vector3();
	private Vector3 worldPosForce = new Vector3();
	private Vector3 gravityForce = new Vector3();

	private CollisionResponse collision = new CollisionResponse();
	private ConstraintsSolver constraintsSolver = null;

	private BoundingBox heightBoundingBox = new BoundingBox();
	private Vector3 heightOnPointCandidate = new Vector3();
	private Vector3 heightOnPointA = new Vector3();
	private Vector3 heightOnPointB = new Vector3();
	private LineSegment heightOnPointLineSegment = new LineSegment();

	private Vector3 sideVector = new Vector3(1f,0f,0f);
	private Vector3 upVector = new Vector3(0f,1f,0f);
	private Vector3 forwardVector = new Vector3(0f,0f,1f);
	private Vector3 heightPoint = new Vector3();
	private Vector3 heightPointDest = new Vector3();

	private ArrayList<RigidBody> collidedRigidBodies = new ArrayList<RigidBody>();
	private ArrayListIterator<RigidBody> collidedRigidBodiesIterator = new ArrayListIterator<RigidBody>(collidedRigidBodies);

	/**
	 * Constructor
	 */
	public World() {
	}

	/**
	 * Resets the physic world
	 */
	public void reset() {
		rigidBodies.clear();
		partition.reset();
	}

	/**
	 * Add a rigid body
	 * @param id
	 * @param enabled
	 * @param transformations
	 * @param obv
	 * @param restitution
	 * @param friction
	 * @param mass
	 * @param inertia matrix
	 * @return rigid body
	 */
	public RigidBody addRigidBody(String id, boolean enabled, int typeId, Transformations transformations, BoundingVolume obv, float restitution, float friction, float mass, Matrix4x4 inertiaMatrix) {
		RigidBody rigidBody = new RigidBody(rigidBodies.size(), id, enabled, typeId, obv, transformations, restitution, friction, mass, inertiaMatrix);
		rigidBodies.add(rigidBody);
		rigidBodiesDynamic.add(rigidBody);
		rigidBodiesById.put(id, rigidBody);
		if (enabled == true) partition.addRigidBody(rigidBody);
		return rigidBody;
	}

	/**
	 * Add a static rigid body
	 * @param id
	 * @param enabled
	 * @param transformations
	 * @param obv
	 * @param friction
	 * @return rigid body
	 */
	public RigidBody addStaticRigidBody(String id, boolean enabled, int typeId, Transformations transformations, BoundingVolume obv, float friction) {
		RigidBody rigidBody = new RigidBody(rigidBodies.size(), id, enabled, typeId, obv, transformations, 0f, friction, 0f, RigidBody.computeInertiaMatrix(obv, 0f,  0f,  0f,  0f));
		rigidBodies.add(rigidBody);
		rigidBodiesById.put(id, rigidBody);
		if (enabled == true) partition.addRigidBody(rigidBody);
		return rigidBody;
	}

	/**
	 * Returns rigid body identified by id 
	 * @param id
	 * @return ridig body
	 */
	public RigidBody getRigidBody(String id) {
		return rigidBodiesById.get(id);
	}

	/**
	 * Update world
	 * @param delta time
	 */
	public void update(float dt) {
		// lazy initiate constraints solver
		if (constraintsSolver == null) {
			constraintsSolver = new ConstraintsSolver(rigidBodies);
		}

		// apply gravity
		for (int i = 0; i < rigidBodies.size(); i++) {
			// update rigid body
			RigidBody rigidBody = rigidBodies.get(i);

			// skip on disabled, static
			if (rigidBody.enabled == false) {
				// System.out.println("World::update()::gravity::skipping " + rigidBody.id + "::disabled");
				continue;
			}

			if (rigidBody.isStatic == true) {
				continue;
			}

			// unset sleeping if velocity change occured
			if (rigidBody.checkVelocityChange() == true) {
				rigidBody.awake(true);
			}

			// skip on sleeping
			if (rigidBody.isSleeping == true) {
				continue;
			}

			// add gravity
			rigidBody.addForce(
				worldPosForce.set(rigidBody.getPosition()).setY(10000f),
				gravityForce.set(0f,-rigidBody.getMass() * MathTools.g,0f)
			);
		}

		// do the collision tests,
		// take every rigid body with every other rigid body into account
		int collisionsTests = 0;
		rigidBodyTestedCollisions.clear();
		for (int i = 0; i < rigidBodies.size(); i++) {
			RigidBody rigidBody1 = rigidBodies.get(i);

			// skip on disabled
			if (rigidBody1.enabled == false) {
				// System.out.println("World::update()::collision::skipping " + rigidBody1.id + "::disabled");
				continue;
			}

			/**
			for (int j = 0; j < rigidBodies.size(); j++) {
				RigidBody rigidBody2 = rigidBodies.get(j);
			*/

			int nearObjects = 0;

			// get objects near to can return a rigid body multiple times
			// dont test test which had been done in reverse order
			for (RigidBody rigidBody2: partition.getObjectsNearTo(rigidBody1.cbv)) {
				// skip on disabled
				if (rigidBody2.enabled == false) {
					// System.out.println("World::update()::collision::skipping " + rigidBody2.id + "::disabled");
					continue;
				}

				// skip if both are static
				if (rigidBody1.isStatic == true &&
					rigidBody2.isStatic == true) continue;

				// skip on same rigid body
				if (rigidBody1 == rigidBody2) continue;

				// skip on rigid body 1 static, 2 non static and sleeping
				if (rigidBody1.isStatic == true &&
					rigidBody2.isStatic == false &&
					rigidBody2.isSleeping == true) {
					continue;
				}

				// skip on rigid body 2 static, 1 non static and sleeping
				if (rigidBody2.isStatic == true &&
					rigidBody1.isStatic == false &&
					rigidBody1.isSleeping == true) {
					continue;
				}

				// check if rigid body 2 want to have collision with rigid body 1
				if (((rigidBody1.typeId & rigidBody2.collisionTypeIds) == rigidBody1.typeId) == false) {
					continue;
				}

				// check if rigid body 1 want to have collision with rigid body 2
				if (((rigidBody2.typeId & rigidBody1.collisionTypeIds) == rigidBody2.typeId) == false) {
					continue;
				}

				//
				nearObjects++;

				// create rigidBody12 key
				Key rigidBodyKey = constraintsSolver.allocateKey();
				rigidBodyKey.reset();
				rigidBodyKey.append(rigidBody1.id);
				rigidBodyKey.append(",");
				rigidBodyKey.append(rigidBody2.id);

				// check if collision has been tested already
				if (rigidBodyTestedCollisions.get(rigidBodyKey) != null) {
					constraintsSolver.releaseKey();
					continue;
				}

				/*
				// create rigidbody21 key
				rigidBodyKey.reset();
				rigidBodyKey.append(rigidBody2.id);
				rigidBodyKey.append(",");
				rigidBodyKey.append(rigidBody1.id);

				if (rigidBodyTestedCollisions.get(rigidBodyKey) != null) {
					constraintsSolver.releaseKey();
					continue;
				}
				*/
	
				// nope, add 12 key
				rigidBodyTestedCollisions.put(rigidBodyKey, rigidBodyKey);

				//
				collisionsTests++;

				// determine collision movement
				collisionMovement.set(rigidBody1.movement);
				if (collisionMovement.computeLength() < MathTools.EPSILON) {
					collisionMovement.set(rigidBody2.movement);
					collisionMovement.scale(-1f);
				}

				// do collision test
				if (rigidBody1.cbv.doesCollideWith(rigidBody2.cbv, collisionMovement, collision) == true &&
					collision.hasPenetration() == true) {

					// check for hit point count
					if (collision.getHitPointsCount() == 0) continue;

					// unset sleeping if both non static and colliding
					if (rigidBody1.isStatic == false &&
						rigidBody2.isStatic == false) {
						rigidBody1.awake(true);
						rigidBody2.awake(true);
					}

					// add constraint entity
					constraintsSolver.allocateConstraintsEntity().set(rigidBody1, rigidBody2, constraintsSolver.allocateCollision().fromResponse(collision));
				}
				
			}
			// System.out.println(rigidBody1.id + ":" + nearObjects);
		}

		// do the solving
		constraintsSolver.compute(dt);
		constraintsSolver.updateAllBodies(dt);
		constraintsSolver.reset();

		// update transformations for rigid body 
		for (int i = 0; i < rigidBodies.size(); i++) {
			RigidBody rigidBody = rigidBodies.get(i);

			// skip if enabled and remove partition
			if (rigidBody.enabled == false) {
				partition.removeRigidBody(rigidBody);
				continue;
			}

			// skip on static
			if (rigidBody.isStatic == true ||
				rigidBody.isSleeping == true) {
				continue;
			}

			// set up transformations, keep care that only 3 rotations exists (x, y, z axis)
			Rotations rotations = rigidBody.transformations.getRotations();
			while (rotations.size() > 1) {
				rotations.remove(rotations.size() - 1);
			}
			while (rotations.size() < 1) {
				rotations.add(new Rotation());
			}
	
			// set up orientation
			rotations.get(0).fromQuaternion(rigidBody.orientation);
			rotations.get(0).getAxix().getArray()[1]*=-1f;

			//	second set up position
			Transformations transformations = rigidBody.transformations;
			transformations.getTranslation().set(rigidBody.position);

			// update
			transformations.update();

			// update bounding volume
			rigidBody.cbv.fromBoundingVolumeWithTransformations(rigidBody.obv, transformations);

			// update partition
			partition.updateRigidBody(rigidBody);
		}
	}

	/**
	 * Synch physics world with engine
	 * @param engine
	 */
	public void synch(Engine engine) {
		for (int i = 0; i < rigidBodies.size(); i++) {
			// update rigid body
			RigidBody rigidBody = rigidBodies.get(i);

			// skip on static objects
			if (rigidBody.isStatic == true) continue;
			if (rigidBody.isSleeping == true) continue;

			Entity engineEntity = engine.getEntity(rigidBody.id);
			if (engineEntity == null) {
				System.out.println("World::entity '" + rigidBody.id + "' not found");
				continue;
			}
			engineEntity.setEnabled(rigidBody.enabled);
			if (rigidBody.enabled == true) {
				engineEntity.fromTransformations(rigidBody.transformations);
			}
		}
	}

	/**
	 * Returns higher vector of
	 * @param a
	 * @param b
	 * @return higher vector
	 */
	private static Vector3 higher(Vector3 a, Vector3 b) {
		return a.getY() > b.getY()?a:b;
	}

	/**
	 * Determine height on x,y,u while respecting step up max
	 * @param type ids
	 * @param step up max
	 * @param point on which height should be calculated
	 * @param point where height has been determined
	 * @return rigid body from which height was determined or null
	 */
	public RigidBody determineHeight(int typeIds, float stepUpMax, Vector3 point, Vector3 dest) {
		dest.set(point);
		float[] pointXYZ = point.getArray();

		// height bounding box to determine partition bounding volumes
		heightBoundingBox.getMin().set(pointXYZ[0], -10000f, pointXYZ[2]);
		heightBoundingBox.getMax().set(pointXYZ[0], +10000f, pointXYZ[2]);
		heightBoundingBox.update();

		// determine height of point on x, z
		heightOnPointCandidate.set(
			pointXYZ[0],
			10000f,
			pointXYZ[2]
		);

		
		float height = -10000f;
		RigidBody heightRigidBody = null;
		for (RigidBody rigidBody: partition.getObjectsNearTo(heightBoundingBox)) {
			if (((rigidBody.typeId & typeIds) == rigidBody.typeId) == false) continue;
			BoundingVolume cbv = rigidBody.cbv;
			if (cbv instanceof BoundingBox) {
				if (heightOnPointLineSegment.doesBoundingBoxCollideWithLineSegment(
					(BoundingBox)cbv,
					heightBoundingBox.getMin(),
					heightBoundingBox.getMax(),
					heightOnPointA,
					heightOnPointB
				) == true) {
					Vector3 heightOnPoint = higher(heightOnPointA, heightOnPointB);
					if (heightOnPoint.getY() >= height &&
						heightOnPoint.getY() < pointXYZ[1] + Math.max(0.1f, stepUpMax)) {
						//
						height = heightOnPoint.getY();
						heightRigidBody = rigidBody;
					}
				}
			} else
			if (cbv instanceof OrientedBoundingBox) {
				if (heightOnPointLineSegment.doesOrientedBoundingBoxCollideWithLineSegment(
					(OrientedBoundingBox)cbv,
					heightBoundingBox.getMin(),
					heightBoundingBox.getMax(),
					heightOnPointA,
					heightOnPointB
				) == true) {
					Vector3 heightOnPoint = higher(heightOnPointA, heightOnPointB);
					if (heightOnPoint.getY() >= height &&
						heightOnPoint.getY() < pointXYZ[1] + Math.max(0.1f, stepUpMax)) {
						//
						height = heightOnPoint.getY();
						heightRigidBody = rigidBody;
					}
				}
			} else {
				// compute closest point on height candidate
				cbv.computeClosestPointOnBoundingVolume(
					heightOnPointCandidate,
					heightOnPointA
				);
	
				// check new height, take only result into account which is near to candidate 
				if (Math.abs(heightOnPointCandidate.getX() - heightOnPointA.getX()) < 0.1f &&
					Math.abs(heightOnPointCandidate.getZ() - heightOnPointA.getZ()) < 0.1f &&
					heightOnPointA.getY() >= height &&
					heightOnPointA.getY() < pointXYZ[1] + Math.max(0.1f, stepUpMax)) {
					//
					height = heightOnPointA.getY();
					heightRigidBody = rigidBody;
				}
			}
		}

		// check if we have a ground
		if (heightRigidBody == null) {
			return null;
		}

		// nope, no collision
		dest.setY(height);
		return heightRigidBody;
	}

	/**
	 * Determine height of bounding volume
	 * @param type ids
	 * @param step up max
	 * @param bounding volume
	 * @param point
	 * @param dest
	 * @return rigid body from which height was determined or null
	 */
	public RigidBody determineHeight(int typeIds, float stepUpMax, BoundingVolume boundingVolume, Vector3 point, Vector3 dest) {
		float determinedHeight = -10000f;
		float width = boundingVolume.computeDimensionOnAxis(sideVector);
		float height = boundingVolume.computeDimensionOnAxis(upVector);
		float depth = boundingVolume.computeDimensionOnAxis(forwardVector);
		float heightPointDestY;
		RigidBody heightRigidBody = null;
		RigidBody rigidBody = null;

		// center, center
		heightPoint.set(boundingVolume.getCenter());
		heightPoint.addY(-height / 2f);
		rigidBody = determineHeight(typeIds, stepUpMax, heightPoint, heightPointDest);
		if (rigidBody != null) {
			heightPointDestY = heightPointDest.getY(); 
			if (heightPointDestY > determinedHeight) {
				heightRigidBody = rigidBody;
				determinedHeight = heightPointDestY;
			}
		}

		// left, top
		heightPoint.set(boundingVolume.getCenter());
		heightPoint.addX(-width / 2f);
		heightPoint.addY(-height / 2f);
		heightPoint.addZ(-depth / 2f);
		rigidBody = determineHeight(typeIds, stepUpMax, heightPoint, heightPointDest);
		if (rigidBody != null) {
			heightPointDestY = heightPointDest.getY(); 
			if (heightPointDestY > determinedHeight) {
				heightRigidBody = rigidBody;
				determinedHeight = heightPointDestY;
			}
		}

		// left, bottom
		heightPoint.set(boundingVolume.getCenter());
		heightPoint.addX(-width / 2f);
		heightPoint.addY(-height / 2f);
		heightPoint.addZ(+depth / 2f);
		rigidBody = determineHeight(typeIds, stepUpMax, heightPoint, heightPointDest);
		if (rigidBody != null) {
			heightPointDestY = heightPointDest.getY(); 
			if (heightPointDestY > determinedHeight) {
				heightRigidBody = rigidBody;
				determinedHeight = heightPointDestY;
			}
		}

		// right, top
		heightPoint.set(boundingVolume.getCenter());
		heightPoint.addX(+width / 2f);
		heightPoint.addY(-height / 2f);
		heightPoint.addZ(-depth / 2f);
		rigidBody = determineHeight(typeIds, stepUpMax, heightPoint, heightPointDest);
		if (rigidBody != null) {
			heightPointDestY = heightPointDest.getY(); 
			if (heightPointDestY > determinedHeight) {
				heightRigidBody = rigidBody;
				determinedHeight = heightPointDestY;
			}
		}

		// right, bottom
		heightPoint.set(boundingVolume.getCenter());
		heightPoint.addX(+width / 2f);
		heightPoint.addY(-height / 2f);
		heightPoint.addZ(+depth / 2f);
		rigidBody = determineHeight(typeIds, stepUpMax, heightPoint, heightPointDest);
		if (rigidBody != null) {
			heightPointDestY = heightPointDest.getY(); 
			if (heightPointDestY > determinedHeight) {
				heightRigidBody = rigidBody;
				determinedHeight = heightPointDestY;
			}
		}

		// set up result
		if (heightRigidBody == null) {
			return null;
		} else {
			dest.set(point);
			dest.setY(determinedHeight);
			return heightRigidBody;
		}
	}

	/**
	 * Check if world collides with given bounding volume
	 * @param type ids
	 * @param bounding volume
	 * @return rigid bodies iterator
	 */
	public ArrayListIterator<RigidBody> doesCollideWith(int typeIds, BoundingVolume boundingVolume) {
		collidedRigidBodies.clear();
		// 
		for (RigidBody rigidBody: partition.getObjectsNearTo(boundingVolume)) {
			if (((rigidBody.typeId & typeIds) == rigidBody.typeId) == false) continue;

			// check if rigid body collides with 
			if (rigidBody.cbv.doesCollideWith(boundingVolume, null, collision) == true &&
				collision.hasPenetration() == true) {
				collidedRigidBodies.add(rigidBody);
			}
		}
		return collidedRigidBodiesIterator;
	}

	/**
	 * Clone this world
	 */
	public World clone() {
		World clonedWorld = new World();
		for (int i = 0; i < rigidBodies.size(); i++) {
			RigidBody rigidBody = rigidBodies.get(i);

			// clone obv
			BoundingVolume obv = rigidBody.obv == null?null:rigidBody.obv.clone();

			// clone static rigid body
			RigidBody clonedRigidBody = null;
			if (rigidBody.isStatic == true) {
				 clonedRigidBody = clonedWorld.addStaticRigidBody(
					rigidBody.id,
					rigidBody.enabled,
					rigidBody.typeId,
					rigidBody.transformations,
					obv,
					rigidBody.friction
				);
			} else {
				// update dynamic rigid body
				clonedRigidBody = clonedWorld.addRigidBody(
					rigidBody.id,
					rigidBody.enabled,
					rigidBody.typeId,
					rigidBody.transformations,
					obv,
					rigidBody.restitution,
					rigidBody.friction,
					rigidBody.mass,
					rigidBody.inverseInertia.clone()
				);				
			}

			// synch additional properties
			synch(clonedRigidBody, clonedRigidBody);
		}
		return clonedWorld;
	}

	/**
	 * Synch into cloned rigid body from rigid body
	 * @param cloned rigid body
	 * @param rigid body
	 */
	private void synch(RigidBody clonedRigidBody, RigidBody rigidBody) {
		// update properties
		clonedRigidBody.enabled = rigidBody.enabled;
		clonedRigidBody.isSleeping = rigidBody.isSleeping;
		clonedRigidBody.collisionTypeIds = rigidBody.collisionTypeIds;

		// 	check if obv has changed
		if (rigidBody.obv != null) {
			if (clonedRigidBody.obv.getClass() != rigidBody.obv.getClass()) {
				// yep, update
				clonedRigidBody.setBoundingVolume(rigidBody.obv.clone());
			}
			clonedRigidBody.cbv.fromBoundingVolume(rigidBody.cbv);
		}

		// ... properties
		clonedRigidBody.isStatic = rigidBody.isStatic;
		clonedRigidBody.mass = rigidBody.mass;
		clonedRigidBody.inverseMass = rigidBody.inverseMass;
		clonedRigidBody.force.set(rigidBody.force);
		clonedRigidBody.torque.set(rigidBody.torque);
		clonedRigidBody.orientation.set(rigidBody.orientation);
		clonedRigidBody.angularVelocity.set(rigidBody.angularVelocity);
		clonedRigidBody.linearVelocity.set(rigidBody.linearVelocity);
		clonedRigidBody.angularVelocityLast.set(rigidBody.angularVelocityLast);
		clonedRigidBody.movement.set(rigidBody.movement);
		clonedRigidBody.position.set(rigidBody.position);
		clonedRigidBody.worldInverseInertia.set(rigidBody.worldInverseInertia);		
		clonedRigidBody.transformations.fromTransformations(rigidBody.transformations);
	}

	/**
	 * Updates given world with this world
	 * 	Given world should be a clone of this world
	 * @param world
	 */
	public void synch(World world) {
		for (int i = 0; i < rigidBodiesDynamic.size(); i++) {
			RigidBody rigidBody = rigidBodiesDynamic.get(i);
			RigidBody clonedRigidBody = world.rigidBodiesById.get(rigidBody.id);
			if (clonedRigidBody == null) {
				System.out.println("Cloned world::entity '" + rigidBody.id + "' not found");
				continue;				
			}

			// synch rigid bodies
			synch(clonedRigidBody, rigidBody);

			// set up rigid body in partition
			if (clonedRigidBody.enabled == true) {
				world.partition.updateRigidBody(clonedRigidBody);
			} else {
				world.partition.removeRigidBody(clonedRigidBody);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "World [rigidBodies=" + rigidBodies + "]";
	}

	

}

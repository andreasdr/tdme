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

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Quaternion;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;

/**
 * Rigid Body class
 * 	ported from "game physics - a practical introduction/ben kenwright"
 * @author Andreas Drewke
 * @version $Id$
 */
public final class RigidBody {

	public final static int TYPEIDS_ALL = Integer.MAX_VALUE;

	private final static float VELOCITY_SLEEPTOLERANCE = 1.0f;
	private final static int SLEEPING_FRAMES = 5 * 60;

	protected PartitionQuadTree partition;

	protected int idx;
	protected String id;
	protected int typeId;
	protected int collisionTypeIds;

	//
	protected boolean enabled;

	//
	protected boolean isStatic;
	protected boolean isSleeping;
	private int sleepingFrameCount;

	protected Transformations transformations;
	protected BoundingVolume obv;
	protected BoundingVolume cbv;

	// friction from 0f - 1f 
	protected float friction;

	// how bouncy the object is from 0f - 1f
	protected float restitution;

	// rigid body mass
	protected float mass;
	protected float inverseMass;

	// rigid body movement
	protected Vector3 movement = new Vector3();

	// rigid body linear
	protected Vector3 position = new Vector3();
	protected Vector3 linearVelocity = new Vector3();
	protected Vector3 linearVelocityLast = new Vector3();
	protected Vector3 force = new Vector3();

	// rigid body angular
	protected Quaternion orientation = new Quaternion();
	protected Vector3 angularVelocity = new Vector3();
	protected Vector3 angularVelocityLast = new Vector3();
	protected Vector3 torque = new Vector3();

	//	
	protected Matrix4x4 inverseInertia = new Matrix4x4().identity().invert();

	// rigid body
	private Matrix4x4 orientationMatrix = new Matrix4x4().identity();
	protected Matrix4x4 worldInverseInertia = new Matrix4x4().identity();
	private Vector3 distance = new Vector3();

	// collision listener
	private ArrayList<CollisionListener> collisionListener = new ArrayList<CollisionListener>();

	//
	private Quaternion tmpQuaternion1 = new Quaternion();
	private Quaternion tmpQuaternion2 = new Quaternion();
	private Vector3 tmpVector3 = new Vector3();

	/**
	 * No rotation inertia matrix
	 * @param bv
	 * @param mass
	 * @return inertia matrix
	 */
	public static Matrix4x4 getNoRotationInertiaMatrix() {
		return new Matrix4x4(
				0,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				0f,
				1f
		);
	}

	/**
	 * Computes the inertia matrix
	 * @param bv
	 * @param mass
	 * @return inertia matrix
	 */
	public static Matrix4x4 computeInertiaMatrix(BoundingVolume bv, float mass, float scaleXAxis, float scaleYAxis, float scaleZAxis) {
		float width = bv.computeDimensionOnAxis(OrientedBoundingBox.AABB_AXIS_X);
		float height = bv.computeDimensionOnAxis(OrientedBoundingBox.AABB_AXIS_Y);
		float depth = bv.computeDimensionOnAxis(OrientedBoundingBox.AABB_AXIS_Z);
		return new Matrix4x4(
				scaleXAxis * 1f / 12f * mass * (height * height + depth * depth),
				0f,
				0f,
				0f,
				0f,
				scaleYAxis * 1f / 12f * mass * (width * width + depth * depth),
				0f,
				0f,
				0f,
				0f,
				scaleZAxis * 1f / 12f * mass * (width * width + height * height),
				0f,
				0f,
				0f,
				0f,
				1f
		).invert();
	}

	/**
	 * Constructor
	 * @param partition
	 * @param idx
	 * @param id
	 * @param enabled
	 * @param type id
	 * @param original bounding volume
	 * @param transformations
	 * @param restitution
	 * @param mass in kg
	 */
	public RigidBody(PartitionQuadTree partition, int idx, String id, boolean enabled, int typeId, BoundingVolume obv, Transformations transformations, float restitution, float friction, float mass, Matrix4x4 inverseInertia) {
		this.partition = partition;
		this.idx = idx;
		this.id = id;
		this.enabled = enabled;
		this.typeId = typeId;
		this.collisionTypeIds = TYPEIDS_ALL;
		this.transformations = new Transformations();
		this.inverseInertia = inverseInertia;
		this.restitution = restitution;
		this.friction = friction;
		this.isSleeping = false;
		this.sleepingFrameCount = 0;
		setBoundingVolume(obv);
		setMass(mass);
		synch(transformations);
		computeWorldInverseInertiaMatrix();
	}

	/**
	 * Set up index in rigid body array list
	 * @param idx
	 */
	public void setIdx(int idx) {
		this.idx = idx;
	}

	/**
	 * @return index in rigid body array list
	 */
	public int getIdx() {
		return idx;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return type id
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return collision type ids bitmask
	 */
	public int getCollisionTypeIds() {
		return collisionTypeIds;
	}

	/**
	 * Set up collision type ids
	 * @param collisionTypeIds
	 */
	public void setCollisionTypeIds(int collisionTypeIds) {
		this.collisionTypeIds = collisionTypeIds;
	}

	/**
	 * @return if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set up if rigid body is enabled
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == true) {
			partition.addRigidBody(this);
		} else {
			partition.removeRigidBody(this);
		}
		this.enabled = enabled;
	}

	/**
	 * @return object is static
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * @return if sleeping
	 */
	public boolean isSleeping() {
		return isSleeping;
	}

	/**
	 * @return transformations
	 */
	public Transformations getTransformations() {
		return transformations;
	}

	/**
	 * @return original bounding volume
	 */
	public BoundingVolume getBoudingVolume() {
		return obv;
	}

	/**
	 * Set up bounding volume
	 * @param obv
	 */
	public void setBoundingVolume(BoundingVolume obv) {
		this.obv = obv;
		this.cbv = this.obv == null?null:obv.clone();
	}

	/**
	 * @return transformed bounding volume
	 */
	public BoundingVolume getBoundingVolumeTransformed() {
		return cbv;
	}

	/**
	 * @return position
	 */
	public Vector3 getPosition() {
		return position;
	}

	/**
	 * @return last frame movement
	 */
	public Vector3 getMovement() {
		return movement;
	}

	/**
	 * @return friction
	 */
	public float getFriction() {
		return friction;
	}

	/**
	 * Set up friction
	 * @param friction
	 */
	public void setFriction(float friction) {
		this.friction = friction;
	}

	/**
	 * @return restitution / bouncyness
	 */
	public float getRestitution() {
		return restitution;
	}

	/**
	 * Set up restitution
	 * @param restitution
	 */
	public void setRestitution(float restitution) {
		this.restitution = restitution;
	}

	/**
	 * @return mass
	 */
	public float getMass() {
		return mass;
	}

	/**
	 * Set up mass
	 * @param mass
	 */
	public void setMass(float mass) {
		this.mass = mass;
		if (Math.abs(mass) < MathTools.EPSILON) {
			this.isStatic = true;
			this.inverseMass = 0f;
		} else {
			this.isStatic = false;
			this.inverseMass = 1f / mass;
		}
	}

	/**
	 * @return linear velocity
	 */
	public Vector3 getLinearVelocity() {
		return linearVelocity;
	}

	/**
	 * @return angular velocity
	 */
	public Vector3 getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * @return force
	 */
	public Vector3 getForce() {
		return force;
	}

	/**
	 * Wake up
	 */
	protected void awake(boolean resetFrameCount) {
		isSleeping = false;
		if (resetFrameCount) sleepingFrameCount = 0;
	}

	/**
	 * Put rigid body to sleep
	 */
	public void sleep() {
		isSleeping = true;
		sleepingFrameCount = 0;
	}

	/**
	 * Compute world inverse inertia
	 */
	private void computeWorldInverseInertiaMatrix() {
		orientation.computeMatrix(orientationMatrix);
		worldInverseInertia.
			set(orientationMatrix).
			transpose().
			multiply(inverseInertia).
			multiply(orientationMatrix);
	}

	/**
	 * Synchronizes this rigid body with transformations
	 * @param transformations
	 */
	public void synch(Transformations transformations) {
		this.transformations.fromTransformations(transformations);
		if (this.cbv != null) this.cbv.fromBoundingVolumeWithTransformations(this.obv, this.transformations);
		this.position.set(this.transformations.getTranslation());
		this.orientation.identity();
		for (int i = 0; i < this.transformations.getRotations().size(); i++) {
			Rotation r = this.transformations.getRotations().get(i);
			this.orientation.multiply(r.getQuaternion());
		}
		this.orientation.getArray()[1]*=-1f;
		this.orientation.normalize();
		this.awake(true);
	}

	/**
	 * Add force
	 * @param position of world force
	 * @param direction magnitude
	 */
	public void addForce(Vector3 forceOrigin, Vector3 force) {
		// skip on static objects
		if (isStatic == true) return;

		// check if we have any force to apply
		if (force.computeLength() < MathTools.EPSILON) return;

		// unset sleeping
		awake(false);

		// linear
		this.force.add(force);

		// angular
		distance.set(forceOrigin).sub(position);
		if (distance.computeLength() < MathTools.EPSILON) {
			Console.println("RigidBody::addForce(): " + id + ": Must not equals position");
		}
		Vector3.computeCrossProduct(force, distance, tmpVector3);
		this.torque.add(tmpVector3);
	}

	/**
	 * Updates this rigid body / integrates it
	 * @param delta time
	 */
	protected void update(float deltaTime) {
		if (isSleeping == true) return;

		// check if to put object into sleep
		if (linearVelocity.computeLength() < VELOCITY_SLEEPTOLERANCE &&
			angularVelocity.computeLength() < VELOCITY_SLEEPTOLERANCE) {
			sleepingFrameCount++;
			if (sleepingFrameCount >= SLEEPING_FRAMES) {
				sleep();
			}
		} else {
			awake(true);
		}

		// unset velocity if sleeping
		if (isSleeping == true) {
			linearVelocity.set(0f,0f,0f);
			angularVelocity.set(0f,0f,0f);
			return;
		}

		// linear
		movement.set(position);
		position.add(tmpVector3.set(linearVelocity).scale(deltaTime));
		movement.sub(position);
		movement.scale(-1f);

		// angular
		float[] angularVelocityXYZ = angularVelocity.getArray();
		tmpQuaternion2.set(
			angularVelocityXYZ[0],
			-angularVelocityXYZ[1],
			angularVelocityXYZ[2],
			0.0f).scale(0.5f * deltaTime);
		tmpQuaternion1.set(orientation);
		tmpQuaternion1.multiply(tmpQuaternion2);
		orientation.add(tmpQuaternion1);
		orientation.normalize();

		//
		force.set(0f,0f,0f);
		torque.set(0f,0f,0f);

		// store last velocities
		linearVelocityLast.set(linearVelocity);
		angularVelocityLast.set(angularVelocity);

		//
		computeWorldInverseInertiaMatrix();
	}

	/**
	 * @return if velocity has been changed
	 */
	protected boolean checkVelocityChange() {
		if (tmpVector3.set(linearVelocity).sub(linearVelocityLast).computeLength() > VELOCITY_SLEEPTOLERANCE) return true;
		if (tmpVector3.set(angularVelocity).sub(angularVelocityLast).computeLength() > VELOCITY_SLEEPTOLERANCE) return true;
		return false;
	}

	/**
	 * Add a collision listener to this rigid body
	 * @param listener
	 */
	public void addCollisionListener(CollisionListener listener) {
		collisionListener.add(listener);
	}

	/**
	 * Remove a collision listener to this rigid body
	 * @param listener
	 */
	public void removeCollisionListener(CollisionListener listener) {
		collisionListener.remove(listener);
	}

	/**
	 * Fire on collision 
	 * @param other
	 * @param collision response
	 */
	protected void fireOnCollision(RigidBody other, CollisionResponse collisionResponse) {
		for (int i = 0; i < collisionListener.size(); i++) {
			collisionListener.get(i).onCollision(this, other, collisionResponse);
		}
	}

	/**
	 * Fire on collision begin
	 * @param other
	 * @param collision response
	 */
	protected void fireOnCollisionBegin(RigidBody other, CollisionResponse collisionResponse) {
		for (int i = 0; i < collisionListener.size(); i++) {
			collisionListener.get(i).onCollisionBegin(this, other, collisionResponse);
		}
	}

	/**
	 * Fire on collision end
	 * @param other
	 * @param collision response
	 */
	protected void fireOnCollisionEnd(RigidBody other) {
		for (int i = 0; i < collisionListener.size(); i++) {
			collisionListener.get(i).onCollisionEnd(this, other);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "RigidBody [id=" + id + "]";
	}

}
	
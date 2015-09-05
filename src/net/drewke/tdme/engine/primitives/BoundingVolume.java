package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.Vector3;

/**
 * Bounding Volume Interface
 * @author Andreas Drewke
 * @version $Id$
 */
public interface BoundingVolume {

	/**
	 * Set up this bounding volume from given bounding volume
	 * @param original
	 */
	public void fromBoundingVolume(BoundingVolume original);

	/**
	 * Create bounding volume from given original(of same type) with applied transformations
	 * @param original bounding volume
	 * @param transformations
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations);

	/**
	 * Computes closest point on bounding volume for given point 
	 * @param point
	 * @param clostest point
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestsPoint);

	/**
	 * Checks if point is in bounding volume
	 * @param point
	 * @return bool if point is in bounding volume
	 */
	public boolean containsPoint(Vector3 point);

	/**
	 * Check if this bounding volume collides with bounding volume 2
	 * @param bounding volume 2
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doesCollideWith(BoundingVolume bv2, Vector3 movement, CollisionResponse collision);

	/**
	 * @return center of bounding volume
	 */
	public Vector3 getCenter();

	/**
	 * @return radius of bounding volume if regarded as sphere
	 */
	public float getSphereRadius();

	/**
	 * Computes dimension on axis
	 * @param axis
	 * @return dimension on axis
	 */
	public float computeDimensionOnAxis(Vector3 axis);

	/**
	 * Updates the bounding volume
	 */
	public void update();

	/**
	 * Clones this bounding volume
	 * @return cloned bounding volume
	 */
	public BoundingVolume clone();

}

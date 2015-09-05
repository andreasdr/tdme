package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Sphere
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Sphere implements BoundingVolume {

	protected Vector3 center;
	protected float radius;
	private Vector3 axis;

	/**
	 * Creates a sphere bounding volume
	 * @param center
	 * @param radius
	 * @return bounding volume
	 */
	public static BoundingVolume createBoundingVolume(Vector3 center, float radius) {
		return new Sphere(center, radius);
	}

	/**
	 * Public constructor
	 */
	public Sphere() {
		this.center = new Vector3();
		this.radius = 0f;
		this.axis = new Vector3();
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param center
	 * @param radius
	 */
	public Sphere(Vector3 center, float radius) {
		this.center = center;
		this.radius = radius;
		this.axis = new Vector3();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof Sphere == false) {
			System.out.println("Sphere::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Sphere sphere = (Sphere)original;

		//
		center.set(sphere.center);
		radius = sphere.radius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof Sphere == false) {
			System.out.println("Sphere::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Sphere sphere = (Sphere)original;

		//
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();

		// apply translations
		// 	translate center
		transformationsMatrix.multiply(sphere.center, center);

		// note:
		//	sphere radius can only be scaled the same on all axes
		//	thats why its enough to only take x axis to determine scaling
		axis.set(sphere.center).addX(sphere.radius);
		transformationsMatrix.multiply(axis, axis);
		radius = axis.sub(center).computeLength();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getCenter()
	 */
	public Vector3 getCenter() {
		return center;
	}

	/**
	 * @return float radius
	 */
	public float getRadius() {
		return radius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getSphereRadius()
	 */
	public float getSphereRadius() {
		return radius;
	}

	/**
	 * Set up radius
	 * @param radius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * Set up sphere
	 * @param center
	 * @param radius
	 * @return this sphere
	 */
	public Sphere set(Vector3 center, float radius) {
		this.center.set(center);
		this.radius = radius;
		this.update();
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestPoint) {
		axis.set(point).sub(center);
		float length = point.computeLength();
		if (length <= radius) {
			closestPoint.set(point);
		} else {
			closestPoint.set(axis).normalize().scale(radius);
			closestPoint.add(center);
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 */
	public boolean containsPoint(Vector3 point) {
		float distance = point.clone().sub(center).computeLength();
		return distance <= radius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#doesCollideWith(net.drewke.tdme.engine.primitives.BoundingVolume, net.drewke.tdme.math.Vector3, net.drewke.tdme.engine.physics.CollisionResponse)
	 */
	public boolean doesCollideWith(BoundingVolume bv2, Vector3 movement, CollisionResponse collision) {
		if (bv2 instanceof BoundingBox) {
			return CollisionDetection.getInstance().doCollide(this, (BoundingBox)bv2, movement, collision);
		} else
		if (bv2 instanceof OrientedBoundingBox) {
			return CollisionDetection.getInstance().doCollide(this, (OrientedBoundingBox)bv2, movement, collision);
		} else
		if (bv2 instanceof Sphere) {
			return CollisionDetection.getInstance().doCollide(this, (Sphere)bv2, movement, collision);
		} else
		if (bv2 instanceof Capsule) {
			return CollisionDetection.getInstance().doCollide(this, (Capsule)bv2, movement, collision);
		} else
		if (bv2 instanceof Triangle) {
			return CollisionDetection.getInstance().doCollide(this, (Triangle)bv2, movement, collision);
		} else
		if (bv2 instanceof ConvexMesh) {
			return CollisionDetection.getInstance().doCollide(this, (ConvexMesh)bv2, movement, collision);
		} else {
			System.out.println("Sphere::doesCollideWith(): unsupported bounding volume 2: " + bv2);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeDimensionOnAxis(net.drewke.tdme.math.Vector3)
	 */
	public float computeDimensionOnAxis(Vector3 axis) {
		return radius * 2f;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		return new Sphere(center.clone(), radius);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Sphere [center=" + center + ", radius=" + radius + "]";
	}

}

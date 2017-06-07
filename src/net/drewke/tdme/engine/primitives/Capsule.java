package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;

/**
 * Capsule primitive
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Capsule implements BoundingVolume {

	protected Vector3 a;
	protected Vector3 b;
	protected float radius;
	protected Vector3 center;
	protected float sphereRadius;

	private Vector3 baSub;
	private Vector3 paSub;
	private Vector3 side;
	private Vector3 cpCvsP;

	/**
	 * Creates a capsule bounding volume
	 * @param a
	 * @param b
	 * @param radius
	 * @return bounding volume
	 */
	public static BoundingVolume createBoundingVolume(Vector3 a, Vector3 b, float radius) {
		return new Capsule(a, b, radius);
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param a
	 * @param b
	 * @param radius
	 */
	public Capsule(Vector3 a, Vector3 b, float radius) {
		this.a = a;
		this.b = b;
		this.radius = radius;
		this.side = new Vector3();
		this.baSub = new Vector3();
		this.paSub = new Vector3();
		this.center = new Vector3();
		this.cpCvsP = new Vector3();
		update();
	}

	/**
	 * @return radius
	 */
	public float getRadius() {
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
	 * @return line segment point a
	 */
	public Vector3 getA() {
		return a;
	}

	/**
	 * @return line segment point b
	 */
	public Vector3 getB() {
		return b;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof Capsule == false) {
			Console.println("Capsule::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Capsule capsule = (Capsule)original;

		// 
		a.set(capsule.a);
		b.set(capsule.b);
		center.set(capsule.center);
		radius = capsule.radius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof Capsule == false) {
			Console.println("Capsule::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Capsule capsule = (Capsule)original;

		//
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();
		transformationsMatrix.multiply(capsule.a, a);
		transformationsMatrix.multiply(capsule.b, b);

		// note:
		//	capsule radius can only be scaled the same on all axes
		//	thats why its enough to only take x axis to determine scaling
		side.set(capsule.a).addX(capsule.radius);
		transformationsMatrix.multiply(side, side);
		radius = side.sub(a).computeLength();

		//
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		baSub.set(b).sub(a); 
		float baSubLength = baSub.computeLength();
		center.set(a).add(baSub.normalize().scale(baSubLength * 0.5f));
		sphereRadius = baSubLength / 2f + radius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getCenter()
	 */
	public Vector3 getCenter() {
		return center;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getSphereRadius()
	 */
	public float getSphereRadius() {
		return sphereRadius;
	}

	/*
	 * based on an algorithm from "game physics - a practical introduction/ben kenwright"
	 * 
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestPoint) {
		baSub.set(b).sub(a); 
		float baSubLength = baSub.computeLength();
		if (baSubLength > 0f) {
			baSub.normalize();
			float t = 0f;
			t = Vector3.computeDotProduct(
				paSub.set(point).sub(a),
				baSub
			) / baSubLength;
			if (t < 0f) t = 0f;
			if (t > 1f) t = 1f;
			closestPoint.set(a).add(baSub.scale(t * baSubLength));
		} else {
			closestPoint.set(a);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 */
	public boolean containsPoint(Vector3 point) {
		computeClosestPointOnBoundingVolume(point, cpCvsP);
		float distance = cpCvsP.sub(point).computeLength();
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
			Console.println("Capsule::doesCollideWith(): unsupported bounding volume 2: " + bv2);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeDimensionOnAxis(net.drewke.tdme.math.Vector3)
	 */
	public float computeDimensionOnAxis(Vector3 axis) {
		return Math.abs(Vector3.computeDotProduct(baSub.set(b).sub(a), axis)) + (radius * 2f);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		return new Capsule(a.clone(), b.clone(), radius);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Capsule [a=" + a + ", b=" + b + ", radius=" + radius + "]";
	}

}

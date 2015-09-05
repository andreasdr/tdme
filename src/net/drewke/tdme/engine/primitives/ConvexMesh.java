package net.drewke.tdme.engine.primitives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.Vector3;

/**
 * Convex mesh collision object
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ConvexMesh implements BoundingVolume {

	private Triangle[] triangles;
	protected Vector3[] vertices;
	protected Vector3 center;
	protected Vector3 distanceVector;
	protected Vector3 closestsPoint;
	protected float sphereRadius;

	/**
	 * Public constructor
	 * @param model
	 */
	public ConvexMesh(Triangle[] triangles) {
		this.center = new Vector3();
		this.distanceVector = new Vector3();
		this.closestsPoint = new Vector3();
		this.triangles = triangles;
		update();
		createVertices();
	}

	/**
	 * Public constructor
	 * @param model
	 */
	public ConvexMesh(Object3DModel model) {
		center = new Vector3();
		distanceVector = new Vector3();
		closestsPoint = new Vector3();
		triangles = model.getFaceTriangles();
		update();
		createVertices();
	}

	/**
	 * Create vertices
	 */
	private void createVertices() {
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		// iterate triangles
		for (int i = 0; i < triangles.length; i++) {
			// iterate triangle vertices
			for (int j = 0; j < triangles[i].vertices.length; j++) {
				// check if we already have this vertex
				boolean haveVertex = false;
				for (int k = 0; k < vertices.size(); k++) {
					if (vertices.get(k).equals(triangles[i].vertices[j]) == true) {
						haveVertex = true;
					}
				}
				if (haveVertex == false) vertices.add(triangles[i].vertices[j]);
			}
		}
		this.vertices = new Vector3[vertices.size()];
		vertices.toArray(this.vertices);
	}

	/**
	 * @return triangles
	 */
	public Triangle[] getTriangles() {
		return triangles;
	}

	/**
	 * @return mesh vertices
	 */
	public Vector3[] getVertices() {
		return vertices;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof ConvexMesh == false) {
			System.out.println("Mesh::fromBoundingVolume(): original is not of same type");
			return;
		}

		ConvexMesh mesh = (ConvexMesh)original;
		if (mesh.triangles.length != triangles.length) {
			System.out.println("Mesh::fromBoundingVolume(): triangles count mismatch");
			return;			
		}

		// set up triangles from original
		for (int i = 0; i < triangles.length; i++) {
			triangles[i].fromBoundingVolume(mesh.triangles[i]);
		}

		// center
		center.set(mesh.center);

		// sphere radius
		sphereRadius = mesh.sphereRadius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.engine.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof ConvexMesh == false) {
			System.out.println("Mesh::fromBoundingVolume(): original is not of same type");
			return;
		}

		ConvexMesh mesh = (ConvexMesh)original;
		if (mesh.triangles.length != triangles.length) {
			System.out.println("Mesh::fromBoundingVolume(): triangles count mismatch");
			return;			
		}

		// set up triangles from original
		for (int i = 0; i < triangles.length; i++) {
			transformations.getTransformationsMatrix().multiply(mesh.triangles[i].vertices[0], triangles[i].vertices[0]);
			transformations.getTransformationsMatrix().multiply(mesh.triangles[i].vertices[1], triangles[i].vertices[1]);
			transformations.getTransformationsMatrix().multiply(mesh.triangles[i].vertices[2], triangles[i].vertices[2]);
			triangles[i].update();
		}
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestsPoint) {
		if (triangles.length == 0) {
			return;
		}
		triangles[0].computeClosestPointOnBoundingVolume(point, closestsPoint);
		float distance = distanceVector.set(point).sub(closestsPoint).computeLength();
		for (int i = 1; i < triangles.length; i++) {
			triangles[i].computeClosestPointOnBoundingVolume(point, this.closestsPoint);
			float _distance = distanceVector.set(point).sub(this.closestsPoint).computeLength();
			if (_distance < distance) {
				closestsPoint.set(this.closestsPoint);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 */
	public boolean containsPoint(Vector3 point) {
		for (int i = 0; i < triangles.length; i++) {
			if (triangles[i].containsPoint(point) == true) return true;
		}
		return false;
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
			System.out.println("Capsule::doesCollideWith(): unsupported bounding volume 2: " + bv2);
			return false;
		}
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
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeDimensionOnAxis(net.drewke.tdme.math.Vector3)
	 */
	public float computeDimensionOnAxis(Vector3 axis) {
		float dimensionOnAxis = 0f;
		for (int i = 0; i < triangles.length; i++) {
			float _dimensionOnAxis = triangles[i].computeDimensionOnAxis(axis);
			if (_dimensionOnAxis > dimensionOnAxis) dimensionOnAxis = _dimensionOnAxis;
		}
		return dimensionOnAxis;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		// center
		center.set(0f,0f,0f);
		for (int i = 0; i < triangles.length; i++) {
			center.add(triangles[i].vertices[0]);
			center.add(triangles[i].vertices[1]);
			center.add(triangles[i].vertices[2]);
		}
		center.scale(1f / (triangles.length * 3f));

		// sphere radius
		this.sphereRadius = 0f;
		for (int i = 0; i < triangles.length; i++)
		for (int j = 0; j < 3; j++) {
			float _sphereRadius = distanceVector.set(center).sub(triangles[i].vertices[j]).computeLength();
			if (_sphereRadius > sphereRadius) sphereRadius = _sphereRadius;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		Triangle[] triangles = new Triangle[this.triangles.length];
		for (int i = 0; i < this.triangles.length; i++) {
			triangles[i] = (Triangle)this.triangles[i].clone();
		}
		return new ConvexMesh(triangles);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return
			"Mesh [center=" + center +
			", sphereRadius=" + sphereRadius +
			", triangles=" + Arrays.toString(triangles) + 
			"]";
	}

}

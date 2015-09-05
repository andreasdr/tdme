package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Axis Aligned Bounding Box
 * @author Andreas Drewke
 * @version $Id$
 */
public final class BoundingBox implements BoundingVolume {

	protected static int[][] facesVerticesIndexes = {
		{0,4,7}, {7,3,0},
		{6,5,1}, {1,2,6},
		{5,4,0}, {0,1,5},
		{3,7,6}, {6,2,3},
		{2,1,0}, {0,3,2},
		{4,5,6}, {6,7,4},
	};

	protected Vector3 min;
	protected Vector3 max;
	protected Vector3 center;
	private float sphereRadius;
	private Vector3 vertices[];
	private Vector3 halfExtension;

	/**
	 * Creates a bounding volume bounding volume
	 * @param min
	 * @param max
	 * @return bounding volume
	 */
	public static BoundingVolume createBoundingVolume(Vector3 min, Vector3 max) {
		return new BoundingBox(min, max);
	}

	/**
	 * Public constructor
	 */
	public BoundingBox() {
		min = new Vector3();
		max = new Vector3();
		center = new Vector3();
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		halfExtension = new Vector3();
		update();
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param bounding box
	 */
	public BoundingBox(BoundingBox boundingBox) {
		this.min = boundingBox.min.clone();
		this.max = boundingBox.max.clone();
		this.center = new Vector3();
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		halfExtension = new Vector3();
		update();
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param min vector
	 * @param max vector
	 */
	public BoundingBox(Vector3 min, Vector3 max) {
		this.min = min;
		this.max = max;
		this.center = new Vector3();
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		halfExtension = new Vector3();
		update();
	}

	/**
	 * @return min x,y,z vertex
	 */
	public Vector3 getMin() {
		return min;
	}

	/**
	 * @return max x,y,z vertex
	 */
	public Vector3 getMax() {
		return max;
	}

	/**
	 * Returns bounding box vertices
	 * @param bounding box
	 * @return vertices
	 */
	public Vector3[] getVertices() {
		return vertices;
	}

	/**
	 * @return faces vertices indexes
	 */
	public static int[][] getFacesVerticesIndexes() {
		return facesVerticesIndexes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof BoundingBox == false) {
			System.out.println("BoundingBox::fromBoundingVolume: original is not of same type");
			return;
		}

		BoundingBox boundingBox = (BoundingBox)original;
		min.set(boundingBox.min);
		max.set(boundingBox.max);
		center.set(boundingBox.center);
		for (int i = 0; i < vertices.length; i++) vertices[i].set(boundingBox.vertices[i]);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof BoundingBox == false) {
			System.out.println("BoundingBox::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		BoundingBox boundingBox = (BoundingBox)original;

		//
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();
		Vector3 _vertices[] = boundingBox.getVertices();

		// apply transformations from original vertices to local vertices
		for(int i = 0; i < vertices.length; i++) {
			transformationsMatrix.multiply(_vertices[i], vertices[i]);
		}

		// determine axis aligned bounding box constraints based on local vertices
		float[] vertexXYZ = vertices[0].getArray(); 
		float minX = vertexXYZ[0], minY = vertexXYZ[1], minZ = vertexXYZ[2];
		float maxX = vertexXYZ[0], maxY = vertexXYZ[1], maxZ = vertexXYZ[2];
		for(int vertexIndex = 1; vertexIndex < vertices.length; vertexIndex++) {
			Vector3 vertex = vertices[vertexIndex];
			vertexXYZ = vertex.getArray(); 
			if (vertexXYZ[0] < minX) minX = vertexXYZ[0];
			if (vertexXYZ[1] < minY) minY = vertexXYZ[1];
			if (vertexXYZ[2] < minZ) minZ = vertexXYZ[2];
			if (vertexXYZ[0] > maxX) maxX = vertexXYZ[0];
			if (vertexXYZ[1] > maxY) maxY = vertexXYZ[1];
			if (vertexXYZ[2] > maxZ) maxZ = vertexXYZ[2];				
		}

		// set up new aabb
		min.set(minX, minY, minZ);
		max.set(maxX, maxY, maxZ);

		// compute new vertices based on aabb constraints
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestPoint) {
		float pointXYZ[] = point.getArray();
		float minXYZ[] = min.getArray();
		float maxXYZ[] = max.getArray();
		float closestX = pointXYZ[0] < minXYZ[0]?minXYZ[0]:pointXYZ[0] > maxXYZ[0]?maxXYZ[0]:pointXYZ[0];
		float closestY = pointXYZ[1] < minXYZ[1]?minXYZ[1]:pointXYZ[1] > maxXYZ[1]?maxXYZ[1]:pointXYZ[1];
		float closestZ = pointXYZ[2] < minXYZ[2]?minXYZ[2]:pointXYZ[2] > maxXYZ[2]?maxXYZ[2]:pointXYZ[2];
		closestPoint.set(
			closestX,
			closestY,
			closestZ
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 */
	public boolean containsPoint(Vector3 point) {
		float pointXYZ[] = point.getArray();
		float minXYZ[] = min.getArray();
		float maxXYZ[] = max.getArray();
		for (int i = 0; i < 3; i++) {
			if (pointXYZ[i] < minXYZ[i]) return false;
			if (pointXYZ[i] > maxXYZ[i]) return false;
		}
		return true;
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
			System.out.println("BoundingBox::doesCollideWith(): unsupported bounding volume 2: " + bv2);
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
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getRadiusSquared()
	 */
	public float getSphereRadius() {
		return sphereRadius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeDimensionOnAxis(net.drewke.tdme.math.Vector3)
	 */
	public float computeDimensionOnAxis(Vector3 axis) {
		float vertexOnAxis = Vector3.computeDotProduct(vertices[0], axis);
		float min = vertexOnAxis;
		float max = vertexOnAxis;
		for (int i = 1; i < vertices.length; i++) {
			vertexOnAxis = Vector3.computeDotProduct(vertices[i], axis);
			if (vertexOnAxis < min) min = vertexOnAxis;
			if (vertexOnAxis > max) max = vertexOnAxis;
		}
		return Math.abs(max-min);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		float minXYZ[] = min.getArray();
		float maxXYZ[] = max.getArray();

		// near, left, top 
		vertices[0].set(minXYZ[0], minXYZ[1], minXYZ[2]);
		// near, right, top 
		vertices[1].set(maxXYZ[0], minXYZ[1], minXYZ[2]);
		// near, right, bottom
		vertices[2].set(maxXYZ[0], maxXYZ[1], minXYZ[2]);
		// near, left, bottom
		vertices[3].set(minXYZ[0], maxXYZ[1], minXYZ[2]);
		// far, left, top
		vertices[4].set(minXYZ[0], minXYZ[1], maxXYZ[2]);
		// far, right, top
		vertices[5].set(maxXYZ[0], minXYZ[1], maxXYZ[2]);
		// far, right, bottom
		vertices[6].set(maxXYZ[0], maxXYZ[1], maxXYZ[2]);
		// far, left, bottom
		vertices[7].set(minXYZ[0], maxXYZ[1], maxXYZ[2]);

		//
		center.set(min).add(max).scale(0.5f);

		//
		halfExtension.set(max).sub(min).scale(0.5f);
		sphereRadius = halfExtension.computeLength();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		return new BoundingBox(min.clone(), max.clone());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "BoundingBox [min=" + min + ", max=" + max + "]";
	}

}
package net.drewke.tdme.engine.primitives;

import java.util.Arrays;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;

/**
 * Oriented Bounding Box
 * @author Andreas Drewke
 * @version $Id$
 */
public final class OrientedBoundingBox implements BoundingVolume {

	public final static Vector3 AABB_AXIS_X = new Vector3(1f,0f,0f);
	public final static Vector3 AABB_AXIS_Y = new Vector3(0f,1f,0f);
	public final static Vector3 AABB_AXIS_Z = new Vector3(0f,0f,1f);

	protected static int[][] facesVerticesIndexes = {
		{0,4,7}, {7,3,0},
		{6,5,1}, {1,2,6},
		{5,4,0}, {0,1,5},
		{3,7,6}, {6,2,3},
		{2,1,0}, {0,3,2},
		{4,5,6}, {6,7,4},
	};

	protected Vector3 center;
	protected Vector3[] axes;
	protected Vector3 halfExtension;
	protected Vector3[] vertices;
	protected Vector3 axis = new Vector3();
	protected Vector3[] axisTransformed = new Vector3[] {new Vector3(), new Vector3(), new Vector3()};
	protected Vector3 direction = new Vector3();
	protected Vector3 scale = new Vector3();
	protected float sphereRadius;

	/**
	 * Creates a oriented bounding box bounding volume
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 * @return bounding volume
	 */
	public static BoundingVolume createBoundingVolume(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		return new OrientedBoundingBox(
			center,
			axis0,
			axis1,
			axis2,
			halfExtension
		);
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 */
	public OrientedBoundingBox(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		this.center = center;
		this.axes = new Vector3[3];
		this.axes[0] = axis0;
		this.axes[1] = axis1;
		this.axes[2] = axis2;
		this.halfExtension = halfExtension;
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		update();
	}

	/**
	 * Protected constructor
	 * @param bounding box
	 */
	public OrientedBoundingBox(BoundingBox bb) {
		this.halfExtension = new Vector3().set(bb.getMax()).sub(bb.getMin()).scale(0.5f);
		this.center = new Vector3().set(bb.getMin()).add(halfExtension);
		this.axes = new Vector3[3];
		this.axes[0] = AABB_AXIS_X.clone();
		this.axes[1] = AABB_AXIS_Y.clone();
		this.axes[2] = AABB_AXIS_Z.clone();
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		update();
	}

	/**
	 * Public constructor
	 * @param bounding box
	 */
	public OrientedBoundingBox() {
		this.halfExtension = new Vector3(0f,0f,0f);
		this.center = new Vector3(0f,0f,0f);
		this.axes = new Vector3[3];
		this.axes[0] = AABB_AXIS_X.clone();
		this.axes[1] = AABB_AXIS_Y.clone();
		this.axes[2] = AABB_AXIS_Z.clone();
		this.vertices = new Vector3[8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new Vector3();
		}
		update();
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

	/**
	 * @return 3 axes
	 */
	public Vector3[] getAxes() {
		return axes;
	}

	/**
	 * @return half extension
	 */
	public Vector3 getHalfExtension() {
		return halfExtension;
	}

	/**
	 * Set up oriented bounding box from bounding box
	 * @param bb
	 */
	public void fromBoundingBox(BoundingBox bb) {
		this.halfExtension.set(bb.getMax()).sub(bb.getMin()).scale(0.5f);
		this.center.set(bb.getMin()).add(halfExtension);
		this.axes[0].set(AABB_AXIS_X);
		this.axes[1].set(AABB_AXIS_Y);
		this.axes[2].set(AABB_AXIS_Z);
		update();
	}

	/**
	 * Set up oriented bounding box from oriented bounding box
	 * @param bb
	 */
	public void fromOrientedBoundingBox(OrientedBoundingBox obb) {
		this.halfExtension.set(obb.halfExtension);
		this.center.set(obb.center);
		this.axes[0].set(obb.axes[0]);
		this.axes[1].set(obb.axes[1]);
		this.axes[2].set(obb.axes[2]);
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof OrientedBoundingBox == false) {
			Console.println("OrientedBoundingBox::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		OrientedBoundingBox obb = (OrientedBoundingBox)original;

		//
		center.set(obb.center);
		for (int i = 0; i < axes.length; i++) axes[i].set(obb.axes[i]);
		halfExtension.set(obb.halfExtension);
		for (int i = 0; i < vertices.length; i++) vertices[i].set(obb.vertices[i]);		
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof OrientedBoundingBox == false) {
			Console.println("OrientedBoundingBox::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		OrientedBoundingBox obb = (OrientedBoundingBox)original;

		//
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();

		// apply rotation, scale, translation
		transformationsMatrix.multiply(obb.center, center);

		// apply transformations rotation + scale to axis
		transformationsMatrix.multiplyNoTranslation(obb.axes[0], axisTransformed[0]);
		transformationsMatrix.multiplyNoTranslation(obb.axes[1], axisTransformed[1]);
		transformationsMatrix.multiplyNoTranslation(obb.axes[2], axisTransformed[2]);

		// set up axes
		axes[0].set(axisTransformed[0]).normalize();
		axes[1].set(axisTransformed[1]).normalize();
		axes[2].set(axisTransformed[2]).normalize();

		// apply scale to half extension
		halfExtension.set(obb.halfExtension);
		halfExtension.scale(
			scale.set(
				axisTransformed[0].computeLength(),
				axisTransformed[1].computeLength(),
				axisTransformed[2].computeLength()
			)
		);

		// compute vertices
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		float[] halfExtensionXYZ = halfExtension.getArray();

		// just for my imagination
		//	near left top
		vertices[0].set(center);
		vertices[0].add(axis.set(axes[0]).scale(-halfExtensionXYZ[0]));
		vertices[0].add(axis.set(axes[1]).scale(-halfExtensionXYZ[1]));
		vertices[0].add(axis.set(axes[2]).scale(-halfExtensionXYZ[2]));

		// just for my imagination
		//	near right top
		vertices[1].set(center);
		vertices[1].add(axis.set(axes[0]).scale(+halfExtensionXYZ[0]));
		vertices[1].add(axis.set(axes[1]).scale(-halfExtensionXYZ[1]));
		vertices[1].add(axis.set(axes[2]).scale(-halfExtensionXYZ[2]));

		// just for my imagination
		//	near right bottom
		vertices[2].set(center);
		vertices[2].add(axis.set(axes[0]).scale(+halfExtensionXYZ[0]));
		vertices[2].add(axis.set(axes[1]).scale(+halfExtensionXYZ[1]));
		vertices[2].add(axis.set(axes[2]).scale(-halfExtensionXYZ[2]));

		// just for my imagination
		//	near left bottom
		vertices[3].set(center);
		vertices[3].add(axis.set(axes[0]).scale(-halfExtensionXYZ[0]));
		vertices[3].add(axis.set(axes[1]).scale(+halfExtensionXYZ[1]));
		vertices[3].add(axis.set(axes[2]).scale(-halfExtensionXYZ[2]));

		// just for my imagination
		//	far left top
		vertices[4].set(center);
		vertices[4].add(axis.set(axes[0]).scale(-halfExtensionXYZ[0]));
		vertices[4].add(axis.set(axes[1]).scale(-halfExtensionXYZ[1]));
		vertices[4].add(axis.set(axes[2]).scale(+halfExtensionXYZ[2]));

		// just for my imagination
		//	far right top
		vertices[5].set(center);
		vertices[5].add(axis.set(axes[0]).scale(+halfExtensionXYZ[0]));
		vertices[5].add(axis.set(axes[1]).scale(-halfExtensionXYZ[1]));
		vertices[5].add(axis.set(axes[2]).scale(+halfExtensionXYZ[2]));

		// just for my imagination
		//	far right bottom
		vertices[6].set(center);
		vertices[6].add(axis.set(axes[0]).scale(+halfExtensionXYZ[0]));
		vertices[6].add(axis.set(axes[1]).scale(+halfExtensionXYZ[1]));
		vertices[6].add(axis.set(axes[2]).scale(+halfExtensionXYZ[2]));

		// just for my imagination
		//	far left bottom
		vertices[7].set(center);
		vertices[7].add(axis.set(axes[0]).scale(-halfExtensionXYZ[0]));
		vertices[7].add(axis.set(axes[1]).scale(+halfExtensionXYZ[1]));
		vertices[7].add(axis.set(axes[2]).scale(+halfExtensionXYZ[2]));

		//
		sphereRadius = halfExtension.computeLength();
	}

	/**
	 * @return oriented bounding box vertices
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
	 * based on an algorithm from "Real-Time Collision Detection" / Ericson
	 * 	Credit:
	 * 		"From Real-Time Collision Detection by Christer Ericson
	 * 		published by Morgan Kaufman Publishers, (c) 2005 Elsevier Inc"
	 * 
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestPoint) {
		direction.set(point).sub(center);
		closestPoint.set(center);
		float[] halfExtensionXYZ = halfExtension.getArray();
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > halfExtensionXYZ[i]) distance = halfExtensionXYZ[i];
			if (distance < -halfExtensionXYZ[i]) distance = -halfExtensionXYZ[i];
			closestPoint.add(axis.set(axes[i]).scale(distance)); 
		}
	}

	/**
	 * Computes nearest point on obb face from point in obb
	 * @param point in obb
	 * @param point on face
	 */
	public void computeNearestPointOnFaceBoundingVolume(Vector3 pointInObb, Vector3 pointOnFace) {
		direction.set(pointInObb).sub(center);
		float[] halfExtensionXYZ = halfExtension.getArray();

		float axisMinPenetration = 10000f;
		int axisIdxLeastPenetration = 0;

		// detemine axis with min penetration
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > halfExtensionXYZ[i]) distance = halfExtensionXYZ[i];
			if (distance < -halfExtensionXYZ[i]) distance = -halfExtensionXYZ[i];
			float penetration;
			if (distance >= 0f) {
				penetration = halfExtensionXYZ[i] - distance;
			} else {
				penetration = halfExtensionXYZ[i] + distance;
			}
			// determine axis with min penetration
			if (penetration < axisMinPenetration) {
				axisMinPenetration = penetration;
				axisIdxLeastPenetration = i;
			}
		}

		//
		computeNearestPointOnFaceBoundingVolumeAxis(axisIdxLeastPenetration, pointInObb, pointOnFace);
	}

	/**
	 * Computes nearest point on obb face from point in obb on given axis
	 * @param axis idx
	 * @param point in obb
	 * @param point on face
	 */
	public void computeNearestPointOnFaceBoundingVolumeAxis(int axisIdx, Vector3 pointInObb, Vector3 pointOnFace) {
		direction.set(pointInObb).sub(center);
		float[] halfExtensionXYZ = halfExtension.getArray();

		// compute point on obb face
		pointOnFace.set(center);
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > halfExtensionXYZ[i]) distance = halfExtensionXYZ[i];
			if (distance < -halfExtensionXYZ[i]) distance = -halfExtensionXYZ[i];
			if (i == axisIdx) {
				if (distance >= 0f) {
					pointOnFace.add(axis.set(axes[i]).scale(+halfExtensionXYZ[i]));
				} else {
					pointOnFace.add(axis.set(axes[i]).scale(-halfExtensionXYZ[i]));
				}
			} else {
				pointOnFace.add(axis.set(axes[i]).scale(distance));
			}
		}
	}

	/**
	 * Computes nearest point on obb face from point in obb
	 * @param point in obb
	 * @param point on face
	 */
	public void computeOppositePointOnFaceBoundingVolume(Vector3 pointInObb, Vector3 pointOnFace) {
		direction.set(pointInObb).sub(center);
		float[] halfExtensionXYZ = halfExtension.getArray();

		float axisMinPenetration = 10000f;
		int axisIdxLeastPenetration = 0;

		// detemine axis with min penetration
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > halfExtensionXYZ[i]) distance = halfExtensionXYZ[i];
			if (distance < -halfExtensionXYZ[i]) distance = -halfExtensionXYZ[i];
			float penetration;
			if (distance >= 0f) {
				penetration = halfExtensionXYZ[i] - distance;
			} else {
				penetration = halfExtensionXYZ[i] + distance;
			}
			// determine axis with min penetration
			if (penetration < axisMinPenetration) {
				axisMinPenetration = penetration;
				axisIdxLeastPenetration = i;
			}
		}

		// compute point on obb face
		computeOppositePointOnFaceBoundingVolumeAxis(axisIdxLeastPenetration, pointInObb, pointOnFace);
	}

	/**
	 * Computes nearest point on obb face from point in obb on given axis
	 * @param axis idx
	 * @param point in obb
	 * @param point on face
	 */
	public void computeOppositePointOnFaceBoundingVolumeAxis(int axisIdx, Vector3 pointInObb, Vector3 pointOnFace) {
		direction.set(pointInObb).sub(center);
		float[] halfExtensionXYZ = halfExtension.getArray();

		// compute point on obb face
		pointOnFace.set(center);
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > halfExtensionXYZ[i]) distance = halfExtensionXYZ[i];
			if (distance < -halfExtensionXYZ[i]) distance = -halfExtensionXYZ[i];
			if (i == axisIdx) {
				if (distance >= 0f) {
					pointOnFace.add(axis.set(axes[i]).scale(-halfExtensionXYZ[i]));
				} else {
					pointOnFace.add(axis.set(axes[i]).scale(+halfExtensionXYZ[i]));
				}
			} else {
				pointOnFace.add(axis.set(axes[i]).scale(distance));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 * 
	 * 	based on an algorithm from "Real-Time Collision Detection" / Ericson
	 * 	Credit:
	 * 		"From Real-Time Collision Detection by Christer Ericson
	 * 		published by Morgan Kaufman Publishers, (c) 2005 Elsevier Inc"
	 */
	public boolean containsPoint(Vector3 point) {
		direction.set(point).sub(center);
		float[] halfExtensionXYZ = halfExtension.getArray();
		for (int i = 0; i < axes.length; i++) {
			float distance = Vector3.computeDotProduct(direction, axes[i]);
			if (distance > 0f) distance+= -MathTools.EPSILON;
			if (distance < 0f) distance+= +MathTools.EPSILON;
			if (distance > halfExtensionXYZ[i]) return false;
			if (distance < -halfExtensionXYZ[i]) return false;
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
			Console.println("OrientedBoundingBox::doesCollideWith(): unsupported bounding volume 2: " + bv2);
			return false;
		}
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
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		return new OrientedBoundingBox(
			center.clone(),
			axes[0].clone(),
			axes[1].clone(),
			axes[2].clone(),
			halfExtension.clone()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "OrientedBoundingBox [center=" + center + ", axes="
				+ Arrays.toString(axes) + ", halfExtension=" + halfExtension
				+ "]";
	}

}
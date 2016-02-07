package net.drewke.tdme.math;

import java.util.ArrayList;
import java.util.Arrays;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;



/**
 * Separated axis test
 * 	ported from "game physics - a practical introduction/ben kenwright"
 * @author Andreas Drewke
 * @version $Id$
 */
public final class SeparatingAxisTheorem {

	private float[] minMax1 = new float[2];
	private float[] minMax2 = new float[2];
	private Vector3 axis = new Vector3();
	private Vector3 hitPlane = new Vector3();
	private Vector3 pointOnPlane = new Vector3();
	private Vector3 tmpVector3 = new Vector3();
	private Vector3[] obb1Normals; 
	private Vector3[] obb1Vertices;
	private Vector3[] obb2Vertices;

	/**
	 * Public constructor
	 */
	public SeparatingAxisTheorem() {
		obb1Normals = new Vector3[6];
		for (int i = 0; i < obb1Normals.length; i++) obb1Normals[i] = new Vector3();
		obb1Vertices = new Vector3[6];
		for (int i = 0; i < obb1Vertices.length; i++) obb1Vertices[i] = new Vector3();
		obb2Vertices = new Vector3[24];
		for (int i = 0; i < obb2Vertices.length; i++) obb2Vertices[i] = new Vector3();
	}

	/**
	 * Check axix
	 * @param axis
	 * @return valididy
	 */
	public boolean checkAxis(Vector3 axis) {
		float[] axisXYZ = axis.getArray();

		// return if axis contains NaN component
		if (Float.isNaN(axisXYZ[0]) ||
			Float.isNaN(axisXYZ[1]) ||
			Float.isNaN(axisXYZ[2])) {
			return false;
		}

		// check if axis has no length
		if (Math.abs(axisXYZ[0]) < MathTools.EPSILON &&
			Math.abs(axisXYZ[1]) < MathTools.EPSILON &&
			Math.abs(axisXYZ[2]) < MathTools.EPSILON) {
			return false;
		}

		// valid
		return true;
	}

	/**
	 * Projects the point on given axis and returns its value
	 * @param point
	 * @param axis
	 * @return
	 */
	private float doCalculatePoint(Vector3 point, Vector3 axis) {
		float distance = Vector3.computeDotProduct(point, axis);
		return distance;
	}

	/**
	 * Projects the vertices onto the plane and returns the minimum and maximum values
	 * 	ported from "game physics - a practical introduction/ben kenwright"
	 * @param obb
	 * @param axis
	 * @return float[] containing min and max
	 */
	private void doCalculateInterval(Vector3[] vertices, Vector3 axis, float[] result) {
		float distance = Vector3.computeDotProduct(vertices[0], axis);
		float min = distance;
		float max = distance;
		for (int i = 1; i < vertices.length; i++) {
			distance = Vector3.computeDotProduct(vertices[i], axis);
			if (distance < min) min = distance;
			if (distance > max) max = distance;
		}

		// return min, max
		result[0] = min;
		result[1] = max;
	}

	/**
	 * Check if point is in vertices on given axis
	 * @param vertices
	 * @param point
	 * @param axis
	 * @return point in vertices
	 */
	public boolean checkPointInVerticesOnAxis(Vector3[] vertices, Vector3 point, Vector3 axis) {
		if (checkAxis(axis) == false) return true;
		doCalculateInterval(vertices, axis, minMax1);
		float pOnAxis = doCalculatePoint(point, axis);
		return pOnAxis >= minMax1[0] && pOnAxis <= minMax1[1];
	}

	/**
	 * Determines penetration of given vertices for both objects on a given axis
	 * 	based on an algorithm from "game physics - a practical introduction/ben kenwright"
	 * @param vertices 1
	 * @param vertices 2
	 * @param axis test
	 * @param axis penetration
	 * @return penetration or negative / -1 if none
	 */
	public boolean doSpanIntersect(Vector3[] vertices1, Vector3[] vertices2, Vector3 axisTest, float[] resultArray, int resultOffset) {
		axis.set(axisTest).normalize();

		// min, max for vertices 1 on axis
		doCalculateInterval(vertices1, axis, minMax1);

		// min, max for vertices 2 on axis
		doCalculateInterval(vertices2, axis, minMax2);

		// determine penetration
		float min1 = minMax1[0]; 
		float max1 = minMax1[1];
		float min2 = minMax2[0]; 
		float max2 = minMax2[1];
		float len1 = max1 - min1;
		float len2 = max2 - min2;
		float min = Math.min(min1, min2);
		float max = Math.max(max1, max2);
		float len = max - min;

		// check if we have no penetration
		if (len > len1 + len2) {
			return false;
		}

		if (min2 < min1) {
			axisTest.scale(-1f);
		}

		//
		resultArray[resultOffset] = len1 + len2 - len;
		return true;
	}

	/**
	 * Compute edge face hit planes
	 * 	based on an algorithm from "game physics - a practical introduction/ben kenwright"
	 * @param obb1
	 * @param obb2
	 * @param collision entity
	 */
	public void computeEdgeFaceHitPlanes(OrientedBoundingBox obb1, OrientedBoundingBox obb2, CollisionResponse.Entity collisionEntity) {
		{
			// these normals + vertices will span a plane for each obb1 face
			Vector3 obb1Center = obb1.getCenter();
			Vector3[] obb1Axes = obb1.getAxes();
			float[] obb1HalfExtensionXYZ = obb1.getHalfExtension().getArray();
			obb1Normals[0].set(axis.set(obb1Axes[1]).scale(-obb1HalfExtensionXYZ[1])).normalize();
			obb1Normals[1].set(axis.set(obb1Axes[1]).scale(+obb1HalfExtensionXYZ[1])).normalize();
			obb1Normals[2].set(axis.set(obb1Axes[0]).scale(-obb1HalfExtensionXYZ[0])).normalize();
			obb1Normals[3].set(axis.set(obb1Axes[0]).scale(+obb1HalfExtensionXYZ[0])).normalize();
			obb1Normals[4].set(axis.set(obb1Axes[2]).scale(-obb1HalfExtensionXYZ[2])).normalize();
			obb1Normals[5].set(axis.set(obb1Axes[2]).scale(+obb1HalfExtensionXYZ[2])).normalize();
			obb1Vertices[0].set(obb1Center).add(axis.set(obb1Axes[1]).scale(-obb1HalfExtensionXYZ[1]));
			obb1Vertices[1].set(obb1Center).add(axis.set(obb1Axes[1]).scale(+obb1HalfExtensionXYZ[1]));
			obb1Vertices[2].set(obb1Center).add(axis.set(obb1Axes[0]).scale(-obb1HalfExtensionXYZ[0]));
			obb1Vertices[3].set(obb1Center).add(axis.set(obb1Axes[0]).scale(+obb1HalfExtensionXYZ[0]));
			obb1Vertices[4].set(obb1Center).add(axis.set(obb1Axes[2]).scale(-obb1HalfExtensionXYZ[2]));
			obb1Vertices[5].set(obb1Center).add(axis.set(obb1Axes[2]).scale(+obb1HalfExtensionXYZ[2]));
		}
		{
			Vector3 obb2Center = obb2.getCenter();
			Vector3[] obb2Axes = obb2.getAxes();
			float[] obb2HalfExtensionXYZ = obb2.getHalfExtension().getArray();
			// first block

			// -x, -y, +z
			obb2Vertices[ 0 + 0].set(obb2Center);
			obb2Vertices[ 0 + 0].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 0].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 0].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// +x, -y, +z
			obb2Vertices[ 0 + 1].set(obb2Center);
			obb2Vertices[ 0 + 1].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 1].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 1].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// -x, -y, -z
			obb2Vertices[ 0 + 2].set(obb2Center);
			obb2Vertices[ 0 + 2].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 2].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 2].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// +x, -y, -z
			obb2Vertices[ 0 + 3].set(obb2Center);
			obb2Vertices[ 0 + 3].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 3].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 3].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, +y, -z
			obb2Vertices[ 0 + 4].set(obb2Center);
			obb2Vertices[ 0 + 4].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 4].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 4].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// +x, +y, -z
			obb2Vertices[ 0 + 5].set(obb2Center);
			obb2Vertices[ 0 + 5].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 5].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 5].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, +y, +z
			obb2Vertices[ 0 + 6].set(obb2Center);
			obb2Vertices[ 0 + 6].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 6].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 6].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// +x, +y, +z
			obb2Vertices[ 0 + 7].set(obb2Center);
			obb2Vertices[ 0 + 7].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 0 + 7].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 0 + 7].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));

			// 2nd block

			// +x, +y, -z
			obb2Vertices[ 8 + 0].set(obb2Center);
			obb2Vertices[ 8 + 0].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 0].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 0].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// +x, +y, +z
			obb2Vertices[ 8 + 1].set(obb2Center);
			obb2Vertices[ 8 + 1].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 1].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 1].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// +x, -y, -z
			obb2Vertices[ 8 + 2].set(obb2Center);
			obb2Vertices[ 8 + 2].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 2].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 2].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// +x, -y, +z
			obb2Vertices[ 8 + 3].set(obb2Center);
			obb2Vertices[ 8 + 3].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 3].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 3].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// -x, -y, -z
			obb2Vertices[ 8 + 4].set(obb2Center);
			obb2Vertices[ 8 + 4].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 4].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 4].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, -y, +z
			obb2Vertices[ 8 + 5].set(obb2Center);
			obb2Vertices[ 8 + 5].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 5].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 5].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// -x, +y, -z
			obb2Vertices[ 8 + 6].set(obb2Center);
			obb2Vertices[ 8 + 6].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 6].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 6].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, +y, +z
			obb2Vertices[ 8 + 7].set(obb2Center);
			obb2Vertices[ 8 + 7].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[ 8 + 7].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[ 8 + 7].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));

			// third block

			// +x, -y, -z
			obb2Vertices[16 + 0].set(obb2Center);
			obb2Vertices[16 + 0].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 0].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 0].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// +x, +y, -z
			obb2Vertices[16 + 1].set(obb2Center);
			obb2Vertices[16 + 1].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 1].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 1].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, -y, -z
			obb2Vertices[16 + 2].set(obb2Center);
			obb2Vertices[16 + 2].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 2].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 2].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, +y, -z
			obb2Vertices[16 + 3].set(obb2Center);
			obb2Vertices[16 + 3].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 3].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 3].add(axis.set(obb2Axes[2]).scale(-obb2HalfExtensionXYZ[2]));
			// -x, -y, +z
			obb2Vertices[16 + 4].set(obb2Center);
			obb2Vertices[16 + 4].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 4].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 4].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// -x, +y, +z
			obb2Vertices[16 + 5].set(obb2Center);
			obb2Vertices[16 + 5].add(axis.set(obb2Axes[0]).scale(-obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 5].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 5].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// +x, -y, +z
			obb2Vertices[16 + 6].set(obb2Center);
			obb2Vertices[16 + 6].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 6].add(axis.set(obb2Axes[1]).scale(-obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 6].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
			// +x, +y, +z
			obb2Vertices[16 + 7].set(obb2Center);
			obb2Vertices[16 + 7].add(axis.set(obb2Axes[0]).scale(+obb2HalfExtensionXYZ[0]));
			obb2Vertices[16 + 7].add(axis.set(obb2Axes[1]).scale(+obb2HalfExtensionXYZ[1]));
			obb2Vertices[16 + 7].add(axis.set(obb2Axes[2]).scale(+obb2HalfExtensionXYZ[2]));
		}

		// do the face plane line test
		for (int i = 0; i < obb1Vertices.length; i++) {
			Vector3 planePoint = obb1Vertices[i];
			Vector3 planeNormal = obb1Normals[i];
			float d = Vector3.computeDotProduct(planePoint, planeNormal);
			for (int j = 0; j < obb2Vertices.length; j+= 2) {
				Vector3 point0 = obb2Vertices[j];
				Vector3 point1 = obb2Vertices[j + 1];
				float side0 = Vector3.computeDotProduct(point0, planeNormal) - d;
				float side1 = Vector3.computeDotProduct(point1, planeNormal) - d;
				if ((side0 < 0f && side1 >= 0f) ||
					(side0 >= 0f && side1 < 0f)) {
					float t =
						Vector3.computeDotProduct(planeNormal, axis.set(planePoint).sub(point0)) /
						Vector3.computeDotProduct(planeNormal, axis.set(point1).sub(point0));
					if (t >= 0f && t <= 1.0f) {
						// hitPlane = p0 + t*(p1 - p0)
						hitPlane.set(point1).sub(point0).scale(t).add(point0);
						// check if hit plane is in obb 1
						if (obb1.containsPoint(hitPlane)) {
							collisionEntity.addHitPoint(hitPlane);
						}
					}
				}
			}
		}

	}

	/**
	 * Calculates hit points
	 * 	based on an algorithm from "game physics - a practical introduction/ben kenwright"
	 * @param obb 1
	 * @param obb 2
	 * @param collision entity
	 */
	public void calculateHitPoints(OrientedBoundingBox obb1, OrientedBoundingBox obb2, CollisionResponse.Entity collisionEntity) {
		computeEdgeFaceHitPlanes(obb1, obb2, collisionEntity);
		computeEdgeFaceHitPlanes(obb2, obb1, collisionEntity);
		doCalculateInterval(obb1.getVertices(), collisionEntity.getNormal(), minMax1);
		float penetration = collisionEntity.getPenetration();
		Vector3 hitNormal = collisionEntity.getNormal();
		float min = minMax1[0];
		float max = minMax1[1];
		float distAlongObb1 = (max - min) * 0.5f - penetration * 0.5f;
		// obb1 center + distAlongObb1 * hitNormal
		pointOnPlane.set(hitNormal).scale(distAlongObb1).add(obb1.getCenter());
		for (int i = 0; i < collisionEntity.getHitPointsCount(); i++) {
			// hitpoint[i] = hitpoint[i] + (hitNormal * dot(hitNormal, pointOnPlane - hitPoints[i]))
			collisionEntity.
				getHitPointAt(i).
				add(
					axis.set(hitNormal).
					scale(
						Vector3.computeDotProduct(
							hitNormal,
							tmpVector3.set(pointOnPlane).sub(collisionEntity.getHitPointAt(i))
						)
					)
				);
		}
	}

}

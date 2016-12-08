package net.drewke.tdme.math;

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

}

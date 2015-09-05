package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;

/**
 * Line segment
 * 	This class should be used on a per thread basis
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LineSegment {

	private Vector3 direction = new Vector3();
	private Vector3 d1 = new Vector3();
	private Vector3 d2 = new Vector3();
	private Vector3 r = new Vector3();

	/**
	 * Computes closest points c1, c2 on line segment p1->q1, p2->q2
	 * 	based on an algorithm from "Real-Time Collision Detection" / Ericson"
	 * 	Credit:
	 * 		"From Real-Time Collision Detection by Christer Ericson
	 * 		published by Morgan Kaufman Publishers, (c) 2005 Elsevier Inc"
	 * @param point p1 on line segment 1
	 * @param point q1 on line segment 1
	 * @param point p2 on line segment 2
	 * @param point q2 on line segment 2
	 * @param closest point on line segment 1 c1
	 * @param closest point on line segment 2 c2
	 */
	public void computeClosestPointsOnLineSegments(Vector3 p1, Vector3 q1, Vector3 p2, Vector3 q2, Vector3 c1, Vector3 c2) {
		float s;
		float t;
		d1.set(q1).sub(p1);
		d2.set(q2).sub(p2);
		r.set(p1).sub(p2);
		float a = Vector3.computeDotProduct(d1, d1);
		float e = Vector3.computeDotProduct(d2, d2);
		float f = Vector3.computeDotProduct(d2, r);
		// both line segments degenerate into points?
		if (a <= MathTools.EPSILON && e <= MathTools.EPSILON) {
			s = 0.0f;
			t = 0.0f;
			c1 = p1;
			c2 = p2;
			return;
		}
		// first line segment degenerates into point?
		if (a <= MathTools.EPSILON) {
			s = 0.0f;
			t = f / e;
			t = MathTools.clamp(t, 0.0f, 1.0f);
		} else {
			float c = Vector3.computeDotProduct(d1, r);
			// second line segment degenerates into point?
			if (e <= MathTools.EPSILON) {
				t = 0.0f;
				s = MathTools.clamp(-c / a, 0.0f, 1.0f);
			} else {
				// nope, use general case
				float b = Vector3.computeDotProduct(d1, d2);
				float denom = a * e - b * b;
				if (denom != 0.0f) {
					s = MathTools.clamp((b * f - c * e) / denom, 0.0f, 1.0f);
				} else {
					s = 0.0f;
				}
				t = (b * s + f) / e;
				if (t < 0.0f) {
					t = 0.0f;
					s = MathTools.clamp(-c / a, 0.0f, 1.0f);
				} else
				if (t > 1.0f) {
					t = 1.0f;
					s = MathTools.clamp((b - c) / a, 0.0f, 1.0f);
				}
			}
		}
		c1.set(p1).add(d1.scale(s));
		c2.set(p2).add(d2.scale(t));
	}

	/**
	 * Check if segment collides with bounding box
	 * 	based on an algorithm from "Real-Time Collision Detection" / Ericson
	 * 	Credit:
	 * 		"From Real-Time Collision Detection by Christer Ericson
	 * 		published by Morgan Kaufman Publishers, (c) 2005 Elsevier Inc"
	 * @param bounding box
	 * @param point p on line segment
	 * @param point q on line segment
	 * @param contact point min
	 * @param contact point max
	 * @return true if collides or false if not
	 */
	public boolean doesBoundingBoxCollideWithLineSegment(BoundingBox boundingBox, Vector3 p, Vector3 q, Vector3 contactMin, Vector3 contactMax) {
		float tmin = 0.0f;
		float tmax = 1.0f;
		float minXYZ[] = boundingBox.min.getArray();
		float maxXYZ[] = boundingBox.max.getArray();
		direction.set(q).sub(p); 
		float directionXYZ[] = direction.getArray();
		float pointXYZ[] = p.getArray();
		for (int i = 0; i < 3; i++) {
			if (Math.abs(directionXYZ[i]) < MathTools.EPSILON &&
				(pointXYZ[i] <= minXYZ[i] ||
				pointXYZ[i] >= maxXYZ[i])) {
				return false;
			} else {
				float odd = 1.0f / directionXYZ[i];
				float t1 = (minXYZ[i] - pointXYZ[i]) * odd;
				float t2 = (maxXYZ[i] - pointXYZ[i]) * odd;
				if (t1 > t2) {
					float tmp = t1;
					t1 = t2;
					t2 = tmp;
				}
				tmin = Math.max(tmin,  t1);
				tmax = Math.min(tmax,  t2);
				if (tmin > tmax) return false;
			}
		}

		//
		if (tmin > 1.0) return false;

		// compute contact points
		contactMin.set(p).add(d1.set(direction).scale(tmin));
		contactMax.set(p).add(d2.set(direction).scale(tmax));

		// we have a collision
		return true;
	}

	/**
	 * Check if segment collides with oriented bounding box
	 * 	based on an algorithm from "Real-Time Collision Detection" / Ericson
	 * 	Credit:
	 * 		"From Real-Time Collision Detection by Christer Ericson
	 * 		published by Morgan Kaufman Publishers, (c) 2005 Elsevier Inc"
	 * @param oriented bounding box
	 * @param point p on line segment
	 * @param point q on line segment
	 * @param contact point min
	 * @param contact point max
	 * @return true if collides or false if not
	 */
	public boolean doesOrientedBoundingBoxCollideWithLineSegment(OrientedBoundingBox orientedBoundingBox, Vector3 p, Vector3 q, Vector3 contactMin, Vector3 contactMax) {
		float tmin = 0.0f;
		float tmax = 1.0f;
		Vector3[] obbAxes = orientedBoundingBox.axes;
		Vector3 obbCenter = orientedBoundingBox.center;
		Vector3 obbHalfExtension = orientedBoundingBox.halfExtension;
		float obbHalfExtensionXYZ[] = obbHalfExtension.getArray();
		direction.set(q).sub(p);
		for (int i = 0; i < 3; i++) {
			float directionLengthOnAxis = Vector3.computeDotProduct(direction, obbAxes[i]);
			float obbExtensionLengthOnAxis = obbHalfExtensionXYZ[i];
			float obbCenterLengthOnAxis = Vector3.computeDotProduct(obbCenter, obbAxes[i]);
			float pointLengthOnAxis = Vector3.computeDotProduct(p, obbAxes[i]);
			if (Math.abs(directionLengthOnAxis) < MathTools.EPSILON &&
				(pointLengthOnAxis <= obbCenterLengthOnAxis - obbExtensionLengthOnAxis ||
				pointLengthOnAxis >= obbCenterLengthOnAxis + obbExtensionLengthOnAxis)) {
				return false;
			} else {
				float odd = 1.0f / directionLengthOnAxis;
				float t1 = (obbCenterLengthOnAxis - obbExtensionLengthOnAxis - pointLengthOnAxis) * odd;
				float t2 = (obbCenterLengthOnAxis + obbExtensionLengthOnAxis - pointLengthOnAxis) * odd;
				if (t1 > t2) {
					float tmp = t1;
					t1 = t2;
					t2 = tmp;
				}
				tmin = Math.max(tmin,  t1);
				tmax = Math.min(tmax,  t2);
				if (tmin > tmax) return false;
			}
		}

		//
		if (tmin > 1.0) return false;

		// compute contact points
		contactMin.set(p).add(d1.set(direction).scale(tmin));
		contactMax.set(p).add(d2.set(direction).scale(tmax));

		// we have a collision
		return true;
	}

}

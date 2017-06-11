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

	private Vector3 d = new Vector3();
	private Vector3 d1 = new Vector3();
	private Vector3 d2 = new Vector3();
	private Vector3 r = new Vector3();
	private Vector3 c1 = new Vector3();
	private Vector3 c2 = new Vector3();
	private Vector3 n = new Vector3();
	private Vector3 t = new Vector3();

	/**
	 * Does line segments collide
	 * @param p1 line 1 point 1
	 * @param q1 line 1 point 2
	 * @param p2 line 2 point 1
	 * @param q2 line 2 point 2
	 * @param p intersection point
	 * @return if collides or not
	 */
	public boolean doesLineSegmentsCollide(Vector3 p1, Vector3 q1, Vector3 p2, Vector3 q2, Vector3 p) {
		computeClosestPointsOnLineSegments(p1, q1, p2, q2, c1, c2);
		if (c1.sub(c2).computeLength() < MathTools.EPSILON) {
			p.set(c2);
			return true;
		} else {
			return false;
		}
	}

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
		d.set(q).sub(p); 
		float directionXYZ[] = d.getArray();
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
		contactMin.set(p).add(d1.set(d).scale(tmin));
		contactMax.set(p).add(d2.set(d).scale(tmax));

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
		d.set(q).sub(p);
		for (int i = 0; i < 3; i++) {
			float directionLengthOnAxis = Vector3.computeDotProduct(d, obbAxes[i]);
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
		contactMin.set(p).add(d1.set(d).scale(tmin));
		contactMax.set(p).add(d2.set(d).scale(tmax));

		// we have a collision
		return true;
	}

	/**
	 * Does line segment collides with triangle
	 * @param p1 triangle point 1
	 * @param p2 triangle point 2
	 * @param p3 triangle point 3
	 * @param r1 line segment point 1
	 * @param r2 line segment point 2
	 * @param point of intersection
	 * @return line segment collides with triangle
	 * @see https://gamedev.stackexchange.com/questions/5585/line-triangle-intersection-last-bits
	 */
	public boolean doesLineSegmentCollideWithTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Vector3 r1, Vector3 r2, Vector3 contact) {
		// find triangle normal
		Vector3.computeCrossProduct(d1.set(p2).sub(p1), d2.set(p3).sub(p1), n).normalize();

		// find distance from LP1 and LP2 to the plane defined by the triangle
		float dist1 = Vector3.computeDotProduct(d1.set(r1).sub(p1), n);
		float dist2 = Vector3.computeDotProduct(d2.set(r2).sub(p1), n);

		// check if line doesn't cross the triangle.
		if (dist1 * dist2 >= 0.0f) {  
			return false; 
		}

		// line and plane are parallel
		if (Math.abs(dist1 - dist2) < MathTools.EPSILON) {  
			return false; 
		}

		// Find point on the line that intersects with the plane
	    contact.set(r2).sub(r1).scale(-dist1 / (dist2-dist1));
	    contact.add(r1);

	    // check intersection p2-p1
	    Vector3.computeCrossProduct(n, d1.set(p2).sub(p1), t);
	    if (Vector3.computeDotProduct(t, d2.set(contact).sub(p1)) < 0f) {
	    	return false;
	    }

	    // check intersection p3-p2
	    Vector3.computeCrossProduct(n, d1.set(p3).sub(p2), t);
	    if (Vector3.computeDotProduct(t, d2.set(contact).sub(p2)) < 0f) {
	    	return false;
	    }

	    // check intersection p1-p3
	    Vector3.computeCrossProduct(n, d1.set(p1).sub(p3), t);
	    if (Vector3.computeDotProduct(t, d2.set(contact).sub(p1)) < 0f) {
	    	return false;
	    }

		// intersection
		return true;
	}

}

package net.drewke.tdme.math;


/**
 * Triangle Triangle Intersection see:
 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
 * 
 * @author Andreas Drewke
 * @version $Id$
 *
 */
public final class TriangleTriangleIntersection {

	private final static float EPSILON = 0.01f;

	public enum ReturnValue {NOINTERSECTION, COPLANAR_INTERSECTION, INTERSECTION};

	private Vector3 E1 = new Vector3();
	private Vector3 E2 = new Vector3();
	private Vector3 N1 = new Vector3();
	private Vector3 N2 = new Vector3();
	private Vector3 D = new Vector3();
	private Vector2 isect1 = new Vector2();
	private Vector2 isect2 = new Vector2();
	private Vector3 isectpointA1 = new Vector3();
	private Vector3 isectpointA2 = new Vector3();
	private Vector3 isectpointB1 = new Vector3();
	private Vector3 isectpointB2 = new Vector3();
	private Vector3 diff = new Vector3();
	private float A[] = new float[] { 0f, 0f, 0f };

	/**
	 * Triangle Triangle Intersection see:
	 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
	 * @param V0
	 * @param U0
	 * @param U1
	 * @param i0
	 * @param i1
	 * @param Ax
	 * @param Ay
	 * @return
	 */
	private static boolean EDGE_EDGE_TEST(float[] V0, float[] U0, float[] U1,
			int i0, int i1, float Ax, float Ay) {
		float Bx, By, Cx, Cy, e, d, f;
		Bx = U0[i0] - U1[i0];
		By = U0[i1] - U1[i1];
		Cx = V0[i0] - U0[i0];
		Cy = V0[i1] - U0[i1];
		f = Ay * Bx - Ax * By;
		d = By * Cx - Bx * Cy;
		if ((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f)) {
			e = Ax * Cy - Ay * Cx;
			if (f > 0) {
				if (e >= 0 && e <= f)
					return true;
			} else {
				if (e <= 0 && e >= f)
					return true;
			}
		}
		return false;
	}

	/**
	 * Triangle Triangle Intersection see:
	 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
	 * @param V0
	 * @param V1
	 * @param U0
	 * @param U1
	 * @param U2
	 * @param i0
	 * @param i1
	 * @return
	 */
	private static boolean EDGE_AGAINST_TRI_EDGES(float[] V0, float[] V1,
			float[] U0, float[] U1, float[] U2, int i0, int i1) {
		float Ax, Ay;
		Ax = V1[i0] - V0[i0];
		Ay = V1[i1] - V0[i1];
		/* test edge U0,U1 against V0,V1 */
		if (EDGE_EDGE_TEST(V0, U0, U1, i0, i1, Ax, Ay) == true)
			return true;
		/* test edge U1,U2 against V0,V1 */
		if (EDGE_EDGE_TEST(V0, U1, U2, i0, i1, Ax, Ay) == true)
			return true;
		/* test edge U2,U1 against V0,V1 */
		if (EDGE_EDGE_TEST(V0, U2, U0, i0, i1, Ax, Ay) == true)
			return true;
		//
		return false;
	}

	/**
	 * Triangle Triangle Intersection see:
	 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
	 * @param V0
	 * @param U0
	 * @param U1
	 * @param U2
	 * @param i0
	 * @param i1
	 * @return
	 */
	private static boolean POINT_IN_TRI(float[] V0, float[] U0, float[] U1,
			float[] U2, int i0, int i1) {
		//
		float a, b, c, d0, d1, d2;
		/* is T1 completly inside T2? */
		/* check if V0 is inside tri(U0,U1,U2) */
		a = U1[i1] - U0[i1];
		b = -(U1[i0] - U0[i0]);
		c = -a * U0[i0] - b * U0[i1];
		d0 = a * V0[i0] + b * V0[i1] + c;

		a = U2[i1] - U1[i1];
		b = -(U2[i0] - U1[i0]);
		c = -a * U1[i0] - b * U1[i1];
		d1 = a * V0[i0] + b * V0[i1] + c;

		a = U0[i1] - U2[i1];
		b = -(U0[i0] - U2[i0]);
		c = -a * U2[i0] - b * U2[i1];
		d2 = a * V0[i0] + b * V0[i1] + c;
		if (d0 * d1 > 0.0) {
			if (d0 * d2 > 0.0)
				return true;
		}
		return false;
	}

	/**
	 * Triangle Triangle Intersection see:
	 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
	 * @param N
	 * @param V0
	 * @param V1
	 * @param V2
	 * @param U0
	 * @param U1
	 * @param U2
	 * @return
	 */
	private boolean coplanar_tri_tri(float[] N, float[] V0, float[] V1,
			float[] V2, float[] U0, float[] U1, float[] U2) {
		int i0, i1;
		/* first project onto an axis-aligned plane, that maximizes the area */
		/* of the triangles, compute indices: i0,i1. */
		A[0] = Math.abs(N[0]);
		A[1] = Math.abs(N[1]);
		A[2] = Math.abs(N[2]);
		if (A[0] > A[1]) {
			if (A[0] > A[2]) {
				i0 = 1; /* A[0] is greatest */
				i1 = 2;
			} else {
				i0 = 0; /* A[2] is greatest */
				i1 = 1;
			}
		} else /* A[0]<=A[1] */
		{
			if (A[2] > A[1]) {
				i0 = 0; /* A[2] is greatest */
				i1 = 1;
			} else {
				i0 = 0; /* A[1] is greatest */
				i1 = 2;
			}
		}

		/* test all edges of triangle 1 against the edges of triangle 2 */
		if (EDGE_AGAINST_TRI_EDGES(V0, V1, U0, U1, U2, i0, i1) == true)
			return true;
		if (EDGE_AGAINST_TRI_EDGES(V1, V2, U0, U1, U2, i0, i1) == true)
			return true;
		if (EDGE_AGAINST_TRI_EDGES(V2, V0, U0, U1, U2, i0, i1) == true)
			return true;

		/* finally, test if tri1 is totally contained in tri2 or vice versa */
		if (POINT_IN_TRI(V0, U0, U1, U2, i0, i1) == true)
			return true;
		if (POINT_IN_TRI(U0, V0, V1, V2, i0, i1) == true)
			return true;

		return false;
	}

	/**
	 * Triangle Triangle Intersection see:
	 * 	http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/
	 * @param VTX0
	 * @param VTX1
	 * @param VTX2
	 * @param VV0
	 * @param VV1
	 * @param VV2
	 * @param D0
	 * @param D1
	 * @param D2
	 * @param isect0
	 * @param isect0Idx
	 * @param isect1
	 * @param isect1Idx
	 * @param isectpoint0
	 * @param isectpoint1
	 */
	private void isect2(Vector3 VTX0, Vector3 VTX1, Vector3 VTX2,
			float VV0, float VV1, float VV2, float D0, float D1, float D2,
			Vector2 isect0, int isect0Idx, Vector2 isect1, int isect1Idx,
			Vector3 isectpoint0, Vector3 isectpoint1) {
		float tmp = D0 / (D0 - D1);
		isect0.getArray()[isect0Idx] = VV0 + (VV1 - VV0) * tmp;
		diff.set(VTX1).sub(VTX0);
		diff.scale(tmp);
		isectpoint0.set(diff).add(VTX0);
		tmp = D0 / (D0 - D2);
		isect1.getArray()[isect1Idx] = VV0 + (VV2 - VV0) * tmp;
		diff.set(VTX2).sub(VTX0);
		diff.scale(tmp);
		isectpoint1.set(VTX0).add(diff);
	}

	/**
	 * 
	 * @param VERT0
	 * @param VERT1
	 * @param VERT2
	 * @param VV0
	 * @param VV1
	 * @param VV2
	 * @param D0
	 * @param D1
	 * @param D2
	 * @param D0D1
	 * @param D0D2
	 * @param isect0
	 * @param isect0Idx
	 * @param isect1
	 * @param isect1Idx
	 * @param isectpoint0
	 * @param isectpoint1
	 * @return
	 */
	private boolean compute_intervals_isectline(Vector3 VERT0,
			Vector3 VERT1, Vector3 VERT2, float VV0, float VV1, float VV2,
			float D0, float D1, float D2, float D0D1, float D0D2,
			Vector2 isect0, int isect0Idx, Vector2 isect1, int isect1Idx,
			Vector3 isectpoint0, Vector3 isectpoint1) {
		if (D0D1 > 0.0f) {
			/* here we know that D0D2<=0.0 */
			/*
			 * that is D0, D1 are on the same side, D2 on the other or on the
			 * plane
			 */
			isect2(VERT2, VERT0, VERT1, VV2, VV0, VV1, D2, D0, D1, isect0,
					isect0Idx, isect1, isect1Idx, isectpoint0, isectpoint1);
		} else if (D0D2 > 0.0f) {
			/* here we know that d0d1<=0.0 */
			isect2(VERT1, VERT0, VERT2, VV1, VV0, VV2, D1, D0, D2, isect0,
					isect0Idx, isect1, isect1Idx, isectpoint0, isectpoint1);
		} else if (D1 * D2 > 0.0f || D0 != 0.0f) {
			/* here we know that d0d1<=0.0 or that D0!=0.0 */
			isect2(VERT0, VERT1, VERT2, VV0, VV1, VV2, D0, D1, D2, isect0,
					isect0Idx, isect1, isect1Idx, isectpoint0, isectpoint1);
		} else if (D1 != 0.0f) {
			isect2(VERT1, VERT0, VERT2, VV1, VV0, VV2, D1, D0, D2, isect0,
					isect0Idx, isect1, isect1Idx, isectpoint0, isectpoint1);
		} else if (D2 != 0.0f) {
			isect2(VERT2, VERT0, VERT1, VV2, VV0, VV1, D2, D0, D1, isect0,
					isect0Idx, isect1, isect1Idx, isectpoint0, isectpoint1);
		} else {
			/* triangles are coplanar */
			return true;
		}
		return false;
	}

	/**
	 * Sort values and return smallest value index
	 * @param values
	 * @return smallest value index
	 */
	private static int SORT2(float[] values) {
		if (values[0] > values[1]) {
			float tmp;
			tmp = values[0];
			values[0] = values[1];
			values[1] = tmp;
			return 1;
		} else
			return 0;
	}

	/**
	 * Compute triangle v0,v1,v2 vs. triangle u0,u1,u2 intersection test 
	 * @param V0
	 * @param V1
	 * @param V2
	 * @param U0
	 * @param U1
	 * @param U2
	 * @param isectpt1 intersection point 1 if not coplanar
	 * @param isectpt2 intersection point 2 if not coplanar
	 * @return
	 */
	public ReturnValue computeTriangleTriangleIntersection(Vector3 V0,
			Vector3 V1, Vector3 V2, Vector3 U0, Vector3 U1, Vector3 U2,
			Vector3 isectpt1, Vector3 isectpt2) {
		//
		float d1, d2;
		float du0, du1, du2, dv0, dv1, dv2;
		float du0du1, du0du2, dv0dv1, dv0dv2;
		int index;
		float vp0, vp1, vp2;
		float up0, up1, up2;
		float b, c, max;
		float tmp;
		int smallest1, smallest2;

		/* compute plane equation of triangle(V0,V1,V2) */
		E1.set(V1).sub(V0);
		E2.set(V2).sub(V0);
		Vector3.computeCrossProduct(E1, E2, N1);
		d1 = -Vector3.computeDotProduct(N1, V0);
		/* plane equation 1: N1.X+d1=0 */

		/*
		 * put U0,U1,U2 into plane equation 1 to compute signed distances to the
		 * plane
		 */
		du0 = Vector3.computeDotProduct(N1, U0) + d1;
		du1 = Vector3.computeDotProduct(N1, U1) + d1;
		du2 = Vector3.computeDotProduct(N1, U2) + d1;

		/* coplanarity robustness check */
		if (Math.abs(du0) < EPSILON)
			du0 = 0.0f;
		if (Math.abs(du1) < EPSILON)
			du1 = 0.0f;
		if (Math.abs(du2) < EPSILON)
			du2 = 0.0f;

		du0du1 = du0 * du1;
		du0du2 = du0 * du2;

		if (du0du1 > 0.0f && du0du2 > 0.0f) /*
											 * same sign on all of them + not
											 * equal 0 ?
											 */
			return ReturnValue.NOINTERSECTION; /* no intersection occurs */

		/* compute plane of triangle (U0,U1,U2) */
		E1.set(U1).sub(U0);
		E2.set(U2).sub(U0);
		Vector3.computeCrossProduct(E1, E2, N2);
		d2 = -Vector3.computeDotProduct(N2, U0);
		/* plane equation 2: N2.X+d2=0 */

		/* put V0,V1,V2 into plane equation 2 */
		dv0 = Vector3.computeDotProduct(N2, V0) + d2;
		dv1 = Vector3.computeDotProduct(N2, V1) + d2;
		dv2 = Vector3.computeDotProduct(N2, V2) + d2;

		/* coplanarity robustness check */
		if (Math.abs(dv0) < EPSILON)
			dv0 = 0.0f;
		if (Math.abs(dv1) < EPSILON)
			dv1 = 0.0f;
		if (Math.abs(dv2) < EPSILON)
			dv2 = 0.0f;

		dv0dv1 = dv0 * dv1;
		dv0dv2 = dv0 * dv2;

		if (dv0dv1 > 0.0f && dv0dv2 > 0.0f) /*
											 * same sign on all of them + not
											 * equal 0 ?
											 */
			return ReturnValue.NOINTERSECTION; /* no intersection occurs */

		/* compute direction of intersection line */
		Vector3.computeCrossProduct(N1, N2, D);

		/* compute and index to the largest component of D */
		max = Math.abs(D.getArray()[0]);
		index = 0;
		b = Math.abs(D.getArray()[1]);
		c = Math.abs(D.getArray()[2]);
		if (b > max) {
			max = b;
			index = 1;
		}
		if (c > max) {
			max = c;
			index = 2;
		}

		/* this is the simplified projection onto L */
		vp0 = V0.getArray()[index];
		vp1 = V1.getArray()[index];
		vp2 = V2.getArray()[index];

		up0 = U0.getArray()[index];
		up1 = U1.getArray()[index];
		up2 = U2.getArray()[index];

		/* compute interval for triangle 1 */
		boolean coplanar = compute_intervals_isectline(V0, V1, V2, vp0, vp1,
				vp2, dv0, dv1, dv2, dv0dv1, dv0dv2, isect1, 0, isect1, 1,
				isectpointA1, isectpointA2);
		if (coplanar == true) {
			if (coplanar_tri_tri(N1.getArray(), V0.getArray(),
					V1.getArray(), V2.getArray(), U0.getArray(), U1.getArray(),
					U2.getArray()) == true) {
				return ReturnValue.COPLANAR_INTERSECTION;
			} else {
				return ReturnValue.NOINTERSECTION;
			}
		}

		/* compute interval for triangle 2 */
		compute_intervals_isectline(U0, U1, U2, up0, up1, up2, du0, du1, du2,
				du0du1, du0du2, isect2, 0, isect2, 1, isectpointB1,
				isectpointB2);

		smallest1 = SORT2(isect1.getArray());
		smallest2 = SORT2(isect2.getArray());

		if (isect1.getArray()[1] < isect2.getArray()[0]
				|| isect2.getArray()[1] < isect1.getArray()[0])
			return ReturnValue.NOINTERSECTION;

		if (isect2.getArray()[0] < isect1.getArray()[0]) {
			if (smallest1 == 0) {
				isectpt1.set(isectpointA1);
			} else {
				isectpt1.set(isectpointA2);
			}

			if (isect2.getArray()[1] < isect1.getArray()[1]) {
				if (smallest2 == 0) {
					isectpt2.set(isectpointB2);
				} else {
					isectpt2.set(isectpointB1);
				}
			} else {
				if (smallest1 == 0) {
					isectpt2.set(isectpointA2);
				} else {
					isectpt2.set(isectpointA1);
				}
			}
		} else {
			if (smallest2 == 0) {
				isectpt1.set(isectpointB1);
			} else {
				isectpt1.set(isectpointB2);
			}

			if (isect2.getArray()[1] > isect1.getArray()[1]) {
				if (smallest1 == 0) {
					isectpt2.set(isectpointA2);
				} else {
					isectpt2.set(isectpointA1);
				}
			} else {
				if (smallest2 == 0) {
					isectpt2.set(isectpointB2);
				} else {
					isectpt2.set(isectpointB1);
				}
			}
		}
		/* at this point, we know that the triangles intersect */
		return ReturnValue.INTERSECTION;
	}

}

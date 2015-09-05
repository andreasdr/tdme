package net.drewke.tdme.engine;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.Plane;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Frustum class
 * 	based on http://www.crownandcutlass.com/features/technicaldetails/frustum.html
 * @author Mark Morley, Andreas Drewke
 * @version $Id$
 */
public final class Frustum {

	private GLRenderer renderer;

	// right, left, bottom, top, far, near
	protected static final int PLANE_RIGHT = 0; 
	protected static final int PLANE_LEFT = 1;
	protected static final int PLANE_BOTTOM = 2;
	protected static final int PLANE_TOP = 3;
	protected static final int PLANE_FAR = 4;
	protected static final int PLANE_NEAR = 5;

	Matrix4x4 projectionMatrixTransposed = new Matrix4x4();
	Matrix4x4 modelViewMatrixTransposed = new Matrix4x4();
	Matrix4x4 frustumMatrix = new Matrix4x4();

	private Plane[] planes;

	/**
	 * Public default constructor
	 * @param renderer
	 */
	public Frustum(GLRenderer renderer) {
		this.renderer = renderer;
		planes = new Plane[6];
		for (int i = 0; i < 6; i++) {
			planes[i] = new Plane();
		}
	}

	/**
	 * @return planes
	 */
	public Plane[] getPlanes() {
		return planes;
	}

	/**
	 * Setups frustum, should be called if frustum did change 
	 * @param gl
	 */
	public void updateFrustum() {
		projectionMatrixTransposed.set(renderer.getProjectionMatrix()).transpose();
		modelViewMatrixTransposed.set(renderer.getModelViewMatrix()).transpose();
		frustumMatrix.set(projectionMatrixTransposed).multiply(modelViewMatrixTransposed);

		float[] data = frustumMatrix.getArray();

		float x,y,z,d,t;

		// right plane
		x = data[12] - data[0];
		y = data[13] - data[1];
		z = data[14] - data[2];
		d = data[15] - data[3];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[0].getNormal().set(x, y, z);
		planes[0].setDistance(d);

		// left plane
		x = data[12] + data[0];
		y = data[13] + data[1];
		z = data[14] + data[2];
		d = data[15] + data[3];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[1].getNormal().set(x, y, z);
		planes[1].setDistance(d);

		// bottom plane
		x = data[12] + data[4];
		y = data[13] + data[5];
		z = data[14] + data[6];
		d = data[15] + data[7];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[2].getNormal().set(x, y, z);
		planes[2].setDistance(d);

		// top plane
		x = data[12] - data[4];
		y = data[13] - data[5];
		z = data[14] - data[6];
		d = data[15] - data[7];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[3].getNormal().set(x, y, z);
		planes[3].setDistance(d);

		// far plane
		x = data[12] - data[8];
		y = data[13] - data[9];
		z = data[14] - data[10];
		d = data[15] - data[11];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[4].getNormal().set(x, y, z);
		planes[4].setDistance(d);

		// near plane
		x = data[12] + data[8];
		y = data[13] + data[9];
		z = data[14] + data[10];
		d = data[15] + data[11];

		// 	normalize
		t = (float)Math.sqrt((x * x) + (y * y) + (z * z));
		x /= t; y /= t; z /= t; d /= t;

		//	setup
		planes[5].getNormal().set(x, y, z);
		planes[5].setDistance(d);
	}

	/**
	 * Checks if given vector is in frustum
	 * @param v
	 * @return visibility
	 */
	public boolean isVisible(Vector3 v) {
		float[] vector = v.getArray();
		for (Plane p: planes) {
			// plane normal
			float[] normal = p.getNormal().getArray();
			if ((normal[0] * vector[0]) +
				(normal[1] * vector[1]) +
				(normal[2] * vector[2]) +
				p.getDistance() <= 0) {
				//
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if sphere is in frustum
	 * @param s
	 * @return visibility
	 */
	public boolean isVisible(Sphere s) {
		// sphere center
		float[] center = s.getCenter().getArray();
		float radius = s.getRadius();
		for (Plane p: planes) {
			// plane normal
			float[] normal = p.getNormal().getArray();
			if ((normal[0] * center[0]) +
				(normal[1] * center[1]) +
				(normal[2] * center[2]) +
				p.getDistance() <= -radius) {
				//
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if bounding box is in frustum
	 * @param s
	 * @return visibility
	 */
	public boolean isVisible(BoundingBox b) {
		float min[] = b.getMin().getArray();
		float max[] = b.getMax().getArray();
		float minX = min[0];
		float minY = min[1];
		float minZ = min[2];
		float maxX = max[0];
		float maxY = max[1];
		float maxZ = max[2];		
		for (Plane p: planes) {
			float[] normal = p.getNormal().getArray();
			float distance = p.getDistance();
			if ((normal[0] * minX) +
				(normal[1] * minY) +
				(normal[2] * minZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * maxX) +
				(normal[1] * minY) +
				(normal[2] * minZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * minX) +
				(normal[1] * maxY) +
				(normal[2] * minZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * maxX) +
				(normal[1] * maxY) +
				(normal[2] * minZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * minX) +
				(normal[1] * minY) +
				(normal[2] * maxZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * maxX) +
				(normal[1] * minY) +
				(normal[2] * maxZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * minX) +
				(normal[1] * maxY) +
				(normal[2] * maxZ) +
				distance > 0) {
				//
				continue;
			}
			if ((normal[0] * maxX) +
				(normal[1] * maxY) +
				(normal[2] * maxZ) +
				distance > 0) {
				//
				continue;
			}
			//
			return false;
		}
		return true;
	}

}
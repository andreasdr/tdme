package net.drewke.tdme.engine;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Camera
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Camera {

	private final static Vector3 defaultUp = new Vector3(0.0f, 1.0f, 0.0f);
	public final static float FOVY = 45f;

	private GLRenderer renderer;

	private int width;
	private int height;
	private float aspect;

	private float zNear;
	private float zFar;

	private Vector3 upVector;
	private Vector3 lookFrom;
	private Vector3 lookAt;

	private Matrix4x4 projectionMatrix;
	private Matrix4x4 modelViewMatrix;
	private Matrix4x4 tmpAxesMatrix;

	private Vector3 tmpLookFromInverted;
	private Vector3 tmpForward;
	private Vector3 tmpSide;
	private Vector3 tmpUp;

	private Frustum frustum;

	/**
	 * Public default constructor
	 * @param renderer
	 */
	public Camera(GLRenderer renderer) {
		this.renderer = renderer;
		width = 0;
		height = 0;
		aspect = 1;
		zNear = 10f;
		zFar = 4000f;
		upVector = new Vector3(0f,1f,0f);
		lookFrom = new Vector3(0f,50f,400f);
		lookAt = new Vector3(0f,50f,0f);
		projectionMatrix = new Matrix4x4();
		modelViewMatrix = new Matrix4x4();
		tmpAxesMatrix = new Matrix4x4();
		tmpLookFromInverted = new Vector3();
		tmpForward = new Vector3();
		tmpSide = new Vector3();
		tmpUp = new Vector3();
		frustum = new Frustum(renderer);
	}

	/**
	 * @return float
	 */
	public float getZNear() {
		return zNear;
	}

	/**
	 * @param zNear
	 */
	public void setZNear(float zNear) {
		this.zNear = zNear;
	}

	/**
	 * @return float
	 */
	public float getZFar() {
		return zFar;
	}

	/**
	 * @param zFar
	 */
	public void setZFar(float zFar) {
		this.zFar = zFar;
	}

	/**
	 * @return up vector
	 */
	public Vector3 getUpVector() {
		return upVector;
	}

	/**
	 * @return look from vector
	 */
	public Vector3 getLookFrom() {
		return lookFrom;
	}

	/**
	 * @return look at vector
	 */
	public Vector3 getLookAt() {
		return lookAt;
	}

	/**
	 * @return frustum
	 */
	public Frustum getFrustum() {
		return frustum;
	}

	/**
	 * Computes the up vector for given look from and look at vectors
	 * @param look from
	 * @param look at
	 * @param up vector
	 */
	public void computeUpVector(Vector3 lookFrom, Vector3 lookAt, Vector3 upVector) {
		tmpForward.set(lookAt).sub(lookFrom).normalize();
		if (Math.abs(tmpForward.getX()) < MathTools.EPSILON && Math.abs(tmpForward.getZ()) < MathTools.EPSILON) {
			upVector.set(0f, 0f, tmpForward.getY()).normalize();
			return;
		}
		Vector3.computeCrossProduct(tmpForward, defaultUp, tmpSide).normalize();
		Vector3.computeCrossProduct(tmpSide, tmpForward, upVector).normalize();
	}

 	/**
	 * Computes the projection matrix
	 * 	based on: http://www.songho.ca/opengl/gl_transform.html
	 * @param y field of view
	 * @param aspect
	 * @param z near
	 * @param z far
	 * @return projection matrix
	 */
	private Matrix4x4 computeProjectionMatrix(float yfieldOfView, float aspect, float zNear, float zFar) {
		float tangent = (float)Math.tan(yfieldOfView / 2.0f * 3.14159265f / 180.0f);
		float height = zNear * tangent;
		float width = height * aspect;

		//
		return computeFrustumMatrix(-width, width, -height, height, zNear, zFar);
	}

	/**
	 * Computes frustum matrix
	 * 	based on: http://www.songho.ca/opengl/gl_transform.html
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @param near
	 * @param far
	 * @return frustum matrix
	 */
	private Matrix4x4 computeFrustumMatrix(float left, float right, float bottom, float top, float near, float far) {
		return projectionMatrix.set(
			2.0f * near / (right - left),
			0.0f,
			0.0f,
			0.0f,
			0.0f,
			2.0f * near / (top - bottom),
			0.0f,
			0.0f,
			(right + left) / (right - left),
			(top + bottom) / (top - bottom),
			-(far + near) / (far - near),
			-1.0f,
			0.0f,
			0.0f,
			-(2.0f * far * near) / (far - near),
			1.0f
		);
	}

	/**
	 * Computes projection matrix for given look from, look at and up vector
	 * @param look from
	 * @param look at
	 * @param up vector
	 * @return model view matrix
	 */
	private Matrix4x4 computeModelViewMatrix(Vector3 lookFrom, Vector3 lookAt, Vector3 upVector) {
		tmpForward.set(lookAt).sub(lookFrom).normalize();
		Vector3.computeCrossProduct(tmpForward, upVector, tmpSide).normalize();
		Vector3.computeCrossProduct(tmpSide, tmpForward, tmpUp);
		float[] sideXYZ = tmpSide.getArray();
		float[] forwardXYZ = tmpForward.getArray();
		float[] upXYZ = tmpUp.getArray();

		//
		modelViewMatrix.
			identity().
			translate(tmpLookFromInverted.set(lookFrom).scale(-1f)).multiply(
				tmpAxesMatrix.set(
					sideXYZ[0], upXYZ[0], -forwardXYZ[0], 0.0f,
					sideXYZ[1], upXYZ[1], -forwardXYZ[1], 0.0f,
					sideXYZ[2], upXYZ[2], -forwardXYZ[2], 0.0f,
					0f, 0f, 0f, 1.0f
				)
			);
		return modelViewMatrix;
	}

	/**
	 * Sets up camera while resizing the view port
	 * @param gl
	 * @param width
	 * @param height
	 */
	public void update(int width, int height) {
		// setup new view port if required
		if (this.width != width || this.height != height) {
			if (height <= 0) height = 1;
			aspect = (float) width / (float) height;
			this.width = width;
			this.height = height;
			renderer.getViewportMatrix().set(
				width / 2f,
				0f,
				0f,
				0f,
				0f,
				height / 2f,
				0f,
				0f,
				0f,
				0f,
				1f,
				0f,
				0 + (width / 2f),
				0 + (height / 2f),
				0f,
				1f
			);
		} else {
			aspect = (float) width / (float) height;
		}

		// setup projection and model view
		renderer.getProjectionMatrix().set(computeProjectionMatrix(FOVY, aspect, zNear, zFar));
		renderer.onUpdateProjectionMatrix();
		renderer.getModelViewMatrix().set(computeModelViewMatrix(lookFrom, lookAt, upVector));
		renderer.onUpdateModelViewMatrix();
		renderer.getCameraMatrix().set(renderer.getModelViewMatrix());
		renderer.onUpdateCameraMatrix();

		// update frustum
		frustum.updateFrustum();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Camera [width=" + width + ", height=" + height + ", aspect="
				+ aspect + ", zNear=" + zNear + ", zFar=" + zFar
				+ ", upVector=" + upVector + ", lookFrom=" + lookFrom
				+ ", lookAt=" + lookAt + "]";
	}	

}
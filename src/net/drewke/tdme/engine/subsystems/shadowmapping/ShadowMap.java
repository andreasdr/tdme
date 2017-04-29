package net.drewke.tdme.engine.subsystems.shadowmapping;

import java.util.ArrayList;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.FrameBuffer;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.ObjectParticleSystemEntity;
import net.drewke.tdme.engine.subsystems.object.Object3DVBORenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Shadow map class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ShadowMap {

	protected final static int TEXTUREUNIT = 4; 

	private ArrayList<Object3D> visibleObjects;
	private ShadowMapping shadowMapping;

	private final static int SHADOWMAP_WIDTH = 2048;
	private final static int SHADOWMAP_HEIGHT = 2048;

	private Camera lightCamera;
	private Vector3 lightDirection;
	private Vector3 lightLookAt;
	private Vector3 lightLookFrom;
	private FrameBuffer frameBuffer;
	private Matrix4x4 biasMatrix;

	private Matrix4x4 depthBiasMVPMatrix;

	/**
	 * Public constructor
	 * @param width
	 * @param height
	 */
	protected ShadowMap(ShadowMapping shadowMapping, int width, int height) {
		visibleObjects = new ArrayList<Object3D>();
		this.shadowMapping = shadowMapping;
		lightCamera = new Camera(shadowMapping.renderer);
		frameBuffer = new FrameBuffer(
			shadowMapping.engine,
			SHADOWMAP_WIDTH,
			SHADOWMAP_HEIGHT,
			FrameBuffer.FRAMEBUFFER_DEPTHBUFFER
		);
		biasMatrix = new Matrix4x4(	
			0.5f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f
		);
		depthBiasMVPMatrix = new Matrix4x4().identity();
		lightDirection = new Vector3();
		lightLookAt = new Vector3();
		lightLookFrom = new Vector3();
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return frameBuffer.getWidth();
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return frameBuffer.getHeight();
	}

	/**
	 * Init frame buffer
	 * @param gl
	 */
	protected void init() {
		frameBuffer.init();
	}

	/**
	 * Reshape frame buffer
	 * @param gl
	 * @param width
	 * @param height
	 */
	protected void reshape(int width, int height) {
		// no op, we have a fixed shadow map size
	}

	/**
	 * Disposes this shadow mapping
	 * @param gl
	 */
	protected void dispose() {
		frameBuffer.dispose();
	}

	/**
	 * Binds frame buffer depth texture
	 * @param gl
	 */
	protected void bindDepthBufferTexture() {
		frameBuffer.bindDepthBufferTexture();
	}

	/**
	 * @return lightCamera
	 */
	protected Camera getCamera() {
		return lightCamera;
	}

	/**
	 * Renders given objects to shadow map
	 * @param gl
	 * @param objects
	 */
	protected void render(Light light, ArrayList<Object3D> objects) {
		// clear visible objects
		visibleObjects.clear();

		// TODO: Spotlights are different!

		// viewers camera
		Camera camera = shadowMapping.engine.getCamera();
		float lightEyeDistance = lightDirection.set(camera.getLookAt()).sub(camera.getLookFrom()).computeLength() * shadowMapping.lightEyeDistanceScale;

		// compute camera from view of light 
		lightDirection.set(light.getSpotDirection()).normalize();
		lightLookAt.set(camera.getLookAt());
		lightLookFrom.set(lightLookAt).sub(lightDirection.scale(lightEyeDistance)); 

		// determine light camera z far
		float lightCameraZFar = lightEyeDistance * 2.0f;
		if (camera.getZFar() > lightCameraZFar) lightCameraZFar = camera.getZFar(); 

		// set up light camera from view of light
		lightCamera.setZNear(camera.getZNear());
		lightCamera.setZFar(lightCameraZFar);
		lightCamera.getLookFrom().set(lightLookFrom);
		lightCamera.getLookAt().set(lightLookAt);
		lightCamera.computeUpVector(lightCamera.getLookFrom(), lightCamera.getLookAt(), lightCamera.getUpVector());
		lightCamera.update(frameBuffer.getWidth(), frameBuffer.getHeight());

		// Bind frame buffer to shadow map fbo id
		frameBuffer.enableFrameBuffer();

		// clear depth buffer
		shadowMapping.renderer.clear(shadowMapping.renderer.CLEAR_DEPTH_BUFFER_BIT);

		// determine visible objects and objects that should generate a shadow
		for (Entity entity: shadowMapping.engine.getPartition().getVisibleEntities(lightCamera.getFrustum())) {
			if (entity instanceof Object3D) {
				Object3D object = (Object3D)entity;
				if (object.isDynamicShadowingEnabled() == false) continue;
				visibleObjects.add(object);
			} else
			if (entity instanceof ObjectParticleSystemEntity) {
				ObjectParticleSystemEntity opse = (ObjectParticleSystemEntity)entity;
				if (opse.isDynamicShadowingEnabled() == false) continue;
				visibleObjects.addAll(opse.getEnabledObjects());
			}
		}

		// generate shadow map texture matrix
		computeDepthBiasMVPMatrix();

		// only draw opaque face entities as shadows will not be produced from transparent objects
		shadowMapping.object3DVBORenderer.render(visibleObjects, true, Object3DVBORenderer.DepthBufferMode.FORCE, null);
	}

	/**
	 * Computes shadow texture matrix and stores it
	 * @param gl
	 */
	protected void computeDepthBiasMVPMatrix() {
		// matrices
		Matrix4x4 modelViewMatrix = shadowMapping.renderer.getModelViewMatrix();
		Matrix4x4 projectionMatrix = shadowMapping.renderer.getProjectionMatrix();

		// compute shadow texture matrix
		depthBiasMVPMatrix.
			set(modelViewMatrix).
			multiply(projectionMatrix).
			multiply(biasMatrix);
	}

	/**
	 * Set up shadow texture matrix computed and stored before
	 */
	protected void updateDepthBiasMVPMatrix() {
		shadowMapping.updateDepthBiasMVPMatrix(depthBiasMVPMatrix);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "ShadowMap [frameBuffer=" + frameBuffer + "]";
	}

}
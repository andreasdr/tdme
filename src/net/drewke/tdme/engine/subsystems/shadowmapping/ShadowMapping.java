package net.drewke.tdme.engine.subsystems.shadowmapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.subsystems.object.Object3DVBORenderer;
import net.drewke.tdme.engine.subsystems.object.Object3DVBORenderer.DepthBufferMode;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector4;

/**
 * Shadow mapping
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ShadowMapping {

	private enum RunState {NONE, PRE, RENDER};

	protected GLRenderer renderer;
	protected Object3DVBORenderer object3DVBORenderer;
	
	private Matrix4x4 shadowTransformationsMatrix;
	private Matrix4x4 depthBiasMVPMatrix;
	private Matrix4x4 tmpMatrix;
	private Matrix4x4 mvMatrix;
	private Matrix4x4 mvpMatrix;
	private Matrix4x4 normalMatrix;

	private Vector4 lightPositionTransformed;
	private Vector4 spotDirection4;
	private Vector4 spotDirection4Transformed;

	Engine engine;
	private ShadowMap[] shadowMaps;

	private int width;
	private int height;

	private RunState runState;

	/**
	 * Constructor
	 * @param engine
	 * @param renderer
	 * @param object 3d vbo renderer
	 */
	public ShadowMapping(Engine engine, GLRenderer renderer, Object3DVBORenderer object3DVBORenderer) {
		width = 0;
		height = 0;
		this.engine = engine;
		this.renderer = renderer;
		this.object3DVBORenderer = object3DVBORenderer;
		shadowMaps = new ShadowMap[engine.getLights().length];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = null;
		}
		shadowTransformationsMatrix = new Matrix4x4();
		depthBiasMVPMatrix = new Matrix4x4().identity();
		tmpMatrix = new Matrix4x4().identity();
		mvMatrix = new Matrix4x4().identity();
		mvpMatrix = new Matrix4x4().identity();
		normalMatrix = new Matrix4x4().identity();
		lightPositionTransformed = new Vector4();
		spotDirection4 = new Vector4();
		spotDirection4Transformed = new Vector4();
		runState = RunState.NONE;
	}

	/**
	 * @return engine
	 */
	public Engine getEngine() {
		return engine;
	}

	/**
	 * Reshape shadow maps
	 * @param width
	 * @param height
	 */
	public void reshape(int width, int height) {
		this.width = width;
		this.height = height;
		//
		for (int i = 0; i < shadowMaps.length; i++) {
			if (shadowMaps[i] != null) shadowMaps[i].reshape(width, height);
		}
	}

	/**
	 * Create shadow maps
	 */
	public void createShadowMaps(ArrayList<Object3D> objects) {
		runState = RunState.PRE;

		// disable color rendering, we only want to write to the Z-Buffer
		renderer.setColorMask(false, false, false, false);

		// render backfaces only, avoid self-shadowing
		renderer.setCullFace(renderer.CULLFACE_FRONT);

		// Use shadow mapping "pre programm"
		Engine.getShadowMappingShader().usePreProgram();

		// render to shadow maps
		for (int i = 0; i < engine.getLights().length; i++) {
			Light light = engine.getLightAt(i);
			if (light.isEnabled()) {
				// create shadow map for light, if required
				if (shadowMaps[i] == null) {
					ShadowMap shadowMap = new ShadowMap(this, width, height);
					shadowMap.init();
					shadowMaps[i] = shadowMap;
				}

				// render
				shadowMaps[i].render(light, objects);
			} else {
				// dispose shadow map
				if (shadowMaps[i] != null) {
					shadowMaps[i].dispose();
					shadowMaps[i] = null;
				}
			}
		}

		// restore disable color rendering
		renderer.setColorMask(true, true, true, true);

		// restore render backfaces only
		renderer.setCullFace(renderer.CULLFACE_BACK);

		//
		runState = RunState.NONE;
	}

	/**
	 * Render shadow maps
	 * @param visible objects
	 */
	public void renderShadowMaps(ArrayList<Object3D> visibleObjects) {
		runState = RunState.RENDER;

		// render using shadow mapping program
		ShadowMappingShader shader = Engine.getShadowMappingShader();
		shader.useProgram();
		shader.setProgramTextureUnit(ShadowMap.TEXTUREUNIT);

		//	do not allow writing to depth buffer
		renderer.disableDepthBuffer();
		//	only process nearest fragments
		renderer.setDepthFunction(renderer.DEPTHFUNCTION_EQUAL);

		// render each shadow map
		for (int i = 0; i < shadowMaps.length; i++) {
			// skip on unused shadow mapping
			if (shadowMaps[i] == null) continue;

			//
			ShadowMap shadowMap = shadowMaps[i];
			Light light = engine.getLightAt(i);

			// set up light shader uniforms
			shader.setProgramLightPosition(
				renderer.getCameraMatrix().multiply(
					light.getPosition(),
					lightPositionTransformed
				).scale(
					1f / lightPositionTransformed.getW()
				)
			);
			shader.setProgramLightDirection(
				renderer.getCameraMatrix().multiply(
					spotDirection4.set(
						light.getSpotDirection(),
						0.0f
					), 
					spotDirection4Transformed
				)
			);
			shader.setProgramLightSpotExponent(light.getSpotExponent());
			shader.setProgramLightSpotCosCutOff(light.getSpotCutOff());
			shader.setProgramLightConstantAttenuation(light.getConstantAttenuation());
			shader.setProgramLightLinearAttenuation(light.getLinearAttenuation());
			shader.setProgramLightQuadraticAttenuation(light.getQuadraticAttenuation());

			// set up texture pixel dimensions in shader
			shader.setProgramTexturePixelDimensions(
				1.0f / (float)shadowMap.getWidth(),
				1.0f / (float)shadowMap.getHeight()
			);

			// setup shadow texture matrix
			shadowMap.updateDepthBiasMVPMatrix();

			// bind shadow map texture on shadow map texture unit
			int textureUnit = renderer.getTextureUnit();
			renderer.setTextureUnit(ShadowMap.TEXTUREUNIT);
			shadowMap.bindDepthBufferTexture();
			// switch back to texture last unit
			renderer.setTextureUnit(textureUnit);

			// render objects, enable blending
			//	will be disabled after rendering transparent faces 
			renderer.enableBlending();

			// 	only opaque face entities as shadows will not be produced on transparent faces
			object3DVBORenderer.render(visibleObjects, true, DepthBufferMode.IGNORE, null);

			// disable blending
			renderer.disableBlending();
		}

		// restore gl texture matrix on texture unit
		int textureUnit = renderer.getTextureUnit();
		renderer.setTextureUnit(ShadowMap.TEXTUREUNIT);
		renderer.bindTexture(renderer.ID_NONE);
		renderer.setTextureUnit(textureUnit);

		// restore gl defaults
		renderer.disableBlending();
		renderer.enableDepthBuffer();
		renderer.setDepthFunction(renderer.DEPTHFUNCTION_LESSEQUAL);
		// renderer.useProgram(renderer.ID_NONE);

		//
		runState = RunState.NONE;
	}

	/**
	 * Dispose shadow maps
	 */
	public void dispose() {
		// dispose shadow mappings
		for (int i = 0; i < shadowMaps.length; i++) {
			if (shadowMaps[i] != null) {
				shadowMaps[i].dispose();
				shadowMaps[i] = null;
			}
		}
	}

	/**
	 * Start object transformations
	 * @param gl
	 * @param transformations matrix
	 */
	public void startObjectTransformations(Matrix4x4 transformationsMatrix) {
		if (runState != RunState.RENDER) return;

		// retrieve current model view matrix and put it on stack
		shadowTransformationsMatrix.set(
			depthBiasMVPMatrix
		);

		// set up new model view matrix
		tmpMatrix.set(depthBiasMVPMatrix);
		depthBiasMVPMatrix.
			set(transformationsMatrix).
			multiply(tmpMatrix);

		//
		updateDepthBiasMVPMatrix();
	}

	/**
	 * End object transformations
	 * @param gl
	 */
	public void endObjectTransformations() {
		if (runState != RunState.RENDER) return;

		// set up new model view matrix
		depthBiasMVPMatrix.set(shadowTransformationsMatrix);
	}

	/**
	 * Update model view and projection matrix
	 */
	public void updateMVPMatrices(GLRenderer renderer) {
		if (runState == RunState.NONE) return;

		// model view matrix
		mvMatrix.
			set(renderer.getModelViewMatrix());
		// object to screen matrix
		mvpMatrix.
			set(mvMatrix).
			multiply(
				renderer.getProjectionMatrix()
			);
		// normal matrix
		normalMatrix.
			set(mvMatrix).
			invert().
			transpose();

		// upload
		ShadowMappingShader shader = engine.getShadowMappingShader();
		switch (runState) {
			case PRE: {
				shader.setPreProgramMVPMatrix(mvpMatrix);
				break;
			}
			case RENDER: {
				shader.setProgramMVMatrix(mvMatrix);
				shader.setProgramMVPMatrix(mvpMatrix);
				shader.setProgramNormalMatrix(normalMatrix);
				break;
			}
			default: {
				System.out.println("ShadowMapping::updateMVPMatrices(): unsupported run state '" + runState + "'");
				break;
			}
		}

		// upload
		
	}

	/**
	 * Update depth bias mvp matrix with given matrix
	 */
	public void updateDepthBiasMVPMatrix(Matrix4x4 depthBiasMVPMatrix) {
		if (runState != RunState.RENDER) return;

		// copy matrix
		this.depthBiasMVPMatrix.set(depthBiasMVPMatrix);

		// upload
		engine.getShadowMappingShader().setProgramDepthBiasMVPMatrix(depthBiasMVPMatrix);
	}

	/**
	 * Update depth bias mvp matrix / upload only
	 */
	public void updateDepthBiasMVPMatrix() {
		if (runState != RunState.RENDER) return;

		// upload
		engine.getShadowMappingShader().setProgramDepthBiasMVPMatrix(depthBiasMVPMatrix);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "ShadowMapping [shadowMaps=" + Arrays.toString(shadowMaps)
				+ ", width=" + width + ", height=" + height + "]";
	}

}
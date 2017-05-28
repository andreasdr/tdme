package net.drewke.tdme.engine.subsystems.shadowmapping;

import java.util.Arrays;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;

/**
 * Shadow mapping shader
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ShadowMappingShader {

	private GLRenderer renderer;

	private int preVertexShaderGlId;
	private int preFragmentShaderGlId;
	private int preUniformMVPMatrix;
	private int preProgramGlId;
	private int renderVertexShaderGlId;
	private int renderFragmentShaderGlId;
	private int renderProgramGlId;
	private int renderUniformTextureUnit;
	private int renderUniformTexturePixelWidth;
	private int renderUniformTexturePixelHeight;
	private int renderUniformDepthBiasMVPMatrix;
	private int renderUniformMVMatrix;
	private int renderUniformMVPMatrix;
	private int renderUniformNormalMatrix;
	private int renderUniformLightPosition;
	private int renderUniformLightDirection;
	private int renderUniformLightSpotExponent;
	private int renderUniformLightSpotCosCutoff;
	private int renderUniformLightConstantAttenuation;
	private int renderUniformLightLinearAttenuation;
	private int renderUniformLightQuadraticAttenuation;

	private boolean initialized;

	/**
	 * Constructor
	 */
	public ShadowMappingShader(GLRenderer renderer) {
		this.renderer = renderer;
		initialized = false;
	}

	/**
	 * @return if initialized and ready to use
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Init shadow mapping
	 */
	public void init() {
		String rendererVersion = renderer.getGLVersion();

		// load shadow mapping shaders
		//	pre render
		preVertexShaderGlId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/shadowmapping",
			"pre_vertexshader.c"
		);
		if (preVertexShaderGlId == 0) return;

		preFragmentShaderGlId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/shadowmapping",
			"pre_fragmentshader.c"
		);
		if (preFragmentShaderGlId == 0) return;

		//	render
		renderVertexShaderGlId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/shadowmapping",
			"render_vertexshader.c"
		);
		if (renderVertexShaderGlId == 0) return;

		renderFragmentShaderGlId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/shadowmapping",
			"render_fragmentshader.c"
		);
		if (renderFragmentShaderGlId == 0) return;

		// create shadow mapping render program
		//	pre
		preProgramGlId = renderer.createProgram();
		renderer.attachShaderToProgram(preProgramGlId, preVertexShaderGlId);
		renderer.attachShaderToProgram(preProgramGlId, preFragmentShaderGlId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(preProgramGlId, 0, "inVertex");
			renderer.setProgramAttributeLocation(preProgramGlId, 1, "inNormal");
			renderer.setProgramAttributeLocation(preProgramGlId, 2, "inTextureUV");
		}

		// link
		if (renderer.linkProgram(preProgramGlId) == false) return;

		//	uniforms
		preUniformMVPMatrix = renderer.getProgramUniformLocation(preProgramGlId, "mvpMatrix");
		if (preUniformMVPMatrix == -1) return;

		//	render
		renderProgramGlId = renderer.createProgram();

		renderer.attachShaderToProgram(renderProgramGlId, renderVertexShaderGlId);
		renderer.attachShaderToProgram(renderProgramGlId, renderFragmentShaderGlId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(renderProgramGlId, 0, "inVertex");
			renderer.setProgramAttributeLocation(renderProgramGlId, 1, "inNormal");
			renderer.setProgramAttributeLocation(renderProgramGlId, 2, "inTextureUV");
		}

		if (renderer.linkProgram(renderProgramGlId) == false) return;

		// uniforms
		renderUniformTextureUnit = renderer.getProgramUniformLocation(renderProgramGlId, "textureUnit");
		if (renderUniformTextureUnit == -1) return;
		renderUniformTexturePixelWidth = renderer.getProgramUniformLocation(renderProgramGlId, "texturePixelWidth");
		if (renderUniformTexturePixelWidth == -1) return;
		renderUniformTexturePixelHeight = renderer.getProgramUniformLocation(renderProgramGlId, "texturePixelHeight");
		if (renderUniformTexturePixelHeight == -1) return;
		renderUniformDepthBiasMVPMatrix = renderer.getProgramUniformLocation(renderProgramGlId, "depthBiasMVPMatrix");
		if (renderUniformDepthBiasMVPMatrix == -1) return;
		renderUniformMVMatrix = renderer.getProgramUniformLocation(renderProgramGlId, "mvMatrix");
		if (renderUniformMVMatrix == -1) return;
		renderUniformMVPMatrix = renderer.getProgramUniformLocation(renderProgramGlId, "mvpMatrix");
		if (renderUniformMVPMatrix == -1) return;
		renderUniformNormalMatrix = renderer.getProgramUniformLocation(renderProgramGlId, "normalMatrix");
		if (renderUniformNormalMatrix == -1) return;

		// light
		renderUniformLightDirection = renderer.getProgramUniformLocation(renderProgramGlId, "lightDirection");
		if (renderUniformLightDirection == -1) return;

		// additional light, shadow uniforms
		// TODO: maybe find a better abstract way to determine if a renderer uses it or not
		if (rendererVersion.equals("gles2") == false) {
			renderUniformLightPosition = renderer.getProgramUniformLocation(renderProgramGlId, "lightPosition");
			if (renderUniformLightPosition == -1) return;
			renderUniformLightSpotExponent = renderer.getProgramUniformLocation(renderProgramGlId, "lightSpotExponent");
			if (renderUniformLightSpotExponent == -1) return;
			renderUniformLightSpotCosCutoff = renderer.getProgramUniformLocation(renderProgramGlId, "lightSpotCosCutoff");
			if (renderUniformLightSpotCosCutoff == -1) return;
			renderUniformLightConstantAttenuation = renderer.getProgramUniformLocation(renderProgramGlId, "lightConstantAttenuation");
			if (renderUniformLightConstantAttenuation == -1) return;
			renderUniformLightLinearAttenuation = renderer.getProgramUniformLocation(renderProgramGlId, "lightLinearAttenuation");
			if (renderUniformLightLinearAttenuation == -1) return;
			renderUniformLightQuadraticAttenuation = renderer.getProgramUniformLocation(renderProgramGlId, "lightQuadraticAttenuation");
			if (renderUniformLightQuadraticAttenuation == -1) return;
		}

		//
		initialized = true;
	}

	/**
	 * Use pre render shadow mapping program
	 */
	public void usePreProgram() {
		renderer.useProgram(preProgramGlId);
	}

	/**
	 * Use render shadow mapping program
	 */
	public void useProgram() {
		renderer.useProgram(renderProgramGlId);
	}

	/**
	 * Set up program texture unit
	 * @param texture unit
	 */
	public void setProgramTextureUnit(int textureUnit) {
		renderer.setProgramUniformInteger(renderUniformTextureUnit, textureUnit);
	}

	/**
	 * Set up program texture unit
	 * @param texture unit
	 */
	public void setProgramTexturePixelDimensions(float width, float height) {
		renderer.setProgramUniformFloat(renderUniformTexturePixelWidth, width);
		renderer.setProgramUniformFloat(renderUniformTexturePixelHeight, height);
	}

	/**
	 * Set up pre program mvp matrix
	 * @param mvp matrix
	 */
	public void setPreProgramMVPMatrix(Matrix4x4 mvpMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(preUniformMVPMatrix, mvpMatrix.getArray());
	}

	/**
	 * Set up program model view matrix
	 * @param model view matrix
	 */
	public void setProgramMVMatrix(Matrix4x4 mvMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(renderUniformMVMatrix, mvMatrix.getArray());
	}

	/**
	 * Set up program mvp matrix
	 * @param mvp matrix
	 */
	public void setProgramMVPMatrix(Matrix4x4 mvpMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(renderUniformMVPMatrix, mvpMatrix.getArray());
	}

	/**
	 * Set up program normal matrix
	 * @param normal matrix
	 */
	public void setProgramNormalMatrix(Matrix4x4 normalMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(renderUniformNormalMatrix, normalMatrix.getArray());
	}

	/**
	 * Set up program light position
	 * @param light position
	 */
	public void setProgramLightPosition(Vector3 lightPosition) {
		renderer.setProgramUniformFloatVec3(renderUniformLightPosition, lightPosition.getArray());
	}

	/**
	 * Set up program light position
	 * @param light position
	 */
	public void setProgramLightDirection(Vector3 lightDirection) {
		renderer.setProgramUniformFloatVec3(renderUniformLightDirection, lightDirection.getArray());
	}

	/**
	 * Set up program depth bias mvp matrix
	 * @param depth bias mvp matrix
	 */
	public void setProgramDepthBiasMVPMatrix(Matrix4x4 depthBiasMVPMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(renderUniformDepthBiasMVPMatrix, depthBiasMVPMatrix.getArray());
	}

	/**
	 * Set up light spot exponent
	 * @param spot exponent
	 */
	public void setProgramLightSpotExponent(float spotExponent) {
		renderer.setProgramUniformFloat(renderUniformLightSpotExponent, spotExponent);
	}

	/**
	 * Set up light spot cos cut off
	 * @param spot cos cut off
	 */
	public void setProgramLightSpotCosCutOff(float spotCosCutOff) {
		renderer.setProgramUniformFloat(renderUniformLightSpotCosCutoff, (float)Math.cos(Math.PI / 180f * spotCosCutOff));
	}

	/**
	 * Set up light constant attenuation
	 * @param constant attenuation
	 */
	public void setProgramLightConstantAttenuation(float constantAttenuation) {
		renderer.setProgramUniformFloat(renderUniformLightConstantAttenuation, constantAttenuation);
	}

	/**
	 * Set up light linear attenuation
	 * @param linear attenuation
	 */
	public void setProgramLightLinearAttenuation(float linearAttenuation) {
		renderer.setProgramUniformFloat(renderUniformLightLinearAttenuation, linearAttenuation);
	}

	/**
	 * Set up light quadratic attenuation
	 * @param quadratic attenuation
	 */
	public void setProgramLightQuadraticAttenuation(float quadraticAttenuation) {
		renderer.setProgramUniformFloat(renderUniformLightQuadraticAttenuation, quadraticAttenuation);
	}

}
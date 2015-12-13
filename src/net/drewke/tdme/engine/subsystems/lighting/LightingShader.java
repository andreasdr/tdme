package net.drewke.tdme.engine.subsystems.lighting;

import net.drewke.tdme.engine.subsystems.object.Object3DGroupMesh;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.engine.subsystems.shader.SkinningShader;
import net.drewke.tdme.math.Matrix4x4;

/**
 * Interface to gl3 lighting shader program
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LightingShader implements SkinningShader {

	public final static int MAX_LIGHTS = 8;

	public final static int TEXTUREUNIT_DIFFUSE = 0;
	public final static int TEXTUREUNIT_SPECULAR = 1;
	public final static int TEXTUREUNIT_DISPLACEMENT = 2;
	public final static int TEXTUREUNIT_NORMAL = 3;

	private int renderLightingProgramId;
	private int renderLightingFragmentShaderId;
	private int renderLightingVertexShaderId;

	/*

		We have the following data in shader program:
			
			struct Material {
				vec4 ambient;
				vec4 diffuse;
				vec4 specular;
				vec4 emission;
				float shininess;
			};

			struct Light {
				int enabled;
				vec4 ambient;
				vec4 diffuse;
				vec4 specular;
				vec4 position;
				vec3 spotDirection;
				float spotExponent;
				float spotCosCutoff;
				float constantAttenuation;
				float linearAttenuation;
				float quadraticAttenuation;
			};

			int diffuseTextureUnit
			mat4 mvpMatrix
			mat4 mvMatrix
			mat4 normalMatrix
			vec4 sceneColor

			uniform Material material;
			uniform Light lights[MAX_LIGHTS];
	*/

	private int uniformDiffuseTextureUnit;
	private int uniformDiffuseTextureAvailable;
	private int uniformSpecularTextureUnit;
	private int uniformSpecularTextureAvailable;
	private int uniformNormalTextureUnit;
	private int uniformNormalTextureAvailable;
	private int uniformDisplacementTextureUnit;
	private int uniformDisplacementTextureAvailable;
	private int uniformMVPMatrix;
	private int uniformMVMatrix;
	private int uniformNormalMatrix;
	private int uniformSceneColor;
	private int uniformEffectColorMul;
	private int uniformEffectColorAdd;
	
	private int uniformMaterialAmbient;
	private int uniformMaterialDiffuse;
	private int uniformMaterialSpecular;
	private int uniformMaterialEmission;
	private int uniformMaterialShininess;

	private int[] uniformLightEnabled;
	private int[] uniformLightAmbient;
	private int[] uniformLightDiffuse;
	private int[] uniformLightSpecular;
	private int[] uniformLightPosition;
	private int[] uniformLightSpotDirection;
	private int[] uniformLightSpotExponent;
	private int[] uniformLightSpotCosCutoff;
	private int[] uniformLightConstantAttenuation;
	private int[] uniformLightLinearAttenuation;
	private int[] uniformLightQuadraticAttenuation;

	private int uniformSkinningEnabled;
	private int uniformSkinningJointsTransformationsMatrices;

	private Matrix4x4 mvMatrix;
	private Matrix4x4 mvpMatrix;
	private Matrix4x4 normalMatrix;
	private float[] defaultSceneColor = {0f,0f,0f,0f};
	private float[] tmpColor4 = {0f,0f,0f,0f};

	private boolean isRunning;

	private boolean initialized;
	private GLRenderer renderer;

	/**
	 * Protected constructor
	 * @param renderer
	 */
	public LightingShader(GLRenderer renderer) {
		this.renderer = renderer;
		isRunning = false;
		initialized = false;
		uniformLightEnabled = new int[MAX_LIGHTS];
		uniformLightAmbient = new int[MAX_LIGHTS];
		uniformLightDiffuse = new int[MAX_LIGHTS];
		uniformLightSpecular = new int[MAX_LIGHTS];
		uniformLightPosition = new int[MAX_LIGHTS];
		uniformLightSpotDirection = new int[MAX_LIGHTS];
		uniformLightSpotExponent = new int[MAX_LIGHTS];
		uniformLightSpotCosCutoff = new int[MAX_LIGHTS];
		uniformLightConstantAttenuation = new int[MAX_LIGHTS];
		uniformLightLinearAttenuation = new int[MAX_LIGHTS];
		uniformLightQuadraticAttenuation = new int[MAX_LIGHTS];
		mvMatrix = new Matrix4x4();
		mvpMatrix = new Matrix4x4();
		normalMatrix = new Matrix4x4();
	}

	/**
	 * @return initialized and ready to be used
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Initialize renderer
	 */
	public void init() {
		String rendererVersion = renderer.getGLVersion();

		// lighting
		//	fragment shader
		renderLightingFragmentShaderId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/lighting",
			"render_fragmentshader.c"
		);
		if (renderLightingFragmentShaderId == 0) return;

		//	vertex shader
		renderLightingVertexShaderId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/lighting",
			"render_vertexshader.c"
		);
		if (renderLightingVertexShaderId == 0) return;

		// create, attach and link program
		renderLightingProgramId = renderer.createProgram();
		renderer.attachShaderToProgram(renderLightingProgramId, renderLightingVertexShaderId);
		renderer.attachShaderToProgram(renderLightingProgramId, renderLightingFragmentShaderId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(renderLightingProgramId, 0, "inVertex");
			renderer.setProgramAttributeLocation(renderLightingProgramId, 1, "inNormal");
			renderer.setProgramAttributeLocation(renderLightingProgramId, 2, "inTextureUV");
			// renderer.setProgramAttributeLocation(renderLightingProgramId, 3, "inColor");
			renderer.setProgramAttributeLocation(renderLightingProgramId, 4, "inSkinningVertexJoints");
			renderer.setProgramAttributeLocation(renderLightingProgramId, 5, "inSkinningVertexJointIdxs");
			renderer.setProgramAttributeLocation(renderLightingProgramId, 6, "inSkinningVertexJointWeights");
		}

		// link program
		if (renderer.linkProgram(renderLightingProgramId) == false) return;

		// get uniforms
		//	globals
		uniformDiffuseTextureUnit = renderer.getProgramUniformLocation(renderLightingProgramId, "diffuseTextureUnit");
		if (uniformDiffuseTextureUnit == -1) return;
		uniformDiffuseTextureAvailable = renderer.getProgramUniformLocation(renderLightingProgramId, "diffuseTextureAvailable");
		if (uniformDiffuseTextureAvailable == -1) return;
		if (renderer.isDisplacementMappingAvailable() == true) {
			uniformDisplacementTextureUnit = renderer.getProgramUniformLocation(renderLightingProgramId, "displacementTextureUnit");
			if (uniformDisplacementTextureUnit == -1) return;
			uniformDisplacementTextureAvailable = renderer.getProgramUniformLocation(renderLightingProgramId, "displacementTextureAvailable");
			if (uniformDisplacementTextureAvailable == -1) return;
		}
		if (renderer.isSpecularMappingAvailable()) {
			uniformSpecularTextureUnit = renderer.getProgramUniformLocation(renderLightingProgramId, "specularTextureUnit");
			if (uniformSpecularTextureUnit == -1) return;
			uniformSpecularTextureAvailable = renderer.getProgramUniformLocation(renderLightingProgramId, "specularTextureAvailable");
			if (uniformSpecularTextureAvailable == -1) return;
		}
		if (renderer.isNormalMappingAvailable()) {
			uniformNormalTextureUnit = renderer.getProgramUniformLocation(renderLightingProgramId, "normalTextureUnit");
			if (uniformNormalTextureUnit == -1) return;
			uniformNormalTextureAvailable = renderer.getProgramUniformLocation(renderLightingProgramId, "normalTextureAvailable");
			if (uniformNormalTextureAvailable == -1) return;
		}
		uniformMVPMatrix =  renderer.getProgramUniformLocation(renderLightingProgramId, "mvpMatrix");
		if (uniformMVPMatrix == -1) return;
		uniformMVMatrix =  renderer.getProgramUniformLocation(renderLightingProgramId, "mvMatrix");
		if (uniformMVMatrix == -1) return;
		uniformNormalMatrix = renderer.getProgramUniformLocation(renderLightingProgramId, "normalMatrix");
		if (uniformNormalMatrix == -1) return;
		uniformSceneColor = renderer.getProgramUniformLocation(renderLightingProgramId, "sceneColor");
		if (uniformSceneColor == -1) return;
		uniformEffectColorMul = renderer.getProgramUniformLocation(renderLightingProgramId, "effectColorMul");
		if (uniformEffectColorMul == -1) return;
		uniformEffectColorAdd = renderer.getProgramUniformLocation(renderLightingProgramId, "effectColorAdd");
		if (uniformEffectColorAdd == -1) return;

		//	material
		uniformMaterialAmbient =  renderer.getProgramUniformLocation(renderLightingProgramId, "material.ambient");
		if (uniformMaterialAmbient == -1) return;
		uniformMaterialDiffuse =  renderer.getProgramUniformLocation(renderLightingProgramId, "material.diffuse");
		if (uniformMaterialDiffuse == -1) return;
		uniformMaterialSpecular =  renderer.getProgramUniformLocation(renderLightingProgramId, "material.specular");
		if (uniformMaterialSpecular == -1) return;
		uniformMaterialEmission =  renderer.getProgramUniformLocation(renderLightingProgramId, "material.emission");
		if (uniformMaterialEmission == -1) return;
		uniformMaterialShininess =  renderer.getProgramUniformLocation(renderLightingProgramId, "material.shininess");
		if (uniformMaterialShininess == -1) return;

		//	lights
		for (int i = 0; i < MAX_LIGHTS; i++) {
			uniformLightEnabled[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].enabled");
			if (uniformLightEnabled[i] == -1) return;
			uniformLightAmbient[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].ambient");
			if (uniformLightAmbient[i] == -1) return;
			uniformLightDiffuse[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].diffuse");
			if (uniformLightDiffuse[i] == -1) return;
			uniformLightSpecular[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].specular");
			if (uniformLightSpecular[i] == -1) return;
			uniformLightPosition[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].position");
			if (uniformLightPosition[i] == -1) return;
			uniformLightSpotDirection[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].spotDirection");
			if (uniformLightSpotDirection[i] == -1) return;
			uniformLightSpotExponent[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].spotExponent");
			if (uniformLightSpotExponent[i] == -1) return;
			uniformLightSpotCosCutoff[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].spotCosCutoff");
			if (uniformLightSpotCosCutoff[i] == -1) return;
			uniformLightConstantAttenuation[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].constantAttenuation");
			if (uniformLightConstantAttenuation[i] == -1) return;
			uniformLightLinearAttenuation[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].linearAttenuation");
			if (uniformLightLinearAttenuation[i] == -1) return;
			uniformLightQuadraticAttenuation[i] = renderer.getProgramUniformLocation(renderLightingProgramId, "lights[" + i + "].quadraticAttenuation");
			if (uniformLightQuadraticAttenuation[i] == -1) return;
		}

		// shader support for skinning
		if (renderer.isSkinningAvailable() == true) {
			uniformSkinningEnabled = renderer.getProgramUniformLocation(renderLightingProgramId, "skinningEnabled");
			if (uniformSkinningEnabled == -1) return;
			uniformSkinningJointsTransformationsMatrices = renderer.getProgramUniformLocation(renderLightingProgramId, "skinningJointsTransformationsMatrices");
			if (uniformSkinningJointsTransformationsMatrices == -1) return;
		} else {
			uniformSkinningEnabled = -1;
			uniformSkinningJointsTransformationsMatrices = -1;
		}

		//
		initialized = true;
	}

	/**
	 * Use lighting program
	 */
	public void useProgram() {
		isRunning = true;
		renderer.useProgram(renderLightingProgramId);
		// initialize static uniforms
		renderer.setProgramUniformInteger(uniformDiffuseTextureUnit, TEXTUREUNIT_DIFFUSE);
		if (renderer.isSpecularMappingAvailable() == true) {
			renderer.setProgramUniformInteger(uniformSpecularTextureUnit, TEXTUREUNIT_SPECULAR);
		}
		if (renderer.isNormalMappingAvailable() == true) {
			renderer.setProgramUniformInteger(uniformNormalTextureUnit, TEXTUREUNIT_NORMAL);
		}
		if (renderer.isDisplacementMappingAvailable() == true) {
			renderer.setProgramUniformInteger(uniformDisplacementTextureUnit, TEXTUREUNIT_DISPLACEMENT);
		}
		renderer.setProgramUniformFloatVec4(uniformSceneColor, defaultSceneColor);
		// initialize dynamic uniforms
		updateEffect(renderer);
		updateMaterial(renderer);
		for (int i = 0; i < MAX_LIGHTS; i++) {
			updateLight(renderer, i);
		}
	}

	/**
	 * Unuse lighting program
	 */
	public void unUseProgram() {
		// Engine.getInstance().renderer.useProgram(Engine.getInstance().renderer.ID_NONE);
		isRunning = false;
	}

	/**
	 * Update effect to program
	 * @param renderer
	 */
	public void updateEffect(GLRenderer renderer) {
		// skip if not running
		if (isRunning == false) return;

		// effect color
		renderer.setProgramUniformFloatVec4(uniformEffectColorMul, renderer.effectColorMul);
		renderer.setProgramUniformFloatVec4(uniformEffectColorAdd, renderer.effectColorAdd);
	}

	/**
	 * Update material to program
	 * @param gl3 renderer
	 */
	public void updateMaterial(GLRenderer renderer) {
		// skip if not running
		if (isRunning == false) return;

		// we dont have alpha on ambient, specular, emission
		tmpColor4[3] = 0.0f;

		// ambient without alpha, as we only use alpha from diffuse color
		System.arraycopy(renderer.material.ambient, 0, tmpColor4, 0, 3);
		renderer.setProgramUniformFloatVec4(
			uniformMaterialAmbient,
			tmpColor4
		);

		// diffuse
		renderer.setProgramUniformFloatVec4(uniformMaterialDiffuse, renderer.material.diffuse);

		// specular without alpha, as we only use alpha from diffuse color
		System.arraycopy(renderer.material.specular, 0, tmpColor4, 0, 3);
		renderer.setProgramUniformFloatVec4(
			uniformMaterialSpecular,
			tmpColor4
		);

		// emission without alpha, as we only use alpha from diffuse color
		System.arraycopy(renderer.material.emission, 0, tmpColor4, 0, 3);
		renderer.setProgramUniformFloatVec4(
			uniformMaterialEmission,
			tmpColor4
		);

		// shininess
		renderer.setProgramUniformFloat(uniformMaterialShininess, renderer.material.shininess);
	}

	/**
	 * Update light to program
	 * @param renderer
	 * @param light id
	 */
	public void updateLight(GLRenderer renderer, int lightId) {
		// skip if not running
		if (isRunning == false) return;

		//
		renderer.setProgramUniformInteger(uniformLightEnabled[lightId], renderer.lights[lightId].enabled);
		if (renderer.lights[lightId].enabled == 1) {
			renderer.setProgramUniformFloatVec4(uniformLightAmbient[lightId], renderer.lights[lightId].ambient);
			renderer.setProgramUniformFloatVec4(uniformLightDiffuse[lightId], renderer.lights[lightId].diffuse);
			renderer.setProgramUniformFloatVec4(uniformLightSpecular[lightId], renderer.lights[lightId].specular);
			renderer.setProgramUniformFloatVec4(uniformLightPosition[lightId], renderer.lights[lightId].position);
			renderer.setProgramUniformFloatVec3(uniformLightSpotDirection[lightId], renderer.lights[lightId].spotDirection);
			renderer.setProgramUniformFloat(uniformLightSpotExponent[lightId], renderer.lights[lightId].spotExponent);
			renderer.setProgramUniformFloat(uniformLightSpotCosCutoff[lightId], renderer.lights[lightId].spotCosCutoff);
			renderer.setProgramUniformFloat(uniformLightConstantAttenuation[lightId], renderer.lights[lightId].constantAttenuation);
			renderer.setProgramUniformFloat(uniformLightLinearAttenuation[lightId], renderer.lights[lightId].linearAttenuation);
			renderer.setProgramUniformFloat(uniformLightQuadraticAttenuation[lightId], renderer.lights[lightId].quadraticAttenuation);
		}
	}

	/**
	 * Update matrices to program
	 * @param renderer
	 */
	public void updateMatrices(GLRenderer renderer) {
		// skip if not running
		if (isRunning == false) return;

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

		// upload matrices
		renderer.setProgramUniformFloatMatrix4x4(uniformMVPMatrix, mvpMatrix.getArray());
		renderer.setProgramUniformFloatMatrix4x4(uniformMVMatrix, mvMatrix.getArray());
		renderer.setProgramUniformFloatMatrix4x4(uniformNormalMatrix, normalMatrix.getArray());
	}

	/**
	 * Bind texture
	 * @param renderer
	 * @param texture id
	 */
	public void bindTexture(GLRenderer renderer, int textureId) {
		// skip if not running
		if (isRunning == false) return;

		switch(renderer.getTextureUnit()) {
			case TEXTUREUNIT_DIFFUSE:
				renderer.setProgramUniformInteger(uniformDiffuseTextureAvailable, textureId == 0?0:1);
				break;
			case TEXTUREUNIT_SPECULAR:
				if (renderer.isSpecularMappingAvailable() == false) break;
				renderer.setProgramUniformInteger(uniformSpecularTextureAvailable, textureId == 0?0:1);
				break;
			case TEXTUREUNIT_NORMAL:
				if (renderer.isNormalMappingAvailable() == false) break;
				renderer.setProgramUniformInteger(uniformNormalTextureAvailable, textureId == 0?0:1);
				break;
			case TEXTUREUNIT_DISPLACEMENT:
				if (renderer.isDisplacementMappingAvailable() == false) break;
				renderer.setProgramUniformInteger(uniformDisplacementTextureAvailable, textureId == 0?0:1);
				break;
		}
	}

	/**
	 * Initializes skinning
	 * @param renderer
	 * @param mesh
	 */
	public void initSkinning(GLRenderer renderer, Object3DGroupMesh mesh) {
		// skip if not running
		if (isRunning == false) return;

		renderer.setProgramUniformInteger(uniformSkinningEnabled, 1);
		renderer.setProgramUniformFloatMatrices4x4(
			uniformSkinningJointsTransformationsMatrices,
			mesh.getSkinningJoints(),
			mesh.getSkinningJointsTransformationsMatricesFloatBuffer()
		);
	}

	/**
	 * Done skinning
	 * @param renderer
	 * @param mesh
	 */
	public void doneSkinning(GLRenderer renderer) {
		// skip if not running
		if (isRunning == false) return;

		renderer.setProgramUniformInteger(uniformSkinningEnabled, 0);
	}

}

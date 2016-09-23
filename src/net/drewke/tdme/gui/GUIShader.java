package net.drewke.tdme.gui;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;

/**
 * GUI Shader
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIShader {

	private GLRenderer renderer;

	private int vertexShaderGlId;
	private int fragmentShaderGlId;
	private int programGlId;
	private int uniformDiffuseTextureUnit;
	private int uniformDiffuseTextureAvailable;
	private int uniformEffectColorMul;
	private int uniformEffectColorAdd;

	private boolean initialized;
	private boolean isRunning;

	/**
	 * Constructor
	 */
	public GUIShader(GLRenderer renderer) {
		this.renderer = renderer;
		initialized = false;
		isRunning = false;
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

		// render shader
		vertexShaderGlId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/gui",
			"render_vertexshader.c"
		);
		if (vertexShaderGlId == 0) return;

		fragmentShaderGlId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/gui",
			"render_fragmentshader.c"
		);
		if (fragmentShaderGlId == 0) return;

		// create gui render program
		programGlId = renderer.createProgram();

		renderer.attachShaderToProgram(programGlId, vertexShaderGlId);
		renderer.attachShaderToProgram(programGlId, fragmentShaderGlId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(programGlId, 0, "inVertex");
			renderer.setProgramAttributeLocation(programGlId, 2, "inTextureUV");
			renderer.setProgramAttributeLocation(programGlId, 3, "inColor");
		}

		if (renderer.linkProgram(programGlId) == false) return;

		// uniforms
		uniformDiffuseTextureUnit = renderer.getProgramUniformLocation(programGlId, "diffuseTextureUnit");
		if (uniformDiffuseTextureUnit == -1) return;
		uniformDiffuseTextureAvailable = renderer.getProgramUniformLocation(programGlId, "diffuseTextureAvailable");
		if (uniformDiffuseTextureAvailable == -1) return;
		uniformEffectColorMul = renderer.getProgramUniformLocation(programGlId, "effectColorMul");
		if (uniformEffectColorMul == -1) return;
		uniformEffectColorAdd = renderer.getProgramUniformLocation(programGlId, "effectColorAdd");

		//
		initialized = true;
	}

	/**
	 * Use render shadow mapping program
	 */
	public void useProgram() {
		// yep, use program
		renderer.useProgram(programGlId);
		// initialize static uniforms
		renderer.setProgramUniformInteger(uniformDiffuseTextureUnit, 0);
		// we are running
		isRunning = true;
	}

	/**
	 * Bind texture
	 * @param renderer
	 * @param texture id
	 */
	public void bindTexture(GLRenderer renderer, int textureId) {
		// skip if not running
		if (isRunning == false) return;

		//
		renderer.setProgramUniformInteger(uniformDiffuseTextureAvailable, textureId == 0?0:1);
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

}

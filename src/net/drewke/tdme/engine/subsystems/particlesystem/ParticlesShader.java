package net.drewke.tdme.engine.subsystems.particlesystem;

import java.nio.ByteBuffer;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.ImageLoader;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;

/**
 * Interface to particles shader program
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ParticlesShader {

	private int renderProgramId;
	private int renderFragmentShaderId;
	private int renderVertexShaderId;

	private int uniformMVPMatrix;
	private int uniformPointSize;
	private int uniformDiffuseTextureUnit;
	private int uniformEffectColorMul;
	private int uniformEffectColorAdd;
	
	private Matrix4x4 mvpMatrix;
	private int pointTextureId;

	private boolean isRunning;

	private boolean initialized;
	private Engine engine;
	private GLRenderer renderer;

	/**
	 * Protected constructor
	 * @param renderer
	 */
	public ParticlesShader(Engine engine, GLRenderer renderer) {
		this.engine = engine;
		this.renderer = renderer;
		isRunning = false;
		initialized = false;
		mvpMatrix = new Matrix4x4();
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
		renderFragmentShaderId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/particles",
			"render_fragmentshader.c"
		);
		if (renderFragmentShaderId == 0) return;

		//	vertex shader
		renderVertexShaderId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/particles",
			"render_vertexshader.c"
		);
		if (renderVertexShaderId == 0) return;

		// create, attach and link program
		renderProgramId = renderer.createProgram();
		renderer.attachShaderToProgram(renderProgramId, renderVertexShaderId);
		renderer.attachShaderToProgram(renderProgramId, renderFragmentShaderId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(renderProgramId, 0, "inVertex");
			renderer.setProgramAttributeLocation(renderProgramId, 3, "inColor");
		}

		// link program
		if (renderer.linkProgram(renderProgramId) == false) return;

		// get uniforms
		//	globals
		uniformMVPMatrix =  renderer.getProgramUniformLocation(renderProgramId, "mvpMatrix");
		if (uniformMVPMatrix == -1) return;
		uniformPointSize =  renderer.getProgramUniformLocation(renderProgramId, "pointSize");
		//
		uniformDiffuseTextureUnit = renderer.getProgramUniformLocation(renderProgramId, "diffuseTextureUnit");
		if (uniformDiffuseTextureUnit == -1) return;
		uniformEffectColorMul = renderer.getProgramUniformLocation(renderProgramId, "effectColorMul");
		if (uniformEffectColorMul == -1) return;
		uniformEffectColorAdd = renderer.getProgramUniformLocation(renderProgramId, "effectColorAdd");
		if (uniformEffectColorAdd == -1) return;

		// load point sprite
		pointTextureId = engine.getTextureManager().addTexture(TextureLoader.loadTexture("resources/textures", "point.png"));

		//
		initialized = true;
	}

	/**
	 * Use lighting program
	 */
	public void useProgram() {
		isRunning = true;
		renderer.useProgram(renderProgramId);
		renderer.setProgramUniformInteger(uniformDiffuseTextureUnit, 0);
		if (uniformPointSize != -1) renderer.setProgramUniformFloat(uniformPointSize, renderer.pointSize);
		renderer.bindTexture(pointTextureId);
		
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
	 * Unuse lighting program
	 */
	public void unUseProgram() {
		// Engine.getInstance().renderer.useProgram(Engine.getInstance().renderer.ID_NONE);
		isRunning = false;
		renderer.bindTexture(renderer.ID_NONE);
	}

	/**
	 * Update matrices to program
	 * @param renderer
	 */
	public void updateMatrices(GLRenderer renderer) {
		// skip if not running
		if (isRunning == false) return;

		// object to screen matrix
		mvpMatrix.
			set(renderer.getModelViewMatrix()).
			multiply(
				renderer.getProjectionMatrix()
			);

		// upload matrices
		renderer.setProgramUniformFloatMatrix4x4(uniformMVPMatrix, mvpMatrix.getArray());
	}

}

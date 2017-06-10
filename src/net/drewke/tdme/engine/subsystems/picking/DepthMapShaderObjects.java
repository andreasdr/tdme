package net.drewke.tdme.engine.subsystems.picking;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;

/**
 * Depth map shader objects 
 * @author Andreas Drewke
 * @version $Id$
 */
public class DepthMapShaderObjects {

	private GLRenderer renderer;
	
	private int vertexShaderId;
	private int fragmentShaderGlId;
	private int uniformMVPMatrix;
	private int programId;
	
	private boolean initialized;
	private boolean running;

	private Matrix4x4 mvpMatrix;

	/**
	 * Constructor
	 */
	public DepthMapShaderObjects(GLRenderer renderer) {
		this.renderer = renderer;
		initialized = false;
		running = false;
		mvpMatrix = new Matrix4x4();
	}

	/**
	 * @return if initialized and ready to use
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Init depth map shader
	 */
	public void initialize() {
		String rendererVersion = renderer.getGLVersion();

		// load shadow mapping shaders
		//	pre render
		vertexShaderId = renderer.loadShader(
			renderer.SHADER_VERTEX_SHADER,
			"shader/" + rendererVersion + "/depthmap",
			"object_vertexshader.c"
		);
		if (vertexShaderId == 0) return;

		fragmentShaderGlId = renderer.loadShader(
			renderer.SHADER_FRAGMENT_SHADER,
			"shader/" + rendererVersion + "/depthmap",
			"object_fragmentshader.c"
		);
		if (fragmentShaderGlId == 0) return;

		// create shadow mapping render program
		//	pre
		programId = renderer.createProgram();
		renderer.attachShaderToProgram(programId, vertexShaderId);
		renderer.attachShaderToProgram(programId, fragmentShaderGlId);

		// map inputs to attributes
		if (renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(programId, 0, "inVertex");
			renderer.setProgramAttributeLocation(programId, 1, "inNormal");
			renderer.setProgramAttributeLocation(programId, 2, "inTextureUV");
		}

		// link
		if (renderer.linkProgram(programId) == false) return;

		//	uniforms
		uniformMVPMatrix = renderer.getProgramUniformLocation(programId, "mvpMatrix");
		if (uniformMVPMatrix == -1) return;

		//
		initialized = true;
	}

	/**
	 * Use depth map shader program
	 */
	public void useProgram() {
		running = true;
		renderer.useProgram(programId);
	}

	/**
	 * Un use depth map shader program
	 */
	public void unUseProgram() {
		running = false;
	}

	/**
	 * Set up matrices
	 */
	public void updateMatrices() {
		// skip if not running
		if (running == false) return;

		// mvp matrix
		mvpMatrix.
			set(renderer.getModelViewMatrix()).
			multiply(
				renderer.getProjectionMatrix()
			);
		renderer.setProgramUniformFloatMatrix4x4(uniformMVPMatrix, mvpMatrix.getArray());
	}

}

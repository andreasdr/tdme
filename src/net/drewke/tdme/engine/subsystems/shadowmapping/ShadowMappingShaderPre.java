package net.drewke.tdme.engine.subsystems.shadowmapping;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;

/**
 * Pre shadow mapping shader for render shadow map pass 
 * @author Andreas Drewke
 * @version $Id$
 */
public class ShadowMappingShaderPre {

	private GLRenderer renderer;
	
	private int preVertexShaderGlId;
	private int preFragmentShaderGlId;
	private int preUniformMVPMatrix;
	private int preProgramGlId;
	
	private boolean initialized;

	/**
	 * Constructor
	 */
	public ShadowMappingShaderPre(GLRenderer renderer) {
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

		//
		initialized = true;
	}

	/**
	 * Use pre render shadow mapping program
	 */
	public void useProgram() {
		renderer.useProgram(preProgramGlId);
	}

	/**
	 * Set up pre program mvp matrix
	 * @param mvp matrix
	 */
	public void setProgramMVPMatrix(Matrix4x4 mvpMatrix) {
		renderer.setProgramUniformFloatMatrix4x4(preUniformMVPMatrix, mvpMatrix.getArray());
	}
	
}

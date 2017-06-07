package net.drewke.tdme.engine.subsystems.skinning;

import net.drewke.tdme.engine.subsystems.object.Object3DGroupMesh;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;

/**
 * Skinning shader base
 * @author Andreas Drewke
 * @version $Id$
 */
public class SkinningShader {

	private int uniformSkinningEnabled;
	private int uniformSkinningJointsTransformationsMatrices;

	private boolean isRunning;
	private boolean initialized;

	/**
	 * Public constructor
	 */
	protected SkinningShader() {
	}

	/**
	 * @return initialized and ready to be used
	 */
	protected boolean isInitialized() {
		return initialized;
	}

	/**
	 * Use lighting program
	 */
	protected void useProgram() {
		isRunning = true;
	}

	/**
	 * Unuse lighting program
	 */
	protected void unUseProgram() {
		isRunning = false;
	}

	/**
	 * Pre link init
	 * @param renderer
	 * @param program id
	 */
	protected void preLinkInit(GLRenderer renderer, int programId) {
		// map inputs to attributes
		if (renderer.isSkinningAvailable() == true &&
			renderer.isUsingProgramAttributeLocation() == true) {
			renderer.setProgramAttributeLocation(programId, 4, "inSkinningVertexJoints");
			renderer.setProgramAttributeLocation(programId, 5, "inSkinningVertexJointIdxs");
			renderer.setProgramAttributeLocation(programId, 6, "inSkinningVertexJointWeights");
		}
	}

	/**
	 * Init skinnning shader
	 * @param renderer
	 * @param program id
	 */
	protected void initialize(GLRenderer renderer, int programId) {
		initialized = false;

		// shader support for skinning
		if (renderer.isSkinningAvailable() == true) {
			uniformSkinningEnabled = renderer.getProgramUniformLocation(programId, "skinningEnabled");
			if (uniformSkinningEnabled == -1) return;
			uniformSkinningJointsTransformationsMatrices = renderer.getProgramUniformLocation(programId, "skinningJointsTransformationsMatrices");
			if (uniformSkinningJointsTransformationsMatrices == -1) return;
		} else {
			uniformSkinningEnabled = -1;
			uniformSkinningJointsTransformationsMatrices = -1;
		}

		//
		initialized = true;
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

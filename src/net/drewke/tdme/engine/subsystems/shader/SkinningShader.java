package net.drewke.tdme.engine.subsystems.shader;

import net.drewke.tdme.engine.subsystems.object.Object3DGroupMesh;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;

/**
 * Skinning Shader
 * @author Andreas Drewke
 * @version $Id$ 
 */
public interface SkinningShader {

	/**
	 * Initializes skinning
	 * @param renderer
	 * @param mesh
	 */
	public void initSkinning(GLRenderer renderer, Object3DGroupMesh mesh);

	/**
	 * Done skinning
	 * @param renderer
	 * @param mesh
	 */
	public void doneSkinning(GLRenderer renderer);
	
}

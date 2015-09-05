package net.drewke.tdme.engine.subsystems.manager;

import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.utils.HashMap;

/**
 * Texture manager
 * @author Andreas Drewke
 * @version $Id$
 */
public final class TextureManager {

	/**
	 * Managed texture entity
	 * @author Andreas Drewke
	 */
	private class TextureManaged {

		String id;
		int glId;
		int referenceCounter;

		/**
		 * Protected constructor
		 * @param id
		 * @param gl id
		 */
		private TextureManaged(String id, int glId) {
			this.id = id;
			this.glId = glId;
			this.referenceCounter = 0;
		}

		/**
		 * @return texture id
		 */
		private String getId() {
			return id;
		}

		/**
		 * @return texture open gl id
		 */
		private int getGlId() {
			return glId;
		}

		/**
		 * @return reference counter
		 */
		private int getReferenceCounter() {
			return referenceCounter;
		}

		/**
		 * decrement reference counter
		 * @return if reference counter = 0
		 */
		private boolean decrementReferenceCounter() {
			referenceCounter--;
			return referenceCounter == 0;
		}

		/**
		 * increment reference counter
		 */
		private void incrementReferenceCounter() {
			referenceCounter++;
		}

	}

	private GLRenderer renderer;
	private HashMap<String, TextureManaged> textures;

	/**
	 * Public constructor
	 * @param renderer
	 */
	public TextureManager(GLRenderer renderer) {
		this.renderer = renderer;
		textures = new HashMap<String, TextureManaged>(); 
	}

	/**
	 * Adds a texture to manager / open gl stack
	 * @param texture
	 * @returns gl texture id
	 */
	public int addTexture(Texture texture) {
		// check if we already manage this texture
		TextureManaged textureManaged = textures.get(texture.getId());
		if (textureManaged != null) {
			//
			textureManaged.incrementReferenceCounter();

			// yep, return open gl id
			return textureManaged.getGlId();
		}

		// create texture
		int textureId = renderer.createTexture();

		// bind texture
		renderer.bindTexture(textureId);

		// upload texture
		renderer.uploadTexture(texture);

		// unbind texture
		renderer.bindTexture(renderer.ID_NONE);


		// create managed texture
		textureManaged = new TextureManaged(
			texture.getId(),
			textureId
		);
		textureManaged.incrementReferenceCounter();

		// add it to our textures
		textures.put(
			texture.getId(),
			textureManaged
		);

		// return open gl id
		return textureId;
	}

	/**
	 * Removes a texture from manager / open gl stack
	 * @param texture id
	 */
	public void removeTexture(String textureId) {
		TextureManaged textureManaged = textures.get(textureId);
		if (textureManaged != null) {
			if (textureManaged.decrementReferenceCounter()) {
				// delete texture
				renderer.disposeTexture(textureManaged.getGlId());
				// remove from our list
				textures.remove(textureId);
			}
			return;
		}
		System.out.println("Warning: texture not loaded by texture manager");
	}

}
package net.drewke.tdme.engine.subsystems.manager;

import java.util.Arrays;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;

/**
 * VBO manager
 * @author Andreas Drewke
 * @version $Id$
 */
public final class VBOManager {

	/**
	 * Managed VBO entity
	 * @author Andreas Drewke
	 */
	public class VBOManaged {

		private String id;
		private int[] vboGlIds;
		private int referenceCounter;
		private boolean uploaded;

		/**
		 * Protected constructor
		 * @param id
		 * @param vbo gl id
		 * @param referenceCounter
		 */
		private VBOManaged(String id, int[] vboGlIds) {
			this.id = id;
			this.vboGlIds = vboGlIds;
			this.referenceCounter = 0;
		}

		/**
		 * @return vbo id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return vbo gl ids
		 */
		public int[] getVBOGlIds() {
			return vboGlIds;
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

		/**
		 * @return if vbo's have been uploaded, will change internal flag to uploaded
		 */
		public boolean isUploaded() {
			if (uploaded == false) {
				uploaded = true;
				return false;
			} else {
				return true;
			}
		}

		/**
		 * @return string representation
		 */
		public String toString() {
			return "VBOManaged [id=" + id + ", vboGlIds="
					+ Arrays.toString(vboGlIds) + ", referenceCounter="
					+ referenceCounter + "]";
		}

	}

	private GLRenderer renderer;
	private HashMap<String, VBOManaged> vbos;

	/**
	 * Public constructor
	 * @param renderer
	 */
	public VBOManager(GLRenderer renderer) {
		this.renderer = renderer;
		vbos = new HashMap<String, VBOManaged>(); 
	}

	/**
	 * Adds a vbo to manager / open gl stack
	 * @param gl
	 * @param texture
	 * @param vbo id count
	 */
	public VBOManaged addVBO(String vboId, int ids) {
		// check if we already manage this vbo
		VBOManaged vboManaged = vbos.get(vboId);
		if (vboManaged != null) {
			//
			vboManaged.incrementReferenceCounter();

			// yep, return vbo id
			return vboManaged;
		}


		// create vertex buffer objects
		int[] vboIds = renderer.createBufferObjects(ids);

		// create managed texture
		vboManaged = new VBOManaged(
			vboId,
			vboIds
		);
		vboManaged.incrementReferenceCounter();

		// add it to our textures
		vbos.put(
			vboManaged.getId(),
			vboManaged
		);

		// return open gl id
		return vboManaged;
	}

	/**
	 * Removes a vbo from manager / open gl stack
	 * @param gl
	 * @param texture
	 */
	public void removeVBO(String vboId) {
		VBOManaged vboManaged = vbos.get(vboId);
		if (vboManaged != null) {
			if (vboManaged.decrementReferenceCounter()) {
				// delete vbos from open gl
				int[] vboIds = vboManaged.getVBOGlIds();
				renderer.disposeBufferObjects(vboIds);
				// remove from our list
				vbos.remove(vboId);
			}
			return;
		}
		Console.println("Warning: vbo not managed by vbo manager");
	}

}
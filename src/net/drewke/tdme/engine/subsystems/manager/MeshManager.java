package net.drewke.tdme.engine.subsystems.manager;

import net.drewke.tdme.engine.subsystems.object.Object3DGroupMesh;
import net.drewke.tdme.utils.HashMap;

/**
 * Mesh manager
 * @author Andreas Drewke
 * @version $Id$
 */
public final class MeshManager {

	/**
	 * Managed Mesh entity
	 * @author Andreas Drewke
	 */
	private class MeshManaged {

		String id;
		Object3DGroupMesh mesh;
		int referenceCounter;

		/**
		 * Protected constructor
		 * @param id
		 * @param vbo gl id
		 * @param referenceCounter
		 */
		private MeshManaged(String id, Object3DGroupMesh mesh) {
			this.id = id;
			this.mesh = mesh;
			this.referenceCounter = 0;
		}

		/**
		 * @return mesh id
		 */
		private String getId() {
			return id;
		}

		/**
		 * @return object 3d group mesh
		 */
		private Object3DGroupMesh getMesh() {
			return mesh;
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
		 * @return string representation
		 */
		public String toString() {
			return "MeshManaged [id=" + id + ", mesh=" + mesh
					+ ", referenceCounter=" + referenceCounter + "]";
		}

	}

	private HashMap<String, MeshManaged> meshes;

	/**
	 * Public constructor
	 */
	public MeshManager() {
		meshes = new HashMap<String, MeshManaged>(); 
	}

	/**
	 * Get mesh from managed meshes
	 * @param meshId
	 * @return object 3d group mesh or null
	 */
	public Object3DGroupMesh getMesh(String meshId) {
		// check if we already manage this mesh
		MeshManaged meshManaged = meshes.get(meshId);
		if (meshManaged != null) {
			//
			meshManaged.incrementReferenceCounter();
			//
			return meshManaged.getMesh();
		}

		// otherwise no mesh
		return null;
	}

	/**
	 * Adds a mesh to manager
	 * @param mesh id
	 * @param mesh
	 */
	public void addMesh(String meshId, Object3DGroupMesh mesh) {
		// create managed texture
		MeshManaged meshManaged = new MeshManaged(
			meshId,
			mesh
		);
		meshManaged.incrementReferenceCounter();

		// add it to our textures
		meshes.put(
			meshManaged.getId(),
			meshManaged
		);
	}

	/**
	 * Removes a mesh from manager
	 * @param gl
	 * @param texture
	 */
	public void removeMesh(String meshId) {
		MeshManaged meshManaged = meshes.get(meshId);
		if (meshManaged != null) {
			if (meshManaged.decrementReferenceCounter()) {
				// remove from our list
				meshes.remove(meshId);
			}
			return;
		}
		System.out.println("Warning: mesh not managed by mesh manager: " + meshId);
	}

}
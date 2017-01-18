package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.Timing;
import net.drewke.tdme.engine.model.AnimationSetup;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.HashMap;

/**
 * Model utilities
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelUtilitiesInternal {

	/**
	 * Model statistics class
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class ModelStatistics {
		private int opaqueFaceCount;
		private int transparentFaceCount;
		private int materialCount;

		/**
		 * Constructor
		 * @param solid face count
		 * @param transparent face count
		 * @param material count
		 */
		public ModelStatistics(int opaqueFaceCount, int transparentFaceCount, int materialCount) {
			super();
			this.opaqueFaceCount = opaqueFaceCount;
			this.transparentFaceCount = transparentFaceCount;
			this.materialCount = materialCount;
		}

		/**
		 * @return opaque face count
		 */
		public int getOpaqueFaceCount() {
			return opaqueFaceCount;
		}

		/**
		 * @return transparent face count
		 */
		public int getTransparentFaceCount() {
			return transparentFaceCount;
		}

		/**
		 * @return material count
		 */
		public int getMaterialCount() {
			return materialCount;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "ModelStatistics [opaqueFaceCount=" + opaqueFaceCount
					+ ", transparentFaceCount=" + transparentFaceCount
					+ ", materialCount=" + materialCount + "]";
		}
	}

	/**
	 * Creates a bounding box from given model
	 * @param model
	 * @return axis aligned bounding box
	 */
	public static BoundingBox createBoundingBox(Model model) {
		return ModelUtilitiesInternal.createBoundingBox(new Object3DModelInternal(model));
	}

	/**
	 * Creates a bounding box from given object3d model
	 * @param model
	 * @return axis aligned bounding box
	 */
	public static BoundingBox createBoundingBox(Object3DModelInternal object3DModelInternal) {
		Model model = object3DModelInternal.getModel();
		AnimationSetup defaultAnimation = model.getAnimationSetup(Model.ANIMATIONSETUP_DEFAULT);

		//
		float minX = 0f, minY = 0f, minZ = 0f;
		float maxX = 0f, maxY = 0f, maxZ = 0f;
		boolean firstVertex = true;

		// create bounding box for whole animation at 60fps
		AnimationState animationState = new AnimationState();
		animationState.setup = defaultAnimation;
		animationState.lastAtTime = Timing.UNDEFINED;
		animationState.currentAtTime = 0L;
		animationState.time = 0.0f;
		animationState.finished = false;
		for (float t = 0.0f;
			t <= (defaultAnimation != null?defaultAnimation.getFrames():0.0f) / model.getFPS();
			t+= 1f / model.getFPS()) {
			//

			// calculate transformations matrices without world transformations
			object3DModelInternal.computeTransformationsMatrices(
				model.getSubGroups(),
				object3DModelInternal.getModel().getImportTransformationsMatrix().clone().multiply(object3DModelInternal.getTransformationsMatrix()),
				animationState,
				0
			);

			Object3DGroup.computeTransformations(
				object3DModelInternal.object3dGroups,
				object3DModelInternal.transformationsMatrices
			);

			// parse through object groups to determine min, max
			for(Object3DGroup object3DGroup: object3DModelInternal.object3dGroups) {
				for(Vector3 vertex: object3DGroup.mesh.transformedVertices) {
					// vertex xyz array
					float[] vertexXYZ = vertex.getArray();
					// determine min, max
					if (firstVertex == true) {
						minX = vertexXYZ[0];
						minY = vertexXYZ[1];
						minZ = vertexXYZ[2];
						maxX = vertexXYZ[0];
						maxY = vertexXYZ[1];
						maxZ = vertexXYZ[2];
						firstVertex = false;
					} else {
						if (vertexXYZ[0] < minX) minX = vertexXYZ[0];
						if (vertexXYZ[1] < minY) minY = vertexXYZ[1];
						if (vertexXYZ[2] < minZ) minZ = vertexXYZ[2];
						if (vertexXYZ[0] > maxX) maxX = vertexXYZ[0];
						if (vertexXYZ[1] > maxY) maxY = vertexXYZ[1];
						if (vertexXYZ[2] > maxZ) maxZ = vertexXYZ[2];
					}
				}
			}
			animationState.currentAtTime = (long)(t * 1000f);
			animationState.lastAtTime = (long)(t * 1000f);
		}

		// skip on models without meshes to be rendered
		if (firstVertex == true) return null;

		// otherwise go with bounding box
		return new BoundingBox(
			new Vector3(minX, minY, minZ),
			new Vector3(maxX, maxY, maxZ)
		);
	}

	/**
	 * Invert normals of a model
	 * @param model
	 */
	public static void invertNormals(Model model) {
		invertNormals(model.getSubGroups());
	}

	/**
	 * Invert normals recursive
	 * @param groups
	 */
	private static void invertNormals(HashMap<String, Group> groups) {
		for (Group group: groups.getValuesIterator()) {
			// invert
			for(Vector3 normal: group.getNormals()) {
				normal.scale(-1f);
			}

			// process sub groups
			invertNormals(group.getSubGroups());
		}
	}

	/**
	 * Compute model statistics
	 * @param model
	 * @return model statistics
	 */
	public static ModelStatistics computeModelStatistics(Model model) {
		return ModelUtilitiesInternal.computeModelStatistics(new Object3DModelInternal(model));	
	}


	/**
	 * Compute model statistics
	 * @param object 3d model internal
	 * @return model statistics
	 */
	public static ModelStatistics computeModelStatistics(Object3DModelInternal object3DModelInternal) {
		HashMap<String, Integer> materialCountById = new HashMap<String, Integer>();
		int opaqueFaceCount = 0;
		int transparentFaceCount = 0;
		for(Object3DGroup object3DGroup: object3DModelInternal.object3dGroups) {
			// check each faces entity
			FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities();
			int facesEntityIdxCount = facesEntities.length;
			for (int faceEntityIdx = 0; faceEntityIdx < facesEntityIdxCount; faceEntityIdx++) {
				FacesEntity facesEntity = facesEntities[faceEntityIdx];
				int faces = facesEntity.getFaces().length;

				// material
				Material material = facesEntity.getMaterial();

				// determine if transparent
				boolean transparentFacesEntity = false;
				//	via material
				if (material != null) { 
					if (material.hasTransparency() == true) transparentFacesEntity = true;
				}

				// setup material usage
				String materialId = material == null?"tdme.material.none":material.getId();
				Integer materialCount = materialCountById.get(materialId);
				if (materialCount == null) {
					materialCountById.put(materialId, 1);
				} else {
					materialCount++;
				}

				// skip, if requested
				if (transparentFacesEntity == true) {
					// keep track of rendered faces
					transparentFaceCount+= faces;
	
					// skip to next entity
					continue;
				}
				opaqueFaceCount+= faces;
			}
		}

		// determine final material count
		int materialCount = 0;
		for (Integer material: materialCountById.getValuesIterator()) {
			materialCount++;
		}

		//
		return new ModelStatistics(opaqueFaceCount, transparentFaceCount, materialCount);
	}

	/**
	 * Compute if model 1 equals model 2
	 * @param model 1
	 * @param model 2
	 * @return model1 equals model2
	 */
	public static boolean equals(Model model1, Model model2) {
		return ModelUtilitiesInternal.equals(new Object3DModelInternal(model1), new Object3DModelInternal(model2));	
	}


	/**
	 * Compute if model 1 equals model 2
	 * @param model 1
	 * @param model 2
	 * @return model1 equals model2
	 */
	public static boolean equals(Object3DModelInternal object3DModel1Internal, Object3DModelInternal object3DModel2Internal) {
		// check number of object 3d groups 
		if (object3DModel1Internal.object3dGroups.length != object3DModel2Internal.object3dGroups.length) return false;

		//
		for(int i = 0; i < object3DModel1Internal.object3dGroups.length; i++) {
			Object3DGroup object3DGroupModel1 = object3DModel1Internal.object3dGroups[i];
			Object3DGroup object3DGroupModel2 = object3DModel2Internal.object3dGroups[i];

			FacesEntity[] facesEntitiesModel1 = object3DGroupModel1.group.getFacesEntities();
			FacesEntity[] facesEntitiesModel2 = object3DGroupModel2.group.getFacesEntities();

			// check transformation matrix
			if (object3DGroupModel1.group.getTransformationsMatrix().equals(object3DGroupModel2.group.getTransformationsMatrix()) == false) return false;

			// check number of faces entities
			if (facesEntitiesModel1.length != facesEntitiesModel2.length) return false;

			// check each faces entity
			for (int j = 0; j < facesEntitiesModel1.length; j++) {
				FacesEntity facesEntityModel1 = facesEntitiesModel1[j];
				FacesEntity facesEntityModel2 = facesEntitiesModel2[j];

				// check material
				if (facesEntityModel1.getMaterial().getId().equals(facesEntityModel2.getMaterial().getId()) == false) return false;

				// check faces
				Face[] facesModel1 = facesEntityModel1.getFaces();
				Face[] facesModel2 = facesEntityModel2.getFaces();

				// number of faces in faces entity
				if (facesModel1.length != facesModel2.length) return false;

				// face indices
				for (int k = 0; k < facesModel1.length; k++) {
					// vertex indices
					int[] vertexIndicesModel1 = facesModel1[k].getVertexIndices();
					int[] vertexIndicesModel2 = facesModel2[k].getVertexIndices();
					if (vertexIndicesModel1[0] != vertexIndicesModel2[0] ||
						vertexIndicesModel1[1] != vertexIndicesModel2[1] ||
						vertexIndicesModel1[2] != vertexIndicesModel2[2]) {
						return false;
					}

					// TODO: maybe other indices
				}

				// TODO: check vertices, normals and such
			}
		}

		//
		return true;
	}

}
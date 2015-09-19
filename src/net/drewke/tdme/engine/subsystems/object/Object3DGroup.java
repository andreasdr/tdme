package net.drewke.tdme.engine.subsystems.object;

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Engine.AnimationProcessingTarget;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Joint;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Skinning;
import net.drewke.tdme.engine.subsystems.manager.MeshManager;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.utils.HashMap;

/**
 * Object 3d render group 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Object3DGroup {

	private static long counter = 0;
	final protected static int GLTEXTUREID_NONE = -1; 
	final protected static int GLTEXTUREID_NOTUSED = 0;

	protected String id;
	protected Object3DBase object;
	protected Group group;
	protected boolean animated;
	protected int[] materialDiffuseTextureIdsByEntities = null;
	protected int[] dynamicDiffuseTextureIdsByEntities = null;
	protected int[] materialSpecularTextureIdsByEntities = null;
	protected int[] materialDisplacementTextureIdsByEntities = null;
	protected int[] materialNormalTextureIdsByEntities = null;
	protected Object3DGroupVBORenderer renderer;
	protected Object3DGroupMesh mesh;
	protected Matrix4x4 groupTransformationsMatrix = null;
	protected ArrayList<Matrix4x4> groupTransformationsMatricesVector = null;

	/**
	 * Creates object 3d groups from given object3d base object
	 * @param object 3d base
	 * @param use mesh manager
	 * @return object 3d group array
	 */
	protected static Object3DGroup[] createGroups(Object3DBase object, boolean useMeshManager, Engine.AnimationProcessingTarget animationProcessingTarget) {
		ArrayList<Object3DGroup> object3DGroups = new ArrayList<Object3DGroup>();
		Model model = object.getModel();
		createGroups(object, object3DGroups, model.getSubGroups(), model.hasAnimations(), useMeshManager, animationProcessingTarget);
		Object3DGroup[] object3DGroupArray = new Object3DGroup[object3DGroups.size()];
		object3DGroups.toArray(object3DGroupArray);
		return object3DGroupArray;
	}

	/**
	 * Creates a object 3d groups recursively for given group and it sub groups
	 * @param object 3D base
	 * @param object 3D groups
	 * @param groups
	 * @param animated
	 * @param use mesh manager
	 */
	private static void createGroups(Object3DBase object3D, ArrayList<Object3DGroup> object3DGroups, HashMap<String, Group> groups, boolean animated, boolean useMeshManager, Engine.AnimationProcessingTarget animationProcessingTarget) {
		for (Group group: groups.getValuesIterator()) {

			// skip on joints
			if (group.isJoint() == true) {
				continue;
			}

			// determine face count
			int faceCount = group.getFaceCount();

			// skip on groups without faces
			if (faceCount > 0) {
				// create group render data
				Object3DGroup object3DGroup = new Object3DGroup();
	
				// add it to group render data list
				object3DGroups.add(object3DGroup);

				// determine mesh id
				object3DGroup.id = group.getModel().getId() + ":" + group.getId() + ":" + animationProcessingTarget.toString().toLowerCase();
				if (animated == true &&
					(animationProcessingTarget == AnimationProcessingTarget.CPU ||
					animationProcessingTarget == AnimationProcessingTarget.CPU_NORENDERING)) {
					//
					object3DGroup.id+= ":" + (counter++);
				}

				object3DGroup.object = object3D;
				object3DGroup.group = group;
				object3DGroup.animated = animated;
				if (useMeshManager == true) {
					MeshManager meshManager = Engine.getInstance().getMeshManager();
					object3DGroup.mesh = meshManager.getMesh(object3DGroup.id);
					if (object3DGroup.mesh == null) {
						object3DGroup.mesh = Object3DGroupMesh.createMesh(animationProcessingTarget, group, object3D.transformationsMatrices);
						meshManager.addMesh(object3DGroup.id, object3DGroup.mesh);
					}
				} else {
					object3DGroup.mesh = Object3DGroupMesh.createMesh(animationProcessingTarget, group, object3D.transformationsMatrices);
				}
				object3DGroup.materialDiffuseTextureIdsByEntities = new int[group.getFacesEntities().length];
				object3DGroup.dynamicDiffuseTextureIdsByEntities = new int[group.getFacesEntities().length];
				object3DGroup.materialSpecularTextureIdsByEntities = new int[group.getFacesEntities().length];
				object3DGroup.materialDisplacementTextureIdsByEntities = new int[group.getFacesEntities().length];
				object3DGroup.materialNormalTextureIdsByEntities = new int[group.getFacesEntities().length];
				for (int j = 0; j < group.getFacesEntities().length; j++) {
					object3DGroup.materialDiffuseTextureIdsByEntities[j] = GLTEXTUREID_NONE;
					object3DGroup.dynamicDiffuseTextureIdsByEntities[j] = GLTEXTUREID_NONE;
					object3DGroup.materialSpecularTextureIdsByEntities[j] = GLTEXTUREID_NONE;
					object3DGroup.materialDisplacementTextureIdsByEntities[j] = GLTEXTUREID_NONE;
					object3DGroup.materialNormalTextureIdsByEntities[j] = GLTEXTUREID_NONE;
				}
	
				// determine renderer
				object3DGroup.renderer = new Object3DGroupVBORenderer(object3DGroup);

				// skinning
				Skinning skinning = group.getSkinning();
				if (skinning != null) {
					object3DGroup.groupTransformationsMatricesVector = new ArrayList<Matrix4x4>();
					for (Joint joint: skinning.getJoints()) {
						object3DGroup.groupTransformationsMatricesVector.add(object3D.transformationsMatrices.get(joint.getGroupId()));
					}
				} else {
					object3DGroup.groupTransformationsMatricesVector = null;
				}
				
				//
				object3DGroup.groupTransformationsMatrix = object3D.transformationsMatrices.get(group.getId());
			}

			// but still check sub groups
			createGroups(object3D, object3DGroups, group.getSubGroups(), animated, useMeshManager, animationProcessingTarget);
		}
	}

	/**
	 * Applies transformations to meshes for given object 3d groups
	 * @param group render data list
	 * @param transformation matrices
	 */
	protected static void computeTransformations(Object3DGroup[] object3DGroups, HashMap<String, Matrix4x4> transformationMatrices) {
		for (Object3DGroup object3DGroup: object3DGroups) {
			object3DGroup.mesh.computeTransformations(object3DGroup.group);
		}
	}

	/**
	 * Set up textures for given object3d group and faces entity
	 * @param object 3D group
	 * @param faces entity idx
	 */
	protected static void setupTextures(Object3DGroup object3DGroup, int facesEntityIdx) {
		FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities(); 
		Material material = facesEntities[facesEntityIdx].getMaterial();

		// get material or use default
		if (material == null) material = Material.getDefaultMaterial();

		// load diffuse texture
		if (object3DGroup.materialDiffuseTextureIdsByEntities[facesEntityIdx] == GLTEXTUREID_NONE) {
			if (material.getDiffuseTexture() != null) {
				//
				object3DGroup.materialDiffuseTextureIdsByEntities[facesEntityIdx] = Engine.getInstance().getTextureManager().addTexture(material.getDiffuseTexture());
			} else {
				object3DGroup.materialDiffuseTextureIdsByEntities[facesEntityIdx] = GLTEXTUREID_NOTUSED;
			}
		}

		// load specular texture
		if (object3DGroup.materialSpecularTextureIdsByEntities[facesEntityIdx] == GLTEXTUREID_NONE) {
			if (material.getSpecularTexture() != null) {
				//
				object3DGroup.materialSpecularTextureIdsByEntities[facesEntityIdx] = Engine.getInstance().getTextureManager().addTexture(material.getSpecularTexture());
			} else {
				object3DGroup.materialSpecularTextureIdsByEntities[facesEntityIdx] = GLTEXTUREID_NOTUSED;
			}
		}

		// load displacement texture
		if (object3DGroup.materialDisplacementTextureIdsByEntities[facesEntityIdx] == GLTEXTUREID_NONE) {
			if (material.getDisplacementTexture() != null) {
				object3DGroup.materialDisplacementTextureIdsByEntities[facesEntityIdx] = Engine.getInstance().getTextureManager().addTexture(material.getDisplacementTexture());
			} else {
				object3DGroup.materialDisplacementTextureIdsByEntities[facesEntityIdx] = GLTEXTUREID_NOTUSED;
			}
		}

		// load normal texture
		if (object3DGroup.materialNormalTextureIdsByEntities[facesEntityIdx] == GLTEXTUREID_NONE) {
			if (material.getNormalTexture() != null) {
				object3DGroup.materialNormalTextureIdsByEntities[facesEntityIdx] = Engine.getInstance().getTextureManager().addTexture(material.getNormalTexture());
			} else {
				object3DGroup.materialNormalTextureIdsByEntities[facesEntityIdx] = GLTEXTUREID_NOTUSED;
			}
		}
	}
	
}
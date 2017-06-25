package net.drewke.tdme.engine.model;

import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.utils.HashMap;

/**
 * Represents a 3d model
 * @author andreas.drewke
 * @version $Id$
 */
public final class Model {

	/**
	 * Up Vector
	 * @author Andreas Drewke
	 *
	 */
	public enum UpVector {
		Y_UP, Z_UP
	}

	public final static String ANIMATIONSETUP_DEFAULT = "tdme.default";
	public final static float FPS_DEFAULT = 30f;

	private String id;
	private String name;
	private UpVector upVector;
	private RotationOrder rotationOrder;
	private HashMap<String, Material> materials;
	private HashMap<String, Group> groups;
	private HashMap<String, Group> subGroups;

	private boolean hasSkinning;

	private float fps;
	private HashMap<String, AnimationSetup> animationSetups;

	private Matrix4x4 importTransformationsMatrix;

	private BoundingBox boundingBox;

	/**
	 * Public constructor
	 * @param id
	 * @param name
	 * @param up vector
	 * @param rotation order
	 * @param bounding box
	 */
	public Model(String id, String name, UpVector upVector, RotationOrder rotationOrder, BoundingBox boundingBox) {
		this.id = id;
		this.name = name;
		this.upVector = upVector;
		this.rotationOrder = rotationOrder;
		materials = new HashMap<String, Material>();
		groups = new HashMap<String, Group>();
		subGroups = new HashMap<String, Group>();
		hasSkinning = false;
		fps = FPS_DEFAULT;
		animationSetups = new HashMap<String, AnimationSetup>();
		importTransformationsMatrix = new Matrix4x4().identity();
		this.boundingBox = boundingBox;
	}

	/**
	 * @return model id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set up model id, can only be modified before usage as object 3d, ...
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return model name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return up vector
	 */
	public UpVector getUpVector() {
		return upVector;
	}

	/**
	 * @return rotation order
	 */
	public RotationOrder getRotationOrder() {
		return rotationOrder;
	}

	/**
	 * Returns all object materials
	 * @return materials
	 */
	public HashMap<String, Material> getMaterials() {
		return materials;
	}

	/**
	 * Returns all object's groups
	 * @return all groups
	 */
	public HashMap<String, Group> getGroups() {
		return groups;
	}

	/**
	 * Returns a group by given name or null
	 * @param id
	 * @return
	 */
	public Group getGroupById(String id) {
		return groups.get(id);
	}

	/**
	 * Returns object's sub groups
	 * @return sub groups
	 */
	public HashMap<String, Group> getSubGroups() {
		return subGroups;
	}

	/**
	 * Returns a sub group by given name or null
	 * @param id
	 * @return
	 */
	public Group getSubGroupById(String id) {
		return subGroups.get(id);
	}

	/**
	 * @return has skinning
	 */
	public boolean hasSkinning() {
		return hasSkinning;
	}

	/**
	 * Set up if model has skinning
	 * @param has skinning
	 */
	protected void setHasSkinning(boolean hasSkinning) {
		this.hasSkinning = hasSkinning;
	}

	/**
	 * @return frames per seconds
	 */
	public float getFPS() {
		return fps;
	}

	/**
	 * Set model animation frames per seconds
	 * @param fps
	 */
	public void setFPS(float fps) {
		this.fps = fps;
	}

	/**
	 * Adds an base animation setup
	 * @param id
	 * @param start frame
	 * @param end frame
	 * @param loop
	 * @return animation setup
	 */
	public AnimationSetup addAnimationSetup(String id, int startFrame, int endFrame, boolean loop) {
		AnimationSetup animationSetup = new AnimationSetup(this, id, startFrame, endFrame, loop, null);
		animationSetups.put(id, animationSetup);
		return animationSetup;
	}

	/**
	 * Adds an overlay animation setup
	 * @param id
	 * @param overlay from group id
	 * @param start frame
	 * @param end frame
	 * @param loop
	 * @return animation setup
	 */
	public AnimationSetup addOverlayAnimationSetup(String id, String overlayFromGroupId, int startFrame, int endFrame, boolean loop) {
		AnimationSetup animationSetup = new AnimationSetup(this, id, startFrame, endFrame, loop, overlayFromGroupId);
		animationSetups.put(id, animationSetup);
		return animationSetup;
	}

	/**
	 * @return animation setup for given id or null
	 */
	public AnimationSetup getAnimationSetup(String id) {
		return animationSetups.get(id);
	}

	/**
	 * @return animation setup for given id or null
	 */
	public HashMap<String, AnimationSetup> getAnimationSetups() {
		return animationSetups;
	}

	/**
	 * @return if model has animations
	 */
	public boolean hasAnimations() {
		return animationSetups.size() > 0;
	}

	/**
	 * @return import transformations matrix like converting Z-UP to Y-UP
	 */
	public Matrix4x4 getImportTransformationsMatrix() {
		return importTransformationsMatrix;
	}

	/**
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {
		if (boundingBox == null) {
			boundingBox = ModelUtilities.createBoundingBox(new Object3DModel(this));
		}
		return boundingBox;
	}

	/**
	 * Computes a transformations matrix for a given frame and group id
	 * @param frame
	 * @param group id
	 * @return group transformations matrix or null
	 */
	public Matrix4x4 computeTransformationsMatrix(int frame, String groupId) {
		return computeTransformationsMatrix(
			subGroups,
			importTransformationsMatrix,
			frame,
			groupId
		);
	}

	/**
	 * Computes a transformations matrix at a given frame for a given group id recursivly
	 * @param groups
	 * @param parent transformations matrix
	 * @param frame
	 * @param group id
	 * @return group transformations matrix or null
	 */
	protected Matrix4x4 computeTransformationsMatrix(HashMap<String, Group> groups, Matrix4x4 parentTransformationsMatrix, int frame, String groupId) {
		// iterate through groups
		for (Group group: groups.getValuesIterator()) {
			// group transformation matrix
			Matrix4x4 transformationsMatrix = null;

			// compute animation matrix if animation setups exist
			Animation animation = group.getAnimation();
			if (animation != null) {
				Matrix4x4[] animationMatrices = animation.getTransformationsMatrices();
				transformationsMatrix = animationMatrices[frame % animationMatrices.length].clone();
			}

			// do we have no animation matrix?
			if (transformationsMatrix == null) {
				// no animation matrix, set up local transformation matrix up as group matrix
				transformationsMatrix = group.getTransformationsMatrix().clone();
			} else {
				// we have animation matrix, so multiply it with group transformation matrix
				transformationsMatrix.multiply(group.getTransformationsMatrix());
			}

			// apply parent transformation matrix 
			if (parentTransformationsMatrix != null) {
				transformationsMatrix.multiply(parentTransformationsMatrix);
			}

			// return matrix if group matches
			if (group.getId().equals(groupId)) return transformationsMatrix;

			// calculate sub groups
			HashMap<String,Group> subGroups = group.getSubGroups(); 
			if (subGroups.size() > 0) {
				Matrix4x4 tmp = computeTransformationsMatrix(
					subGroups,
					transformationsMatrix,
					frame,
					groupId
				);
				if (tmp != null) return tmp;
			}
		}

		//
		return null;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "Model [name=" + name +
				", subGroups=" + subGroups + "]";
	}

} 

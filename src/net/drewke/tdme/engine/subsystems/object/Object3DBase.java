package net.drewke.tdme.engine.subsystems.object;

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Timing;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Animation;
import net.drewke.tdme.engine.model.AnimationSetup;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.Triangle;
import net.drewke.tdme.engine.subsystems.manager.MeshManager;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.HashMap;

/**
 * Object3DInternal base class, contains 
 * @author Andreas Drewke
 */
public class Object3DBase extends Transformations {

	protected Model model;
	protected HashMap<String, Matrix4x4> transformationsMatrices;
	protected Matrix4x4 parentTransformationsMatrix;
	protected Matrix4x4 transformationsMatrix;
	protected Matrix4x4[] transformationsMatricesStack;
	protected Matrix4x4 tmpMatrix1;

	protected AnimationState baseAnimation;
	protected HashMap<String, AnimationState> overlayAnimationsById;
	protected HashMap<String, AnimationState> overlayAnimationsByJointId; 
	protected Object3DGroup[] object3dGroups = null;

	protected boolean recreateBuffers;
	protected boolean usesMeshManager;
	protected Engine.AnimationProcessingTarget animationProcessingTarget;

	private ArrayList<AnimationState> overlayAnimationsToRemove;

	/**
	 * Public constructor
	 * @param model
	 */
	protected Object3DBase(Model model, boolean useMeshManager, Engine.AnimationProcessingTarget animationProcessingTarget) {
		this.model = model;
		this.animationProcessingTarget = animationProcessingTarget; 

		//
		this.baseAnimation = new AnimationState();
		this.overlayAnimationsById = new HashMap<String, AnimationState>();
		this.overlayAnimationsByJointId = new HashMap<String, AnimationState>();
		this.usesMeshManager = useMeshManager;
		this.overlayAnimationsToRemove = new ArrayList<AnimationState>();

		//
		transformationsMatrices = new HashMap<String, Matrix4x4>();
		parentTransformationsMatrix = new Matrix4x4();
		transformationsMatrix = super.getTransformationsMatrix();
		tmpMatrix1 = new Matrix4x4();

		// animation
		setAnimation(Model.ANIMATIONSETUP_DEFAULT);

		// create transformations matrices
		createTransformationsMatrices(model.getSubGroups());

		// object 3d groups
		object3dGroups = Object3DGroup.createGroups(this, useMeshManager, animationProcessingTarget);

		int transformationsMatricesStackDepth = determineTransformationsMatricesStackDepth(model.getSubGroups(), 0);
		transformationsMatricesStack = new Matrix4x4[transformationsMatricesStackDepth];
		for (int i = 0; i < transformationsMatricesStack.length; i++) {
			transformationsMatricesStack[i] = new Matrix4x4();
		}

		// calculate transformations matrices
		computeTransformationsMatrices(
			model.getSubGroups(),
			model.getImportTransformationsMatrix(),
			baseAnimation,
			0
		);

		// do initial transformations
		Object3DGroup.computeTransformations(object3dGroups, transformationsMatrices);

		// reset animation
		setAnimation(Model.ANIMATIONSETUP_DEFAULT);
	}

	/**
	 * @return model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Sets up a base animation to play
	 * @param id
	 */
	public void setAnimation(String id) {
		AnimationSetup _animationActiveSetup = model.getAnimationSetup(id);
		// only switch animation if we have one
		if (_animationActiveSetup != null) {
			baseAnimation.setup = _animationActiveSetup;
			baseAnimation.lastAtTime = Timing.UNDEFINED;
			baseAnimation.currentAtTime = 0L;
			baseAnimation.time = 0.0f;
			baseAnimation.finished = false;
		}
	}

	/**
	 * Overlays a animation above the base animation
	 * @param id
	 */
	public void addOverlayAnimation(String id) {
		// remove active overlay animation with given ids
		removeOverlayAnimation(id);

		// check overlay animation
		AnimationSetup animationSetup = model.getAnimationSetup(id);
		if (animationSetup == null) return;
		if (animationSetup.getOverlayFromGroupId() == null) return;

		// create animation state
		AnimationState animationState = new AnimationState();
		animationState.setup = animationSetup;
		animationState.lastAtTime = Timing.UNDEFINED;
		animationState.currentAtTime = 0L;
		animationState.time = 0.0f;
		animationState.finished = false;

		// register overlay animation
		overlayAnimationsById.put(id, animationState);
		overlayAnimationsByJointId.put(animationSetup.getOverlayFromGroupId(), animationState);
	}

	/**
	 * Removes a overlay animation
	 * @param id
	 */
	public void removeOverlayAnimation(String id) {
		AnimationState animationState = overlayAnimationsById.remove(id);
		if (animationState != null) overlayAnimationsByJointId.remove(animationState.setup.getOverlayFromGroupId());
	}

	/**
	 * Removes a overlay animation
	 * @param animation stati
	 */
	public void removeOverlayAnimation(AnimationState animationState) {
		AnimationState _animationState = overlayAnimationsById.remove(animationState.setup.getId());
		if (_animationState != null) overlayAnimationsByJointId.remove(_animationState.setup.getOverlayFromGroupId());
	}

	/**
	 * Removes all finished overlay animations
	 */
	public void removeOverlayAnimationsFinished() {
		// determine finished overlay animations
		overlayAnimationsToRemove.clear();
		for (AnimationState animationState: overlayAnimationsById.getValuesIterator()) {
			if (animationState.finished == true) {
				overlayAnimationsToRemove.add(animationState);
			}
		}

		// remove them
		for (int i = 0; i < overlayAnimationsToRemove.size(); i++) {
			removeOverlayAnimation(overlayAnimationsToRemove.get(i));
		}
	}

	/**
	 * Removes all overlay animations
	 */
	public void removeOverlayAnimations() {
		// remove them
		for (String overlayAnimationId: overlayAnimationsById.getKeysIterator()) {
			removeOverlayAnimation(overlayAnimationId);
		}
	}

	/**
	 * @return active animation setup id
	 */
	public String getAnimation() {
		return baseAnimation.setup == null?"none":baseAnimation.setup.getId();
	}

	/**
	 * Returns current base animation time 
	 * @return 0.0 <= time <= 1.0
	 */
	public float getAnimationTime() {
		return baseAnimation.time;
	}

	/**
	 * Returns if there is currently running a overlay animation with given id
	 * @param id
	 * @return animation is running
	 */
	public boolean hasOverlayAnimation(String id) {
		return overlayAnimationsById.get(id) != null;
	}

	/**
	 * Returns current overlay animation time
	 * @param id 
	 * @return 0.0 <= time <= 1.0
	 */
	public float getOverlayAnimationTime(String id) {
		AnimationState animationState = overlayAnimationsById.get(id);
		return animationState == null?1.0f:animationState.time;
	}

	/**
	 * Returns transformation matrix for given group
	 * @param group id
	 * @return transformation matrix or null
	 */
	public Matrix4x4 getTransformationsMatrix(String id) {
		return transformationsMatrices.get(id);
	}

	/**
	 * Creates all groups transformation matrices
	 * @param groups
	 * @depth
	 */
	protected void createTransformationsMatrices(HashMap<String, Group> groups) {
		// iterate through groups
		for (Group group: groups.getValuesIterator()) {
			// put and associate transformation matrices with group
			transformationsMatrices.put(group.getId(), new Matrix4x4().identity());

			// calculate sub groups
			HashMap<String,Group> subGroups = group.getSubGroups(); 
			if (subGroups.size() > 0) {
				createTransformationsMatrices(
					subGroups
				);
			}
		}
	}

	/**
	 * Calculates all groups transformation matrices
	 * @param groups
	 * @param parent transformations matrix
	 * @param animation state
	 * @param depth
	 */
	protected void computeTransformationsMatrices(HashMap<String, Group> groups, Matrix4x4 parentTransformationsMatrix, AnimationState animationState, int depth) {
		// iterate through groups
		for (Group group: groups.getValuesIterator()) {
			// check for overlay animation
			AnimationState overlayAnimation = overlayAnimationsByJointId.get(group.getId());
			if (overlayAnimation != null) animationState = overlayAnimation;

			// group transformation matrix
			Matrix4x4 transformationsMatrix = null;

			// compute animation matrix if animation setups exist
			Animation animation = group.getAnimation();
			if (animation != null && animationState.finished == false) {
				Matrix4x4[] animationMatrices = animation.getTransformationsMatrices();
				int frames = animationState.setup.getFrames();
				float fps = model.getFPS();

				// determine current and last matrix
				float frameAtLast = (animationState.lastAtTime / 1000f) * fps;
				float frameAtCurrent = (animationState.currentAtTime / 1000f) * fps;

				// check if looping is disabled
				if (animationState.setup.isLoop() == false && frameAtCurrent >= frames) {
					// set frame at current to last frame
					frameAtLast = frames - 1;
					frameAtCurrent = frames - 1;
					animationState.finished = true;
				}

				//
				int matrixAtLast = ((int)frameAtLast % frames);
				int matrixAtCurrent = ((int)frameAtCurrent % frames);
				animationState.time = frames <= 1?0.0f:(float)matrixAtCurrent / (float)(frames - 1); 

				// compute animation transformations matrix
				float t = frameAtCurrent - (float)Math.floor(frameAtLast);
				if (t < 1f) {
					if (matrixAtLast == matrixAtCurrent) {
						matrixAtCurrent = ((matrixAtCurrent + 1) % frames);
					}
					transformationsMatrix = Matrix4x4.interpolateLinear(
						animationMatrices[matrixAtLast + animationState.setup.getStartFrame()],
						animationMatrices[matrixAtCurrent + animationState.setup.getStartFrame()],
						t,
						tmpMatrix1
					);
				} else {
					transformationsMatrix = tmpMatrix1.set(animationMatrices[matrixAtCurrent + animationState.setup.getStartFrame()]);
				}
			}

			// do we have no animation matrix?
			if (transformationsMatrix == null) {
				// no animation matrix, set up local transformation matrix up as group matrix
				transformationsMatrix = tmpMatrix1.set(group.getTransformationsMatrix());
			} else {
				// we have animation matrix, so multiply it with group transformation matrix
				transformationsMatrix.multiply(group.getTransformationsMatrix());
			}

			// apply parent transformation matrix 
			if (parentTransformationsMatrix != null) {
				transformationsMatrix.multiply(parentTransformationsMatrix);
			}

			// put and associate transformation matrices with group
			transformationsMatrices.get(group.getId()).set(transformationsMatrix);

			// calculate for sub groups
			HashMap<String,Group> subGroups = group.getSubGroups(); 
			if (subGroups.size() > 0) {
				// put to matrices stack
				transformationsMatricesStack[depth].set(transformationsMatrix);

				// compute sub groups transformations
				computeTransformationsMatrices(
					subGroups,
					transformationsMatricesStack[depth],
					animationState,
					depth + 1
				);
			}
		}
	}

	/**
	 * Calculates all groups transformation matrices
	 * @param groups
	 * @param depth
	 */
	protected int determineTransformationsMatricesStackDepth(HashMap<String, Group> groups, int depth) {
		int depthMax = depth;
		// iterate through groups
		for (Group group: groups.getValuesIterator()) {
			// calculate sub groups
			HashMap<String,Group> subGroups = group.getSubGroups(); 
			if (subGroups.size() > 0) {
				int _depth = determineTransformationsMatricesStackDepth(
					subGroups,
					depth + 1
				);
				if (_depth > depthMax) depthMax = _depth;
			}
		}
		return depthMax;
	}

	/**
	 * Pre render step, computes transformations
	 */
	public void computeTransformations() {
		// do transformations if we have a animation
		if (baseAnimation.setup != null) {
			Engine engine = Engine.getInstance();

			// animation timing
			Timing timing = engine.getTiming();
			baseAnimation.lastAtTime = baseAnimation.currentAtTime;
			long currentFrameAtTime = timing.getCurrentFrameAtTime();
			long lastFrameAtTime = timing.getLastFrameAtTime();

			// do progress of base animation
			baseAnimation.lastAtTime = baseAnimation.currentAtTime;
			if (lastFrameAtTime != Timing.UNDEFINED) {
				baseAnimation.currentAtTime+= currentFrameAtTime - lastFrameAtTime; 
			}

			// do progress of overlay animations
			for (AnimationState overlayAnimationState: overlayAnimationsById.getValuesIterator()) {
				overlayAnimationState.lastAtTime = overlayAnimationState.currentAtTime;
				if (lastFrameAtTime != Timing.UNDEFINED) {
					overlayAnimationState.currentAtTime+= currentFrameAtTime - lastFrameAtTime; 
				}
			}

			// set up parent transformations matrix
			parentTransformationsMatrix.set(model.getImportTransformationsMatrix());
			if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
				parentTransformationsMatrix.multiply(transformationsMatrix);
			}

			// calculate transformations matrices
			computeTransformationsMatrices(
				model.getSubGroups(),
				parentTransformationsMatrix,
				baseAnimation,
				0
			);

			// do transformations in group render data
			Object3DGroup.computeTransformations(object3dGroups, transformationsMatrices);
		} else
		if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
			// set up parent transformations matrix
			parentTransformationsMatrix.set(model.getImportTransformationsMatrix());
			if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
				parentTransformationsMatrix.multiply(transformationsMatrix);
			}

			// calculate transformations matrices
			computeTransformationsMatrices(
				model.getSubGroups(),
				parentTransformationsMatrix,
				baseAnimation,
				0
			);

			// do transformations in group render data
			Object3DGroup.computeTransformations(object3dGroups, transformationsMatrices);
		}
	}

	/**
	 * Retrieves complete list of face triangles for all render groups 
	 * @return faces
	 */
	public Triangle[] getFaceTriangles() {
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		for (Object3DGroup object3DGroup: object3dGroups) {
			Vector3[] groupVerticesTransformed = object3DGroup.mesh.transformedVertices;
			for (FacesEntity facesEntity: object3DGroup.group.getFacesEntities())
			for (Face face: facesEntity.getFaces()) {
				int[] faceVertexIndices = face.getVertexIndices();
				triangles.add(
					new Triangle(
						groupVerticesTransformed[faceVertexIndices[0]].clone(),
						groupVerticesTransformed[faceVertexIndices[1]].clone(),
						groupVerticesTransformed[faceVertexIndices[2]].clone()
					)
				);
			}
		}
		Triangle[] triangleArray = new Triangle[triangles.size()];
		triangles.toArray(triangleArray);
		return triangleArray;
	}

	/**
	 * Returns object3d group mesh object
	 * @param group id
	 * @return object3d group mesh object
	 */
	public Object3DGroupMesh getMesh(String groupId) {
		// TODO: maybe rather use a hash map than an array to have a faster access
		for (Object3DGroup object3DGroup: object3dGroups) {
			if (object3DGroup.group.getId().equals(groupId)) {
				return object3DGroup.mesh;
			}
		}
		return null;
	}

	/**
	 * Initiates this object3d 
	 */
	public void init() {
		MeshManager meshManager = Engine.getInstance().getMeshManager();

		// init mesh
		for(int i = 0; i < object3dGroups.length; i++) {
			Object3DGroup object3DGroup = object3dGroups[i];

			// initiate mesh if not yet done, happens usually after disposing from engine and readding to engine
			if (object3DGroup.mesh == null) {
				if (usesMeshManager == true) {
					object3DGroup.mesh = meshManager.getMesh(object3DGroup.id);
					if (object3DGroup.mesh == null) {
						object3DGroup.mesh = Object3DGroupMesh.createMesh(animationProcessingTarget, object3DGroup.group, object3DGroup.object.transformationsMatrices);
						meshManager.addMesh(object3DGroup.id, object3DGroup.mesh);
					}
				} else {
					object3DGroup.mesh = Object3DGroupMesh.createMesh(animationProcessingTarget, object3DGroup.group, object3DGroup.object.transformationsMatrices);
				}			
			}
		}
	}

	/**
	 * Disposes this object3d 
	 */
	public void dispose() {
		MeshManager meshManager = Engine.getInstance().getMeshManager();

		// dispose mesh
		for(int i = 0; i < object3dGroups.length; i++) {
			Object3DGroup object3DGroup = object3dGroups[i];

			// dispose mesh
			meshManager.removeMesh(object3DGroup.id);
			object3DGroup.mesh = null;
		}
	}

}

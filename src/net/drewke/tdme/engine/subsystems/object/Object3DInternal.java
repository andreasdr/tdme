package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.FrameBuffer;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.manager.TextureManager;
import net.drewke.tdme.math.MathTools;

/**
 * TDME Object 3D
 * @author Andreas Drewke
 * @version $Id$
 */
public class Object3DInternal extends Object3DBase {

	protected String id;
	protected boolean enabled;
	protected boolean pickable;
	protected boolean dynamicShadowing;
	protected Color4 effectColorMul;
	protected Color4 effectColorAdd;
	protected BoundingBox boundingBox;
	protected BoundingBox boundingBoxTransformed;

	/**
	 * Public constructor
	 * @param model
	 */
	public Object3DInternal(String id, Model model) {
		super(model, true, Engine.animationProcessingTarget);
		this.id = id;
		enabled = true;
		pickable = false;
		dynamicShadowing = false;
		effectColorMul = new Color4(1.0f, 1.0f, 1.0f, 1.0f);
		effectColorAdd = new Color4(0.0f, 0.0f, 0.0f, 0.0f);
		boundingBox = (BoundingBox)model.getBoundingBox().clone();
		boundingBox.getMin().sub(0.1f); // scale a bit up to make picking work better
		boundingBox.getMax().add(0.1f); // same here
		boundingBox.update();
		boundingBoxTransformed = (BoundingBox)boundingBox.clone();
		boundingBoxTransformed.fromBoundingVolumeWithTransformations(boundingBox, this);
	}

	/**
	 * @return object id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return true if enabled to be rendered
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enable/disable rendering
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return if object is pickable
	 */
	public boolean isPickable() {
		return pickable;
	}

	/**
	 * Set this object pickable
	 * @param pickable
	 */
	public void setPickable(boolean pickable) {
		this.pickable = pickable;
	}

	/**
	 * @return dynamic shadowing enabled
	 */
	public boolean isDynamicShadowingEnabled() {
		return dynamicShadowing;
	}

	/**
	 * Enable/disable dynamic shadowing
	 * @param dynamicShadowing
	 */
	public void setDynamicShadowingEnabled(boolean dynamicShadowing) {
		this.dynamicShadowing = dynamicShadowing;
	}

	/**
	 * The effect color will be multiplied with fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorMul() {
		return effectColorMul;
	}

	/**
	 * The effect color will be added to fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorAdd() {
		return effectColorAdd;
	}

	/**
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Retrieves bounding sphere with transformations applied
	 * @return bounding sphere
	 */
	public BoundingBox getBoundingBoxTransformed() {
		return boundingBoxTransformed;
	}

	/**
	 * Bind frame buffer color texture to this object
	 * @param group id
	 * @param faces entity name or null if texture should be bound to all faces entities
	 * @param frame buffer
	 */
	public void bindDiffuseTexture(FrameBuffer frameBuffer) {
		setDynamicDiffuseTexture(null, null, frameBuffer.getColorBufferTextureId());
	}

	/**
	 * Bind frame buffer color texture to a group of this object
	 * @param group id
	 * @param faces entity name or null if texture should be bound to all faces entities
	 * @param frame buffer
	 */
	public void bindDiffuseTexture(String groupId, FrameBuffer frameBuffer) {
		setDynamicDiffuseTexture(groupId, null, frameBuffer.getColorBufferTextureId());
	}

	/**
	 * Bind frame buffer color texture to a group and faces entity of this object
	 * @param group id
	 * @param faces entity id
	 * @param frame buffer
	 */
	public void bindDiffuseTexture(String groupId, String facesEntityId, FrameBuffer frameBuffer) {
		setDynamicDiffuseTexture(groupId, facesEntityId, frameBuffer.getColorBufferTextureId());
	}

	/**
	 * Unbind dynamic texture of this object
	 */
	public void unbindDiffuseTexture() {
		setDynamicDiffuseTexture(null, null, Object3DGroup.GLTEXTUREID_NONE);
	}

	/**
	 * Unbind dynamic texture to a group of this object
	 * @param group id
	 */
	public void unbindDiffuseTexture(String groupId) {
		setDynamicDiffuseTexture(groupId, null, Object3DGroup.GLTEXTUREID_NONE);
	}

	/**
	 * Unbind dynamic texture to a group and faces entity of this object
	 * @param group id
	 * @param faces entity id
	 */
	public void unbindDiffuseTexture(String groupId, String facesEntityId) {
		setDynamicDiffuseTexture(groupId, facesEntityId, Object3DGroup.GLTEXTUREID_NONE);
	}

	/**
	 * Bind a texture to a group and faces entity
	 * @param group id
	 * @param faces entity id or null if texture should be bound to all faces entities
	 * @param texture id
	 */
	private void setDynamicDiffuseTexture(String groupId, String facesEntityId, int textureId) {
		for(int i = 0; i < object3dGroups.length; i++) {
			Object3DGroup object3DGroup = object3dGroups[i];

			// skip if a group is desired but not matching
			if (groupId != null && groupId.equals(object3DGroup.group.getId()) == false) continue;

			FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities();
			for (int facesEntityIdx = 0; facesEntityIdx < facesEntities.length; facesEntityIdx++) {
				FacesEntity facesEntity = facesEntities[facesEntityIdx];

				// skip if a faces entity is desired but not matching
				if (facesEntityId != null && facesEntityId.equals(facesEntity.getId()) == false) continue;

				// gl texture id, skip if not set up
				object3DGroup.dynamicDiffuseTextureIdsByEntities[facesEntityIdx] = textureId;
			}
		}
	}

	/**
	 * Initiates this object3d 
	 */
	public void initialize() {
		super.initialize();
		// we currently initiate stuff to render this object on demand, means when beeing first rendered
		// could be changed as this takes some serious time on not that fast machines
	}

	/**
	 * Dispose this object3d
	 */
	public void dispose() {
		super.dispose();
		
		// delete textures
		for(int i = 0; i < object3dGroups.length; i++) {
			Object3DGroup object3DGroup = object3dGroups[i];

			// dispose renderer
			object3DGroup.renderer.dispose();
			
			// dispose object3d group
			object3DGroup.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Transformations#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		//
		super.fromTransformations(transformations);

		//
		boundingBoxTransformed.fromBoundingVolumeWithTransformations(boundingBox, this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Transformations#update()
	 */
	public void update() {
		//
		super.update();

		//
		boundingBoxTransformed.fromBoundingVolumeWithTransformations(boundingBox, this);
	}

}

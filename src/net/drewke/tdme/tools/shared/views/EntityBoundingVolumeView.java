package net.drewke.tdme.tools.shared.views;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.controller.EntityBoundingVolumeSubScreenController;
import net.drewke.tdme.tools.shared.controller.ModelViewerScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;

/**
 * Entity bounding volume view
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityBoundingVolumeView {

	private Engine engine;

	private EntityBoundingVolumeSubScreenController modelViewerScreenController;
	private PopUps popUps;

	/**
	 * Public constructor
	 * @param pop ups
	 * @param model viewer screen controller
	 */
	public EntityBoundingVolumeView(EntityBoundingVolumeSubScreenController modelViewerScreenController, PopUps popUps) {
		this.engine = Engine.getInstance();
		this.popUps = popUps;
		this.modelViewerScreenController = modelViewerScreenController;
	}

	/**
	 * @return pop up views
	 */
	public PopUps getPopUpsViews() {
		return popUps;
	}

	/**
	 * Init
	 */
	public void init() {
		// set up bounding volume types
		for (int i = 0; i < EntityBoundingVolumeSubScreenController.MODEL_BOUNDINGVOLUME_COUNT; i++) {
			modelViewerScreenController.setupBoundingVolumeTypes(
				i,
				new String[] {
					"None", "Sphere",
					"Capsule", "Bounding Box",
					"Oriented Bounding Box", "Convex Mesh"
				}
			);
			modelViewerScreenController.selectBoundingVolume(
				i, 
				EntityBoundingVolumeSubScreenController.BoundingVolumeType.NONE
			);
		}
	}

	/**
	 * Reset bounding volume
	 * @param entity
	 * @param idx
	 */
	public void resetBoundingVolume(LevelEditorEntity entity, int idx) {
		// determine AABB
		BoundingBox aabb = null;

		// if we have a model we also have a AABB
		if (entity.getModel() != null) {
			aabb = entity.getModel().getBoundingBox();
		} else {
			// otherwise just create one for now
			// this applies currently for particle systems
			// TODO: check if particle system
			aabb = new BoundingBox(new Vector3(-0.5f, 0f, -0.5f), new Vector3(0.5f, 3f, 0.5f));
		}

		// set up oriented bounding box
		OrientedBoundingBox obb = new OrientedBoundingBox(aabb);

		// set up sphere
		modelViewerScreenController.setupSphere(
			idx,
			obb.getCenter(),
			obb.getHalfExtension().computeLength()
		);

		// set up capsule
		{
			Vector3 a = new Vector3();
			Vector3 b = new Vector3();
			float radius = 0.0f;
			float[] halfExtensionXYZ = obb.getHalfExtension().getArray();

			// determine a, b
			if (halfExtensionXYZ[0] > halfExtensionXYZ[1] &&
				halfExtensionXYZ[0] > halfExtensionXYZ[2]) {
				radius = (float)Math.sqrt(halfExtensionXYZ[1] * halfExtensionXYZ[1] + halfExtensionXYZ[2] * halfExtensionXYZ[2]);
				a.set(obb.getAxes()[0]);
				a.scale(-(halfExtensionXYZ[0] - radius));
				a.add(obb.getCenter());
				b.set(obb.getAxes()[0]);
				b.scale(+(halfExtensionXYZ[0] - radius));
				b.add(obb.getCenter());
			} else
			if (halfExtensionXYZ[1] > halfExtensionXYZ[0] &&
				halfExtensionXYZ[1] > halfExtensionXYZ[2]) {
				radius = (float)Math.sqrt(halfExtensionXYZ[0] * halfExtensionXYZ[0] + halfExtensionXYZ[2] * halfExtensionXYZ[2]);
				a.set(obb.getAxes()[1]);
				a.scale(-(halfExtensionXYZ[1] - radius));
				a.add(obb.getCenter());
				b.set(obb.getAxes()[1]);
				b.scale(+(halfExtensionXYZ[1] - radius));
				b.add(obb.getCenter()); 
			} else {
				radius = (float)Math.sqrt(halfExtensionXYZ[0] * halfExtensionXYZ[0] + halfExtensionXYZ[1] * halfExtensionXYZ[1]);
				a.set(obb.getAxes()[2]);
				a.scale(-(halfExtensionXYZ[2] - radius));
				a.add(obb.getCenter());
				b.set(obb.getAxes()[2]);
				b.scale(+(halfExtensionXYZ[2] - radius));
				b.add(obb.getCenter()); 						
			}

			// setup capsule
			modelViewerScreenController.setupCapsule(idx,a, b, radius);
		}

		// set up AABB bounding box
		modelViewerScreenController.setupBoundingBox(idx, aabb.getMin(), aabb.getMax());

		// set up oriented bounding box
		modelViewerScreenController.setupOrientedBoundingBox(
			idx,
			obb.getCenter(),
			obb.getAxes()[0],
			obb.getAxes()[1],
			obb.getAxes()[2],
			obb.getHalfExtension()
		);

		//
		modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.NONE);
	}

	/**
	 * Set bounding volumes
	 * @param entity
	 */
	public void setBoundingVolumes(LevelEditorEntity entity) {
		// set up default bounding volumes
		for (int i = 0; i < EntityBoundingVolumeSubScreenController.MODEL_BOUNDINGVOLUME_COUNT; i++) {
			resetBoundingVolume(entity, i);
		}

		// set up existing bounding volumes
		for (int i = 0; i < entity.getBoundingVolumeCount(); i++) {
			LevelEditorEntityBoundingVolume bv = entity.getBoundingVolumeAt(i);
			if (bv == null) {
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.NONE);
				continue;
			} else
			if (bv.getBoundingVolume() instanceof Sphere) {
				Sphere sphere = (Sphere)bv.getBoundingVolume();
				modelViewerScreenController.setupSphere(i, sphere.getCenter(), sphere.getRadius());
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.SPHERE);
			} else
			if (bv.getBoundingVolume() instanceof Capsule) {
				Capsule capsule = (Capsule)bv.getBoundingVolume();
				modelViewerScreenController.setupCapsule(i, capsule.getA(), capsule.getB(), capsule.getRadius());
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.CAPSULE);
			} else
			if (bv.getBoundingVolume() instanceof BoundingBox) {
				BoundingBox aabb = (BoundingBox)bv.getBoundingVolume(); 
				modelViewerScreenController.setupBoundingBox(i, aabb.getMin(), aabb.getMax());
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.BOUNDINGBOX);
			} else
			if (bv.getBoundingVolume() instanceof OrientedBoundingBox) {
				OrientedBoundingBox obb = (OrientedBoundingBox)bv.getBoundingVolume(); 
				modelViewerScreenController.setupOrientedBoundingBox(
					i, 
					obb.getCenter(), 
					obb.getAxes()[0], 
					obb.getAxes()[1], 
					obb.getAxes()[2], 
					obb.getHalfExtension()
				);
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
			} else
			if (bv.getBoundingVolume() instanceof ConvexMesh) { 
				modelViewerScreenController.setupConvexMesh(i, bv.getModelMeshFile());
				modelViewerScreenController.selectBoundingVolume(i, EntityBoundingVolumeSubScreenController.BoundingVolumeType.CONVEXMESH);
			}
			// enable bounding volume and set type in GUI
			modelViewerScreenController.enableBoundingVolume(i);
			modelViewerScreenController.setupModelBoundingVolumeType(entity, i); 
		}
	}

	/**
	 * Unset bounding volumes
	 */
	public void unsetBoundingVolumes() {
		for (int i = 0; i < EntityBoundingVolumeSubScreenController.MODEL_BOUNDINGVOLUME_COUNT; i++) {
			modelViewerScreenController.disableBoundingVolume(i);
		}
	}

	/**
	 * Select bounding volume type
	 * @param idx
	 * @param bounding volume type
	 */
	public void selectBoundingVolumeType(int idx, int bvTypeId) {
		switch (bvTypeId) {
			case 0:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.NONE);
				break;
			case 1:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.SPHERE);
				break;
			case 2:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.CAPSULE);
				break;
			case 3:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.BOUNDINGBOX);
				break;
			case 4:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
				break;
			case 5:
				modelViewerScreenController.selectBoundingVolume(idx, EntityBoundingVolumeSubScreenController.BoundingVolumeType.CONVEXMESH);
				break;
		}
	}

	/**
	 * Update model bounding volume
	 * @param entity
	 * @param idx
	 */
	private void updateModelBoundingVolume(LevelEditorEntity entity, int idx) {
		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);

		// remove old bv
		String id = EntityBoundingVolumeSubScreenController.MODEL_BOUNDINGVOLUME_IDS[idx];
		Entity modelBoundingVolumeObject = engine.getEntity(id);
		if (modelBoundingVolumeObject != null) {
			engine.removeEntity(id);
		}

		// add new bv
		if (entityBoundingVolume.getModel() == null) return;
		modelBoundingVolumeObject = new Object3D(id, entityBoundingVolume.getModel());
		modelBoundingVolumeObject.setEnabled(false);
		engine.addEntity(modelBoundingVolumeObject);
	}

	/**
	 * On bounding volume none apply
	 * @param entity
	 * @param bounding volume index
	 */
	public void applyBoundingVolumeNone(LevelEditorEntity entity, int idx) {
		// exit if no entity
		if (entity == null) return;

		//
		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupNone();
		updateModelBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume sphere apply
	 * @param entity
	 * @param bounding volume index
	 * @param sphere center
	 * @param radius
	 */
	public void applyBoundingVolumeSphere(LevelEditorEntity entity, int idx, Vector3 center, float radius) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupSphere(center, radius);
		updateModelBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume capsule apply
	 * @param entity
	 * @param bounding volume index
	 * @param point a
	 * @param point b
	 * @param radius
	 */
	public void applyBoundingVolumeCapsule(LevelEditorEntity entity, int idx, Vector3 a, Vector3 b, float radius) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupCapsule(a, b, radius);
		updateModelBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume AABB apply
	 * @param entity
	 * @param bounding volume index
	 * @param AABB min vector
	 * @param AABB max vector
	 */
	public void applyBoundingVolumeAabb(LevelEditorEntity entity, int idx, Vector3 min, Vector3 max) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupAabb(min, max);
		updateModelBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume OBB apply
	 * @param entity
	 * @param bounding volume index
	 * @param OBB center
	 * @param OBB axis 0
	 * @param OBB axis 1
	 * @param OBB axis 2
	 * @param OBB half extension
	 */
	public void applyBoundingVolumeObb(LevelEditorEntity entity, int idx, Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupObb(center, axis0, axis1, axis2, halfExtension);
		updateModelBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume convex mesh apply
	 * @param entity
	 * @param bounding volume index
	 * @param file
	 */
	public void applyBoundingVolumeConvexMesh(LevelEditorEntity entity, int idx, String file) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupConvexMesh(file);
		updateModelBoundingVolume(entity, idx);
	}

}

package net.drewke.tdme.tools.shared.views;

import java.io.File;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.shared.controller.ModelViewerScreenController;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME Model Viewer View
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelViewerView extends View implements GUIInputEventHandler {

	protected Engine engine;

	private PopUps popUps;
	private ModelViewerScreenController modelViewerScreenController;

	private LevelEditorEntity entity;
	private boolean loadModelRequested;
	private boolean initModelRequested;
	private File modelFile;

	private boolean displayGroundPlate = false;
	private boolean displayShadowing = false;
	private boolean displayBoundingVolume = false;

	private CameraRotationInputHandler cameraRotationInputHandler;

	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public ModelViewerView(PopUps popUps) {
		this.popUps = popUps;
		engine = Engine.getInstance();
		modelViewerScreenController = null;
		loadModelRequested = false;
		initModelRequested = false;
		entity = null;
		modelFile = null;
		cameraRotationInputHandler = new CameraRotationInputHandler(engine);
	}

	/**
	 * @return pop up views
	 */
	public PopUps getPopUpsViews() {
		return popUps;
	}

	/**
	 * @return display ground plate
	 */
	public boolean isDisplayGroundPlate() {
		return displayGroundPlate;
	}

	/**
	 * Set up ground plate visibility
	 * @param ground plate visible
	 */
	public void setDisplayGroundPlate(boolean groundPlate) {
		this.displayGroundPlate = groundPlate;
	}

	/**
	 * @return display shadowing
	 */
	public boolean isDisplayShadowing() {
		return displayShadowing;
	}

	/**
	 * Set up shadow rendering
	 * @param shadow rendering
	 */
	public void setDisplayShadowing(boolean shadowing) {
		this.displayShadowing = shadowing;
	}

	/**
	 * @return display bounding volume
	 */
	public boolean isDisplayBoundingVolume() {
		return displayBoundingVolume;
	}

	/**
	 * Set up bounding volume visibility
	 * @param bounding volume
	 */
	public void setDisplayBoundingVolume(boolean displayBoundingVolume) {
		this.displayBoundingVolume = displayBoundingVolume;
	}

	/**
	 * @return entity
	 */
	public LevelEditorEntity getEntity() {
		return entity;
	}

	/**
	 * Set entity
	 */
	public void setEntity(LevelEditorEntity entity) {
		this.entity = entity;
		initModelRequested = true;
	}

	/**
	 * Init model
	 */
	protected void initModel(GLAutoDrawable drawable) {
		if (entity == null) return;

		//
		modelFile = new File(entity.getFileName());

		// set up model in engine
		Tools.setupModel(entity, engine, cameraRotationInputHandler.getLookFromRotations(), cameraRotationInputHandler.getScale());

		// Make model screenshot
		Tools.oseThumbnail(drawable, entity);

		// max axis dimension
		cameraRotationInputHandler.setMaxAxisDimension(Tools.computeMaxAxisDimension(entity.getModel().getBoundingBox()));

		// set up model statistics
		ModelUtilities.ModelStatistics stats = ModelUtilities.computeModelStatistics(entity.getModel());
		modelViewerScreenController.setStatistics(stats.getOpaqueFaceCount(), stats.getTransparentFaceCount(), stats.getMaterialCount());

		// set up oriented bounding box
		BoundingBox aabb = entity.getModel().getBoundingBox();
		OrientedBoundingBox obb = new OrientedBoundingBox(aabb);

		// iterate entity bounding volumes
		for (int i = 0; i < 8; i++) {
			// set up sphere
			modelViewerScreenController.setupSphere(
				i,
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
				modelViewerScreenController.setupCapsule(i,a, b, radius);
			}
	
			// set up AABB bounding box
			modelViewerScreenController.setupBoundingBox(i, aabb.getMin(), aabb.getMax());
	
			// set up oriented bounding box
			modelViewerScreenController.setupOrientedBoundingBox(
				i,
				obb.getCenter(),
				obb.getAxes()[0],
				obb.getAxes()[1],
				obb.getAxes()[2],
				obb.getHalfExtension()
			);
		}

		// 
		updateGUIElements();
	}

	/**
	 * @return current model file name
	 */
	public String getFileName() {
		if (modelFile == null) return "";
		return modelFile.getName();
	}

	/**
	 * Issue file loading
	 */
	public void loadFile(String pathName, String fileName) {
		loadModelRequested = true;
		modelFile = new File(pathName, fileName);
	}

	/**
	 * Triggers saving a map
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		ModelMetaDataFileExport.export(new File(pathName, fileName).getCanonicalPath(), entity);
	}


	/**
	 * Issue file reloading
	 */
	public void reloadFile() {
		loadModelRequested = true;
	}

	/**
	 * Apply pivot
	 * @param x
	 * @param y
	 * @param z
	 */
	public void pivotApply(float x, float y, float z) {
		if (entity == null) return;
		entity.getPivot().set(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIInputEventHandler#handleInputEvents()
	 */
	public void handleInputEvents() {
		cameraRotationInputHandler.handleInputEvents();
	}

	/**
	 * Renders the scene 
	 */
	public void display(GLAutoDrawable drawable) {
		// load model
		if (loadModelRequested == true) {
			initModelRequested = true;
			loadModelRequested = false;
			engine.reset();
			loadModel();
			cameraRotationInputHandler.reset();
		}

		// init model
		if (initModelRequested == true) {
			initModel(drawable);
			initModelRequested = false;
		}

		// apply settings from gui
		if (entity != null) {
			Entity model = engine.getEntity("model");
			Entity ground = engine.getEntity("ground");
			model.setDynamicShadowingEnabled(displayShadowing);
			ground.setEnabled(displayGroundPlate);
			Entity modelBoundingVolume = engine.getEntity("model_bv");
			if (modelBoundingVolume != null) {
				modelBoundingVolume.setEnabled(displayBoundingVolume);
			}
		}

		// do GUI
		engine.getGUI().render();
		engine.getGUI().handleEvents();
	}

	/**
	 * Init GUI elements
	 */
	public void updateGUIElements() {
		if (entity != null) {
			modelViewerScreenController.setScreenCaption("Model Viewer - " + entity.getName());
			PropertyModelClass preset = entity.getProperty("preset");
			modelViewerScreenController.setEntityProperties(preset != null ? preset.getValue() : null, entity.getProperties(), null);
			modelViewerScreenController.setEntityData(entity.getName(), entity.getDescription());
			modelViewerScreenController.setPivot(entity.getPivot());
			for (int i = 0; i < entity.getBoundingVolumeCount(); i++) {
				modelViewerScreenController.setBoundingVolume(i);
				modelViewerScreenController.setupModelBoundingVolume(i);	
			}
			for (int i = entity.getBoundingVolumeCount(); i < 8; i++) {
				modelViewerScreenController.unsetBoundingVolume(i);
			}
		} else {
			modelViewerScreenController.setScreenCaption("Model Viewer - no entity loaded");
			modelViewerScreenController.unsetEntityProperties();
			modelViewerScreenController.unsetEntityData();
			modelViewerScreenController.unsetPivot();
			for (int i = 0; i < 8; i++) {
				modelViewerScreenController.unsetBoundingVolume(i);
			}
		}
	}

	/**
	 * Shutdown
	 */
	public void dispose(GLAutoDrawable drawable) {
		Engine.getInstance().reset();
	}

	/**
	 * On init additional screens
	 * @param drawable
	 */
	public void onInitAdditionalScreens() {
	}

	/**
	 * Initialize
	 */
	public void init(GLAutoDrawable drawable) {
		// reset engine and partition
		engine.reset();
		engine.setPartition(new PartitionNone());

		//
		try {
			modelViewerScreenController = new ModelViewerScreenController(this);
			modelViewerScreenController.init();
			engine.getGUI().addScreen(modelViewerScreenController.getScreenNode().getId(), modelViewerScreenController.getScreenNode());
			modelViewerScreenController.getScreenNode().setInputEventHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up bounding volume types
		for (int i = 0; i < 8; i++) {
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
				ModelViewerScreenController.BoundingVolumeType.NONE
			);
		}

		// set up gui
		updateGUIElements();

		//
		engine.getGUI().resetRenderScreens();
		engine.getGUI().addRenderScreen(modelViewerScreenController.getScreenNode().getId());
		onInitAdditionalScreens();
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());
	}

	/**
	 * On load model
	 * @param oldModel
	 * @oaram entity
	 */
	public void onLoadModel(LevelEditorEntity oldModel, LevelEditorEntity model) {
	}

	/**
	 * Load a model
	 */
	private void loadModel() {
		//
		System.out.println("Model file: " + modelFile);

		// scene
		try {
			LevelEditorEntity oldModel = entity;

			// add entity to library
			entity = loadModel(
				modelFile.getName(),
				"",
				modelFile.getParentFile().getAbsolutePath(),
				modelFile.getName(),
				new Vector3()
			);
			onLoadModel(oldModel, entity);
		} catch (Exception exception) {
			popUps.getInfoDialogScreenController().show("Warning", exception.getMessage());
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
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.NONE);
				break;
			case 1:
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.SPHERE);
				break;
			case 2:
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.CAPSULE);
				break;
			case 3:
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.BOUNDINGBOX);
				break;
			case 4:
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
				break;
			case 5:
				modelViewerScreenController.selectBoundingVolume(idx, ModelViewerScreenController.BoundingVolumeType.CONVEXMESH);
				break;
		}
	}

	/**
	 * Load model
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @param pivot
	 * @return level editor entity
	 * @throws Exception
	 */
	protected LevelEditorEntity loadModel(String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		if (fileName.toLowerCase().endsWith(".dae")) {
			Model model = DAEReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
			LevelEditorEntity levelEditorEntity = new LevelEditorEntity(
				LevelEditorEntity.ID_NONE,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				pivot
			);
			levelEditorEntity.setDefaultBoundingVolumes();
			return levelEditorEntity;
		} else
		if (fileName.toLowerCase().endsWith(".tm")) {
			Model model = TMReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
			LevelEditorEntity levelEditorEntity = new LevelEditorEntity(
				LevelEditorEntity.ID_NONE,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				pivot
			);
			levelEditorEntity.setDefaultBoundingVolumes();
			return levelEditorEntity;
		} else
		if (fileName.toLowerCase().endsWith(".tmm")) {
			LevelEditorEntity levelEditorEntity = ModelMetaDataFileImport.doImport(
				LevelEditorEntity.ID_NONE, 
				pathName, 
				fileName
			);
			levelEditorEntity.setDefaultBoundingVolumes();
			return levelEditorEntity;
		}
		return null;
	}

	/**
	 * Update model bounding volume
	 * @param entity
	 */
	private void updateModelBoundingVolume(int idx) {
		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);

		// remove old bv
		String id = "model_bv." + idx;
		Entity modelBoundingVolumeObject = engine.getEntity(id);
		if (modelBoundingVolumeObject != null) {
			engine.removeEntity(id);
		}

		// add new bv
		if (entityBoundingVolume.getModel() == null) return;
		modelBoundingVolumeObject = new Object3D(id, entityBoundingVolume.getModel());
		modelBoundingVolumeObject.setEnabled(displayBoundingVolume);
		engine.addEntity(modelBoundingVolumeObject);
	}

	/**
	 * On bounding volume none apply
	 * @param bounding volume index
	 */
	public void applyBoundingVolumeNone(int idx) {
		// exit if no entity
		if (entity == null) return;

		//
		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupNone();
		updateModelBoundingVolume(idx);
	}

	/**
	 * On bounding volume sphere apply
	 * @param bounding volume index
	 * @param sphere center
	 * @param radius
	 */
	public void applyBoundingVolumeSphere(int idx, Vector3 center, float radius) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupSphere(center, radius);
		updateModelBoundingVolume(idx);
	}

	/**
	 * On bounding volume capsule apply
	 * @param bounding volume index
	 * @param point a
	 * @param point b
	 * @param radius
	 */
	public void applyBoundingVolumeCapsule(int idx, Vector3 a, Vector3 b, float radius) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupCapsule(a, b, radius);
		updateModelBoundingVolume(idx);
	}

	/**
	 * On bounding volume AABB apply
	 * @param bounding volume index
	 * @param AABB min vector
	 * @param AABB max vector
	 */
	public void applyBoundingVolumeAabb(int idx, Vector3 min, Vector3 max) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupAabb(min, max);
		updateModelBoundingVolume(idx);
	}

	/**
	 * On bounding volume OBB apply
	 * @param bounding volume index
	 * @param OBB center
	 * @param OBB axis 0
	 * @param OBB axis 1
	 * @param OBB axis 2
	 * @param OBB half extension
	 */
	public void applyBoundingVolumeObb(int idx, Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupObb(center, axis0, axis1, axis2, halfExtension);
		updateModelBoundingVolume(idx);
	}

	/**
	 * On bounding volume convex mesh apply
	 * @param bounding volume index
	 * @param file
	 */
	public void applyBoundingVolumeConvexMesh(int idx, String file) {
		// exit if no entity
		if (entity == null) return;

		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		entityBoundingVolume.setupConvexMesh(file);
		updateModelBoundingVolume(idx);
	}

	/**
	  * On set entity data hook
	  */
	public void onSetEntityData() {
	}

}
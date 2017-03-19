package net.drewke.tdme.tools.shared.views;

import java.io.File;
import java.util.ArrayList;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.PartitionQuadTree;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIKeyboardEvent.Type;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.controller.ModelViewerScreenController;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
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

	private LevelEditorModel model;
	private boolean loadModelRequested;
	private boolean initModelRequested;
	private File modelFile;

	private float maxAxisDimension;
	
	Transformations lookFromRotations;
	float scale;

	private boolean mouseDragging;
	private boolean keyLeft;
	private boolean keyRight;
	private boolean keyUp;
	private boolean keyDown;
	private boolean keyA;
	private boolean keyD;
	private boolean keyW;
	private boolean keyS;
	private boolean keyPeriod;
	private boolean keyComma;
	private boolean keyPlus;
	private boolean keyMinus;
	private boolean keyR;

	private int mouseLastX = 0;
	private int mouseLastY = 0;

	private boolean displayGroundPlate = false;
	private boolean displayShadowing = false;
	private boolean displayBoundingVolume = false;

	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public ModelViewerView(PopUps popUps) {
		this.popUps = popUps;
		modelViewerScreenController = null;
		loadModelRequested = false;
		initModelRequested = false;
		model = null;
		modelFile = null;
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyDown = false;
		keyA = false;
		keyD = false;
		keyW = false;
		keyS = false;
		keyPlus = false;
		keyMinus = false;
		keyR = false;
		mouseDragging = false;

		lookFromRotations = new Transformations();
		maxAxisDimension = 0.0f;
		scale = 1.0f;
		// rotation x
		lookFromRotations.getRotations().add(new Rotation(-45f, new Vector3(0f, 1f, 0f)));
		// rotation y
		lookFromRotations.getRotations().add(new Rotation(-45f, new Vector3(1f, 0f, 0f)));
		// rotation z
		lookFromRotations.getRotations().add(new Rotation(0f, new Vector3(0f, 0f, 1f)));
		// update
		lookFromRotations.update();

		// offscreen engine transformations
		engine = Engine.getInstance();
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
	 * @return model
	 */
	public LevelEditorModel getModel() {
		return model;
	}

	/**
	 * @return selected model
	 */
	public void setModel(LevelEditorModel model) {
		this.model = model;
		initModelRequested = true;
	}

	/**
	 * Init model
	 */
	protected void initModel(GLAutoDrawable drawable) {
		if (model == null) return;

		//
		modelFile = new File(model.getFileName());

		// set up model in engine
		Tools.setupModel(model, engine, lookFromRotations, scale);

		// Make model screenshot
		Tools.oseThumbnail(drawable, model);

		// add model
		maxAxisDimension = Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel()));

		// set up model statistics
		ModelUtilities.ModelStatistics stats = ModelUtilities.computeModelStatistics(model.getModel());
		modelViewerScreenController.setStatistics(stats.getOpaqueFaceCount(), stats.getTransparentFaceCount(), stats.getMaterialCount());

		// set up oriented bounding box
		BoundingBox aabb = Engine.getModelBoundingBox(model.getModel());
		OrientedBoundingBox obb = new OrientedBoundingBox(aabb);

		// set up sphere
		modelViewerScreenController.setupSphere(
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
			modelViewerScreenController.setupCapsule(a, b, radius);
		}

		// set up AABB bounding box
		modelViewerScreenController.setupBoundingBox(aabb.getMin(), aabb.getMax());

		// set up oriented bounding box
		modelViewerScreenController.setupOrientedBoundingBox(
			obb.getCenter(),
			obb.getAxes()[0],
			obb.getAxes()[1],
			obb.getAxes()[2],
			obb.getHalfExtension()
		);

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
		ModelMetaDataFileExport.export(new File(pathName, fileName).getCanonicalPath(), model);
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
		if (model == null) return;
		model.getPivot().set(x, y, z);
	}

	/**
	 * Apply model property preset
	 * @param preset id
	 */
	public void modelPropertiesPreset(String presetId) {
		if (model == null) return;

		// clear model properties
		model.clearProperties();

		// add model properties by preset if missing
		ArrayList<PropertyModelClass> modelPropertyPresetVector = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (modelPropertyPresetVector != null) {
			for (PropertyModelClass modelPropertyPreset: modelPropertyPresetVector) {
				model.addProperty(modelPropertyPreset.getName(), modelPropertyPreset.getValue());
			}
		}

		// update model properties to gui
		modelViewerScreenController.setModelProperties(
			presetId,
			model.getProperties(),
			null
		);
	}

	/**
	 * Save a model property
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean modelPropertySave(String oldName, String name, String value) {
		if (model == null) return false;

		// try to update property
		if (model.updateProperty(oldName, name, value) == true) {
			// reload model properties
			modelViewerScreenController.setModelProperties(
				null,
				model.getProperties(),
				name
			);

			// 
			return true;
		}

		//
		return false;
	}

	/**
	 * Add a model property
	 * @return success
	 */
	public boolean modelPropertyAdd() {
		if (model == null) return false;

		// try to add property
		if (model.addProperty("new.property", "new.value")) {
			// reload model properties
			modelViewerScreenController.setModelProperties(
				null,
				model.getProperties(),
				"new.property"
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Remove a model property from model properties
	 * @param name
	 * @return success
	 */
	public boolean modelPropertyRemove(String name) {
		if (model == null) return false;

		// try to remove property
		int idx = model.getPropertyIndex(name);
		if (idx != -1 && model.removeProperty(name) == true) {
			// get property first at index that was removed 
			PropertyModelClass property = model.getPropertyByIndex(idx);
			if (property == null) {
				// if current index does not work, take current one -1
				property = model.getPropertyByIndex(idx - 1);
			}

			// reload model properties
			modelViewerScreenController.setModelProperties(
				null,
				model.getProperties(),
				property == null?null:property.getName()
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * On set model data hook
	 */
	public void onSetModelData() {
	}

	/**
	 * Update current model data
	 * @param name
	 * @param description
	 */
	public void setModelData(String name, String description) {
		if (model == null) return;
		model.setName(name);
		model.setDescription(description);
		onSetModelData();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIInputEventHandler#handleInputEvents()
	 */
	public void handleInputEvents() {
		// handle mouse events
		for (int i = 0; i < engine.getGUI().getMouseEvents().size(); i++) {
			GUIMouseEvent event = engine.getGUI().getMouseEvents().get(i);

			// skip on processed events
			if (event.isProcessed() == true) continue;

			// dragging
			if (mouseDragging == true) {	
				if (event.getButton() == 1) {
					float xMoved = (event.getX() - mouseLastX) / 5f;
					float yMoved = (event.getY() - mouseLastY) / 5f;
					mouseLastX = event.getX();
					mouseLastY = event.getY();
					Rotation xRotation = lookFromRotations.getRotations().get(0);
					Rotation yRotation = lookFromRotations.getRotations().get(1);
					float xRotationAngle = xRotation.getAngle() + xMoved;
					float yRotationAngle = yRotation.getAngle() + yMoved;
					xRotation.setAngle(xRotationAngle);
					yRotation.setAngle(yRotationAngle);
					lookFromRotations.update();					
				} else {
					mouseDragging = false;
				}
			} else {
				if (event.getButton() == 1) {
					mouseDragging = true;
					mouseLastX = event.getX();
					mouseLastY = event.getY();					
				}
			}

			// process mouse wheel events
			float mouseWheel = event.getWheelY();
			if (mouseWheel != 0) {
				scale+= mouseWheel * 0.05f;
				if (scale < 0.05f) scale = 0.05f;
			}
		}

		// handle keyboard events
		for (int i = 0; i < engine.getGUI().getKeyboardEvents().size(); i++) {
			GUIKeyboardEvent event = engine.getGUI().getKeyboardEvents().get(i);

			// skip on processed events
			if (event.isProcessed() == true) continue;

			//
			boolean isKeyDown = event.getType() == Type.KEY_PRESSED;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_LEFT) keyLeft = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_RIGHT) keyRight = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_UP) keyUp = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_DOWN) keyDown = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'a') keyA = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'd') keyD = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'w') keyW = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 's') keyS = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == '.') keyPeriod = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == ',') keyComma = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == '+') keyPlus = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == '-') keyMinus = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'r') keyR = isKeyDown;
		}
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
		}

		// init model
		if (initModelRequested == true) {
			initModel(drawable);
		}

		// handle keyboard input
		//	get rotations
		Rotation rotationX = lookFromRotations.getRotations().get(0);
		Rotation rotationY = lookFromRotations.getRotations().get(1);
		Rotation rotationZ = lookFromRotations.getRotations().get(2);

		// 	transfer keyboard inputs to rotations
		if (keyLeft) rotationX.setAngle(rotationX.getAngle() - 1f);
		if (keyRight) rotationX.setAngle(rotationX.getAngle() + 1f);
		if (keyUp) rotationY.setAngle(rotationY.getAngle() + 1f);
		if (keyDown) rotationY.setAngle(rotationY.getAngle() - 1f);
		if (keyComma) rotationZ.setAngle(rotationZ.getAngle() - 1f);
		if (keyPeriod) rotationZ.setAngle(rotationZ.getAngle() + 1f);
		if (keyMinus) scale+= 0.05f;
		if (keyPlus && scale > 0.05f) scale-= 0.05f;
		if (keyR == true || initModelRequested == true) {
			rotationY.setAngle(-45f);
			rotationZ.setAngle(0f);
			scale = 1.0f;
			initModelRequested = false;
		}

		// 	update transformations if key was pressed
		if (keyLeft || keyRight || keyUp || keyDown || keyComma || keyPeriod || keyR) {
			lookFromRotations.update();
		}

		// set up cam
		Camera cam = engine.getCamera();

		// we have a fixed look at
		Vector3 lookAt = cam.getLookAt();

		// look at -> look to vector
		Vector3 lookAtToFromVector =
			new Vector3(
				0f,
				0f,
				+(maxAxisDimension * 1.2f)
			);

		// apply look from rotations
		// apply look from rotations
		Vector3 lookAtToFromVectorTransformed = new Vector3();
		Vector3 lookAtToFromVectorScaled = new Vector3();
		Vector3 upVector = new Vector3();
		lookFromRotations.getTransformationsMatrix().multiply(lookAtToFromVector, lookAtToFromVectorTransformed);
		lookAtToFromVectorScaled.set(lookAtToFromVectorTransformed).scale(scale);
		lookFromRotations.getRotations().get(2).getQuaternion().multiply(new Vector3(0f,1f,0f), upVector);

		/*
		Vector3 forwardVector = lookAtToFromVectorTransformed.clone().scale(-1f);
		Vector3 sideVector = Vector3.computeCrossProduct(forwardVector, upVector);

		if (keyA) camLookAt.sub(sideVector.clone().scale(0.05f));
		if (keyD) camLookAt.add(sideVector.clone().scale(0.05f));
		if (keyW) camLookAt.add(upVector.clone().scale(0.05f * forwardVector.computeLength()));
		if (keyS) camLookAt.sub(upVector.clone().scale(0.05f * forwardVector.computeLength()));
		*/

		// look from with rotations
		Vector3 lookFrom = lookAt.clone().add(lookAtToFromVectorScaled);
		cam.getLookFrom().set(lookFrom);

		// up vector
		cam.getUpVector().set(upVector);

		// apply settings from gui
		if (model != null) {
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
	private void updateGUIElements() {
		if (model != null) {
			modelViewerScreenController.setScreenCaption("Model Viewer - " + model.getModel().getName());
			PropertyModelClass preset = model.getProperty("preset");
			modelViewerScreenController.setModelProperties(preset != null?preset.getValue():null, model.getProperties(), null);
			modelViewerScreenController.setModelData(model.getName(), model.getDescription());
			modelViewerScreenController.setPivot(model.getPivot());
			modelViewerScreenController.setBoundingVolume();
			modelViewerScreenController.setupModelBoundingVolume();
		} else {
			modelViewerScreenController.setScreenCaption("Model Viewer - no model loaded");
			modelViewerScreenController.unsetModelProperties();
			modelViewerScreenController.unsetModelData();
			modelViewerScreenController.unsetPivot();
			modelViewerScreenController.unsetBoundingVolume();
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

		// set up model properties presets
		modelViewerScreenController.setModelPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());

		// set up bounding volume types
		modelViewerScreenController.setupBoundingVolumeTypes(
			new String[] {
				"None", "Sphere",
				"Capsule", "Bounding Box",
				"Oriented Bounding Box", "Convex Mesh"
			}
		);
		modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.NONE);

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
	 * @oaram model
	 */
	public void onLoadModel(LevelEditorModel oldModel, LevelEditorModel model) {
	}

	/**
	 * Load a model
	 */
	private void loadModel() {
		//
		System.out.println("Model file: " + modelFile);

		// scene
		try {
			LevelEditorModel oldModel = model;

			// add model to library
			model = loadModel(
				modelFile.getName(),
				"",
				modelFile.getParentFile().getAbsolutePath(),
				modelFile.getName(),
				new Vector3()
			);
			onLoadModel(oldModel, model);
		} catch (Exception exception) {
			popUps.getInfoDialogScreenController().show("Warning", exception.getMessage());
		}
	}

	/**
	 * Select bounding volume type
	 * @param bounding volume type
	 */
	public void selectBoundingVolumeType(int bvTypeId) {
		switch (bvTypeId) {
			case 0:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.NONE);
				break;
			case 1:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.SPHERE);
				break;
			case 2:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.CAPSULE);
				break;
			case 3:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.BOUNDINGBOX);
				break;
			case 4:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
				break;
			case 5:
				modelViewerScreenController.selectBoundingVolume(ModelViewerScreenController.BoundingVolumeType.CONVEXMESH);
				break;
		}
	}

	/**
	 * Load model method
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @param pivot
	 * @return level editor model
	 * @throws Exception
	 */
	protected LevelEditorModel loadModel(String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		if (fileName.toLowerCase().endsWith(".dae")) {
			Model model = DAEReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
			Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
			LevelEditorModel levelEditorModel = new LevelEditorModel(
				LevelEditorModel.ID_NONE,
				LevelEditorModel.ModelType.MODEL,
				name,
				description,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				null,
				modelBoundingVolume,
				boundingBox,
				pivot
			);
			return levelEditorModel;
		} else
		if (fileName.toLowerCase().endsWith(".tm")) {
			Model model = TMReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
			Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
			LevelEditorModel levelEditorModel = new LevelEditorModel(
				LevelEditorModel.ID_NONE,
				LevelEditorModel.ModelType.MODEL,
				name,
				description,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				null,
				modelBoundingVolume,
				boundingBox,
				pivot
			);
			return levelEditorModel;
		} else
		if (fileName.toLowerCase().endsWith(".tmm")) {
			LevelEditorModel levelEditorModel = ModelMetaDataFileImport.doImport(LevelEditorModel.ID_NONE, pathName, fileName);
			return levelEditorModel;
		}
		return null;
	}

	/**
	 * Update model bounding volume
	 * @param model
	 */
	private void updateModelBoundingVolume(LevelEditorModel model) {
		// remove old bv
		Entity modelBoundingVolumeObject = engine.getEntity("model_bv");
		if (modelBoundingVolumeObject != null) {
			engine.removeEntity("model_bv");
		}

		// add new bv
		if (model.getModelBoundingVolume() == null) return;
		modelBoundingVolumeObject = new Object3D("model_bv", model.getModelBoundingVolume());
		modelBoundingVolumeObject.setEnabled(displayBoundingVolume);
		engine.addEntity(modelBoundingVolumeObject);
	}

	/**
	 * On bounding volume none apply
	 */
	public void applyBoundingVolumeNone() {
		if (model == null) return;

		model.setupBoundingVolumeNone();
		updateModelBoundingVolume(model);
	}

	/**
	 * On bounding volume sphere apply
	 */
	public void applyBoundingVolumeSphere(Vector3 center, float radius) {
		if (model == null) return;

		model.setupBoundingVolumeSphere(center, radius);
		updateModelBoundingVolume(model);
	}

	/**
	 * On bounding volume capsule apply
	 */
	public void applyBoundingVolumeCapsule(Vector3 a, Vector3 b, float radius) {
		if (model == null) return;

		model.setupBoundingVolumeCapsule(a, b, radius);
		updateModelBoundingVolume(model);
	}

	/**
	 * On bounding volume AABB apply
	 */
	public void applyBoundingVolumeAabb(Vector3 min, Vector3 max) {
		if (model == null) return;

		model.setupBoundingVolumeAabb(min, max);
		updateModelBoundingVolume(model);
	}

	/**
	 * On bounding volume OBB apply
	 */
	public void applyBoundingVolumeObb(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		if (model == null) return;

		model.setupBoundingVolumeObb(center, axis0, axis1, axis2, halfExtension);
		updateModelBoundingVolume(model);
	}

	/**
	 * On bounding volume convex mesh apply
	 */
	public void applyBoundingVolumeConvexMesh(String file) {
		if (model == null) return;

		model.setupBoundingVolumeConvexMesh(file);
		updateModelBoundingVolume(model);
	}

}
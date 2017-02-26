package net.drewke.tdme.tools.viewer.views;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.gui.events.GUIInputEventsProcessor;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIKeyboardEvent.Type;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.viewer.Tools;
import net.drewke.tdme.tools.viewer.controller.FileDialogPopUpController;
import net.drewke.tdme.tools.viewer.controller.InfoDialogPopUpController;
import net.drewke.tdme.tools.viewer.controller.ModelLibraryController;
import net.drewke.tdme.tools.viewer.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.viewer.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.viewer.model.LevelEditorModel;
import net.drewke.tdme.tools.viewer.model.LevelPropertyPresets;
import net.drewke.tdme.tools.viewer.model.PropertyModelClass;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME Model Library
 * @author andreas.drewke
 * @version $Id: 04313d20d0978eefc881024d6e0af748196c1425 $
 */
public final class ModelLibraryView extends View implements GUIInputEventsProcessor {

	private Engine engine;

	private InfoDialogPopUpController infoDialogPopUpController;
	private FileDialogPopUpController fileDialogPopUpController;
	private ModelLibraryController modelLibraryController;

	private LevelEditorModel model;
	private boolean loadModelRequested;
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
	 */
	public ModelLibraryView() {
		modelLibraryController = null;
		loadModelRequested = false;
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
	 * @return file dialog popup controller
	 */
	public FileDialogPopUpController getFileDialogPopUpController() {
		return fileDialogPopUpController;
	}

	/**
	 * @return info dialog popup controller
	 */
	public InfoDialogPopUpController getInfoDialogPopUpController() {
		return infoDialogPopUpController;
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
	 * @return selected model
	 */
	public LevelEditorModel getSelectedModel() {
		return model;
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
	 * Apply object property preset
	 * @param preset id
	 */
	public void objectPropertiesPreset(String presetId) {
		if (model == null) return;

		// clear object properties
		model.clearProperties();

		// add object properties by preset if missing
		ArrayList<PropertyModelClass> objectPropertyPresetVector = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (objectPropertyPresetVector != null) {
			for (PropertyModelClass objectPropertyPreset: objectPropertyPresetVector) {
				model.addProperty(objectPropertyPreset.getName(), objectPropertyPreset.getValue());
			}
		}

		// update object properties to gui
		modelLibraryController.setObjectProperties(
			presetId,
			model.getProperties(),
			null
		);
	}

	/**
	 * Save a object property
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean objectPropertySave(String oldName, String name, String value) {
		if (model == null) return false;

		// try to update property
		if (model.updateProperty(oldName, name, value) == true) {
			// reload object properties
			modelLibraryController.setObjectProperties(
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
	 * Add a object property
	 * @return success
	 */
	public boolean objectPropertyAdd() {
		if (model == null) return false;

		// try to add property
		if (model.addProperty("new.property", "new.value")) {
			// reload object properties
			modelLibraryController.setObjectProperties(
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
	 * Remove a object property from object
	 * @param name
	 * @return success
	 */
	public boolean objectPropertyRemove(String name) {
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

			// reload object properties
			modelLibraryController.setObjectProperties(
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
	 * Update current model data
	 * @param name
	 * @param description
	 */
	public void setModelData(String name, String description) {
		if (model == null) return;
		model.setName(name);
		model.setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIInputEventsProcessor#processInputEvents()
	 */
	public void processInputEvents() {
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
		if (loadModelRequested == true) {
			model = null;
			engine.reset();
			loadModel();
			if (model != null) {
				// set up model in engine
				Tools.setupModel(model, engine, lookFromRotations, scale);

				// Make model screenshot
				Tools.oseThumbnail(drawable, model);

				// add model
				maxAxisDimension = Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel()));

				// set up model statistics
				ModelUtilities.ModelStatistics stats = ModelUtilities.computeModelStatistics(model.getModel());
				modelLibraryController.setStatistics(stats.getOpaqueFaceCount(), stats.getTransparentFaceCount(), stats.getMaterialCount());

				// set up oriented bounding box
				BoundingBox aabb = Engine.getModelBoundingBox(model.getModel());
				OrientedBoundingBox obb = new OrientedBoundingBox(aabb);

				// set up sphere
				modelLibraryController.setupSphere(
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
					modelLibraryController.setupCapsule(a, b, radius);
				}

				// set up AABB bounding box
				modelLibraryController.setupBoundingBox(aabb.getMin(), aabb.getMax());

				// set up oriented bounding box
				modelLibraryController.setupOrientedBoundingBox(
					obb.getCenter(),
					obb.getAxes()[0],
					obb.getAxes()[1],
					obb.getAxes()[2],
					obb.getHalfExtension()
				);
			}

			//
			updateGUIElements(drawable);
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
		if (keyR) {
			rotationX.setAngle(-45f);
			rotationY.setAngle(-45f);
			rotationZ.setAngle(0f);
			scale = 1.0f;
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

		// Render screens and handle input
		String activeId = modelLibraryController.getScreenNode().getId();
		engine.getGUI().render(modelLibraryController.getScreenNode().getId());
		if (fileDialogPopUpController.isActive() == true) {
			engine.getGUI().render(fileDialogPopUpController.getScreenNode().getId());
			activeId = fileDialogPopUpController.getScreenNode().getId();
		}
		if (infoDialogPopUpController.isActive() == true) {
			engine.getGUI().render(infoDialogPopUpController.getScreenNode().getId());
			activeId = infoDialogPopUpController.getScreenNode().getId();
		}
		engine.getGUI().handleEvents(activeId, this);
	}

	/**
	 * Init GUI elements
	 */
	private void updateGUIElements(GLAutoDrawable drawable) {
		if (model != null) {
			modelLibraryController.setScreenCaption("Model Library - " + model.getModel().getName());
			PropertyModelClass preset = model.getProperty("preset");
			modelLibraryController.setObjectProperties(preset != null?preset.getValue():null, model.getProperties(), null);
			modelLibraryController.setModelData(model.getName(), model.getDescription());
			modelLibraryController.setPivot(model.getPivot());
			modelLibraryController.setBoundingVolume();
			modelLibraryController.setupModelBoundingVolume();
		} else {
			modelLibraryController.setScreenCaption("Model Library - no model loaded");
			modelLibraryController.unsetObjectProperties();
			modelLibraryController.unsetModelData();
			modelLibraryController.unsetPivot();
			modelLibraryController.unsetBoundingVolume();
		}
	}

	/**
	 * Shutdown
	 */
	public void dispose(GLAutoDrawable drawable) {
		Engine.getInstance().reset();
	}

	/**
	 * Initialize
	 */
	public void init(GLAutoDrawable drawable) {
		try {
			modelLibraryController = new ModelLibraryController();
			modelLibraryController.init();
			fileDialogPopUpController = new FileDialogPopUpController(modelLibraryController);
			fileDialogPopUpController.init();
			infoDialogPopUpController = new InfoDialogPopUpController(modelLibraryController);
			infoDialogPopUpController.init();
			engine.getGUI().addScreen(modelLibraryController.getScreenNode().getId(), modelLibraryController.getScreenNode()); 
			engine.getGUI().addScreen(fileDialogPopUpController.getScreenNode().getId(), fileDialogPopUpController.getScreenNode());
			engine.getGUI().addScreen(infoDialogPopUpController.getScreenNode().getId(), infoDialogPopUpController.getScreenNode());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up object properties presets
		modelLibraryController.setObjectPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());

		// set up bounding volume types
		modelLibraryController.setupBoundingVolumeTypes(
			new String[] {
				"None", "Sphere",
				"Capsule", "Bounding Box",
				"Oriented Bounding Box", "Convex Mesh"
			}
		);
		modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.NONE);

		// set up gui
		updateGUIElements(drawable);
	}

	/**
	 * Load a model
	 */
	private void loadModel() {
		//
		System.out.println("Model file: " + modelFile);

		// scene
		try {
			// add model to library
			model = loadModel(
				modelFile.getName(),
				"",
				modelFile.getParentFile().getAbsolutePath(),
				modelFile.getName(),
				new Vector3()
			);
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(null, "Could not load object: " + exception.getMessage());
			exception.printStackTrace();
		}

		//
		loadModelRequested = false;
	}

	/**
	 * Select bounding volume type
	 * @param bounding volume type
	 */
	public void selectBoundingVolumeType(int bvTypeId) {
		switch (bvTypeId) {
			case 0:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.NONE);
				break;
			case 1:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.SPHERE);
				break;
			case 2:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.CAPSULE);
				break;
			case 3:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.BOUNDINGBOX);
				break;
			case 4:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
				break;
			case 5:
				modelLibraryController.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.CONVEXMESH);
				break;
		}
	}
	
	/**
	 * Load model method
	 * @param id
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @param pivot
	 * @return level editor model
	 * @throws Exception
	 */
	private LevelEditorModel loadModel(String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
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
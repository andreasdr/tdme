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
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.tools.viewer.Tools;
import net.drewke.tdme.tools.viewer.controller.ModelLibraryController;
import net.drewke.tdme.tools.viewer.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.viewer.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.viewer.model.LevelEditorModel;
import net.drewke.tdme.tools.viewer.model.LevelPropertyPresets;
import net.drewke.tdme.tools.viewer.model.PropertyModelClass;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GLAutoDrawable;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem;
import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem.MouseInputEvent;

/**
 * TDME Model Library
 * @author andreas.drewke
 * @version $Id: 04313d20d0978eefc881024d6e0af748196c1425 $
 */
public final class ModelLibraryView extends View  {

	private Engine engine;
	private Nifty nifty;

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
	 * Remove a object property from object
	 * @param object property
	 */
	public void objectPropertyRemove(PropertyModelClass property) {
		if (model == null) return;
		model.removeProperty(property.getName());
	}

	/**
	 * Apply object property preset
	 * @param preset id
	 */
	public void objectPropertiesPreset(String presetId) {
		if (model == null) return;

		//
		ModelLibraryController controller = (ModelLibraryController)nifty.getCurrentScreen().getScreenController();

		// clear object properties
		model.clearProperties();

		// add object properties by preset if missing
		ArrayList<PropertyModelClass> objectPropertyPresetVector = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (objectPropertyPresetVector != null) {
			for (PropertyModelClass objectPropertyPreset: objectPropertyPresetVector) {
				model.addProperty(objectPropertyPreset.clone());
			}
		}

		// update object properties to gui
		controller.setObjectProperties(
			presetId,
			model.getProperties()
		);
	}

	/**
	 * Save a object property
	 * @param object property
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean objectPropertySave(PropertyModelClass objectProperty, String name, String value) {
		if (model == null) return false;

		//
		return model.updateProperty(objectProperty, name, value);
	}

	/**
	 * Add a object property
	 * @return map property
	 */
	public PropertyModelClass objectPropertyAdd() {
		if (model == null) return null;

		PropertyModelClass objectProperty = new PropertyModelClass("new.property", "new.value");
		if (model.addProperty(objectProperty)) return objectProperty;

		//
		return null;
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

	/**
	 * Do input system
	 */
	public void doInputSystem(GLAutoDrawable drawable) {
		// process mouse events
		JoglInputSystem niftyInputSystem = TDMEViewer.getInstance().getNiftyInputSystem();
		if (niftyInputSystem == null) return;

		//
		while (TDMEViewer.getInstance().getNiftyInputSystem().hasNextMouseEvent()) {
			MouseInputEvent event = niftyInputSystem.nextMouseEvent();

			// dragging
			if (mouseDragging == true) {	
				if (event.isButtonDown()) {
					float xMoved = (event.getMouseX() - mouseLastX) / 5f;
					float yMoved = (event.getMouseY() - mouseLastY) / 5f;
					mouseLastX = event.getMouseX();
					mouseLastY = event.getMouseY();
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
				if (event.isButtonDown()) {
					mouseDragging = true;
					mouseLastX = event.getMouseX();
					mouseLastY = event.getMouseY();					
				}
			}

			// process mouse wheel events
			int mouseWheel = event.getMouseWheel();
			if (mouseWheel != 0) {
				scale+= mouseWheel * 0.05f;
				if (scale < 0.05f) scale = 0.05f;
			}
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
				ModelLibraryController controller = (ModelLibraryController)TDMEViewer.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
				maxAxisDimension = Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel()));

				// set up model statistics
				ModelUtilities.ModelStatistics stats = ModelUtilities.computeModelStatistics(model.getModel());
				controller.setStatistics(stats.getOpaqueFaceCount(), stats.getTransparentFaceCount(), stats.getMaterialCount());

				// set up oriented bounding box
				BoundingBox aabb = Engine.getModelBoundingBox(model.getModel());
				OrientedBoundingBox obb = new OrientedBoundingBox(aabb);

				// set up sphere
				controller.setupSphere(
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
					controller.setupCapsule(a, b, radius);
				}

				// set up AABB bounding box
				controller.setupBoundingBox(aabb.getMin(), aabb.getMax());

				// set up oriented bounding box
				controller.setupOrientedBoundingBox(
					obb.getCenter(),
					obb.getAxes()[0],
					obb.getAxes()[1],
					obb.getAxes()[2],
					obb.getHalfExtension()
				);
			}
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
	}

	/**
	 * Init GUI elements
	 */
	private void updateGUIElements(GLAutoDrawable drawable) {
		ModelLibraryController controller = (ModelLibraryController)TDMEViewer.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
		if (model != null) {
			controller.setScreenCaption("Model Library - " + model.getModel().getName());
			PropertyModelClass preset = model.getProperty("preset");
			controller.setObjectProperties(preset != null?preset.getValue():"",model.getProperties());
			controller.setModelData(model.getName(), model.getDescription());
			controller.setPivot(model.getPivot());
			controller.setBoundingVolume();
		} else {
			controller.setScreenCaption("Model Library - no model loaded");
			controller.unsetObjectProperties();
			controller.unsetModelData();
			controller.unsetPivot();
			controller.unsetBoundingVolume();
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
		nifty = TDMEViewer.getInstance().getNifty(drawable);

		//
		nifty.fromXml("resources/tools/viewer/gui/screen_modellibrary.xml", "modellibrary");
		nifty.update();

		//
		ModelLibraryController controller = (ModelLibraryController)TDMEViewer.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();

		// set up object properties presets
		controller.setObjectPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());

		// set up bounding volume types
		controller.setupBoundingVolumeTypes(
			new String[] {
				"None", "Sphere",
				"Capsule", "Bounding Box",
				"Oriented Bounding Box", "Convex Mesh"
			},
			"None"
		);
		controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.NONE);

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
		ModelLibraryController controller = (ModelLibraryController)nifty.getCurrentScreen().getScreenController();
		switch (bvTypeId) {
			case 0:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.NONE);
				break;
			case 1:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.SPHERE);
				break;
			case 2:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.CAPSULE);
				break;
			case 3:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.BOUNDINGBOX);
				break;
			case 4:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.ORIENTEDBOUNDINGBOX);
				break;
			case 5:
				controller.selectBoundingVolume(ModelLibraryController.BoundingVolumeType.CONVEXMESH);
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

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseClicked(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseEntered(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseExited(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mousePressed(com.jogamp.newt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseReleased(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseDragged(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyPressed(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyPressed(com.jogamp.newt.event.KeyEvent event) {
		if (event.isAutoRepeat() == true) return;

		//
		int keyCode = event.getKeyCode();
		char keyChar = event.getKeyChar();
		boolean keyConsumed = false;
		if (keyCode == KeyEvent.VK_LEFT) { keyLeft = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_RIGHT) { keyRight = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_UP) { keyUp = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_DOWN) { keyDown = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_A) { keyA = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_D) { keyD = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_W) { keyW = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_S) { keyS = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_COMMA) { keyComma = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_PERIOD) { keyPeriod = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_PLUS || keyChar == '+') { keyPlus = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_MINUS || keyChar == '-') { keyMinus = true; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_R) {  keyR = true; keyConsumed = true; }

		//
		event.setConsumed(keyConsumed);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyReleased(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyReleased(com.jogamp.newt.event.KeyEvent event) {
		if (event.isAutoRepeat() == true) return;

		//
		int keyCode = event.getKeyCode();
		char keyChar = event.getKeyChar();
		boolean keyConsumed = false;
		if (keyCode == KeyEvent.VK_LEFT) { keyLeft = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_RIGHT) {  keyRight = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_UP) {  keyUp = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_DOWN) { keyDown = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_A) { keyA = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_D) { keyD = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_W) { keyW = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_S) { keyS = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_COMMA) {  keyComma = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_PERIOD) {  keyPeriod = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_PLUS || keyChar == '+') { keyPlus = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_MINUS || keyChar == '-') { keyMinus = false; keyConsumed = true; }
		if (keyCode == KeyEvent.VK_R) {  keyR = false; keyConsumed = true; }

		//
		event.setConsumed(keyConsumed);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseWheelMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseWheelMoved(MouseEvent arg0) {
	}

}
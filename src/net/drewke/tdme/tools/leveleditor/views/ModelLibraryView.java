package net.drewke.tdme.tools.leveleditor.views;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.Tools;
import net.drewke.tdme.tools.leveleditor.controller.ModelLibraryController;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.leveleditor.model.LevelPropertyPresets;
import net.drewke.tdme.tools.leveleditor.model.PropertyModelClass;

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
	private boolean createTriggerRequested;
	private boolean setModelRequested;
	private boolean createThumbnailRequested;
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
	private boolean dispalyShadowing = false;
	private boolean displayBoundingVolume = false;

	/**
	 * Public constructor
	 */
	public ModelLibraryView() {
		loadModelRequested = false;
		createTriggerRequested = false;
		setModelRequested = false;
		createThumbnailRequested = false;
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
	 * Set up ground plate visibility
	 * @param ground plate visible
	 */
	public void setDisplayGroundPlate(boolean groundPlate) {
		this.displayGroundPlate = groundPlate;
	}

	/**
	 * Set up shadow rendering
	 * @param shadow rendering
	 */
	public void setDisplayShadowing(boolean shadowing) {
		this.dispalyShadowing = shadowing;
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
	 * Create trigger
	 */
	public void createTrigger() {
		createTriggerRequested = true;
	}

	/**
	 * Set up trigger dimension
	 * @param width
	 * @param height
	 * @param depth
	 */
	public void triggerApply(float width, float height, float depth) {
		if (model == null || model.getType() != LevelEditorModel.ModelType.TRIGGER) return;

		// current model
		LevelEditorModel replacedModel = model;

		// remove
		LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getLevel().getModelLibrary();
		ArrayList<PropertyModelClass> properties = new ArrayList<PropertyModelClass>();
		if (model != null) {
			// store properties
			for (PropertyModelClass property: model.getProperties()) {
				properties.add(property);
			}
			// remove model
			modelLibrary.removeModel(model.getId());
		}

		// recreate
		try {
			// recreate trigger
			model = modelLibrary.createTrigger(
				model.getId(), 
				model.getName(),
				model.getDescription(),
				width,
				height,
				depth,
				new Vector3()
			);

			// set up properties
			for (PropertyModelClass property: properties) {
				model.addProperty(property);
			}

			// replace model in map
			TDMELevelEditor.getInstance().getLevel().replaceModel(replacedModel.getId(), model.getId());
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		setModelRequested = true;
		createThumbnailRequested = true;
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
		TDMELevelEditor.getInstance().getLevel().updatePivot(model.getId(), model.getPivot());
	}

	/**
	 * Load model from library
	 * @param id
	 */
	public void loadModelFromLibrary(int id) {
		model = TDMELevelEditor.getInstance().getModelLibrary().getModel(id);
		setModelRequested = true;
	}

	/**
	 * Removes currently selected model
	 */
	public void removeModel() {
		TDMELevelEditor.getInstance().getLevel().removeObjectsByModelId(model.getId());
		if (model != null) TDMELevelEditor.getInstance().getModelLibrary().removeModel(model.getId());
		model = null;
		setModelRequested = true;
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
		JoglInputSystem niftyInputSystem = TDMELevelEditor.getInstance().getNiftyInputSystem();
		if (niftyInputSystem == null) return;

		//
		while (TDMELevelEditor.getInstance().getNiftyInputSystem().hasNextMouseEvent()) {
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
		// load new model if requested
		if (createTriggerRequested == true) {
			model = null;
			engine.reset();
			try {
				// add model to library
				model = TDMELevelEditor.getInstance().getModelLibrary().createTrigger(
					LevelEditorModelLibrary.ID_ALLOCATE,
					"New trigger",
					"",
					1f,
					2f,
					1f,
					new Vector3()
				);
			} catch (Exception exception) {
				JOptionPane.showMessageDialog(null, "Could not create trigger: " + exception.getMessage());
			}

			//
			if (model != null) {
				// set up model in engine
				Tools.setupModel(model, engine, lookFromRotations, scale);

				// Make model screenshot
				Tools.oseThumbnail(drawable, model);

				// add model
				ModelLibraryController controller = (ModelLibraryController)TDMELevelEditor.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
				controller.addModel(model);
				maxAxisDimension = Tools.computeMaxAxisDimension(model.getBoundingVolume());
			}
			updateGUIElements(drawable);
			createTriggerRequested = false;
		} else
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
				ModelLibraryController controller = (ModelLibraryController)TDMELevelEditor.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
				controller.addModel(model);
				maxAxisDimension = 
					model.getBoundingVolume() == null?
					Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel())):
					Tools.computeMaxAxisDimension(model.getBoundingVolume());
			}
			updateGUIElements(drawable);
		} else
		if (setModelRequested == true) {
			engine.reset();
			if (model != null) {
				Tools.setupModel(model, engine, lookFromRotations, scale);
				maxAxisDimension = 
						model.getBoundingVolume() == null?
						Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel())):
						Tools.computeMaxAxisDimension(model.getBoundingVolume());
				if (createThumbnailRequested == true) {
					// Make model screenshot
					Tools.oseThumbnail(drawable, model);
					ModelLibraryController controller = (ModelLibraryController)TDMELevelEditor.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
					controller.updateModel(model);
					//
					createThumbnailRequested = false;
				}
			}
			updateGUIElements(drawable);
			setModelRequested = false;
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
		Vector3 lookAtToFromVectorTransformed = new Vector3();
		Vector3 upVector = new Vector3();
		lookFromRotations.getTransformationsMatrix().multiply(lookAtToFromVector, lookAtToFromVectorTransformed);
		Vector3 lookAtToFromVectorScaled = lookAtToFromVectorTransformed.clone().scale(scale);
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
			model.setDynamicShadowingEnabled(dispalyShadowing);
			ground.setEnabled(displayGroundPlate);
			Entity modelBoundingVolume = engine.getEntity("model_bv");
			if (modelBoundingVolume != null) modelBoundingVolume.setEnabled(displayBoundingVolume);
		}
	}

	/**
	 * Init GUI elements
	 */
	private void updateGUIElements(GLAutoDrawable drawable) {
		ModelLibraryController controller = (ModelLibraryController)TDMELevelEditor.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();
		if (model != null) {
			controller.setScreenCaption("Model Library - " + model.getModel().getName());
			PropertyModelClass preset = model.getProperty("preset");
			controller.setObjectProperties(preset != null?preset.getValue():"",model.getProperties());
			controller.setModelData(model.getName(), model.getDescription());
			if (model.getType() == LevelEditorModel.ModelType.TRIGGER) {
				Vector3 triggerDimension = new Vector3();
				triggerDimension.set(
					((BoundingBox)model.getBoundingVolume()).getMax()).sub(((BoundingBox)model.getBoundingVolume()).getMin());
				controller.setTrigger(triggerDimension);
			} else {
				controller.unsetTrigger();
			}
			controller.setPivot(model.getPivot());
		} else {
			controller.setScreenCaption("Model Library - no model loaded");
			controller.unsetObjectProperties();
			controller.unsetModelData();
			controller.unsetTrigger();
			controller.unsetPivot();
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
		nifty = TDMELevelEditor.getInstance().getNifty(drawable);

		//
		nifty.fromXml("resources/tools/leveleditor/gui/screen_modellibrary.xml", "modellibrary");
		nifty.update();

		//
		ModelLibraryController controller = (ModelLibraryController)TDMELevelEditor.getInstance().getNifty(drawable).getCurrentScreen().getScreenController();

		// set up object properties presets
		controller.setObjectPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());

		//
		LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getModelLibrary();
		for (int i = 0; i < modelLibrary.getModelCount(); i++) {
			controller.addModel(modelLibrary.getModelAt(i));
		}

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
			model = TDMELevelEditor.getInstance().getModelLibrary().addModel(
				LevelEditorModelLibrary.ID_ALLOCATE,
				modelFile.getName(),
				"",
				modelFile.getParentFile().getAbsolutePath(),
				modelFile.getName(),
				new Vector3()
			);
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(null, "Could not load object: " + exception.getMessage());
		}

		//
		loadModelRequested = false;
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
package net.drewke.tdme.tools.leveleditor.views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Timing;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Model.UpVector;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIKeyboardEvent.Type;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.Tools;
import net.drewke.tdme.tools.leveleditor.controller.LevelEditorModelLibraryScreenController;
import net.drewke.tdme.tools.leveleditor.controller.LevelEditorScreenController;
import net.drewke.tdme.tools.shared.controller.FileDialogScreenController;
import net.drewke.tdme.tools.shared.controller.InfoDialogScreenController;
import net.drewke.tdme.tools.shared.files.LevelFileExport;
import net.drewke.tdme.tools.shared.files.LevelFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.views.View;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME Level Editor View
 * @author andreas.drewke
 * @version $Id: 04313d20d0978eefc881024d6e0af748196c1425 $
 */
public final class LevelEditorView extends View implements GUIInputEventHandler  {

	/**
	 * Object Color
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	class ObjectColor {

		/**
		 * Public constructor
		 * @param name
		 * @param colorMulR
		 * @param colorMulG
		 * @param colorMulB
		 * @param colorAddR
		 * @param colorAddG
		 * @param colorAddB
		 */
		public ObjectColor(
			float colorMulR, float colorMulG, float colorMulB,
			float colorAddR, float colorAddG, float colorAddB) {
			this.colorMulR = colorMulR;
			this.colorMulG = colorMulG;
			this.colorMulB = colorMulB;
			this.colorAddR = colorAddR;
			this.colorAddG = colorAddG;
			this.colorAddB = colorAddB;
		}

		// protected members
		protected float colorMulR = 1.0f;
		protected float colorMulG = 1.0f;
		protected float colorMulB = 1.0f;
		protected float colorAddR = 0.0f;
		protected float colorAddG = 0.0f;
		protected float colorAddB = 0.0f;		
	}

	private final static String[] OBJECTCOLOR_NAMES = {"blue", "yellow", "magenta", "cyan", "none"};
	private final static String MODEL_ROOT = "/resources/models/";
	private final static String GAME_ROOT = "/../../../";

	private final static int MOUSE_BUTTON_NONE = 0;
	private final static int MOUSE_BUTTON_LEFT = 1;
	private final static int MOUSE_BUTTON_MIDDLE = 2;
	private final static int MOUSE_BUTTON_RIGHT = 3;
	private final static int MOUSE_DOWN_LAST_POSITION_NONE = -1;
	private final static int MOUSE_PANNING_NONE = 0;
	private final static int MOUSE_ROTATION_NONE = 0;

	private int GRID_DIMENSION_X = 20;
	private int GRID_DIMENSION_Y = 20;

	private LevelEditorModelLibraryScreenController levelEditorModelLibraryScreenController;
	private InfoDialogScreenController infoDialogScreenController;
	private FileDialogScreenController fileDialogScreenController;
	private LevelEditorScreenController levelEditorScreenController;

	private Engine engine;

	private LevelEditorModel selectedModel;

	private boolean reloadModelLibrary;

	private HashMap<String, ObjectColor> objectColors;

	// camera properties
	Rotation camLookRotationX = new Rotation(-45f, new Vector3(1f, 0f, 0f));
	Rotation camLookRotationY = new Rotation(0f, new Vector3(0f, 1f, 0f));
	Vector3 camLookAt;
	Vector3 camLookFrom;
	float camScale;
	float camScaleMax = 3f;
	float camScaleMin = 0.05f;

	// input properties
	private int mouseDownLastX = MOUSE_DOWN_LAST_POSITION_NONE;
	private int mouseDownLastY = MOUSE_DOWN_LAST_POSITION_NONE;
	private boolean mouseDragging;
	private Entity mouseDraggingLastObject;
	private int mousePanningSide = MOUSE_PANNING_NONE;
	private int mousePanningForward = MOUSE_PANNING_NONE;
	private int mouseRotationX = MOUSE_ROTATION_NONE;
	private int mouseRotationY = MOUSE_ROTATION_NONE;
	private Vector3 gridCenter;
	private Vector3 gridCenterLast;
	private boolean gridEnabled;
	private float gridY;

	private boolean keyLeft;
	private boolean keyRight;
	private boolean keyUp;
	private boolean keyDown;
	private boolean keyA;
	private boolean keyD;
	private boolean keyW;
	private boolean keyS;
	private boolean keyPlus;
	private boolean keyMinus;
	private boolean keyR;
	private boolean keyControl;
	private boolean keyEscape;

	float groundPlateWidth = 1.0f;
	float groundPlateDepth = 1.0f;
	private Model levelEditorGround;

	private LevelEditorLevel level;
	private ArrayList<Entity> selectedObjects = null;
	private HashMap<String, Entity> selectedObjectsById = null;
	private ArrayList<Entity> pasteObjects = null;

	/**
	 * Public constructor
	 */
	public LevelEditorView() {
		level = TDMELevelEditor.getInstance().getLevel();
		reloadModelLibrary = false;
		selectedModel = null;
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyDown = false;
		keyPlus = false;
		keyMinus = false;
		keyR = false;
		keyControl = false;
		keyEscape = false;
		mouseDownLastX = MOUSE_DOWN_LAST_POSITION_NONE;
		mouseDownLastY = MOUSE_DOWN_LAST_POSITION_NONE;
		mouseDragging = false;
		mouseDraggingLastObject = null;
		gridCenter = new Vector3();
		gridCenterLast = null;
		gridEnabled = true;
		gridY = 0f;
		objectColors = new HashMap<String, LevelEditorView.ObjectColor>();
		objectColors.put("red", new ObjectColor(1.5f, 0.8f, 0.8f, 0.5f, 0.0f, 0.0f));
		objectColors.put("green", new ObjectColor(0.8f, 1.5f, 0.8f, 0.0f, 0.5f, 0.0f));
		objectColors.put("blue", new ObjectColor(0.8f, 0.8f, 1.5f, 0.0f, 0.0f, 0.5f));
		objectColors.put("yellow", new ObjectColor(1.5f, 1.5f, 0.8f, 0.5f, 0.5f, 0.0f));
		objectColors.put("magenta", new ObjectColor(1.5f, 0.8f, 1.5f, 0.5f, 0.0f, 0.5f));
		objectColors.put("cyan", new ObjectColor(0.8f, 1.5f, 1.5f, 0.0f, 0.5f, 0.5f));
		objectColors.put("none", new ObjectColor(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f));
		selectedObjects = new ArrayList<Entity>();
		selectedObjectsById = new HashMap<String, Entity>();
		pasteObjects = new ArrayList<Entity>();

		camScale = 1.0f;
		camLookRotationX.update();
		camLookRotationY.update();

		//
		levelEditorGround = createLevelEditorGroundPlateModel();

		//
		engine = Engine.getInstance();
		camLookFrom = engine.getCamera().getLookFrom();
		camLookAt = engine.getCamera().getLookAt();
	}

	/**
	 * @return file dialog popup controller
	 */
	public FileDialogScreenController getFileDialogPopUpController() {
		return fileDialogScreenController;
	}

	/**
	 * @return info dialog popup controller
	 */
	public InfoDialogScreenController getInfoDialogPopUpController() {
		return infoDialogScreenController;
	}

	/**
	 * @return level file name
	 */
	public String getFileName() {
		return new File(level.getFileName()).getName();
	}

	/**
	 * @return level
	 */
	public LevelEditorLevel getLevel() {
		return level;
	}

	/**
	 * @return selected selectedModel
	 */
	public LevelEditorModel getSelectedModel() {
		return selectedModel;
	}

	/**
	 * @return selected level editor object
	 */
	public LevelEditorObject getSelectedObject() {
		if (selectedObjects.size() != 1) return null;

		//
		Entity selectedObject = selectedObjects.get(0);
		return selectedObject != null && selectedObject.getId().startsWith("leveleditor.") == false?level.getObjectById(selectedObject.getId()):null;
	}

	/**
	 * @return grid enabled
	 */
	public boolean isGridEnabled() {
		return gridEnabled;
	}

	/**
	 * @param grid enabled
	 */
	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		if (gridEnabled) {
			gridCenterLast = null;
			updateGrid();
		} else {
			removeGrid();
		}
	}

	/**
	 * @return grid y
	 */
	public float getGridY() {
		return gridY;
	}

	/**
	 * Set grid y position 
	 * @param grid y
	 */
	public void setGridY(float gridY) {
		if (gridEnabled) removeGrid();
		this.gridY = gridY;
		if (gridEnabled) updateGrid();
	}

	/**
	 * Load selectedModel from library
	 * @param id
	 */
	public void loadModelFromLibrary(int id) {
		selectedModel = TDMELevelEditor.getInstance().getModelLibrary().getModel(id);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIInputEventHandler#handleInputEvents()
	 */
	public void handleInputEvents() {
		// handle level editor model library screen controller events
		engine.getGUI().handleEvents(levelEditorModelLibraryScreenController.getScreenNode().getId(), null, false);

		// handle keyboard events
		for (int i = 0; i < engine.getGUI().getKeyboardEvents().size(); i++) {
			GUIKeyboardEvent event = engine.getGUI().getKeyboardEvents().get(i);

			// skip on processed events
			if (event.isProcessed() == true) continue;

			//
			boolean isKeyDown = event.getType() == Type.KEY_PRESSED;

			//
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_CONTROL) keyControl = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_ESCAPE) keyEscape = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_LEFT) keyLeft = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_RIGHT) keyRight = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_UP) keyUp = isKeyDown;
			if (event.getKeyCode() == GUIKeyboardEvent.KEYCODE_DOWN) keyDown = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'a') keyA = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'd') keyD = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'w') keyW = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 's') keyS = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == '+') keyPlus = isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == '-') keyMinus= isKeyDown;
			if (Character.toLowerCase(event.getKeyChar()) == 'r') keyR = isKeyDown;
		}

		// 	unselect objects by key escape
		if (keyEscape == true && selectedObjects.size() > 0) {
			ArrayList<Entity> objectsToRemove = new ArrayList<Entity>();
			for (Entity selectedObject: selectedObjects) {
				objectsToRemove.add(selectedObject);
			}
			for (Entity objectToRemove: objectsToRemove) {
				setStandardObjectColorEffect(objectToRemove);
				selectedObjects.remove(objectToRemove);
				selectedObjectsById.remove(objectToRemove.getId());
			}
			levelEditorScreenController.unselectObjectsInObjectListBox();
		}

		// handle mouse events
		for (int i = 0; i < engine.getGUI().getMouseEvents().size(); i++) {
			GUIMouseEvent event = engine.getGUI().getMouseEvents().get(i);

			// skip on processed events
			if (event.isProcessed() == true) continue;

			// check if dragging
			if (event.getButton() != MOUSE_BUTTON_NONE) {
				// check if dragging
				if (mouseDragging == false) {
					if (mouseDownLastX != event.getX() ||
						mouseDownLastY != event.getY()) {
						mouseDragging = true;
					}
				}
			} else {
				// unset dragging
				if (mouseDragging == true) {
					mouseDownLastX = MOUSE_DOWN_LAST_POSITION_NONE;
					mouseDownLastY = MOUSE_DOWN_LAST_POSITION_NONE;
					mouseDragging = false;
					mouseDraggingLastObject = null;
				}
			}

			// selection
			if (event.getButton() == MOUSE_BUTTON_LEFT) {
				// check if dragging
				if (mouseDragging == false) {
					if (mouseDownLastX != event.getX() ||
						mouseDownLastY != event.getY()) {
						mouseDragging = true;
					}
				}

				// unselect current selected objects
				if (keyControl == false) {
					ArrayList<Entity> objectsToRemove = new ArrayList<Entity>();
					for (Entity selectedObject: selectedObjects) {
						if (mouseDragging == true && mouseDraggingLastObject == selectedObject) {
							/// no op
						} else {
							objectsToRemove.add(selectedObject);
						}
					}
					for (Entity objectToRemove: objectsToRemove) {
						setStandardObjectColorEffect(objectToRemove);
						selectedObjects.remove(objectToRemove);
						selectedObjectsById.remove(objectToRemove.getId());
						levelEditorScreenController.unselectObjectInObjectListBox(objectToRemove.getId());
					}
				}

				// check if ground plate was clicked
				Entity selectedObject = engine.getObjectByMousePosition(event.getX(), event.getY());

				// select cell if any was selected
				if (selectedObject != null) {
					// add to selected objects if not yet done
					if (mouseDragging == true && mouseDraggingLastObject == selectedObject) {
						// no op
					} else {
						if (selectedObjects.contains(selectedObject) == false) {
							setStandardObjectColorEffect(selectedObject);
							setHighlightObjectColorEffect(selectedObject);
							selectedObjects.add(selectedObject);
							selectedObjectsById.put(selectedObject.getId(), selectedObject);

							// select in objects listbox
							levelEditorScreenController.selectObjectInObjectListbox(selectedObject.getId());
						} else {
							// undo add
							setStandardObjectColorEffect(selectedObject);
							selectedObjects.remove(selectedObject);
							selectedObjectsById.remove(selectedObject.getId());

							// unselect in objects listbox
							levelEditorScreenController.unselectObjectInObjectListBox(selectedObject.getId());
						}
					}
				}

				// set mouse dragging last
				mouseDraggingLastObject = selectedObject;

				// update gui elements
				updateGUIElements();
			} else
			// panning
			if (event.getButton() == MOUSE_BUTTON_RIGHT) {
				if (mouseDownLastX != MOUSE_DOWN_LAST_POSITION_NONE &&
					mouseDownLastY != MOUSE_DOWN_LAST_POSITION_NONE) {
					mousePanningSide = event.getX() - mouseDownLastX;
					mousePanningForward = event.getY() - mouseDownLastY;
				}
			} else
			if (event.getButton() == MOUSE_BUTTON_MIDDLE) {
				centerObject();
				if (mouseDownLastX != MOUSE_DOWN_LAST_POSITION_NONE &&
					mouseDownLastY != MOUSE_DOWN_LAST_POSITION_NONE) {
					mouseRotationX = event.getX() - mouseDownLastX;
					mouseRotationY = event.getY() - mouseDownLastY;
				}
			}

			// last mouse down position
			if (event.getButton() != MOUSE_BUTTON_NONE) {
				//
				mouseDownLastX = event.getX();
				mouseDownLastY = event.getY();
			}

			// process mouse wheel events
			float mouseWheel = event.getWheelY();
			if (mouseWheel != 0) {
				camScale+= -mouseWheel * 0.05f;
				if (camScale < camScaleMin) camScale = camScaleMin;
				if (camScale > camScaleMax) camScale = camScaleMax;
			}
		}
	}

	/**
	 * Renders the scene 
	 */
	public void display(GLAutoDrawable drawable) {
		if (reloadModelLibrary == true) {
			LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getModelLibrary();
			for (int i = 0; i < modelLibrary.getModelCount(); i++) {
				selectedModel = modelLibrary.getModelAt(i);
				Tools.oseThumbnail(drawable, selectedModel);
			}
			reloadModelLibrary = false;
			levelEditorModelLibraryScreenController.setModelLibrary(modelLibrary);
		}

		// do camera rotation by middle mouse button
		if (mouseRotationX != MOUSE_ROTATION_NONE) {
			camLookRotationY.setAngle(camLookRotationY.getAngle() + mouseRotationX);
			camLookRotationY.update();
			mouseRotationX = 0;
		}
		if (mouseRotationY != MOUSE_ROTATION_NONE) {
			camLookRotationX.setAngle(camLookRotationX.getAngle() + mouseRotationY);
			camLookRotationX.update();
			mouseRotationY = 0;
		}

		// 	transfer keyboard inputs to rotations
		if (keyA) camLookRotationY.setAngle(camLookRotationY.getAngle() + 1.0f);
		if (keyD) camLookRotationY.setAngle(camLookRotationY.getAngle() - 1.0f);
		if (keyW) camLookRotationX.setAngle(camLookRotationX.getAngle() + 1.0f);
		if (keyS) camLookRotationX.setAngle(camLookRotationX.getAngle() - 1.0f);
		if (keyMinus) camScale+= 0.05f;
		if (keyPlus) camScale-= 0.05f;
		if (camScale < camScaleMin) camScale = camScaleMin;
		if (camScale > camScaleMax) camScale = camScaleMax;
		if (keyR) {
			camLookRotationX.setAngle(-45.0f);
			camLookRotationX.update();
			camLookRotationY.setAngle(0.0f);
			camLookRotationY.update();
			camLookAt.set(level.computeCenter());
			camScale = 1.0f;
		}


		// update cam look from rotations if changed
		if (keyA || keyD) camLookRotationY.update();
		if (keyW || keyS) {
			if (camLookRotationX.getAngle() > 89.99f) camLookRotationX.setAngle(89.99f);
			if (camLookRotationX.getAngle() < -89.99f) camLookRotationX.setAngle(-89.99f);
			camLookRotationX.update();
		}
		Camera cam = engine.getCamera();

		// look at -> look to vector
		Vector3 lookAtToFromUnitVector = new Vector3(0f, 0f, 1.0f);
		Vector3 lookAtToFromVectorXAxisRotation = new Vector3();
		Vector3 lookAtToFromVectorXYAxisRotation = new Vector3();
		camLookRotationX.getQuaternion().multiply(lookAtToFromUnitVector, lookAtToFromVectorXAxisRotation);
		camLookRotationY.getQuaternion().multiply(lookAtToFromVectorXAxisRotation, lookAtToFromVectorXYAxisRotation);

		// apply look from rotations
		Vector3 lookAtToFromVector =
			lookAtToFromVectorXYAxisRotation.
			scale(camScale * 10.0f);

		Timing timing = engine.getTiming();
		// do camera movement with arrow keys
		Vector3 forwardVector = new Vector3();
		Vector3 sideVector = new Vector3();
		camLookRotationY.getQuaternion().multiply(new Vector3(0.0f, 0.0f, 1.0f), forwardVector).scale(timing.getDeltaTime() / 1000f * 60f);
		camLookRotationY.getQuaternion().multiply(new Vector3(1.0f, 0.0f, 0.0f), sideVector).scale(timing.getDeltaTime() / 1000f * 60f);
		if (keyUp) camLookAt.sub(forwardVector.clone().scale(0.1f));
		if (keyDown) camLookAt.add(forwardVector.clone().scale(0.1f));
		if (keyLeft) camLookAt.sub(sideVector.clone().scale(0.1f));
		if (keyRight) camLookAt.add(sideVector.clone().scale(0.1f));
		if (mousePanningForward != MOUSE_PANNING_NONE) {
			camLookAt.sub(forwardVector.clone().scale(mousePanningForward / 30f * camScale));
			mousePanningForward = MOUSE_PANNING_NONE;
		}
		if (mousePanningSide != MOUSE_PANNING_NONE) {
			camLookAt.sub(sideVector.clone().scale(mousePanningSide / 30f * camScale));
			mousePanningSide = MOUSE_PANNING_NONE;
		}

		// look from with rotations
		camLookFrom.set(camLookAt.clone().add(lookAtToFromVector));

		// up vector
		cam.computeUpVector(camLookFrom, camLookAt, cam.getUpVector());

		// update grid
		gridCenter.set(camLookAt);
		updateGrid();

		// gui
		// Render screens and handle input
		String activeId = levelEditorScreenController.getScreenNode().getId();
		engine.getGUI().render(levelEditorScreenController.getScreenNode().getId());
		engine.getGUI().render(levelEditorModelLibraryScreenController.getScreenNode().getId());
		if (fileDialogScreenController.isActive() == true) {
			engine.getGUI().render(fileDialogScreenController.getScreenNode().getId());
			activeId = fileDialogScreenController.getScreenNode().getId();
		}
		if (infoDialogScreenController.isActive() == true) {
			engine.getGUI().render(infoDialogScreenController.getScreenNode().getId());
			activeId = infoDialogScreenController.getScreenNode().getId();
		}
		engine.getGUI().handleEvents(activeId, this);
	}

	/**
	 * Select objects
	 * @param object ids
	 */
	public void selectObjects(ArrayList<String> objectIds) {
		// remove all objects which are currently selected 
		ArrayList<Object3D> objectsToRemove = (ArrayList<Object3D>)selectedObjects.clone();
		for (Object3D objectToRemove: objectsToRemove) {
			setStandardObjectColorEffect(objectToRemove);
			selectedObjects.remove(objectToRemove);
			selectedObjectsById.remove(objectToRemove.getId());
		}

		// select objects from selection
		for (String objectId: objectIds) {
			Entity selectedObject = engine.getEntity(objectId);
			setStandardObjectColorEffect(selectedObject);
			setHighlightObjectColorEffect(selectedObject);
			selectedObjects.add(selectedObject);
			selectedObjectsById.put(selectedObject.getId(), selectedObject);
		}

		// update gui elements
		updateGUIElements();
	}

	/**
	 * Select objects by id
	 * @param id
	 */
	public void unselectObjects() {
		// remove all objects which are currently selected 
		ArrayList<Object3D> objectsToRemove = (ArrayList<Object3D>)selectedObjects.clone();
		for (Object3D objectToRemove: objectsToRemove) {
			setStandardObjectColorEffect(objectToRemove);
			selectedObjects.remove(objectToRemove);
			selectedObjectsById.remove(objectToRemove.getId());
		}

		//
		levelEditorScreenController.unselectObjectsInObjectListBox();

		// update gui elements
		updateGUIElements();
	}

	/**
	 * Update GUI elements
	 * 	screen caption
	 *  level size
	 *  selected object
	 *		object properties
	 *		object 3d transformations
	 *		object data  
	 */
	private void updateGUIElements() {
		levelEditorScreenController.setScreenCaption("Level Editor - " + level.getFileName());
		levelEditorScreenController.setLevelSize(level.getDimension().getX(), level.getDimension().getZ(), level.getDimension().getY());
		if (selectedObjects.size() == 1) {
			Entity selectedObject = selectedObjects.get(0);
			if (selectedObject != null && selectedObject.getId().startsWith("leveleditor.") == false) {
				LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
				PropertyModelClass preset = levelEditorObject.getProperty("preset");
				levelEditorScreenController.setObjectProperties(
					preset != null?preset.getValue():"",
					levelEditorObject.getProperties(),
					null
				);
				levelEditorScreenController.setObject(
					selectedObject.getTranslation(),
					selectedObject.getScale(),
					selectedObject.getRotations().get(level.getRotationOrder().getAxisXIndex()).getAngle(),
					selectedObject.getRotations().get(level.getRotationOrder().getAxisYIndex()).getAngle(),
					selectedObject.getRotations().get(level.getRotationOrder().getAxisZIndex()).getAngle()
				);
				BoundingVolume bv = levelEditorObject.getModel().getBoundingBox().clone();
				bv.fromBoundingVolumeWithTransformations(bv, levelEditorObject.getTransformations());
				Vector3 objectCenter = bv.getCenter();
				levelEditorScreenController.setObjectData(
					levelEditorObject.getId(),
					levelEditorObject.getDescription(),
					levelEditorObject.getModel().getName(),
					objectCenter
				);
			} else {
				levelEditorScreenController.unsetObjectData();
				levelEditorScreenController.unsetObject();
				levelEditorScreenController.unsetObjectProperties();
			}
		} else
		if (selectedObjects.size() > 1) {
			levelEditorScreenController.unsetObjectData();
			levelEditorScreenController.setObject(
				new Vector3(0f,0f,0f),
				new Vector3(1f,1f,1f),
				0f,
				0f, 
				0f
			);			
			levelEditorScreenController.unsetObjectProperties();
		} else
		if (selectedObjects.size() == 0) {
			levelEditorScreenController.unsetObjectData();
			levelEditorScreenController.unsetObject();
			levelEditorScreenController.unsetObjectProperties();
		}

		// set up lights
		for (int i = 0; i < 4; i++) {
			levelEditorScreenController.setLight(
				i,
				level.getLightAt(i).getAmbient(),
				level.getLightAt(i).getDiffuse(),
				level.getLightAt(i).getSpecular(),
				level.getLightAt(i).getPosition(),
				level.getLightAt(i).getConstantAttenuation(),
				level.getLightAt(i).getLinearAttenuation(),
				level.getLightAt(i).getQuadraticAttenuation(),
				level.getLightAt(i).getSpotTo(),
				level.getLightAt(i).getSpotDirection(),
				level.getLightAt(i).getSpotExponent(),
				level.getLightAt(i).getSpotCutOff(),
				level.getLightAt(i).isEnabled()
			);
		}
	}

	/**
	 * Updates objects list box
	 */
	public void setObjectsListBox() {
		levelEditorScreenController.setObjectListbox(level.getObjectIdsIterator());
	}

	/**
	 * Unselect light presets
	 */
	public void unselectLightPresets() {
		levelEditorScreenController.unselectLightPresets();
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
			levelEditorModelLibraryScreenController = new LevelEditorModelLibraryScreenController();
			levelEditorModelLibraryScreenController.init();
			levelEditorScreenController = new LevelEditorScreenController(this);
			levelEditorScreenController.init();
			fileDialogScreenController = new FileDialogScreenController();
			fileDialogScreenController.init();
			infoDialogScreenController = new InfoDialogScreenController();
			infoDialogScreenController.init();
			engine.getGUI().addScreen(levelEditorModelLibraryScreenController.getScreenNode().getId(), levelEditorModelLibraryScreenController.getScreenNode());
			engine.getGUI().addScreen(levelEditorScreenController.getScreenNode().getId(), levelEditorScreenController.getScreenNode()); 
			engine.getGUI().addScreen(fileDialogScreenController.getScreenNode().getId(), fileDialogScreenController.getScreenNode());
			engine.getGUI().addScreen(infoDialogScreenController.getScreenNode().getId(), infoDialogScreenController.getScreenNode());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getModelLibrary();
		levelEditorModelLibraryScreenController.setModelLibrary(modelLibrary);

		// set up grid
		levelEditorScreenController.setGrid(gridEnabled, gridY);

		// set up map properties
		levelEditorScreenController.setMapProperties(level.getProperties(), null);

		// set up object properties presets
		levelEditorScreenController.setObjectPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());

		// set up ligh presets
		levelEditorScreenController.setLightPresetsIds(LevelPropertyPresets.getInstance().getLightPresets().keySet());

		//
		updateGUIElements();

		// lights
		Light light0 = engine.getLightAt(0);
		light0.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
		light0.getDiffuse().set(1.0f, 1.0f, 1.0f, 1.0f);
		light0.getPosition().set(0f, 20f, 0f, 1.0f);
		light0.setEnabled(true);

		// cam
		Camera cam = engine.getCamera();
		cam.setZNear(1f);
		cam.setZFar(1000f);
		camLookAt.set(level.computeCenter());

		//
		loadLevel();

		//
	}

	/**
	 * Set highlight object color effect
	 * @param object
	 */
	private void setHighlightObjectColorEffect(Entity object) {
		ObjectColor red = objectColors.get("red");
		object.getEffectColorAdd().set(red.colorAddR, red.colorAddG, red.colorAddB, 0.0f);
		object.getEffectColorMul().set(red.colorMulR, red.colorMulG, red.colorMulB, 1.0f);
	}

	/**
	 * Set standard object color effect
	 * @param object
	 */
	private void setStandardObjectColorEffect(Entity object) {
		ObjectColor color = objectColors.get("none");
		object.getEffectColorAdd().set(color.colorAddR, color.colorAddG, color.colorAddB, 0.0f);
		object.getEffectColorMul().set(color.colorMulR, color.colorMulG, color.colorMulB, 1.0f);

		// color object
		LevelEditorObject levelEditorObject = level.getObjectById(object.getId());
		if (levelEditorObject == null) return;

		// try to get object color from object properties
		PropertyModelClass colorProperty = levelEditorObject.getProperty("object.color");

		// try to get object color from model properties
		if (colorProperty == null) colorProperty = levelEditorObject.getModel().getProperty("object.color");

		// handle object color if we have any
		ObjectColor objectColor = colorProperty != null?objectColors.get(colorProperty.getValue()):null;
		if (objectColor != null) {
			object.getEffectColorAdd().set(
				object.getEffectColorAdd().getRed() + objectColor.colorAddR,
				object.getEffectColorAdd().getGreen() + objectColor.colorAddG,
				object.getEffectColorAdd().getBlue() + objectColor.colorAddB,
				0.0f
			);
			object.getEffectColorMul().set(
				object.getEffectColorMul().getRed() * objectColor.colorMulR,
				object.getEffectColorMul().getGreen() * objectColor.colorMulG,
				object.getEffectColorMul().getBlue() * objectColor.colorMulB,
				1.0f
			);
		}
	}

	/**
	 * Loads a level from internal level representation to tdme
	 */
	private void loadLevel() {
		engine.reset();
		selectedObjects.clear();
		selectedObjectsById.clear();

		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject entity = level.getObjectAt(i);
			// add to 3d engine
			Object3D object = new Object3D(entity.getId(), entity.getModel().getModel());
			object.fromTransformations(entity.getTransformations());
			object.setPickable(true);
			setStandardObjectColorEffect(object);
			engine.addEntity(object);
		}

		// load lights
		for (int i = 0; i < level.getLightCount(); i++) {
			engine.getLightAt(i).getAmbient().set(level.getLightAt(i).getAmbient());
			engine.getLightAt(i).getDiffuse().set(level.getLightAt(i).getDiffuse());
			engine.getLightAt(i).getSpecular().set(level.getLightAt(i).getSpecular());
			engine.getLightAt(i).getPosition().set(level.getLightAt(i).getPosition());
			engine.getLightAt(i).getSpotDirection().set(level.getLightAt(i).getSpotDirection());
			engine.getLightAt(i).setSpotExponent(level.getLightAt(i).getSpotExponent());
			engine.getLightAt(i).setSpotCutOff(level.getLightAt(i).getSpotCutOff());
			engine.getLightAt(i).setConstantAttenuation(level.getLightAt(i).getConstantAttenuation());
			engine.getLightAt(i).setLinearAttenuation(level.getLightAt(i).getLinearAttenuation());
			engine.getLightAt(i).setQuadraticAttenuation(level.getLightAt(i).getQuadraticAttenuation());
			engine.getLightAt(i).setEnabled(level.getLightAt(i).isEnabled());
		}

		//
		camLookAt.set(level.computeCenter());
		gridCenter.set(camLookAt);
		gridCenterLast = null;
		setObjectsListBox();
		unselectLightPresets();
		updateGrid();
	}

	/**
	 * Update dynamic grid
	 */
	private void updateGrid() {
		// check if to display grid
		if (gridEnabled == false) return;

		// TODO: use pool

		// check if to move grid
		int centerX = (int)gridCenter.getX();
		int centerZ = (int)gridCenter.getZ();
		int centerLastX = gridCenterLast == null?centerX:(int)gridCenterLast.getX();
		int centerLastZ = gridCenterLast == null?centerZ:(int)gridCenterLast.getZ();		
		if (gridCenterLast != null &&
			(centerLastX != centerX || centerLastZ != centerZ) == false) {
			return;
		}
		int gridDimensionLeft = GRID_DIMENSION_X + (centerLastX < centerX?centerX - centerLastX:0);
		int gridDimensionRight = GRID_DIMENSION_X + (centerLastX > centerX?centerLastX - centerX:0);
		int gridDimensionNear = GRID_DIMENSION_Y + (centerLastZ < centerZ?centerZ - centerLastZ:0);
		int gridDimensionFar = GRID_DIMENSION_Y + (centerLastZ > centerZ?centerLastZ - centerZ:0);
		// create ground plates
		int addedCells = 0;
		int removedCells = 0;
		int reAddedCells = 0;
		for (int gridZ = -gridDimensionNear; gridZ < gridDimensionFar; gridZ++)
		for (int gridX = -gridDimensionLeft; gridX < gridDimensionRight; gridX++) {
			String objectId = "leveleditor.ground@" + (centerX + gridX) + "," + (centerZ + gridZ);
			Entity _object = engine.getEntity(objectId);
			if (gridX < -GRID_DIMENSION_X || gridX >= GRID_DIMENSION_X ||
				gridZ < -GRID_DIMENSION_Y || gridZ >= GRID_DIMENSION_Y) {
				if (_object != null) {
					engine.removeEntity(objectId);
					removedCells++;
				}
			} else
			if (_object == null) {
				_object = selectedObjectsById.get(objectId);
				if (_object != null) {
					engine.addEntity(_object);
					reAddedCells++;
				} else {
					_object = new Object3D(objectId, levelEditorGround);
					_object.getRotations().add(new Rotation(0f, new Vector3(1f,0f,0f)));
					_object.getRotations().add(new Rotation(0f, new Vector3(0f,1f,0f)));
					_object.getRotations().add(new Rotation(0f, new Vector3(0f,0f,1f)));
					_object.getTranslation().set(
						centerX + (float)gridX * groundPlateWidth,
						gridY-0.05f,
						centerZ + (float)gridZ * groundPlateDepth);
					//_object.setDynamicShadowingEnabled(false); // TODO
					_object.setEnabled(true);
					_object.setPickable(true);
					_object.update();
					setStandardObjectColorEffect(_object);
					engine.addEntity(_object);
					addedCells++;
				}
			}
		}
		// some stats to check if its working
		// System.out.println("readded: " + reAddedCells + ", added: " + addedCells + ", removed: " + removedCells + ", total:" + engine.getEntityCount());
		if (gridCenterLast == null) gridCenterLast = new Vector3();
		gridCenterLast.set(gridCenter);
	}

	/**
	 * Remove grid
	 */
	private void removeGrid() {
		if (gridCenterLast == null) return;
		int removedCells = 0;
		int centerX = (int)gridCenterLast.getX();
		int centerZ = (int)gridCenterLast.getZ();
		for (int gridZ = -GRID_DIMENSION_Y; gridZ < GRID_DIMENSION_Y; gridZ++)
		for (int gridX = -GRID_DIMENSION_X; gridX < GRID_DIMENSION_X; gridX++) {
			String objectId = "leveleditor.ground@" + (centerX + gridX) + "," + (centerZ + gridZ);
			Entity _object = engine.getEntity(objectId);
			if (_object != null) {
				removedCells++;
				engine.removeEntity(objectId);
			}
		}
		// System.out.println("removed: " + removedCells + ", total:" + engine.getEntityCount());
		gridCenterLast = null;
	}

	/**
	 * Creates a level editor ground plate
	 * @return ground
	 */
	private Model createLevelEditorGroundPlateModel() {
		// ground selectedModel
		Model groundPlate = new Model("leveleditor.ground", "leveleditor.ground", UpVector.Y_UP, RotationOrder.XYZ);

		//	material
		Material groundPlateMaterial = new Material("ground");
		groundPlateMaterial.getDiffuseColor().setAlpha(0.75f);
		groundPlateMaterial.setDiffuseTexture("resources/tools/leveleditor/textures", "groundplate.png");
		groundPlateMaterial.getSpecularColor().set(0f, 0f, 0f, 1f);
		groundPlate.getMaterials().put("ground", groundPlateMaterial);

		//	group
		Group groundGroup = new Group(groundPlate, "ground", "ground");

		//	faces entity
		//		ground
		FacesEntity groupFacesEntityGround = new FacesEntity(groundGroup, "leveleditor.ground.facesentity");
		groupFacesEntityGround.setMaterial(groundPlateMaterial);

		//	faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntityGround);

		//	vertices
		ArrayList<Vector3> groundVertices = new ArrayList<Vector3>();
		// left, near, ground
		groundVertices.add(new Vector3(0.0f, 0.0f, 0.0f));
		// left, far, ground
		groundVertices.add(new Vector3(0.0f, 0.0f, +groundPlateDepth));
		// right far, ground
		groundVertices.add(new Vector3(+groundPlateWidth, 0.0f, +groundPlateDepth));
		// right, near, ground
		groundVertices.add(new Vector3(+groundPlateWidth, 0.0f, 0.0f));

		//	normals
		ArrayList<Vector3> groundNormals = new ArrayList<Vector3>();
		//		ground
		groundNormals.add(new Vector3(0f, 1f, 0f));

		// texture coordinates
		ArrayList<TextureCoordinate> groundTextureCoordinates = new ArrayList<TextureCoordinate>();
		groundTextureCoordinates.add(new TextureCoordinate(0f, 1f));
		groundTextureCoordinates.add(new TextureCoordinate(0f, 0f));
		groundTextureCoordinates.add(new TextureCoordinate(1f, 0f));
		groundTextureCoordinates.add(new TextureCoordinate(1f, 1f));

		//	faces ground
		ArrayList<Face> groundFacesGround = new ArrayList<Face>();
		groundFacesGround.add(new Face(groundGroup,0,1,2,0,0,0,0,1,2));
		groundFacesGround.add(new Face(groundGroup,2,3,0,0,0,0,2,3,0));

		// set up faces entity
		groupFacesEntityGround.setFaces(groundFacesGround);

		// setup ground group
		groundGroup.setVertices(groundVertices);
		groundGroup.setNormals(groundNormals);
		groundGroup.setTextureCoordinates(groundTextureCoordinates);
		groundGroup.setFacesEntities(groupFacesEntities);

		// register group
		groundPlate.getGroups().put(groundGroup.getId(), groundGroup);
		groundPlate.getSubGroups().put(groundGroup.getId(), groundGroup);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(groundPlate);

		//
		return groundPlate;
	}

	/**
	 * On object data apply
	 * @param name
	 * @param description
	 */
	public boolean objectDataApply(String name, String description) {
		if (selectedObjects.size() != 1) return false;

		// we only accept a single selection
		Entity selectedObject = selectedObjects.get(0);

		// skip if level editor internal object
		if (selectedObject.getId().startsWith("leveleditor.")) return false;

		// check if not a internal leveleditor object
		LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());

		// we need a associated object in level 
		if (levelEditorObject == null) return false;

		// we always can safely set the description
		levelEditorObject.setDescription(description);

		// did id changed
		if (levelEditorObject.getId().equals(name) == false) {
			// yep, check if name is already in use
			if (engine.getEntity(name) != null) {
				return false;
			}

			// remove from engine, level and selection
			String oldId = levelEditorObject.getId();
			level.removeObject(levelEditorObject.getId());
			engine.removeEntity(levelEditorObject.getId());
			selectedObjectsById.clear();
			selectedObjects.clear();

			// set up id
			levelEditorObject.setId(name);

			// add to level, 3d engine
			level.addObject(levelEditorObject);
			Object3D object = new Object3D(levelEditorObject.getId(), levelEditorObject.getModel().getModel());
			object.fromTransformations(levelEditorObject.getTransformations());
			object.setPickable(true);
			setStandardObjectColorEffect(object);
			setHighlightObjectColorEffect(object);
			engine.addEntity(object);

			// add to selected objects
			selectedObjects.add(object);
			selectedObjectsById.put(object.getId(), object);

			// update in objects listbox
			levelEditorScreenController.setObjectListbox(level.getObjectIdsIterator());
		}

		// set description
		levelEditorObject.setDescription(description);

		//
		return true;
	}

	/**
	 * Places selected model on selected object
	 */
	public void placeObject() {
		for(Entity selectedObject: selectedObjects) {
			placeObject(selectedObject);
		}
		level.computeDimension();
		updateGUIElements();
	}

	/**
	 * Places selected model on given object
	 */
	public void placeObject(Entity selectedObject) {
		if (selectedModel != null && selectedObject != null) {
			// get selected level entity if it is one
			LevelEditorObject selectedLevelEditorObject = level.getObjectById(selectedObject.getId());

			// create level entity
			Transformations levelEditorObjectTransformations = new Transformations();

			// take translation of selected object as base
			levelEditorObjectTransformations.getTranslation().set(selectedObject.getTranslation());

			Vector3 centerSelectedObject = 
				selectedObject.getBoundingBox().getMin().clone().add(selectedObject.getBoundingBox().getMax()).scale(0.5f);

			// compute center of selected model
			Vector3 centerNewObject = 
				selectedModel.getBoundingBox().getCenter().clone();

			// put new object on middle of selected object
			levelEditorObjectTransformations.getTranslation().add(centerNewObject.clone().add(centerSelectedObject));

			// set on selected object / y
			if (selectedLevelEditorObject == null) {
				levelEditorObjectTransformations.getTranslation().setY(
					gridY +
					-selectedModel.getBoundingBox().getMin().getY()
				);
			} else {
				// create transformed level editor object bounding box
				BoundingVolume bv = selectedLevelEditorObject.getModel().getBoundingBox().clone();
				bv.fromBoundingVolumeWithTransformations(
					selectedLevelEditorObject.getModel().getBoundingBox(),
					selectedLevelEditorObject.getTransformations()
				);

				//
				levelEditorObjectTransformations.getTranslation().setY(
					bv.computeDimensionOnAxis(new Vector3(0f,1f,0f)) / 2 + bv.getCenter().getY() +
					-selectedModel.getBoundingBox().getMin().getY()
				);
			}

			// standard scale
			levelEditorObjectTransformations.getScale().set(new Vector3(1f,1f,1f));

			// standard rotations
			levelEditorObjectTransformations.getPivot().set(selectedModel.getPivot());
			levelEditorObjectTransformations.getRotations().add(new Rotation(0f, new Vector3(1f,0f,0f)));
			levelEditorObjectTransformations.getRotations().add(new Rotation(0f, new Vector3(0f,1f,0f)));
			levelEditorObjectTransformations.getRotations().add(new Rotation(0f, new Vector3(0f,0f,1f)));
			levelEditorObjectTransformations.update();

			// check if entity already exists
			for (int i = 0; i < level.getObjectCount(); i++) {
				LevelEditorObject levelEditorObject = level.getObjectAt(i);
				if (levelEditorObject.getModel() == selectedModel &&
					levelEditorObject.getTransformations().getTranslation().equals(levelEditorObjectTransformations.getTranslation())) {
					// we already have a object with selected model on this translation
					return;
				}
			}

			// create new level editor object
			LevelEditorObject levelEditorObject = new LevelEditorObject(
				selectedModel.getName() + "_" + level.allocateObjectId(),
				"",
				levelEditorObjectTransformations,
				selectedModel
			);

			//	add to level
			level.addObject(levelEditorObject);

			// add to 3d engine
			Object3D object = new Object3D(levelEditorObject.getId(), levelEditorObject.getModel().getModel());
			object.fromTransformations(levelEditorObjectTransformations);
			object.setPickable(true);
			engine.addEntity(object);

			// add to objects listbox
			levelEditorScreenController.setObjectListbox(level.getObjectIdsIterator());
		}
	}

	/**
	 * Removes selected object
	 */
	public void removeObject() {
		ArrayList<Entity> objectsToRemove = new ArrayList<Entity>();
		for (Entity selectedObject: selectedObjects) {
			if (selectedObject != null && selectedObject.getId().startsWith("leveleditor.") == false) {
				level.removeObject(selectedObject.getId());
				engine.removeEntity(selectedObject.getId());
				objectsToRemove.add(selectedObject);
			}
		}
		for (Entity objectToRemove: objectsToRemove) {
			pasteObjects.remove(objectToRemove);
			selectedObjects.remove(objectToRemove);
			// add to objects listbox
			levelEditorScreenController.setObjectListbox(level.getObjectIdsIterator());
		}
		level.computeDimension();
		updateGUIElements();
	}

	/**
	 * Centers selected objects
	 */
	public void colorObject() {
		// skip if no objects selected
		if (selectedObjects.size() == 0) return;

		// color selected objects in blue
		for (Entity selectedObject: selectedObjects) {
			LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
			if (levelEditorObject == null) continue;
			String color = OBJECTCOLOR_NAMES[0];
			PropertyModelClass colorProperty = levelEditorObject.getProperty("object.color");
			if (colorProperty == null) {
				levelEditorObject.addProperty("object.color", color);
			} else {
				// switch color
				color = colorProperty.getValue();
				for (int i = 0; i < OBJECTCOLOR_NAMES.length; i++) {
					if (color.equalsIgnoreCase(OBJECTCOLOR_NAMES[i])) {
						color = OBJECTCOLOR_NAMES[(i + 1) % OBJECTCOLOR_NAMES.length];
						break;
					}
				}

				// set up color in object properties
				if (color.equals("none")) {
					levelEditorObject.removeProperty("object.color");
				} else {
					levelEditorObject.updateProperty(colorProperty.getName(), "object.color", color);
				}
			}
			setStandardObjectColorEffect(selectedObject);
			setHighlightObjectColorEffect(selectedObject);
		}

		// set object properties if changed
		if (selectedObjects.size() == 1) {
			Entity selectedObject = selectedObjects.get(0);
			if (selectedObject != null && selectedObject.getId().startsWith("leveleditor.") == false) {
				LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
				PropertyModelClass preset = levelEditorObject.getProperty("preset");
				levelEditorScreenController.setObjectProperties(
					preset != null?preset.getValue():"",
					levelEditorObject.getProperties(),
					null
				);
			} else {
				levelEditorScreenController.unsetObjectProperties();
			}
		} else
		if (selectedObjects.size() > 1) {
			levelEditorScreenController.unsetObjectProperties();
		}
	}

	/**
	 * Centers selected objects
	 */
	public void centerObject() {
		// skip if no objects selected
		if (selectedObjects.size() == 0) {
			camLookAt.set(0f,0f,0f);
			return;
		}

		// compute center of selected objects
		Vector3 center = new Vector3();
		for (Entity selectedObject: selectedObjects) {
			center.add(
				selectedObject.getBoundingBoxTransformed().getMin().clone().add(
					selectedObject.getBoundingBoxTransformed().getMax()
				).
				scale(0.5f)
			);
		}
		camLookAt.set(
			center.scale(1.0f / selectedObjects.size())
		);
	}

	/**
	 * Apply object translation
	 * @param x
	 * @param y
	 * @param z
	 */
	public void objectTranslationApply(float x, float y, float z) {
		if (selectedObjects.size() == 0) return;

		// handle single object
		if (selectedObjects.size() == 1) {
			Entity selectedObject = selectedObjects.get(0);
			LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
			if (currentEntity == null) return;
	
			currentEntity.getTransformations().getTranslation().set(x,y,z);
			currentEntity.getTransformations().update();
			selectedObject.fromTransformations(currentEntity.getTransformations());
		} else
		if (selectedObjects.size() > 1) {
			// multiple objects
			for (Entity selectedObject: selectedObjects) {
				LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
				if (currentEntity == null) continue;
		
				currentEntity.getTransformations().getTranslation().add(new Vector3(x,y,z));
				currentEntity.getTransformations().update();
				selectedObject.fromTransformations(currentEntity.getTransformations());				
			}
			// reset controller object properties
			levelEditorScreenController.setObject(
				new Vector3(0f,0f,0f),
				new Vector3(1f,1f,1f),
				0f,
				0f, 
				0f
			);			
		}
		level.computeDimension();
		updateGUIElements();
	}

	/**
	 * Apply object scale
	 * @param x
	 * @param y
	 * @param z
	 */
	public void objectScaleApply(float x, float y, float z) {
		if (selectedObjects.size() == 0) return;

		// handle single object
		if (selectedObjects.size() == 1) {
			Entity selectedObject = selectedObjects.get(0);
			LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
			if (currentEntity == null) return;
	
			currentEntity.getTransformations().getScale().set(x,y,z);
			currentEntity.getTransformations().update();
			selectedObject.fromTransformations(currentEntity.getTransformations());
		} else
		if (selectedObjects.size() > 1) {
			// multiple objects
			for (Entity selectedObject: selectedObjects) {
				LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
				if (currentEntity == null) continue;

				currentEntity.getTransformations().getScale().scale(new Vector3(x,y,z));
				currentEntity.getTransformations().update();
				selectedObject.fromTransformations(currentEntity.getTransformations());
			}
			// reset controller object properties
			levelEditorScreenController.setObject(
				new Vector3(0f,0f,0f),
				new Vector3(1f,1f,1f),
				0f,
				0f, 
				0f
			);
		}
		level.computeDimension();
		updateGUIElements();
	}

	/**
	 * Apply object rotations
	 * @param x
	 * @param y
	 * @param z
	 */
	public void objectRotationsApply(float x, float y, float z) {
		if (selectedObjects.size() == 0) return;

		// handle single object
		if (selectedObjects.size() == 1) {
			Entity selectedObject = selectedObjects.get(0);

			LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
			if (currentEntity == null) return;
	
			currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisXIndex()).setAngle(x);
			currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisYIndex()).setAngle(y);
			currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisZIndex()).setAngle(z);
			currentEntity.getTransformations().update();
			selectedObject.fromTransformations(currentEntity.getTransformations());
		} else
		if (selectedObjects.size() > 1) {
			// multiple objects
			for (Entity selectedObject: selectedObjects) {
				LevelEditorObject currentEntity = level.getObjectById(selectedObject.getId());
				if (currentEntity == null) continue;

				currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisXIndex()).setAngle(
					currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisXIndex()).getAngle() + x
				);
				currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisYIndex()).setAngle(
					currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisYIndex()).getAngle() + y
				);
				currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisZIndex()).setAngle(
					currentEntity.getTransformations().getRotations().get(level.getRotationOrder().getAxisZIndex()).getAngle() + z
				);
				currentEntity.getTransformations().update();
				selectedObject.fromTransformations(currentEntity.getTransformations());
			}
			// reset controller object properties
			levelEditorScreenController.setObject(
				new Vector3(0f,0f,0f),
				new Vector3(1f,1f,1f),
				0f,
				0f, 
				0f
			);
		}
		level.computeDimension();
		updateGUIElements();
	}

	/**
	 * Save a map property
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean mapPropertySave(String oldName, String name, String value) {
		// try to update property
		if (level.updateProperty(oldName, name, value) == true) {
			// reload model properties
			levelEditorScreenController.setMapProperties(
				level.getProperties(),
				name
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Add a map property
	 * @return success
	 */
	public boolean mapPropertyAdd() {
		// try to add property
		if (level.addProperty("new.property", "new.value")) {
			// reload model properties
			levelEditorScreenController.setMapProperties(
				level.getProperties(),
				"new.property"
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Remove a map property from model properties
	 * @param name
	 * @return success
	 */
	public boolean mapPropertyRemove(String name) {
		// try to remove property
		int idx = level.getPropertyIndex(name);
		if (idx != -1 && level.removeProperty(name) == true) {
			// get property first at index that was removed
			PropertyModelClass property = level.getPropertyByIndex(idx);
			if (property == null) {
				// if current index does not work, take current one -1
				property = level.getPropertyByIndex(idx - 1);
			}

			// reload model properties
			levelEditorScreenController.setMapProperties(
				level.getProperties(),
				property == null?null:property.getName()
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Remove a object property from object properties
	 * @param name
	 * @return success
	 */
	public boolean objectPropertyRemove(String name) {
		if (selectedObjects.size() != 1) return false;

		// handle single object
		Entity selectedObject = selectedObjects.get(0);
		LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
		if (levelEditorObject == null) return false;

		// try to remove property
		int idx = levelEditorObject.getPropertyIndex(name);
		if (idx != -1 && levelEditorObject.removeProperty(name) == true) {
			// get property first at index that was removed
			PropertyModelClass property = levelEditorObject.getPropertyByIndex(idx);
			if (property == null) {
				// if current index does not work, take current one -1
				property = levelEditorObject.getPropertyByIndex(idx - 1);
			}

			// reload model properties
			levelEditorScreenController.setObjectProperties(
				null,
				levelEditorObject.getProperties(),
				property == null?null:property.getName()
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Apply object property preset
	 * @param preset id
	 */
	public void objectPropertiesPreset(String presetId) {
		if (selectedObjects.size() != 1) return;

		// handle single object
		Entity selectedObject = selectedObjects.get(0);
		LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
		if (levelEditorObject == null) return;

		// clear object properties
		levelEditorObject.clearProperties();

		// add object properties by preset if missing
		ArrayList<PropertyModelClass> objectPropertyPresetVector = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (objectPropertyPresetVector != null) {
			for (PropertyModelClass objectPropertyPreset: objectPropertyPresetVector) {
				levelEditorObject.addProperty(objectPropertyPreset.getName(), objectPropertyPreset.getValue());
			}
		}

		// update object properties to gui
		levelEditorScreenController.setObjectProperties(
			presetId,
			levelEditorObject.getProperties(),
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
	public boolean objectPropertySave(String oldName, String name, String value) {
		if (selectedObjects.size() != 1) return false;

		// handle single object
		Entity selectedObject = selectedObjects.get(0);
		LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
		if (levelEditorObject == null) return false;

		// try to update property
		if (levelEditorObject.updateProperty(oldName, name, value) == true) {
			// reload model properties
			levelEditorScreenController.setObjectProperties(
				null,
				levelEditorObject.getProperties(),
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
	public boolean objectPropertyAdd() {
		if (selectedObjects.size() != 1) return false;

		// handle single object
		Entity selectedObject = selectedObjects.get(0);
		LevelEditorObject levelEditorObject = level.getObjectById(selectedObject.getId());
		if (levelEditorObject == null) return false;

		// try to add property
		if (levelEditorObject.addProperty("new.property", "new.value")) {
			// reload model properties
			levelEditorScreenController.setObjectProperties(
				null,
				levelEditorObject.getProperties(),
				"new.property"
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Triggers loading a map
	 */
	public void loadMap(String path, String file) {
		selectedModel = null;
		try {
			// export level from DAE if requested
			if (file.toLowerCase().endsWith(".dae") == true) {
				LevelEditorLevel daeLevel = DAEReader.readLevel(path, file);
				// adjust file name
				file+= ".tl";
			}

			// do import
			LevelFileImport.doImport(
				MODEL_ROOT,
				GAME_ROOT,
				path,
				file,
				level
			);

			// set up map properties
			levelEditorScreenController.setMapProperties(level.getProperties(), null);
			levelEditorScreenController.unsetObjectProperties();
			levelEditorScreenController.unsetObject();
			loadLevel();
			reloadModelLibrary = true;
			updateGUIElements();
		} catch (Exception exception) {
			exception.printStackTrace();
			levelEditorScreenController.showErrorPopUp("Warning: Could not load level file", exception.getMessage());
		}
	}

	/**
	 * Triggers saving a map
	 */
	public void saveMap(String pathName, String fileName) {
		File levelFile = new File(pathName, fileName);
		LevelFileExport.export(levelFile.getAbsolutePath(), level);
		updateGUIElements();
	}

	/**
	 * Copy current selected objects
	 */
	private void copyObjects() {
		pasteObjects.clear();
		for (Entity selectedObject: selectedObjects) {
			if (selectedObject != null && selectedObject.getId().startsWith("leveleditor.") == false) {
				pasteObjects.add(selectedObject);
			}
		}
	}

	/**
	 * Paste objects
	 */
	private void pasteObjects() {
		// determine top left of paste objects
		float pasteObjectsMinX = Float.MAX_VALUE;
		float pasteObjectsMinZ = Float.MAX_VALUE;
		float pasteObjectsMinY = Float.MIN_VALUE;
		for (Entity object: pasteObjects) {
			float[] objectBBMinXYZ = object.getBoundingBoxTransformed().getMin().getArray();
			if (objectBBMinXYZ[0] < pasteObjectsMinX) pasteObjectsMinX = objectBBMinXYZ[0];
			if (objectBBMinXYZ[1] < pasteObjectsMinY) pasteObjectsMinY = objectBBMinXYZ[1];
			if (objectBBMinXYZ[2] < pasteObjectsMinZ) pasteObjectsMinZ = objectBBMinXYZ[2];
		}

		// determine top left of selected objects
		float selectedObjectsMinX = Float.MAX_VALUE;
		float selectedObjectsMinZ = Float.MAX_VALUE;
		float selectedObjectsMaxY = Float.MIN_VALUE;
		for (Entity object: selectedObjects) {
			float[] objectBBMinXYZ = object.getBoundingBoxTransformed().getMin().getArray();
			float[] objectBBMaxXYZ = object.getBoundingBoxTransformed().getMax().getArray();
			if (objectBBMinXYZ[0] < selectedObjectsMinX) selectedObjectsMinX = objectBBMinXYZ[0];
			if (objectBBMaxXYZ[1] > selectedObjectsMaxY) selectedObjectsMaxY = objectBBMaxXYZ[1];
			if (objectBBMinXYZ[2] < selectedObjectsMinZ) selectedObjectsMinZ = objectBBMinXYZ[2];
		}

		// paste objects
		for (Entity pasteObject: pasteObjects) {
			// get selected level entity if it is one
			LevelEditorObject selectedLevelEditorObject = level.getObjectById(pasteObject.getId());
			LevelEditorModel pasteModel = selectedLevelEditorObject.getModel();

			// create level entity, copy transformations from original
			Transformations levelEditorObjectTransformations = new Transformations();
			levelEditorObjectTransformations.fromTransformations(selectedLevelEditorObject.getTransformations());

			// compute new translation
			float objectDiffX = selectedLevelEditorObject.getTransformations().getTranslation().getX() - pasteObjectsMinX;
			float objectDiffY = selectedLevelEditorObject.getTransformations().getTranslation().getY() - pasteObjectsMinY;
			float objectDiffZ = selectedLevelEditorObject.getTransformations().getTranslation().getZ() - pasteObjectsMinZ;
			levelEditorObjectTransformations.getTranslation().setX(selectedObjectsMinX + objectDiffX);
			levelEditorObjectTransformations.getTranslation().setY(selectedObjectsMaxY + objectDiffY);
			levelEditorObjectTransformations.getTranslation().setZ(selectedObjectsMinZ + objectDiffZ);
			levelEditorObjectTransformations.update();			

			// check if entity already exists
			for (int i = 0; i < level.getObjectCount(); i++) {
				LevelEditorObject levelEditorObject = level.getObjectAt(i);
				if (levelEditorObject.getModel() == pasteModel &&
					levelEditorObject.getTransformations().getTranslation().equals(levelEditorObjectTransformations.getTranslation())) {
					// we already have a object with selected model on this translation
					return;
				}
			}

			// create new level editor object
			LevelEditorObject levelEditorObject = new LevelEditorObject(
				pasteModel.getName() + "_" + level.allocateObjectId(),
				"",
				levelEditorObjectTransformations,
				pasteModel
			);

			// copy properties
			for (PropertyModelClass property: selectedLevelEditorObject.getProperties()) {
				levelEditorObject.addProperty(property.getName(), property.getValue());
			}

			//	add to level
			level.addObject(levelEditorObject);

			// add to 3d engine
			Object3D object = new Object3D(levelEditorObject.getId(), levelEditorObject.getModel().getModel());
			object.fromTransformations(levelEditorObjectTransformations);
			object.setPickable(true);
			engine.addEntity(object);

			// add to objects listbox
			levelEditorScreenController.setObjectListbox(level.getObjectIdsIterator());
		}
	}

	/**
	 * Compute spot direction
	 * @param i
	 */
	public void computeSpotDirection(int i, Vector4 position, Vector3 spotTo) {
		Vector3 _from = new Vector3(position.getArray());
		Vector3 spotDirection = spotTo.clone().sub(_from);

		// set up in level light
		level.getLightAt(i).getPosition().set(
			position.getX(),
			position.getY(),
			position.getZ(),
			position.getW()
		);
		level.getLightAt(i).getSpotTo().set(
			spotTo.getX(),
			spotTo.getY(),
			spotTo.getZ()
		);
		level.getLightAt(i).getSpotDirection().set(
			spotDirection.getX(),
			spotDirection.getY(),
			spotDirection.getZ()
		);
	
		// set up in engine light
		engine.getLightAt(i).getPosition().set(
			position.getX(),
			position.getY(),
			position.getZ(),
			position.getW()
		);
		engine.getLightAt(i).getSpotDirection().set(
			spotDirection.getX(),
			spotDirection.getY(),
			spotDirection.getZ()
		);

		// set light in controller
		levelEditorScreenController.setLight(
			i,
			level.getLightAt(i).getAmbient(),
			level.getLightAt(i).getDiffuse(),
			level.getLightAt(i).getSpecular(),
			level.getLightAt(i).getPosition(),
			level.getLightAt(i).getConstantAttenuation(),
			level.getLightAt(i).getLinearAttenuation(),
			level.getLightAt(i).getQuadraticAttenuation(),
			level.getLightAt(i).getSpotTo(),
			level.getLightAt(i).getSpotDirection(),
			level.getLightAt(i).getSpotExponent(),
			level.getLightAt(i).getSpotCutOff(),
			level.getLightAt(i).isEnabled()
		);
	}

	/**
	 * Apply light with index i
	 * @param i
	 * @param ambient
	 * @param diffuse
	 * @param position
	 * @param constant attenuation
	 * @param linear attenuation
	 * @param quadratic attenuation
	 * @param spot to
	 * @param spot direction
	 * @param spot exponent
	 * @param spot cutoff
	 * @param enabled
	 */
	public void applyLight(int i,
		Color4 ambient, Color4 diffuse, Color4 specular,
		Vector4 position, float constantAttenuation, float linearAttenuation, float quadraticAttenuation,
		Vector3 spotTo, Vector3 spotDirection,
		float spotExponent, float spotCutoff, boolean enabled) {

		// set up light in level
		level.getLightAt(i).getAmbient().set(
			ambient.getRed(),
			ambient.getGreen(),
			ambient.getBlue(),
			ambient.getAlpha()
		);
		level.getLightAt(i).getDiffuse().set(
			diffuse.getRed(),
			diffuse.getGreen(),
			diffuse.getBlue(),
			diffuse.getAlpha()
		);
		level.getLightAt(i).getSpecular().set(
			specular.getRed(),
			specular.getGreen(),
			specular.getBlue(),
			specular.getAlpha()
		);
		level.getLightAt(i).getPosition().set(
			position.getX(),
			position.getY(),
			position.getZ(),
			position.getW()
		);
		level.getLightAt(i).setConstantAttenuation(constantAttenuation);
		level.getLightAt(i).setLinearAttenuation(linearAttenuation);
		level.getLightAt(i).setQuadraticAttenuation(quadraticAttenuation);
		level.getLightAt(i).getSpotTo().set(
			spotTo.getX(),
			spotTo.getY(),
			spotTo.getZ()
		);
		level.getLightAt(i).getSpotDirection().set(
			spotDirection.getX(),
			spotDirection.getY(),
			spotDirection.getZ()
		);
		level.getLightAt(i).setSpotExponent(spotExponent);
		level.getLightAt(i).setSpotCutOff(spotCutoff);
		level.getLightAt(i).setEnabled(enabled);

		// set up light in engine
		engine.getLightAt(i).getAmbient().set(
			ambient.getRed(),
			ambient.getGreen(),
			ambient.getBlue(),
			ambient.getAlpha()
		);
		engine.getLightAt(i).getDiffuse().set(
			diffuse.getRed(),
			diffuse.getGreen(),
			diffuse.getBlue(),
			diffuse.getAlpha()
		);
		engine.getLightAt(i).getSpecular().set(
			specular.getRed(),
			specular.getGreen(),
			specular.getBlue(),
			specular.getAlpha()
		);
		engine.getLightAt(i).getPosition().set(
			position.getX(),
			position.getY(),
			position.getZ(),
			position.getW()
		);
		engine.getLightAt(i).setConstantAttenuation(constantAttenuation);
		engine.getLightAt(i).setLinearAttenuation(linearAttenuation);
		engine.getLightAt(i).setQuadraticAttenuation(quadraticAttenuation);
		engine.getLightAt(i).getSpotDirection().set(
			spotDirection.getX(),
			spotDirection.getY(),
			spotDirection.getZ()
		);
		engine.getLightAt(i).setSpotExponent(spotExponent);
		engine.getLightAt(i).setSpotCutOff(spotCutoff);
		engine.getLightAt(i).setEnabled(enabled);

		// set light in controller
		levelEditorScreenController.setLight(
			i,
			level.getLightAt(i).getAmbient(),
			level.getLightAt(i).getDiffuse(),
			level.getLightAt(i).getSpecular(),
			level.getLightAt(i).getPosition(),
			level.getLightAt(i).getConstantAttenuation(),
			level.getLightAt(i).getLinearAttenuation(),
			level.getLightAt(i).getQuadraticAttenuation(),
			level.getLightAt(i).getSpotTo(),
			level.getLightAt(i).getSpotDirection(),
			level.getLightAt(i).getSpotExponent(),
			level.getLightAt(i).getSpotCutOff(),
			level.getLightAt(i).isEnabled()
		);
	}

}
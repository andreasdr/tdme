package net.drewke.tdme.tools.leveleditor.views;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIKeyboardEvent.Type;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.controller.TriggerScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME Model Viewer View
 * @author Andreas Drewke
 * @version $Id$
 */
public class TriggerView extends View implements GUIInputEventHandler {

	protected Engine engine;

	private PopUps popUps;
	private TriggerScreenController triggerScreenController;

	private LevelEditorModel model;
	private boolean initModelRequested;

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

	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public TriggerView(PopUps popUps) {
		this.popUps = popUps;
		triggerScreenController = null;
		initModelRequested = false;
		model = null;
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

		// set up model in engine
		Tools.setupModel(model, engine, lookFromRotations, scale);

		// Make model screenshot
		Tools.oseThumbnail(drawable, model);

		// add model
		maxAxisDimension = Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(model.getModel()));

		// set up engine object settings
		Entity model = engine.getEntity("model");
		Entity ground = engine.getEntity("ground");
		model.setDynamicShadowingEnabled(false);
		ground.setEnabled(false);
		Entity modelBoundingVolume = engine.getEntity("model_bv");
		if (modelBoundingVolume != null) {
			modelBoundingVolume.setEnabled(false);
		}

		// 
		updateGUIElements();
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

		// do GUI
		engine.getGUI().render();
		engine.getGUI().handleEvents();
	}

	/**
	 * Init GUI elements
	 */
	public void updateGUIElements() {
		if (model != null) {
			triggerScreenController.setScreenCaption("Trigger - " + model.getName());
			PropertyModelClass preset = model.getProperty("preset");
			triggerScreenController.setModelProperties(preset != null ? preset.getValue() : null, model.getProperties(), null);
			triggerScreenController.setModelData(model.getName(), model.getDescription());

			// trigger
			Vector3 dimension = new Vector3();
			dimension.set(((BoundingBox)model.getBoundingVolume()).getMax());
			dimension.sub(((BoundingBox)model.getBoundingVolume()).getMin());
			triggerScreenController.setTrigger(dimension.getX(), dimension.getY(), dimension.getZ());
		} else {
			triggerScreenController.setScreenCaption("Trigger - no trigger loaded");
			triggerScreenController.unsetModelProperties();
			triggerScreenController.unsetModelData();
			triggerScreenController.unsetTrigger();
		}
	}

	/**
	 * Shutdown
	 */
	public void dispose(GLAutoDrawable drawable) {
		Engine.getInstance().reset();
	}

	/**
	 * Trigger apply
	 * @param width
	 * @param height
	 * @param depth
	 */
	public void triggerApply(float width, float height, float depth) {
		if (model == null) return;

		// create new trigger, replacing old with new
		try {
			// save reference to old model, create new model
			LevelEditorModel oldModel = model;
			model = TDMELevelEditor.getInstance().getModelLibrary().createTrigger(	
				LevelEditorModelLibrary.ID_ALLOCATE,
				oldModel.getName(),
				oldModel.getDescription(),
				width,
				height,
				depth,
				new Vector3()
			);

			// clone properties
			for (int i = 0; i < oldModel.getPropertyCount(); i++) {
				PropertyModelClass property = oldModel.getPropertyByIndex(i);
				model.addProperty(property.getName(), property.getValue());
			}

			// replace old with new
			TDMELevelEditor.getInstance().getLevel().replaceModel(oldModel.getId(), model.getId());
			TDMELevelEditor.getInstance().getModelLibrary().removeModel(oldModel.getId());
			TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().setModelLibrary();
	
			// init model
			initModelRequested = true;

			//
			updateGUIElements();
		} catch (Exception exception) {
			popUps.getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
		}
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
			triggerScreenController = new TriggerScreenController(this);
			triggerScreenController.init();
			engine.getGUI().addScreen(triggerScreenController.getScreenNode().getId(), triggerScreenController.getScreenNode());
			triggerScreenController.getScreenNode().setInputEventHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up gui
		updateGUIElements();

		//
		engine.getGUI().resetRenderScreens();
		engine.getGUI().addRenderScreen(triggerScreenController.getScreenNode().getId());
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());
	}

}
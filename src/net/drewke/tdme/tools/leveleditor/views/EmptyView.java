package net.drewke.tdme.tools.leveleditor.views;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.controller.EmptyScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.CameraRotationInputHandler;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * Empty View
 * @author Andreas Drewke
 * @version $Id$
 */
public class EmptyView extends View implements GUIInputEventHandler {

	protected Engine engine;

	private PopUps popUps;
	private EmptyScreenController emptyScreenController;

	private LevelEditorEntity entity;
	private boolean initModelRequested;

	private CameraRotationInputHandler cameraRotationInputHandler;
	
	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public EmptyView(PopUps popUps) {
		this.popUps = popUps;
		emptyScreenController = null;
		initModelRequested = false;
		entity = null;

		// offscreen engine transformations
		engine = Engine.getInstance();

		// camera rotation input handler
		cameraRotationInputHandler = new CameraRotationInputHandler(engine);
	}

	/**
	 * @return pop up views
	 */
	public PopUps getPopUpsViews() {
		return popUps;
	}

	/**
	 * @return entity
	 */
	public LevelEditorEntity getEntity() {
		return entity;
	}

	/**
	 * @return selected entity
	 */
	public void setEntity(LevelEditorEntity entity) {
		this.entity = entity;
		initModelRequested = true;
	}

	/**
	 * Init entity
	 */
	protected void initModel(GLAutoDrawable drawable) {
		if (entity == null) return;

		// set up entity in engine
		Tools.setupModel(entity, engine, cameraRotationInputHandler.getLookFromRotations(), cameraRotationInputHandler.getScale());

		// Make entity screenshot
		Tools.oseThumbnail(drawable, entity);

		// max axis dimension
		cameraRotationInputHandler.setMaxAxisDimension(Tools.computeMaxAxisDimension(entity.getModel().getBoundingBox()));

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
		cameraRotationInputHandler.handleInputEvents();
	}

	/**
	 * Renders the scene 
	 */
	public void display(GLAutoDrawable drawable) {
		// init entity
		if (initModelRequested == true) {
			initModel(drawable);
			cameraRotationInputHandler.reset();
			initModelRequested = false;
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
			emptyScreenController.setScreenCaption("Empty - " + entity.getName());
			PropertyModelClass preset = entity.getProperty("preset");
			emptyScreenController.setEntityProperties(preset != null ? preset.getValue() : null, entity.getProperties(), null);
			emptyScreenController.setEntityData(entity.getName(), entity.getDescription());

			// trigger
			Vector3 dimension = new Vector3();
			dimension.set(entity.getModel().getBoundingBox().getMax());
			dimension.sub(entity.getModel().getBoundingBox().getMin());
		} else {
			emptyScreenController.setScreenCaption("Empty - no trigger loaded");
			emptyScreenController.unsetEntityProperties();
			emptyScreenController.unsetEntityData();
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
		// reset engine and partition
		engine.reset();
		engine.setPartition(new PartitionNone());

		//
		try {
			emptyScreenController = new EmptyScreenController(this);
			emptyScreenController.init();
			engine.getGUI().addScreen(emptyScreenController.getScreenNode().getId(), emptyScreenController.getScreenNode());
			emptyScreenController.getScreenNode().setInputEventHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up gui
		updateGUIElements();

		//
		engine.getGUI().resetRenderScreens();
		engine.getGUI().addRenderScreen(emptyScreenController.getScreenNode().getId());
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());
	}

}
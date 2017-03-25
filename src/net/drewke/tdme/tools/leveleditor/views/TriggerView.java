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
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.CameraRotationInputHandler;
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

	private LevelEditorEntity entity;
	private boolean initModelRequested;

	private CameraRotationInputHandler cameraRotationInputHandler;
	
	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public TriggerView(PopUps popUps) {
		this.popUps = popUps;
		triggerScreenController = null;
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
		cameraRotationInputHandler.setMaxAxisDimension(Tools.computeMaxAxisDimension(Engine.getModelBoundingBox(entity.getModel())));

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
			triggerScreenController.setScreenCaption("Trigger - " + entity.getName());
			PropertyModelClass preset = entity.getProperty("preset");
			triggerScreenController.setEntityProperties(preset != null ? preset.getValue() : null, entity.getProperties(), null);
			triggerScreenController.setEntityData(entity.getName(), entity.getDescription());

			// trigger
			Vector3 dimension = new Vector3();
			dimension.set(((BoundingBox)entity.getBoundingVolume()).getMax());
			dimension.sub(((BoundingBox)entity.getBoundingVolume()).getMin());
			triggerScreenController.setTrigger(dimension.getX(), dimension.getY(), dimension.getZ());
		} else {
			triggerScreenController.setScreenCaption("Trigger - no trigger loaded");
			triggerScreenController.unsetEntityProperties();
			triggerScreenController.unsetEntityData();
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
		if (entity == null) return;

		// create new trigger, replacing old with new
		try {
			// save reference to old entity, create new entity
			LevelEditorEntity oldModel = entity;
			entity = TDMELevelEditor.getInstance().getEntityLibrary().addTrigger(	
				LevelEditorEntityLibrary.ID_ALLOCATE,
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
				entity.addProperty(property.getName(), property.getValue());
			}

			// replace old with new
			TDMELevelEditor.getInstance().getLevel().replaceEntity(oldModel.getId(), entity.getId());
			TDMELevelEditor.getInstance().getEntityLibrary().removeEntity(oldModel.getId());
			TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
	
			// init entity
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
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());
	}

}
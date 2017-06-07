package net.drewke.tdme.tools.shared.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.controller.ModelViewerScreenController;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.utils.Console;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME Model Viewer View
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelViewerView implements View, GUIInputEventHandler {

	protected Engine engine;

	private PopUps popUps;
	private ModelViewerScreenController modelViewerScreenController;

	private EntityDisplayView entityDisplayView;
	private EntityBoundingVolumeView entityBoundingVolumeView;

	private LevelEditorEntity entity;
	private boolean loadModelRequested;
	private boolean initModelRequested;
	private File modelFile;

	private CameraRotationInputHandler cameraRotationInputHandler;

	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public ModelViewerView(PopUps popUps) {
		this.popUps = popUps;
		engine = Engine.getInstance();
		modelViewerScreenController = null;
		entityDisplayView = null;
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
		modelFile = new File(entity.getEntityFileName() != null?entity.getEntityFileName():entity.getFileName());

		// set up model in engine
		Tools.setupEntity(entity, engine, cameraRotationInputHandler.getLookFromRotations(), cameraRotationInputHandler.getScale());

		// Make model screenshot
		Tools.oseThumbnail(drawable, entity);

		// max axis dimension
		cameraRotationInputHandler.setMaxAxisDimension(Tools.computeMaxAxisDimension(entity.getModel().getBoundingBox()));

		// set up model statistics
		ModelUtilities.ModelStatistics stats = ModelUtilities.computeModelStatistics(entity.getModel());
		modelViewerScreenController.setStatistics(stats.getOpaqueFaceCount(), stats.getTransparentFaceCount(), stats.getMaterialCount());

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
		ModelMetaDataFileExport.export(pathName, fileName, entity);
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
			loadModel();
			cameraRotationInputHandler.reset();
		}

		// init model
		if (initModelRequested == true) {
			engine.reset();
			initModel(drawable);
			initModelRequested = false;
		}

		// delegate to entity display view
		entityDisplayView.display(entity);

		// do GUI
		engine.getGUI().render();
		engine.getGUI().handleEvents();
	}

	/**
	 * Init GUI elements
	 */
	public void updateGUIElements() {
		if (entity != null) {
			modelViewerScreenController.setScreenCaption("Model Viewer - " + (entity.getEntityFileName() != null?Tools.getFileName(entity.getEntityFileName()):Tools.getFileName(entity.getFileName())));
			PropertyModelClass preset = entity.getProperty("preset");
			modelViewerScreenController.setEntityProperties(preset != null ? preset.getValue() : null, entity.getProperties(), null);
			modelViewerScreenController.setEntityData(entity.getName(), entity.getDescription());
			modelViewerScreenController.setPivot(entity.getPivot());
			entityBoundingVolumeView.setBoundingVolumes(entity);
		} else {
			modelViewerScreenController.setScreenCaption("Model Viewer - no entity loaded");
			modelViewerScreenController.unsetEntityProperties();
			modelViewerScreenController.unsetEntityData();
			modelViewerScreenController.unsetPivot();
			entityBoundingVolumeView.unsetBoundingVolumes();
		}
	}

	/**
	 * On init additional screens
	 * @param drawable
	 */
	public void onInitAdditionalScreens() {
	}

	/**
	 * Load settings
	 */
	private void loadSettings() {
		// read settings
		FileInputStream fis = null;
		Object tmp;
		try {
			fis = new FileInputStream("./settings/modelviewer.properties");
			Properties settings = new Properties();
			settings.load(fis);
			entityDisplayView.setDisplayBoundingVolume((tmp = settings.get("display.boundingvolumes")) != null?tmp.equals("true") == true:false);
			entityDisplayView.setDisplayGroundPlate((tmp = settings.get("display.groundplate")) != null?tmp.equals("true") == true:false);
			entityDisplayView.setDisplayShadowing((tmp = settings.get("display.shadowing")) != null?tmp.equals("true") == true:false); 
			modelViewerScreenController.getModelPath().setPath((tmp = settings.get("model.path")) != null?tmp.toString():"");
			fis.close();
		} catch (Exception ioe) {
			if (fis != null) try { fis.close(); } catch (IOException ioeInner) {}
			ioe.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.View#initialize()
	 */
	public void initialize() {
		//
		try {
			modelViewerScreenController = new ModelViewerScreenController(this);
			modelViewerScreenController.initialize();
			entityDisplayView = modelViewerScreenController.getEntityDisplaySubScreenController().getView();
			entityBoundingVolumeView = modelViewerScreenController.getEntityBoundingVolumeSubScreenController().getView();
			engine.getGUI().addScreen(modelViewerScreenController.getScreenNode().getId(), modelViewerScreenController.getScreenNode());
			modelViewerScreenController.getScreenNode().setInputEventHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// load settings
		loadSettings();

		// set up display
		modelViewerScreenController.getEntityDisplaySubScreenController().setupDisplay();

		//
		entityBoundingVolumeView.initialize();

		// set up gui
		updateGUIElements();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.View#activate()
	 */
	public void activate() {
		// reset engine and partition
		engine.reset();
		engine.setPartition(new PartitionNone());

		//
		engine.getGUI().resetRenderScreens();
		engine.getGUI().addRenderScreen(modelViewerScreenController.getScreenNode().getId());
		onInitAdditionalScreens();
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());
	}

	/**
	 * Store settings
	 */
	private void storeSettings() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("./settings/modelviewer.properties");
			Properties settings = new Properties();
			settings.put("display.boundingvolumes", entityDisplayView.isDisplayBoundingVolume() == true?"true":"false");
			settings.put("display.groundplate", entityDisplayView.isDisplayGroundPlate() == true?"true":"false");
			settings.put("display.shadowing", entityDisplayView.isDisplayShadowing() == true?"true":"false");
			settings.put("model.path", modelViewerScreenController.getModelPath().getPath());
			settings.store(fos, null);
			fos.close();
		} catch (Exception ioe) {
			if (fos != null) try { fos.close(); } catch (IOException ioeInner) {}
			ioe.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.View#deactivate()
	 */
	public void deactivate() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.View#dispose()
	 */
	public void dispose() {
		// store settings
		storeSettings();
		// reset engine
		Engine.getInstance().reset();
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
		Console.println("Model file: " + modelFile);

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
			LevelEditorEntity levelEditorEntity = new LevelEditorEntity(
				LevelEditorEntity.ID_NONE,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				null,
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
			LevelEditorEntity levelEditorEntity = new LevelEditorEntity(
				LevelEditorEntity.ID_NONE,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				null,
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
	  * On set entity data hook
	  */
	public void onSetEntityData() {
	}

}

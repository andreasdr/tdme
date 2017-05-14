package net.drewke.tdme.tools.shared.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.controller.ParticleSystemScreenController;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileExport;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * TDME particle system view
 * @author Andreas Drewke
 * @version $Id$
 */
public class ParticleSystemView extends View implements GUIInputEventHandler {

	protected Engine engine;

	private PopUps popUps;
	private ParticleSystemScreenController particleSystemScreenController;

	private EntityDisplayView entityDisplayView;
	private EntityBoundingVolumeView entityBoundingVolumeView;

	private LevelEditorEntity entity;
	private boolean loadParticleSystemRequested;
	private boolean initParticleSystemRequested;
	private File particleSystemFile;

	private CameraRotationInputHandler cameraRotationInputHandler;

	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public ParticleSystemView(PopUps popUps) {
		this.popUps = popUps;
		engine = Engine.getInstance();
		particleSystemScreenController = null;
		entityDisplayView = null;
		loadParticleSystemRequested = false;
		initParticleSystemRequested = false;
		particleSystemFile = null;
		cameraRotationInputHandler = new CameraRotationInputHandler(engine);
		entity = new LevelEditorEntity(
			-1, 
			LevelEditorEntity.EntityType.PARTICLESYSTEM, 
			"Untitled", 
			"", 
			"Untitled.tps", 
			null, 
			null, 
			null,
			new Vector3()
		);
		entity.setDefaultBoundingVolumes();
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
		initParticleSystemRequested = true;
	}

	/**
	 * Init particle system
	 */
	public void initParticleSystem() {
		initParticleSystemRequested = true;
	}

	/**
	 * Init particle system
	 */
	protected void initParticleSystem(GLAutoDrawable drawable) {
		if (entity == null) return;

		//
		particleSystemFile = entity.getEntityFileName() != null?new File(entity.getEntityFileName()):null;

		// set up model in engine
		Tools.setupEntity(entity, engine, cameraRotationInputHandler.getLookFromRotations(), cameraRotationInputHandler.getScale());

		// Make model screenshot
		Tools.oseThumbnail(drawable, entity);

		// max axis dimension
		BoundingBox boundingBox = null;
		if (entity.getModel() == null) {
			boundingBox = new BoundingBox(new Vector3(-0.5f, 0f, -0.5f), new Vector3(0.5f, 3f, 0.5f));
		} else {
			boundingBox = entity.getModel().getBoundingBox();
		}
		cameraRotationInputHandler.setMaxAxisDimension(Tools.computeMaxAxisDimension(boundingBox));

		// 
		updateGUIElements();
	}

	/**
	 * @return current particle system file name
	 */
	public String getFileName() {
		if (particleSystemFile == null) return "";
		return particleSystemFile.getName();
	}

	/**
	 * Issue particle system loading
	 */
	public void loadFile(String pathName, String fileName) {
		loadParticleSystemRequested = true;
		particleSystemFile = new File(pathName, fileName);
	}

	/**
	 * Triggers saving a particle system
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		ModelMetaDataFileExport.export(new File(pathName, fileName).getCanonicalPath(), entity);
	}

	/**
	 * Issue file reloading
	 */
	public void reloadFile() {
		loadParticleSystemRequested = true;
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
		if (loadParticleSystemRequested == true) {
			initParticleSystemRequested = true;
			loadParticleSystemRequested = false;
			loadParticleSystem();
			cameraRotationInputHandler.reset();
		}

		// init model
		if (initParticleSystemRequested == true) {
			engine.reset();
			initParticleSystem(drawable);
			particleSystemScreenController.setParticleSystemType();
			particleSystemScreenController.setParticleSystemEmitter();
			initParticleSystemRequested = false;
		}

		// emit and update
		ParticleSystemEntity particleSystemEntity = (ParticleSystemEntity)engine.getEntity("model");
		if (particleSystemEntity != null && particleSystemEntity.isAutoEmit() == false)  {
			particleSystemEntity.emitParticles();
			particleSystemEntity.updateParticles();
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
			particleSystemScreenController.setScreenCaption("Particle System - " + (entity.getEntityFileName() != null?Tools.getFileName(entity.getEntityFileName()):entity.getName()));
			PropertyModelClass preset = entity.getProperty("preset");
			particleSystemScreenController.setEntityProperties(preset != null ? preset.getValue() : null, entity.getProperties(), null);
			particleSystemScreenController.setEntityData(entity.getName(), entity.getDescription());
			entityBoundingVolumeView.setBoundingVolumes(entity);
		} else {
			particleSystemScreenController.setScreenCaption("Particle System - no entity loaded");
			particleSystemScreenController.unsetEntityProperties();
			particleSystemScreenController.unsetEntityData();
			entityBoundingVolumeView.unsetBoundingVolumes();
		}
	}

	/**
	 * Store settings
	 */
	private void storeSettings() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("./settings/particlesystem.properties");
			Properties settings = new Properties();
			settings.put("display.boundingvolumes", entityDisplayView.isDisplayBoundingVolume() == true?"true":"false");
			settings.put("display.groundplate", entityDisplayView.isDisplayGroundPlate() == true?"true":"false");
			settings.put("display.shadowing", entityDisplayView.isDisplayShadowing() == true?"true":"false");
			settings.put("particlesystem.path", particleSystemScreenController.getParticleSystemPath().getPath());
			settings.put("model.path", particleSystemScreenController.getModelPath().getPath());
			settings.store(fos, null);
			fos.close();
		} catch (Exception ioe) {
			if (fos != null) try { fos.close(); } catch (IOException ioeInner) {}
			ioe.printStackTrace();
		}
	}

	/**
	 * Shutdown
	 */
	public void dispose(GLAutoDrawable drawable) {
		// store settings
		storeSettings();
		// reset engine
		Engine.getInstance().reset();
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
			fis = new FileInputStream("./settings/particlesystem.properties");
			Properties settings = new Properties();
			settings.load(fis);
			entityDisplayView.setDisplayBoundingVolume((tmp = settings.get("display.boundingvolumes")) != null?tmp.equals("true") == true:false);
			entityDisplayView.setDisplayGroundPlate((tmp = settings.get("display.groundplate")) != null?tmp.equals("true") == true:false);
			entityDisplayView.setDisplayShadowing((tmp = settings.get("display.shadowing")) != null?tmp.equals("true") == true:false); 
			particleSystemScreenController.getParticleSystemPath().setPath((tmp = settings.get("particlesystem.path")) != null?tmp.toString():"");
			particleSystemScreenController.getModelPath().setPath((tmp = settings.get("model.path")) != null?tmp.toString():"");
			fis.close();
		} catch (Exception ioe) {
			if (fis != null) try { fis.close(); } catch (IOException ioeInner) {}
			ioe.printStackTrace();
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
			particleSystemScreenController = new ParticleSystemScreenController(this);
			particleSystemScreenController.init();
			entityDisplayView = particleSystemScreenController.getEntityDisplaySubScreenController().getView();
			entityBoundingVolumeView = particleSystemScreenController.getEntityBoundingVolumeSubScreenController().getView();
			engine.getGUI().addScreen(particleSystemScreenController.getScreenNode().getId(), particleSystemScreenController.getScreenNode());
			particleSystemScreenController.getScreenNode().setInputEventHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// load settings
		loadSettings();

		// set up display
		particleSystemScreenController.getEntityDisplaySubScreenController().setupDisplay();

		// init entity bounding volume view
		entityBoundingVolumeView.init();

		// particle system types
		ArrayList<String> particleSystemTypes = new ArrayList<String>();
		particleSystemTypes.add("None");
		particleSystemTypes.add("Object Particle System");
		particleSystemTypes.add("Points Particle System");
		particleSystemScreenController.setParticleSystemTypes(particleSystemTypes);

		// particle system types
		ArrayList<String> particleSystemEmitters = new ArrayList<String>();
		particleSystemEmitters.add("None");
		particleSystemEmitters.add("Point Particle Emitter");
		particleSystemEmitters.add("BoundingBox Particle Emitter");
		particleSystemEmitters.add("Circle Particle Emitter");
		particleSystemEmitters.add("Circle Particle Emitter Plane Velocity");
		particleSystemEmitters.add("Sphere Particle Emitter");
		particleSystemScreenController.setParticleSystemEmitters(particleSystemEmitters);

		// set up gui
		updateGUIElements();

		//
		engine.getGUI().resetRenderScreens();
		engine.getGUI().addRenderScreen(particleSystemScreenController.getScreenNode().getId());
		onInitAdditionalScreens();
		engine.getGUI().addRenderScreen(popUps.getFileDialogScreenController().getScreenNode().getId());
		engine.getGUI().addRenderScreen(popUps.getInfoDialogScreenController().getScreenNode().getId());

		// init particle system
		initParticleSystemRequested = true;
	}

	/**
	 * On load particle system
	 * @param old entity
	 * @oaram entity
	 */
	public void onLoadParticleSystem(LevelEditorEntity oldEntity, LevelEditorEntity entity) {
	}

	/**
	 * Load a particle system
	 */
	private void loadParticleSystem() {
		//
		System.out.println("Particle system file: " + particleSystemFile);

		// scene
		try {
			LevelEditorEntity oldEntity = entity;

			// add entity to library
			entity = loadParticleSystem(
				particleSystemFile.getName(),
				"",
				particleSystemFile.getParentFile().getAbsolutePath(),
				particleSystemFile.getName()
			);
			onLoadParticleSystem(oldEntity, entity);
		} catch (Exception exception) {
			popUps.getInfoDialogScreenController().show("Warning", exception.getMessage());
		}
	}

	/**
	 * Load particle system
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @return level editor entity
	 * @throws Exception
	 */
	protected LevelEditorEntity loadParticleSystem(String name, String description, String pathName, String fileName) throws Exception {
		if (fileName.toLowerCase().endsWith(".tps")) {
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
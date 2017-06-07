package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.BoundingBoxParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitterPlaneVelocity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.PointParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.SphereParticleEmitter;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ParticleSystemView;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.MutableString;

/**
 * Model viewer screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ParticleSystemScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private final static String TYPE_NONE = "None";
	private final static String TYPE_OBJECTPARTICLESYSTEM = "Object Particle System";
	private final static String TYPE_POINTSPARTICLESYSTEM = "Points Particle System";

	private final static String EMITTER_NONE = "None";
	private final static String EMITTER_POINTPARTICLEEMITTER = "Point Particle Emitter";
	private final static String EMITTER_BOUNDINGBOXPARTICLEEMITTER = "BoundingBox Particle Emitter";
	private final static String EMITTER_CIRCLEPARTICLEEMITTER = "Circle Particle Emitter";
	private final static String EMITTER_CIRCLEPARTICLEEMITTERPLANEVELOCITY = "Circle Particle Emitter Plane Velocity";
	private final static String EMITTER_SPHEREPARTICLEEMITTER = "Sphere Particle Emitter";

	private EntityBaseSubScreenController entityBaseSubScreenController;
	private EntityDisplaySubScreenController entityDisplaySubScreenController;
	private EntityBoundingVolumeSubScreenController entityBoundingVolumeSubScreenController;

	private final ParticleSystemView view;

	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode particleSystemReload;
	private GUIElementNode particleSystemSave;

	private GUIElementNode particleSystemTypes;
	private GUIElementNode particleSystemType;

	private GUIElementNode particleSystemEmitters;
	private GUIElementNode particleSystemEmitter;

	private GUIElementNode opsScale;
	private GUIElementNode opsMaxCount;
	private GUIElementNode opsModel;
	private GUIElementNode opsAutoEmit;
	private GUIElementNode ppsAutoEmit;

	private GUIElementNode ppsMaxPoints;

	private GUIElementNode ppeCount;
	private GUIElementNode ppeLifeTime;
	private GUIElementNode ppeLifeTimeRnd;
	private GUIElementNode ppeMass;
	private GUIElementNode ppeMassRnd;
	private GUIElementNode ppePosition;
	private GUIElementNode ppeVelocity;
	private GUIElementNode ppeVelocityRnd;
	private GUIElementNode ppeColorStart;
	private GUIElementNode ppeColorEnd;

	private GUIElementNode bbpeCount;
	private GUIElementNode bbpeLifeTime;
	private GUIElementNode bbpeLifeTimeRnd;
	private GUIElementNode bbpeMass;
	private GUIElementNode bbpeMassRnd;
	private GUIElementNode bbpeVelocity;
	private GUIElementNode bbpeVelocityRnd;
	private GUIElementNode bbpeColorStart;
	private GUIElementNode bbpeColorEnd;
	private GUIElementNode bbpeObbCenter;
	private GUIElementNode bbpeObbHalfextension;
	private GUIElementNode bbpeObbRotationX;
	private GUIElementNode bbpeObbRotationY;
	private GUIElementNode bbpeObbRotationZ;

	private GUIElementNode cpeCount;
	private GUIElementNode cpeLifeTime;
	private GUIElementNode cpeLifeTimeRnd;
	private GUIElementNode cpeMass;
	private GUIElementNode cpeMassRnd;
	private GUIElementNode cpeVelocity;
	private GUIElementNode cpeVelocityRnd;
	private GUIElementNode cpeColorStart;
	private GUIElementNode cpeColorEnd;
	private GUIElementNode cpeCenter;
	private GUIElementNode cpeRadius;
	private GUIElementNode cpeRotationX;
	private GUIElementNode cpeRotationY;
	private GUIElementNode cpeRotationZ;

	private GUIElementNode cpepvCount;
	private GUIElementNode cpepvLifeTime;
	private GUIElementNode cpepvLifeTimeRnd;
	private GUIElementNode cpepvMass;
	private GUIElementNode cpepvMassRnd;
	private GUIElementNode cpepvVelocity;
	private GUIElementNode cpepvVelocityRnd;
	private GUIElementNode cpepvColorStart;
	private GUIElementNode cpepvColorEnd;
	private GUIElementNode cpepvCenter;
	private GUIElementNode cpepvRadius;
	private GUIElementNode cpepvRotationX;
	private GUIElementNode cpepvRotationY;
	private GUIElementNode cpepvRotationZ;

	private GUIElementNode speCount;
	private GUIElementNode speLifeTime;
	private GUIElementNode speLifeTimeRnd;
	private GUIElementNode speMass;
	private GUIElementNode speMassRnd;
	private GUIElementNode speVelocity;
	private GUIElementNode speVelocityRnd;
	private GUIElementNode speColorStart;
	private GUIElementNode speColorEnd;
	private GUIElementNode speCenter;
	private GUIElementNode speRadius;

	private MutableString value;

	private FileDialogPath particleSystemPath;
	private FileDialogPath modelPath;

	/**
	 * Public constructor
	 * @param view
	 */
	public ParticleSystemScreenController(ParticleSystemView view) {
		this.particleSystemPath = new FileDialogPath(".");
		this.modelPath = new FileDialogPath(".");
		this.view = view;
		final ParticleSystemView finalView = view;
		this.entityBaseSubScreenController = new EntityBaseSubScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				finalView.updateGUIElements();
				finalView.onSetEntityData();
			}
		});
		this.entityDisplaySubScreenController = new EntityDisplaySubScreenController();
		this.entityBoundingVolumeSubScreenController = new EntityBoundingVolumeSubScreenController(view.getPopUpsViews(), particleSystemPath);
		this.value = new MutableString();
	}

	/**
	 * @return entity display sub screen controller
	 */
	public EntityDisplaySubScreenController getEntityDisplaySubScreenController() {
		return entityDisplaySubScreenController;
	}

	/**
	 * @return entity bounding volume sub screen controller
	 */
	public EntityBoundingVolumeSubScreenController getEntityBoundingVolumeSubScreenController() {
		return entityBoundingVolumeSubScreenController;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return particle system path
	 */
	public FileDialogPath getParticleSystemPath() {
		return particleSystemPath;
	}

	/**
	 * @return model path
	 */
	public FileDialogPath getModelPath() {
		return modelPath;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void initialize() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/particlesystem/gui", "screen_particlesystem.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			particleSystemReload = (GUIElementNode)screenNode.getNodeById("button_entity_reload");
			particleSystemSave = (GUIElementNode)screenNode.getNodeById("button_entity_save");
			particleSystemTypes = (GUIElementNode)screenNode.getNodeById("ps_types");
			particleSystemType = (GUIElementNode)screenNode.getNodeById("ps_type");
			particleSystemEmitters = (GUIElementNode)screenNode.getNodeById("ps_emitters");
			particleSystemEmitter = (GUIElementNode)screenNode.getNodeById("ps_emitter");

			// object particle system
			opsScale = (GUIElementNode)screenNode.getNodeById("ops_scale");
			opsMaxCount = (GUIElementNode)screenNode.getNodeById("ops_maxcount");
			opsModel = (GUIElementNode)screenNode.getNodeById("ops_model");
			opsAutoEmit = (GUIElementNode)screenNode.getNodeById("ops_auto_emit");

			// point particle system
			ppsMaxPoints = (GUIElementNode)screenNode.getNodeById("pps_maxpoints");
			ppsAutoEmit = (GUIElementNode)screenNode.getNodeById("pps_auto_emit");

			// point particle emitter
			ppeCount = (GUIElementNode)screenNode.getNodeById("ppe_count");
			ppeLifeTime = (GUIElementNode)screenNode.getNodeById("ppe_lifetime");
			ppeLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("ppe_lifetimernd");
			ppeMass = (GUIElementNode)screenNode.getNodeById("ppe_mass");
			ppeMassRnd = (GUIElementNode)screenNode.getNodeById("ppe_massrnd");
			ppePosition = (GUIElementNode)screenNode.getNodeById("ppe_position");
			ppeVelocity = (GUIElementNode)screenNode.getNodeById("ppe_velocity");
			ppeVelocityRnd = (GUIElementNode)screenNode.getNodeById("ppe_velocityrnd");
			ppeColorStart = (GUIElementNode)screenNode.getNodeById("ppe_colorstart");
			ppeColorEnd = (GUIElementNode)screenNode.getNodeById("ppe_colorend");

			// bounding box particle emitter
			bbpeCount = (GUIElementNode)screenNode.getNodeById("bbpe_count");
			bbpeLifeTime = (GUIElementNode)screenNode.getNodeById("bbpe_lifetime");
			bbpeLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("bbpe_lifetimernd");
			bbpeMass = (GUIElementNode)screenNode.getNodeById("bbpe_mass");
			bbpeMassRnd = (GUIElementNode)screenNode.getNodeById("bbpe_massrnd");
			bbpeVelocity = (GUIElementNode)screenNode.getNodeById("bbpe_velocity");
			bbpeVelocityRnd = (GUIElementNode)screenNode.getNodeById("bbpe_velocityrnd");
			bbpeColorStart = (GUIElementNode)screenNode.getNodeById("bbpe_colorstart");
			bbpeColorEnd = (GUIElementNode)screenNode.getNodeById("bbpe_colorend");
			bbpeObbCenter = (GUIElementNode)screenNode.getNodeById("bbpe_obb_center");
			bbpeObbHalfextension = (GUIElementNode)screenNode.getNodeById("bbpe_obb_halfextension");
			bbpeObbRotationX = (GUIElementNode)screenNode.getNodeById("bbpe_obb_rotation_x");
			bbpeObbRotationY = (GUIElementNode)screenNode.getNodeById("bbpe_obb_rotation_y");
			bbpeObbRotationZ = (GUIElementNode)screenNode.getNodeById("bbpe_obb_rotation_z");

			// circle particle emitter
			cpeCount = (GUIElementNode)screenNode.getNodeById("cpe_count");
			cpeLifeTime = (GUIElementNode)screenNode.getNodeById("cpe_lifetime");
			cpeLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("cpe_lifetimernd");
			cpeMass = (GUIElementNode)screenNode.getNodeById("cpe_mass");
			cpeMassRnd = (GUIElementNode)screenNode.getNodeById("cpe_massrnd");
			cpeVelocity = (GUIElementNode)screenNode.getNodeById("cpe_velocity");
			cpeVelocityRnd = (GUIElementNode)screenNode.getNodeById("cpe_velocityrnd");
			cpeColorStart = (GUIElementNode)screenNode.getNodeById("cpe_colorstart");
			cpeColorEnd = (GUIElementNode)screenNode.getNodeById("cpe_colorend");
			cpeCenter = (GUIElementNode)screenNode.getNodeById("cpe_center");
			cpeRadius = (GUIElementNode)screenNode.getNodeById("cpe_radius");
			cpeRotationX = (GUIElementNode)screenNode.getNodeById("cpe_rotation_x");
			cpeRotationY = (GUIElementNode)screenNode.getNodeById("cpe_rotation_y");
			cpeRotationZ = (GUIElementNode)screenNode.getNodeById("cpe_rotation_z");

			// circle particle emitter plane velocity
			cpepvCount = (GUIElementNode)screenNode.getNodeById("cpepv_count");
			cpepvLifeTime = (GUIElementNode)screenNode.getNodeById("cpepv_lifetime");
			cpepvLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("cpepv_lifetimernd");
			cpepvMass = (GUIElementNode)screenNode.getNodeById("cpepv_mass");
			cpepvMassRnd = (GUIElementNode)screenNode.getNodeById("cpepv_massrnd");
			cpepvVelocity = (GUIElementNode)screenNode.getNodeById("cpepv_velocity");
			cpepvVelocityRnd = (GUIElementNode)screenNode.getNodeById("cpepv_velocityrnd");
			cpepvColorStart = (GUIElementNode)screenNode.getNodeById("cpepv_colorstart");
			cpepvColorEnd = (GUIElementNode)screenNode.getNodeById("cpepv_colorend");
			cpepvCenter = (GUIElementNode)screenNode.getNodeById("cpepv_center");
			cpepvRadius = (GUIElementNode)screenNode.getNodeById("cpepv_radius");
			cpepvRotationX = (GUIElementNode)screenNode.getNodeById("cpepv_rotation_x");
			cpepvRotationY = (GUIElementNode)screenNode.getNodeById("cpepv_rotation_y");
			cpepvRotationZ = (GUIElementNode)screenNode.getNodeById("cpepv_rotation_z");

			// sphere particle emitter
			speCount = (GUIElementNode)screenNode.getNodeById("spe_count");
			speLifeTime = (GUIElementNode)screenNode.getNodeById("spe_lifetime");
			speLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("spe_lifetimernd");
			speMass = (GUIElementNode)screenNode.getNodeById("spe_mass");
			speMassRnd = (GUIElementNode)screenNode.getNodeById("spe_massrnd");
			speVelocity = (GUIElementNode)screenNode.getNodeById("spe_velocity");
			speVelocityRnd = (GUIElementNode)screenNode.getNodeById("spe_velocityrnd");
			speColorStart = (GUIElementNode)screenNode.getNodeById("spe_colorstart");
			speColorEnd = (GUIElementNode)screenNode.getNodeById("spe_colorend");
			speCenter = (GUIElementNode)screenNode.getNodeById("spe_center");
			speRadius = (GUIElementNode)screenNode.getNodeById("spe_radius");

			// done
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init entity base sub screen controller
		entityBaseSubScreenController.initialize(screenNode);

		// init display sub screen controller
		entityDisplaySubScreenController.initialize(screenNode);

		// init bounding volume sub screen controller
		entityBoundingVolumeSubScreenController.initialize(screenNode);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Set screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getText().set(text);
		screenNode.layout(screenCaption);
	}

	/**
	  * Set up general entity data
	  * @param name
	  * @param description
	  */
	public void setEntityData(String name, String description) {
		entityBaseSubScreenController.setEntityData(name, description);
		particleSystemReload.getController().setDisabled(false);
		particleSystemSave.getController().setDisabled(false);
	}

	/**
	 * Unset entity data
	 */
	public void unsetEntityData() {
		entityBaseSubScreenController.unsetEntityData();
		particleSystemReload.getController().setDisabled(true);
		particleSystemSave.getController().setDisabled(true);
	}

	/**
	 * Set up entity properties
	 * @param preset id
	 * @param entity properties
	 * @param selected name
	 */
	public void setEntityProperties(String presetId, Iterable<PropertyModelClass> entityProperties, String selectedName) {
		entityBaseSubScreenController.setEntityProperties(view.getEntity(), presetId, entityProperties, selectedName);
	}

	/**
 	 * Unset entity properties
	 */
	public void unsetEntityProperties() {
		entityBaseSubScreenController.unsetEntityProperties();
	}

	/**
	 * Set up particle system types
	 * @param particle system types
	 */
	public void setParticleSystemTypes(ArrayList<String> particleSystemTypesCollection) {
		// particle system types inner node
		GUIParentNode particleSystemTypesInnerNode = (GUIParentNode)(particleSystemTypes.getScreenNode().getNodeById(particleSystemTypes.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 0;
		String particleSystemTypesInnerNodeSubNodesXML = "";
		particleSystemTypesInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + particleSystemTypes.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100\">\n";
		for (String particleSystem: particleSystemTypesCollection) {
			particleSystemTypesInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(particleSystem) + "\" value=\"" + GUIParser.escapeQuotes(particleSystem) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}
		particleSystemTypesInnerNodeSubNodesXML+= "</scrollarea-vertical>";

		// inject sub nodes
		try {
			particleSystemTypesInnerNode.replaceSubNodes(
				particleSystemTypesInnerNodeSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set up emitters
	 * @param emitters
	 */
	public void setParticleSystemEmitters(ArrayList<String> emittersCollection) {
		// particle system emitters inner node
		GUIParentNode particleSystemEmittersInnerNode = (GUIParentNode)(particleSystemEmitters.getScreenNode().getNodeById(particleSystemEmitters.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 0;
		String particleSystemEmittersInnerNodeSubNodesXML = "";
		particleSystemEmittersInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + particleSystemEmitters.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100\">\n";
		for (String particleSystemEmitter: emittersCollection) {
			particleSystemEmittersInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(particleSystemEmitter) + "\" value=\"" + GUIParser.escapeQuotes(particleSystemEmitter) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}
		particleSystemEmittersInnerNodeSubNodesXML+= "</scrollarea-vertical>";

		// inject sub nodes
		try {
			particleSystemEmittersInnerNode.replaceSubNodes(
				particleSystemEmittersInnerNodeSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
	}

	/**
	 * Set particle system type
	 */
	public void setParticleSystemType() {
		LevelEditorEntityParticleSystem particleSystem = view.getEntity().getParticleSystem();
		particleSystemType.getActiveConditions().removeAll();
		switch (particleSystem.getType()) {
			case NONE:
				particleSystemTypes.getController().setValue(value.set(TYPE_NONE));
				particleSystemType.getActiveConditions().add(TYPE_NONE);
				break;
			case OBJECT_PARTICLE_SYSTEM:
				particleSystemTypes.getController().setValue(value.set(TYPE_OBJECTPARTICLESYSTEM));
				particleSystemType.getActiveConditions().add(TYPE_OBJECTPARTICLESYSTEM);
				opsMaxCount.getController().setValue(value.set(particleSystem.getObjectParticleSystem().getMaxCount()));
				opsScale.getController().setValue(value.set(Tools.formatVector3(particleSystem.getObjectParticleSystem().getScale())));
				opsModel.getController().setValue(value.set(particleSystem.getObjectParticleSystem().getModelFile()));
				opsAutoEmit.getController().setValue(value.set(particleSystem.getObjectParticleSystem().isAutoEmit() == true?"1":""));
				break;
			case POINT_PARTICLE_SYSTEM:
				particleSystemTypes.getController().setValue(value.set(TYPE_POINTSPARTICLESYSTEM));
				particleSystemType.getActiveConditions().add(TYPE_POINTSPARTICLESYSTEM);
				ppsMaxPoints.getController().setValue(value.set(particleSystem.getPointParticleSystem().getMaxPoints()));
				ppsAutoEmit.getController().setValue(value.set(particleSystem.getPointParticleSystem().isAutoEmit() == true?"1":""));
				break;
			default:
				Console.println("ParticleSystemScreenController::setParticleSystemType(): unknown particle system type '" + particleSystem.getType() + "'");
				break;
		}

		// re init entity in view
		view.initParticleSystem();
	}

	/**
	 * Set particle system type
	 */
	public void onParticleSystemTypeDataApply() {
		try {
			LevelEditorEntityParticleSystem particleSystem = view.getEntity().getParticleSystem();
			switch (particleSystem.getType()) {
				case NONE:
					break;
				case OBJECT_PARTICLE_SYSTEM:
					particleSystem.getObjectParticleSystem().setMaxCount(Tools.convertToInt(opsMaxCount.getController().getValue().toString()));
					particleSystem.getObjectParticleSystem().getScale().set(Tools.convertToVector3(opsScale.getController().getValue().toString()));
					particleSystem.getObjectParticleSystem().setAutoEmit(opsAutoEmit.getController().getValue().equals("1"));
					try {
						particleSystem.getObjectParticleSystem().setModelFile(opsModel.getController().getValue().toString());
					} catch (Exception exception) {
						view.getPopUpsViews().getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
					}
					break;
				case POINT_PARTICLE_SYSTEM:
					particleSystem.getPointParticleSystem().setMaxPoints(Tools.convertToInt(ppsMaxPoints.getController().getValue().toString()));
					particleSystem.getPointParticleSystem().setAutoEmit(ppsAutoEmit.getController().getValue().equals("1"));
					break;
				default:
					Console.println("ParticleSystemScreenController::setParticleSystemType(): unknown particle system type '" + particleSystem.getType() + "'");
					break;
			}
		} catch (NumberFormatException exception) {
			showErrorPopUp("Warning", "Invalid number entered");
		}

		// re init entity in view
		view.initParticleSystem();
	}

	/**
	 * On particle system type apply
	 */
	public void onParticleSystemTypeApply() {
		String particleSystemTypeString = particleSystemTypes.getController().getValue().toString();
		particleSystemType.getActiveConditions().removeAll();
		particleSystemType.getActiveConditions().add(particleSystemTypeString);
		if (particleSystemTypeString.equals(TYPE_NONE) == true) {
			view.getEntity().getParticleSystem().setType(LevelEditorEntityParticleSystem.Type.NONE);
		} else
		if (particleSystemTypeString.equals(TYPE_OBJECTPARTICLESYSTEM) == true) {
			view.getEntity().getParticleSystem().setType(LevelEditorEntityParticleSystem.Type.OBJECT_PARTICLE_SYSTEM);
		} else
		if (particleSystemTypeString.equals(TYPE_POINTSPARTICLESYSTEM) == true) {
			view.getEntity().getParticleSystem().setType(LevelEditorEntityParticleSystem.Type.POINT_PARTICLE_SYSTEM);
		} else {
			Console.println("ParticleSystemScreenController::onParticleSystemTypeApply(): unknown particle system type '" + particleSystemTypeString + "'");
		}

		//
		setParticleSystemType();

		// re init entity in view
		view.initParticleSystem();
	}

	/**
	 * On particle system emitter apply
	 */
	public void onParticleSystemEmitterApply() {
		LevelEditorEntityParticleSystem particleSystem = view.getEntity().getParticleSystem();
		String particleSystemEmitterString = particleSystemEmitters.getController().getValue().toString();
		particleSystemEmitter.getActiveConditions().removeAll();
		particleSystemEmitter.getActiveConditions().add(particleSystemEmitterString);

		// set new emitter
		if (particleSystemEmitterString.equals(EMITTER_NONE) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.NONE);
		} else
		if (particleSystemEmitterString.equals(EMITTER_POINTPARTICLEEMITTER) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.POINT_PARTICLE_EMITTER);
		} else
		if (particleSystemEmitterString.equals(EMITTER_BOUNDINGBOXPARTICLEEMITTER) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.BOUNDINGBOX_PARTICLE_EMITTER);
		} else
		if (particleSystemEmitterString.equals(EMITTER_CIRCLEPARTICLEEMITTER) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.CIRCLE_PARTICLE_EMITTER);
		} else
		if (particleSystemEmitterString.equals(EMITTER_CIRCLEPARTICLEEMITTERPLANEVELOCITY) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY);
		} else
		if (particleSystemEmitterString.equals(EMITTER_SPHEREPARTICLEEMITTER) == true) {
			particleSystem.setEmitter(LevelEditorEntityParticleSystem.Emitter.SPHERE_PARTICLE_EMITTER);
		} else {
			Console.println("ParticleSystemScreenController::onParticleSystemEmitterApply(): unknown particle system emitter '" + particleSystemEmitterString + "'");
		}

		//
		setParticleSystemEmitter();
	}

	/**
	 * Set particle system emitter
	 */
	public void onParticleSystemEmitterDataApply() {
		try {
			LevelEditorEntityParticleSystem particleSystem = view.getEntity().getParticleSystem();
			switch (particleSystem.getEmitter()) {
				case NONE:
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_NONE));
						break;
					}
				case POINT_PARTICLE_EMITTER:
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_POINTPARTICLEEMITTER));
						PointParticleEmitter emitter = particleSystem.getPointParticleEmitter();
						emitter.setCount(Tools.convertToInt(ppeCount.getController().getValue().toString()));
						emitter.setLifeTime(Tools.convertToInt(ppeLifeTime.getController().getValue().toString()));
						emitter.setLifeTimeRnd(Tools.convertToInt(ppeLifeTimeRnd.getController().getValue().toString()));
						emitter.setMass(Tools.convertToFloat(ppeMass.getController().getValue().toString()));
						emitter.setMassRnd(Tools.convertToFloat(ppeMassRnd.getController().getValue().toString()));
						emitter.getPosition().set(Tools.convertToVector3(ppePosition.getController().getValue().toString()));
						emitter.getVelocity().set(Tools.convertToVector3(ppeVelocity.getController().getValue().toString()));
						emitter.getVelocityRnd().set(Tools.convertToVector3(ppeVelocityRnd.getController().getValue().toString()));
						emitter.getColorStart().set(Tools.convertToColor4(ppeColorStart.getController().getValue().toString()));
						emitter.getColorEnd().set(Tools.convertToColor4(ppeColorEnd.getController().getValue().toString()));
						break;
					}
				case BOUNDINGBOX_PARTICLE_EMITTER:
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_BOUNDINGBOXPARTICLEEMITTER));
						BoundingBoxParticleEmitter emitter = particleSystem.getBoundingBoxParticleEmitters();
						emitter.setCount(Tools.convertToInt(bbpeCount.getController().getValue().toString()));
						emitter.setLifeTime(Tools.convertToInt(bbpeLifeTime.getController().getValue().toString()));
						emitter.setLifeTimeRnd(Tools.convertToInt(bbpeLifeTimeRnd.getController().getValue().toString()));
						emitter.setMass(Tools.convertToFloat(bbpeMass.getController().getValue().toString()));
						emitter.setMassRnd(Tools.convertToFloat(bbpeMassRnd.getController().getValue().toString()));
						emitter.getVelocity().set(Tools.convertToVector3(bbpeVelocity.getController().getValue().toString()));
						emitter.getVelocityRnd().set(Tools.convertToVector3(bbpeVelocityRnd.getController().getValue().toString()));
						emitter.getColorStart().set(Tools.convertToColor4(bbpeColorStart.getController().getValue().toString()));
						emitter.getColorEnd().set(Tools.convertToColor4(bbpeColorEnd.getController().getValue().toString()));
						emitter.getObbCenter().set(Tools.convertToVector3(bbpeObbCenter.getController().getValue().toString()));
						emitter.getObbHalfextension().set(Tools.convertToVector3(bbpeObbHalfextension.getController().getValue().toString()));

						// rotation axes by rotation angle for x,y,z
						Transformations rotations = new Transformations();
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(bbpeObbRotationZ.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Z));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(bbpeObbRotationY.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Y));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(bbpeObbRotationX.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_X));
						rotations.update();

						// extract axes from matrix
						rotations.getTransformationsMatrix().getAxes(emitter.getObbAxis0(), emitter.getObbAxis1(), emitter.getObbAxis2());

						// done
						break;
					}
				case CIRCLE_PARTICLE_EMITTER:
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_CIRCLEPARTICLEEMITTER));
						CircleParticleEmitter emitter = particleSystem.getCircleParticleEmitter();
						emitter.setCount(Tools.convertToInt(cpeCount.getController().getValue().toString()));
						emitter.setLifeTime(Tools.convertToInt(cpeLifeTime.getController().getValue().toString()));
						emitter.setLifeTimeRnd(Tools.convertToInt(cpeLifeTimeRnd.getController().getValue().toString()));
						emitter.setMass(Tools.convertToFloat(cpeMass.getController().getValue().toString()));
						emitter.setMassRnd(Tools.convertToFloat(cpeMassRnd.getController().getValue().toString()));
						emitter.getVelocity().set(Tools.convertToVector3(cpeVelocity.getController().getValue().toString()));
						emitter.getVelocityRnd().set(Tools.convertToVector3(cpeVelocityRnd.getController().getValue().toString()));
						emitter.getColorStart().set(Tools.convertToColor4(cpeColorStart.getController().getValue().toString()));
						emitter.getColorEnd().set(Tools.convertToColor4(cpeColorEnd.getController().getValue().toString()));
						emitter.getCenter().set(Tools.convertToVector3(cpeCenter.getController().getValue().toString()));
						emitter.setRadius(Tools.convertToFloat(cpeRadius.getController().getValue().toString()));

						// rotation axes by rotation angle for x,y,z
						Transformations rotations = new Transformations();
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpeRotationZ.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Z));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpeRotationY.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Y));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpeRotationX.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_X));
						rotations.update();

						// extract axes from matrix
						rotations.getTransformationsMatrix().getAxes(emitter.getAxis0(), new Vector3(), emitter.getAxis1());

						// done
						break;
					}
				case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY: 
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_CIRCLEPARTICLEEMITTERPLANEVELOCITY));
						CircleParticleEmitterPlaneVelocity emitter = particleSystem.getCircleParticleEmitterPlaneVelocity();
						emitter.setCount(Tools.convertToInt(cpepvCount.getController().getValue().toString()));
						emitter.setLifeTime(Tools.convertToInt(cpepvLifeTime.getController().getValue().toString()));
						emitter.setLifeTimeRnd(Tools.convertToInt(cpepvLifeTimeRnd.getController().getValue().toString()));
						emitter.setMass(Tools.convertToFloat(cpepvMass.getController().getValue().toString()));
						emitter.setMassRnd(Tools.convertToFloat(cpepvMassRnd.getController().getValue().toString()));
						emitter.setVelocity(Tools.convertToFloat(cpepvVelocity.getController().getValue().toString()));
						emitter.setVelocityRnd(Tools.convertToFloat(cpepvVelocityRnd.getController().getValue().toString()));
						emitter.getColorStart().set(Tools.convertToColor4(cpepvColorStart.getController().getValue().toString()));
						emitter.getColorEnd().set(Tools.convertToColor4(cpepvColorEnd.getController().getValue().toString()));
						emitter.getCenter().set(Tools.convertToVector3(cpepvCenter.getController().getValue().toString()));
						emitter.setRadius(Tools.convertToFloat(cpepvRadius.getController().getValue().toString()));

						// rotation axes by rotation angle for x,y,z
						Transformations rotations = new Transformations();
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpepvRotationZ.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Z));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpepvRotationY.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Y));
						rotations.getRotations().add(new Rotation(Tools.convertToFloat(cpepvRotationX.getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_X));
						rotations.update();

						// extract axes from matrix
						rotations.getTransformationsMatrix().getAxes(emitter.getAxis0(), new Vector3(), emitter.getAxis1());

						// done
						break;
					}
				case SPHERE_PARTICLE_EMITTER:
					{
						particleSystemEmitters.getController().setValue(value.set(EMITTER_SPHEREPARTICLEEMITTER));
						SphereParticleEmitter emitter = particleSystem.getSphereParticleEmitter();
						emitter.setCount(Tools.convertToInt(speCount.getController().getValue().toString()));
						emitter.setLifeTime(Tools.convertToInt(speLifeTime.getController().getValue().toString()));
						emitter.setLifeTimeRnd(Tools.convertToInt(speLifeTimeRnd.getController().getValue().toString()));
						emitter.setMass(Tools.convertToFloat(speMass.getController().getValue().toString()));
						emitter.setMassRnd(Tools.convertToFloat(speMassRnd.getController().getValue().toString()));
						emitter.getVelocity().set(Tools.convertToVector3(speVelocity.getController().getValue().toString()));
						emitter.getVelocityRnd().set(Tools.convertToVector3(speVelocityRnd.getController().getValue().toString()));
						emitter.getColorStart().set(Tools.convertToColor4(speColorStart.getController().getValue().toString()));
						emitter.getColorEnd().set(Tools.convertToColor4(speColorEnd.getController().getValue().toString()));
						emitter.getCenter().set(Tools.convertToVector3(speCenter.getController().getValue().toString()));
						emitter.setRadius(Tools.convertToFloat(speRadius.getController().getValue().toString()));
						break;
					}
				default:
					Console.println("ParticleSystemScreenController::onParticleSystemEmitterApply(): unknown particle system emitter '" + particleSystem.getEmitter() + "'");
			}
		} catch (NumberFormatException exception) {
			showErrorPopUp("Warning", "Invalid number entered");
		}

		// re init entity in view
		view.initParticleSystem();
	}

	/**
	 * Set particle system emitter
	 */
	public void setParticleSystemEmitter() {
		particleSystemEmitter.getActiveConditions().removeAll();
		LevelEditorEntityParticleSystem particleSystem = view.getEntity().getParticleSystem();
		switch (particleSystem.getEmitter()) {
			case NONE:
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_NONE));
					particleSystemEmitter.getActiveConditions().add(EMITTER_NONE);
					break;
				}
			case POINT_PARTICLE_EMITTER:
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_POINTPARTICLEEMITTER));
					particleSystemEmitter.getActiveConditions().add(EMITTER_POINTPARTICLEEMITTER);
					PointParticleEmitter emitter = particleSystem.getPointParticleEmitter();
					ppeCount.getController().setValue(value.set(emitter.getCount()));
					ppeLifeTime.getController().setValue(value.set((int)emitter.getLifeTime()));
					ppeLifeTimeRnd.getController().setValue(value.set((int)emitter.getLifeTimeRnd()));
					ppeMass.getController().setValue(value.set(emitter.getMass(), 4));
					ppeMassRnd.getController().setValue(value.set(emitter.getMassRnd(), 4));
					ppePosition.getController().setValue(value.set(Tools.formatVector3(emitter.getPosition())));
					ppeVelocity.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocity())));
					ppeVelocityRnd.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocityRnd())));
					ppeColorStart.getController().setValue(value.set(Tools.formatColor4(emitter.getColorStart())));
					ppeColorEnd.getController().setValue(value.set(Tools.formatColor4(emitter.getColorEnd())));
					break;
				}
			case BOUNDINGBOX_PARTICLE_EMITTER:
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_BOUNDINGBOXPARTICLEEMITTER));
					particleSystemEmitter.getActiveConditions().add(EMITTER_BOUNDINGBOXPARTICLEEMITTER);
					BoundingBoxParticleEmitter emitter = particleSystem.getBoundingBoxParticleEmitters();
					bbpeCount.getController().setValue(value.set(emitter.getCount()));
					bbpeLifeTime.getController().setValue(value.set((int)emitter.getLifeTime()));
					bbpeLifeTimeRnd.getController().setValue(value.set((int)emitter.getLifeTimeRnd()));
					bbpeMass.getController().setValue(value.set(emitter.getMass(), 4));
					bbpeMassRnd.getController().setValue(value.set(emitter.getMassRnd(), 4));
					bbpeVelocity.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocity())));
					bbpeVelocityRnd.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocityRnd())));
					bbpeColorStart.getController().setValue(value.set(Tools.formatColor4(emitter.getColorStart())));
					bbpeColorEnd.getController().setValue(value.set(Tools.formatColor4(emitter.getColorEnd())));
					bbpeObbCenter.getController().setValue(value.set(Tools.formatVector3(emitter.getObbCenter())));
					bbpeObbHalfextension.getController().setValue(value.set(Tools.formatVector3(emitter.getObbHalfextension())));

					// set up rotation matrix to extract euler angles
					Vector3 rotation = new Vector3();
					Matrix4x4 rotationMatrix = new Matrix4x4().identity();
					rotationMatrix.setAxes(emitter.getObbAxis0(), emitter.getObbAxis1(), emitter.getObbAxis2());
					rotationMatrix.computeEulerAngles(rotation);

					// set up rotation
					bbpeObbRotationX.getController().setValue(value.set(Tools.formatFloat(rotation.getX())));
					bbpeObbRotationY.getController().setValue(value.set(Tools.formatFloat(rotation.getY())));
					bbpeObbRotationZ.getController().setValue(value.set(Tools.formatFloat(rotation.getZ())));
					break;
				}
			case CIRCLE_PARTICLE_EMITTER:
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_CIRCLEPARTICLEEMITTER));
					particleSystemEmitter.getActiveConditions().add(EMITTER_CIRCLEPARTICLEEMITTER);
					CircleParticleEmitter emitter = particleSystem.getCircleParticleEmitter();
					cpeCount.getController().setValue(value.set(emitter.getCount()));
					cpeLifeTime.getController().setValue(value.set((int)emitter.getLifeTime()));
					cpeLifeTimeRnd.getController().setValue(value.set((int)emitter.getLifeTimeRnd()));
					cpeMass.getController().setValue(value.set(emitter.getMass(), 4));
					cpeMassRnd.getController().setValue(value.set(emitter.getMassRnd(), 4));
					cpeVelocity.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocity())));
					cpeVelocityRnd.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocityRnd())));
					cpeColorStart.getController().setValue(value.set(Tools.formatColor4(emitter.getColorStart())));
					cpeColorEnd.getController().setValue(value.set(Tools.formatColor4(emitter.getColorEnd())));
					cpeCenter.getController().setValue(value.set(Tools.formatVector3(emitter.getCenter())));
					cpeRadius.getController().setValue(value.set(emitter.getRadius(), 4));

					// set up rotation matrix to extract euler angles
					Vector3 rotation = new Vector3();
					Matrix4x4 rotationMatrix = new Matrix4x4().identity();
					rotationMatrix.setAxes(emitter.getAxis0(), Vector3.computeCrossProduct(emitter.getAxis0(), emitter.getAxis1()), emitter.getAxis1());
					rotationMatrix.computeEulerAngles(rotation);

					// set up rotation
					cpeRotationX.getController().setValue(value.set(Tools.formatFloat(rotation.getX())));
					cpeRotationY.getController().setValue(value.set(Tools.formatFloat(rotation.getY())));
					cpeRotationZ.getController().setValue(value.set(Tools.formatFloat(rotation.getZ())));
					break;
				}
			case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY: 
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_CIRCLEPARTICLEEMITTERPLANEVELOCITY));
					particleSystemEmitter.getActiveConditions().add(EMITTER_CIRCLEPARTICLEEMITTERPLANEVELOCITY);
					CircleParticleEmitterPlaneVelocity emitter = particleSystem.getCircleParticleEmitterPlaneVelocity();
					cpepvCount.getController().setValue(value.set(emitter.getCount()));
					cpepvLifeTime.getController().setValue(value.set((int)emitter.getLifeTime()));
					cpepvLifeTimeRnd.getController().setValue(value.set((int)emitter.getLifeTimeRnd()));
					cpepvMass.getController().setValue(value.set(emitter.getMass(), 4));
					cpepvMassRnd.getController().setValue(value.set(emitter.getMassRnd(), 4));
					cpepvVelocity.getController().setValue(value.set(emitter.getVelocity(), 4));
					cpepvVelocityRnd.getController().setValue(value.set(emitter.getVelocityRnd(), 4));
					cpepvColorStart.getController().setValue(value.set(Tools.formatColor4(emitter.getColorStart())));
					cpepvColorEnd.getController().setValue(value.set(Tools.formatColor4(emitter.getColorEnd())));
					cpepvCenter.getController().setValue(value.set(Tools.formatVector3(emitter.getCenter())));
					cpepvRadius.getController().setValue(value.set(emitter.getRadius(), 4));

					// set up rotation matrix to extract euler angles
					Vector3 rotation = new Vector3();
					Matrix4x4 rotationMatrix = new Matrix4x4().identity();
					rotationMatrix.setAxes(emitter.getAxis0(), Vector3.computeCrossProduct(emitter.getAxis0(), emitter.getAxis1()), emitter.getAxis1());
					rotationMatrix.computeEulerAngles(rotation);

					// set up rotation
					cpepvRotationX.getController().setValue(value.set(Tools.formatFloat(rotation.getX())));
					cpepvRotationY.getController().setValue(value.set(Tools.formatFloat(rotation.getY())));
					cpepvRotationZ.getController().setValue(value.set(Tools.formatFloat(rotation.getZ())));
					break;
				}
			case SPHERE_PARTICLE_EMITTER:
				{
					particleSystemEmitters.getController().setValue(value.set(EMITTER_SPHEREPARTICLEEMITTER));
					particleSystemEmitter.getActiveConditions().add(EMITTER_SPHEREPARTICLEEMITTER);
					SphereParticleEmitter emitter = particleSystem.getSphereParticleEmitter();
					speCount.getController().setValue(value.set(emitter.getCount()));
					speLifeTime.getController().setValue(value.set((int)emitter.getLifeTime()));
					speLifeTimeRnd.getController().setValue(value.set((int)emitter.getLifeTimeRnd()));
					speMass.getController().setValue(value.set(emitter.getMass(), 4));
					speMassRnd.getController().setValue(value.set(emitter.getMassRnd(), 4));
					speVelocity.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocity())));
					speVelocityRnd.getController().setValue(value.set(Tools.formatVector3(emitter.getVelocityRnd())));
					speColorStart.getController().setValue(value.set(Tools.formatColor4(emitter.getColorStart())));
					speColorEnd.getController().setValue(value.set(Tools.formatColor4(emitter.getColorEnd())));
					speCenter.getController().setValue(value.set(Tools.formatVector3(emitter.getCenter())));
					speRadius.getController().setValue(value.set(emitter.getRadius(), 4));
					break;
				}
			default:
				Console.println("ParticleSystemScreenController::onParticleSystemEmitterApply(): unknown particle system emitter '" + particleSystem.getEmitter() + "'");
		}

		// re init entity in view
		view.initParticleSystem();
	}

	/**
	 * On particle system load
	 */
	public void onParticleSystemLoad() {
		view.getPopUpsViews().getFileDialogScreenController().show(
			particleSystemPath.getPath(),
			"Load from: ", 
			new String[]{"tps"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.loadFile(
						view.getPopUpsViews().getFileDialogScreenController().getPathName(),
						view.getPopUpsViews().getFileDialogScreenController().getFileName()
					);
					particleSystemPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On model save
	 */
	public void onEntitySave() {
		// try to use entity file name
		String fileName = view.getEntity().getEntityFileName();
		// do we have a entity name?
		if (fileName == null) {
			fileName = "untitle.tps";
		}
		// we only want the file name and not path
		fileName = Tools.getFileName(fileName);

		//
		view.getPopUpsViews().getFileDialogScreenController().show(
			particleSystemPath.getPath(),
			"Save from: ", 
			new String[]{"tps"},
			fileName,
			new Action() {
				public void performAction() {
					try {
						view.saveFile(
							view.getPopUpsViews().getFileDialogScreenController().getPathName(),
							view.getPopUpsViews().getFileDialogScreenController().getFileName()
						);
						particleSystemPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
						view.getPopUpsViews().getFileDialogScreenController().close();
					} catch (Exception ioe) {
						ioe.printStackTrace();
						showErrorPopUp("Warning", ioe.getMessage());
					}
				}
				
			}
		);
	}

	/**
	 * On particle system reload
	 */
	public void onParticleSystemReload() {
		view.reloadFile();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.PopUpsController#saveFile(java.lang.String, java.lang.String)
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		view.saveFile(pathName, fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.PopUpsController#loadFile(java.lang.String, java.lang.String)
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		view.loadFile(pathName, fileName);
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		view.getPopUpsViews().getInfoDialogScreenController().show(caption, message);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		// delegate to model base screen controller
		entityBaseSubScreenController.onValueChanged(node, view.getEntity());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// delegate to model base sub screen controller
		entityBaseSubScreenController.onActionPerformed(type, node, view.getEntity());
		// delegate to display sub screen controller
		entityDisplaySubScreenController.onActionPerformed(type, node);
		// delegate to display bounding volume sub screen controller
		entityBoundingVolumeSubScreenController.onActionPerformed(type, node, view.getEntity());
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_entity_load")) {
						onParticleSystemLoad();
					} else
					if (node.getId().equals("button_entity_reload")) {
						onParticleSystemReload();
					} else
					if (node.getId().equals("button_entity_save")) {
						onEntitySave();
					} else
					if (node.getId().equals("button_ps_type_apply")) {
						onParticleSystemTypeApply();
					} else
					if (node.getId().equals("button_ops_apply") ||
						node.getId().equals("button_pps_type_apply")) {
						onParticleSystemTypeDataApply();
					} else
					if (node.getId().equals("button_emitter_apply")) {
						onParticleSystemEmitterApply();
					} else
					if (node.getId().equals("button_ppe_emitter_apply") ||
						node.getId().equals("button_bbpe_emitter_apply") ||
						node.getId().equals("button_cpe_emitter_apply") ||
						node.getId().equals("button_cpepv_emitter_apply") ||
						node.getId().equals("button_spe_emitter_apply")) {
						onParticleSystemEmitterDataApply();
					} else
					if (node.getId().equals("button_ops_model_file")) {
						//
						view.getPopUpsViews().getFileDialogScreenController().show(
								modelPath.getPath(),
								"Load from: ", 
								new String[]{"dae", "tm"},
								"",
								new Action() {
									public void performAction() {
										opsModel.getController().setValue(
											value.set(
												view.getPopUpsViews().getFileDialogScreenController().getPathName() + 
												"/" + 
												view.getPopUpsViews().getFileDialogScreenController().getFileName()
											)
										);
										modelPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
										view.getPopUpsViews().getFileDialogScreenController().close();
									}
								}
							);
					} else {
						Console.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					}
					break;
				}
			case PERFORMING:
				{
					// Console.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					break;
				}
		}
	}

}
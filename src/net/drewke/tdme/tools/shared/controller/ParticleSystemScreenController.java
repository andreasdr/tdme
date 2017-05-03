package net.drewke.tdme.tools.shared.controller;

import java.util.Collection;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ModelViewerView;
import net.drewke.tdme.tools.shared.views.ParticleSystemView;
import net.drewke.tdme.tools.viewer.TDMEViewer;

/**
 * Model viewer screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ParticleSystemScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

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
	private GUIElementNode bbpeObbAxis0;
	private GUIElementNode bbpeObbAxis1;
	private GUIElementNode bbpeObbAxis2;

	private GUIElementNode cpeCount;
	private GUIElementNode cpeLifeTime;
	private GUIElementNode cpeLifeTimeRnd;
	private GUIElementNode cpeMass;
	private GUIElementNode cpeMassrnd;
	private GUIElementNode cpeVelocity;
	private GUIElementNode cpeVelocityRnd;
	private GUIElementNode cpeColorStart;
	private GUIElementNode cpeColorEnd;
	private GUIElementNode cpeCenter;
	private GUIElementNode cpeRadius;
	private GUIElementNode cpeObbAxis0;
	private GUIElementNode cpeObbAxis1;

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
	private GUIElementNode cpepvObbAxis0;
	private GUIElementNode cpepvObbAxis1;

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

	private FileDialogPath particleSystemPath;

	/**
	 * Public constructor
	 * @param view
	 */
	public ParticleSystemScreenController(ParticleSystemView view) {
		this.particleSystemPath = new FileDialogPath(".");
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void init() {
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

			// point particle system
			ppsMaxPoints = (GUIElementNode)screenNode.getNodeById("pps_maxpoints");

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
			bbpeObbAxis0 = (GUIElementNode)screenNode.getNodeById("bbpe_obb_axis0");
			bbpeObbAxis1 = (GUIElementNode)screenNode.getNodeById("bbpe_obb_axis1");
			bbpeObbAxis2 = (GUIElementNode)screenNode.getNodeById("bbpe_obb_axis2");

			// circle particle emitter
			cpeCount = (GUIElementNode)screenNode.getNodeById("cpe_count");
			cpeLifeTime = (GUIElementNode)screenNode.getNodeById("cpe_lifetime");
			cpeLifeTimeRnd = (GUIElementNode)screenNode.getNodeById("cpe_lifetimernd");
			cpeMass = (GUIElementNode)screenNode.getNodeById("cpe_mass");
			cpeMassrnd = (GUIElementNode)screenNode.getNodeById("cpe_massrnd");
			cpeVelocity = (GUIElementNode)screenNode.getNodeById("cpe_velocity");
			cpeVelocityRnd = (GUIElementNode)screenNode.getNodeById("cpe_velocityrnd");
			cpeColorStart = (GUIElementNode)screenNode.getNodeById("cpe_colorstart");
			cpeColorEnd = (GUIElementNode)screenNode.getNodeById("cpe_colorend");
			cpeCenter = (GUIElementNode)screenNode.getNodeById("cpe_center");
			cpeRadius = (GUIElementNode)screenNode.getNodeById("cpe_radius");
			cpeObbAxis0 = (GUIElementNode)screenNode.getNodeById("cpe_obb_axis0");
			cpeObbAxis1 = (GUIElementNode)screenNode.getNodeById("cpe_obb_axis1");

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
			cpepvObbAxis0 = (GUIElementNode)screenNode.getNodeById("cpepv_obb_axis0");
			cpepvObbAxis1 = (GUIElementNode)screenNode.getNodeById("cpepv_obb_axis1");

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
		entityBaseSubScreenController.init(screenNode);

		// init display sub screen controller
		entityDisplaySubScreenController.init(screenNode);

		// init bounding volume sub screen controller
		entityBoundingVolumeSubScreenController.init(screenNode);
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
	public void setParticleSystemTypes(Collection<String> particleSystemTypesCollection) {
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

		// initial selection
		onParticleSystemTypeApply();
	}

	/**
	 * Set up emitters
	 * @param emitters
	 */
	public void setParticleSystemEmitters(Collection<String> emittersCollection) {
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

		// initial selection
		onParticleSystemEmitterApply();
	}

	/**
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
	}

	/**
	 * On particle system type apply
	 */
	public void onParticleSystemTypeApply() {
		particleSystemType.getActiveConditions().removeAll();
		particleSystemType.getActiveConditions().add(particleSystemTypes.getController().getValue().toString());
	}

	/**
	 * On particle system emittter apply
	 */
	public void onParticleSystemEmitterApply() {
		particleSystemEmitter.getActiveConditions().removeAll();
		particleSystemEmitter.getActiveConditions().add(particleSystemEmitters.getController().getValue().toString());
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
					} 
					if (node.getId().equals("button_emitter_apply")) {
						onParticleSystemEmitterApply();
					} else {
						System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					}
					break;
				}
			case PERFORMING:
				{
					// System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					break;
				}
		}
	}

}
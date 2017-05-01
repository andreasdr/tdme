package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
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
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
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
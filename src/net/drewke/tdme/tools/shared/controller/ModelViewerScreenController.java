package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ModelViewerView;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.MutableString;

/**
 * Model viewer screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ModelViewerScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private final static MutableString TEXT_EMPTY = new MutableString("");

	private EntityBaseSubScreenController entityBaseSubScreenController;
	private EntityDisplaySubScreenController entityDisplaySubScreenController;
	private EntityBoundingVolumeSubScreenController entityBoundingVolumeSubScreenController;

	private final ModelViewerView view;

	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode modelReload;
	private GUIElementNode modelSave;
	private GUIElementNode pivotX;
	private GUIElementNode pivotY;
	private GUIElementNode pivotZ;
	private GUIElementNode pivotApply;
	private GUIElementNode statsOpaqueFaces;
	private GUIElementNode statsTransparentFaces;
	private GUIElementNode statsMaterialCount;

	private MutableString value;

	private FileDialogPath modelPath;

	/**
	 * Public constructor
	 * @param view
	 */
	public ModelViewerScreenController(ModelViewerView view) {
		this.modelPath = new FileDialogPath(".");
		this.view = view;
		final ModelViewerView finalView = view;
		this.entityBaseSubScreenController = new EntityBaseSubScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				finalView.updateGUIElements();
				finalView.onSetEntityData();
			}
		});
		this.entityDisplaySubScreenController = new EntityDisplaySubScreenController();
		this.entityBoundingVolumeSubScreenController = new EntityBoundingVolumeSubScreenController(view.getPopUpsViews(), modelPath);
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
			screenNode = GUIParser.parse("resources/tools/viewer/gui", "screen_modelviewer.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			modelReload = (GUIElementNode)screenNode.getNodeById("button_model_reload");
			modelSave = (GUIElementNode)screenNode.getNodeById("button_model_save");
			pivotX = (GUIElementNode)screenNode.getNodeById("pivot_x");
			pivotY = (GUIElementNode)screenNode.getNodeById("pivot_y");
			pivotZ = (GUIElementNode)screenNode.getNodeById("pivot_z");
			pivotApply = (GUIElementNode)screenNode.getNodeById("button_pivot_apply");
			statsOpaqueFaces = (GUIElementNode)screenNode.getNodeById("stats_opaque_faces");
			statsTransparentFaces = (GUIElementNode)screenNode.getNodeById("stats_transparent_faces");
			statsMaterialCount = (GUIElementNode)screenNode.getNodeById("stats_material_count");
			statsOpaqueFaces.getController().setDisabled(true);
			statsTransparentFaces.getController().setDisabled(true);
			statsMaterialCount.getController().setDisabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init entity base sub screen controller
		entityBaseSubScreenController.initialize(screenNode);

		// init display sub screen controller
		entityDisplaySubScreenController.initialize(screenNode);

		// init bounding volume sub screen controller
		entityBoundingVolumeSubScreenController.initialize(screenNode);

		//
		value = new MutableString();
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
		modelReload.getController().setDisabled(false);
		modelSave.getController().setDisabled(false);
	}

	/**
	 * Unset entity data
	 */
	public void unsetEntityData() {
		entityBaseSubScreenController.unsetEntityData();
		modelReload.getController().setDisabled(true);
		modelSave.getController().setDisabled(true);
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
	 * Set pivot tab
	 * @param pivot
	 */
	public void setPivot(Vector3 pivot) {
		pivotX.getController().setDisabled(false);
		pivotX.getController().getValue().set(Tools.formatFloat(pivot.getX()));
		pivotY.getController().setDisabled(false);
		pivotY.getController().getValue().set(Tools.formatFloat(pivot.getY()));
		pivotZ.getController().setDisabled(false);
		pivotZ.getController().getValue().set(Tools.formatFloat(pivot.getZ()));
		pivotApply.getController().setDisabled(false);
	}

	/**
	 * Unset pivot tab
	 */
	public void unsetPivot() {
		pivotX.getController().setDisabled(true);
		pivotX.getController().getValue().set(TEXT_EMPTY);
		pivotY.getController().setDisabled(true);
		pivotY.getController().getValue().set(TEXT_EMPTY);
		pivotZ.getController().setDisabled(true);
		pivotZ.getController().getValue().set(TEXT_EMPTY);
		pivotApply.getController().setDisabled(true);
	}

	/**
	 * Set up model statistics
	 * @param stats opaque faces
	 * @param stats transparent faces
	 * @param stats material count
	 */
	public void setStatistics(int statsOpaqueFaces, int statsTransparentFaces, int statsMaterialCount) {
		this.statsOpaqueFaces.getController().setValue(value.set(statsOpaqueFaces));
		this.statsTransparentFaces.getController().setValue(value.set(statsTransparentFaces));
		this.statsMaterialCount.getController().setValue(value.set(statsMaterialCount));
	}

	/**
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
	}

	/**
	 * On model load
	 */
	public void onModelLoad() {
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath.getPath(),
			"Load from: ", 
			new String[]{"tmm", "dae", "tm"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.loadFile(
						view.getPopUpsViews().getFileDialogScreenController().getPathName(),
						view.getPopUpsViews().getFileDialogScreenController().getFileName()
					);
					modelPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On model save
	 */
	public void onModelSave() {
		// try to use entity file name
		String fileName = view.getEntity().getEntityFileName();
		// do we have a entity name?
		if (fileName == null) {
			// nope, take file model filename
			fileName = view.getFileName();
			// and add .tmm
			if (fileName.toLowerCase().endsWith(".tmm") == false) {
				fileName+= ".tmm";
			}
		}
		// we only want the file name and not path
		fileName = Tools.getFileName(fileName);

		//
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath.getPath(),
			"Save from: ", 
			new String[]{"tmm"},
			fileName,
			new Action() {
				public void performAction() {
					try {
						view.saveFile(
							view.getPopUpsViews().getFileDialogScreenController().getPathName(),
							view.getPopUpsViews().getFileDialogScreenController().getFileName()
						);
						modelPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
						view.getPopUpsViews().getFileDialogScreenController().close();
					} catch (Exception ioe) {
						showErrorPopUp("Warning", ioe.getMessage());
					}
				}
				
			}
		);
	}

	/**
	 * On model reload
	 */
	public void onModelReload() {
		view.reloadFile();
	}

	/**
	 * On pivot apply
	 */
	public void onPivotApply() {
		try {
			float x = Float.parseFloat(pivotX.getController().getValue().toString());
			float y = Float.parseFloat(pivotY.getController().getValue().toString());
			float z = Float.parseFloat(pivotZ.getController().getValue().toString());
			view.pivotApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
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
					if (node.getId().equals("button_model_load")) {
						onModelLoad();
					} else
					if (node.getId().equals("button_model_reload")) {
						onModelReload();
					} else
					if (node.getId().equals("button_model_save")) {
						onModelSave();
					} else
					if (node.getId().equals("button_pivot_apply")) {
						onPivotApply();
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
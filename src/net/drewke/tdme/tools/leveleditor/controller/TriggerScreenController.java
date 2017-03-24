package net.drewke.tdme.tools.leveleditor.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.views.TriggerView;
import net.drewke.tdme.tools.shared.controller.Action;
import net.drewke.tdme.tools.shared.controller.ModelBaseSubScreenController;
import net.drewke.tdme.tools.shared.controller.ScreenController;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.utils.MutableString;

/**
 * Model viewer screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class TriggerScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private final static MutableString TEXT_EMPTY = new MutableString("");

	private ModelBaseSubScreenController modelBaseSubScreenController;

	private TriggerView view;
	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode triggerWidth;
	private GUIElementNode triggerHeight;
	private GUIElementNode triggerDepth;
	private GUIElementNode triggerApply;

	/**
	 * Public constructor
	 * @param view
	 */
	public TriggerScreenController(TriggerView view) {
		this.view = view;
		this.modelBaseSubScreenController = new ModelBaseSubScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				view.updateGUIElements();
				TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().setModelLibrary();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/leveleditor/gui", "screen_trigger.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			triggerWidth = (GUIElementNode)screenNode.getNodeById("trigger_width");
			triggerHeight = (GUIElementNode)screenNode.getNodeById("trigger_height");
			triggerDepth = (GUIElementNode)screenNode.getNodeById("trigger_depth");
			triggerApply = (GUIElementNode)screenNode.getNodeById("button_trigger_apply");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init model base view
		modelBaseSubScreenController.init(screenNode);
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
		screenCaption.layout();
	}

	/**
	  * Set up general model data
	  * @param name
	  * @param description
	  */
	public void setModelData(String name, String description) {
		modelBaseSubScreenController.setModelData(name, description);
	}

	/**
	 * Unset model data
	 */
	public void unsetModelData() {
		modelBaseSubScreenController.unsetModelData();
	}

	/**
	 * Set up model properties
	 * @param preset id
	 * @param model properties
	 * @param selected name
	 */
	public void setModelProperties(String presetId, Iterable<PropertyModelClass> modelProperties, String selectedName) {
		modelBaseSubScreenController.setModelProperties(view.getModel(), presetId, modelProperties, selectedName);
	}

	/**
 	 * Unset model properties
	 */
	public void unsetModelProperties() {
		modelBaseSubScreenController.unsetModelProperties();
	}

	/**
	 * Set trigger tab
	 * @param dimension
	 */
	public void setTrigger(float width, float height, float depth) {
		triggerWidth.getController().setDisabled(false);
		triggerWidth.getController().getValue().set(Tools.formatFloat(width));
		triggerHeight.getController().setDisabled(false);
		triggerHeight.getController().getValue().set(Tools.formatFloat(height));
		triggerDepth.getController().setDisabled(false);
		triggerDepth.getController().getValue().set(Tools.formatFloat(depth));
		triggerApply.getController().setDisabled(false);
	}

	/**
	 * Unset trigger tab
	 */
	public void unsetTrigger() {
		triggerWidth.getController().setDisabled(true);
		triggerWidth.getController().getValue().set(TEXT_EMPTY);
		triggerHeight.getController().setDisabled(true);
		triggerHeight.getController().getValue().set(TEXT_EMPTY);
		triggerDepth.getController().setDisabled(true);
		triggerDepth.getController().getValue().set(TEXT_EMPTY);
		triggerApply.getController().setDisabled(true);
	}

	/**
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
	}


	/**
	 * On trigger apply
	 */
	public void onTriggerApply() {
		try {
			float width = Float.parseFloat(triggerWidth.getController().getValue().toString());
			float height = Float.parseFloat(triggerHeight.getController().getValue().toString());
			float depth = Float.parseFloat(triggerDepth.getController().getValue().toString());
			view.triggerApply(width, height, depth);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
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
		modelBaseSubScreenController.onValueChanged(node, view.getModel());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// delegate to model base screen controller
		modelBaseSubScreenController.onActionPerformed(type, node, view.getModel());
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_trigger_apply")) {
						onTriggerApply();
					} else {
						System.out.println("TriggerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
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
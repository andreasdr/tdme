package net.drewke.tdme.tools.leveleditor.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.views.TriggerView;
import net.drewke.tdme.tools.shared.controller.EntityBaseSubScreenController;
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

	private EntityBaseSubScreenController entityBaseSubScreenController;

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
		final TriggerView finalView = view;
		this.entityBaseSubScreenController = new EntityBaseSubScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				finalView .updateGUIElements();
				TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
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
		entityBaseSubScreenController.init(screenNode);
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
	}

	/**
	 * Unset entity data
	 */
	public void unsetEntityData() {
		entityBaseSubScreenController.unsetEntityData();
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
		entityBaseSubScreenController.onValueChanged(node, view.getEntity());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// delegate to model base screen controller
		entityBaseSubScreenController.onActionPerformed(type, node, view.getEntity());
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
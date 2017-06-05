package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.gui.events.GUIActionListener.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.tools.shared.views.EntityDisplayView;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.MutableString;

/**
 * Entity display sub screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityDisplaySubScreenController {

	private final static MutableString CHECKBOX_CHECKED = new MutableString("1");
	private final static MutableString CHECKBOX_UNCHECKED = new MutableString("");

	private GUIElementNode displayBoundingVolume;
	private GUIElementNode displayShadowing;
	private GUIElementNode displayGround;

	private EntityDisplayView view;

	/**
	 * Public constructor
	 */
	public EntityDisplaySubScreenController() {
		view = new EntityDisplayView(this);
	}

	/**
	 * @return view
	 */
	public EntityDisplayView getView() {
		return view;
	}

	/**
	 * Init
	 * @param screen node
	 */
	public void init(GUIScreenNode screenNode) {
		// load screen node
		try {
			displayBoundingVolume = (GUIElementNode)screenNode.getNodeById("display_boundingvolume");
			displayShadowing = (GUIElementNode)screenNode.getNodeById("display_shadowing");
			displayGround = (GUIElementNode)screenNode.getNodeById("display_ground");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set up display section
	 */
	public void setupDisplay() {
		displayShadowing.getController().setValue(view.isDisplayShadowing() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayGround.getController().setValue(view.isDisplayGroundPlate() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayBoundingVolume.getController().setValue(view.isDisplayBoundingVolume() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
	}

	/**
	 * On display apply button event
	 */
	public void onDisplayApply() {
		view.setDisplayShadowing(displayShadowing.getController().getValue().equals(CHECKBOX_CHECKED));
		view.setDisplayGroundPlate(displayGround.getController().getValue().equals(CHECKBOX_CHECKED));
		view.setDisplayBoundingVolume(displayBoundingVolume.getController().getValue().equals(CHECKBOX_CHECKED));
	}

	/**
	 * @return display shadowing checked
	 */
	public boolean getDisplayShadowing() {
		return displayShadowing.getController().getValue().equals(CHECKBOX_CHECKED);
	}

	/**
	 * @return display ground checked
	 */
	public boolean getDisplayGround() {
		return displayGround.getController().getValue().equals(CHECKBOX_CHECKED);
	}

	/**
	 * @return display bounding volume checked
	 */
	public boolean getDisplayBoundingVolume() {
		return displayBoundingVolume.getController().getValue().equals(CHECKBOX_CHECKED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_display_apply")) {
						onDisplayApply();
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

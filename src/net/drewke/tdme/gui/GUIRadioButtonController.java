package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIMouseEvent.Type;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIRadioButtonController extends GUINodeController {

	protected static final String CONDITION_SELECTED = "selected";
	protected static final String CONDITION_UNSELECTED = "unselected";

	protected boolean selected;

	private static HashMap<String, ArrayList<GUIElementNode>> radioButtonGroupNodesByName = new HashMap<String, ArrayList<GUIElementNode>>();

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUIRadioButtonController(GUINode node) {
		super(node);
		this.selected = false;

		// add to nodes
		//	check if group exists
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.screenNode.id + "_radiobuttongroup_" + ((GUIElementNode)this.node).name);
		// nope?
		if (radioButtonGroupNodes == null) {
			// ok, create
			radioButtonGroupNodes = new ArrayList<GUIElementNode>();
			radioButtonGroupNodesByName.put(node.screenNode.id + "_radiobuttongroup_" + ((GUIElementNode)node).name, radioButtonGroupNodes);
		}
		// add node
		radioButtonGroupNodes.add((GUIElementNode)node);
	}

	/**
	 * @return is checked
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Select
	 * @param checked
	 */
	public void select() {
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.screenNode.id + "_radiobuttongroup_" + ((GUIElementNode)this.node).name);
		// unselect all radio buttons
		if (radioButtonGroupNodes != null) {
			for (GUIElementNode radioButtonNode: radioButtonGroupNodes) {
				GUINodeConditions nodeConditions = radioButtonNode.getActiveConditions();
				GUIRadioButtonController nodeController = (GUIRadioButtonController)radioButtonNode.controller;
				nodeConditions.remove(nodeController.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
				nodeController.selected = false;
				nodeConditions.add(nodeController.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);				
			}
		}

		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = true;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
		radioButtonGroupNodesByName.remove(this.node.screenNode.id + "_radiobuttongroup_" + ((GUIElementNode)this.node).name);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#handleEvent(net.drewke.tdme.gui.GUINode, net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUINode node, GUIMouseEvent event) {
		if (node.id.equals(this.node.id) &&
			node.isEventBelongingToNode(event) && 
			event.type == Type.MOUSE_RELEASED && 
			event.button == 1) {
			//
			select();
		}
	}

}
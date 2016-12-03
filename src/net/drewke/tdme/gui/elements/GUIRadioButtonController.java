package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIRadioButtonController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private boolean selected;

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
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
		// nope?
		if (radioButtonGroupNodes == null) {
			// ok, create
			radioButtonGroupNodes = new ArrayList<GUIElementNode>();
			radioButtonGroupNodesByName.put(node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)node).getName(), radioButtonGroupNodes);
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
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
		// unselect all radio buttons
		if (radioButtonGroupNodes != null) {
			for (GUIElementNode radioButtonNode: radioButtonGroupNodes) {
				GUINodeConditions nodeConditions = radioButtonNode.getActiveConditions();
				GUIRadioButtonController nodeController = (GUIRadioButtonController)radioButtonNode.getController();
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
		radioButtonGroupNodesByName.remove(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// check if our node was clicked
		if (node == this.node &&
			node.isEventBelongingToNode(event) &&  
			event.getButton() == 1) {
			// set event processed
			event.setProcessed(true);

			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				select();
	
				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)node);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		// no op for now
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusGained()
	 */
	public void onFocusGained() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
	}

}

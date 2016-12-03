package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI select box controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIDropDownController extends GUINodeController {

	private static final String CONDITION_OPENED = "opened";
	private static final String CONDITION_CLOSED = "closed";

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private boolean isOpen = false;
	private GUIElementNode arrowNode = null;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIDropDownController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		arrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_panel_arrow");
		((GUIElementNode)node).getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @return drop down open state
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Unselect all nodes
	 */
	protected void unselect() {
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUIDropDownOptionController) {
				((GUIDropDownOptionController)childController).unselect();
			}
		}
	}

	/**
	 * Toggle open state
	 */
	public void toggleOpenState() {
		((GUIElementNode)node).getActiveConditions().remove(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().remove(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		isOpen = isOpen == true?false:true;
		((GUIElementNode)node).getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
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
				toggleOpenState();
	
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

}

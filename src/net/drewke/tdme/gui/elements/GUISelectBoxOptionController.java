package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;

/**
 * GUI select box option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBoxOptionController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private GUINode selectBoxNode;
	private boolean selected;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxOptionController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.selected = ((GUIElementNode)node).isSelected();
	}

	/**
	 * @return is selected
	 */
	protected boolean isSelected() {
		return selected;
	}

	/**
	 * Select
	 */
	protected void select() {
		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = true;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/**
	 * Unselect
	 * @param checked
	 */
	protected void unselect() {
		// unselect current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = false;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// determine drop down node
		selectBoxNode = node.getParentControllerNode();
		while(true == true) {
			selectBoxNode = selectBoxNode.getParentControllerNode();
			if (selectBoxNode.getController() instanceof GUISelectBoxController) {
				break;
			}
		}

		// set initial state
		if (selected == true) {
			select();
		} else {
			unselect();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#postLayout()
	 */
	public void postLayout() {
		if (selected == true) {
			node.scrollToNodeX();
			node.scrollToNodeY();
		}
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
			if (event.getType() == Type.MOUSE_PRESSED) {
				// unselect all options, select this option 
				((GUISelectBoxController)selectBoxNode.getController()).unselect();
				select();

				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)selectBoxNode);

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)selectBoxNode);

				//
				node.scrollToNodeX();
				node.scrollToNodeY();
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
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#tick()
	 */
	public void tick() {
		// no op
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#hasValue()
	 */
	public boolean hasValue() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#getValue()
	 */
	public String getValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		// no op
	}

}

package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI select box multiple option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBoxMultipleOptionController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private static final String CONDITION_FOCUSSED = "focussed";
	private static final String CONDITION_UNFOCUSSED = "unfocussed";

	private GUIParentNode selectBoxMultipleNode;
	private boolean selected;
	private boolean focussed;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxMultipleOptionController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.selected = ((GUIElementNode)node).isSelected();
		this.focussed = false;
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

	/**
	 * Toggle selection
	 */
	protected void toggle() {
		if (selected == true) {
			unselect();
		} else {
			select();
		}
	}

	/**
	 * @return is focussed
	 */
	public boolean isFocussed() {
		return focussed;
	}

	/**
	 * Focus
	 */
	protected void focus() {
		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.focussed == true?CONDITION_FOCUSSED:CONDITION_UNFOCUSSED);
		this.focussed = true;
		nodeConditions.add(this.focussed == true?CONDITION_FOCUSSED:CONDITION_UNFOCUSSED);
	}

	/**
	 * Unfocus
	 * @param checked
	 */
	protected void unfocus() {
		// unselect current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.focussed == true?CONDITION_FOCUSSED:CONDITION_UNFOCUSSED);
		this.focussed = false;
		nodeConditions.add(this.focussed == true?CONDITION_FOCUSSED:CONDITION_UNFOCUSSED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// determine drop down node
		selectBoxMultipleNode = node.getParentControllerNode();
		while(true == true) {
			if (selectBoxMultipleNode.getController() instanceof GUISelectBoxMultipleController) {
				break;
			}
			selectBoxMultipleNode = selectBoxMultipleNode.getParentControllerNode();
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
			node.scrollToNodeX(selectBoxMultipleNode);
			node.scrollToNodeY(selectBoxMultipleNode);
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
				((GUISelectBoxMultipleController)selectBoxMultipleNode.getController()).unfocus();

				// unselect all options, select this option 
				toggle();

				// focus
				focus();

				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)selectBoxMultipleNode);

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)selectBoxMultipleNode);

				//
				node.scrollToNodeX(selectBoxMultipleNode);
				node.scrollToNodeY(selectBoxMultipleNode);
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
	public MutableString getValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		// no op
	}

}

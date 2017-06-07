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
 * GUI select box option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBoxOptionController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private GUIParentNode selectBoxNode;
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#isDisabled()
	 */
	public boolean isDisabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		// no op
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

		// disabled
		boolean disabled = ((GUISelectBoxController)selectBoxNode.getController()).isDisabled();
		nodeConditions.remove(CONDITION_DISABLED);
		nodeConditions.remove(CONDITION_ENABLED);
		nodeConditions.add(disabled==true?CONDITION_DISABLED:CONDITION_ENABLED);
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

		// disabled
		boolean disabled = ((GUISelectBoxController)selectBoxNode.getController()).isDisabled();
		nodeConditions.remove(CONDITION_DISABLED);
		nodeConditions.remove(CONDITION_ENABLED);
		nodeConditions.add(disabled==true?CONDITION_DISABLED:CONDITION_ENABLED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#initialize()
	 */
	public void initialize() {
		// determine drop down node
		selectBoxNode = node.getParentControllerNode();
		while(true == true) {
			if (selectBoxNode.getController() instanceof GUISelectBoxController) {
				break;
			}
			selectBoxNode = selectBoxNode.getParentControllerNode();
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
			node.scrollToNodeX(selectBoxNode);
			node.scrollToNodeY(selectBoxNode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// disabled
		boolean disabled = ((GUISelectBoxController)selectBoxNode.getController()).isDisabled();

		// check if our node was clicked
		if (disabled == false &&
			node == this.node &&
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
				node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)selectBoxNode);

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)selectBoxNode);

				//
				node.scrollToNodeX(selectBoxNode);
				node.scrollToNodeY(selectBoxNode);
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

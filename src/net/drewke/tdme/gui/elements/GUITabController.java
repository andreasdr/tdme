package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIColor;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI tab controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITabController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private GUINode tabsNode;
	private GUINode tabsHeaderNode;
	private boolean selected;

	private GUIColor unfocussedNodeBorderLeftColor;
	private GUIColor unfocussedNodeBorderRightColor;
	private GUIColor unfocussedNodeBorderTopColor;
	private GUIColor unfocussedNodeBorderBottomColor;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITabController(GUINode node) {
		super(node);
		this.tabsNode = null;
		this.tabsHeaderNode = null;
		this.selected = false;
		this.unfocussedNodeBorderLeftColor = null;
		this.unfocussedNodeBorderRightColor = null;
		this.unfocussedNodeBorderTopColor = null;
		this.unfocussedNodeBorderBottomColor = null;
	}

	/**
	 * @return is checked
	 */
	protected boolean isSelected() {
		return selected;
	}

	/**
	 * Set checked
	 * @param selected
	 */
	protected void setSelected(boolean selected) {
		// remove old selection condition, add new selection condition
		GUINodeConditions nodeConditions = ((GUIElementNode)this.node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = selected;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);

		// handle focus, alter border depending on tabs header node focus and selection state
		if (((GUITabsHeaderController)tabsHeaderNode.getController()).hasFocus() == true) {
			if (selected == true) {
				GUIColor focussedBorderColor = node.getScreenNode().getFoccussedBorderColor();
				GUINode.Border border = node.getBorder();
				border.topColor = focussedBorderColor;
				border.leftColor = focussedBorderColor; 
				border.bottomColor = focussedBorderColor;
				border.rightColor = focussedBorderColor;
			} else {
				GUINode.Border border = node.getBorder();
				border.topColor = unfocussedNodeBorderTopColor;
				border.leftColor = unfocussedNodeBorderLeftColor;
				border.bottomColor = unfocussedNodeBorderBottomColor;
				border.rightColor = unfocussedNodeBorderRightColor;
			}
		} else {
			GUINode.Border border = node.getBorder();
			border.topColor = unfocussedNodeBorderTopColor;
			border.leftColor = unfocussedNodeBorderLeftColor;
			border.bottomColor = unfocussedNodeBorderBottomColor;
			border.rightColor = unfocussedNodeBorderRightColor;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// get "tabs" node
		tabsNode = ((GUIParentNode)node).getParentControllerNode().getParentControllerNode();

		// get "tabs header" node
		tabsHeaderNode = ((GUIParentNode)node).getParentControllerNode();

		// store original border
		GUINode.Border border = node.getBorder();
		unfocussedNodeBorderTopColor = border.topColor;
		unfocussedNodeBorderLeftColor = border.leftColor;
		unfocussedNodeBorderBottomColor = border.bottomColor;
		unfocussedNodeBorderRightColor = border.rightColor;

		// set initial state
		setSelected(selected);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
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
				GUITabsController guiTabsController = (GUITabsController)tabsNode.getController();
	
				// unselect all tabs
				guiTabsController.unselect();
	
				// select current
				setSelected(selected == true?false:true);
	
				// select tab content
				guiTabsController.setTabContentSelected(node.getId());
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

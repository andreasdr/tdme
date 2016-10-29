package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
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
public class GUITabController extends GUINodeController {

	protected static final String CONDITION_SELECTED = "selected";
	protected static final String CONDITION_UNSELECTED = "unselected";

	protected GUINode tabsNode;
	private boolean selected;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITabController(GUINode node) {
		super(node);
		this.selected = false;
	}

	/**
	 * @return is checked
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set checked
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		// remove old selection condition, add new selection condition
		GUINodeConditions nodeConditions = ((GUIElementNode)this.node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = selected;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// get "tabs" node
		tabsNode = ((GUIParentNode)node).getParentControllerNode();
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
	 * @see net.drewke.tdme.gui.GUINodeController#handleEvent(net.drewke.tdme.gui.GUINode, net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUINode node, GUIMouseEvent event) {
		if (node.getId().equals(this.node.getId()) &&
			node.isEventBelongingToNode(event) && 
			event.getType() == Type.MOUSE_RELEASED && 
			event.getButton() == 1) {
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

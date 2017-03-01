package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI tabs header controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITabsHeaderController extends GUINodeController {

	private GUINode tabsNode;

	private ArrayList<GUINode> childControllerNodes;
	private ArrayList<GUITabController> tabControllers;
	private boolean hasFocus;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITabsHeaderController(GUINode node) {
		super(node);
		this.childControllerNodes = new ArrayList<GUINode>();
		this.tabControllers = new ArrayList<GUITabController>();
		this.hasFocus = false;
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// get "tabs" node
		tabsNode = ((GUIParentNode)node).getParentControllerNode();
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
		// no op
	}

	/**
	 * @return has focus
	 */
	protected boolean hasFocus() {
		return hasFocus;
	}

	/**
	 * Unselect all nodes
	 */
	protected void unselect() {
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUITabController) {
				GUITabController guiTabController = (GUITabController)childController;
				if (guiTabController.getNode().getParentControllerNode() != node) continue;
				guiTabController.setSelected(false);
			}
		}
	}

	/**
	 * Determine select box option controllers
	 */
	private void determineTabControllers() {
		tabControllers.clear();
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController();
			if (childController instanceof GUITabController) {
				GUITabController guiTabController = (GUITabController)childController;
				if (guiTabController.getNode().getParentControllerNode() != node) continue;
				if (guiTabController.isDisabled() == true) continue;
				tabControllers.add(guiTabController);
			}
		}
	}

	/**
	 * Get selected tab idx
	 */
	private int getSelectedTabIdx() {
		int tabControllerIdx = -1;
		for (int i = 0; i < tabControllers.size(); i++) {
			GUITabController tabController = tabControllers.get(i);
			// determine selected idx
			if (tabController.isSelected() == true) {
				tabControllerIdx = i;
				break;
			}
		}

		//
		return tabControllerIdx;
	}

	/**
	 * Select next node
	 */
	private void selectNext() {
		// determine select box option controllers
		determineTabControllers();

		// determine current selected option index
		int tabControllerIdx = getSelectedTabIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		tabControllerIdx = (tabControllerIdx + 1) % tabControllers.size();
		if (tabControllerIdx < 0) tabControllerIdx += tabControllers.size();

		// select
		tabControllers.get(tabControllerIdx).setSelected(true);

		// select tab content
		GUITabsController guiTabsController = (GUITabsController)tabsNode.getController();
		guiTabsController.setTabContentSelected(tabControllers.get(tabControllerIdx).getNode().getId());
	}

	/**
	 * Select previous
	 */
	private void selectPrevious() {
		// determine select box option controllers
		determineTabControllers();

		// determine current selected option index
		int tabControllerIdx = getSelectedTabIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		tabControllerIdx = (tabControllerIdx - 1) % tabControllers.size();
		if (tabControllerIdx < 0) tabControllerIdx += tabControllers.size();

		// select
		tabControllers.get(tabControllerIdx).setSelected(true);

		// select tab content
		GUITabsController guiTabsController = (GUITabsController)tabsNode.getController();
		guiTabsController.setTabContentSelected(tabControllers.get(tabControllerIdx).getNode().getId());
	}

	/**
	 * Select current
	 */
	private void selectCurrent() {
		// determine select box option controllers
		determineTabControllers();

		// determine current selected option index
		int tabControllerIdx = getSelectedTabIdx();

		// unselect all selections
		unselect();

		// select
		tabControllers.get(tabControllerIdx).setSelected(true);

		// select tab content
		GUITabsController guiTabsController = (GUITabsController)tabsNode.getController();
		guiTabsController.setTabContentSelected(tabControllers.get(tabControllerIdx).getNode().getId());
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
		if (node == this.node) {
			switch (event.getKeyCode()) {
				case GUIKeyboardEvent.KEYCODE_LEFT:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							selectPrevious();
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_RIGHT:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							selectNext();
						}
					}
					break;
			}
		}
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
		hasFocus = true;
		selectCurrent();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
		hasFocus = false;
		selectCurrent();
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

package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI tabs header controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITabsHeaderController extends GUINodeController {

	private GUINode tabsNode;

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private ArrayList<GUITabController> tabControllers = new ArrayList<GUITabController>();

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITabsHeaderController(GUINode node) {
		super(node);
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
				((GUITabController)childController).setSelected(false);
			}
		}
	}

	/**
	 * Determine select box option controllers
	 */
	private void determineSelectBoxOptionControllers() {
		tabControllers.clear();
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController();
			if (childController instanceof GUITabController) {
				tabControllers.add((GUITabController)childController);
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
	protected void selectNext() {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

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
	protected void selectPrevious() {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
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
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusGained()
	 */
	public void onFocusGained() {
		// determine child nodes
		// determine selected child node
		// set border
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
		// determine child nodes
		// determine selected child node
		// unset border
	}

}
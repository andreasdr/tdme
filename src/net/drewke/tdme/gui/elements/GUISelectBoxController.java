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
public final class GUISelectBoxController extends GUINodeController {

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private ArrayList<GUISelectBoxOptionController> selectBoxOptionControllers = new ArrayList<GUISelectBoxOptionController>();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
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
			if (childController instanceof GUISelectBoxOptionController) {
				((GUISelectBoxOptionController)childController).unselect();
			}
		}
	}

	/**
	 * Determine select box option controllers
	 */
	private void determineSelectBoxOptionControllers() {
		selectBoxOptionControllers.clear();
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController();
			if (childController instanceof GUISelectBoxOptionController) {
				selectBoxOptionControllers.add((GUISelectBoxOptionController)childController);
			}
		}
	}

	/**
	 * Get selected option idx
	 */
	private int getSelectedOptionIdx() {
		int selectBoxOptionControllerIdx = -1;
		for (int i = 0; i < selectBoxOptionControllers.size(); i++) {
			GUISelectBoxOptionController selectBoxOptionController = selectBoxOptionControllers.get(i);
			// determine selected idx
			if (selectBoxOptionController.isSelected() == true) {
				selectBoxOptionControllerIdx = i;
				break;
			}
		}

		//
		return selectBoxOptionControllerIdx;
	}

	/**
	 * Select next node
	 */
	protected void selectNext() {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

		// determine current selected option index
		int selectBoxOptionControllerIdx = getSelectedOptionIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx + 1) % selectBoxOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += selectBoxOptionControllers.size();

		// select
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).select();
	}

	/**
	 * Select previous
	 */
	protected void selectPrevious() {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

		// determine current selected option index
		int selectBoxOptionControllerIdx = getSelectedOptionIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx - 1) % selectBoxOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += selectBoxOptionControllers.size();

		// select
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).select();
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
				case GUIKeyboardEvent.KEYCODE_UP:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							selectPrevious();
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_DOWN:
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
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#getValue()
	 */
	public String getValue() {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

		// return selected value if exists
		for (int i = 0; i < selectBoxOptionControllers.size(); i++) {
			GUISelectBoxOptionController selectBoxOptionController = selectBoxOptionControllers.get(i);
			if (selectBoxOptionController.isSelected() == true) {
				return ((GUIElementNode)selectBoxOptionController.getNode()).getValue();
			}
		}

		// otherwise return empty string
		return "";
	}

}

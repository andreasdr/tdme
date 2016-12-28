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
	private ArrayList<GUIDropDownOptionController> dropDownOptionControllers = new ArrayList<GUIDropDownOptionController>();
	private boolean isOpen = false;

	private GUIParentNode dropDownNode = null;
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
		dropDownNode = (GUIParentNode)node.getScreenNode().getNodeById(node.getId() + "_layout_horizontal");
		arrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_arrow");
		((GUIElementNode)node).getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#postLayout()
	 */
	public void postLayout() {
		// no op
	}

	/**
	 * @return drop down open state
	 */
	protected boolean isOpen() {
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
	protected void toggleOpenState() {
		((GUIElementNode)node).getActiveConditions().remove(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().remove(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		isOpen = isOpen == true?false:true;
		((GUIElementNode)node).getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
	}

	/**
	 * Determine drop down option controllers
	 */
	private void determineDropDownOptionControllers() {
		dropDownOptionControllers.clear();
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController();
			if (childController instanceof GUIDropDownOptionController) {
				dropDownOptionControllers.add((GUIDropDownOptionController)childController);
			}
		}
	}

	/**
	 * Get selected option idx
	 */
	private int getSelectedOptionIdx() {
		int selectBoxOptionControllerIdx = -1;
		for (int i = 0; i < dropDownOptionControllers.size(); i++) {
			GUIDropDownOptionController selectBoxOptionController = dropDownOptionControllers.get(i);
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
	private void selectNext() {
		// determine select box option controllers
		determineDropDownOptionControllers();

		// determine current selected option index
		int selectBoxOptionControllerIdx = getSelectedOptionIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx + 1) % dropDownOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += dropDownOptionControllers.size();

		// select
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).select();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY();
	}

	/**
	 * Select previous
	 */
	private void selectPrevious() {
		// determine select box option controllers
		determineDropDownOptionControllers();

		// determine current selected option index
		int selectBoxOptionControllerIdx = getSelectedOptionIdx();

		// unselect all selections
		unselect();

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx - 1) % dropDownOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += dropDownOptionControllers.size();

		// select
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).select();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// check if our node was clicked
		if (node == this.dropDownNode &&
			node.isEventBelongingToNode(event) &&  
			event.getButton() == 1) {
			// set event processed
			event.setProcessed(true);

			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				toggleOpenState();
	
				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)this.node);
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
							if (isOpen() == false) {
								toggleOpenState();
							} else {
								selectPrevious();

								// delegate change event
								node.getScreenNode().delegateValueChanged((GUIElementNode)node);
							}
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_DOWN:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							if (isOpen() == false) {
								toggleOpenState();
							} else {
								selectNext();

								// delegate change event
								node.getScreenNode().delegateValueChanged((GUIElementNode)node);
							}
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_SPACE:
					{
						// set event processed
						event.setProcessed(true);
	
						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							toggleOpenState();
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
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
		/*
		if (isOpen() == true) {
			toggleOpenState();
		}
		*/
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
		determineDropDownOptionControllers();

		//
		for (int i = 0; i < dropDownOptionControllers.size(); i++) {
			GUIDropDownOptionController dropDownOptionController = dropDownOptionControllers.get(i);
			if (dropDownOptionController.isSelected() == true) {
				return ((GUIElementNode)dropDownOptionController.getNode()).getValue();
			}
		}

		//
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		// determine select box option controllers
		determineDropDownOptionControllers();

		// unselect all selections
		unselect();

		// determine new selection
		for (int i = 0; i < dropDownOptionControllers.size(); i++) {
			GUIDropDownOptionController dropDownOptionController = dropDownOptionControllers.get(i);
			GUIElementNode dropDownOptionNode = ((GUIElementNode)dropDownOptionController.getNode());
			if (dropDownOptionNode.getValue().equals(value)) {
				dropDownOptionController.select();
				dropDownOptionNode.scrollToNodeX();
				dropDownOptionNode.scrollToNodeY();
				break;
			}
		}
	}

}

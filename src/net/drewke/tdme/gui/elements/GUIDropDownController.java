package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

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
 * GUI drop down controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIDropDownController extends GUINodeController {

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private static final String CONDITION_OPENED = "opened";
	private static final String CONDITION_CLOSED = "closed";

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private ArrayList<GUIDropDownOptionController> dropDownOptionControllers = new ArrayList<GUIDropDownOptionController>();
	private boolean isOpen = false;
	private boolean disabled;

	private GUIParentNode dropDownNode = null;
	private GUIElementNode arrowNode = null;
	private GUIElementNode textElementNode = null;

	private MutableString value = new MutableString();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIDropDownController(GUINode node) {
		super(node);

		//
		this.disabled = ((GUIElementNode)node).isDisabled();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#isDisabled()
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		GUINodeConditions nodeConditionsTextElement = textElementNode.getActiveConditions();
		nodeConditions.remove(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);
		nodeConditionsTextElement.remove(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);
		this.disabled = disabled;
		nodeConditions.add(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);
		nodeConditionsTextElement.add(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);

		// close if open
		if (disabled == true && isOpen() == true) {
			toggleOpenState();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		dropDownNode = (GUIParentNode)node.getScreenNode().getNodeById(node.getId() + "_layout_horizontal");
		arrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_arrow");
		textElementNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_layout_horizontal_element");
		((GUIElementNode)node).getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);
		arrowNode.getActiveConditions().add(isOpen == true?CONDITION_OPENED:CONDITION_CLOSED);

		//
		setDisabled(disabled);
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

		// skip if no options
		if (dropDownOptionControllers.size() == 0) return;

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx + 1) % dropDownOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += dropDownOptionControllers.size();

		// select
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).select();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX(dropDownNode);
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY(dropDownNode);
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

		// skip if no options
		if (dropDownOptionControllers.size() == 0) return;

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx - 1) % dropDownOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += dropDownOptionControllers.size();

		// select
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).select();
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX(dropDownNode);
		dropDownOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY(dropDownNode);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// check if our node was clicked
		if (disabled == false &&
			node == this.dropDownNode &&
			node.isEventBelongingToNode(event) &&  
			event.getButton() == 1) {
			// set event processed
			event.setProcessed(true);

			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				toggleOpenState();
	
				// set focussed node
				node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)this.node);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		if (disabled == false &&
			node == this.node) {
			//
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
	public MutableString getValue() {
		//
		value.reset();

		// determine select box option controllers
		determineDropDownOptionControllers();

		//
		for (int i = 0; i < dropDownOptionControllers.size(); i++) {
			GUIDropDownOptionController dropDownOptionController = dropDownOptionControllers.get(i);
			if (dropDownOptionController.isSelected() == true) {
				value.append(((GUIElementNode)dropDownOptionController.getNode()).getValue());
			}
		}

		//
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		// determine select box option controllers
		determineDropDownOptionControllers();

		// unselect all selections
		unselect();

		// determine new selection
		for (int i = 0; i < dropDownOptionControllers.size(); i++) {
			GUIDropDownOptionController dropDownOptionController = dropDownOptionControllers.get(i);
			GUIElementNode dropDownOptionNode = ((GUIElementNode)dropDownOptionController.getNode());
			if (value.equals(dropDownOptionNode.getValue())) {
				dropDownOptionController.select();
				dropDownOptionNode.scrollToNodeX(dropDownNode);
				dropDownOptionNode.scrollToNodeY(dropDownNode);
				break;
			}
		}
	}

}

package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI select box controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBoxController extends GUINodeController {

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private ArrayList<GUISelectBoxOptionController> selectBoxOptionControllers = new ArrayList<GUISelectBoxOptionController>();

	private boolean disabled;
	private MutableString value = new MutableString();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxController(GUINode node) {
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
		nodeConditions.remove(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);
		this.disabled = disabled;
		nodeConditions.add(this.disabled == true?CONDITION_DISABLED:CONDITION_ENABLED);

		//
		selectCurrent();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#initialize()
	 */
	public void initialize() {
		// no op
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
	 * Select current option
	 */
	protected void selectCurrent() {
		setValue(getValue());
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

		// skip if no options
		if (selectBoxOptionControllers.size() == 0) return;

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx + 1) % selectBoxOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += selectBoxOptionControllers.size();

		// select
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).select();
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX((GUIParentNode)node);
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY((GUIParentNode)node);
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

		// skip if no options
		if (selectBoxOptionControllers.size() == 0) return;

		// determine new selection idx
		selectBoxOptionControllerIdx = (selectBoxOptionControllerIdx - 1) % selectBoxOptionControllers.size();
		if (selectBoxOptionControllerIdx < 0) selectBoxOptionControllerIdx += selectBoxOptionControllers.size();

		// select
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).select();
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeX((GUIParentNode)node);
		selectBoxOptionControllers.get(selectBoxOptionControllerIdx).getNode().scrollToNodeY((GUIParentNode)node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// disabled
		boolean disabled = ((GUISelectBoxController)this.node.getController()).isDisabled();

		// check if our node was clicked
		if (disabled == false &&
			node == this.node &&
			node.isEventBelongingToNode(event) &&
			event.getButton() == 1) {
			// set event processed
			event.setProcessed(true);

			// check if mouse released
			if (event.getType() == Type.MOUSE_PRESSED) {
				// set focussed node
				node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)node);
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
			switch (event.getKeyCode()) {
				case GUIKeyboardEvent.KEYCODE_UP:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							selectPrevious();

							// delegate change event
							node.getScreenNode().delegateValueChanged((GUIElementNode)node);
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

							// delegate change event
							node.getScreenNode().delegateValueChanged((GUIElementNode)node);
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
		value.reset();

		// determine select box option controllers
		determineSelectBoxOptionControllers();

		// return selected value if exists
		for (int i = 0; i < selectBoxOptionControllers.size(); i++) {
			GUISelectBoxOptionController selectBoxOptionController = selectBoxOptionControllers.get(i);
			if (selectBoxOptionController.isSelected() == true) {
				value.append(((GUIElementNode)selectBoxOptionController.getNode()).getValue());
			}
		}

		// otherwise return empty string
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		// determine select box option controllers
		determineSelectBoxOptionControllers();

		// unselect all selections
		unselect();

		// determine new selection
		for (int i = 0; i < selectBoxOptionControllers.size(); i++) {
			GUISelectBoxOptionController selectBoxOptionController = selectBoxOptionControllers.get(i);
			GUIElementNode selectBoxOptionNode = (GUIElementNode)selectBoxOptionController.getNode();
			if (value.equals(selectBoxOptionNode.getValue()) == true) {
				selectBoxOptionController.select();
				selectBoxOptionNode.scrollToNodeX((GUIParentNode)node);
				selectBoxOptionNode.scrollToNodeY((GUIParentNode)node);
				break;
			}
		}
	}

}

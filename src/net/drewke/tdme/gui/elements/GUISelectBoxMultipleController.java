package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI select box multiple controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBoxMultipleController extends GUINodeController {

	private final static char VALUE_DELIMITER = '|';

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();
	private ArrayList<GUISelectBoxMultipleOptionController> selectBoxMultipleOptionControllers = new ArrayList<GUISelectBoxMultipleOptionController>();

	private MutableString value = new MutableString();
	private MutableString searchValue = new MutableString();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxMultipleController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
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
			if (childController instanceof GUISelectBoxMultipleOptionController) {
				((GUISelectBoxMultipleOptionController)childController).unselect();
			}
		}
	}

	/**
	 * Unfocus all nodes
	 */
	protected void unfocus() {
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUISelectBoxMultipleOptionController) {
				((GUISelectBoxMultipleOptionController)childController).unfocus();
			}
		}
	}

	/**
	 * Determine select box option controllers
	 */
	private void determineSelectBoxMultipleOptionControllers() {
		selectBoxMultipleOptionControllers.clear();
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController();
			if (childController instanceof GUISelectBoxMultipleOptionController) {
				selectBoxMultipleOptionControllers.add((GUISelectBoxMultipleOptionController)childController);
			}
		}
	}

	/**
	 * Get focussed option idx
	 */
	private int getFocussedOptionIdx() {
		int selectBoxOptionControllerIdx = -1;
		for (int i = 0; i < selectBoxMultipleOptionControllers.size(); i++) {
			GUISelectBoxMultipleOptionController selectBoxOptionController = selectBoxMultipleOptionControllers.get(i);
			// determine selected idx
			if (selectBoxOptionController.isFocussed() == true) {
				selectBoxOptionControllerIdx = i;
				break;
			}
		}

		//
		return selectBoxOptionControllerIdx;
	}

	/**
	 * Focus next node
	 */
	protected void focusNext() {
		// determine select box option controllers
		determineSelectBoxMultipleOptionControllers();

		// determine current selected option index
		int selectBoxMultipleOptionControllerIdx = getFocussedOptionIdx();

		// unfocus
		unfocus();

		// determine new selection idx
		selectBoxMultipleOptionControllerIdx = (selectBoxMultipleOptionControllerIdx + 1) % selectBoxMultipleOptionControllers.size();
		if (selectBoxMultipleOptionControllerIdx < 0) selectBoxMultipleOptionControllerIdx += selectBoxMultipleOptionControllers.size();

		// focus
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).focus();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeX();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeY();
	}

	/**
	 * Focus previous
	 */
	protected void focusPrevious() {
		// determine select box option controllers
		determineSelectBoxMultipleOptionControllers();

		// determine current selected option index
		int selectBoxMultipleOptionControllerIdx = getFocussedOptionIdx();

		// unfocus
		unfocus();

		// determine new selection idx
		selectBoxMultipleOptionControllerIdx = (selectBoxMultipleOptionControllerIdx - 1) % selectBoxMultipleOptionControllers.size();
		if (selectBoxMultipleOptionControllerIdx < 0) selectBoxMultipleOptionControllerIdx += selectBoxMultipleOptionControllers.size();

		// select
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).focus();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeX();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeY();
	}

	/**
	 * Toggle selected node
	 */
	protected void toggle() {
		// determine select box option controllers
		determineSelectBoxMultipleOptionControllers();

		// determine current selected option index
		int selectBoxMultipleOptionControllerIdx = getFocussedOptionIdx();

		// select
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).toggle();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeX();
		selectBoxMultipleOptionControllers.get(selectBoxMultipleOptionControllerIdx).getNode().scrollToNodeY();
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
				case GUIKeyboardEvent.KEYCODE_UP:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							focusPrevious();
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_DOWN:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							focusNext();
						}
					}
					break;
				case GUIKeyboardEvent.KEYCODE_SPACE:
					{
						// set event processed
						event.setProcessed(true);
	
						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							toggle();
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
		determineSelectBoxMultipleOptionControllers();

		// return selected values if exists
		for (int i = 0; i < selectBoxMultipleOptionControllers.size(); i++) {
			GUISelectBoxMultipleOptionController selectBoxOptionController = selectBoxMultipleOptionControllers.get(i);
			if (selectBoxOptionController.isSelected() == true) {
				value.append(((GUIElementNode)selectBoxOptionController.getNode()).getValue());
				value.append(VALUE_DELIMITER);
			}
		}

		if (value.length() > 0) {
			value.insert(0, VALUE_DELIMITER);
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
		determineSelectBoxMultipleOptionControllers();

		// unselect all selections
		unselect();

		// determine new selection
		for (int i = 0; i < selectBoxMultipleOptionControllers.size(); i++) {
			GUISelectBoxMultipleOptionController selectBoxOptionController = selectBoxMultipleOptionControllers.get(i);
			GUIElementNode selectBoxOptionNode = (GUIElementNode)selectBoxOptionController.getNode();

			// set up value we search for
			searchValue.reset();
			searchValue.append(VALUE_DELIMITER);
			searchValue.append(selectBoxOptionNode.getValue());
			searchValue.append(VALUE_DELIMITER);

			// check if value
			if (value.indexOf(searchValue) != -1) {
				selectBoxOptionController.select();
				selectBoxOptionNode.scrollToNodeX();
				selectBoxOptionNode.scrollToNodeY();
			}
		}
	}

}

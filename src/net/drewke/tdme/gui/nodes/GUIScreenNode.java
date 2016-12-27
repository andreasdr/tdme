package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIKeyboardEvent.Type;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Screen Node
 * @author andreas
 * @version $Id$
 */
public final class GUIScreenNode extends GUIParentNode {

	private GUIColor foccussedBorderColor = null;

	private int nodeCounter;

	private int screenWidth;
	private int screenHeight;

	private HashMap<String, GUINode> nodesById;

	private ArrayList<GUINode> floatingNodes;

	private boolean invalidateFocussedNode;
	private ArrayList<GUIElementNode> focusableNodes;
	private GUIElementNode focussedNode;

	private GUIColor unfocussedNodeBorderLeftColor;
	private GUIColor unfocussedNodeBorderRightColor;
	private GUIColor unfocussedNodeBorderTopColor;
	private GUIColor unfocussedNodeBorderBottomColor;

	private ArrayList<GUIActionListener> actionListener;
	private ArrayList<GUIChangeListener> changeListener;

	private ArrayList<GUINode> childControllerNodes;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param overflow x
	 * @param overflow y
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 */
	public GUIScreenNode(
		GUIParentNode parentNode, 
		String id, 
		Flow flow,
		Overflow overflowX,
		Overflow overflowY,
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor,
		Border border, 
		Padding padding,
		GUINodeConditions showOn, 
		GUINodeConditions hideOn,
		boolean scrollable
		) throws GUIParserException {
		super(null, parentNode, id, flow, overflowX, overflowY, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.foccussedBorderColor = new GUIColor("#FF4040");
		this.nodeCounter = 0;
		this.screenWidth = 0;
		this.screenHeight = 0;
		this.nodesById = new HashMap<String, GUINode>();
		this.floatingNodes = new ArrayList<GUINode>();
		this.focusableNodes = new ArrayList<GUIElementNode>();
		this.focussedNode = null;
		this.unfocussedNodeBorderLeftColor = null;
		this.unfocussedNodeBorderRightColor = null;
		this.unfocussedNodeBorderTopColor = null;
		this.unfocussedNodeBorderBottomColor = null;
		this.invalidateFocussedNode = true;
		this.actionListener = new ArrayList<GUIActionListener>();
		this.changeListener = new ArrayList<GUIChangeListener>();
		this.childControllerNodes = new ArrayList<GUINode>();
	}

	/**
	 * @return screen width
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * @return screen height
	 */
	public int getScreenHeight() {
		return screenHeight;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return false;
	}

	/**
	 * @return content width
	 */
	public int getContentWidth() {
		return -1;
	}

	/**
	 * @return content height
	 */
	public int getContentHeight() {
		return -1;
	}

	/**
	 * Layout
	 */
	public void layout() {
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}

		// determine screen child controller nodes
		getChildControllerNodes(childControllerNodes);

		// call controller.postLayout()
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode node = childControllerNodes.get(i);
			GUINodeController controller = node.getController();
			if (controller != null) {
				controller.postLayout();
			}
		}
	}

	/**
	 * Set screen size
	 * @param width
	 * @param height
	 */
	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		this.requestedConstraints.widthType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.width = width;
		this.requestedConstraints.heightType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.height = height;
		this.computedConstraints.left = 0;
		this.computedConstraints.top = 0;
		this.computedConstraints.width = width;
		this.computedConstraints.height = height;
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "screen";
	}

	/**
	 * Get GUI node by id
	 * @param nodeId
	 * @return GUI node or null
	 */
	public GUINode getNodeById(String nodeId) {
		return nodesById.get(nodeId); 
	}

	/**
	 * Allocate node id
	 * @return node id
	 */
	public String allocateNodeId() {
		return "tdme_gui_anonymous_node_" + (nodeCounter++);
	}

	/**
	 * Add node
	 * @param node
	 * @return success
	 */
	public boolean addNode(GUINode node) {
		// having a node without a ID is valid, this node will not be added
		if (node.id.length() == 0) {
			node.id = allocateNodeId();
		}

		// check if we have a node registered already
		if (nodesById.get(node.id) != null) {
			return false;
		}

		// nope, add node
		nodesById.put(node.id, node);
		return true;
	}

	/**
	 * @return focussed border color
	 */
	public GUIColor getFoccussedBorderColor() {
		return foccussedBorderColor;
	}

	/**
	 * Invalidate focussed node
	 */
	public void invalidateFocussedNode() {
		unfocusNode();
		focussedNode = null;
		invalidateFocussedNode = true;
	}

	/**
	 * Render
	 * @param guiRenderer
	 */
	public void render(GUIRenderer guiRenderer) {
		if (invalidateFocussedNode == true) {
			invalidateFocussedNode = false;
			focusNextNode();
		}
		floatingNodes.clear();
		super.render(guiRenderer, floatingNodes);
		for (int i = 0; i < floatingNodes.size(); i++) {
			floatingNodes.get(i).render(guiRenderer, null);
		}
	}

	/**
	 * Determine focussed nodes
	 * @param parent node
	 */
	private void determineFocussedNodes(GUIParentNode parentNode) {
		// check if conditions were met
		if (parentNode.conditionsMet == false) {
			return;
		}

		// check if parent node is GUIElementNode and focusable
		if (parentNode instanceof GUIElementNode && ((GUIElementNode)parentNode).focusable == true) {
			// yep, we have a focusable node
			focusableNodes.add((GUIElementNode)parentNode);
		}

		// check child nodes
		for (int i = 0; i < parentNode.subNodes.size(); i++) {
			GUINode subNode = parentNode.subNodes.get(i);
			// check if parent node
			if (subNode instanceof GUIParentNode) {
				// yep, do the recursion
				determineFocussedNodes((GUIParentNode)subNode);
			}
			// ignore normal nodes
		}
	}

	/**
	 * Determine focussed nodes
	 */
	private void determineFocussedNodes() {
		focusableNodes.clear();
		determineFocussedNodes(this);
	}

	/**
	 * @return focussed node
	 */
	public GUIElementNode getFocussedNode() {
		return this.focussedNode;
	}

	/**
	 * Unfocus current focussed node
	 */
	public void unfocusNode() {
		// unfocus current focussed element
		if (focussedNode != null) {
			focussedNode.getActiveConditions().remove(GUIElementNode.CONDITION_FOCUS);
			focussedNode.border.topColor = unfocussedNodeBorderTopColor;
			focussedNode.border.leftColor = unfocussedNodeBorderLeftColor;
			focussedNode.border.bottomColor = unfocussedNodeBorderBottomColor;
			focussedNode.border.rightColor = unfocussedNodeBorderRightColor;
			if (focussedNode.controller != null) focussedNode.controller.onFocusLost();
		}
	}

	/**
	 * Focus current focussed node
	 */
	public void focusNode() {
		// focus new focus node
		if (focussedNode != null) {
			focussedNode.getActiveConditions().add(GUIElementNode.CONDITION_FOCUS);
			unfocussedNodeBorderTopColor = focussedNode.border.topColor;
			unfocussedNodeBorderLeftColor = focussedNode.border.leftColor;
			unfocussedNodeBorderBottomColor = focussedNode.border.bottomColor;
			unfocussedNodeBorderRightColor = focussedNode.border.rightColor;
			focussedNode.border.topColor = foccussedBorderColor;
			focussedNode.border.leftColor = foccussedBorderColor;
			focussedNode.border.bottomColor = foccussedBorderColor;
			focussedNode.border.rightColor = foccussedBorderColor;
			if (focussedNode.controller != null) focussedNode.controller.onFocusGained();
		}
	}

	/**
	 * Set focussed node
	 * @param foccussed node
	 */
	public void setFoccussedNode(GUIElementNode newFoccussedNode) {
		// skip if already set up as focussed node
		if (this.focussedNode == newFoccussedNode) {
			return;
		}

		// unfocus current focussed element
		unfocusNode();

		// set up new focus node
		this.focussedNode = newFoccussedNode;

		// focus current focussed element
		focusNode();

		// determine focussable nodes
		determineFocussedNodes();
	}

	/**
	 * Focus next node
	 */
	public void focusNextNode() {
		// determine focussable nodes
		determineFocussedNodes();

		// unfocus current focussed element
		unfocusNode();

		// check if we have focussable nodes
		if (focusableNodes.size() > 0) {
			// determine current focussed node idx
			int focussedNodeIdx = -1;
			for (int i = 0; i < focusableNodes.size(); i++) {
				if (focussedNode == focusableNodes.get(i)) {
					focussedNodeIdx = i;
				}
			}
	
			// choose next
			int focussedNextNodeIdx = (focussedNodeIdx + 1) % focusableNodes.size();
			focussedNode = focusableNodes.get(focussedNextNodeIdx);
	
			// focus current focussed element
			focusNode();

			// scroll to node
			focussedNode.scrollToNodeX();
			focussedNode.scrollToNodeY();
		}
	}

	/**
	 * Focus next node
	 */
	public void focusPreviousNode() {
		// determine focussable nodes
		determineFocussedNodes();

		// unfocus current focussed element
		unfocusNode();

		// check if we have focussable nodes
		if (focusableNodes.size() > 0) {
			// determine current focussed node idx
			int focussedNodeIdx = -1;
			for (int i = 0; i < focusableNodes.size(); i++) {
				if (focussedNode == focusableNodes.get(i)) {
					focussedNodeIdx = i;
				}
			}
	
			// choose previous
			int focussedPreviousNodeIdx = (focussedNodeIdx - 1) % focusableNodes.size();
			if (focussedPreviousNodeIdx < 0) focussedPreviousNodeIdx+= focusableNodes.size(); 
			focussedNode = focusableNodes.get(focussedPreviousNodeIdx);
	
			// focus current focussed element
			focusNode();

			// scroll to node
			focussedNode.scrollToNodeX();
			focussedNode.scrollToNodeY();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleKeyboardEvent(net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
		// handle focussing
		switch(event.getKeyCode()) {
			case(GUIKeyboardEvent.KEYCODE_TAB):
				{
					if (event.getType() == Type.KEY_RELEASED) {
						if (event.isShiftDown() == true) {
							focusPreviousNode();
						} else {
							focusNextNode();
						}
					}
					event.setProcessed(true);
					break;
				}
			default:
				{
					break;
				}
		}

		// delegate keyboard event to node if not yet processed
		if (event.isProcessed() == false &&
			focussedNode != null) {
			focussedNode.handleKeyboardEvent(event);
		}
	}

	/**
	 * Add action listener
	 * @param listener
	 */
	public void addActionListener(GUIActionListener listener) {
		actionListener.add(listener);
	}

	/**
	 * Remove action listener
	 * @param listener
	 */
	public void removeActionListener(GUIActionListener listener) {
		actionListener.remove(listener);
	}

	/**
	 * Delegate action performed
	 * @param node
	 */
	public void delegateActionPerformed(GUIActionListener.Type type, GUIElementNode node) {
		for (int i = 0; i < actionListener.size(); i++) {
			actionListener.get(i).onActionPerformed(type, node);
		}
	}

	/**
	 * Add change listener
	 * @param listener
	 */
	public void addChangeListener(GUIChangeListener listener) {
		changeListener.add(listener);
	}

	/**
	 * Remove change listener
	 * @param listener
	 */
	public void removeChangeListener(GUIChangeListener listener) {
		changeListener.remove(listener);
	}

	/**
	 * Delegate value changed
	 * @param node
	 */
	public void delegateValueChanged(GUIElementNode node) {
		for (int i = 0; i < changeListener.size(); i++) {
			changeListener.get(i).onValueChanged(node);
		}
	}

	/**
	 * Get values
	 * @param values
	 */
	public void getValues(HashMap<String, String> values) {
		// clear values
		values.clear();

		// determine screen child controller nodes
		getChildControllerNodes(childControllerNodes);

		// iterate nodes
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);

			// skip on non element nodes
			if (childControllerNode instanceof GUIElementNode == false) continue;

			// cast to element node
			GUIElementNode guiElementNode = ((GUIElementNode)childControllerNode);

			// get controller
			GUINodeController guiElementNodeController = guiElementNode.getController();

			// does the controller provides a value
			if (guiElementNodeController.hasValue()) {
				String name = guiElementNode.getName();
				String value = guiElementNodeController.getValue();
				String currentValue = values.get(name);

				// if not yet set, do it
				if (currentValue == null || currentValue.length() == 0) {
					values.put(name, value);
				}
			}
		}
	}

	/**
	 * Set values
	 * @param values
	 */
	public void setValues(HashMap<String, String> values) {
		// determine screen child controller nodes
		getChildControllerNodes(childControllerNodes);

		// iterate nodes
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);

			// skip on non element nodes
			if (childControllerNode instanceof GUIElementNode == false) continue;

			// cast to element node
			GUIElementNode guiElementNode = ((GUIElementNode)childControllerNode);

			// get controller
			GUINodeController guiElementNodeController = guiElementNode.getController();

			// does the controller provides a value
			if (guiElementNodeController.hasValue()) {
				String name = guiElementNode.getName();
				String newValue = values.get(name);
				if (newValue == null) continue;
				guiElementNodeController.setValue(newValue);
			}
		}
	}

}

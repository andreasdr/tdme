package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import com.sun.scenario.effect.Effect;

import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.effects.GUIEffect;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.events.GUIInputEventHandler;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI Screen Node
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIScreenNode extends GUIParentNode {

	private GUI gui;

	private int nodeCounter;

	private int screenWidth;
	private int screenHeight;

	private HashMap<String, GUINode> nodesById;

	private ArrayList<GUINode> floatingNodes;

	private ArrayList<GUIActionListener> actionListener;
	private ArrayList<GUIChangeListener> changeListener;
	private GUIInputEventHandler inputEventHandler;

	private ArrayList<GUINode> childControllerNodes;

	protected boolean mouseEventProcessedByFloatingNode;

	protected boolean visible;
	protected boolean popUp;

	private HashMap<String, GUIEffect> effects;

	// GUI effect offset x,y
	private int guiEffectOffsetX = 0;
	private int guiEffectOffsetY = 0;

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
	 * @param scrollable
	 * @param pop up
	 */
	public GUIScreenNode( 
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
		boolean scrollable,
		boolean popUp
		) throws GUIParserException {
		super(null, null, id, flow, overflowX, overflowY, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.gui = null;
		this.nodeCounter = 0;
		this.screenWidth = 0;
		this.screenHeight = 0;
		this.nodesById = new HashMap<String, GUINode>();
		this.floatingNodes = new ArrayList<GUINode>();
		this.actionListener = new ArrayList<GUIActionListener>();
		this.changeListener = new ArrayList<GUIChangeListener>();
		this.inputEventHandler = null;
		this.childControllerNodes = new ArrayList<GUINode>();
		this.screenNode = this;
		this.parentNode = null;
		this.visible = true;
		this.popUp = popUp;
		this.effects = new HashMap<String, GUIEffect>();
	}

	/**
	 * @return GUI
	 */
	public GUI getGUI() {
		return gui;
	}

	/**
	 * Set GUI
	 * @param gui
	 */
	public void setGUI(GUI gui) {
		this.gui = gui;
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

	/**
	 * @return is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Set visible
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (gui != null) gui.invalidateFocussedNode();
	}

	/**
	 * @return is pop up
	 */
	public boolean isPopUp() {
		return popUp;
	}

	/**
	 * Set pop up
	 * @param pop up
	 */
	public void setPopUp(boolean popUp) {
		this.popUp = popUp;
	}

	/**
	 * @return floating nodes
	 */
	public ArrayList<GUINode> getFloatingNodes() {
		return floatingNodes;
	}

	/**
	 * @return GUI effect offset X
	 */
	public int getGUIEffectOffsetX() {
		return guiEffectOffsetX;
	}

	/**
	 * Set GUI effect offset X
	 * @param gui effect offset X
	 */
	public void setGUIEffectOffsetX(int guiEffectOffsetX) {
		this.guiEffectOffsetX = guiEffectOffsetX;
	}

	/**
	 * @return GUI effect offset Y 
	 */
	public int getGUIEffectOffsetY() {
		return guiEffectOffsetY;
	}

	/**
	 * Set GUI effect offset Y
	 * @param gui effect offset Y
	 */
	public void setGUIEffectOffsetY(int guiEffectOffsetY) {
		this.guiEffectOffsetY = guiEffectOffsetY;
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
	 * Layout node
	 * @param node
	 */
	public void layout(GUINode node) {
		//
		node.layout();

		// call controller.postLayout()
		GUINodeController controller = node.getController();
		if (controller != null) {
			controller.postLayout();
		}
	}

	/**
	 * Layout sub nodes
	 * @param parent node 
	 */
	public void layoutSubNodes(GUIParentNode parentNode) {
		parentNode.layoutSubNodes();

		// determine screen child controller nodes
		parentNode.getChildControllerNodes(childControllerNodes);

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
	protected boolean addNode(GUINode node) {
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
	 * Add node
	 * @param node
	 * @return success
	 */
	protected boolean removeNode(GUINode node) {
		nodesById.remove(node.id);
		if (node instanceof GUIParentNode) {
			GUIParentNode parentNode = (GUIParentNode)node;
			for (int i = 0; i < parentNode.subNodes.size(); i++) {
				removeNode(parentNode.subNodes.get(i));
			}
		}
		return true;
	}

	/**
	 * Render screen
	 * @param gui renderer
	 */
	public void render(GUIRenderer guiRenderer) {
		// init screen
		guiRenderer.initScreen(this);

		// update and apply effects
		for (GUIEffect effect: effects.getValuesIterator()) {
			if (effect.isActive() == true) {
				effect.update(guiRenderer);
				effect.apply(guiRenderer);
			}
		}

		// clear floating nodes
		floatingNodes.clear();

		// render
		super.render(guiRenderer, floatingNodes);

		// done screen
		guiRenderer.doneScreen();
	}

	/**
	 * Render floating nodes
	 * @param gui renderer
	 */
	public void renderFloatingNodes(GUIRenderer guiRenderer) {
		// init screen
		guiRenderer.initScreen(this);

		// apply effects
		for (GUIEffect effect: effects.getValuesIterator()) {
			effect.apply(guiRenderer);
		}

		// render floating nodes
		for (int i = 0; i < floatingNodes.size(); i++) {
			floatingNodes.get(i).render(guiRenderer, null);
		}

		// done screen
		guiRenderer.doneScreen();
	}

	/**
	 * Determine focussed nodes
	 * @param parent node
	 * @param focusable nodes
	 */
	public void determineFocussedNodes(GUIParentNode parentNode, ArrayList<GUIElementNode> focusableNodes) {
		// check if conditions were met
		if (parentNode.conditionsMet == false) {
			return;
		}

		// check if parent node is GUIElementNode and focusable
		if (parentNode instanceof GUIElementNode) { 
			GUIElementNode parentElementNode = (GUIElementNode)parentNode;
			if (parentElementNode.focusable == true &&
				(parentElementNode.getController() == null || parentElementNode.getController().isDisabled() == false)) {
				// yep, we have a focusable node
				focusableNodes.add((GUIElementNode)parentNode);
			}
		}

		// check child nodes
		for (int i = 0; i < parentNode.subNodes.size(); i++) {
			GUINode subNode = parentNode.subNodes.get(i);
			// check if parent node
			if (subNode instanceof GUIParentNode) {
				// yep, do the recursion
				determineFocussedNodes((GUIParentNode)subNode, focusableNodes);
			}
			// ignore normal nodes
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUIParentNode#handleMouseEvent(net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUIMouseEvent event) {
		mouseEventProcessedByFloatingNode = false;
		for (int i = 0; i < floatingNodes.size(); i++) {
			GUINode floatingNode = floatingNodes.get(i);
			floatingNode.handleMouseEvent(event);
		}
		super.handleMouseEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleKeyboardEvent(net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
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
	 * @return input event handler
	 */
	public GUIInputEventHandler getInputEventHandler() {
		return inputEventHandler;
	}

	/**
	 * Set input event handler 
	 * @param input event handler
	 */
	public void setInputEventHandler(GUIInputEventHandler inputEventHandler) {
		this.inputEventHandler = inputEventHandler;
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
	public void getValues(HashMap<String, MutableString> values) {
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
				MutableString value = guiElementNodeController.getValue();
				MutableString currentValue = values.get(name);

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
	public void setValues(HashMap<String, MutableString> values) {
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
				MutableString newValue = values.get(name);
				if (newValue == null) continue;
				guiElementNodeController.setValue(newValue);
			}
		}
	}

	/**
	 * Add effect that will be removed if finished
	 * @param id
	 * @param effect
	 * @return success
	 */
	public boolean addEffect(String id, GUIEffect effect) {
		// check if effect with given id already exists
		if (effects.get(id) != null) {
			return false;
		}
	
		// add effect
		effects.put(id, effect);

		// return positively
		return true;
	}

	/**
	 * Get effect
	 * @param id
	 * @return effect or null
	 */
	public GUIEffect getEffect(String id) {
		return effects.get(id);
	}

	/**
	 * Remove effect
	 * @param id
	 * @return success
	 */
	public boolean removeEffect(String id) {
		return effects.remove(id) != null;
	}

}

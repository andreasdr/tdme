package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIRadioButtonController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private boolean selected;
	private boolean disabled;

	private static HashMap<String, ArrayList<GUIElementNode>> radioButtonGroupNodesByName = new HashMap<String, ArrayList<GUIElementNode>>();

	private MutableString value = new MutableString();

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUIRadioButtonController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.selected = ((GUIElementNode)node).isSelected();
		this.disabled = ((GUIElementNode)node).isDisabled();

		// add to nodes
		//	check if group exists
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
		// nope?
		if (radioButtonGroupNodes == null) {
			// ok, create
			radioButtonGroupNodes = new ArrayList<GUIElementNode>();
			radioButtonGroupNodesByName.put(node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)node).getName(), radioButtonGroupNodes);
		}
		// add node
		radioButtonGroupNodes.add((GUIElementNode)node);
	}

	/**
	 * @return is checked
	 */
	protected boolean isSelected() {
		return selected;
	}

	/**
	 * Select
	 * @param checked
	 */
	protected void select() {
		ArrayList<GUIElementNode> radioButtonGroupNodes = radioButtonGroupNodesByName.get(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
		// unselect all radio buttons
		if (radioButtonGroupNodes != null) {
			for (int i = 0; i < radioButtonGroupNodes.size(); i++) {
				GUIElementNode radioButtonNode = radioButtonGroupNodes.get(i);
				GUINodeConditions nodeConditions = radioButtonNode.getActiveConditions();
				GUIRadioButtonController nodeController = (GUIRadioButtonController)radioButtonNode.getController();
				nodeConditions.remove(nodeController.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
				nodeController.selected = false;
				nodeConditions.add(nodeController.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);				
			}
		}

		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = true;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
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
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#initialize()
	 */
	public void initialize() {
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		setDisabled(disabled);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
		radioButtonGroupNodesByName.remove(this.node.getScreenNode().getId() + "_radiobuttongroup_" + ((GUIElementNode)this.node).getName());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#postLayout()
	 */
	public void postLayout() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// check if our node was clicked
		if (disabled == false &&
			node == this.node &&
			node.isEventBelongingToNode(event) &&  
			event.getButton() == 1) {
			// set event processed
			event.setProcessed(true);

			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				select();
	
				// set focussed node
				node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)node);

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)node);
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
				case GUIKeyboardEvent.KEYCODE_SPACE:
					{
						// set event processed
						event.setProcessed(true);
		
						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							// select
							select();

							// delegate change event
							node.getScreenNode().delegateValueChanged((GUIElementNode)node);
						}
					}
					break;
				default:
					{
						break;
					}
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
		if (selected == true) {
			value.append(((GUIElementNode)node).getValue());
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		if (value.equals(((GUIElementNode)node).getValue()) == true) {
			select();
		}
	}

}

package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI drop down option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIDropDownOptionController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private GUIParentNode dropDownNode;
	private boolean selected;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIDropDownOptionController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.selected = ((GUIElementNode)node).isSelected();
	}

	/**
	 * @return is selected
	 */
	protected boolean isSelected() {
		return selected;
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

	/**
	 * Select
	 */
	protected void select() {
		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = true;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);

		// set text from option to heading text
		GUITextNode dropDownOptionTextNode = (GUITextNode)node.getScreenNode().getNodeById(node.getId() + "_unselected");
		GUITextNode dropDownTextNodeEnabled = (GUITextNode)node.getScreenNode().getNodeById(dropDownNode.getId() + "_text_enabled");
		dropDownTextNodeEnabled.getText().reset();
		dropDownTextNodeEnabled.getText().append(dropDownOptionTextNode.getText());
		GUITextNode dropDownTextNodeDisabled = (GUITextNode)node.getScreenNode().getNodeById(dropDownNode.getId() + "_text_disabled");
		dropDownTextNodeDisabled.getText().reset();
		dropDownTextNodeDisabled.getText().append(dropDownOptionTextNode.getText());
	}

	/**
	 * Unselect
	 * @param checked
	 */
	protected void unselect() {
		// unselect current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = false;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void initialize() {
		// determine drop down node
		dropDownNode = node.getParentControllerNode();
		while(true == true) {
			if (dropDownNode.getController() instanceof GUIDropDownController) {
				break;
			}
			dropDownNode = dropDownNode.getParentControllerNode();
		}

		// init state
		if (selected == true) {
			select();
		} else {
			unselect();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#postLayout()
	 */
	public void postLayout() {
		if (selected == true) {
			node.scrollToNodeX(dropDownNode);
			node.scrollToNodeY(dropDownNode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
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
				//
				((GUIDropDownController)dropDownNode.getController()).unselect();
				select();
				((GUIDropDownController)dropDownNode.getController()).toggleOpenState();

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)dropDownNode);

				//
				node.scrollToNodeX(dropDownNode);
				node.scrollToNodeY(dropDownNode);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		// no op for now
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

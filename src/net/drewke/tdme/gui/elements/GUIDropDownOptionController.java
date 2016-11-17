package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUITextNode;

/**
 * GUI select box option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIDropDownOptionController extends GUINodeController {

	private static final String CONDITION_SELECTED = "selected";
	private static final String CONDITION_UNSELECTED = "unselected";

	private GUINode dropDownNode;
	private boolean selected;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIDropDownOptionController(GUINode node) {
		super(node);
		this.selected = false;
	}

	/**
	 * Select
	 */
	public void select() {
		// select current
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = true;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);

		// set text from option to heading text
		GUITextNode dropDownOptionTextNode = (GUITextNode)node.getScreenNode().getNodeById(node.getId() + "_unselected");
		GUITextNode dropDownTextNode = (GUITextNode)node.getScreenNode().getNodeById(dropDownNode.getId() + "_panel_text");
		dropDownTextNode.setText(dropDownOptionTextNode.getText());
	}

	/**
	 * Unselect
	 * @param checked
	 */
	public void unselect() {
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
	public void init() {
		dropDownNode = ((GUIParentNode)node).getParentControllerNode();
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#handleEvent(net.drewke.tdme.gui.GUINode, net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUINode node, GUIMouseEvent event) {
		if (node == this.node &&
			node.isEventBelongingToNode(event) && 
			event.getType() == Type.MOUSE_PRESSED && 
			event.getButton() == 1) {
			//
			((GUIDropDownController)dropDownNode.getController()).unselect();
			select();
			((GUIDropDownController)dropDownNode.getController()).toggleOpenState();
		}
	}

}

package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIMouseEvent.Type;

/**
 * GUI select box option controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUISelectBoxOptionController extends GUINodeController {

	protected static final String CONDITION_SELECTED = "selected";
	protected static final String CONDITION_UNSELECTED = "unselected";

	protected GUINode selectBoxNode;
	protected boolean selected;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxOptionController(GUINode node) {
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
		selectBoxNode = ((GUIParentNode)node).getParentControllerNode();
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
		if (node.id.equals(this.node.id) &&
			node.isEventBelongingToNode(event) && 
			event.type == Type.MOUSE_RELEASED && 
			event.button == 1) {
			//
			((GUISelectBoxController)selectBoxNode.controller).unselect();
			select();
		}
	}

}

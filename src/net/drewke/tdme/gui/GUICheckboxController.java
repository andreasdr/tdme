package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUIMouseEvent.Type;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUICheckboxController extends GUINodeController {

	protected static final String CONDITION_CHECKED = "checked";
	protected static final String CONDITION_UNCHECKED = "unchecked";

	private boolean checked;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUICheckboxController(GUINode node) {
		super(node);
		this.checked = false;
	}

	/**
	 * @return is checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * Set checked
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		GUINodeConditions nodeConditions = ((GUIElementNode)node).getActiveConditions();
		nodeConditions.remove(this.checked == true?CONDITION_CHECKED:CONDITION_UNCHECKED);
		this.checked = checked;
		nodeConditions.add(this.checked == true?CONDITION_CHECKED:CONDITION_UNCHECKED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		setChecked(checked);
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
			setChecked(checked == true?false:true);
		}
	}

}
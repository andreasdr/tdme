package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI tab content controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUITabContentController extends GUINodeController {

	protected String CONDITION_SELECTED;
	protected String CONDITION_UNSELECTED;

	private boolean selected;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITabContentController(GUINode node) {
		super(node);
		this.selected = false;
		CONDITION_SELECTED = node.getId() + "-selected";
		CONDITION_UNSELECTED = node.getId() + "-unselected";
	}

	/**
	 * @return is checked
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set checked
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		// remove old selection condition, add new selection condition
		GUINodeConditions nodeConditions = ((GUIElementNode)this.node.getParentNode()).getActiveConditions();
		nodeConditions.remove(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
		this.selected = selected;
		nodeConditions.add(this.selected == true?CONDITION_SELECTED:CONDITION_UNSELECTED);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// set initial state
		setSelected(selected);
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
		// no op
	}

}

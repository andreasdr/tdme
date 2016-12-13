package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUICheckboxController extends GUINodeController {

	private static final String CONDITION_CHECKED = "checked";
	private static final String CONDITION_UNCHECKED = "unchecked";

	private boolean checked;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUICheckboxController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.checked = ((GUIElementNode)node).isSelected();
	}

	/**
	 * @return is checked
	 */
	protected boolean isChecked() {
		return checked;
	}

	/**
	 * Set checked
	 * @param checked
	 */
	protected void setChecked(boolean checked) {
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
				setChecked(checked == true?false:true);
	
				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)node);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		if (node == this.node) {
			switch (event.getKeyCode()) {
				case GUIKeyboardEvent.KEYCODE_SPACE:
					{
						// set event processed
						event.setProcessed(true);
		
						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							// toggle
							setChecked(checked == true?false:true);
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
	public String getValue() {
		return checked == true?((GUIElementNode)node).getValue():"";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		setChecked(((GUIElementNode)node).getValue().equals(value));
	}

}

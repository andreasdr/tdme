package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeConditions;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI Checkbox controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUICheckboxController extends GUINodeController {

	private static final String CONDITION_CHECKED = "checked";
	private static final String CONDITION_UNCHECKED = "unchecked";

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private boolean checked;
	private boolean disabled;
	private MutableString value;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUICheckboxController(GUINode node) {
		super(node);

		// derive if selected from node default
		this.checked = ((GUIElementNode)node).isSelected();
		this.disabled = ((GUIElementNode)node).isDisabled();
		this.value = new MutableString();
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
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void initialize() {
		setChecked(checked);
		setDisabled(disabled);
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
				setChecked(checked == true?false:true);

				// delegate change event
				node.getScreenNode().delegateValueChanged((GUIElementNode)node);
	
				// set focussed node
				node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)node);
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
							// toggle
							setChecked(checked == true?false:true);

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
		if (checked == true) {
			value.append(((GUIElementNode)node).getValue());
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		setChecked(value.equals(((GUIElementNode)node).getValue()));
	}

}

package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI element controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIElementController extends GUINodeController {

	private static final String CONDITION_DISABLED = "disabled";
	private static final String CONDITION_ENABLED = "enabled";

	private boolean disabled;
	private boolean isActionPerforming;

	/**
	 * GUI element controller
	 * @param node
	 */
	protected GUIElementController(GUINode node) {
		super(node);
		this.isActionPerforming = false;

		//
		this.disabled = ((GUIElementNode)node).isDisabled();
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
	public void init() {
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

			// check if performing
			if (event.getType() == Type.MOUSE_PRESSED) {
				isActionPerforming = true;

				// set focussed node if focusable
				if (((GUIElementNode)node).isFocusable() == true) {
					node.getScreenNode().getGUI().setFoccussedNode((GUIElementNode)node);
				}
			} else
			if (event.getType() == Type.MOUSE_DRAGGED) {
				isActionPerforming = true;
			} else
			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				isActionPerforming = false;

				// delegate action performed
				node.getScreenNode().delegateActionPerformed(GUIActionListener.Type.PERFORMED, (GUIElementNode)node);
			}
		} else {
			// no anymore performing
			isActionPerforming = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		if (disabled == false &&
			node == this.node) {
			switch (event.getKeyCode()) {
				case GUIKeyboardEvent.KEYCODE_SPACE:
					{
						// set event processed
						event.setProcessed(true);

						// check if key pressed
						if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
							// delegate action performed
							node.getScreenNode().delegateActionPerformed(GUIActionListener.Type.PERFORMED, (GUIElementNode)node);
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
		// check if performing
		if (isActionPerforming == true) {
			if (disabled == true) {
				isActionPerforming = false;
				return;
			}
			// yep, delegate action performing
			node.getScreenNode().delegateActionPerformed(GUIActionListener.Type.PERFORMING, (GUIElementNode)node);
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

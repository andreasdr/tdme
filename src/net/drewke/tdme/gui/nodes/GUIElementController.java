package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;

/**
 * GUI element controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIElementController extends GUINodeController {

	private boolean isActionPerforming;

	/**
	 * GUI element controller
	 * @param node
	 */
	protected GUIElementController(GUINode node) {
		super(node);
		this.isActionPerforming = false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
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

			// check if performing
			if (event.getType() == Type.MOUSE_PRESSED ||
				event.getType() == Type.MOUSE_DRAGGED) {
				isActionPerforming = true;
			} else
			// check if mouse released
			if (event.getType() == Type.MOUSE_RELEASED) {
				//
				isActionPerforming = false;

				// delegate action performed
				node.getScreenNode().delegateActionPerformed(GUIActionListener.Type.PERFORMED, (GUIElementNode)node);

				// set focussed node
				node.getScreenNode().setFoccussedNode((GUIElementNode)node);
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
		if (node == this.node) {
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
	public String getValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		// no op
	}

}

package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;

/**
 * GUI input controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITextInputController extends GUINodeController {

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITextInputController(GUINode node) {
		super(node);
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
		if (node == this.node &&
			node.isEventBelongingToNode(event) && 
			event.getType() == Type.MOUSE_RELEASED && 
			event.getButton() == 1) {
			//
			System.out.println(node.getId() + ": INPUT CLICKED");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		GUITextInputNode textInputNode = ((GUITextInputNode)node);
		switch (event.getType()) {
			case KEY_PRESSED:
				{
					textInputNode.setText(textInputNode.getText() + event.getKeyChar());
					break;
				}
			case KEY_RELEASED:
				{
					break;
				}
			default:
				{
					break;
				}
		}
	}

}
package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;

/**
 * GUI panel node
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIPanelNode extends GUILayoutNode {

	/**
	 * Public constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param overflow x
	 * @param overflow y
	 * @param alignments
	 * @param requested constraints
	 * @param background color
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param alignment
	 * @throws GUIParserException
	 */
	public GUIPanelNode(
		GUIScreenNode screenNode, 
		GUIParentNode parentNode,
		String id, 
		Flow flow, 
		Overflow overflowX, 
		Overflow overflowY,
		Alignments alignments,
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor, 
		Border border, 
		Padding padding,
		GUINodeConditions showOn, 
		GUINodeConditions hideOn,
		Alignment alignment) throws GUIParserException {
		//
		super(
			screenNode, 
			parentNode, 
			id, 
			flow, 
			overflowX, 
			overflowY,
			alignments, 
			requestedConstraints, 
			backgroundColor, 
			border,
			padding, 
			showOn, 
			hideOn, 
			alignment
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUILayoutNode#getNodeType()
	 */
	protected String getNodeType() {
		return "panel";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleMouseEvent(net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUIMouseEvent event) {
		super.handleMouseEvent(event);

		// if event belongs to panel node mark event as processed
		if (isEventBelongingToNode(event) == true) {
			event.setProcessed(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUIParentNode#handleKeyboardEvent(net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
		super.handleKeyboardEvent(event);
	}

}

package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.renderer.GUIRenderer;

/**
 * GUI Panel
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIPanelNode extends GUILayoutNode {

	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param background color
	 * @param background image
	 * @param alignment
	 */
	public GUIPanelNode(
		GUIScreenNode screenNode,
		GUIParentNode parentNode, 
		String id, 
		Flow flow,
		Alignments alignments,
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor,
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn,
		Alignment alignment) 
		throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn, alignment);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getNodeType()
	 */
	protected String getNodeType() {
		return "panel";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer) {
		// check if conditions apply
		if (checkConditions() == false) return;

		// call parent renderer
		super.render(guiRenderer);
	}

}

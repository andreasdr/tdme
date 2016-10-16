package net.drewke.tdme.gui;

import java.util.ArrayList;

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
	protected GUIPanelNode(
		GUIScreenNode screenNode,
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments,
		RequestedConstraints requestedConstraints,
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn,
		String backgroundColor,
		String backgroundImage,
		String alignment) 
		throws GUIParserException {
		//
		super(screenNode, parentNode, id, alignments, requestedConstraints, border, padding, showOn, hideOn, backgroundColor, backgroundImage, alignment);
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
	protected void render(GUIRenderer guiRenderer) {
		// check if conditions apply
		if (checkConditions() == false) return;

		// call parent renderer
		super.render(guiRenderer);
	}

}

package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParentNode.Border;
import net.drewke.tdme.gui.GUIParentNode.Margin;

/**
 * GUI Panel
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIPanelNode extends GUILayoutNode {

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 * @param hide on
	 * @param border
	 * @param margin
	 * @param background color
	 * @param background image
	 * @param alignment
	 */
	protected GUIPanelNode(
		GUINode parentNode, 
		String id, 
		Alignments alignments,
		RequestedConstraints requestedConstraints,
		ArrayList<String> showOn, 
		ArrayList<String> hideOn,
		Border border, 
		Margin margin, 
		String backgroundColor,
		String backgroundImage,
		String alignment) 
		throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, showOn, hideOn, border, margin, backgroundColor, backgroundImage, alignment);
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

		// 
		super.render(guiRenderer);
	}

}

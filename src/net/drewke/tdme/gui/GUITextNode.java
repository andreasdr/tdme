package net.drewke.tdme.gui;

import java.io.File;

/**
 * GUI text node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITextNode extends GUIElementChildNode {

	private GUIFont font;
	private GUIColor color;
	private String text;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 * @param font
	 * @param color
	 * @param text
	 */
	protected GUITextNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, String[] showOn, String font, String color, String text) throws Exception {
		super(parentNode, id, alignments, requestedConstraints, showOn);
		this.font = GUI.getFont(font);
		this.color = color == null || color.length() == 0?new GUIColor():new GUIColor(color);
		this.text = text;
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "text";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return font.getTextWidth(text);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return font.getTextHeight(text);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected void render(GUIRenderer guiRenderer) {
		// draw string
		font.drawString(
			guiRenderer, 
			computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.contentAlignmentLeft, 
			computedConstraints.top  + computedConstraints.alignmentTop + computedConstraints.contentAlignmentTop, 
			text, 
			color
		);
	}

}

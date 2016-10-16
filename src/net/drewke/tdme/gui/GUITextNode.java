package net.drewke.tdme.gui;

import java.util.ArrayList;

/**
 * GUI text node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITextNode extends GUINode {

	private GUIFont font;
	private GUIColor color;
	private String text;

	/**
	 * Constructor
	 * @param screen node
	 * @param parent mode
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param font
	 * @param color
	 * @param text
	 * @throws Exception
	 */
	protected GUITextNode(
		GUIScreenNode screenNode, 
		GUIParentNode parentNode,
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints, 
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn, 
		String font, 
		String color, 
		String text
		) throws Exception {
		//
		super(screenNode, parentNode, id, alignments, requestedConstraints, border, padding, showOn, hideOn);
		this.font = GUI.getFont(font);
		this.color = color == null || color.length() == 0?new GUIColor():new GUIColor(color);
		this.text = text;

		// init font
		this.font.init();
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "text";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return font.getTextWidth(text) + border.left + border.right + padding.left + padding.right;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return font.getTextHeight(text) + border.top + border.bottom + padding.top + padding.bottom;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	protected void dispose() {
		// dispose font
		this.font.dispose();
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

		// draw string
		font.drawString(
			guiRenderer, 
			computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.contentAlignmentLeft, 
			computedConstraints.top  + computedConstraints.alignmentTop + computedConstraints.contentAlignmentTop, 
			text, 
			color
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		//
		super.handleEvent(event);
	}

}

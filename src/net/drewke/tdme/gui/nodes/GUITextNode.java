package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.renderer.GUIFont;
import net.drewke.tdme.gui.renderer.GUIRenderer;

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
	 * @param flow
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
	public GUITextNode(
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
		String font, 
		String color, 
		String text
		) throws Exception {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
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
	public void dispose() {
		// dispose font
		this.font.dispose();
		// parent dispose
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions apply
		if (checkConditions() == false) return;

		// call parent renderer
		super.render(guiRenderer, floatingNodes);

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

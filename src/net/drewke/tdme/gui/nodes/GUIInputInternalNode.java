package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.nodes.GUIInputInternalController.CursorMode;
import net.drewke.tdme.gui.renderer.GUIFont;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI input internal node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIInputInternalNode extends GUINode {

	private GUIFont font;
	private GUIColor color;
	private MutableString text;

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
	public GUIInputInternalNode(
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
		MutableString text
		) throws Exception {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.font = GUI.getFont(font);
		this.color = color == null || color.length() == 0?new GUIColor():new GUIColor(color);
		this.text = text;

		// init font
		this.font.init();

		// controller
		this.controller = new GUIInputInternalController(this);
		this.controller.init();
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
	public int getContentWidth() {
		return font.getTextWidth(text) + border.left + border.right + padding.left + padding.right;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	public int getContentHeight() {
		return font.getTextHeight(text) + border.top + border.bottom + padding.top + padding.bottom;
	}

	/**
	 * @return font
	 */
	public GUIFont getFont() {
		return font;
	}

	/**
	 * @return text
	 */
	public MutableString getText() {
		return text;
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
		// controller dispose
		this.controller.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions were met
		if (conditionsMet == false) return;

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

		// get controller
		GUIInputInternalController controller = (GUIInputInternalController)this.controller;

		// check if to show cursor
		if (controller.getCursorMode() == CursorMode.SHOW) {
			// screen dimension
			float screenWidth = guiRenderer.getGUI().getWidth();
			float screenHeight = guiRenderer.getGUI().getHeight();

			// element location and dimensions
			float left = computedConstraints.left + computedConstraints.alignmentLeft + border.left + padding.left + font.getTextIndexX(text, controller.getIndex());
			float top = computedConstraints.top + computedConstraints.alignmentTop + border.top + padding.top;
			float width = 2;
			float height = computedConstraints.height - border.top - border.bottom - padding.top - padding.bottom;

			// background color
			float[] colorData = color.getData();

			// render cursor
			guiRenderer.bindTexture(0);
			guiRenderer.addQuad(
				((left) / (screenWidth / 2f)) - 1f,
				((screenHeight - top) / (screenHeight / 2f)) - 1f,
				colorData[0], colorData[1], colorData[2], colorData[3],
				0f, 1f,
				((left + width) / (screenWidth / 2f)) - 1f,
				((screenHeight - top) / (screenHeight / 2f)) - 1f,
				colorData[0], colorData[1], colorData[2], colorData[3],
				1f, 1f,
				((left + width) / (screenWidth / 2f)) - 1f,
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,
				colorData[0], colorData[1], colorData[2], colorData[3],
				1f, 0f,
				((left) / (screenWidth / 2f)) - 1f,
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,
				colorData[0], colorData[1], colorData[2], colorData[3],
				0f, 0f
			);
			guiRenderer.render();
		}
	}

}

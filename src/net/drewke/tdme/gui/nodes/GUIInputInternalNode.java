package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.elements.GUIInputController;
import net.drewke.tdme.gui.nodes.GUIInputInternalController.CursorMode;
import net.drewke.tdme.gui.renderer.GUIFont;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI input internal node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIInputInternalNode extends GUINode {

	/**
	 * Create max length
	 * @param s
	 * @return max length
	 */
	public static int createMaxLength(String s) {
		try {
			int maxLength = Integer.parseInt(s);
			return maxLength;
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	private GUIFont font;
	private GUIColor color;
	private GUIColor colorDisabled;
	private MutableString text;
	private int maxLength;

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
	 * @param color if disabled
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
		String colorDisabled,
		MutableString text,
		int maxLength
		) throws Exception {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.font = GUI.getFont(font);
		this.color = color == null || color.length() == 0?new GUIColor():new GUIColor(color);
		this.colorDisabled = colorDisabled == null || colorDisabled.length() == 0?new GUIColor():new GUIColor(colorDisabled);
		this.text = text;
		this.maxLength = maxLength;

		// init font
		this.font.initialize();

		// controller
		this.controller = new GUIInputInternalController(this);
		this.controller.initialize();
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
		return font.getLineHeight() + border.top + border.bottom + padding.top + padding.bottom;
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

	/**
	 * @return max length
	 */
	public int getMaxLength() {
		return maxLength;
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

		// get controller
		GUIInputInternalController controller = (GUIInputInternalController)this.controller;
		GUIInputController inputController = (GUIInputController)this.getParentControllerNode().getController();

		// disabled
		boolean disable = inputController.isDisabled();

		// draw string
		font.drawString(
			guiRenderer, 
			computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.contentAlignmentLeft, 
			computedConstraints.top  + computedConstraints.alignmentTop + computedConstraints.contentAlignmentTop, 
			text, 
			controller.getOffset(),
			0,
			disable == false?color:colorDisabled
		);

		// check if to show cursor
		if (screenNode.getGUI().getFocussedNode() == this.parentNode &&
			controller.getCursorMode() == CursorMode.SHOW) {
			// screen dimension
			float screenWidth = guiRenderer.getGUI().getWidth();
			float screenHeight = guiRenderer.getGUI().getHeight();

			// element location and dimensions
			float left = computedConstraints.left + computedConstraints.alignmentLeft + border.left + padding.left + font.getTextIndexX(text, controller.getOffset(), 0, controller.getIndex());
			float top = computedConstraints.top + computedConstraints.alignmentTop + border.top + padding.top;
			float width = 2;
			float height = computedConstraints.height - border.top - border.bottom - padding.top - padding.bottom;

			// background color
			float[] colorData = (disable == false?color:colorDisabled).getArray();

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

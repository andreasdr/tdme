package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.ArrayList;

/**
 * GUI horizontal scrollbar internal node
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIHorizontalScrollbarInternalNode extends GUINode {

	private GUIColor barColorNone;
	private GUIColor barColorMouseOver;
	private GUIColor barColorDragging;

	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param alignments
	 * @param requested constraints
	 * @param background color
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 */
	public GUIHorizontalScrollbarInternalNode(
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
		GUIColor barColorNone,
		GUIColor barColorMouseOver,
		GUIColor barColorDragging) {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		//
		this.controller = new GUIHorizontalScrollbarInternalController(this);
		this.barColorNone = barColorNone;
		this.barColorMouseOver = barColorMouseOver;
		this.barColorDragging = barColorDragging;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#getNodeType()
	 */
	protected String getNodeType() {
		return "scrollbar";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentWidth()
	 */
	public int getContentWidth() {
		// we derive content width from requested -> computed constraints
		return computedConstraints.width;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	public int getContentHeight() {
		// we derive content height from requested -> computed constraints
		return computedConstraints.height;
	}

	/**
	 * Render
	 * 
	 * @param gui renderer
	 * @param floating nodes
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// call parent renderer
		super.render(guiRenderer, floatingNodes);

		// screen dimension
		float screenWidth = guiRenderer.getGUI().getWidth();
		float screenHeight = guiRenderer.getGUI().getHeight();

		// controller
		GUIHorizontalScrollbarInternalController controller = (GUIHorizontalScrollbarInternalController)this.controller;

		// bar constraints
		float barWidth = controller.getBarWidth(); 
		float barLeft = controller.getBarLeft();

		// element location and dimensions
		float left = barLeft;
		float top = computedConstraints.top + computedConstraints.alignmentTop + border.top;
		float width = barWidth;
		float height = computedConstraints.height - border.top - border.bottom;

		// background color
		float[] barColorArray = null;
		switch (controller.getState()) {
			case NONE:
				barColorArray = barColorNone.getArray();
				break;
			case MOUSEOVER:
				barColorArray = barColorMouseOver.getArray();
				break;
			case DRAGGING:
				barColorArray = barColorDragging.getArray();
				break;
		}

		// render background
		guiRenderer.bindTexture(0);
		guiRenderer.addQuad(
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			barColorArray[0], barColorArray[1], barColorArray[2], barColorArray[3],
			0f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			barColorArray[0], barColorArray[1], barColorArray[2], barColorArray[3],
			1f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			barColorArray[0], barColorArray[1], barColorArray[2], barColorArray[3],
			1f, 0f, 
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			barColorArray[0], barColorArray[1], barColorArray[2], barColorArray[3],
			0f, 0f
		);
		guiRenderer.render();
	}

}

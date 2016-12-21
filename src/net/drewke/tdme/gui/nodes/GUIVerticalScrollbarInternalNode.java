package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.renderer.GUIRenderer;

public class GUIVerticalScrollbarInternalNode extends GUINode {

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
	public GUIVerticalScrollbarInternalNode(
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
		this.controller = new GUIVerticalScrollbarInternalController(this);
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

		GUIVerticalScrollbarInternalController controller = (GUIVerticalScrollbarInternalController)this.controller;

		// bar constraints
		float barHeight = controller.getBarHeight(); 
		float barTop = controller.getBarTop();

		// element location and dimensions
		float left = computedConstraints.left + computedConstraints.alignmentLeft + border.left;
		float top = barTop;
		float width = computedConstraints.width - border.left - border.right;
		float height = barHeight;

		// background color
		float[] barColorArray = null;
		switch (controller.getState()) {
			case NONE:
				barColorArray = barColorNone.getData();
				break;
			case MOUSEOVER:
				barColorArray = barColorMouseOver.getData();
				break;
			case DRAGGING:
				barColorArray = barColorDragging.getData();
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

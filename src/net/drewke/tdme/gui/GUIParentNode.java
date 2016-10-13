package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * A parent node supporting child notes
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIParentNode extends GUINode {

	//
	private String backgroundImage;
	private GUIColor backgroundColor;
	protected ArrayList<GUINode> subNodes;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param margin
	 * @param show on
	 * @param hide on
	 * @oaram background color
	 * @param background image 
	 */
	protected GUIParentNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints, 
		Border border, 
		Margin margin, 
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		String backgroundColor,
		String backgroundImage
		) throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, border, margin, showOn, hideOn);
		this.border = border;
		this.margin = margin;
		this.backgroundColor = backgroundColor == null || backgroundColor.length() == 0?GUIColor.TRANSPARENT:new GUIColor(backgroundColor);
		this.backgroundImage = backgroundImage;
		subNodes = new ArrayList<GUINode>();
	}

	/**
	 * @return sub nodes
	 */
	protected ArrayList<GUINode> getSubNodes() {
		return subNodes;
	}

	/**
	 * Create requested constraints
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return requested constraints
	 */
	protected static RequestedConstraints createRequestedConstraints(String left, String top, String width, String height) {
		RequestedConstraints constraints = new RequestedConstraints();
		constraints.leftType = getRequestedConstraintsType(left.trim(), RequestedConstraintsType.PIXEL);
		constraints.left = getRequestedConstraintsValue(left.trim(), 0);
		constraints.topType = getRequestedConstraintsType(top.trim(), RequestedConstraintsType.PIXEL);
		constraints.top = getRequestedConstraintsValue(top.trim(), 0);
		constraints.widthType = getRequestedConstraintsType(width.trim(), RequestedConstraintsType.AUTO);
		constraints.width = getRequestedConstraintsValue(width.trim(), -1);
		constraints.heightType = getRequestedConstraintsType(height.trim(), RequestedConstraintsType.AUTO);
		constraints.height = getRequestedConstraintsValue(height.trim(), -1);
		return constraints;
	}

	/**
	 * Layout
	 */
	protected void layout() {
		super.layout();

		//
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}
	}

	/**
	 * Compute horizontal children alignment
	 */
	protected void computeHorizontalChildrenAlignment() {
		// align all vertical aligned children horizontally
		switch (alignments.horizontal) {
			case LEFT: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = border.left + margin.left; 
					}
					break;
				}
			case CENTER: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width) / 2; 
					}
					break;
				}
			case RIGHT: {
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width - border.right - margin.right); 
					}
					break;
				}
			}
		}
	}

	/**
	 * Compute vertical children alignment
	 */
	protected void computeVerticalChildrenAlignment() {
		// align all horizontal aligned children vertically 
		switch (alignments.vertical) {
			case TOP: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = border.top + margin.top; 
					}
					break;
				}
			case CENTER: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height) / 2; 
					}
					break;
				}
			case BOTTOM: {
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height - border.bottom - margin.bottom); 
					}
					break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	protected void dispose() {
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).dispose();
		}
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

		// screen dimension
		float screenWidth = guiRenderer.gui.width;
		float screenHeight = guiRenderer.gui.height;

		// render background if not transparent
		//	TODO: render background image
		if (backgroundColor != GUIColor.TRANSPARENT) {
			// element location and dimensions
			float left = computedConstraints.left + computedConstraints.alignmentLeft + border.left;
			float top = computedConstraints.top + computedConstraints.alignmentTop + border.top;
			float width = computedConstraints.width - border.left - border.right;
			float height = computedConstraints.height - border.top - border.bottom;
	
			// background color
			float[] bgColorData = backgroundColor.getData();
	
			// render background
			guiRenderer.bindTexture(0);
			guiRenderer.addQuad(
				((left) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top) / (screenHeight / 2f)) - 1f,  
				bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
				0f, 1f, 
				((left + width) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top) / (screenHeight / 2f)) - 1f,  
				bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
				1f, 1f, 
				((left + width) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
				bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
				1f, 0f, 
				((left) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
				bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
				0f, 0f
			);
			guiRenderer.render();
		}

		//
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).render(guiRenderer);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).handleEvent(event);
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#toString()
	 */
	public String toString() {
		return toString(0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#toString(int)
	 */
	protected String toString(int indent) {
		String tmp =
				indent(indent) +
				"GUIParentNode ["
				+ "type=" + getNodeType() 
				+ ", id=" + id 
				+ ", alignments=" + alignments
				+ ", requestedConstraints=" + requestedConstraints 
				+ ", computedConstraints=" + computedConstraints
				+ ", border=" + border 
				+ ", margin=" + margin + 
				"]" + "\n";
		for (int i = 0; i < subNodes.size(); i++) {
			tmp+= subNodes.get(i).toString(indent + 1) + (i == subNodes.size() - 1?"":"\n");
		}
		return tmp;
	}

}

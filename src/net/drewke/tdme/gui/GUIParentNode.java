package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * A parent node supporting child notes
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIParentNode extends GUINode {

	/**
	 * Margin
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	static class Margin {
		protected int left;
		protected int top;
		protected int right;
		protected int bottom;
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Margin [left=" + left + ", top=" + top + ", right=" + right
					+ ", bottom=" + bottom + "]";
		}
	}

	/**
	 * Border
	 * 
	 * @author Andreas Drewke
	 * @ersion $Id$
	 */
	static class Border {
		protected GUIColor leftColor;
		protected GUIColor topColor;
		protected GUIColor rightColor;
		protected GUIColor bottomColor;
		protected int left;
		protected int top;
		protected int right;
		protected int bottom;
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Border [left=" + left + ", top=" + top
					+ ", right=" + right + ", bottom=" + bottom
					// + ", leftColor=" + leftColor + ", topColor=" + topColor
					// + ", rightColor=" + rightColor + ", bottomColor="
					// + bottomColor 
					+ "]";
		}	
	}

	//
	private String backgroundImage;
	private GUIColor backgroundColor;
	protected Margin margin;
	protected Border border;
	protected ArrayList<GUINode> subNodes;

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
	 * @oaram background color
	 * @param background image 
	 */
	protected GUIParentNode(
		GUINode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints, 
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		Border border, 
		Margin margin, 
		String backgroundColor,
		String backgroundImage
		) throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, showOn, hideOn);
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
	 * Get requested pixel value
	 * @param value
	 * @param default value
	 * @return value
	 */
	protected static int getRequestedPixelValue(String value, int defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		} else{
			return Integer.valueOf(value);
		}
	}

	/**
	 * Get color
	 * @param color
	 * @param default color
	 * @return value
	 */
	protected static GUIColor getRequestedColor(String color, GUIColor defaultColor) throws GUIParserException {
		if (color == null || color.length() == 0) {
			return defaultColor;
		} else{
			return new GUIColor(color);
		}
	}

	/**
	 * Create border
	 * @param all border
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param all border color
	 * @param left color
	 * @param top color
	 * @param right color
	 * @param bottom color
	 * @return border
	 */
	protected static Border createBorder(
		String allBorder,
		String left, String top, String right, String bottom,
		String allBorderColor,
		String leftColor, String topColor, String rightColor, String bottomColor
		) throws GUIParserException {
		Border border = new Border();
		border.left = getRequestedPixelValue(allBorder, 0);
		border.top = getRequestedPixelValue(allBorder, 0);
		border.right = getRequestedPixelValue(allBorder, 0);
		border.bottom = getRequestedPixelValue(allBorder, 0);
		border.left = getRequestedPixelValue(left, border.left);
		border.top = getRequestedPixelValue(top, border.top);
		border.right = getRequestedPixelValue(right, border.right);
		border.bottom = getRequestedPixelValue(bottom, border.bottom);
		border.leftColor = getRequestedColor(allBorderColor, GUIColor.BLACK);
		border.topColor = getRequestedColor(allBorderColor, GUIColor.BLACK);
		border.rightColor = getRequestedColor(allBorderColor, GUIColor.BLACK);
		border.bottomColor = getRequestedColor(allBorderColor, GUIColor.BLACK);
		border.leftColor = getRequestedColor(leftColor, border.leftColor);
		border.topColor = getRequestedColor(topColor, border.topColor);
		border.rightColor = getRequestedColor(rightColor, border.rightColor);
		border.bottomColor = getRequestedColor(bottomColor, border.bottomColor);
		return border;
	}

	/**
	 * Create margin
	 * @param all margin
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return margin
	 */
	protected static Margin createMargin(
		String allMargin,
		String left, String top, String right, String bottom
		) throws GUIParserException {
		//
		Margin margin = new Margin();
		margin.left = getRequestedPixelValue(allMargin, 0);
		margin.top = getRequestedPixelValue(allMargin, 0);
		margin.right = getRequestedPixelValue(allMargin, 0);
		margin.bottom = getRequestedPixelValue(allMargin, 0);
		margin.left = getRequestedPixelValue(left, margin.left);
		margin.top = getRequestedPixelValue(top, margin.top);
		margin.right = getRequestedPixelValue(right, margin.right);
		margin.bottom = getRequestedPixelValue(bottom, margin.bottom);
		return margin;
	}

	/**
	 * Layout
	 */
	protected void layout() {
		super.layout();
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

		// render border if given
		if (border != null) {
			// 
			guiRenderer.bindTexture(0);

			// render border top
			if (border.top > 0) {
				float left = computedConstraints.left + computedConstraints.alignmentLeft;
				float top = computedConstraints.top + computedConstraints.alignmentTop;
				float width = computedConstraints.width;
				float height = border.top;
	
				// background color
				float[] borderColorData = border.topColor.getData();
	
				// render panel background
				guiRenderer.addQuad(
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 0f, 
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 0f
				);
			}
	
			// render border bottom
			if (border.bottom > 0) {
				float left = computedConstraints.left + computedConstraints.alignmentLeft;
				float top = computedConstraints.top + computedConstraints.alignmentTop + computedConstraints.height - border.bottom;
				float width = computedConstraints.width;
				float height = border.bottom;
	
				// background color
				float[] borderColorData = border.bottomColor.getData();
	
				// render panel background
				guiRenderer.addQuad(
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 0f, 
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 0f
				);
			}
	
			// render border bottom
			if (border.left > 0) {
				float left = computedConstraints.left + computedConstraints.alignmentLeft;
				float top = computedConstraints.top + computedConstraints.alignmentTop;
				float width = border.left;
				float height = computedConstraints.height;
	
				// background color
				float[] borderColorData = border.leftColor.getData();
	
				// render panel background
				guiRenderer.addQuad(
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 0f, 
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 0f
				);
			}
	
			// render border bottom
			if (border.right > 0) {
				float left = computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.width - border.right;
				float top = computedConstraints.top + computedConstraints.alignmentTop;
				float width = border.right;
				float height = computedConstraints.height;
	
				// background color
				float[] borderColorData = border.rightColor.getData();
	
				// render panel background
				guiRenderer.addQuad(
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 1f, 
					((left + width) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					1f, 0f, 
					((left) / (screenWidth / 2f)) - 1f, 
					((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
					borderColorData[0], borderColorData[1], borderColorData[2], borderColorData[3],
					0f, 0f
				);
			}

			//
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

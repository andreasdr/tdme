package net.drewke.tdme.gui;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI Node class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUINode {

	enum AlignmentHorizontal {LEFT, CENTER, RIGHT};
	enum AlignmentVertical {TOP, CENTER, BOTTOM};

	/**
	 * Alignments
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	static class Alignments {
		protected AlignmentHorizontal horizontal; 
		protected AlignmentVertical vertical;
		public String toString() {
			return horizontal.toString().toLowerCase() + ", " + vertical.toString().toLowerCase();
		}
	}

	/**
	 * Requested constraints for this node
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	static class RequestedConstraints {
		enum RequestedConstraintsType {PIXEL, PERCENT, STAR, AUTO};
		protected RequestedConstraintsType leftType;
		protected int left;
		protected RequestedConstraintsType topType;
		protected int top;
		protected RequestedConstraintsType widthType;
		protected int width;
		protected RequestedConstraintsType heightType;
		protected int height;
		public String toString() {
			return 
				left + " " + leftType.toString().toLowerCase() + ", " +
				top + " " + topType.toString().toLowerCase() + ", " +
				width + " " + widthType.toString().toLowerCase() + ", " +
				height + " " + heightType.toString().toLowerCase();
		}
	}

	/**
	 * Computed constraints for this node
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	static class ComputedConstraints {
		protected int left; 
		protected int top;
		protected int width; 
		protected int height;
		protected int alignmentLeft;
		protected int alignmentTop;
		protected int contentAlignmentLeft;
		protected int contentAlignmentTop;
		public String toString() {
			return 
				left + ", " +
				top + ", " +
				width + ", " +
				height + 
				(alignmentLeft != 0 || alignmentTop != 0?
					" +A(" + alignmentLeft + ", " + alignmentTop + ")":
					""
				) +
				(contentAlignmentLeft != 0 || contentAlignmentTop != 0?
					" +CA(" + contentAlignmentLeft + ", " + contentAlignmentTop + ")":
					""
				);
		}
	}

	/**
	 * Padding
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	static class Padding {
		protected int left;
		protected int top;
		protected int right;
		protected int bottom;
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Padding [left=" + left + ", top=" + top + ", right=" + right
					+ ", bottom=" + bottom + "]";
		}
	}

	/**
	 * Border
	 * @author Andreas Drewke
	 * @version $Id$
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

	protected GUIScreenNode screenNode;
	protected GUIParentNode parentNode;
	protected String id;
	protected Alignments alignments;
	protected RequestedConstraints requestedConstraints;
	protected ComputedConstraints computedConstraints;
	protected Padding padding;
	protected Border border;

	private GUINodeConditions showOn;
	private GUINodeConditions hideOn;

	private GUINodeController controller;

	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 */
	protected GUINode(
		GUIScreenNode screenNode,
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn
		) {
		//
		this.screenNode = screenNode;
		this.parentNode = parentNode;
		this.id = id;
		this.alignments = alignments;
		this.requestedConstraints = requestedConstraints;
		this.computedConstraints = new ComputedConstraints();
		this.border = border;
		this.padding = padding;
		this.showOn = showOn;
		this.hideOn = hideOn;
		this.controller = null;
	}

	/**
	 * @return node type
	 */
	protected abstract String getNodeType();

	/**
	 * @return scren node
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return parent node
	 */
	protected GUIParentNode getParentNode() {
		return parentNode;
	}
	
	/**
	 * @return id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @return is content node
	 */
	protected abstract boolean isContentNode();

	/**
	 * @return content width including border, margin
	 */
	protected abstract int getContentWidth();

	/**
	 * @return content height including border, margin
	 */
	protected abstract int getContentHeight();

	/**
	 * @return auto width if auto width requested or content width
	 */
	protected int getAutoWidth() {
		if (requestedConstraints.widthType == RequestedConstraintsType.AUTO) {
			return getContentWidth();
		} else {
			return computedConstraints.width;
		}
	}

	/**
	 * @return auto height if auto height requested or content height
	 */
	protected int getAutoHeight() {
		if (requestedConstraints.heightType == RequestedConstraintsType.AUTO) {
			return getContentHeight();
		} else {
			return computedConstraints.height;
		}
	}

	/**
	 * @return requested constraints
	 */
	protected RequestedConstraints getRequestsConstraints() {
		return requestedConstraints;
	}

	/**
	 * @return computed constraints
	 */
	protected ComputedConstraints getComputedConstraints() {
		return computedConstraints;
	}

	/**
	 * Set computed left
	 * @param left
	 */
	protected void setLeft(int left) {
		computedConstraints.left = left;
	}

	/**
	 * Set computed top
	 * @param top
	 */
	protected void setTop(int top) {
		computedConstraints.top = top;
	}

	/**
	 * Layout
	 */
	protected void layout() {
		// parent node constraints
		int parentNodeContentWidth = parentNode.computedConstraints.width - parentNode.border.left - parentNode.border.right - parentNode.padding.left - parentNode.padding.right;
		int parentNodeContentHeight = parentNode.computedConstraints.height - parentNode.border.top - parentNode.border.bottom - parentNode.padding.top - parentNode.padding.bottom;

		// compute constraints
		computedConstraints.left =
			parentNode.computedConstraints.left + 
			layoutConstraintPixel(
				requestedConstraints.leftType,
				0,
				parentNodeContentWidth, 
				requestedConstraints.left
			);
		computedConstraints.top = 
			parentNode.computedConstraints.top +
			layoutConstraintPixel(
				requestedConstraints.topType,
				0,
				parentNodeContentHeight, 
				requestedConstraints.top
			);
		computedConstraints.width = 
			layoutConstraintPixel(
				requestedConstraints.widthType,
				getAutoWidth(),
				parentNodeContentWidth, 
				requestedConstraints.width
			);
		computedConstraints.height = 
			layoutConstraintPixel(
				requestedConstraints.heightType,
				getAutoHeight(),
				parentNodeContentHeight, 
				requestedConstraints.height
			);

		// reset additional constraints
		computedConstraints.alignmentLeft = 0;
		computedConstraints.alignmentTop = 0;
		computedConstraints.contentAlignmentLeft = 0;
		computedConstraints.contentAlignmentTop = 0;

		//
		computeContentAlignment();
	}

	/**
	 * Do content alignment
	 */
	protected void computeContentAlignment() {
		// align content nodes
		if (isContentNode() == true) {
			// content alignment horizontal
			switch (alignments.horizontal) {
				case LEFT:
					{
						computedConstraints.contentAlignmentLeft = border.left + padding.left;
						break;
					}
				case CENTER:
					{
						computedConstraints.contentAlignmentLeft = (computedConstraints.width - getContentWidth()) / 2 + border.left + padding.left;
						break;
					}
				case RIGHT: {
					{
						computedConstraints.contentAlignmentLeft = computedConstraints.width - getContentWidth() + border.left + padding.left;
						break;
					}
				}
			}
	
			// content alignment vertical
			switch (alignments.vertical) {
				case TOP:
					{
						computedConstraints.contentAlignmentTop = border.top + padding.top;
						break;
					}
				case CENTER:
					{
						computedConstraints.contentAlignmentTop = (computedConstraints.height - getContentHeight()) / 2 + border.top + padding.top;
						break;
					}
				case BOTTOM: {
					{
						computedConstraints.contentAlignmentTop = computedConstraints.height - getContentHeight() + border.left + padding.left;
						break;
					}
				}
			}
		}
	}

	/**
	 * Layout constraint
	 * @param type
	 * @param auto value
	 * @param parent value
	 * @param value
	 * @return pixel
	 */
	protected int layoutConstraintPixel(RequestedConstraintsType type, int autoValue, int parentValue, int value) {
		if (type.equals(RequestedConstraintsType.PIXEL)) {
			return value; 
		} else
		if (type.equals(RequestedConstraintsType.PERCENT)) {
			return (int)(parentValue / 100.0 * value); 
		} else 
		if (type.equals(RequestedConstraintsType.AUTO)) {
			return autoValue; 
		}
		return -1;
	}

	/**
	 * Create requested constraints
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return requested constraints
	 */
	protected static Alignments createAlignments(String horizontal, String vertical) {
		Alignments alignments = new Alignments();
		alignments.horizontal = AlignmentHorizontal.valueOf(horizontal != null && horizontal.length() > 0?horizontal.toUpperCase():"LEFT");
		alignments.vertical = AlignmentVertical.valueOf(vertical != null && vertical.length() > 0?vertical.toUpperCase():"TOP");
		return alignments;
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
	 * Get requested constraints type
	 * @param constraint
	 * @param default constraints type
	 * @return requested constraints type
	 */
	protected static RequestedConstraintsType getRequestedConstraintsType(String constraint, RequestedConstraintsType defaultConstraintsType) {
		if (constraint == null || constraint.length() == 0) {
			return defaultConstraintsType;
		} else
		if (constraint.equals("auto")) {
			return RequestedConstraintsType.AUTO; 
		} else
		if (constraint.equals("*")) {
			return RequestedConstraintsType.STAR; 
		} else
		if (constraint.endsWith("%")) {
			return RequestedConstraintsType.PERCENT;
		} else {
			return RequestedConstraintsType.PIXEL;
		}
	}

	/**
	 * Get requested constraints value
	 * @param constraint
	 * @param default constraints value
	 * @return requested constraints value
	 */
	protected static int getRequestedConstraintsValue(String constraint, int defaultConstraintsValue) {
		if (constraint == null || constraint.length() == 0) {
			return defaultConstraintsValue;
		} else
		if (constraint.equals("auto")) {
			return -1; 
		} else
		if (constraint.equals("*")) {
			return -1; 
		} else
		if (constraint.endsWith("%")) {
			return Integer.valueOf(constraint.substring(0, constraint.length() - 1));
		} else {
			return Integer.valueOf(constraint);
		}
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
	 * Create padding
	 * @param all padding
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return padding
	 */
	protected static Padding createPadding(
		String allPadding,
		String left, String top, String right, String bottom
		) throws GUIParserException {
		//
		Padding padding = new Padding();
		padding.left = getRequestedPixelValue(allPadding, 0);
		padding.top = getRequestedPixelValue(allPadding, 0);
		padding.right = getRequestedPixelValue(allPadding, 0);
		padding.bottom = getRequestedPixelValue(allPadding, 0);
		padding.left = getRequestedPixelValue(left, padding.left);
		padding.top = getRequestedPixelValue(top, padding.top);
		padding.right = getRequestedPixelValue(right, padding.right);
		padding.bottom = getRequestedPixelValue(bottom, padding.bottom);
		return padding;
	}

	/**
	 * Create conditions
	 * @param conditions
	 */
	protected static GUINodeConditions createConditions(String conditions) {
		GUINodeConditions guiNodeConditions = new GUINodeConditions();
		StringTokenizer t = new StringTokenizer(conditions, ",");
		while (t.hasMoreTokens()) {
			guiNodeConditions.add(t.nextToken());
		}
		return guiNodeConditions;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected boolean checkConditions() {
		ArrayList<String> showOn = this.showOn.conditions;
		ArrayList<String> hideOn = this.hideOn.conditions;

		// check for on-show "always"
		for (int i = 0; i < showOn.size(); i++) {
			if (showOn.get(i).equals(GUIElementNode.CONDITION_ALWAYS)) return true;
		}

		// check for on-hide "always"
		for (int i = 0; i < hideOn.size(); i++) {
			if (hideOn.get(i).equals(GUIElementNode.CONDITION_ALWAYS)) return false;
		}

		// determine parent element node
		GUINode node = parentNode;
		while (node != null && node instanceof GUIElementNode == false) {
			node = node.parentNode;
		}

		// exit if no element node
		if (node == null) {
			return true;
		}

		GUIElementNode elementNode = (GUIElementNode)node;

		// check for on-hide match
		for (int i = 0; i < hideOn.size(); i++) {
			for (int j = 0; j < elementNode.activeConditions.conditions.size(); j++) {
				if (hideOn.get(i).equals(elementNode.activeConditions.conditions.get(j))) return false;
			}
		}

		// check for a on-show match
		for (int i = 0; i < showOn.size(); i++) {
			for (int j = 0; j < elementNode.activeConditions.conditions.size(); j++) {
				if (showOn.get(i).equals(elementNode.activeConditions.conditions.get(j))) return true;
			}
		}

		// always is default if no show-on given
		return showOn.size() == 0;
	}

	/**
	 * Dispose node
	 */
	protected abstract void dispose();

	/**
	 * Render
	 * 
	 * @param gui renderer
	 */
	protected void render(GUIRenderer guiRenderer) {
		// screen dimension
		float screenWidth = guiRenderer.gui.width;
		float screenHeight = guiRenderer.gui.height;

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
	}

	/**
	 * Is event belonging to node
	 * @param event
	 * @return boolean
	 */
	protected boolean isEventBelongingToNode(GUIMouseEvent event) {
		return
			event.x >= computedConstraints.left + computedConstraints.alignmentLeft && event.x <= computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.width &&
			event.y >= computedConstraints.top + computedConstraints.alignmentTop && event.y <= computedConstraints.top + computedConstraints.alignmentTop + computedConstraints.height;

	}

	/**
	 * Handle event
	 * @param event
	 */
	public void handleEvent(GUIMouseEvent event) {
		// determine first node up the tree with controller
		GUINode node = this;
		while (node != null && node.controller == null) {
			node = node.parentNode;
		}

		// exit if no element node with controller
		if (node == null) {
			return;
		}

		// otherwise call controller
		node.controller.handleEvent(this, event);
	}

	/**
	 * Set up node controller
	 * @param controller
	 */
	protected void setController(GUINodeController controller) {
		this.controller = controller;
	}

	/**
	 * Compute indent string
	 * @param ident
	 * @return indented string
	 */
	protected String indent(int indent) {
		String tmp = "";
		for (int i = 0; i < indent; i++) tmp+= "\t";
		return tmp;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "GUINode [id=" + id +
				", alignments=" + alignments +
				", requestedConstraints=" + requestedConstraints +
				", computedConstraints=" + computedConstraints + "]";
	}

	/**
	 * Indented string representation
	 * @param ident
	 * @return string representation
	 */
	protected String toString(int indent) {
		return 
			indent(indent) +
			"GUINode [type=" + getNodeType() + ", id=" + id +
			", alignments=" + alignments +
			", requestedConstraints=" + requestedConstraints +
			", computedConstraints=" + computedConstraints +
			", border=" + border + 
			", padding=" + padding + 
			"]";
	}

}

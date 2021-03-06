package net.drewke.tdme.gui.nodes;

import java.util.StringTokenizer;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.nodes.GUIParentNode.Overflow;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.ArrayList;

/**
 * GUI Node class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUINode {

	protected enum Flow {INTEGRATED, FLOATING};
	public enum AlignmentHorizontal {LEFT, CENTER, RIGHT};
	public enum AlignmentVertical {TOP, CENTER, BOTTOM};

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
	public static class ComputedConstraints {
		public int left;
		public int top;
		public int width;
		public int height;
		public int alignmentLeft;
		public int alignmentTop;
		public int contentAlignmentLeft;
		public int contentAlignmentTop;
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
		public int left;
		public int top;
		public int right;
		public int bottom;
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
	public static class Border {
		public GUIColor leftColor;
		public GUIColor topColor;
		public GUIColor rightColor;
		public GUIColor bottomColor;
		public int left;
		public int top;
		public int right;
		public int bottom;
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
	protected Flow flow;
	protected Alignments alignments;
	protected RequestedConstraints requestedConstraints;
	protected ComputedConstraints computedConstraints;
	protected GUIColor backgroundColor;
	protected Padding padding;
	protected Border border;

	protected GUINodeConditions showOn;
	protected GUINodeConditions hideOn;

	protected GUINodeController controller;

	protected boolean conditionsMet;

	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
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
		Flow flow,
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor,
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn
		) {
		//
		this.screenNode = screenNode;
		this.parentNode = parentNode;
		this.id = id;
		this.flow = flow;
		this.alignments = alignments;
		this.requestedConstraints = requestedConstraints;
		this.computedConstraints = new ComputedConstraints();
		this.computedConstraints.alignmentLeft = 0;
		this.computedConstraints.alignmentTop = 0;
		this.computedConstraints.contentAlignmentLeft = 0;
		this.computedConstraints.contentAlignmentTop = 0;
		this.backgroundColor = backgroundColor;
		this.border = border;
		this.padding = padding;
		this.showOn = showOn;
		this.hideOn = hideOn;
		this.controller = null;
		this.conditionsMet = false;
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
	public GUIParentNode getParentNode() {
		return parentNode;
	}
	
	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return is content node
	 */
	protected abstract boolean isContentNode();

	/**
	 * @return content width including border, margin
	 */
	public abstract int getContentWidth();

	/**
	 * @return content height including border, margin
	 */
	public abstract int getContentHeight();

	/**
	 * @return auto width if auto width requested or content width
	 */
	public int getAutoWidth() {
		if (requestedConstraints.widthType == RequestedConstraintsType.AUTO) {
			return getContentWidth();
		} else {
			return computedConstraints.width;
		}
	}

	/**
	 * @return auto height if auto height requested or content height
	 */
	public int getAutoHeight() {
		if (requestedConstraints.heightType == RequestedConstraintsType.AUTO) {
			return getContentHeight();
		} else {
			return computedConstraints.height;
		}
	}

	/**
	 * @return border
	 */
	public Border getBorder() {
		return border;
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
	public ComputedConstraints getComputedConstraints() {
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
	 * Create alignments
	 * @param horizontal
	 * @param vertical
	 * @return alignments
	 */
	public static Alignments createAlignments(String horizontal, String vertical) {
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
	public static RequestedConstraints createRequestedConstraints(String left, String top, String width, String height) {
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
	public static GUIColor getRequestedColor(String color, GUIColor defaultColor) throws GUIParserException {
		if (color == null || color.length() == 0) {
			return defaultColor;
		} else{
			return new GUIColor(color);
		}
	}

	/**
	 * Create flow
	 * @param flow
	 * @return flow
	 */
	public static Flow createFlow(String flow) {
		return Flow.valueOf(flow != null && flow.length() > 0?flow.toUpperCase():"INTEGRATED");
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
	public static Border createBorder(
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
	public static Padding createPadding(
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
	public static GUINodeConditions createConditions(String conditions) {
		GUINodeConditions guiNodeConditions = new GUINodeConditions();
		StringTokenizer t = new StringTokenizer(conditions, ",");
		while (t.hasMoreTokens()) {
			guiNodeConditions.add(t.nextToken().trim());
		}
		return guiNodeConditions;
	}

	/**
	 * @return conditions met
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
	public void dispose() {
		if (controller != null) controller.dispose();
	}

	/**
	 * Set conditions met for the while tree
	 */
	public void setConditionsMet() {
		// check conditions
		conditionsMet = checkConditions();
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

		// screen dimension
		float screenWidth = guiRenderer.getGUI().getWidth();
		float screenHeight = guiRenderer.getGUI().getHeight();

		// render background if not transparent
		//	TODO: render background image
		if (backgroundColor != GUIColor.TRANSPARENT) {
			// element location and dimensions
			float left = computedConstraints.left + computedConstraints.alignmentLeft + border.left;
			float top = computedConstraints.top + computedConstraints.alignmentTop + border.top;
			float width = computedConstraints.width - border.left - border.right;
			float height = computedConstraints.height - border.top - border.bottom;

			// background color
			float[] bgColorData = backgroundColor.getArray();

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
				float[] borderColorData = border.topColor.getArray();
	
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
				float[] borderColorData = border.bottomColor.getArray();
	
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
				float[] borderColorData = border.leftColor.getArray();
	
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
				float[] borderColorData = border.rightColor.getArray();
	
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
	 * @return compute parent children render offset X total
	 */
	protected float computeParentChildrenRenderOffsetXTotal() {
		float childrenRenderOffSetX = 0f;

		// take parent nodes into account
		GUIParentNode parentNode = this.parentNode;
		while (parentNode != null) {
			childrenRenderOffSetX+= parentNode.childrenRenderOffsetX;
			parentNode = parentNode.parentNode;
		}

		//
		return childrenRenderOffSetX;
	}

	/**
	 * @return compute children render offset Y total
	 */
	protected float computeParentChildrenRenderOffsetYTotal() {
		float childrenRenderOffSetY = 0f;

		// take parent nodes into account
		GUIParentNode parentNode = this.parentNode;
		while (parentNode != null) {
			childrenRenderOffSetY+= parentNode.childrenRenderOffsetY;
			parentNode = parentNode.parentNode;
		}

		//
		return childrenRenderOffSetY;
	}

	/**
	 * Is event belonging to node
	 * @param event
	 * @param x,y position in node coordinate system 
	 * @return boolean
	 */
	public boolean isEventBelongingToNode(GUIMouseEvent event, int[] position) {
		int eventXScreen = event.getX();
		int eventYScreen = event.getY();

		// check parent nodes
		GUIParentNode parentNode = this.parentNode;
		while (parentNode != null) {
			// floating nodes have no parent nodes to check 
			if (parentNode.flow == Flow.FLOATING) break;

			// take parent children render offset into account
			float eventX = eventXScreen + parentNode.computeParentChildrenRenderOffsetXTotal();
			float eventY = eventYScreen + parentNode.computeParentChildrenRenderOffsetYTotal();
			if ((eventX >= parentNode.computedConstraints.left + parentNode.computedConstraints.alignmentLeft &&
				eventX < parentNode.computedConstraints.left + parentNode.computedConstraints.alignmentLeft + parentNode.computedConstraints.width &&
				eventY >= parentNode.computedConstraints.top + parentNode.computedConstraints.alignmentTop && 
				eventY < parentNode.computedConstraints.top + parentNode.computedConstraints.alignmentTop + parentNode.computedConstraints.height) == false) {
				//
				return false;
			}
			parentNode = parentNode.parentNode;
		}

		// take parent children render offset into account
		float eventX = eventXScreen + computeParentChildrenRenderOffsetXTotal();
		float eventY = eventYScreen + computeParentChildrenRenderOffsetYTotal();

		// check node
		boolean belongsToElement =
			eventX >= computedConstraints.left + computedConstraints.alignmentLeft && eventX < computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.width &&
			eventY >= computedConstraints.top + computedConstraints.alignmentTop && eventY < computedConstraints.top + computedConstraints.alignmentTop + computedConstraints.height;


		// compute position
		if (belongsToElement == true && position != null) {
			position[0] = (int)(eventX - (computedConstraints.left + computedConstraints.alignmentLeft));
			position[1] = (int)(eventY - (computedConstraints.top + computedConstraints.alignmentTop));
		}

		//
		return belongsToElement;
	}

	/**
	 * Is event belonging to node
	 * @param event
	 * @return boolean
	 */
	public boolean isEventBelongingToNode(GUIMouseEvent event) {
		return isEventBelongingToNode(event, null);
	}

	/**
	 * Get event off node relative position
	 * @param event
	 * @param x,y position (will return x = 0 if in node on x axis, will return x < 0  (-pixel) if on the left of element, x > 0 (+pixel) if on the right of element, y behaves analogous to x)  
	 * @return void
	 */
	public void getEventOffNodeRelativePosition(GUIMouseEvent event, int[] position) {
		int eventXScreen = event.getX();
		int eventYScreen = event.getY();

		// take parent children render offset into account
		float eventX = eventXScreen + computeParentChildrenRenderOffsetXTotal();
		float eventY = eventYScreen + computeParentChildrenRenderOffsetYTotal();

		// constraints
		float left = computedConstraints.left + computedConstraints.alignmentLeft;
		float right = computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.width;
		float top = computedConstraints.top + computedConstraints.alignmentTop;
		float bottom = computedConstraints.top + computedConstraints.alignmentTop + computedConstraints.height;

		// x
		if (eventX < left) {
			position[0] = (int)(eventX - left);
		} else
		if (eventX > right) {
			position[0] = (int)(eventX - right);
		} else {
			position[0] = 0;
		}

		// y
		if (eventY < top) {
			position[1] = (int)(eventY - top); 
		} else
		if (eventY > bottom) {
			position[1] = (int)(eventY - bottom);
		} else {
			position[1] = 0;
		}
	}

	/**
	 * @return first parent node in tree with controller node attached
	 */
	public GUIParentNode getParentControllerNode() {
		// determine first node up the tree with controller
		GUIParentNode node = this.parentNode;
		while (node != null && node.controller == null) {
			node = node.parentNode;
		}

		//
		return node;
	}

	/**
	 * Handle mouse event
	 * @param event
	 */
	public void handleMouseEvent(GUIMouseEvent event) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// skip if processed by floating node
		if (screenNode.mouseEventProcessedByFloatingNode == true) return;

		// determine first node up the tree with controller
		GUINode node = this;
		if (node.controller == null) {
			node = getParentControllerNode();
		}

		// exit if no element node with controller
		if (node == null) {
			return;
		}

		// otherwise call controller
		node.controller.handleMouseEvent(this, event);
	}

	/**
	 * Handle keyboard event
	 * @param event
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// determine first node up the tree with controller
		GUINode node = this;
		if (node.controller == null) {
			node = getParentControllerNode();
		}

		// exit if no element node with controller
		if (node == null) {
			return;
		}

		// otherwise call controller
		node.controller.handleKeyboardEvent(this, event);
	}

	/**
	 * Tick method will be executed once per frame
	 */
	public void tick() {
		// check if conditions were met
		if (conditionsMet == false) return;

		// call controller tick
		if (controller != null) controller.tick();
	}

	/**
	 * @return controller
	 */
	public GUINodeController getController() {
		return controller;
	}

	/**
	 * Set up node controller
	 * @param controller
	 */
	public void setController(GUINodeController controller) {
		this.controller = controller;
	}

	/**
	 * Scroll to node Y
	 */
	public void scrollToNodeY() {
		scrollToNodeY(null);
	}

	/**
	 * Scroll to node Y
	 * @param stop at node to node
	 */
	public void scrollToNodeY(GUIParentNode toNode) {
		// find parent
		GUIParentNode scrollYParentNode = this.parentNode;
		while (true == true) {
			if (scrollYParentNode == toNode || scrollYParentNode == null) return;
			if (scrollYParentNode.overflowY == Overflow.SCROLL) {
				break;
			}
			scrollYParentNode = scrollYParentNode.parentNode;
			if (scrollYParentNode == null) return;
		}

		// check if above viewport 
		if (computedConstraints.top < scrollYParentNode.childrenRenderOffsetY + scrollYParentNode.computedConstraints.top) {
			scrollYParentNode.childrenRenderOffsetY = computedConstraints.top - scrollYParentNode.computedConstraints.top;
		}

		// check if below viewport 
		if (computedConstraints.top + computedConstraints.height > scrollYParentNode.childrenRenderOffsetY + scrollYParentNode.computedConstraints.top + scrollYParentNode.computedConstraints.height) {
			scrollYParentNode.childrenRenderOffsetY = computedConstraints.top + computedConstraints.height - scrollYParentNode.computedConstraints.top - scrollYParentNode.computedConstraints.height;
		}

		// scroll parent node into view
		scrollYParentNode.scrollToNodeY(toNode);
	}

	/**
	 * Scroll to node X
	 */
	public void scrollToNodeX() {
		scrollToNodeX(null);
	}

	/**
	 * Scroll to node X
	 * @param stop at node to node
	 */
	public void scrollToNodeX(GUIParentNode toNode) {
		// find parent
		GUIParentNode scrollXParentNode = this.parentNode;
		while (true == true) {
			if (scrollXParentNode == toNode || scrollXParentNode == null) return;
			if (scrollXParentNode.overflowX == Overflow.SCROLL) {
				break;
			}
			scrollXParentNode = scrollXParentNode.parentNode;
		}

		// check if left of viewport 
		if (computedConstraints.left < scrollXParentNode.childrenRenderOffsetX + scrollXParentNode.computedConstraints.left) {
			scrollXParentNode.childrenRenderOffsetX = computedConstraints.left - scrollXParentNode.computedConstraints.left;
		}

		// check if right of viewport 
		if (computedConstraints.left + computedConstraints.width > scrollXParentNode.childrenRenderOffsetX + scrollXParentNode.computedConstraints.left + scrollXParentNode.computedConstraints.width) {
			scrollXParentNode.childrenRenderOffsetX = computedConstraints.left + computedConstraints.width - scrollXParentNode.computedConstraints.left - scrollXParentNode.computedConstraints.width;
		}

		// scroll parent node into view
		scrollXParentNode.scrollToNodeX(toNode);
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
			", controller=" + (this.controller != null?"yes":"no") +
			"]";
	}

}

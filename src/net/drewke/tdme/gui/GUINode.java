package net.drewke.tdme.gui;

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
		public String toString() {
			return 
				left + ", " +
				top + ", " +
				width + ", " +
				height + 
				(alignmentLeft != 0 || alignmentTop != 0?
					" +(" + alignmentLeft + ", " + alignmentTop + ")":
					""
				);
		}
	} 

	protected GUINode parentNode;
	protected String id;
	protected Alignments alignments;
	protected RequestedConstraints requestedConstraints;
	protected ComputedConstraints computedConstraints;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 */
	protected GUINode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints) {
		this.parentNode = parentNode;
		this.id = id;
		this.alignments = alignments;
		this.requestedConstraints = requestedConstraints;
		this.computedConstraints = new ComputedConstraints();
	}

	/**
	 * @return node type
	 */
	protected abstract String getNodeType();

	/**
	 * @return parent node
	 */
	protected GUINode getParentNode() {
		return parentNode;
	}
	
	/**
	 * @return id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @return content width
	 */
	protected abstract int getContentWidth();

	/**
	 * @return content height
	 */
	protected abstract int getContentHeight();

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
		computedConstraints.left =
			parentNode.computedConstraints.left + 
			layoutConstraintPixel(
				requestedConstraints.leftType,
				0,
				parentNode.computedConstraints.width, 
				requestedConstraints.left
			);
		computedConstraints.top = 
			parentNode.computedConstraints.top +
			layoutConstraintPixel(
				requestedConstraints.topType,
				0,
				parentNode.computedConstraints.height, 
				requestedConstraints.top
			);
		computedConstraints.width = 
			layoutConstraintPixel(
				requestedConstraints.widthType,
				getContentWidth(),
				parentNode.computedConstraints.width, 
				requestedConstraints.width
			);
		computedConstraints.height = 
			layoutConstraintPixel(
				requestedConstraints.heightType,
				getContentHeight(),
				parentNode.computedConstraints.height, 
				requestedConstraints.height
			);
	}

	/**
	 * Layout constraint
	 * @param type
	 * @param auto value
	 * @param parent value
	 * @param value
	 * @return pixel
	 */
	public int layoutConstraintPixel(RequestedConstraintsType type, int autoValue, int parentValue, int value) {
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
	 * Compute indent string
	 * @param ident
	 * @return indented string
	 */
	public String indent(int indent) {
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
			", computedConstraints=" + computedConstraints + "]";
	}

}

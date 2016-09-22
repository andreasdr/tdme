package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI space node
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUISpaceNode extends GUINode {

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 */
	protected GUISpaceNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints) {
		super(parentNode, id, alignments, requestedConstraints);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "space";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentWidth()
	 */
	protected int getContentWidth() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	protected int getContentHeight() {
		return 0;
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
		constraints.widthType = getRequestedConstraintsType(width.trim(), RequestedConstraintsType.PERCENT);
		constraints.width = getRequestedConstraintsValue(width.trim(), 100);
		constraints.heightType = getRequestedConstraintsType(height.trim(), RequestedConstraintsType.PERCENT);
		constraints.height = getRequestedConstraintsValue(height.trim(), 100);
		return constraints;
	}
}

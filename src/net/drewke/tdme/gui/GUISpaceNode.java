package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUINode.RequestedConstraints;
import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI space node
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUISpaceNode extends GUINode {

	/**
	 * GUI space node
	 * @param parentNode
	 * @param id
	 * @param requestedConstraints
	 */
	protected GUISpaceNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints) {
		super(parentNode, id, requestedConstraints);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "space";
	}

	/**
	 * @return content width
	 */
	protected int getContentWidth() {
		return -1;
	}

	/**
	 * @return content height
	 */
	protected int getContentHeight() {
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

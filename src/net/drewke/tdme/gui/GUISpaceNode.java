package net.drewke.tdme.gui;

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

}

package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI Screen Node
 * @author andreas
 * @version $Id$
 */
public class GUIScreenNode extends GUIParentNode {

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param requested constraints
	 */
	protected GUIScreenNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints) {
		super(parentNode, id, requestedConstraints);
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
	 * Layout
	 */
	public void layout() {
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}
	}

	/**
	 * Set screen size
	 * @param width
	 * @param height
	 */
	public void setScreenSize(int width, int height) {
		this.requestedConstraints.widthType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.width = width;
		this.requestedConstraints.heightType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.height = width;
		this.computedConstraints.left = 0;
		this.computedConstraints.top = 0;
		this.computedConstraints.width = width;
		this.computedConstraints.height = height;
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "screen";
	}

}

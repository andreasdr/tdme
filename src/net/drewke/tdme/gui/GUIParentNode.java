package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * A parent node supporting child notes
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIParentNode extends GUINode {

	protected ArrayList<GUINode> subNodes;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 */
	protected GUIParentNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints) {
		super(parentNode, id, alignments, requestedConstraints);
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
		constraints.widthType = getRequestedConstraintsType(width.trim(), RequestedConstraintsType.PERCENT);
		constraints.width = getRequestedConstraintsValue(width.trim(), 100);
		constraints.heightType = getRequestedConstraintsType(height.trim(), RequestedConstraintsType.PERCENT);
		constraints.height = getRequestedConstraintsValue(height.trim(), 100);
		return constraints;
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
		switch (alignments.horizontal) {
			case LEFT: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = 0; 
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
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width); 
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
		switch (alignments.vertical) {
			case TOP: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = 0; 
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
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height); 
					}
					break;
				}
			}
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
				+ ", computedConstraints=" + computedConstraints + 
				"]" + "\n";
		for (int i = 0; i < subNodes.size(); i++) {
			tmp+= subNodes.get(i).toString(indent + 1) + (i == subNodes.size() - 1?"":"\n");
		}
		return tmp;
	}

}

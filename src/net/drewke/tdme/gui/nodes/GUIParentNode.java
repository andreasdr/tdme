package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.renderer.GUIRenderer;

/**
 * A parent node supporting child notes
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIParentNode extends GUINode {

	protected ArrayList<GUINode> subNodes;

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
	 * @oaram background color
	 * @param background image 
	 */
	protected GUIParentNode(
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
		) throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		subNodes = new ArrayList<GUINode>();
	}

	/**
	 * @return sub nodes
	 */
	public ArrayList<GUINode> getSubNodes() {
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
	 * Layout
	 */
	protected void layout() {
		super.layout();
		layoutSubNodes();
	}

	/**
	 * Layout sub nodes
	 */
	protected void layoutSubNodes() {
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
						guiSubNode.computedConstraints.alignmentLeft = border.left + padding.left; 
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
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width - border.right - padding.right); 
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
						guiSubNode.computedConstraints.alignmentTop = border.top + padding.top; 
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
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height - border.bottom - padding.bottom); 
					}
					break;
				}
			}
		}
	}

	/**
	 * Get child controller nodes internal
	 * @param child controller nodes
	 */
	private void getChildControllerNodesInternal(ArrayList<GUINode> childControllerNodes) {
		// dispose sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode node = subNodes.get(i);
			if (node.controller != null) {
				childControllerNodes.add(node);
			}
			if (node instanceof GUIParentNode) {
				((GUIParentNode)node).getChildControllerNodesInternal(childControllerNodes);
			}
		}		
	}

	/**
	 * Get child controller nodes
	 * @param child controller nodes
	 */
	public void getChildControllerNodes(ArrayList<GUINode> childControllerNodes) {
		childControllerNodes.clear();
		getChildControllerNodesInternal(childControllerNodes);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	public void dispose() {
		// dispose sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).dispose();
		}
		// parent dispose
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions apply
		if (checkConditions() == false) return;

		// call parent renderer
		super.render(guiRenderer, floatingNodes);

		//
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			// render floating nodes later
			if (guiSubNode.flow == Flow.FLOATING) {
				floatingNodes.add(guiSubNode);
				continue;
			}
			guiSubNode.render(guiRenderer, floatingNodes);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode subNode = subNodes.get(i);
			subNode.handleEvent(event);
		}

		//
		super.handleEvent(event);
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
				+ ", padding=" + padding
				+ ", controller=" + (this.controller != null?"yes":"no")
				+ "]" + "\n";
		for (int i = 0; i < subNodes.size(); i++) {
			tmp+= subNodes.get(i).toString(indent + 1) + (i == subNodes.size() - 1?"":"\n");
		}
		return tmp;
	}

}

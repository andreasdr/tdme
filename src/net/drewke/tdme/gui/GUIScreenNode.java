package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Screen Node
 * @author andreas
 * @version $Id$
 */
public final class GUIScreenNode extends GUIParentNode {

	private int screenWidth;
	private int screenHeight;

	private HashMap<String, GUINode> nodesById;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 */
	protected GUIScreenNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints, 
		Border border, 
		Padding padding,
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		String backgroundColor,
		String backgroundImage) throws GUIParserException {
		super(null, parentNode, id, alignments, requestedConstraints, border, padding, showOn, hideOn, backgroundColor, backgroundImage);
		this.screenWidth = 0;
		this.screenHeight = 0;
		this.nodesById = new HashMap<String, GUINode>();
	}

	/**
	 * @return screen width
	 */
	protected int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * @return screen height
	 */
	protected int getScreenHeight() {
		return screenHeight;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return false;
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
		this.screenWidth = width;
		this.screenHeight = height;
		this.requestedConstraints.widthType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.width = width;
		this.requestedConstraints.heightType = RequestedConstraintsType.PIXEL;
		this.requestedConstraints.height = height;
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

	/**
	 * Get GUI node by id
	 * @param nodeId
	 * @return GUI node or null
	 */
	protected GUINode getNodeById(String nodeId) {
		return nodesById.get(nodeId); 
	}

	/**
	 * Add node
	 * @param node
	 * @return success
	 */
	protected boolean addNode(GUINode node) {
		// having a node without a ID is valid, this node will not be added
		if (node.getId().length() == 0) return true;

		// check if we have a node registered already
		if (nodesById.get(node.getId()) != null) {
			return false;
		}
		// nope, add node
		nodesById.put(node.getId(), node);
		return true;
	}

}

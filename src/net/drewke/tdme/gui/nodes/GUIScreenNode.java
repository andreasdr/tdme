package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Screen Node
 * @author andreas
 * @version $Id$
 */
public final class GUIScreenNode extends GUIParentNode {

	private int nodeCounter;

	private int screenWidth;
	private int screenHeight;

	private HashMap<String, GUINode> nodesById;

	private ArrayList<GUINode> floatingNodes;

	/**
	 * Constructor
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
	public GUIScreenNode(
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
		super(null, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.nodeCounter = 0;
		this.screenWidth = 0;
		this.screenHeight = 0;
		this.nodesById = new HashMap<String, GUINode>();
		this.floatingNodes = new ArrayList<GUINode>();
	}

	/**
	 * @return screen width
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * @return screen height
	 */
	public int getScreenHeight() {
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
	public GUINode getNodeById(String nodeId) {
		return nodesById.get(nodeId); 
	}

	/**
	 * Allocate node id
	 * @return node id
	 */
	public String allocateNodeId() {
		return "tdme_gui_anonymous_node_" + (nodeCounter++);
	}

	/**
	 * Add node
	 * @param node
	 * @return success
	 */
	public boolean addNode(GUINode node) {
		// having a node without a ID is valid, this node will not be added
		if (node.id.length() == 0) {
			node.id = allocateNodeId();
		}

		// check if we have a node registered already
		if (nodesById.get(node.id) != null) {
			return false;
		}

		// nope, add node
		nodesById.put(node.id, node);
		return true;
	}

	/**
	 * Render
	 * @param guiRenderer
	 */
	public void render(GUIRenderer guiRenderer) {
		super.render(guiRenderer, floatingNodes);
		for (int i = 0; i < floatingNodes.size(); i++) {
			floatingNodes.get(i).render(guiRenderer, null);
		}
	}

}

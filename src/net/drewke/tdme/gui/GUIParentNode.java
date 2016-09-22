package net.drewke.tdme.gui;

import java.util.ArrayList;

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
	 * @param requested constraints
	 */
	protected GUIParentNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints) {
		super(parentNode, id, requestedConstraints);
		subNodes = new ArrayList<GUINode>();
	}

	/**
	 * @return sub nodes
	 */
	protected ArrayList<GUINode> getSubNodes() {
		return subNodes;
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
				"GUIParentNode [type=" + getNodeType() + ", id=" + id + ", requestedConstraints="
				+ requestedConstraints + ", computedConstraints="
				+ computedConstraints + "]" + "\n";
		for (int i = 0; i < subNodes.size(); i++) {
			tmp+= subNodes.get(i).toString(indent + 1) + (i == subNodes.size() - 1?"":"\n");
		}
		return tmp;
	}

}

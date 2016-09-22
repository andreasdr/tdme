package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI element node
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIElementNode extends GUIParentNode {

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param requested constraints
	 */
	protected GUIElementNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints) {
		super(parentNode, id, requestedConstraints);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "element";
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
	 * Set computed left
	 * @param left
	 */
	protected void setLeft(int left) {
		super.setLeft(left);
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).setLeft(left);
		}
	}

	/**
	 * Set computed top
	 * @param top
	 */
	protected void setTop(int top) {
		super.setTop(top);
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).setTop(top);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIParentNode#layout()
	 */
	protected void layout() {
		// super layout
		super.layout();

		// width type AUTO means we determine width by max content width
		if (requestedConstraints.widthType.equals(RequestedConstraintsType.AUTO)) {
			int widthMax = 0;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiElementNode = subNodes.get(i);
				if (guiElementNode.getContentWidth() > widthMax) {
					widthMax = guiElementNode.getContentWidth();
				}
			}
			computedConstraints.width = widthMax;
		}

		// height type AUTO means we determine width by max content height
		if (requestedConstraints.widthType.equals(RequestedConstraintsType.AUTO)) {
			int heightMax = 0;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiElementNode = subNodes.get(i);
				if (guiElementNode.getContentHeight() > heightMax) {
					heightMax = guiElementNode.getContentHeight();
				}
			}
			computedConstraints.height = heightMax;
		}

		// layout sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}
	}

}

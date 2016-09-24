package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUILayoutNode.Alignment;
import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI element node
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIElementNode extends GUIParentNode {

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 */
	protected GUIElementNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints) {
		super(parentNode, id, alignments, requestedConstraints);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "element";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentWidth()
	 */
	protected int getContentWidth() {
		int width = 0;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentWidth = guiSubNode.getContentWidth();
			if (contentWidth > width) {
				width = contentWidth;
			}
		}
		return width;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	protected int getContentHeight() {
		int height = 0;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentHeight = guiSubNode.getContentHeight();
			if (contentHeight > height) {
				height = contentHeight;
			}
		}
		return height;
	}

	/**
	 * Set computed left
	 * @param left
	 */
	protected void setLeft(int left) {
		super.setLeft(left);
		left+= computedConstraints.alignmentLeft;
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
		top+= computedConstraints.alignmentTop;
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

		// width type AUTO means we determine children width by content width or overridden width
		if (requestedConstraints.widthType.equals(RequestedConstraintsType.AUTO)) {
			int widthMax = 0;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiElementNode = subNodes.get(i);
				if (guiElementNode.getAutoWidth() > widthMax) {
					widthMax = guiElementNode.getAutoWidth();
				}
			}
			computedConstraints.width = widthMax;
		}

		// width type AUTO means we determine children width by content height or overridden height
		if (requestedConstraints.widthType.equals(RequestedConstraintsType.AUTO)) {
			int heightMax = 0;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiElementNode = subNodes.get(i);
				if (guiElementNode.getAutoHeight() > heightMax) {
					heightMax = guiElementNode.getAutoHeight();
				}
			}
			computedConstraints.height = heightMax;
		}

		// layout sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}

		// compute children alignments
		computeHorizontalChildrenAlignment();
		computeVerticalChildrenAlignment();
	}

}

package net.drewke.tdme.gui;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

public class GUILayoutNode extends GUIParentNode {

	enum Alignment {VERTICAL, HORIZONTAL};

	private Alignment alignment; 

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param alignment
	 */
	protected GUILayoutNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, String alignment) {
		super(parentNode, id, alignments, requestedConstraints);
		this.alignment = Alignment.valueOf(alignment.toUpperCase());
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "layout";
	}

	/**
	 * @return content width
	 */
	protected int getContentWidth() {
		int width = 0;
		if (alignment == Alignment.HORIZONTAL) {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				width+= guiSubNode.getContentWidth();
			}
		} else {
			GUINode guiSubNode = subNodes.get(0);
			width = guiSubNode.getContentWidth();
		}
		return width;
	}

	/**
	 * @return content height
	 */
	protected int getContentHeight() {
		int height = 0;
		if (alignment == Alignment.VERTICAL) {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				height+= guiSubNode.getContentHeight();
			}
		} else {
			GUINode guiSubNode = subNodes.get(0);
			height = guiSubNode.getContentHeight();
		}
		return height;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIParentNode#layout()
	 */
	protected void layout() {
		// GUI parent node layout
		super.layout();

		//
		switch (alignment) {
			case VERTICAL:
				{
					// determine vertical stars
					int starCount = 0;
					int height = computedConstraints.height;
					int nodesHeight = 0;
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
							starCount++;
						} else {
							nodesHeight+= guiSubNode.computedConstraints.height;
						}
					}
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
							guiSubNode.computedConstraints.height = (height - nodesHeight) / starCount;
						}
					}
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
							guiSubNode.computedConstraints.height = (height - nodesHeight) / starCount;
						}
					}

					// compute children alignments
					computeHorizontalChildrenAlignment();

					//
					break;
				}
			case HORIZONTAL:
				{
					// determine horizontal stars
					int starCount = 0;
					int width = computedConstraints.width;
					int nodesWidth = 0;
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
							starCount++;
						} else {
							nodesWidth+= guiSubNode.computedConstraints.width;
						}
					}
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
							guiSubNode.computedConstraints.width = (width - nodesWidth) / starCount;
						}
					}
					
					// compute children alignments
					computeVerticalChildrenAlignment();
					
					//
					break;
				}
		}

		// do layout, layout children left, top 
		setTop(computedConstraints.top);
		setLeft(computedConstraints.left);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#setTop(int)
	 */
	protected void setTop(int top) {
		super.setTop(top);
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			guiSubNode.setTop(top);
			if (alignment == Alignment.VERTICAL) {
				top+= guiSubNode.computedConstraints.height;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#setLeft(int)
	 */
	protected void setLeft(int left) {
		super.setLeft(left);
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			guiSubNode.setLeft(left);
			if (alignment == Alignment.HORIZONTAL) {
				left+= guiSubNode.computedConstraints.width;
			}
		}
	}

}

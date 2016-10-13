package net.drewke.tdme.gui;

import java.util.ArrayList;

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
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param background color
	 * @param background image
	 * @param alignment
	 */
	protected GUILayoutNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints 
		requestedConstraints, 
		Border border, 
		Padding padding, 
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		String backgroundColor,
		String backgroundImage,
		String alignment
		) throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, border, padding, showOn, hideOn, backgroundColor, backgroundImage);
		this.alignment = Alignment.valueOf(alignment.toUpperCase());
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "layout";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentWidth()
	 */
	protected int getContentWidth() {
		// determine content width
		int width = 0;
		if (alignment == Alignment.HORIZONTAL) {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				width+= guiSubNode.getAutoWidth();
			}
		} else {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				int contentWidth = guiSubNode.getAutoWidth();
				if (contentWidth > width) {
					width = contentWidth;
				}
			}
		}

		// add border
		width+= border.left + border.right;

		// add padding
		width+= padding.left + padding.right;

		//
		return width;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	protected int getContentHeight() {
		// determine content height
		int height = 0;
		if (alignment == Alignment.VERTICAL) {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				height+= guiSubNode.getAutoHeight();
			}
		} else {
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode guiSubNode = subNodes.get(i);
				int contentHeight = guiSubNode.getAutoHeight();
				if (contentHeight > height) {
					height = contentHeight;
				}
			}
		}

		// add border
		height+= border.top + border.bottom;

		// add padding
		height+= padding.top + padding.bottom;

		//
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
					int height = computedConstraints.height - border.top - border.bottom - padding.top - padding.bottom;
					int nodesHeight = 0;
					int finalNodesHeight = 0;
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
							starCount++;
						} else {
							nodesHeight+= guiSubNode.computedConstraints.height;
							finalNodesHeight+= guiSubNode.computedConstraints.height;
						}
					}
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
							guiSubNode.computedConstraints.height = (height - nodesHeight) / starCount;
							if (guiSubNode.computedConstraints.height < 0) {
								guiSubNode.computedConstraints.height = 0;
							}
							finalNodesHeight+= guiSubNode.computedConstraints.height;
						}
					}

					// do vertical alignments, take border, padding into account
					switch (alignments.vertical) {
						case TOP:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentTop = border.top + padding.top;
							}
							break;
						case CENTER:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentTop = border.top + padding.top + ((height - finalNodesHeight) / 2); 
							}
							break;
						case BOTTOM:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentTop = (height - finalNodesHeight); 
							}
							break;
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
					int width = computedConstraints.width - border.left - border.right - padding.left - padding.right;
					int nodesWidth = 0;
					int finalNodesWidth = 0;
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
							starCount++;
						} else {
							nodesWidth+= guiSubNode.computedConstraints.width;
							finalNodesWidth+= guiSubNode.computedConstraints.width;
						}
					}
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
							guiSubNode.computedConstraints.width = (width - nodesWidth) / starCount;
							if (guiSubNode.computedConstraints.width < 0) {
								guiSubNode.computedConstraints.width = 0;
							}
							finalNodesWidth+= guiSubNode.computedConstraints.width;
						}
					}
					
					// do horizontal alignments, take border, padding into account
					switch (alignments.horizontal) {
						case LEFT:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentLeft = border.left + padding.left;
							}
							break;
						case CENTER:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentLeft = border.left + padding.left + ((width - finalNodesWidth) / 2); 
							}
							break;
						case RIGHT:
							for (int i = 0; i < subNodes.size(); i++) {
								GUINode guiSubNode = subNodes.get(i);
								guiSubNode.computedConstraints.alignmentLeft = (width - finalNodesWidth); 
							}
							break;
					}

					// compute children alignments
					computeVerticalChildrenAlignment();
					
					//
					break;
				}
		}

		// do parent + children top, left adjustments
		setTop(computedConstraints.top);
		setLeft(computedConstraints.left);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#setTop(int)
	 */
	protected void setTop(int top) {
		super.setTop(top);
		top+= computedConstraints.alignmentTop;
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
		left+= computedConstraints.alignmentLeft;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			guiSubNode.setLeft(left);
			if (alignment == Alignment.HORIZONTAL) {
				left+= guiSubNode.computedConstraints.width;
			}
		}
	}

}

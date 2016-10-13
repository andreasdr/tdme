package net.drewke.tdme.gui;

import java.util.ArrayList;

/**
 * GUI element node
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIElementNode extends GUIParentNode {

	protected static final String CONDITION_ALWAYS = "always";
	protected static final String CONDITION_ONMOUSEOVER = "mouseover";
	protected static final String CONDITION_CLICK = "click";

	protected ArrayList<String> activeConditions = new ArrayList<String>();

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param margin
	 * @param show on
	 * @param hide on
	 * @param background color
	 * @param background image
	 */
	protected GUIElementNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints, 
		Border border, 
		Margin margin,
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		String backgroundColor,
		String backgroundImage) throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, border, margin, showOn, hideOn, backgroundColor, backgroundImage);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "element";
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
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentWidth = guiSubNode.getAutoWidth();
			if (contentWidth > width) {
				width = contentWidth;
			}
		}

		// add border, margin
		width+= border.left + border.right;
		width+= margin.left + margin.right;

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
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentHeight = guiSubNode.getAutoHeight();
			if (contentHeight > height) {
				height = contentHeight;
			}
		}

		// add border, margin
		height+= border.top + border.bottom;
		height+= margin.top + margin.bottom;

		//
		return height;
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIParentNode#layout()
	 */
	protected void layout() {
		// super layout
		super.layout();

		// do parent + children top, left adjustments
		setTop(computedConstraints.top);
		setLeft(computedConstraints.left);

		// compute children alignments
		computeHorizontalChildrenAlignment();
		computeVerticalChildrenAlignment();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		activeConditions.clear();
		if (event.x >= computedConstraints.left + computedConstraints.alignmentLeft && event.x <= computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.width &&
			event.y >= computedConstraints.top + computedConstraints.alignmentTop && event.y <= computedConstraints.top + computedConstraints.alignmentTop + computedConstraints.height) {
			switch (event.type) {
				case MOUSE_MOVED:
					activeConditions.add(CONDITION_ONMOUSEOVER);
					break;
				case MOUSE_PRESSED:
					activeConditions.add(CONDITION_CLICK);
					break;
				default:
					break;
			}
		}
		super.handleEvent(event);
	}

}
	
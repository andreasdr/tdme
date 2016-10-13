package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI Screen Node
 * @author andreas
 * @version $Id$
 */
public final class GUIScreenNode extends GUIParentNode {

	private int screenWidth;
	private int screenHeight;

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
	 */
	protected GUIScreenNode(
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
		super(parentNode, id, alignments, requestedConstraints, border, margin, showOn, hideOn, backgroundColor, backgroundImage);
		this.screenWidth = 0;
		this.screenHeight = 0;
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

}

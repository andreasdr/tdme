package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI space node
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISpaceNode extends GUINode {

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
	protected GUISpaceNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		Border border, 
		Margin margin, 
		ArrayList<String> showOn, 
		ArrayList<String> hideOn
		) {
		//
		super(parentNode, id, alignments, requestedConstraints, border, margin, showOn, hideOn);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "space";
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
		// we derive content width from requested -> computed constraints
		return computedConstraints.width;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	protected int getContentHeight() {
		// we derive content height from requested -> computed constraints
		return computedConstraints.height;
	}

	/**
	 * Create requested constraints
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return requested constraints
	 */
	protected static RequestedConstraints createRequestedConstraints(String left, String top, String width, String height) {
		RequestedConstraints constraints = new RequestedConstraints();
		constraints.leftType = getRequestedConstraintsType(left.trim(), RequestedConstraintsType.PIXEL);
		constraints.left = getRequestedConstraintsValue(left.trim(), 0);
		constraints.topType = getRequestedConstraintsType(top.trim(), RequestedConstraintsType.PIXEL);
		constraints.top = getRequestedConstraintsValue(top.trim(), 0);
		constraints.widthType = getRequestedConstraintsType(width.trim(), RequestedConstraintsType.PIXEL);
		constraints.width = getRequestedConstraintsValue(width.trim(), 1);
		constraints.heightType = getRequestedConstraintsType(height.trim(), RequestedConstraintsType.PIXEL);
		constraints.height = getRequestedConstraintsValue(height.trim(), 1);
		return constraints;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	protected void dispose() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected void render(GUIRenderer guiRenderer) {
		// call parent renderer
		super.render(guiRenderer);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		// no op
	}

}

package net.drewke.tdme.gui;


/**
 * GUI image node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIImageNode extends GUIElementChildNode {

	private String src;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 * @param src
	 */
	protected GUIImageNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, String[] showOn, String src) {
		super(parentNode, id, alignments, requestedConstraints, showOn);
		this.src = src;
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "image";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return 100; // TODO: implement me
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return 20; // TODO: implement me
	}

}

package net.drewke.tdme.gui;

/**
 * GUI text node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUITextNode extends GUIElementChildNode {

	private String font;
	private String color;
	private String text;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param requested constraints
	 * @param show on
	 * @param font
	 * @param color
	 * @param text
	 */
	protected GUITextNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints, String[] showOn, String font, String color, String text) {
		super(parentNode, id, requestedConstraints, showOn);
		this.font = font;
		this.color = color;
		this.text = text;
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "text";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return 120; // TODO: implement me
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return 30; // TODO: implement me
	}

}

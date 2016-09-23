package net.drewke.tdme.gui;

/**
 * GUI text node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITextNode extends GUIElementChildNode {

	private String font;
	private String color;
	private String text;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 * @param font
	 * @param color
	 * @param text
	 */
	protected GUITextNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, String[] showOn, String font, String color, String text) {
		super(parentNode, id, alignments, requestedConstraints, showOn);
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected void render(GUIRenderer guiRenderer) {
		// no op
	}

}

package net.drewke.tdme.gui;



/**
 * GUI Panel
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIPanelNode extends GUILayoutNode {

	private String backgroundImage;
	private GUIColor backgroundColor;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param alignment
	 * @param background color
	 * @param background image
	 */
	protected GUIPanelNode(
		GUINode parentNode, 
		String id, 
		Alignments alignments,
		RequestedConstraints requestedConstraints, 
		String alignment, 
		String backgroundColor,
		String backgroundImage) 
		throws GUIParserException {
		//
		super(parentNode, id, alignments, requestedConstraints, alignment);
		this.backgroundColor = backgroundColor == null || backgroundColor.length() == 0?new GUIColor():new GUIColor(backgroundColor);
		this.backgroundImage = backgroundImage;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getNodeType()
	 */
	protected String getNodeType() {
		return "panel";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected void render(GUIRenderer guiRenderer) {
		super.render(guiRenderer);
	}

}

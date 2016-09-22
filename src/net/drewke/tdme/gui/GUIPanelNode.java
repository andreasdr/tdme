package net.drewke.tdme.gui;


/**
 * GUI Panel
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIPanelNode extends GUILayoutNode {

	private String backgroundColor;
	private String backgroundImage; 

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
		String backgroundImage) {
		super(parentNode, id, alignments, requestedConstraints, alignment);
		this.backgroundColor = backgroundColor;
		this.backgroundImage = backgroundImage;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getNodeType()
	 */
	protected String getNodeType() {
		return "panel";
	}

}

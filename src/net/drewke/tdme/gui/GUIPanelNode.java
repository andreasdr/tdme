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
		// screen dimension
		float screenWidth = guiRenderer.gui.width;
		float screenHeight = guiRenderer.gui.height;

		// element location and dimensions
		float left = computedConstraints.left + computedConstraints.alignmentLeft;
		float top = computedConstraints.top + computedConstraints.alignmentTop;
		float width = computedConstraints.width;
		float height = computedConstraints.height;

		// background color
		float[] bgColorData = backgroundColor.getData();

		// render panel background
		guiRenderer.bindTexture(0);
		guiRenderer.addQuad(
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
			0f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
			1f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
			1f, 0f, 
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			bgColorData[0], bgColorData[1], bgColorData[2], bgColorData[3],
			0f, 0f
		);
		guiRenderer.render();

		// render children
		super.render(guiRenderer);
	}

}

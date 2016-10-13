package net.drewke.tdme.gui;

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;
import net.drewke.tdme.gui.GUINode.Border;
import net.drewke.tdme.gui.GUINode.Margin;


/**
 * GUI image node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIImageNode extends GUINode {

	private Texture texture;
	private int textureId;

	private float[] color = {1f, 1f, 1f, 1f};

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
	 * @param src
	 */
	protected GUIImageNode(
		GUIParentNode parentNode, 
		String id, 
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		Border border, 
		Margin margin, 
		ArrayList<String> showOn, 
		ArrayList<String> hideOn, 
		String src
		) {
		//
		super(parentNode, id, alignments, requestedConstraints, border, margin, showOn, hideOn);
		this.texture = GUI.getImage(src);
		this.textureId = Engine.getInstance().getTextureManager().addTexture(texture);
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "image";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return (texture != null?texture.getWidth():0) + border.left + border.right + margin.left + margin.right;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return (texture != null?texture.getHeight():0) + border.top + border.bottom + margin.top + margin.bottom;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	protected void dispose() {
		Engine.getInstance().getTextureManager().removeTexture(texture.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected void render(GUIRenderer guiRenderer) {
		// check if conditions apply
		if (checkConditions() == false) return;

		// call parent renderer
		super.render(guiRenderer);

		// screen dimension
		float screenWidth = guiRenderer.gui.width;
		float screenHeight = guiRenderer.gui.height;

		// element location and dimensions
		float left = computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.contentAlignmentLeft;
		float top = computedConstraints.top  + computedConstraints.alignmentTop + computedConstraints.contentAlignmentTop;
		float width = getContentWidth();
		float height = getContentHeight();

		// render panel background
		guiRenderer.bindTexture(textureId);
		guiRenderer.addQuad(
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			0f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			1f, 1f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			1f, 0f, 
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			0f, 0f
		);
		guiRenderer.render();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		// no op
	}

}

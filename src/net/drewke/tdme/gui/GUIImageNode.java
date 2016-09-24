package net.drewke.tdme.gui;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;


/**
 * GUI image node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIImageNode extends GUIElementChildNode {

	private String src;
	private Texture texture;
	private int textureId;

	private float[] color = {1f, 1f, 1f, 1f};

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
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentWidth()
	 */
	protected int getContentWidth() {
		return texture != null?texture.getWidth():0;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	protected int getContentHeight() {
		return texture != null?texture.getHeight():0;
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
		float left = computedConstraints.left + computedConstraints.contentAlignmentLeft;
		float top = computedConstraints.top + computedConstraints.contentAlignmentTop;
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

}

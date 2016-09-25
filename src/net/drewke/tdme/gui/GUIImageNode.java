package net.drewke.tdme.gui;

import java.util.ArrayList;

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
	 * @param hide on
	 * @param src
	 */
	protected GUIImageNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, ArrayList<String> showOn, ArrayList<String> hideOn, String src) {
		super(parentNode, id, alignments, requestedConstraints, showOn, hideOn);
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
		// check if conditions apply
		if (checkConditions() == false) return;

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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#handleEvent(net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUIMouseEvent event) {
		// no op
	}

}

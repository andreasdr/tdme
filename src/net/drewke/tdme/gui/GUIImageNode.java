package net.drewke.tdme.gui;

import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;


/**
 * GUI image node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIImageNode extends GUIElementChildNode {

	private String src;
	private Texture texture;

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
		this.texture = TextureLoader.loadTexture(".", src);
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

}

package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.renderer.GUIRenderer;


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

	// effect colors
	private GUIColor effectColorMul;
	private GUIColor effectColorAdd;


	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param alignments
	 * @param requested constraints
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param src
	 * @param effect color mul
	 * @param effect color add
	 */
	public GUIImageNode(
		GUIScreenNode screenNode,
		GUIParentNode parentNode, 
		String id,
		Flow flow,
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor,
		Border border, 
		Padding padding, 
		GUINodeConditions showOn, 
		GUINodeConditions hideOn, 
		String src,
		GUIColor effectColorMul,
		GUIColor effectColorAdd
		) throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.texture = GUI.getImage(src);
		this.textureId = Engine.getInstance().getTextureManager().addTexture(texture);
		this.effectColorMul = effectColorMul;
		this.effectColorAdd = effectColorAdd;
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
	public int getContentWidth() {
		return (texture != null?texture.getWidth():0) + border.left + border.right + padding.left + padding.right;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElementChildNode#getContentHeight()
	 */
	public int getContentHeight() {
		return (texture != null?texture.getHeight():0) + border.top + border.bottom + padding.top + padding.bottom;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	public void dispose() {
		Engine.getInstance().getTextureManager().removeTexture(texture.getId());
		// parent dispose
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// call parent renderer
		super.render(guiRenderer, floatingNodes);

		// screen dimension
		float screenWidth = guiRenderer.getGUI().getWidth();
		float screenHeight = guiRenderer.getGUI().getHeight();

		// element location and dimensions
		float left = computedConstraints.left + computedConstraints.alignmentLeft + computedConstraints.contentAlignmentLeft;
		float top = computedConstraints.top  + computedConstraints.alignmentTop + computedConstraints.contentAlignmentTop;
		float width = getContentWidth();
		float height = getContentHeight();

		// render panel background
		guiRenderer.bindTexture(textureId);
		guiRenderer.setEffectColorMul(effectColorMul);
		guiRenderer.setEffectColorAdd(effectColorAdd);
		guiRenderer.addQuad(
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			0f, 0f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			1f, 0f, 
			((left + width) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			1f, 1f, 
			((left) / (screenWidth / 2f)) - 1f, 
			((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
			color[0], color[1], color[2], color[3],
			0f, 1f
		);
		guiRenderer.render();
	}

}

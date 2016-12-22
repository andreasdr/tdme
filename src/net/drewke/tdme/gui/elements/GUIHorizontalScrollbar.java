package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI vertical scrollbar element
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIHorizontalScrollbar extends GUIElement {

	private static final String NAME = "horizontal-scrollbar";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUIHorizontalScrollbar() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "horizontal-scrollbar.xml");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#getTemplate()
	 */
	public String getTemplate() {
		return template;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#getAttributes(net.drewke.tdme.gui.nodes.GUIScreenNode)
	 */
	public HashMap<String, String> getAttributes(GUIScreenNode screenNode) {
		attributes.clear();
		attributes.put("id", screenNode.allocateNodeId());
		attributes.put("width", "100%");
		attributes.put("height", "100%");
		attributes.put("horizontal-align", "left");
		attributes.put("vertical-align", "top");
		attributes.put("alignment", "vertical");
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#createController(net.drewke.tdme.gui.nodes.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return new GUIHorizontalScrollbarController(node);
	}

}

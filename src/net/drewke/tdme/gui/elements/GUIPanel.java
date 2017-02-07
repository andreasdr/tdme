package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI panel element
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIPanel extends GUIElement {

	private static final String NAME = "panel";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUIPanel() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "panel.xml");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getTemplate(net.drewke.tdme.utils.HashMap)
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
		attributes.put("left", "");
		attributes.put("top", "");
		attributes.put("width", "auto");
		attributes.put("height", "auto");
		attributes.put("alignment", "none");
		attributes.put("horizontal-align", "center");
		attributes.put("vertical-align", "center");
		attributes.put("border-color", "#808080");
		attributes.put("border", "1");
		attributes.put("padding", "0");
		attributes.put("background-color", "#F0F0F0");
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#createController(net.drewke.tdme.gui.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return null;
	}

}

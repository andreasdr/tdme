package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI tab element
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITab extends GUIElement {

	private static final String NAME = "tab";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUITab() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "tab.xml");
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
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#createController(net.drewke.tdme.gui.nodes.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return new GUITabController(node);
	}

}

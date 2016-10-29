package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI tab content element
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUITabContent extends GUIElement {

	private static final String NAME = "tab-content";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUITabContent() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "tab-content.xml");
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
	 * @see net.drewke.tdme.gui.elements.GUIElement#getAttributes()
	 */
	public HashMap<String, String> getAttributes() {
		attributes.clear();
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.elements.GUIElement#createController(net.drewke.tdme.gui.nodes.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return new GUITabContentController(node);
	}

}

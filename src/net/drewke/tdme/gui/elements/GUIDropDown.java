package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI select box
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIDropDown extends GUIElement  {

	private static final String NAME = "dropdown";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUIDropDown() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "dropdown.xml");
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
	 * @see net.drewke.tdme.gui.GUIElement#getAttributes()
	 */
	public HashMap<String, String> getAttributes() {
		attributes.clear();
		attributes.put("id", "");
		attributes.put("width", "100%");
		attributes.put("height", "auto");
		attributes.put("padding", "10");
		attributes.put("text", "10");
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#createController(net.drewke.tdme.gui.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return new GUIDropDownController(node);
	}

}

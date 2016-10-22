package net.drewke.tdme.gui;

import java.io.IOException;

import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI Checkbox
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUICheckbox extends GUIElement  {

	private static final String NAME = "checkbox";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	protected GUICheckbox() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "checkbox.xml");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getName()
	 */
	protected String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getTemplate(net.drewke.tdme.utils.HashMap)
	 */
	protected String getTemplate() {
		return template;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getAttributes()
	 */
	protected HashMap<String, String> getAttributes() {
		attributes.clear();
		attributes.put("id", "");
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#createController(net.drewke.tdme.gui.GUINode)
	 */
	protected GUINodeController createController(GUINode node) {
		return new GUICheckboxController(node);
	}

}

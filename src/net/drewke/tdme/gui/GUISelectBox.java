package net.drewke.tdme.gui;

import java.io.IOException;

import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI select box
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUISelectBox extends GUIElement  {

	private static final String NAME = "selectbox";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	protected GUISelectBox() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "selectbox.xml");
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
		// TODO Auto-generated method stub
		return new GUISelectBoxController(node);
	}

}

package net.drewke.tdme.gui.elements;

import java.io.IOException;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI select box
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUISelectBox extends GUIElement  {

	private static final String NAME = "selectbox";

	private HashMap<String, String> attributes;
	private String template;

	/**
	 * Constructor
	 */
	public GUISelectBox() throws IOException {
		attributes = new HashMap<String, String>();
		template = FileSystem.getInstance().getContent("resources/gui/definitions/elements", "selectbox.xml");
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
		attributes.put("width", "100%");
		attributes.put("height", "auto");
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#createController(net.drewke.tdme.gui.GUINode)
	 */
	public GUINodeController createController(GUINode node) {
		return new GUISelectBoxController(node);
	}

}

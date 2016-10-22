package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI element
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIElement {

	/**
	 * @return name
	 */
	public abstract String getName();

	/**
	 * @return template
	 */
	public abstract String getTemplate();

	/**
	 * @return default attributes
	 */
	public abstract HashMap<String, String> getAttributes();

	/**
	 * Create controller which is attached to this node
	 * @param node
	 * @return
	 */
	public abstract GUINodeController createController(GUINode node);

}

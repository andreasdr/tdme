package net.drewke.tdme.gui;

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
	protected abstract String getName();

	/**
	 * @return template
	 */
	protected abstract String getTemplate();

	/**
	 * @return default attributes
	 */
	protected abstract HashMap<String, String> getAttributes();

	/**
	 * Create controller which is attached to this node
	 * @param node
	 * @return
	 */
	protected abstract GUINodeController createController(GUINode node);

}

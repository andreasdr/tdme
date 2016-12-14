package net.drewke.tdme.gui.events;

import net.drewke.tdme.gui.nodes.GUIElementNode;

/**
 * GUI change listener
 * @author Andreas Drewke
 * @version $Id$
 */
public interface GUIChangeListener {

	/**
	 * On value changed
	 * @param node
	 */
	public void onValueChanged(GUIElementNode node);

}

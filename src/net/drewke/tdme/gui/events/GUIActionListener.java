package net.drewke.tdme.gui.events;

import net.drewke.tdme.gui.nodes.GUIElementNode;

/**
 * GUI action listener
 * @author Andreas Drewke
 * @version $Id$
 */
public interface GUIActionListener {

	/**
	 * On action performed
	 * @param element node
	 */
	public void onActionPerformed(GUIElementNode node);

}
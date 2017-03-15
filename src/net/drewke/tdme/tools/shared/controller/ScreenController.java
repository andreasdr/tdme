package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.tools.shared.views.View;

/**
 * Screen controller, which connects GUI screen definition with code
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class ScreenController {

	/**
	 * @return screen node
	 */
	public abstract GUIScreenNode getScreenNode();

	/**
	 * Init
	 */
	public abstract void init();

	/**
	 * Dispose
	 */
	public abstract void dispose();

}

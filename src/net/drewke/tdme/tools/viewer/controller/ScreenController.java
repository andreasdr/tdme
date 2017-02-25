package net.drewke.tdme.tools.viewer.controller;

import net.drewke.tdme.gui.nodes.GUIScreenNode;

/**
 * ScreenController
 * @author Andreas Drewke
 *
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

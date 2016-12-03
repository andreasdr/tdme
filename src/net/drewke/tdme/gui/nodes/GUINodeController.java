package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;

/**
 * GUI element controller
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUINodeController {

	protected GUINode node;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUINodeController(GUINode node) {
		this.node = node;
	}

	/**
	 * @return node
	 */
	public GUINode getNode() {
		return node;
	}

	/**
	 * Initialize controller after element has been created
	 */
	public abstract void init();

	/**
	 * Dispose controller
	 */
	public abstract void dispose();

	/**
	 * Handle mouse event
	 * @param node
	 * @param event
	 */
	public abstract void handleMouseEvent(GUINode node, GUIMouseEvent event);

	/**
	 * Handle keyboard event
	 * @param node
	 * @param event
	 */
	public abstract void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event);

	/**
	 * On focus gained
	 */
	public abstract void onFocusGained();

	/**
	 * On focus lost
	 */
	public abstract void onFocusLost();

}
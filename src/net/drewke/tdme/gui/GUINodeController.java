package net.drewke.tdme.gui;

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
	 * Initialize controller after element has been created
	 */
	public abstract void init();

	/**
	 * Dispose controller
	 */
	public abstract void dispose();

	/**
	 * Handle event
	 * @param node
	 * @param event
	 */
	public abstract void handleEvent(GUINode node, GUIMouseEvent event);

}

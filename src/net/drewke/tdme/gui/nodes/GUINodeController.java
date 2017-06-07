package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.utils.MutableString;

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
	public abstract void initialize();

	/**
	 * Dispose controller
	 */
	public abstract void dispose();

	/**
	 * Post layout event
	 */
	public abstract void postLayout();

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
	 * Tick method will be executed once per frame
	 */
	public abstract void tick();

	/**
	 * On focus gained
	 */
	public abstract void onFocusGained();

	/**
	 * On focus lost
	 */
	public abstract void onFocusLost();

	/**
	 * @return has value
	 */
	public abstract boolean hasValue();

	/**
	 * @return value
	 */
	public abstract MutableString getValue();

	/**
	 * Set value
	 * @param value
	 */
	public abstract void setValue(MutableString value);

	/**
	 * @return is disabled
	 */
	public abstract boolean isDisabled();

	/**
	 * Set disabled 
	 * @param disabled
	 */
	public abstract void setDisabled(boolean disabled);

}
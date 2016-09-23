package net.drewke.tdme.gui;

import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.utils.HashMap;

/**
 * GUI
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUI {

	private GUIRenderer guiRenderer;
	private HashMap<String, GUIScreenNode> screens;

	/**
	 * Constructor
	 * @param renderer
	 */
	public GUI(GLRenderer renderer) {
		this.guiRenderer = new GUIRenderer(renderer);
		this.screens = new HashMap<String, GUIScreenNode>();
	}

	/**
	 * Get screen
	 * @param id
	 * @return screen
	 */
	public GUIScreenNode getScreen(String id) {
		return screens.get(id);
	}

	/**
	 * Add screen
	 * @param id
	 * @param gui
	 */
	public void addScreen(String id, GUIScreenNode screen) {
		screens.put(id,  screen);
	}

	/**
	 * Render screen with given id
	 */
	public void render(String id) {
		GUIScreenNode screen = screens.get(id);
		if (screen != null) {
			screen.render(guiRenderer);
		}
	}

}

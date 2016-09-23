package net.drewke.tdme.gui;

import net.drewke.tdme.engine.Engine;
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

	protected int width;
	protected int height;

	/**
	 * Constructor
	 * @param renderer
	 */
	public GUI(GLRenderer renderer) {
		this.guiRenderer = new GUIRenderer(this, renderer);
		this.screens = new HashMap<String, GUIScreenNode>();
	}

	/**
	 * Init
	 */
	public void init() {
		guiRenderer.init();
	}

	/**
	 * Reshape
	 * @param width
	 * @param height
	 */
	public void reshape(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		guiRenderer.dispose();
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
			// update screen size and layout
			// 	TODO: only if screen dimension has changed and layout is invalidated or has not yet been done
			screen.setScreenSize(width, height);
			screen.layout(); 

			// render
			guiRenderer.initRendering();
			screen.render(guiRenderer);
			guiRenderer.doneRendering();
		}
	}

}

package net.drewke.tdme.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.gui.GUIMouseEvent.Type;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Pool;

/**
 * GUI
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUI implements MouseListener, MouseMotionListener {

	private Engine engine;
	private GUIRenderer guiRenderer;
	private HashMap<String, GUIScreenNode> screens;

	private static HashMap<String, GUIFont> fontCache = new HashMap<String, GUIFont>();
	private static HashMap<String, Texture> imageCache = new HashMap<String, Texture>();

	private Pool<GUIMouseEvent> mouseEventsPool = new Pool<GUIMouseEvent>() {
		public GUIMouseEvent instantiate() {
			return new GUIMouseEvent();
		}
		
	};
	private ArrayList<GUIMouseEvent> mouseEvents = new ArrayList<GUIMouseEvent>(); 

	protected int width;
	protected int height;

	/**
	 * Constructor
	 * @param engine 
	 * @param renderer
	 */
	public GUI(Engine engine, GLRenderer renderer) {
		this.engine = engine;
		this.guiRenderer = new GUIRenderer(this, renderer);
		this.screens = new HashMap<String, GUIScreenNode>();
		this.width = 0;
		this.height = 0;
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
	 * @return mouse events
	 */
	public ArrayList<GUIMouseEvent> getMouseEvents() {
		return mouseEvents;
	}

	/**
	 * Get font
	 * @param file name
	 * @return
	 */
	protected static GUIFont getFont(String fileName) {
		// determine key
		String key = null; 
		try {
			key = new File(fileName).getCanonicalPath();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}

		// get font from cache
		GUIFont font = fontCache.get(key);

		// do we have it in cache
		if (font == null) {
			// nope, parse and put into key
			try {
				font = GUIFont.parse(new File(fileName).getParentFile().getCanonicalPath() , new File(fileName).getName());
			} catch (Exception exception) {
				exception.printStackTrace();
				return null;
			}
			fontCache.put(key, font);
		}

		// return
		return font;
	}

	/**
	 * Get image
	 * @param file name
	 * @return
	 */
	protected static Texture getImage(String fileName) {
		// determine key
		String key = null; 
		try {
			key = new File(fileName).getCanonicalPath();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}

		// get texture from cache
		Texture image = imageCache.get(key);

		// do we have it in cache
		if (image == null) {
			// nope, parse and put into key
			try {
				image = TextureLoader.loadTexture(new File(fileName).getParentFile().getCanonicalPath() , new File(fileName).getName());
			} catch (Exception exception) {
				exception.printStackTrace();
				return null;
			}
			imageCache.put(key, image);
		}

		// return
		return image;
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
	public void render(String screenId) {
		GUIScreenNode screen = screens.get(screenId);
		if (screen != null) {
			// update screen size and layout if reshaped
			if (screen.getScreenWidth() != width || screen.getScreenHeight() != height) {
				screen.setScreenSize(width, height);
				screen.layout();
				// debugging
				System.out.println(screen);
			}

			// render
			guiRenderer.initRendering();
			screen.render(guiRenderer);
			guiRenderer.doneRendering();
		}
	}

	/**
	 * Handle events
	 * @param screen id
	 */
	public void handleEvents(String screenId) {
		GUIScreenNode screen = screens.get(screenId);
		if (screen != null) {
			for (int i = 0; i < mouseEvents.size(); i++) {
				GUIMouseEvent event = mouseEvents.get(i);
				screen.handleEvent(event);
			}
		}
	}

	/**
	 * Discard events
	 */
	public void discardEvents() {
		mouseEventsPool.reset();
		mouseEvents.clear();
	} 

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		// not yet
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_MOVED);
		guiMouseEvent.setX(e.getX());
		guiMouseEvent.setY(e.getY());
		guiMouseEvent.setButton(e.getButton());
		mouseEvents.add(guiMouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// not yet
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_PRESSED);
		guiMouseEvent.setX(e.getX());
		guiMouseEvent.setY(e.getY());
		guiMouseEvent.setButton(e.getButton());
		mouseEvents.add(guiMouseEvent);	
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_RELEASED);
		guiMouseEvent.setX(e.getX());
		guiMouseEvent.setY(e.getY());
		guiMouseEvent.setButton(e.getButton());
		mouseEvents.add(guiMouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// no op
	}

}

package net.drewke.tdme.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.renderer.GUIFont;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Pool;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

/**
 * GUI
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUI implements MouseListener, KeyListener {

	private GUIRenderer guiRenderer;

	private Engine engine;
	private HashMap<String, GUIScreenNode> screens;

	private static HashMap<String, GUIFont> fontCache = new HashMap<String, GUIFont>();
	private static HashMap<String, Texture> imageCache = new HashMap<String, Texture>();

	private Pool<GUIMouseEvent> mouseEventsPool = new Pool<GUIMouseEvent>() {
		public GUIMouseEvent instantiate() {
			return new GUIMouseEvent();
		}
	};
	private ArrayList<GUIMouseEvent> mouseEvents = new ArrayList<GUIMouseEvent>(); 

	private Pool<GUIKeyboardEvent> keyboardEventsPool = new Pool<GUIKeyboardEvent>() {
		public GUIKeyboardEvent instantiate() {
			return new GUIKeyboardEvent();
		}
	};
	private ArrayList<GUIKeyboardEvent> keyboardEvents = new ArrayList<GUIKeyboardEvent>(); 

	private int width;
	private int height;

	/**
	 * Constructor
	 * @param engine 
	 * @param GUI renderer
	 */
	public GUI(Engine engine, GUIRenderer guiRenderer) {
		this.engine = engine;
		this.guiRenderer = guiRenderer;
		this.screens = new HashMap<String, GUIScreenNode>();
		this.width = 0;
		this.height = 0;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Init
	 */
	public void init() {
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
		reset();
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
	public static GUIFont getFont(String fileName) {
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
	public static Texture getImage(String fileName) {
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
	 * Removes an screen
	 * @param id
	 */
	public void removeScreen(String id) {
		GUIScreenNode screen = screens.remove(id);
		if (screen != null) {
			screen.dispose();
		}
	}

	/**
	 * Removes all screens and caches
	 */
	public void reset() {
		Iterator<String> screenKeys = screens.getKeysIterator();
		ArrayList<String> entitiesToRemove = new ArrayList<String>();
		while(screenKeys.hasNext()) {
			String screen = screenKeys.next();
			entitiesToRemove.add(screen);
		}
		for (int i = 0; i< entitiesToRemove.size(); i++) {
			removeScreen(entitiesToRemove.get(i));
		}
		fontCache.clear();
		imageCache.clear();
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
			guiRenderer.setGUI(this);
			engine.initGUIMode();
			guiRenderer.initRendering();
			screen.setConditionsMet();
			screen.tick();
			screen.render(guiRenderer);
			guiRenderer.doneRendering();
			engine.doneGUIMode();
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
				screen.handleMouseEvent(event);
			}
			for (int i = 0; i < keyboardEvents.size(); i++) {
				GUIKeyboardEvent event = keyboardEvents.get(i);
				screen.handleKeyboardEvent(event);
			}
		}
	}

	/**
	 * Discard events
	 */
	public void discardEvents() {
		mouseEventsPool.reset();
		mouseEvents.clear();
		keyboardEventsPool.reset();
		keyboardEvents.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseClicked(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseDragged(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent event) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_DRAGGED);
		guiMouseEvent.setX(event.getX());
		guiMouseEvent.setY(event.getY());
		guiMouseEvent.setButton(event.getButton());
		mouseEvents.add(guiMouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseEntered(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseExited(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent event) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_MOVED);
		guiMouseEvent.setX(event.getX());
		guiMouseEvent.setY(event.getY());
		guiMouseEvent.setButton(event.getButton());
		mouseEvents.add(guiMouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mousePressed(com.jogamp.newt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent event) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_PRESSED);
		guiMouseEvent.setX(event.getX());
		guiMouseEvent.setY(event.getY());
		guiMouseEvent.setButton(event.getButton());
		guiMouseEvent.setProcessed(false);
		mouseEvents.add(guiMouseEvent);	
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseReleased(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent event) {
		GUIMouseEvent guiMouseEvent = mouseEventsPool.allocate();
		guiMouseEvent.setTime(System.currentTimeMillis());
		guiMouseEvent.setType(Type.MOUSE_RELEASED);
		guiMouseEvent.setX(event.getX());
		guiMouseEvent.setY(event.getY());
		guiMouseEvent.setButton(event.getButton());
		guiMouseEvent.setProcessed(false);
		mouseEvents.add(guiMouseEvent);

		// add additional mouse moved event
		mouseMoved(event);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseWheelMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseWheelMoved(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyPressed(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {
		GUIKeyboardEvent guiKeyboardEvent = keyboardEventsPool.allocate();
		guiKeyboardEvent.setTime(System.currentTimeMillis());
		guiKeyboardEvent.setType(GUIKeyboardEvent.Type.KEY_PRESSED);
		guiKeyboardEvent.setKeyCode(event.getKeyCode());
		guiKeyboardEvent.setKeyChar(event.getKeyChar());
		guiKeyboardEvent.setMetaDown(event.isMetaDown());
		guiKeyboardEvent.setControlDown(event.isControlDown());
		guiKeyboardEvent.setAltDown(event.isAltDown());
		guiKeyboardEvent.setShiftDown(event.isShiftDown());
		guiKeyboardEvent.setProcessed(false);
		keyboardEvents.add(guiKeyboardEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyReleased(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent event) {
		GUIKeyboardEvent guiKeyboardEvent = keyboardEventsPool.allocate();
		guiKeyboardEvent.setTime(System.currentTimeMillis());
		guiKeyboardEvent.setType(GUIKeyboardEvent.Type.KEY_RELEASED);
		guiKeyboardEvent.setKeyCode(event.getKeyCode());
		guiKeyboardEvent.setKeyChar(event.getKeyChar());
		guiKeyboardEvent.setMetaDown(event.isMetaDown());
		guiKeyboardEvent.setControlDown(event.isControlDown());
		guiKeyboardEvent.setAltDown(event.isAltDown());
		guiKeyboardEvent.setShiftDown(event.isShiftDown());
		guiKeyboardEvent.setProcessed(false);
		keyboardEvents.add(guiKeyboardEvent);
	}

}

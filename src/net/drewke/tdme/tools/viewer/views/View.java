package net.drewke.tdme.tools.viewer.views;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * View interface
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class View implements KeyListener, MouseListener {

	/**
	 * Initiates the view
	 * @param drawable
	 */
	public void init(GLAutoDrawable drawable) {
	}

	/**
	 * Renders the view
	 */
	public void display(GLAutoDrawable drawable) {
	}

	/**
	 * Disposes the view
	 * @param drawable
	 */
	public void dispose(GLAutoDrawable drawable)  {
	}

	/**
	 * handle nifty input events, ...
	 * @param drawable
	 */
	public void doInputSystem() {
	}

}

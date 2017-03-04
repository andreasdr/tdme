package net.drewke.tdme.tools.shared.views;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * View interface, this combines application logic, regarding a application view, with screen controllers
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class View {

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
	 * handle input events, ...
	 */
	public void handleEvents() {
	}

}

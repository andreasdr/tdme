package net.drewke.tdme.tools.shared.views;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * View interface, this combines application logic, regarding a application view, with screen controllers
 * @author Andreas Drewke
 * @version $Id$
 */
public interface View {

	/**
	 * Initiates the view
	 */
	abstract public void init();

	/**
	 * Activate view
	 */
	abstract public void activate();

	/**
	 * Renders the view
	 */
	abstract public void display(GLAutoDrawable drawable);

	/**
	 * Deactivate view
	 */
	abstract public void deactivate();

	/**
	 * Disposes the view
	 */
	abstract public void dispose();

}

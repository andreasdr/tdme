package net.drewke.tdme.tools.particlesystem;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ParticleSystemView;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * TDME Particle System
 * @author andreas.drewke
 * @version $Id$
 */
public final class TDMEParticleSystem implements GLEventListener, WindowListener {

	private final static String VERSION = "0.9.9";

	private static TDMEParticleSystem instance;

	private Engine engine;

	private GLWindow glWindow;
	private FPSAnimator animator;

	private View view;
	private boolean viewInitialized;
	private View viewNew;

	private boolean quitRequested = false;

	private PopUps popUps;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		System.out.println("TDMEParticleSystem " + VERSION);
		System.out.println("Programmed 2017 by Andreas Drewke, drewke.net.");
		System.out.println();

		// no nifty logging
		Logger.getLogger("").setLevel(Level.SEVERE);

		// create GL caps
		GLProfile glp = Engine.getProfile();
		GLCapabilities caps = new GLCapabilities(glp);
		System.out.println(glp);
		System.out.println(caps);

		// create GL window
		GLWindow glWindow = GLWindow.create(caps);
		glWindow.setTitle("TDMEParticleSystem " + VERSION);

		// animator
		FPSAnimator animator = new FPSAnimator(glWindow, 60);

		// tdme particle system
		TDMEParticleSystem tdmeLevelEditor = new TDMEParticleSystem(glWindow, animator);
		
		// GL Window
		glWindow.addWindowListener(tdmeLevelEditor);
		glWindow.addGLEventListener(tdmeLevelEditor);
		glWindow.setSize(800, 600);
		glWindow.setVisible(true);

		// start animator
		animator.start();
	}

	/**
	 * Public constructor
	 * @param gl window
	 * @param animator
	 */
	public TDMEParticleSystem(GLWindow glWindow, FPSAnimator animator) {
		this.glWindow = glWindow;
		this.animator = animator;
		TDMEParticleSystem.instance = this;
		engine = Engine.getInstance();
		view = null;
		viewInitialized = false;
		viewNew = null;
		popUps = new PopUps();
	}

	/**
	 * @return particle system instance
	 */
	public static TDMEParticleSystem getInstance() {
		return instance;
	}

	/**
	 * Set up new view
	 * @param view
	 */
	public void setView(View view) {
		viewNew = view;
	}

	/**
	 * @return current view
	 */
	public View getView() {
		return view;
	}

	/**
	 * Request to exit the viewer
	 */
	public void quit() {
		quitRequested = true;
	}

	/**
	 * Renders the scene 
	 */
	public void display(GLAutoDrawable drawable) {
		// replace view if requested
		if (viewNew != null) {
			if (view != null && viewInitialized == true) {
				view.deactivate();
				view.dispose();
				viewInitialized = false;
			}
			view = viewNew;
			viewNew = null;
		}

		// do view init, render
		if (view != null) {
			if (viewInitialized == false) {
				view.init();
				view.activate();
				viewInitialized = true;
			}
			view.display(drawable);
		}

		//
		engine.display(drawable);

		// render view
		view.display(drawable);

		// 
		if (quitRequested == true) {
			if (view != null) {
				view.deactivate();
				view.dispose();
			}
			animator.stop();
			glWindow.setVisible(false);
			System.exit(0);
		}
	}

	/**
	 * Shutdown tdme viewer
	 */
	public void dispose(GLAutoDrawable drawable) {
		if (view != null && viewInitialized == true) {
			view.deactivate();
			view.dispose();
			view = null;
		}
		engine.dispose(drawable);
		Tools.oseDispose(drawable);
	}

	/**
	 * Initialize tdme level editor
	 */
	public void init(GLAutoDrawable drawable) {
		// init engine
		engine.init(drawable);
		
		// register gui to mouse, keyboard events
		glWindow.addMouseListener(engine.getGUI());
		glWindow.addKeyListener(engine.getGUI());

		// off screen engine init
		Tools.oseInit(drawable);

		// pop ups
		popUps.init();

		// view
		setView(new ParticleSystemView(popUps));
	}

	/**
	 * reshape tdme level editor
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowDestroyNotify(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowDestroyNotify(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowDestroyed(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowDestroyed(WindowEvent arg0) {
		System.exit(0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowGainedFocus(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowLostFocus(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowLostFocus(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowMoved(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowMoved(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowRepaint(com.jogamp.newt.event.WindowUpdateEvent)
	 */
	public void windowRepaint(WindowUpdateEvent arg0) {		
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.WindowListener#windowResized(com.jogamp.newt.event.WindowEvent)
	 */
	public void windowResized(WindowEvent arg0) {
	}

}
package net.drewke.tdme.tools.viewer;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ModelViewerView;
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
 * TDME Level Editor
 * @author andreas.drewke
 * @version $Id$
 */
public final class TDMEViewer implements GLEventListener, WindowListener {

	private final static String VERSION = "0.9.9";

	private static TDMEViewer instance;

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
		String modelFileName = null;

		//
		System.out.println("TDMEViewer " + VERSION);
		System.out.println("Programmed 2014,...,2017 by Andreas Drewke, drewke.net.");
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
		glWindow.setTitle("TDMEViewer " + VERSION);

		// animator
		FPSAnimator animator = new FPSAnimator(glWindow, 60);

		// tdme level editor
		TDMEViewer tdmeLevelEditor = new TDMEViewer(glWindow, animator, modelFileName);
		
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
	 * @param model file name
	 */
	public TDMEViewer(GLWindow glWindow, FPSAnimator animator, String modelFileName) {
		this.glWindow = glWindow;
		this.animator = animator;
		TDMEViewer.instance = this;
		engine = Engine.getInstance();
		view = null;
		viewInitialized = false;
		viewNew = null;
		popUps = new PopUps();
	}

	/**
	 * @return level editor instance
	 */
	public static TDMEViewer getInstance() {
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
				view.dispose(drawable);
				viewInitialized = false;
			}
			view = viewNew;
			viewNew = null;
		}

		// do view init, render
		if (view != null) {
			if (viewInitialized == false) {
				view.init(drawable);
				viewInitialized = true;
			}
			view.display(drawable);
		}

		//
		engine.display(drawable);

		// view handle events
		if (view != null) {
			view.handleEvents();
		}

		// render view
		view.display(drawable);

		// 
		if (quitRequested == true) {
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
			view.dispose(drawable);
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
		setView(new ModelViewerView(popUps));
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
package net.drewke.tdme.tools.leveleditor;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
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

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendFactory;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

/**
 * TDME Level Editor
 * @author andreas.drewke
 * @version $Id$
 */
public final class TDMELevelEditor implements GLEventListener, WindowListener {

	private final static String VERSION = "0.9.9";

	private static TDMELevelEditor instance;

	private Engine engine;

	private GLWindow glWindow;
	private FPSAnimator animator;

	private View view;
	private boolean viewInitialized;
	private View viewNew;

	private boolean quitRequested = false;

	private LevelEditorLevel level;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String modelFileName = null;

		//
		System.out.println("TDMELevelEditor " + VERSION);
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
		glWindow.setTitle("TDMELevelEditor " + VERSION);

		// animator
		FPSAnimator animator = new FPSAnimator(glWindow, 60);

		// tdme level editor
		TDMELevelEditor tdmeLevelEditor = new TDMELevelEditor(glWindow, animator, modelFileName);
		
		// GL Window
		glWindow.addWindowListener(tdmeLevelEditor);
		glWindow.addGLEventListener(tdmeLevelEditor);
		glWindow.setSize(1024, 600);
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
	public TDMELevelEditor(GLWindow glWindow, FPSAnimator animator, String modelFileName) {
		this.glWindow = glWindow;
		this.animator = animator;
		TDMELevelEditor.instance = this;
		level = new LevelEditorLevel();
		engine = Engine.getInstance();
		view = null;
		viewInitialized = false;
		viewNew = null;
	}

	/**
	 * @return level editor instance
	 */
	public static TDMELevelEditor getInstance() {
		return instance;
	}

	/**
	 * @return model library
	 */
	public LevelEditorModelLibrary getModelLibrary() {
		return level.getModelLibrary();
	}

	/**
	 * @return level
	 */
	public LevelEditorLevel getLevel() {
		return level;
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

		// view inputsystem
		if (view != null) {
			view.handleEvents();
		}

		//
		Engine.getInstance().initGUIMode();

		// render view
		view.display(drawable);

		//
		Engine.getInstance().doneGUIMode();

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
		engine.init(drawable);

		// register gui to mouse, keyboard events
		glWindow.addMouseListener(engine.getGUI());
		glWindow.addKeyListener(engine.getGUI());

		// init off screen engine
		Tools.oseInit(drawable);

		// show up level editor view
		setView(new LevelEditorView());
	}

	/**
	 * reshape tdme level editor
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
	}

	/**
	 * Switch to level editor
	 */
	public void switchToLevelEditor() {
		setView(new LevelEditorView());
	}

	/**
	 * Switch to level editor
	 */
	public void switchToModelLibrary() {
		// setView(new ModelLibraryView());
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
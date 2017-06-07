package net.drewke.tdme.tools.leveleditor;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.leveleditor.controller.LevelEditorEntityLibraryScreenController;
import net.drewke.tdme.tools.leveleditor.views.EmptyView;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.leveleditor.views.ModelViewerView;
import net.drewke.tdme.tools.leveleditor.views.ParticleSystemView;
import net.drewke.tdme.tools.leveleditor.views.TriggerView;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;
import net.drewke.tdme.utils.Console;

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
public final class TDMELevelEditor implements GLEventListener, WindowListener {

	private final static String VERSION = "0.9.9";

	private static TDMELevelEditor instance;

	private Engine engine;

	private GLWindow glWindow;
	private FPSAnimator animator;

	private View view;

	private boolean quitRequested = false;

	private LevelEditorLevel level;
	private LevelEditorEntityLibraryScreenController levelEditorEntityLibraryScreenController;
	private PopUps popUps;

	private LevelEditorView levelEditorView;
	private ModelViewerView modelViewerView;
	private TriggerView triggerView;
	private EmptyView emptyView;
	private ParticleSystemView particleSystemView;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String modelFileName = null;

		//
		Console.println("TDMELevelEditor " + VERSION);
		Console.println("Programmed 2014,...,2017 by Andreas Drewke, drewke.net.");
		Console.println();

		// no nifty logging
		Logger.getLogger("").setLevel(Level.SEVERE);

		// create GL caps
		GLProfile glp = Engine.getProfile();
		GLCapabilities caps = new GLCapabilities(glp);
		Console.println(glp);
		Console.println(caps);

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
		LevelPropertyPresets.getInstance().setDefaultLevelProperties(level);
		engine = Engine.getInstance();
		view = null;
		popUps = new PopUps();
	}

	/**
	 * @return level editor instance
	 */
	public static TDMELevelEditor getInstance() {
		return instance;
	}

	/**
	 * @return level editor entity library screen controller
	 */
	public LevelEditorEntityLibraryScreenController getLevelEditorEntityLibraryScreenController() {
		return levelEditorEntityLibraryScreenController;
	}

	/**
	 * @return entity library
	 */
	public LevelEditorEntityLibrary getEntityLibrary() {
		return level.getEntityLibrary();
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
		if (this.view != null) this.view.deactivate();
		this.view = view;
		if (this.view != null) this.view.activate();
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
		//
		engine.display(drawable);

		//
		Engine.getInstance().initGUIMode();

		// render view
		if (view != null) view.display(drawable);

		//
		Engine.getInstance().doneGUIMode();

		// 
		if (quitRequested == true) {
			dispose(drawable);
			animator.stop();
			glWindow.setVisible(false);
			System.exit(0);
		}
	}

	/**
	 * Shutdown tdme viewer
	 */
	public void dispose(GLAutoDrawable drawable) {
		// deactivate current view
		if (view != null) view.deactivate();

		// dispose views
		levelEditorView.dispose();
		modelViewerView.dispose();
		triggerView.dispose();
		emptyView.dispose();
		particleSystemView.dispose();

		// dispose engines
		engine.dispose(drawable);
		Tools.oseDispose(drawable);
	}

	/**
	 * Initialize tdme level editor
	 */
	public void init(GLAutoDrawable drawable) {
		engine.initialize(drawable);

		// register gui to mouse, keyboard events
		glWindow.addMouseListener(engine.getGUI());
		glWindow.addKeyListener(engine.getGUI());

		// init off screen engine
		Tools.oseInit(drawable);

		// level editor model library screen controller
		levelEditorEntityLibraryScreenController = new LevelEditorEntityLibraryScreenController(popUps);
		levelEditorEntityLibraryScreenController.initialize();
		engine.getGUI().addScreen(levelEditorEntityLibraryScreenController.getScreenNode().getId(), levelEditorEntityLibraryScreenController.getScreenNode());

		// pop ups
		popUps.initialize();

		//
		levelEditorView = new LevelEditorView(popUps);
		levelEditorView.initialize();
		modelViewerView = new ModelViewerView(popUps);
		modelViewerView.initialize();
		triggerView = new TriggerView(popUps);
		triggerView.initialize();
		emptyView = new EmptyView(popUps);
		emptyView.initialize();
		particleSystemView = new ParticleSystemView(popUps);
		particleSystemView.initialize();

		// show up level editor view
		setView(levelEditorView);
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
		setView(levelEditorView);
	}

	/**
	 * Switch to model viewer
	 */
	public void switchToModelViewer() {
		setView(modelViewerView);
	}

	/**
	 * Switch to trigger view
	 */
	public void switchToTriggerView() {
		setView(triggerView);
	}

	/**
	 * Switch to empty view
	 */
	public void switchToEmptyView() {
		setView(emptyView);
	}

	/**
	 * Switch to particle system view
	 */
	public void switchToParticleSystemView() {
		setView(particleSystemView);
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
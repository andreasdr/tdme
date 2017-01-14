package net.drewke.tdme.tools.leveleditor;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorLevel;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.leveleditor.model.LevelPropertyPresets;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.leveleditor.views.ModelLibraryView;
import net.drewke.tdme.tools.leveleditor.views.View;

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

	private static TDMELevelEditor instance;

	private Engine engine;

	private GLWindow glWindow;
	private FPSAnimator animator;

	private Nifty nifty;
	private JoglInputSystem niftyInputSystem;
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
		System.out.println("TDMELevelEditor 0.9.5a");
		System.out.println("Programmed 2014 by Andreas Drewke, drewke.net.");
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
		glWindow.setTitle("TDMELevelEditor 0.9.5a");

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
		level = new LevelEditorLevel(LevelPropertyPresets.getInstance().getMapPropertiesPreset());
		engine = Engine.getInstance();
		nifty = null;
		niftyInputSystem = null;
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
	 * @return Nifty instance
	 */
	public Nifty getNifty(GLAutoDrawable drawable) {
		// create nifty if not yet created
		if (nifty == null) {
			// create nifty instance
			niftyInputSystem = new JoglInputSystem(glWindow);
			glWindow.addMouseListener(niftyInputSystem);
			glWindow.addKeyListener(niftyInputSystem);
			System.out.println("Nifty::delegated renderer::GL2 = " + drawable.getGL().isGL2() + ", GLES2 = " + drawable.getGL().isGLES2() + ", GL3 = " + drawable.getGL().isGL3());
			nifty = new Nifty(
				drawable.getGL().isGL3() || drawable.getGL().isGLES2()?
					new BatchRenderDevice(JoglBatchRenderBackendCoreProfileFactory.create(glWindow)):
					new BatchRenderDevice(JoglBatchRenderBackendFactory.create(glWindow)),
				new OpenALSoundDevice(),
				niftyInputSystem,
				new AccurateTimeProvider()
			);
			nifty.fromXml("resources/tools/leveleditor/gui/globals.xml", null);
		}

		//
		return nifty;
	}

	/**
	 * @return nifty input system or null if not yet initialized
	 */
	public JoglInputSystem getNiftyInputSystem() {
		return niftyInputSystem;
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
				glWindow.removeMouseListener(view);
				glWindow.removeKeyListener(view);
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
				Screen niftyScreen = nifty.getCurrentScreen();
				niftyScreen.getFocusHandler().setKeyFocus(niftyScreen.findElementByName(niftyScreen.getDefaultFocusElementId()));;
				glWindow.addMouseListener(view);
				glWindow.addKeyListener(view);
				viewInitialized = true;
			}
			view.display(drawable);
		}

		//
		engine.display(drawable);

		// view inputsystem
		if (view != null) {
			view.doInputSystem(drawable);
		}

		//
		Engine.getInstance().initGUIMode();

		// render GUI
		nifty.update();
		nifty.render(false);

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
			glWindow.removeMouseListener(view);
			glWindow.removeKeyListener(view);
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
		Tools.oseInit(drawable);
		setView(new LevelEditorView());
	}

	/**
	 * reshape tdme level editor
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
		if (nifty != null) nifty.resolutionChanged();
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
		setView(new ModelLibraryView());
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
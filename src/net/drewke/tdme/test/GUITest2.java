package net.drewke.tdme.test;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.gui.GUIParser;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * GUI test 2
 * @author andreas.drewke
 * @version $Id$
 */
public final class GUITest2 implements GLEventListener, KeyListener {

	private GLCanvas glCanvas;
	private Engine engine;
	private FPSAnimator animator;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING);

		// create GL canvas
		GLProfile glp = Engine.getProfile();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(false);
		caps.setDepthBits(16);
		final GLCanvas glCanvas = new GLCanvas(caps);

		// create AWT frame
		final Frame frame = new Frame("GUITest2");
		frame.setSize(640, 480);
		frame.add(glCanvas);
		frame.setVisible(true);

		// add window listener to be able to handle window close events
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.remove(glCanvas);
				frame.dispose();
				System.exit(0);
			}
		});

		// event listener
		GUITest2 guiTest = new GUITest2(glCanvas);
		glCanvas.addGLEventListener(guiTest);
		glCanvas.addKeyListener(guiTest);
		glCanvas.setEnabled(true);
	}

	/**
	 * Public constructor
	 * 
	 * @param gl
	 *            canvas
	 */
	public GUITest2(GLCanvas glCanvas) {
		this.glCanvas = glCanvas;
		engine = Engine.getInstance();
		// animator, do the frames
		animator = new FPSAnimator(glCanvas, 60);
		animator.setUpdateFPSFrames(3, null);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		engine.display(drawable);
		engine.getGUI().render("test");
		engine.getGUI().handleEvents("test");
		engine.getGUI().discardEvents();
	}

	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(0);
		engine.init(drawable);
		try {
			engine.getGUI().addScreen("test", GUIParser.parse("resources/gui/definitions", "button-example.xml"));
			engine.getGUI().getScreen("test").setScreenSize(640, 480);
			engine.getGUI().getScreen("test").layout();
			System.out.println(engine.getGUI().getScreen("test").toString());
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		// events
		glCanvas.addMouseListener(engine.getGUI());
		glCanvas.addMouseMotionListener(engine.getGUI());
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}

}
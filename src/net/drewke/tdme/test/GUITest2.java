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
public final class GUITest2 implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

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
		glCanvas.addMouseListener(guiTest);
		glCanvas.addMouseMotionListener(guiTest);
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
		engine = Engine.getInstance();
		// animator, do the frames
		animator = new FPSAnimator(glCanvas, 60);
		animator.setUpdateFPSFrames(3, null);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		engine.display(drawable);
	}

	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(0);
		engine.init(drawable);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
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
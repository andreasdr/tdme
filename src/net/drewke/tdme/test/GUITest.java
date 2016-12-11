package net.drewke.tdme.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;

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
 * GUI Test 3
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUITest implements GLEventListener, WindowListener {

	private GLWindow glWindow;
	private Engine engine;

	/**
	 * Public constructor
	 */
	public GUITest(GLWindow glWindow) {
		this.glWindow = glWindow;
		this.engine = Engine.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.opengl.GLEventListener#init(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable) {
		// init engine
		engine.init(drawable);
		
		// register gui to mouse, keyboard events
		glWindow.addMouseListener(engine.getGUI());
		glWindow.addKeyListener(engine.getGUI());

		//
		try {
			engine.getGUI().addScreen("test", GUIParser.parse("resources/gui/definitions", "test.xml"));
			engine.getGUI().getScreen("test").setScreenSize(640, 480);
			engine.getGUI().getScreen("test").addActionListener(new GUIActionListener() {
				public void actionPerformed(GUIElementNode node) {
					System.out.println(node.getId() + ".actionPerformed()");
				}
			});
			engine.getGUI().getScreen("test").layout();
			System.out.println(engine.getGUI().getScreen("test").toString());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.opengl.GLEventListener#dispose(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.opengl.GLEventListener#reshape(com.jogamp.opengl.GLAutoDrawable, int, int, int, int)
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		engine.reshape(drawable, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.opengl.GLEventListener#display(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable) {
		engine.display(drawable);
		engine.getGUI().render("test");
		engine.getGUI().handleEvents("test");
		engine.getGUI().discardEvents();
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING);

		// gl profile
		GLProfile glp = Engine.getProfile();

		// create GL caps
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setBackgroundOpaque(true);
		caps.setDepthBits(16);
		caps.setDoubleBuffered(true);
		System.out.println(glp);
		System.out.println(caps);

		// create GL window
		GLWindow glWindow = GLWindow.create(caps);
		glWindow.setTitle("GUI Test");
		glWindow.setSize(800, 600);

		// animator
		FPSAnimator animator = new FPSAnimator(glWindow, 60);
		animator.setUpdateFPSFrames(3, null);

		// create test
		GUITest guiTest = new GUITest(glWindow);
		glWindow.addGLEventListener(guiTest);
		glWindow.addWindowListener(guiTest);
		glWindow.setVisible(true);

		// start animator
		animator.start();
	}

}

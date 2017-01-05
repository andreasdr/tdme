package net.drewke.tdme.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.elements.GUITabController;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.MutableString;

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
 * GUI Test
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
				public void onActionPerformed(GUIActionListener.Type type, GUIElementNode node) {
					// check if button 1 is pressed
					if (type == Type.PERFORMED && node.getName().equals("button")) {
						// action performed
						System.out.println(node.getId() + ".actionPerformed()");
	
						// test get values
						HashMap<String,MutableString> values = new HashMap<String,MutableString>();
						node.getScreenNode().getValues(values);
						System.out.println(values);
	
						// test set values
						values.clear();
						values.put("select", new MutableString("8"));
						values.put("input", new MutableString("Enter some more text here!"));
						values.put("checkbox1", new MutableString("1"));
						values.put("checkbox2", new MutableString("1"));
						values.put("checkbox3", new MutableString("1"));
						values.put("dropdown", new MutableString("11"));
						values.put("radio", new MutableString("3"));
						values.put("selectmultiple", new MutableString("|1|2|3|15|16|17|"));
						node.getScreenNode().setValues(values);
	
						// test GUI tab controller select tab method
						((GUITabController)node.getScreenNode().getNodeById("tab1").getController()).selectTab();
					} else
					// check if button 2 is pressed
					if (type == Type.PERFORMED && node.getName().equals("button2")) {
						try {
							{
								GUIParentNode parentNode = (GUIParentNode)(node.getScreenNode().getNodeById("sadd_inner"));
								parentNode.clearSubNodes();
								GUIParser.parse(
									parentNode, 
									"<dropdown-option text=\"Option 1\" value=\"1\" />" +
									"<dropdown-option text=\"Option 2\" value=\"2\" />" +
									"<dropdown-option text=\"Option 3\" value=\"3\" />" +
									"<dropdown-option text=\"Option 4\" value=\"4\" />" +
									"<dropdown-option text=\"Option 5\" value=\"5\" />" +
									"<dropdown-option text=\"Option 6\" value=\"6\" />" +
									"<dropdown-option text=\"Option 7\" value=\"7\" />" +
									"<dropdown-option text=\"Option 8\" value=\"8\" selected=\"true\" />" +
									"<dropdown-option text=\"Option 9\" value=\"9\" />" +
									"<dropdown-option text=\"Option 10\" value=\"10\" />"
								);
								parentNode.getScreenNode().layout();
							}

							{
								//
								GUIParentNode parentNode = (GUIParentNode)(node.getScreenNode().getNodeById("sasb_inner"));
								parentNode.clearSubNodes();
								GUIParser.parse(
									parentNode, 
									"<selectbox-option text=\"Option 1\" value=\"1\" />" +
									"<selectbox-option text=\"Option 2\" value=\"2\" />" +
									"<selectbox-option text=\"Option 3\" value=\"3\" />" +
									"<selectbox-option text=\"Option 4\" value=\"4\" selected=\"true\" />" +
									"<selectbox-option text=\"Option 5\" value=\"5\" />" +
									"<selectbox-option text=\"Option 6\" value=\"6\" />" +
									"<selectbox-option text=\"Option 7\" value=\"7\" />" +
									"<selectbox-option text=\"Option 8\" value=\"8\" />" +
									"<selectbox-option text=\"Option 9\" value=\"9\" />" +
									"<selectbox-option text=\"Option 10\" value=\"10\" />"
								);
								parentNode.getScreenNode().layout();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						// test GUI tab controller select tab method
						((GUITabController)node.getScreenNode().getNodeById("tab2").getController()).selectTab();
					}
				}
			});
			engine.getGUI().getScreen("test").addChangeListener(new GUIChangeListener() {
				public void onValueChanged(GUIElementNode node) {
					System.out.println(node.getName() + ":onValueChanged: " + node.getController().getValue());
				}
			});
			engine.getGUI().getScreen("test").layout();
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

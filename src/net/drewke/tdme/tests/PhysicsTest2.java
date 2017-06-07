package net.drewke.tdme.tests;

import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.physics.RigidBody;
import net.drewke.tdme.engine.physics.World;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.math.Vector3;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
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
 * Engine with physics test 2
 * @author andreas.drewke
 * @version $Id$
 */
public final class PhysicsTest2 implements GLEventListener, MouseListener, MouseMotionListener, KeyListener, WindowListener {

	private final static int RIGID_TYPEID_STANDARD = 1;

	private final static int BOX_COUNT = 4;

	private Engine engine;
	private World world;

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING);

		// create GL canvas
		GLProfile glp = Engine.getProfile();
		GLCapabilities caps = new GLCapabilities(glp);

		// create GL window
		GLWindow glWindow = GLWindow.create(caps);
		glWindow.setTitle("PhysicsTest2");

		// animator
		FPSAnimator animator = new FPSAnimator(glWindow, 60);

		// tdme level editor
		PhysicsTest2 physicsTest2 = new PhysicsTest2();
		
		// GL Window
		glWindow.addGLEventListener(physicsTest2);
		glWindow.setSize(800, 600);
		glWindow.setVisible(true);
		glWindow.addKeyListener(physicsTest2);
		glWindow.addMouseListener(physicsTest2);
		glWindow.addWindowListener(physicsTest2);
		
		// start animator
		animator.setUpdateFPSFrames(3, null);
		animator.start();
	}

	/**
	 * Public constructor
	 */
	public PhysicsTest2() {
		engine = Engine.getInstance();
		world = new World();
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.opengl.GLEventListener#display(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable) {
		float fps = 60f; // animator.getLastFPS();
		long start = System.currentTimeMillis();
		world.update(1f/fps);
		world.synch(engine);
		long end = System.currentTimeMillis();
		engine.display(drawable);
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
	 * @see com.jogamp.opengl.GLEventListener#init(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(0);
		engine.initialize(drawable);
		Object3D entity;

		// cam
		Camera cam = engine.getCamera();
		// Test scenario serg
		cam.setZNear(0.1f);
		cam.setZFar(100.00f);
		cam.getLookFrom().set(0f, 30f, 30f);
		cam.getLookAt().set(0f, 0f, 0f);

		// lights
		Light light0 = engine.getLightAt(0);
		light0.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
		light0.getDiffuse().set(0.5f,0.5f,0.5f,1f);
		light0.getSpecular().set(1f,1f,1f,1f);
		light0.getPosition().set(
			0f,
			20000f,
			0f,
			1f
		);
		light0.getSpotDirection().set(0f,0f,0f).sub(new Vector3(light0.getPosition().getArray()));
		light0.setConstantAttenuation(0.5f);
		light0.setLinearAttenuation(0f);
		light0.setQuadraticAttenuation(0f);
		light0.setSpotExponent(0f);
		light0.setSpotCutOff(180f);
		light0.setEnabled(true);

		// ground
		OrientedBoundingBox ground = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(30f,1f,30f)
		);
		Model groundModel = PrimitiveModel.createModel(ground, "ground_model");
		groundModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f, 0.8f, 0.8f, 1f);
		groundModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,1f,1f,1f);
		entity = new Object3D("ground", groundModel);
		entity.getTranslation().setY(-1f);
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("ground", true, RIGID_TYPEID_STANDARD, entity, ground, 0.5f);

		OrientedBoundingBox box = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(1f,1f,1f)
		);
		Model boxModel = PrimitiveModel.createModel(box, "box_model");
		boxModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f,0.5f,0.5f,1f);
		boxModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,0f,0f,1f);

		// boxes
		for (int i = 0; i < BOX_COUNT; i++) { 
			entity = new Object3D("box" + i, boxModel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(i * 2f + 1f);
			//entity.getTranslation().addX(i * 50f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("box" + i, true, RIGID_TYPEID_STANDARD, entity, box, 0f, 0.8f, 100f, RigidBody.computeInertiaMatrix(box, 100f, 1f, 1f, 1f));
		}
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
	 * @see com.jogamp.newt.event.MouseListener#mouseClicked(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseEntered(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseExited(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mousePressed(com.jogamp.newt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseReleased(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseDragged(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyPressed(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.KeyListener#keyReleased(com.jogamp.newt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
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

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseDragged(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseDragged(java.awt.event.MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseMoved(java.awt.event.MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jogamp.newt.event.MouseListener#mouseWheelMoved(com.jogamp.newt.event.MouseEvent)
	 */
	public void mouseWheelMoved(MouseEvent arg0) {
	}

}

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

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Engine with physics test 2
 * @author andreas.drewke
 * @version $Id$
 */
public final class PhysicsTest2 implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

	private final static int RIGID_TYPEID_STANDARD = 1;

	private final static int BOX_COUNT = 4;

	private Engine engine;
	private FPSAnimator animator;

	private boolean keyLeft;
	private boolean keyRight;
	private boolean keyUp;
	private boolean keyDown;

	private boolean keyW;
	private boolean keyA;
	private boolean keyS;
	private boolean keyD;

	private World world;
	private Vector3 worldPosForce;
	private Vector3 directionMagnitude;

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
		final Frame frame = new Frame("PhysicsTest");
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
		PhysicsTest2 physicsTest = new PhysicsTest2(glCanvas);
		glCanvas.addGLEventListener(physicsTest);
		glCanvas.addMouseListener(physicsTest);
		glCanvas.addMouseMotionListener(physicsTest);
		glCanvas.addKeyListener(physicsTest);
		glCanvas.setEnabled(true);
	}

	/**
	 * Public constructor
	 * 
	 * @param gl
	 *            canvas
	 */
	public PhysicsTest2(GLCanvas glCanvas) {
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyDown = false;
		engine = Engine.getInstance();
		world = new World();
		worldPosForce = new Vector3();
		directionMagnitude = new Vector3();
		// animator, do the frames
		animator = new FPSAnimator(glCanvas, 60);
		animator.setUpdateFPSFrames(3, null);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		/*
		RigidBody capsuleBig1 = world.getRigidBodyDirect("capsulebig1");
		if (keyLeft) capsuleBig1.getVelocity().setX(6f); else
		if (keyRight) capsuleBig1.getVelocity().setX(-6f); else 
			capsuleBig1.getVelocity().setX(0f);
		if (keyUp) capsuleBig1.getVelocity().setZ(6f); else
		if (keyDown) capsuleBig1.getVelocity().setZ(-6f); else
			capsuleBig1.getVelocity().setZ(0f);

		RigidBodyDirect capsuleBig2 = world.getRigidBodyDirect("capsulebig2");
		if (keyA) capsuleBig2.getVelocity().setX(6f); else
		if (keyD) capsuleBig2.getVelocity().setX(-6f); else 
			capsuleBig2.getVelocity().setX(0f);
		if (keyW) capsuleBig2.getVelocity().setZ(6f); else
		if (keyS) capsuleBig2.getVelocity().setZ(-6f); else
			capsuleBig2.getVelocity().setZ(0f);
		*/

		float fps = 60f; // animator.getLastFPS();
		long start = System.currentTimeMillis();
		world.update(1f/fps);
		world.synch(engine);
		long end = System.currentTimeMillis();
		engine.display(drawable);
	}

	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(0);
		engine.init(drawable);
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
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) keyLeft = true;
		if (keyCode == KeyEvent.VK_RIGHT) keyRight = true;
		if (keyCode == KeyEvent.VK_UP) keyUp = true;
		if (keyCode == KeyEvent.VK_DOWN) keyDown = true;
		if (keyCode == KeyEvent.VK_A) keyA = true;
		if (keyCode == KeyEvent.VK_D) keyD = true;
		if (keyCode == KeyEvent.VK_W) keyW = true;
		if (keyCode == KeyEvent.VK_S) keyS = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) keyLeft = false;
		if (keyCode == KeyEvent.VK_RIGHT) keyRight = false;
		if (keyCode == KeyEvent.VK_UP) keyUp = false;
		if (keyCode == KeyEvent.VK_DOWN) keyDown = false;
		if (keyCode == KeyEvent.VK_A) keyA = false;
		if (keyCode == KeyEvent.VK_D) keyD = false;
		if (keyCode == KeyEvent.VK_W) keyW = false;
		if (keyCode == KeyEvent.VK_S) keyS = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}

}
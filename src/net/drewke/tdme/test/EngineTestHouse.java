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
import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Rotations;
import net.drewke.tdme.engine.fileio.models.DAEParser;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.math.Vector3;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Engine test
 * @author andreas.drewke
 * @version $Id$
 */
public final class EngineTestHouse implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

	private Engine engine;

	private int mousePressedX = -1;
	private Rotation xRotation;

	private boolean keyMinus;
	private boolean keyPlus;
	private boolean keyW;
	private boolean keyA;
	private boolean keyS;
	private boolean keyD;

	private boolean cameraChanged;

	private Object3D player;
	private BoundingVolume playerCapsule;
	private BoundingVolume playerCapsuleTransformed;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING);

		// create GL canvas
		GLProfile glp = Engine.getProfile();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(false);
		final GLCanvas glCanvas = new GLCanvas(caps);

		// create AWT frame
		final Frame frame = new Frame("Engine Test House");
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
		EngineTestHouse renderTest = new EngineTestHouse(glCanvas);
		glCanvas.addGLEventListener(renderTest);
		glCanvas.addMouseListener(renderTest);
		glCanvas.addMouseMotionListener(renderTest);
		glCanvas.addKeyListener(renderTest);
		glCanvas.setEnabled(true);

		// animator, do the frames
		FPSAnimator animator = new FPSAnimator(glCanvas, 60);
		animator.start();
	}

	/**
	 * Public constructor
	 * 
	 * @param gl
	 *            canvas
	 */
	public EngineTestHouse(GLCanvas glCanvas) {
		keyW = false;
		keyA = false;
		keyS = false;
		keyD = false;
		xRotation = new Rotation(0f, new Vector3(0f,1f,0f));
	}

	private void doPlayerControl(boolean keyLeft, boolean keyRight, boolean keyUp) {
		float fps = engine.getTiming().getCurrentFPS();

		Rotations rotations = player.getRotations();
		Rotation r = rotations.get(0);

		// left, right
		if (keyRight) r.setAngle(r.getAngle() + (135f / fps));
		if (keyLeft) r.setAngle(r.getAngle() - (135f / fps));
		if (keyRight || keyLeft) player.update();

		// forward
		if (keyUp) {
			// apply movement
			Vector3 movement = new Vector3();
			r.getQuaternion().multiply(new Vector3(0f,0f,1f), movement).scale((10f / fps));
			player.getTranslation().add(
				movement
			);
			player.update();
			playerCapsuleTransformed.fromBoundingVolumeWithTransformations(playerCapsule, player);

			// walking animation
			if (player.getAnimation().equals("walk") == false) {
				player.setAnimation("walk");
			}
		} else {
			// still animation
			if (player.getAnimation().equals("walk") == true) {
				player.setAnimation("still");
			}			
		}
	}

	private void setupCamera() {
		/*
		Engine engine = Engine.getInstance();
		float fps = engine.getTiming().getCurrentFPS();
		Camera cam = engine.getCamera();
		cam.getLookAt().set(player.getTranslation());
		Vector3 lookFrom =
			cam.getLookAt().clone().sub(
				player.getRotations().get(0).getMatrix().multiply(
					new Vector3(0f,-2f,10f)
				)
			);
		float lookFromHeight = envCollisionInformation.computeHeight(lookFrom, -100000f);
		if (lookFromHeight > lookFrom.getY()) lookFrom.setY(lookFromHeight + 2f);
		cam.getLookFrom().set(lookFrom);
		*/
	}

	public void display(GLAutoDrawable drawable) {
		setupCamera();
		doPlayerControl(keyA, keyD, keyW);
		engine.display(drawable);
	}

	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	public void init(GLAutoDrawable drawable) {
		//
		engine = Engine.getInstance();

		// init engine
		engine.init(drawable);

		// camera
		Camera cam = engine.getCamera();
		cam.setZNear(0.1f);
		cam.setZFar(100f);
		cam.getLookFrom().set(-20.0f, 20.0f, 40.00f);
		cam.getLookAt().set(-20.0f, 0f, 0f);
		cam.computeUpVector(cam.getLookFrom(), cam.getLookAt(), cam.getUpVector());

		// light
		Light light0 = engine.getLightAt(0);
		light0.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
		light0.getPosition().set(-0f, 40.0f, -0f, 1.0f);
		light0.setEnabled(true);

		//
		Model _environment = null;
		try {
			System.out.println("Loading environment ...");
			_environment = DAEParser.parse("resources/environment/indoor_outdoor_house", "indoor_outdoor_house.dae");
			System.out.println(ModelUtilities.createBoundingBox(_environment));
			// System.out.println(_environment);

			System.out.println("Loading player");
			Model _player = DAEParser.parse("resources/models/dummy", "testDummy_textured.DAE");
			_player.addAnimationSetup("still", 3, 3, true);
			_player.addAnimationSetup("walk", 0, 18, true);

			// player capsule
			playerCapsule = Capsule.createBoundingVolume(new Vector3(0,15,0), new Vector3(0,160,0), 25);

			// add player 1
			//	player
			player = new Object3D("player1", _player);
			player.getTranslation().set(-10f, 0f, 20f);
			player.setAnimation("still");
			player.getRotations().add(new Rotation(0f, new Vector3(0f, 1f, 0f)));
			player.getScale().set(0.01f, 0.01f, 0.01f);
			player.update();
			player.setDynamicShadowingEnabled(true);
			engine.addEntity(player);

			//	capsule transformed
			playerCapsuleTransformed = playerCapsule.clone();
			playerCapsuleTransformed.fromBoundingVolumeWithTransformations(playerCapsule, player);

			System.out.println("Adding environment ...");
			engine.addEntity(
				new Object3D(
					"environment",
					_environment
				)
			);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.out.println("Could not load object: " + exception.getMessage());
			System.exit(0);
		}
		cameraChanged = true;
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
		mousePressedX = e.getX();
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
		float xRotationAngle = xRotation.getAngle();
		xRotationAngle+= (e.getX() - mousePressedX) / 10f;
		xRotationAngle%= 360;
		if (xRotationAngle < 0f) xRotationAngle+= 360f;
		xRotation.setAngle(xRotationAngle);
		xRotation.update();
		cameraChanged = true;
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
		char keyChar = Character.toLowerCase(e.getKeyChar());
		if (keyChar == '-') keyMinus = true;
		if (keyChar == '+') keyPlus = true;
		if (keyChar == 'w') keyW = true;
		if (keyChar == 'a') keyA = true;
		if (keyChar == 's') keyS = true;
		if (keyChar == 'd') keyD = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		char keyChar = Character.toLowerCase(e.getKeyChar());
		if (keyChar == '-') keyMinus = false;
		if (keyChar == '+') keyPlus = false;
		if (keyChar == 'w') keyW = false;
		if (keyChar == 'a') keyA = false;
		if (keyChar == 's') keyS = false;
		if (keyChar == 'd') keyD = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}

}
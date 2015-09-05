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
import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.fileio.models.DAEParser;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.physics.RigidBody;
import net.drewke.tdme.engine.physics.World;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.engine.primitives.Sphere;
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
public final class PhysicsTest implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

	private final static int RIGID_TYPEID_STANDARD = 1;

	private final static int BOX_COUNT = 5;
	private final static int BOXSTACK_COUNT = 2;
	private final static int CAPSULE_COUNT = 10;
	private final static int SPHERE_COUNT = 10;
	
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
		PhysicsTest physicsTest = new PhysicsTest(glCanvas);
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
	public PhysicsTest(GLCanvas glCanvas) {
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyDown = false;
		engine = Engine.getInstance();
		world = new World();
		// animator, do the frames
		animator = new FPSAnimator(glCanvas, 60);
		animator.setUpdateFPSFrames(3, null);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		// apply some manual damping on boxes on x and z axis
		for (int i = 0; i < BOX_COUNT; i++) {
			RigidBody body = world.getRigidBody("box" + i);
			body.getLinearVelocity().setX(body.getLinearVelocity().getX() * (1f - 1f/10f));
			body.getLinearVelocity().setZ(body.getLinearVelocity().getZ() * (1f - 1f/10f));
		}
		for (int i = 0; i < BOXSTACK_COUNT; i++) {
			RigidBody body = world.getRigidBody("box" + (BOX_COUNT + i));
			body.getLinearVelocity().setX(body.getLinearVelocity().getX() * (1f - 1f/10f));
			body.getLinearVelocity().setZ(body.getLinearVelocity().getZ() * (1f - 1f/10f));
		}

		RigidBody capsuleBig1 = world.getRigidBody("capsulebig1");
		if (keyLeft) capsuleBig1.getLinearVelocity().setX(8f); else
		if (keyRight) capsuleBig1.getLinearVelocity().setX(-8f); else 
			capsuleBig1.getLinearVelocity().setX(0f);
		if (keyUp) capsuleBig1.getLinearVelocity().setZ(8f); else
		if (keyDown) capsuleBig1.getLinearVelocity().setZ(-8f); else
			capsuleBig1.getLinearVelocity().setZ(0f);

		RigidBody capsuleBig2 = world.getRigidBody("capsulebig2");
		if (keyA) capsuleBig2.getLinearVelocity().setX(6f); else
		if (keyD) capsuleBig2.getLinearVelocity().setX(-6f); else 
			capsuleBig2.getLinearVelocity().setX(0f);
		if (keyW) capsuleBig2.getLinearVelocity().setZ(6f); else
		if (keyS) capsuleBig2.getLinearVelocity().setZ(-6f); else
			capsuleBig2.getLinearVelocity().setZ(0f);

		long start = System.currentTimeMillis();
		float fps = 60f; // animator.getLastFPS();
		world.update(1f/fps);
		world.synch(engine);
		engine.display(drawable);
		long end = System.currentTimeMillis();
		System.out.println("PhysicsTest::display::" + (end-start) + "ms");
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
		cam.setZNear(0.10f);
		cam.setZFar(50.00f);
		cam.getLookFrom().set(0f, 4f * 2.5f, -6f * 2.5f);
		cam.getLookAt().set(0f, 0.0f, 0f);		
		cam.computeUpVector(cam.getLookFrom(), cam.getLookAt(), cam.getUpVector());

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
			new Vector3(8f,1f,8f)
		);
		Model groundModel = PrimitiveModel.createModel(ground, "ground_model");
		groundModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f, 0.8f, 0.8f, 1f);
		groundModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,1f,1f,1f);
		entity = new Object3D("ground", groundModel);
		// entity.getRotations().add(new Rotation(10f, OrientedBoundingBox.AABB_AXIS_Z.clone()));
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("ground", true, RIGID_TYPEID_STANDARD, entity, ground, 0.5f);

		// side
		OrientedBoundingBox side = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(1f,16f,8f)
		);
		Model sideModel = PrimitiveModel.createModel(side, "side_model");
		sideModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f,0.8f,0.8f,1f);
		sideModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,1f,1f,1f);

		// far
		OrientedBoundingBox nearFar = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(8f,16f,1f)
		);
		Model nearFarModel = PrimitiveModel.createModel(nearFar, "far_model");
		nearFarModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f,0.8f,0.8f,1f);
		nearFarModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,1f,1f,1f);

		// far
		entity = new Object3D("far", nearFarModel);
		entity.getTranslation().addZ(+9f);
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("far", true, RIGID_TYPEID_STANDARD, entity, nearFar, 0.5f);

		// near
		entity = new Object3D("near", nearFarModel);
		entity.getTranslation().addZ(-9f);
		entity.getEffectColorMul().set(1f,1f,1f,0f);
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("near", true, RIGID_TYPEID_STANDARD, entity, nearFar, 0.5f);

		// side left
		entity = new Object3D("sideright", sideModel);
		entity.getTranslation().addX(-9f);
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("sideright", true, RIGID_TYPEID_STANDARD, entity, side, 0.5f);

		// side right
		entity = new Object3D("sideleft", sideModel);
		entity.getTranslation().addX(9f);
		entity.update();
		engine.addEntity(entity);
		world.addStaticRigidBody("sideleft", true, RIGID_TYPEID_STANDARD, entity, side, 0.5f);

		// box
		OrientedBoundingBox box = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(0.6f,0.6f,0.6f)
		);
		Model boxModel = PrimitiveModel.createModel(box, "box_model");
		boxModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f,0.5f,0.5f,1f);
		boxModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,0f,0f,1f);

		// boxes
		for (int i = 0; i < BOX_COUNT; i++) {
			entity = new Object3D("box" + i, boxModel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(10f + i * 3.0f);
			entity.getTranslation().addX(-2f + i * 0.1f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("box" + i, true, RIGID_TYPEID_STANDARD, entity, box, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(box, 100f, 1f, 1f, 1f));
		}

		// stack
		for (int i = 0; i < BOXSTACK_COUNT; i++) {
			entity = new Object3D("box" + (BOX_COUNT + i), boxModel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(1.6f + (i * 1.2f));
			entity.getTranslation().addX(+3f);
			entity.getTranslation().addZ(-5f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("box" + (BOX_COUNT + i), true, RIGID_TYPEID_STANDARD, entity, box, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(box, 100f, 1f, 1f, 1f));
		}

		// sphere
		Sphere sphere = new Sphere(
			new Vector3(0f,0f,0f),
			0.4f
		);
		Model sphereModel = PrimitiveModel.createModel(sphere, "sphere_model");
		sphereModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.5f,0.8f,0.8f,1f);
		sphereModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(0f,1f,1f,1f);

		// spheres
		for (int i = 0; i < SPHERE_COUNT; i++) {
			entity = new Object3D("sphere" + i, sphereModel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(12f + (i * 1f));
			entity.getTranslation().addX(0.45f * i - 3f);
			entity.getTranslation().addZ(0.1f * i - 3f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("sphere" + i, true, RIGID_TYPEID_STANDARD, entity, sphere, 0.75f, 0.4f, 10f, RigidBody.computeInertiaMatrix(sphere, 10f, 1f, 1f, 1f));
		}

		// sphere
		Capsule capsule = new Capsule(
			new Vector3(0f,0.5f,0f),
			new Vector3(0f,-0.5f,0f),
			0.25f
		);
		Model capsuleModel = PrimitiveModel.createModel(capsule, "capsule_model");
		capsuleModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(0.8f,0.0f,0.8f,1f);
		capsuleModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,0f,1f,1f);

		//
		for (int i = 0; i < CAPSULE_COUNT; i++) {
			entity = new Object3D("capsule" + i, capsuleModel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(14f + (i * 2f));
			entity.getTranslation().addX((i * 0.5f));
			// entity.getPivot().set(capsule.getCenter());
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("capsule" + i, true, RIGID_TYPEID_STANDARD, entity, capsule, 0.0f, 0.4f, 3f, RigidBody.computeInertiaMatrix(capsule, 3f, 1f, 1f, 1f));
		}
		// sphere

		//Capsule capsuleBig = new Capsule(
		//	new Vector3(0f,+1f,0f),
		//	new Vector3(0f,-1f,0f),
		//	0.5f
		//);

		OrientedBoundingBox capsuleBig = new OrientedBoundingBox(
			new Vector3(0f,0f,0f),
			OrientedBoundingBox.AABB_AXIS_X.clone(),
			OrientedBoundingBox.AABB_AXIS_Y.clone(),
			OrientedBoundingBox.AABB_AXIS_Z.clone(),
			new Vector3(0.5f,1f,0.5f)
		);
		
		Model capsuleBigModel = PrimitiveModel.createModel(capsuleBig, "capsulebig_model");
		capsuleBigModel.getMaterials().get("tdme.primitive.material").getAmbientColor().set(1f,0.8f,0.8f,1f);
		capsuleBigModel.getMaterials().get("tdme.primitive.material").getDiffuseColor().set(1f,0f,0f,1f);
		System.out.println(capsuleBig.getCenter());

		//
		entity = new Object3D("capsulebig1", capsuleBigModel);
		entity.setDynamicShadowingEnabled(true);
		entity.getTranslation().addY(5f);
		entity.getTranslation().addX(-2f);
		entity.update();
		engine.addEntity(entity);
		world.addRigidBody("capsulebig1", true, RIGID_TYPEID_STANDARD, entity, capsuleBig, 0f, 1f, 80f, RigidBody.getNoRotationInertiaMatrix());

		//
		entity = new Object3D("capsulebig2", capsuleBigModel);
		entity.setDynamicShadowingEnabled(true);
		entity.getTranslation().addY(5f);
		entity.getTranslation().addX(+2f);
		entity.update();
		engine.addEntity(entity);
		world.addRigidBody("capsulebig2", true, RIGID_TYPEID_STANDARD, entity, capsuleBig, 0f, 1f, 100f, RigidBody.getNoRotationInertiaMatrix());

		try {
			// load barrel, set up bounding volume
			Model _barrel = DAEParser.parse("resources/models/barrel", "barrel.dae");
			// _barrel.getImportTransformationsMatrix().scale(2f);
			ConvexMesh barrelBoundingVolume = new ConvexMesh(new Object3DModel(_barrel));

			// set up barrel 1 in 3d engine
			entity = new Object3D("barrel1", _barrel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(+4f);
			entity.getScale().set(2f,2f,2f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("barrel1", true, RIGID_TYPEID_STANDARD, entity, barrelBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(barrelBoundingVolume, 100f, 1f, 1f, 1f));

			// set up barrel 2 in 3d engine
			entity = new Object3D("barrel2", _barrel);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(+6f);
			entity.getScale().set(2f,2f,2f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("barrel2", true, RIGID_TYPEID_STANDARD, entity, barrelBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(barrelBoundingVolume, 100f, 1f, 1f, 1f));

			// load cone, set up bounding volume
			Model _cone = DAEParser.parse("resources/models/cone", "cone.dae");
			// _barrel.getImportTransformationsMatrix().scale(2f);
			ConvexMesh coneBoundingVolume = new ConvexMesh(new Object3DModel(_cone));

			// set up cone 1 in 3d engine
			entity = new Object3D("cone1", _cone);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(-4f);
			entity.getScale().set(3f,3f,3f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("cone1", true, RIGID_TYPEID_STANDARD, entity, coneBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(coneBoundingVolume, 100f, 1f, 1f, 1f));

			// set up cone 1 in 3d engine
			entity = new Object3D("cone2", _cone);
			entity.setDynamicShadowingEnabled(true);
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(-5f);
			entity.getScale().set(3f,3f,3f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("cone2", true, RIGID_TYPEID_STANDARD, entity, coneBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(coneBoundingVolume, 100f, 1f, 1f, 1f));

			// load cone, set up bounding volume
			Model _tire = DAEParser.parse("resources/models/tire", "tire.dae");
			// _barrel.getImportTransformationsMatrix().scale(2f);
			ConvexMesh tireBoundingVolume = new ConvexMesh(new Object3DModel(_tire));

			// set up tire 1 in 3d engine
			entity = new Object3D("tire1", _tire);
			entity.setDynamicShadowingEnabled(true);
			entity.getRotations().add(new Rotation(90f, new Vector3(1f,0f,0f)));
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(-4f);
			entity.getTranslation().addZ(-2f);
			entity.getScale().set(2f,2f,2f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("tire1", true, RIGID_TYPEID_STANDARD, entity, tireBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(tireBoundingVolume, 100f, 1f, 1f, 1f));

			// set up tire 1 in 3d engine
			entity = new Object3D("tire2", _tire);
			entity.setDynamicShadowingEnabled(true);
			entity.getRotations().add(new Rotation(90f, new Vector3(1f,0f,0f)));
			entity.getTranslation().addY(5f);
			entity.getTranslation().addX(-6f);
			entity.getTranslation().addZ(-2f);
			entity.getScale().set(2f,2f,2f);
			entity.update();
			engine.addEntity(entity);
			world.addRigidBody("tire2", true, RIGID_TYPEID_STANDARD, entity, tireBoundingVolume, 0f, 1f, 100f, RigidBody.computeInertiaMatrix(tireBoundingVolume, 100f, 1f, 1f, 1f));
		} catch (Exception e) {
			e.printStackTrace();
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
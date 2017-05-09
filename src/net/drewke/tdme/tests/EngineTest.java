package net.drewke.tdme.tests;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.PointsParticleSystemEntity;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Rotations;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Model.UpVector;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.subsystems.particlesystem.BoundingBoxParticleEmitter;
import net.drewke.tdme.engine.subsystems.particlesystem.CircleParticleEmitter;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity;
import net.drewke.tdme.engine.subsystems.particlesystem.SphereParticleEmitter;
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
public final class EngineTest implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

	private Engine engine;
	private Engine osEngine;

	private CollisionResponse collision;
	private ArrayList<Object3D> players;
	private ArrayList<Object3D> playersBoundingVolumeModel;
	private BoundingVolume playerBoundingVolume;
	private Model playerBoundingVolumeModel;
	private ArrayList<BoundingVolume> playerBoundingVolumesTransformed;

	private Object3D cube;
	private Model cubeBoundingVolumeModel;
	private BoundingVolume cubeBoundingVolume;
	private BoundingVolume cubeBoundingVolumeTransformed;
	private BoundingVolume barrelBoundingVolume;
	private BoundingVolume barrelBoundingVolumeTransformed;

	private Transformations circleTransformations;

	Entity entityClicked;
	private int[] mouseClicked;

	private boolean keyLeft;
	private boolean keyRight;
	private boolean keyUp;

	private boolean keyW;
	private boolean keyA;
	private boolean keyS;
	private boolean keyD;

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
		final Frame frame = new Frame("Enginetest");
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
		EngineTest renderTest = new EngineTest(glCanvas);
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
	public EngineTest(GLCanvas glCanvas) {
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyW = false;
		keyA = false;
		keyS = false;
		keyD = false;
		mouseClicked = null;
		entityClicked = null;
		collision = new CollisionResponse();
		engine = Engine.getInstance();
	}

	private Model createWallModel() {
		// wall model
		Model wall = new Model("wall", "wall", UpVector.Y_UP, RotationOrder.XYZ, null);

		// wall material
		Material wallMaterial = new Material("wall");
		wall.getMaterials().put("wall", wallMaterial);


		//	group
		Group wallGroup = new Group(wall, null, "wall", "wall");

		//	faces entity
		//		far plane
		FacesEntity groupFacesEntityFarPlane = new FacesEntity(wallGroup, "wall");
		wallMaterial.getAmbientColor().set(1f,1f,1f,1f);;
		groupFacesEntityFarPlane.setMaterial(wallMaterial);

		//	faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntityFarPlane);

		//	vertices
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();

		// left, near, far plane
		vertices.add(new Vector3(-4f, 0f, +4f));
		// left, far, far plane
		vertices.add(new Vector3(-4f, +4f, +4f));
		// right far, far plane
		vertices.add(new Vector3(+4f, +4f, +4f));
		// right, near, far plane
		vertices.add(new Vector3(+4f, 0f, +4f));

		//	normals
		ArrayList<Vector3> normals = new ArrayList<Vector3>();
		//		ground
		normals.add(new Vector3(0f, 1f, 0f));
		//		far plane
		normals.add(new Vector3(0f, 0f, 1f));

		// texture coordinates
		ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();
		textureCoordinates.add(new TextureCoordinate(0f, 0f));
		textureCoordinates.add(new TextureCoordinate(0f, 1f));
		textureCoordinates.add(new TextureCoordinate(1f, 1f));
		textureCoordinates.add(new TextureCoordinate(1f, 0f));

		//	faces ground far plane
		ArrayList<Face> facesFarPlane = new ArrayList<Face>();
		facesFarPlane.add(new Face(wallGroup,0,1,2,1,1,1,0,1,2));
		facesFarPlane.add(new Face(wallGroup,2,3,0,1,1,1,2,3,0));

		// set up faces entity
		groupFacesEntityFarPlane.setFaces(facesFarPlane);

		// setup ground group
		wallGroup.setVertices(vertices);
		wallGroup.setNormals(normals);
		wallGroup.setTextureCoordinates(textureCoordinates);
		wallGroup.setFacesEntities(groupFacesEntities);

		// register group
		wall.getGroups().put("wall", wallGroup);
		wall.getSubGroups().put("wall", wallGroup);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(wall);

		//
		return wall;
	}

	public void display(GLAutoDrawable drawable) {
		circleTransformations.getTranslation().setX(players.get(0).getTranslation().getX());
		circleTransformations.getTranslation().setZ(players.get(0).getTranslation().getZ());
		circleTransformations.getTranslation().addY(0.1f);
		if (circleTransformations.getTranslation().getY() > 1.5f) {
			circleTransformations.getTranslation().setY(0f);
		}
		circleTransformations.update();
		((ParticleSystemEntity)engine.getEntity("circle")).getParticleEmitter().fromTransformations(circleTransformations);
		// particle system test
		((ParticleSystemEntity)engine.getEntity("circle")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("circle")).updateParticles();
		((ParticleSystemEntity)engine.getEntity("snow")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("snow")).updateParticles();
		((ParticleSystemEntity)engine.getEntity("firebase")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("firebase")).updateParticles();
		((ParticleSystemEntity)engine.getEntity("firetop")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("firetop")).updateParticles();
		((ParticleSystemEntity)engine.getEntity("firesmoke")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("firesmoke")).updateParticles();
		((ParticleSystemEntity)engine.getEntity("water")).emitParticles();
		((ParticleSystemEntity)engine.getEntity("water")).updateParticles();

		// player control
		doPlayerControl(0, keyLeft, keyRight, keyUp);
		doPlayerControl(1, keyA, keyD, keyW);

		// update bounding boxes
		for (int i = 0; i < players.size(); i++) {
			playersBoundingVolumeModel.get(i).fromTransformations(players.get(i));
		}

		// render
		osEngine.display(drawable);
		engine.display(drawable);
		// osEngine.makeScreenshot(".", "ostest.png");

		// handle mouse clicked
		if (mouseClicked != null) {
			if (entityClicked != null) {
				entityClicked.getEffectColorMul().setRed(1.0f);
				entityClicked.getEffectColorMul().setGreen(1.0f);
				entityClicked.getEffectColorMul().setBlue(1.0f);
			}
			Entity _object3DClicked = engine.getObjectByMousePosition(mouseClicked[0], mouseClicked[1]);
			if (_object3DClicked != null) {
				_object3DClicked.getEffectColorMul().setRed(2.0f);
				_object3DClicked.getEffectColorMul().setGreen(2.0f);
				_object3DClicked.getEffectColorMul().setBlue(2.0f);
			}
			entityClicked = _object3DClicked;
			mouseClicked = null;
		}
	}

	private void doPlayerControl(int idx, boolean keyLeft, boolean keyRight, boolean keyUp) {
		float fps = engine.getTiming().getCurrentFPS();

		Object3D player = players.get(idx);
		BoundingVolume playerBoundingVolumeTransformed = playerBoundingVolumesTransformed.get(idx);

		Rotations rotations = player.getRotations();
		Rotation r = rotations.get(0);
		player.update();

		Vector3 movement = new Vector3();

		// left, right
		if (keyRight) r.setAngle(r.getAngle() - (135f / fps));
		if (keyLeft) r.setAngle(r.getAngle() + (135f / fps));
		if (keyRight || keyLeft) {
			player.update();
			playerBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player);
		}

		// forward
		if (keyUp) {
			// apply movement
			r.getQuaternion().multiply(new Vector3(0f,0f,1f), movement);
			movement.scale(1.50f / fps);
			player.getTranslation().add(
				movement
			);
			player.update();
			playerBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player);

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

		// check if collides with cube
		if (playerBoundingVolumeTransformed.doesCollideWith(
				cubeBoundingVolumeTransformed,
				movement,
				collision
			) == true &&
			collision.hasPenetration() == true) {
			System.out.println("cube: " + collision);
			// yep, move object out of collision 
			player.getTranslation().sub(
				collision.getNormal().clone().scale(collision.getPenetration())
			);
			player.update();
			playerBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player);
		}

		// check if collides with barrel
		if (CollisionDetection.getInstance().doCollide(
				(Capsule)playerBoundingVolumeTransformed,
				(ConvexMesh)barrelBoundingVolumeTransformed,
				movement,
				collision
			) == true &&
			collision.hasPenetration() == true) {
			System.out.println("barrel: " + collision);
			// yep, move object out of collision 
			player.getTranslation().sub(
				collision.getNormal().clone().scale(collision.getPenetration())
			);
			player.update();
			playerBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player);
		}

		// check if collides with other players
		for (int i = 0; i < players.size(); i++) {
			// do not check with same player
			if (idx == i) continue;

			// do collide?
			if (playerBoundingVolumeTransformed.doesCollideWith(
					playerBoundingVolumesTransformed.get(i),
					movement, 
					collision
				) == true && collision.hasPenetration()) {
				System.out.println("player: " + collision);

				// yep, move object out of collision
				player.getTranslation().sub(
					collision.getNormal().clone().scale(collision.getPenetration())
				);
				player.update();
				playerBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player);
			}
		}
	}

	public void dispose(GLAutoDrawable drawable) {
		engine.dispose(drawable);
	}

	public void init(GLAutoDrawable drawable) {
		engine.init(drawable);
		if (osEngine == null) {
			osEngine = Engine.createOffScreenInstance(drawable, 512, 512);
			//
			Light osLight0 = osEngine.getLightAt(0);
			osLight0.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
			osLight0.getDiffuse().set(1.0f, 1.0f, 1.0f, 1.0f);
			osLight0.getPosition().set(0.0f, -4f, -4f, 1.0f);
			osLight0.getSpotDirection().set(new Vector3(osLight0.getPosition().getArray())).sub(new Vector3(0f,0f,0f));
			osLight0.setEnabled(true);

			// cam
			Camera osCam = osEngine.getCamera();
			osCam.setZNear(0.10f);
			osCam.setZFar(50.00f);
			osCam.getLookFrom().set(0f, 4f, -4f);
			osCam.getLookAt().set(0f, 0.50f, 0f);
			osCam.computeUpVector(osCam.getLookFrom(), osCam.getLookAt(), osCam.getUpVector());

			// scene color
			osEngine.getSceneColor().set(1f, 1f,1f, 1.0f);
		}

		// cam
		Camera cam = engine.getCamera();
		cam.setZNear(0.10f);
		cam.setZFar(50.00f);
		cam.getLookFrom().set(0f, 4f, -6f);
		cam.getLookAt().set(0f, 0.50f, 0f);
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
		Light light1 = engine.getLightAt(1);
		light1.getDiffuse().set(1.0f, 0.0f, 0.0f, 1.0f);
		light1.getPosition().set(-4.0f, 5.0f, -5.0f, 1.0f);
		light1.getSpotDirection().set(0f,0f,0f).sub(new Vector3(light1.getPosition().getArray()));
		light1.setEnabled(true);
		Light light2 = engine.getLightAt(2);
		light2.getDiffuse().set(0.0f, 1.0f, 0.0f, 1.0f);
		light2.getPosition().set(+4.0f, 5.0f, 0.0f, 1.0f);
		light2.getSpotDirection().set(0f,0f,0f).sub(new Vector3(light2.getPosition().getArray()));
		light2.setEnabled(true);

		// scene
		players = new ArrayList<Object3D>();
		playersBoundingVolumeModel = new ArrayList<Object3D>();
		playerBoundingVolumesTransformed = new ArrayList<BoundingVolume>();
		try {
			Model _barrel = DAEReader.read("resources/tests/models/barrel", "barrel.dae");
			Object3D barrel = new Object3D("barrel", _barrel);
			barrelBoundingVolume = new ConvexMesh(new Object3DModel(_barrel));
			barrel.getTranslation().set(1.5f,0.35f,-2f);
			barrel.setDynamicShadowingEnabled(true);
			barrel.update();
			barrelBoundingVolumeTransformed = barrelBoundingVolume.clone();
			barrelBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(barrelBoundingVolume, barrel);
			engine.addEntity(barrel);

			// wall
			Model _farPlane = createWallModel();
			Object3D farPlane = new Object3D("wall", _farPlane);
			farPlane.bindDiffuseTexture("wall", "wall", osEngine.getFrameBuffer());
			engine.addEntity(farPlane);

			//
			Model _grass = DAEReader.read("resources/tests/models/grass", "grass.dae");
			Object3D grass = new Object3D("ground", _grass);
			grass.getScale().set(8f,1f,8f);
			grass.update();
			engine.addEntity(grass);

			// players
			Model _player = DAEReader.read("resources/tests/models/dummy", "testDummy_textured.DAE");
			_player.addAnimationSetup("still", 3, 3, true);
			_player.addAnimationSetup("walk", 0, 18, true);

			// player bounding volume
			// playerBoundingVolume = Sphere.createBoundingVolume(new Vector3(0,90f/130f,0), 90f/130f);
			playerBoundingVolume = Capsule.createBoundingVolume(new Vector3(0,30f/130f,0), new Vector3(0,230f/130f,0), 25/130f);
			// playerBoundingVolume = BoundingBox.createBoundingVolume(new Vector3(-25f/130f, 0, -25f/130f), new Vector3(+25f/130f, 180f/130f, +25f/130f));

			/*
			playerBoundingVolume = OrientedBoundingBox.createBoundingVolume(
				new Vector3(0f, 90f/130f, 0f),
				new Vector3(1f, 0f, 0f),
				new Vector3(0f, 1f, 0f),
				new Vector3(0f, 0f, 1f),
				new Vector3(25f/130f, 90f/130f, 25f/130f)
			);
			*/
			
			playerBoundingVolumeModel = PrimitiveModel.createModel(playerBoundingVolume, "player_bv");

			// add player 1
			//	player
			Object3D player1 = new Object3D("player1", _player);
			player1.getTranslation().add(new Vector3(-1.50f,0f,0f));
			player1.setAnimation("still");
			player1.getRotations().add(new Rotation(0f, new Vector3(0f, 1f, 0f)));
			player1.update();
			player1.setPickable(true);
			player1.setDynamicShadowingEnabled(true);
			engine.addEntity(player1);
			//	bounding volume transformed
			BoundingVolume player1BoundingVolumeTransformed = playerBoundingVolume.clone();
			player1BoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player1);
			playerBoundingVolumesTransformed.add(player1BoundingVolumeTransformed);
			//	add to engine
			players.add(player1);

			//	bounding volume
			Object3D player1BoundingVolume = new Object3D("player1_bv", playerBoundingVolumeModel);
			player1BoundingVolume.fromTransformations(player1);
			player1BoundingVolume.setEnabled(true);
			playersBoundingVolumeModel.add(player1BoundingVolume);
			engine.addEntity(player1BoundingVolume);

			// add player 2
			//	player
			Object3D player2 = new Object3D("player2", _player);
			player2.getTranslation().add(new Vector3(1.50f,0f,0f));
			player2.setAnimation("still");
			player2.getRotations().add(new Rotation(0f, new Vector3(0f, 1f, 0f)));
			player2.update();
			player2.setPickable(true);
			player2.setDynamicShadowingEnabled(true);
			players.add(player2);
			//	bounding volume transformed
			BoundingVolume player2BoundingVolumeTransformed = playerBoundingVolume.clone();
			player2BoundingVolumeTransformed.fromBoundingVolumeWithTransformations(playerBoundingVolume, player2);
			playerBoundingVolumesTransformed.add(player2BoundingVolumeTransformed);
			//	add to engine
			engine.addEntity(player2);

			//	bounding volume
			Object3D player2BoundingVolume = new Object3D("player2_bv", playerBoundingVolumeModel);
			player2BoundingVolume.fromTransformations(player2);
			player2BoundingVolume.setEnabled(true);
			playersBoundingVolumeModel.add(player2BoundingVolume);
			engine.addEntity(player2BoundingVolume);

			// add cube
			Model _cube = DAEReader.read("resources/tests/models/test", "cube.dae");
			cube = new Object3D("cube", _cube);
			cube.getTranslation().add(new Vector3(0f,0f,0f));
			cube.getScale().set(2f,2f,2f);
			cube.update();
			cube.setPickable(true);
			cube.setDynamicShadowingEnabled(true);
			cube.setEnabled(true);
			cubeBoundingVolume = cube.getBoundingBox();
			cubeBoundingVolumeTransformed = cubeBoundingVolume.clone();
			cubeBoundingVolumeTransformed.fromBoundingVolumeWithTransformations(cubeBoundingVolume, cube);
			engine.addEntity(cube);

			//
			cubeBoundingVolumeModel = PrimitiveModel.createModel(cubeBoundingVolume, "cube_bv");

			//	bounding volume
			Object3D cubeBoundingVolumeObject3D = new Object3D("cube_bv", cubeBoundingVolumeModel);
			cubeBoundingVolumeObject3D.fromTransformations(cube);
			cubeBoundingVolumeObject3D.setEnabled(true);
			engine.addEntity(cubeBoundingVolumeObject3D);

			// wall
			Model _wall = DAEReader.read("resources/tests/models/wall", "wall.dae");
			Object3D wall0 = new Object3D("wall0", _wall);
			wall0.getTranslation().add(new Vector3(-1.00f,0f,3.00f));
			wall0.update();
			wall0.setPickable(true);
			wall0.setEnabled(true);
			engine.addEntity(wall0);
			Object3D wall1 = new Object3D("wall1", _wall);
			wall1.getTranslation().add(new Vector3(0f,0f,3.00f));
			wall1.update();
			wall1.setPickable(true);
			wall1.setEnabled(true);
			engine.addEntity(wall1);

			// os engine test
			Object3D osCube = new Object3D("cube", _cube);
			osCube.getTranslation().add(new Vector3(0f,0f,0f));
			osCube.getScale().set(2f,2f,2f);
			osCube.update();
			osEngine.addEntity(osCube);

			//
			circleTransformations = new Transformations();
			engine.addEntity(
				new PointsParticleSystemEntity(
					"circle",
					false,
					new CircleParticleEmitter(
						3000,
						50, 
						50,
						new Vector3(1f,0f,0f),
						new Vector3(0f,0f,1f),
						new Vector3(0f, 0f, 0f),
						0.4f,
						0f,
						0f,
						new Vector3(0f, 0.2f, 0f),
						new Vector3(0f, 0.2f, 0f),
						new Color4(1f, 1f, 1f, 0.3f),
						new Color4(1f, 1f, 1f, 0.3f)
					),
					1000,
					false
				)
			);
			engine.addEntity(
				new PointsParticleSystemEntity(
					"water",
					true,
					new SphereParticleEmitter(
						4000,
						1000, 
						0,
						0.1f,
						0.0f,
						new Sphere(new Vector3(-1f,1f,0f), 0.05f),
						new Vector3(-4f,0f,1f),
						new Vector3(-1f, 0f, 0f),
						new Color4(0.8f,0.8f,1f,0.25f),
						new Color4(0.8f,0.8f,1f,0.25f)
					),
					4000,
					false
				)
			);
			engine.addEntity(
				new PointsParticleSystemEntity(
					"snow",
					true,
					new BoundingBoxParticleEmitter(
						100,
						15000,
						1000,
						0,
						0,
						new OrientedBoundingBox(
							new Vector3(0f,5f,0f),
							new Vector3(1f,0f,0f),
							new Vector3(0f,1f,0f),
							new Vector3(0f,0f,1f),
							new Vector3(4f,0f,4f)
						),
						new Vector3(0f,-0.5f,0f),
						new Vector3(0f,-0.1f,0f),
						new Color4(0.8f,0.8f,1f,0.5f),
						new Color4(0.8f,0.8f,1f,0.5f)							
					),
					1024,
					false
				)
			);
			engine.addEntity(
				new PointsParticleSystemEntity(
					"firebase",
					false,
					new SphereParticleEmitter(
						2048,
						1024, 
						2048,
						0,
						0,
						new Sphere(
							new Vector3(2.5f, 0.2f, 0f),
							0.2f
						),
						new Vector3(0f, 0.1f, 0f),
						new Vector3(0f, 0.1f, 0f),
						new Color4(0.0f, 0f, 0f, 0.5f),
						new Color4(0.4f, 0f, 0f, 0.5f)
					),
					2048,
					false
				)
			);
			//
			engine.addEntity(
				new PointsParticleSystemEntity(
					"firetop",
					false,
					new SphereParticleEmitter(
						2048,
						1024, 
						2048,
						0,
						0,
						new Sphere(
							new Vector3(2.5f, 0.7f, 0f),
							0.1f
						),
						new Vector3(0f, 0.06f, 0f),
						new Vector3(0f, 0.12f, 0f),
						new Color4(0.75f, 0.0f, 0f, 0.5f),
						new Color4(1f, 1f, 0f, 0.5f)
					),
					2048,
					false
				)
			);
			//
			engine.addEntity(
				new PointsParticleSystemEntity(
					"firesmoke",
					false,
					new SphereParticleEmitter(
						2048,
						1024, 
						2048,
						0,
						0,
						new Sphere(
							new Vector3(2.5f, 0.7f, 0f),
							0.1f
						),
						new Vector3(0f, 0.2f, 0f),
						new Vector3(0f, 0.4f, 0f),
						new Color4(0.8f, 0.8f, 0.8f, 0.1f),
						new Color4(0.8f, 0.8f, 0.8f, 0.1f)
					),
					2048,
					false
				)
			);
			((ParticleSystemEntity)engine.getEntity("circle")).setPickable(false);
			((ParticleSystemEntity)engine.getEntity("snow")).setPickable(false);
			((ParticleSystemEntity)engine.getEntity("firebase")).setPickable(true);
			((ParticleSystemEntity)engine.getEntity("firetop")).setPickable(true);
			((ParticleSystemEntity)engine.getEntity("firesmoke")).setPickable(true);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.out.println("Could not load object: " + exception.getMessage());
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
		mouseClicked = new int[]{e.getX(), e.getY()};
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
		char keyChar = Character.toLowerCase(e.getKeyChar());
		if (keyChar == 'w') keyW = true;
		if (keyChar == 'a') keyA = true;
		if (keyChar == 's') keyS = true;
		if (keyChar == 'd') keyD = true;
		if (keyCode == KeyEvent.VK_LEFT) keyLeft = true;
		if (keyCode == KeyEvent.VK_RIGHT) keyRight = true;
		if (keyCode == KeyEvent.VK_UP) keyUp = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		char keyChar = Character.toLowerCase(e.getKeyChar());
		if (keyChar == 'w') keyW = false;
		if (keyChar == 'a') keyA = false;
		if (keyChar == 's') keyS = false;
		if (keyChar == 'd') keyD = false;
		if (keyCode == KeyEvent.VK_LEFT) keyLeft = false;
		if (keyCode == KeyEvent.VK_RIGHT) keyRight = false;
		if (keyCode == KeyEvent.VK_UP) keyUp = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		char keyChar = Character.toLowerCase(e.getKeyChar());
	}

}
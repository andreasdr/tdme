package net.drewke.tdme.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import net.drewke.tdme.engine.fileio.textures.PNG;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.subsystems.lighting.LightingShader;
import net.drewke.tdme.engine.subsystems.manager.MeshManager;
import net.drewke.tdme.engine.subsystems.manager.TextureManager;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;
import net.drewke.tdme.engine.subsystems.object.Object3DVBORenderer;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticlesShader;
import net.drewke.tdme.engine.subsystems.renderer.GL2Renderer;
import net.drewke.tdme.engine.subsystems.renderer.GL3Renderer;
import net.drewke.tdme.engine.subsystems.renderer.GLES2Renderer;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.engine.subsystems.shadowmapping.ShadowMapping;
import net.drewke.tdme.engine.subsystems.shadowmapping.ShadowMappingShaderPre;
import net.drewke.tdme.engine.subsystems.shadowmapping.ShadowMappingShaderRender;
import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.renderer.GUIRenderer;
import net.drewke.tdme.gui.renderer.GUIShader;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector2;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.DebugGLES2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLES2;
import com.jogamp.opengl.GLProfile;

/**
 * Engine main class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Engine {

	protected static Engine instance = null;
	protected static GLRenderer renderer;

	private static TextureManager textureManager = null;
	private static VBOManager vboManager = null;
	private static MeshManager meshManager = null;
	private static GUIRenderer guiRenderer = null;

	public enum AnimationProcessingTarget {GPU, CPU, CPU_NORENDERING};
	public static AnimationProcessingTarget animationProcessingTarget = AnimationProcessingTarget.GPU;

	private static ShadowMappingShaderPre shadowMappingShaderPre = null;
	private static ShadowMappingShaderRender shadowMappingShaderRender = null;
	protected static LightingShader lightingShader = null;
	protected static ParticlesShader particlesShader = null;
	protected static GUIShader guiShader = null;

	private int width;
	private int height;

	private GUI gui;
	private Timing timing;
	private Camera camera;
	protected Partition partition;
	private Light[] lights;

	private Color4 sceneColor;

	private FrameBuffer frameBuffer;
	private ShadowMapping shadowMapping;

	private HashMap<String,Entity> entitiesById;

	protected ArrayList<Object3D> objects;
	private ArrayList<Object3D> visibleObjects;

	private ArrayList<ObjectParticleSystemEntity> visibleOpses;

	protected ArrayList<PointsParticleSystemEntity> ppses;
	private ArrayList<PointsParticleSystemEntity> visiblePpses;

	protected Object3DVBORenderer object3DVBORenderer;

	protected HashMap<String,ParticleSystemEntity> particleSystemEntitiesById;

	private boolean shadowMappingEnabled;

	private boolean renderingInitiated;
	private boolean renderingComputedTransformations;

	private Matrix4x4 modelViewMatrix;
	private Matrix4x4 projectionMatrix;
	private Matrix4x4 tmpMatrix4x4;
	private Vector3 tmpVector3a;
	private Vector3 tmpVector3b;
	private Vector4 tmpVector4a;
	private Vector4 tmpVector4b;

	protected boolean initialized;

	/**
	 * Returns engine instance
	 * @return
	 */
	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}

	/**
	 * Creates an offscreen rendering instance
	 * 	Note:
	 * 		- the root engine must have been initialized before
	 * 		- the created offscreen engine must not be initialized
	 *  	
	 * @return off screen engine
	 */
	public static Engine createOffScreenInstance(GLAutoDrawable drawable, int width, int height) {
		if (instance == null || instance.initialized == false) {
			Console.println("Engine::createOffScreenInstance(): Engine not created or not initialized.");
			return null;
		}
		// create off screen engine
		Engine offScreenEngine = new Engine();
		offScreenEngine.initialized = true;
		// create GUI
		offScreenEngine.gui = new GUI(offScreenEngine, guiRenderer);
		// create object 3d vbo renderer
		offScreenEngine.object3DVBORenderer = new Object3DVBORenderer(offScreenEngine, renderer);
		offScreenEngine.object3DVBORenderer.init();
		offScreenEngine.frameBuffer = new FrameBuffer(
			offScreenEngine,
			width, height,
			FrameBuffer.FRAMEBUFFER_DEPTHBUFFER | FrameBuffer.FRAMEBUFFER_COLORBUFFER
		);
		offScreenEngine.frameBuffer.init();
		// create camera, frustum partition
		offScreenEngine.camera = new Camera(renderer);
		offScreenEngine.partition = new PartitionQuadTree();
		// create lights
		for (int i = 0; i < offScreenEngine.lights.length; i++) offScreenEngine.lights[i] = new Light(renderer, i);
		// create shadow mapping
		if (instance.shadowMappingEnabled == true) {
			offScreenEngine.shadowMapping = new ShadowMapping(offScreenEngine, renderer, offScreenEngine.object3DVBORenderer);			
		}
		offScreenEngine.reshape(drawable, 0, 0, width, height);
		return offScreenEngine;
	}

	/**
	 * @return supported GL profile
	 */
	public static GLProfile getProfile() {
		GLProfile glp = null;
		if (GLProfile.isAvailable(GLProfile.GL3)) {
			Console.println("TDME::Proposing GL3");
			glp = GLProfile.get(GLProfile.GL3);
		} else
		if (GLProfile.isAvailable(GLProfile.GL2)) {
			Console.println("TDME::Proposing GL2");
			glp = GLProfile.get(GLProfile.GL2);
		} else
		if (GLProfile.isAvailable(GLProfile.GLES2)) {
			Console.println("TDME::Proposing GLES2");
			glp = GLProfile.get(GLProfile.GLES2);
		} else {
			Console.println("TDME::No suiting OpenGL profile available!");
			return null;
		}
		Console.println("TDME::Proposing " + glp + ", GL2 = " + glp.isGL2() + ", GLES2 = " + glp.isGLES2() + ", GL3 = " + glp.isGL3());
		return glp;
	}

	/**
	 * Updates the renderer with given drawable
	 * @param drawable
	 */
	private void updateRenderer(GLAutoDrawable drawable) {
		if (drawable.getGL().isGL3()) {
			GL3 gl = (GL3)drawable.getGL().getGL3();
			// notify gl context to renderer
			renderer.setGL(gl);
		} else
		if (drawable.getGL().isGL2()) {
			GL2 gl = (GL2)drawable.getGL().getGL2();
			// notify gl context to renderer
			renderer.setGL(gl);
		} else
		if (drawable.getGL().isGLES2()) {
			GLES2 gl = (GLES2)drawable.getGL().getGLES2();
			// notify gl context to renderer
			renderer.setGL(gl);
		} else {
			Console.println("Engine::updateRenderer(): unsupported GL!");
		}
	}

	/**
	 * Default constructor
	 */
	private Engine() {
		width = 0;
		height = 0;
		timing = new Timing();
		camera = null;
		lights = new Light[8];
		sceneColor = new Color4(0.0f, 0.0f, 0.0f, 1.0f);
		frameBuffer = null;
		entitiesById = new HashMap<String,Entity>();
		objects = new ArrayList<Object3D>();
		visibleObjects = new ArrayList<Object3D>();
		visibleOpses = new ArrayList<ObjectParticleSystemEntity>();
		ppses = new ArrayList<PointsParticleSystemEntity>();
		visiblePpses = new ArrayList<PointsParticleSystemEntity>();
		particleSystemEntitiesById = new HashMap<String, ParticleSystemEntity>();

		// shadow mapping
		shadowMappingEnabled = false;
		shadowMapping = null;

		// render process state
		renderingInitiated = false;
		renderingComputedTransformations = false;

		// matrices
		modelViewMatrix = new Matrix4x4();
		projectionMatrix = new Matrix4x4();

		// tmp 3d entities
		tmpMatrix4x4 = new Matrix4x4();
		tmpVector3a = new Vector3();
		tmpVector3b = new Vector3();
		tmpVector4a = new Vector4();
		tmpVector4b = new Vector4();

		//
		initialized = false;
	}

	/**
	 * @return if initialized and ready to be used
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return shadow mapping or null if disabled
	 */
	public ShadowMapping getShadowMapping() {
		return shadowMapping;
	}

	/**
	 * @return GUI
	 */
	public GUI getGUI() {
		return gui;
	}

	/**
	 * @return Timing
	 */
	public Timing getTiming() {
		return timing;
	}

	/**
	 * @return Camera
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * @return partition
	 */
	public Partition getPartition() {
		return partition;
	}

	/**
	 * Set partition
	 * @param partition
	 */
	public void setPartition(Partition partition) {
		this.partition = partition;
	}

	/**
	 * @return lights
	 */
	public Light[] getLights() {
		return lights;
	}

	/**
	 * @return frame buffer or null
	 */
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	/**
	 * Returns light at idx (0 <= idx < 8)
	 * @param idx
	 * @return Light
	 */
	public Light getLightAt(int idx) {
		assert(idx >= 0 && idx < 8);
		return lights[idx];
	}

	/**
	 * @return texture manager
	 */
	public TextureManager getTextureManager() {
		return textureManager;
	}

	/**
	 * @return vertex buffer object manager
	 */
	public VBOManager getVBOManager() {
		return vboManager;
	}

	/**
	 * @return mesh manager
	 */
	public MeshManager getMeshManager() {
		return meshManager;
	}

	/**
	 * @return shadow mapping shader
	 */
	public static ShadowMappingShaderPre getShadowMappingShaderPre() {
		return shadowMappingShaderPre;
	}

	/**
	 * @return shadow mapping shader
	 */
	public static ShadowMappingShaderRender getShadowMappingShaderRender() {
		return shadowMappingShaderRender;
	}

	/**
	 * @return lighting shader
	 */
	public static LightingShader getLightingShader() {
		return lightingShader;
	}

	/**
	 * @return particles shader
	 */
	public static ParticlesShader getParticlesShader() {
		return particlesShader;
	}

	/**
	 * @return GUI shader
	 */
	public static GUIShader getGUIShader() {
		return guiShader;
	}

	/**
	 * @return object 3d vbo renderer
	 */
	public Object3DVBORenderer getObject3DVBORenderer() {
		return object3DVBORenderer;
	}

	/**
	 * @return scene / background color
	 */
	public Color4 getSceneColor() {
		return sceneColor;
	}

	/**
	 * @return entity count
	 */
	public int getEntityCount() {
		return entitiesById.size();
	}

	/**
	 * Returns a entity by given id
	 * @param id
	 * @return entity or null
	 */
	public Entity getEntity(String id) {
		return entitiesById.get(id);
	}

	/**
	 * Adds an entity by id
	 * @param object
	 */
	public void addEntity(Entity entity) {
		// init object
		entity.setEngine(this);
		entity.setRenderer(renderer);
		entity.init();

		// dispose old object if any did exist in engine with same id
		Entity oldEntity = entitiesById.put(entity.getId(), entity);
		// unload old object
		if (oldEntity != null) {
			oldEntity.dispose();
			if (oldEntity.isEnabled() == true) partition.removeEntity(oldEntity);
		}

		// add to partition if enabled
		if (entity.isEnabled() == true) partition.addEntity(entity);
	}

	/**
	 * Removes an entity
	 * @param id
	 */
	public void removeEntity(String id) {
		Entity entity = entitiesById.remove(id);
		if (entity != null) {
			if (entity.isEnabled() == true) partition.removeEntity(entity);
			entity.dispose();
			entity.setEngine(null);
			entity.setRenderer(null);
		}
	}

	/**
	 * Removes all entities and caches
	 */
	public void reset() {
		Iterator<String> entityKeys = entitiesById.getKeysIterator();
		ArrayList<String> entitiesToRemove = new ArrayList<String>();
		while(entityKeys.hasNext()) {
			String entityKey = entityKeys.next();
			entitiesToRemove.add(entityKey);
		}
		for (int i = 0; i< entitiesToRemove.size(); i++) {
			removeEntity(entitiesToRemove.get(i));
		}
		partition.reset();
		object3DVBORenderer.reset();
		CollisionDetection.reset();
	}

	/**
	 * Initialize render engine
	 * @param drawable
	 */
	public void init(GLAutoDrawable drawable) {
		init(drawable, false);
	}

	/**
	 * Initialize render engine
	 * @param drawable
	 * @param debug
	 */
	public void init(GLAutoDrawable drawable, boolean debug) {
		// exit if already initialized like a offscreen engine instance
		if (initialized == true) return;

		//
		GLContext glContext = drawable.getGL().getContext();
		if (drawable.getGL().isGL3()) {
			GL3 gl = (GL3)drawable.getGL().getGL3();
			if (debug == true) {
				drawable.setGL(new DebugGL3(gl));
			}
			// use gl3 renderer
			renderer = new GL3Renderer() {
				final public void onUpdateProjectionMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateCameraMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateModelViewMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onBindTexture(int textureId) {
					if (lightingShader != null) lightingShader.bindTexture(this, textureId);
					if (guiShader != null) guiShader.bindTexture(this, textureId);
				}
				final public void onUpdateTextureMatrix() {
					// no op
				}
				final public void onUpdateEffect() {
					if (lightingShader != null) lightingShader.updateEffect(this);
					if (particlesShader != null) particlesShader.updateEffect(this);
					if (guiShader != null) guiShader.updateEffect(this);
				}
				final public void onUpdateLight(int lightId) {
					if (lightingShader != null) lightingShader.updateLight(this, lightId);
				}
				final public void onUpdateMaterial() {
					if (lightingShader != null) lightingShader.updateMaterial(this);
				}
			};
			// notify gl context to renderer
			renderer.setGL(gl);
			// print gl version, extensions
			Console.println("TDME::Using GL3");
			Console.println("TDME::Extensions: " + gl.glGetString(GL.GL_EXTENSIONS));
			// engine defaults
			shadowMappingEnabled = true;
			animationProcessingTarget = AnimationProcessingTarget.CPU;
			ShadowMapping.setShadowMapSize(2048, 2048);
		} else
		if (drawable.getGL().isGL2()) {
			GL2 gl = (GL2)drawable.getGL().getGL2();
			if (debug == true) {
				drawable.setGL(new DebugGL2(gl));
			}
			// use gl2 renderer
			renderer = new GL2Renderer() {
				final public void onUpdateProjectionMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateCameraMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateModelViewMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onBindTexture(int textureId) {
					if (lightingShader != null) lightingShader.bindTexture(this, textureId);
					if (guiShader != null) guiShader.bindTexture(this, textureId);
				}
				final public void onUpdateTextureMatrix() {
					// no op
				}
				final public void onUpdateEffect() {
					if (lightingShader != null) lightingShader.updateEffect(this);
					if (particlesShader != null) particlesShader.updateEffect(this);
					if (guiShader != null) guiShader.updateEffect(this);
				}
				final public void onUpdateLight(int lightId) {
					if (lightingShader != null) lightingShader.updateLight(this, lightId);
				}
				final public void onUpdateMaterial() {
					if (lightingShader != null) lightingShader.updateMaterial(this);
				}
			};
			// notify gl context to renderer
			renderer.setGL(gl);
			// print gl version, extensions
			Console.println("TDME::Using GL2");
			Console.println("TDME::Extensions: " + gl.glGetString(GL.GL_EXTENSIONS));
			// engine defaults
			shadowMappingEnabled = true;
			animationProcessingTarget = AnimationProcessingTarget.CPU;
			ShadowMapping.setShadowMapSize(2048, 2048);
		} else
		if (drawable.getGL().isGLES2()) {
			GLES2 gl = (GLES2)drawable.getGL().getGLES2();
			if (debug == true) {
				drawable.setGL(new DebugGLES2(gl));
			}
			// use gl es 2 renderer
			renderer = new GLES2Renderer() {
				final public void onUpdateProjectionMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateCameraMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onUpdateModelViewMatrix() {
					if (lightingShader != null) lightingShader.updateMatrices(this);
					if (particlesShader != null) particlesShader.updateMatrices(this);
					if (shadowMapping != null) shadowMapping.updateMVPMatrices(this);
				}
				final public void onBindTexture(int textureId) {
					if (lightingShader != null) lightingShader.bindTexture(this, textureId);
					if (guiShader != null) guiShader.bindTexture(this, textureId);
				}
				final public void onUpdateTextureMatrix() {
					// no op
				}
				final public void onUpdateEffect() {
					if (lightingShader != null) lightingShader.updateEffect(this);
					if (particlesShader != null) particlesShader.updateEffect(this);
					if (guiShader != null) guiShader.updateEffect(this);
				}
				final public void onUpdateLight(int lightId) {
					if (lightingShader != null) lightingShader.updateLight(this, lightId);
				}
				final public void onUpdateMaterial() {
					if (lightingShader != null) lightingShader.updateMaterial(this);
				}
			};
			// notify gl context to renderer
			renderer.setGL(gl);
			// print gl version, extensions
			Console.println("TDME::Using GLES2");
			Console.println("TDME::Extensions: " + gl.glGetString(GL.GL_EXTENSIONS));
			// engine defaults
			// is shadow mapping available?
			if (renderer.isBufferObjectsAvailable() == true &&
				renderer.isDepthTextureAvailable() == true) {
				// yep, nice
				shadowMappingEnabled = true;
				animationProcessingTarget = AnimationProcessingTarget.CPU;
				ShadowMapping.setShadowMapSize(512, 512);
			} else {
				// nope, renderer skinning on GPU to speed up things and do not shadow mapping
				shadowMappingEnabled = false;
				animationProcessingTarget = AnimationProcessingTarget.GPU;
			}
		} else {
			Console.println("Engine::init(): unsupported GL!");
			return;
		}

		// init
		initialized = true;
		renderer.init();
		renderer.renderingTexturingClientState = false;

		// create manager
		textureManager = new TextureManager(renderer);
		vboManager = new VBOManager(renderer);
		meshManager = new MeshManager();

		// create object 3d vbo renderer
		object3DVBORenderer = new Object3DVBORenderer(this, renderer);
		object3DVBORenderer.init();

		// create GUI
		guiRenderer = new GUIRenderer(renderer);
		guiRenderer.init();
		gui = new GUI(this, guiRenderer);
		gui.init();

		// create camera
		camera = new Camera(renderer);
		partition = new PartitionQuadTree();

		// create lights
		for (int i = 0; i < lights.length; i++) lights[i] = new Light(renderer, i);

		// create lighting shader
		lightingShader = new LightingShader(renderer);
		lightingShader.init();

		// create particles shader
		particlesShader = new ParticlesShader(this, renderer);
		particlesShader.init();

		// create GUI shader
		guiShader = new GUIShader(renderer);
		guiShader.init();

		// check if VBOs are available
		if (renderer.isBufferObjectsAvailable()) {
			Console.println("TDME::VBOs are available.");
		} else {
			Console.println("TDME::VBOs are not available! Engine will not work!");
			initialized = false;
		}

		// check FBO support
		if (glContext.hasBasicFBOSupport() == false) {
			Console.println("TDME::Basic FBOs are not available!");
			shadowMappingEnabled = false;
		} else {
			Console.println("TDME::Basic FBOs are available.");
		}

		// initialize shadow mapping
		if (shadowMappingEnabled == true) {
			Console.println("TDME::Using shadow mapping");
			shadowMappingShaderPre = new ShadowMappingShaderPre(renderer);
			shadowMappingShaderPre.init();
			shadowMappingShaderRender = new ShadowMappingShaderRender(renderer);
			shadowMappingShaderRender.init();
			shadowMapping = new ShadowMapping(this, renderer, object3DVBORenderer);
		} else {
			Console.println("TDME::Not using shadow mapping");
		}

		// print out animation processing target
		Console.println("TDME: animation processing target: " + animationProcessingTarget);

		// determine initialized from sub systems
		initialized&= shadowMappingShaderPre == null?true:shadowMappingShaderPre.isInitialized();
		initialized&= shadowMappingShaderRender == null?true:shadowMappingShaderRender.isInitialized();
		initialized&= lightingShader.isInitialized();
		initialized&= particlesShader.isInitialized();
		initialized&= guiShader.isInitialized();

		//
		Console.println("TDME::initialized & ready: " + initialized);
	}

	/**
	 * Reshape
	 * @param drawable
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// update our width and height
		this.width = width;
		this.height = height;

		// update renderer
		updateRenderer(drawable);

		// update frame buffer if we have one
		if (frameBuffer != null) {
			frameBuffer.reshape(width, height);
		}

		// update shadow mapping
		if (shadowMapping != null) {
			shadowMapping.reshape(width, height);
		}

		// update GUI system
		gui.reshape(width, height);
	}

	/**
	 * Initiates the rendering process
	 * 	updates timing, updates camera
	 * @param drawable
	 */
	private void initRendering(GLAutoDrawable drawable) {
		// update renderer
		updateRenderer(drawable);

		// update timing
		timing.updateTiming();

		// update camera
		camera.update(width, height);

		// clear lists of known objects
		objects.clear();
		ppses.clear();

		// clear lists of visible objects
		visibleObjects.clear();
		visibleOpses.clear();
		visiblePpses.clear();

		//
		renderingInitiated = true;
	}

	/**
	 * Computes visibility and transformations
	 * @param drawable
	 */
	public void computeTransformations(GLAutoDrawable drawable) {
		// init rendering if not yet done
		if (renderingInitiated == false) initRendering(drawable);

		// do particle systems auto emit
		for (Entity entity: entitiesById.getValuesIterator()) {
			// skip on disabled entities
			if (entity.isEnabled() == false) continue;

			// object particle system entity
			if (entity instanceof ParticleSystemEntity) {
				ParticleSystemEntity pse = (ParticleSystemEntity)entity;
				// do auto emit
				if (pse.isAutoEmit() == true)  {
					pse.emitParticles();
					pse.updateParticles();
				}
			}
		}

		// add visible entities to related lists 
		for(Entity entity: partition.getVisibleEntities(camera.getFrustum())) {
			// object 3d
			if (entity instanceof Object3D) {
				Object3D object = (Object3D)entity;
				// compute transformations
				object.computeTransformations();
				// add to visible objects
				visibleObjects.add(object);
			} else
			// object particle system entity
			if (entity instanceof ObjectParticleSystemEntity) {
				ObjectParticleSystemEntity opse = (ObjectParticleSystemEntity)entity;
				visibleObjects.addAll(opse.getEnabledObjects());
				visibleOpses.add(opse);
			} else
			// points particle system entity
			if (entity instanceof PointsParticleSystemEntity) {
				PointsParticleSystemEntity ppse = (PointsParticleSystemEntity)entity;
				visiblePpses.add(ppse);
			}
		}

		//
		renderingComputedTransformations = true;
	}

	/**
	 * Renders the scene
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		// do pre rendering steps
		if (renderingInitiated == false) initRendering(drawable);
		if (renderingComputedTransformations == false) computeTransformations(drawable);

		// init frame
		Engine.renderer.initFrame();

		// enable vertex and normal arrays, we always have them
		Engine.renderer.enableClientState(Engine.renderer.CLIENTSTATE_VERTEX_ARRAY);
		Engine.renderer.enableClientState(Engine.renderer.CLIENTSTATE_NORMAL_ARRAY);

		// set up camera
		camera.update(width, height);

		// render shadow maps
		if (shadowMapping != null) shadowMapping.createShadowMaps(objects);

		// switch back to framebuffer if we have one
		if (frameBuffer != null) {
			frameBuffer.enableFrameBuffer();
		} else {
			FrameBuffer.disableFrameBuffer();
		}

		// restore camera from shadow map rendering
		camera.update(width, height);

		// set up clear color
		Engine.renderer.setClearColor(
			sceneColor.getRed(),
			sceneColor.getGreen(),
			sceneColor.getBlue(),
			sceneColor.getAlpha()
		);

		// clear previous frame values
		renderer.clear(renderer.CLEAR_DEPTH_BUFFER_BIT | renderer.CLEAR_COLOR_BUFFER_BIT);

		// enable materials
		renderer.setMaterialEnabled();

		// setup up gl3 stuff
		if (lightingShader != null) {
			lightingShader.useProgram();
		}

		// update lights
		for (int j = 0; j < lights.length; j++) {
			lights[j].update();
		}

		// render objects
		object3DVBORenderer.render(visibleObjects, true, Object3DVBORenderer.DepthBufferMode.NORMAL, lightingShader);

		// setup up gl3 stuff
		if (lightingShader != null) {
			lightingShader.unUseProgram();
		}

		// render shadows if required
		if (shadowMapping != null) shadowMapping.renderShadowMaps(visibleObjects);

		// disable materials
		renderer.setMaterialDisabled();

		// use particle shader
		if (particlesShader != null) {
			particlesShader.useProgram();
		}

		// render points based particle systems 
		object3DVBORenderer.render(visiblePpses);

		// unuse particle shader
		if (particlesShader != null) {
			particlesShader.unUseProgram();
		}

		// disable vertex and normal arrays
		Engine.renderer.disableClientState(Engine.renderer.CLIENTSTATE_VERTEX_ARRAY);
		Engine.renderer.disableClientState(Engine.renderer.CLIENTSTATE_NORMAL_ARRAY);
		Engine.renderer.disableClientState(Engine.renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);

		// clear pre render states
		renderingInitiated = false;
		renderingComputedTransformations = false;
		renderer.renderingTexturingClientState = false;

		// store matrices
		modelViewMatrix.set(renderer.getModelViewMatrix());
		projectionMatrix.set(renderer.getProjectionMatrix());

		// unuse framebuffer if we have one
		if (frameBuffer != null) FrameBuffer.disableFrameBuffer();
	}

	/**
	 * Compute world coordinate from mouse position
	 * @param mouse x
	 * @param mouse y
	 * @param world coordinate
	 */
	public void computeWorldCoordinateByMousePosition(int mouseX, int mouseY, Vector3 worldCoordinate) {
		// use framebuffer if we have one
		if (frameBuffer != null) frameBuffer.enableFrameBuffer();

		// http://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords-solved
		tmpMatrix4x4.set(modelViewMatrix).multiply(projectionMatrix).invert();
		float mouseToProjectionX = (2.0f * mouseX / width) - 1.0f;
		float mouseToProjectionY = 1.0f - (2.0f * mouseY / height);
		float pixelDepth = renderer.readPixelDepth(mouseX, height - mouseY);
		tmpMatrix4x4.multiply(
			tmpVector4a.set(
				mouseToProjectionX,
				mouseToProjectionY,
				2.0f * pixelDepth - 1.0f,
				1.0f
			),
			tmpVector4b
		);
		tmpVector4b.scale(1.0f / tmpVector4b.getW());

		// unuse framebuffer if we have one
		if (frameBuffer != null) FrameBuffer.disableFrameBuffer();

		//
		worldCoordinate.set(tmpVector4b.getArray());
	}

	/**
	 * Retrieves object by mouse position
	 * @param mouse x
	 * @param mouse y
	 * @return entity or null
	 */
	public Entity getObjectByMousePosition(int mouseX, int mouseY) {
		return getObjectByMousePosition(mouseX, mouseY, null);
	}

	/**
	 * Retrieves object by mouse position
	 * @param mouse x
	 * @param mouse y
	 * @param filter
	 * @return entity or null
	 */
	public Entity getObjectByMousePosition(int mouseX, int mouseY, EntityPickingFilter filter) {
		// get world coordinate
		computeWorldCoordinateByMousePosition(mouseX, mouseY, tmpVector3a);

		float selectedEntityVolume = Float.MAX_VALUE;
		Entity selectedEntity = null;

		// iterate visible objects
		for (int i = 0; i < visibleObjects.size(); i++) {
			Object3D entity = visibleObjects.get(i);
			if (entity.isPickable() == false) continue;
			if (filter != null && filter.filterEntity(entity) == false) continue;
			if (entity.getBoundingBoxTransformed().containsPoint(tmpVector3a) == true) {
				// yep, got one, its pickable and mouse world coordinate is in bounding volume
				float entityVolume =
					tmpVector3b.set(
						entity.getBoundingBoxTransformed().getMax()
					).sub(
						entity.getBoundingBoxTransformed().getMin()
					).computeLength();
				// check if not yet selected entity or its volume smaller than previous match
				if (selectedEntity == null || entityVolume < selectedEntityVolume) {
					selectedEntity = entity;
					selectedEntityVolume = entityVolume;
				}
			}
		}

		// iterate visible object particle system entities
		for (int i = 0; i < visibleOpses.size(); i++) {
			ObjectParticleSystemEntity entity = visibleOpses.get(i);
			if (entity.isPickable() == false) continue;
			if (filter != null && filter.filterEntity(entity) == false) continue;
			if (entity.getBoundingBoxTransformed().containsPoint(tmpVector3a)) {
				// yep, got one, its pickable and mouse world coordinate is in bounding volume
				float entityVolume =
					tmpVector3b.set(
						entity.getBoundingBoxTransformed().getMax()
					).sub(
						entity.getBoundingBoxTransformed().getMin()
					).computeLength();
				// check if not yet selected entity or its volume smaller than previous match
				if (selectedEntity == null || entityVolume < selectedEntityVolume) {
					selectedEntity = entity;
					selectedEntityVolume = entityVolume;
				}
			}
		}

		// iterate visible pointparticle system entities
		for (int i = 0; i < visiblePpses.size(); i++) {
			PointsParticleSystemEntity entity = visiblePpses.get(i);
			if (entity.isPickable() == false) continue;
			if (filter != null && filter.filterEntity(entity) == false) continue;
			if (entity.getBoundingBoxTransformed().containsPoint(tmpVector3a)) {
				// yep, got one, its pickable and mouse world coordinate is in bounding volume
				float entityVolume =
					tmpVector3b.set(
						entity.getBoundingBoxTransformed().getMax()
					).sub(
						entity.getBoundingBoxTransformed().getMin()
					).computeLength();
				// check if not yet selected entity or its volume smaller than previous match
				if (selectedEntity == null || entityVolume < selectedEntityVolume) {
					selectedEntity = entity;
					selectedEntityVolume = entityVolume;
				}
			}
		}

		//
		return selectedEntity;
	}

	/**
	 * Convert screen coordinate by world coordinate
	 * @param world woordinate
	 * @param screen coordinate
	 */
	public void computeScreenCoordinateByWorldCoordinate(Vector3 worldCoordinate, Vector2 screenCoordinate) {
		// convert to normalized device coordinates
		tmpMatrix4x4.
			set(modelViewMatrix).
			multiply(
				projectionMatrix
			);
		tmpMatrix4x4.multiply(new Vector4(worldCoordinate, 1.0f), tmpVector4a);
		tmpVector4a.scale(1.0f / tmpVector4a.getW());
		float[] screenCoordinateXYZW = tmpVector4a.getArray();

		// convert to screen coordinate
		screenCoordinate.setX((screenCoordinateXYZW[0] + 1.0f) * width / 2f);
		screenCoordinate.setY(height - ((screenCoordinateXYZW[1] + 1.0f) * height / 2f));
	}

	/**
	 * Shutdown the engine
	 * @param drawable
	 */
	public void dispose(GLAutoDrawable drawable) {
		// update renderer
		updateRenderer(drawable);

		// dispose entities
		Iterator<String> entityKeys = entitiesById.getKeysIterator();
		ArrayList<String> entitiesToRemove = new ArrayList<String>();
		while(entityKeys.hasNext()) {
			String entityKey = entityKeys.next();
			entitiesToRemove.add(entityKey);
		}
		for (int i = 0; i< entitiesToRemove.size(); i++) {
			removeEntity(entitiesToRemove.get(i));
		}

		// dispose shadow mapping
		if (shadowMapping != null) {
			shadowMapping.dispose();
			shadowMapping = null;
		}

		// dispose frame buffer
		if (frameBuffer != null) {
			frameBuffer.dispose();
			frameBuffer = null;
		}

		// dispose GUI
		gui.dispose();

		// if disposing main engine
		if (this == Engine.instance) {
			guiRenderer.dispose();
		}
	}

	/**
	 * Set up GUI mode rendering
	 * @param drawable
	 */
	public void initGUIMode() {
		// use framebuffer if we have one
		if (frameBuffer != null) frameBuffer.enableFrameBuffer();

		// 
		renderer.initGuiMode();
	}

	/**
	 * Set up GUI mode rendering
	 * @param drawable
	 */
	public void doneGUIMode() {
		renderer.doneGuiMode();

		// unuse framebuffer if we have one
		if (frameBuffer != null) FrameBuffer.disableFrameBuffer();
	}

	/**
	 * Creates a PNG file from current screen
	 * @param file name
	 */
	public void makeScreenshot(String pathName, String fileName) {
		// use framebuffer if we have one
		if (frameBuffer != null) frameBuffer.enableFrameBuffer();

		// fetch pixel
		ByteBuffer pixels = renderer.readPixels(0, 0, width, height);

		// 
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pathName + File.separator + fileName);
			PNG.save(width, height, pixels, fos);
		} catch (IOException ioe) {
			Console.println("Engine::makeScreenshot(): failed: " + ioe.getMessage());
		} finally {
			if (fos != null) try { fos.close(); } catch (IOException ioe2) {}
		}

		// unuse framebuffer if we have one
		if (frameBuffer != null) FrameBuffer.disableFrameBuffer();
	}

	/**
	 * Retrieves an input stream for a tdme.jar packaged file or from filesystem 
	 * @param file name
	 * @param path name
	 * @return
	 */
	public InputStream getInputStream(String pathName, String fileName) throws IOException {
		// check file system first
		try {
			return FileSystem.getInstance().getInputStream(pathName, fileName);
		} catch (IOException ioe) {
			// no op
		}

		// check tdme jar next
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(pathName + "/" + fileName);
		if (is == null) throw new FileNotFoundException(pathName + "/" + fileName);
		return is;
	}

}

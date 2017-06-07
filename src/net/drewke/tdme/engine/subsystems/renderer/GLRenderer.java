package net.drewke.tdme.engine.subsystems.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.math.Matrix4x4;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

/**
 * OpenGL renderer interface
 * @author Andreas Drewke
 * @ersion $Id$
 */
abstract public class GLRenderer {

	/**
	 * Bean holding material data
	 */
	public class Material {
		public float[] ambient;
		public float[] diffuse;
		public float[] specular;
		public float[] emission;
		public float shininess;
	}

	/**
	 * Bean holding light data
	 */
	public class Light {
		public int enabled;
		public float[] ambient;
		public float[] diffuse;
		public float[] specular;
		public float[] position;
		public float[] spotDirection;
		public float spotExponent;
		public float spotCosCutoff;
		public float constantAttenuation;
		public float linearAttenuation;
		public float quadraticAttenuation;		
	}

	public int ID_NONE = -1;

	public int CLEAR_DEPTH_BUFFER_BIT = -1;
	public int CLEAR_COLOR_BUFFER_BIT = -1;

	public int CULLFACE_FRONT = -1;
	public int CULLFACE_BACK = -1;

	public int CLIENTSTATE_TEXTURECOORD_ARRAY = -1;
	public int CLIENTSTATE_VERTEX_ARRAY = -1;
	public int CLIENTSTATE_NORMAL_ARRAY = -1;
	public int CLIENTSTATE_COLOR_ARRAY = -1;

	public int TEXTUREUNITS_MAX = -1;

	public int SHADER_FRAGMENT_SHADER = -1;
	public int SHADER_VERTEX_SHADER = -1;

	public int DEPTHFUNCTION_LESSEQUAL = -1;
	public int DEPTHFUNCTION_EQUAL = -1;

	public int FRAMEBUFFER_DEFAULT = -1;

	public int FRONTFACE_CW = -1;
	public int FRONTFACE_CCW = -1;

	public float[] effectColorMul;
	public float[] effectColorAdd;

	public Material material;
	public Light[] lights;

	protected int viewPortX;
	protected int viewPortY;
	protected int viewPortWidth;
	protected int viewPortHeight;
	protected Matrix4x4[] textureMatrix;
	protected int activeTextureUnit;

	protected Matrix4x4 projectionMatrix;
	protected Matrix4x4 cameraMatrix;
	protected Matrix4x4 modelViewMatrix;
	protected Matrix4x4 viewportMatrix;

	public boolean renderingTexturingClientState;

	public float pointSize;

	/**
	 * public constructor
	 */
	public GLRenderer() {
		// set up effect colors
		effectColorMul = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
		effectColorAdd = new float[] {0.0f, 0.0f, 0.0f, 0.0f};

		// set up material
		material = new Material();
		material.ambient = new float[] {0.2f, 0.2f, 0.2f, 1.0f};
		material.diffuse = new float[] {0.8f, 0.8f, 0.8f, 1.0f};
		material.specular = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
		material.emission = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
		material.shininess = 0.0f;

		// set up lights
		lights = new Light[8];
		for (int i = 0; i < lights.length; i++) {
			lights[i] = new Light();
			lights[i].enabled = 0;
			lights[i].ambient = new float[] {0f,0f,0f,1f};
			lights[i].diffuse = new float[] {1f,1f,1f,1f};
			lights[i].specular = new float[] {1f,1f,1f,1f};
			lights[i].position = new float[] {0f,0f,0f,0f};
			lights[i].spotDirection = new float[] {0f,0f,-1f};
			lights[i].spotExponent = 0f;
			lights[i].spotCosCutoff = (float)Math.cos(Math.PI / 180f * 180f);
			lights[i].constantAttenuation = 1f;
			lights[i].linearAttenuation = 0f;
			lights[i].quadraticAttenuation = 0f;	
		}

		projectionMatrix = new Matrix4x4().identity();
		cameraMatrix = new Matrix4x4().identity();
		modelViewMatrix = new Matrix4x4().identity();
		viewportMatrix = new Matrix4x4().identity();

		//
		viewPortX = 0;
		viewPortY = 0;
		viewPortWidth = 0;
		viewPortHeight = 0;
		TEXTUREUNITS_MAX = 2;
		textureMatrix = new Matrix4x4[TEXTUREUNITS_MAX];
		for (int i = 0; i < textureMatrix.length; i++) {
			textureMatrix[i] = new Matrix4x4().identity();
		}
		activeTextureUnit = 0;
	}

	/**
	 * Updates gl
	 * @param gl
	 */
	abstract public void setGL(GL gl);

	/**
	 * Initialize renderer
	 */
	abstract public void initialize();

	/**
	 * Pre Frame Initialization
	 */
	abstract public void initializeFrame();

	/**
	 * @return renderer version e.g. gl2, gl3 or gles2
	 */
	abstract public String getGLVersion();

	/**
	 * Checks if buffer objects is available
	 * @return buffer objects availability
	 */
	abstract public boolean isBufferObjectsAvailable();

	/**
	 * Checks if depth texture is available
	 * @return depth texture is available
	 */
	abstract public boolean isDepthTextureAvailable();

	/**
	 * @return requires program attribute location
	 */
	abstract public boolean isUsingProgramAttributeLocation();

	/**
	 * @return if specular mapping is supported
	 */
	abstract public boolean isSpecularMappingAvailable();

	/**
	 * @return if normal mapping is supported
	 */
	abstract public boolean isNormalMappingAvailable();

	/**
	 * @return if displacement mapping is supported
	 */
	abstract public boolean isDisplacementMappingAvailable();
	
	/**
	 * @return if skinning is supported
	 */
	abstract public boolean isSkinningAvailable();

	/**
	 * @return number of texture units
	 */
	abstract public int getTextureUnits();

	/**
	 * Loads a shader into open gl stack
	 * @param gl
	 * @param type
	 * @param pathName
	 * @param fileName
	 * @return shader handle
	 */
	abstract public int loadShader(int type, String pathName, String fileName);

	/**
	 * Use shader program
	 * @param programId
	 */
	abstract public void useProgram(int programId);

	/**
	 * Creates a shader program
	 * @return int
	 */
	abstract public int createProgram();

	/**
	 * Attaches a shader to a program
	 * @param program id
	 * @param shader id
	 */
	abstract public void attachShaderToProgram(int programId, int shaderId);

	/**
	 * Links attached shaders to a program
	 * @param program id
	 * @return success
	 */
	abstract public boolean linkProgram(int programId);

	/**
	 * Returns location of given uniform variable
	 * @param program id
	 * @param uniform name
	 * @return
	 */
	abstract public int getProgramUniformLocation(int programId, String name);

	/**
	 * Set up a integer uniform value
	 * @param uniform id
	 * @param value
	 */
	abstract public void setProgramUniformInteger(int uniformId, int value);

	/**
	 * Set up a float uniform value
	 * @param uniform id
	 * @param value
	 */
	abstract public void setProgramUniformFloat(int uniformId, float value);

	/**
	 * Set up a float matrix 3x3 uniform value
	 * @param uniform id
	 * @param value
	 */
	abstract public void setProgramUniformFloatMatrix3x3(int uniformId, float[] value);

	/**
	 * Set up a float matrix 4x4 uniform value
	 * @param uniform id
	 * @param value
	 */
	abstract public void setProgramUniformFloatMatrix4x4(int uniformId, float[] value);

	/**
	 * Set up a float matrices 4x4 uniform values
	 * @param uniform id
	 * @param count
	 * @param data
	 */
	abstract public void setProgramUniformFloatMatrices4x4(int uniformId, int count, FloatBuffer data);

	/**
	 * Set up a float vec4 uniform value
	 * @param uniform id
	 * @param data
	 */
	abstract public void setProgramUniformFloatVec4(int uniformId, float[] data);

	/**
	 * Set up a float vec3 uniform value
	 * @param uniform id
	 * @param data
	 */
	abstract public void setProgramUniformFloatVec3(int uniformId, float[] data);

	/**
	 * Bind attribute to a input location
	 * @param program id
	 * @param location
	 * @param attribute name
	 */
	abstract public void setProgramAttributeLocation(int programId, int location, String name);

	/**
	 * Set up viewport parameter
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	abstract public void setViewPort(int x, int y, int width, int height);

	/**
	 * Update viewport
	 */
	abstract public void updateViewPort();

	/**
	 * @return projection matrix
	 */
	public Matrix4x4 getProjectionMatrix() {
		return projectionMatrix;
	}

	/**
	 * Update projection matrix event
	 */
	abstract public void onUpdateProjectionMatrix();

	/**
	 * @return camera matrix
	 */
	public Matrix4x4 getCameraMatrix() {
		return cameraMatrix;
	}

	/**
	 * Update camera matrix event
	 */
	abstract public void onUpdateCameraMatrix();

	/**
	 * @return model view matrix
	 */
	public Matrix4x4 getModelViewMatrix() {
		return modelViewMatrix;
	}

	/**
	 * Update model view matrix event
	 */
	abstract public void onUpdateModelViewMatrix();

	/**
	 * @return view port matrix
	 */
	public Matrix4x4 getViewportMatrix() {
		return viewportMatrix;
	}

	/**
	 * @return texture matrix
	 */
	abstract public Matrix4x4 getTextureMatrix();

	/**
	 * Update texture matrix for active texture unit event
	 */
	abstract public void onUpdateTextureMatrix();

	/**
	 * Set up clear color
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	abstract public void setClearColor(float red, float green, float blue, float alpha);

	/**
	 * Enable culling
	 */
	abstract public void enableCulling();

	/**
	 * Disable culling
	 */
	abstract public void disableCulling();

	/**
	 * Enables blending
	 */
	abstract public void enableBlending();

	/**
	 * Disables blending
	 */
	abstract public void disableBlending();

	/**
	 * Enable depth buffer
	 */
	abstract public void enableDepthBuffer();

	/**
	 * Disable depth buffer
	 */
	abstract public void disableDepthBuffer();

	/**
	 * Set up depth function
	 * @param depth function
	 */
	abstract public void setDepthFunction(int depthFunction);

	/**
	 * Set up GL rendering colormask
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	abstract public void setColorMask(boolean red, boolean green, boolean blue, boolean alpha);

	/**
	 * Clear render buffer with given mask
	 * @param mask
	 */
	abstract public void clear(int mask);

	/**
	 * Sets up which face will be culled
	 * @param cull face
	 */
	abstract public void setCullFace(int cullFace);

	/**
	 * Set up clock wise or counter clock wise faces as front face 
	 * @param frontFace
	 */
	abstract public void setFrontFace(int frontFace);

	/**
	 * Creates a texture
	 * @return texture id
	 */
	abstract public int createTexture();

	/**
	 * Creates a depth buffer texture
	 * @param width
	 * @param height
	 * @return depth texture id
	 */
	abstract public int createDepthBufferTexture(int width, int height);

	/**
	 * Creates a color buffer texture
	 * @param width
	 * @param height
	 * @return color buffer texture id
	 */
	abstract public int createColorBufferTexture(int width, int height);

	/**
	 * Uploads texture data to current bound texture
	 * @param texture
	 */
	abstract public void uploadTexture(Texture texture);

	/**
	 * Resizes a depth texture
	 * @param texture id
	 * @param width
	 * @param height
	 */
	abstract public void resizeDepthBufferTexture(int textureId, int width, int height);

	/**
	 * Resize color buffer texture
	 * @param texture id
	 * @param width
	 * @param height
	 */
	abstract public void resizeColorBufferTexture(int textureId, int width, int height);

	/**
	 * Binds a texture with given id or unbinds when using ID_NONE
	 * @param textureId
	 */
	abstract public void bindTexture(int textureId);
	
	/**
	 * On bind texture event
	 * @param textureId
	 */
	abstract public void onBindTexture(int textureId);

	/**
	 * Dispose a texture
	 * @param texture id
	 */
	abstract public void disposeTexture(int textureId);

	/**
	 * Creates a frame buffer object with depth texture attached
	 * @param colorBufferTextureGlId TODO
	 * @param depth texture gl id
	 * @return frame buffer object id
	 */
	abstract public int createFramebufferObject(int depthBufferTextureGlId, int colorBufferTextureGlId);

	/**
	 * Enables a framebuffer to be rendered
	 * @param frameBufferId
	 */
	abstract public void bindFrameBuffer(int frameBufferId);

	/**
	 * Disposes a frame buffer object
	 * @param frame buffer id
	 */
	abstract public void disposeFrameBufferObject(int frameBufferId);

	/**
	 * Generate buffer objects for vertex data and such
	 * @param buffers
	 * @return ids
	 */
	abstract public int[] createBufferObjects(int buffers);

	/**
	 * Uploads buffer data to buffer object
	 * @param buffer object id
	 * @param size
	 * @param data
	 */
	abstract public void uploadBufferObject(int bufferObjectId, int size, FloatBuffer data);

	/**
	 * Uploads buffer data to buffer object
	 * @param buffer object id
	 * @param size
	 * @param data
	 */
	abstract public void uploadIndicesBufferObject(int bufferObjectId, int size, ShortBuffer data);

	/**
	 * Uploads buffer data to buffer object
	 * @param buffer object id
	 * @param size
	 * @param data
	 */
	abstract public void uploadBufferObject(int bufferObjectId, int size, ShortBuffer data);

	/**
	 * Bind indices buffer object
	 * @param buffer object id
	 */
	abstract public void bindIndicesBufferObject(int bufferObjectId);

	/**
	 * Bind texture coordinates buffer object
	 * @param buffer object id
	 */
	abstract public void bindTextureCoordinatesBufferObject(int bufferObjectId);

	/**
	 * Bind vertices buffer object
	 * @param buffer object id
	 */
	abstract public void bindVerticesBufferObject(int bufferObjectId);

	/**
	 * Bind skinning vertices joints buffer object
	 * @param buffer object id
	 */
	abstract public void bindSkinningVerticesJointsBufferObject(int bufferObjectId);

	/**
	 * Bind skinning vertices vertex joints idx buffer object
	 * @param buffer object id
	 */
	abstract public void bindSkinningVerticesVertexJointsIdxBufferObject(int bufferObjectId);

	/**
	 * Bind skinning
	 * @param buffer object id
	 */
	abstract public void bindSkinningVerticesVertexJointsWeightBufferObject(int bufferObjectId);

	/**
	 * Bind normals buffer object
	 * @param buffer object id
	 */
	abstract public void bindNormalsBufferObject(int bufferObjectId);

	/**
	 * Bind colors buffer object
	 * @param buffer object id
	 */
	abstract public void bindColorsBufferObject(int bufferObjectId);

	/**
	 * Bind tangents buffer object
	 * @param buffer object id
	 */
	abstract public void bindTangentsBufferObject(int bufferObjectId);

	/**
	 * Bind bitangents buffer object
	 * @param buffer object id
	 */
	abstract public void bindBitangentsBufferObject(int bufferObjectId);

	/**
	 * Draw indexed triangles from buffer objects 
	 * @param triangles
	 * @param triangles offset
	 */
	abstract public void drawIndexedTrianglesFromBufferObjects(int triangles, int trianglesOffset);

	/**
	 * Draw triangles from buffer objects 
	 * @param triangles
	 * @param triangles offset
	 */
	abstract public void drawTrianglesFromBufferObjects(int triangles, int trianglesOffset);

	/**
	 * Draw points from buffer objects 
	 * @param points
	 * @param points offset
	 */
	abstract public void drawPointsFromBufferObjects(int points, int pointsOffset);

	/**
	 * Unbind buffer objects
	 */
	abstract public void unbindBufferObjects();

	/**
	 * Disposes a frame buffer object
	 * @param frame buffer id
	 */
	abstract public void disposeBufferObjects(int[] bufferObjectIds);

	/**
	 * @return active texture unit
	 */
	abstract public int getTextureUnit();

	/**
	 * Sets up texture unit
	 * @param texture unit
	 */
	abstract public void setTextureUnit(int textureUnit);

	/**
	 * Enable a client state / capability
	 * @param client state
	 */
	abstract public void enableClientState(int clientState);

	/**
	 * Disable a client state / capability
	 * @param client state
	 */
	abstract public void disableClientState(int clientState);

	/**
	 * Enable light
	 * @param light id
	 */
	public void setLightEnabled(int lightId) {
		lights[lightId].enabled = 1;
	}

	/**
	 * Disable light
	 * @param light id
	 */
	public void setLightDisabled(int lightId) {
		lights[lightId].enabled = 0;
	}

	/**
	 * Set light ambient color
	 * @param light id
	 * @param ambient
	 */
	public void setLightAmbient(int lightId, float[] ambient) {
		System.arraycopy(ambient, 0, lights[lightId].ambient, 0, Math.min(ambient.length, lights[lightId].ambient.length));
	}

	/**
	 * Set light diffuse color
	 * @param light id
	 * @param diffuse
	 */
	public void setLightDiffuse(int lightId, float[] diffuse) {
		System.arraycopy(diffuse, 0, lights[lightId].diffuse, 0, Math.min(diffuse.length, lights[lightId].diffuse.length));
	}

	/**
	 * Set light position
	 * @param light id
	 * @param position
	 */
	public void setLightPosition(int lightId, float[] position) {
		System.arraycopy(position, 0, lights[lightId].position, 0, Math.min(position.length, lights[lightId].position.length));
	}

	/**
	 * Set light spot direction
	 * @param light id
	 * @param spot direction
	 */
	public void setLightSpotDirection(int lightId, float[] spotDirection) {
		System.arraycopy(spotDirection, 0, lights[lightId].spotDirection, 0, Math.min(spotDirection.length, lights[lightId].spotDirection.length));
	}

	/**
	 * Set light spot exponent
	 * @param light id
	 * @param spot exponent
	 */
	public void setLightSpotExponent(int lightId, float spotExponent) {
		lights[lightId].spotExponent = spotExponent;
	}

	/**
	 * Set light spot cut off
	 * @param light id
	 * @param spot cut off
	 */
	public void setLightSpotCutOff(int lightId, float spotCutOff) {
		lights[lightId].spotCosCutoff = (float)Math.cos(Math.PI / 180f * spotCutOff);
	}

	/**
	 * Set light constant attenuation
	 * @param light id
	 * @param constant attenuation
	 */
	public void setLightConstantAttenuation(int lightId, float constantAttenuation) {
		lights[lightId].constantAttenuation = constantAttenuation;
	}

	/**
	 * Set light linear attenuation
	 * @param light id
	 * @param linear attenuation
	 */
	public void setLightLinearAttenuation(int lightId, float linearAttenuation) {
		lights[lightId].linearAttenuation = linearAttenuation;
	}

	/**
	 * Set light quadratic attenuation
	 * @param light id
	 * @param quadratic attenuation
	 */
	public void setLightQuadraticAttenuation(int lightId, float QuadraticAttenuation) {
		lights[lightId].quadraticAttenuation = QuadraticAttenuation;
	}

	/**
	 * Update light
	 * @param light id
	 */
	abstract public void onUpdateLight(int lightId);

	/**
	 * Set up effect color multiplication
	 * @param effect color for multiplication
	 */
	public void setEffectColorMul(float[] effectColorMul) {
		System.arraycopy(effectColorMul, 0, this.effectColorMul, 0, Math.min(effectColorMul.length, this.effectColorMul.length));
	}

	/**
	 * Set up effect color addition
	 * @param effect color for addition
	 */
	public void setEffectColorAdd(float[] effectColorAdd) {
		System.arraycopy(effectColorAdd, 0, this.effectColorAdd, 0, Math.min(effectColorAdd.length, this.effectColorAdd.length));
	}

	/**
	 * Update material
	 */
	abstract public void onUpdateEffect();

	/**
	 * Enable materials
	 */
	public void setMaterialEnabled() {
	}

	/**
	 * Disable materials
	 */
	public void setMaterialDisabled() {
	}

	/**
	 * Set material ambient color
	 * @param ambient
	 */
	public void setMaterialAmbient(float[] ambient) {
		System.arraycopy(ambient, 0, material.ambient, 0, Math.min(ambient.length, material.ambient.length));
	}

	/**
	 * Set material diffuse color
	 * @param diffuse
	 */
	public void setMaterialDiffuse(float[] diffuse) {
		System.arraycopy(diffuse, 0, material.diffuse, 0, Math.min(diffuse.length, material.diffuse.length));
	}

	/**
	 * Set material specular color
	 * @param specular
	 */
	public void setMaterialSpecular(float[] specular) {
		System.arraycopy(specular, 0, material.specular, 0, Math.min(specular.length, material.specular.length));
	}

	/**
	 * Set material emission color
	 * @param emission
	 */
	public void setMaterialEmission(float[] emission) {
		System.arraycopy(emission, 0, material.emission, 0, Math.min(emission.length, material.emission.length));
	}

	/**
	 * Set material shininess
	 * @param shininess
	 */
	public void setMaterialShininess(float shininess) {
		material.shininess = shininess;
	}

	/**
	 * Update material
	 */
	abstract public void onUpdateMaterial();

	/**
	 * Reads a pixel depth
	 * @param x
	 * @param y
	 * @return depth 0.0f..1.0f
	 */
	abstract public float readPixelDepth(int x, int y);

	/**
	 * Read pixels
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return byte buffer
	 */
	abstract public ByteBuffer readPixels(int x, int y, int width, int height);

	/**
	 * Set up renderer for GUI rendering
	 */
	abstract public void initGuiMode();

	/**
	 * Set up renderer for 3d rendering
	 */
	abstract public void doneGuiMode();

}

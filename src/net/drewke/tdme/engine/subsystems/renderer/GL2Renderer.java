package net.drewke.tdme.engine.subsystems.renderer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.utils.Console;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

/**
 * OpenGL 2 renderer
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GL2Renderer extends GLRenderer {

	private GL2 gl;
	private boolean bufferObjectsAvailable;
	private int viewPortX;
	private int viewPortY;
	private int viewPortWidth;
	private int viewPortHeight;
	private Matrix4x4[] textureMatrix;
	private int activeTextureUnit;

	/**
	 * final public constructor
	 */
	public GL2Renderer() {
		// setup GL2 consts
		ID_NONE = 0;

		CLEAR_DEPTH_BUFFER_BIT = GL2.GL_DEPTH_BUFFER_BIT;
		CLEAR_COLOR_BUFFER_BIT = GL2.GL_COLOR_BUFFER_BIT;

		CULLFACE_FRONT = GL2.GL_FRONT;
		CULLFACE_BACK = GL2.GL_BACK;

		FRONTFACE_CW = GL2.GL_CW;
		FRONTFACE_CCW = GL2.GL_CCW;

		CLIENTSTATE_TEXTURECOORD_ARRAY = GL2.GL_TEXTURE_COORD_ARRAY;
		CLIENTSTATE_VERTEX_ARRAY = GL2.GL_VERTEX_ARRAY;
		CLIENTSTATE_NORMAL_ARRAY = GL2.GL_NORMAL_ARRAY;
		CLIENTSTATE_COLOR_ARRAY = GL2.GL_COLOR_ARRAY;

		SHADER_FRAGMENT_SHADER = GL2.GL_FRAGMENT_SHADER;
		SHADER_VERTEX_SHADER = GL2.GL_VERTEX_SHADER;

		DEPTHFUNCTION_LESSEQUAL = GL2.GL_LEQUAL;
		DEPTHFUNCTION_EQUAL = GL2.GL_EQUAL;

		//
		bufferObjectsAvailable = false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setGL(javax.media.opengl.GL2)
	 */
	final public void setGL(GL gl) {
		this.gl = (GL2)gl;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#getGLVersion()
	 */
	final public String getGLVersion() {
		return "gl2";
	}

	/**
	 * Checks if VBO is available
	 * 	based on http://www.felixgers.de/teaching/jogl/vertexBufferObject.html
	 * @param gl
	 * @return vbo availability
	 */
	final private boolean checkBufferObjectsAvailable() {
		// Check if extension is available.
		boolean extensionOK = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");

		// Check for VBO functions.
		boolean functionsOK =
			gl.isFunctionAvailable("glGenBuffersARB") &&
			gl.isFunctionAvailable("glBindBufferARB") &&
			gl.isFunctionAvailable("glBufferDataARB") &&
			gl.isFunctionAvailable("glDeleteBuffersARB");

		return extensionOK == true && functionsOK == true; 
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#isDepthTextureAvailable()
	 */
	final public boolean isDepthTextureAvailable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#initialize()
	 */
	final public void initialize() {
		gl.glGetError();

		// get default framebuffer
		FRAMEBUFFER_DEFAULT = gl.getContext().getDefaultDrawFramebuffer();

		// check VBO
		bufferObjectsAvailable = checkBufferObjectsAvailable();
	
		// setup open gl
		gl.glShadeModel(GL2.GL_SMOOTH);									// Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);						// Black Background
		gl.glClearDepth(1.0f); 											// Depth Buffer Setup
		gl.glEnable(GL2.GL_DEPTH_TEST); 								// Enables Depth Testing
		gl.glEnable(GL2.GL_CULL_FACE);									// The Type Of Depth Testing To Do
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);	// Really Nice Perspective Calculations
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);  // enable alpha transparency
		gl.glBlendEquation(GL2.GL_FUNC_ADD);
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_PROGRAM_POINT_SIZE_EXT);

		//
		setTextureUnit(0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#initFrame()
	 */
	final public void initializeFrame() {
		if (gl.getContext().isCurrent() == false) gl.getContext().makeCurrent();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#isBufferObjectsAvailable()
	 */
	final public boolean isBufferObjectsAvailable() {
		return bufferObjectsAvailable;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#isUsingProgramAttributeLocation()
	 */
	final public boolean isUsingProgramAttributeLocation() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#isSpecularMappingAvailable()
	 */
	final public boolean isSpecularMappingAvailable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#isNormalMappingAvailable()
	 */
	final public boolean isNormalMappingAvailable() {
		return false;
	}

	/**
	 * @return if displacement mapping is supported
	 */
	final public boolean isDisplacementMappingAvailable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#isSkinningAvailable()
	 */
	final public boolean isSkinningAvailable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#getTextureUnits()
	 */
	final public int getTextureUnits() {
		// check for number of texture units
		IntBuffer tmp = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, tmp);
		while (tmp.remaining() > 0) {
			int textureUnits = tmp.get();
			return textureUnits;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#loadShader(int, java.lang.String, java.lang.String)
	 */
	final public int loadShader(int type, String pathName, String fileName) {
		// create shader
		int handle = gl.glCreateShader(type);

		// exit if no handle returned
		if (handle == 0) return 0;

		// shader source
		String[] shaderSources = new String[1];
		shaderSources[0] = new String();

		// read shader source
		DataInputStream sourceInputStream = null;
		try {
			sourceInputStream = new DataInputStream(Engine.getInstance().getInputStream(pathName, fileName));
			BufferedReader reader = new BufferedReader(new InputStreamReader(sourceInputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSources[0]+= line + "\n";
			}
			sourceInputStream.close();
		} catch (IOException ioe) {
			gl.glDeleteShader(handle);
			return 0;
		} finally {
			try { sourceInputStream.close(); } catch (IOException ioe) {}
		}

		// load source
		gl.glShaderSourceARB(
			handle,
			1,
			shaderSources,
			null
		);

		// compile
		gl.glCompileShaderARB(handle);

		// check state
		IntBuffer compileStatus = IntBuffer.allocate(1);
		gl.glGetObjectParameterivARB(handle, GL2.GL_OBJECT_COMPILE_STATUS_ARB, compileStatus);
		while (compileStatus.remaining() > 0) {
			int result = compileStatus.get();
			if (result == 0) {
				// get error
				IntBuffer infoLogLengthBuffer = Buffers.newDirectIntBuffer(1);
				ByteBuffer infoLogBuffer = Buffers.newDirectByteBuffer(2048); 
		        gl.glGetShaderInfoLog(handle, infoLogBuffer.limit(), infoLogLengthBuffer, infoLogBuffer);
		        final byte[] infoLogBytes = new byte[infoLogLengthBuffer.get()]; 
		        infoLogBuffer.get(infoLogBytes); 
		        String infoLogString = new String(infoLogBytes);

		        // be verbose
		        Console.println("[" + handle + "]" + pathName + "/" + fileName + ": failed: " + infoLogString);

				// remove shader
				gl.glDeleteShader(handle);
				return 0;
			}
		}

		//
		return handle;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#useProgram(int)
	 */
	final public void useProgram(int programId) {
		gl.glUseProgramObjectARB(programId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createProgram()
	 */
	final public int createProgram() {
		int programId = gl.glCreateProgram();
		return programId;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#attachShaderToProgram(int, int)
	 */
	final public void attachShaderToProgram(int programId, int shaderId) {
		gl.glAttachObjectARB(programId, shaderId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#linkProgram(int)
	 */
	final public boolean linkProgram(int programId) {
		gl.glLinkProgramARB(programId);

		// check state
		IntBuffer linkStatus = IntBuffer.allocate(1);
		gl.glGetProgramiv(programId, GL2.GL_LINK_STATUS, linkStatus);
		while (linkStatus.remaining() > 0) {
			int result = linkStatus.get();
			if (result == 0) {
				// get error
				IntBuffer infoLogLengthBuffer = Buffers.newDirectIntBuffer(1);
				ByteBuffer infoLogBuffer = Buffers.newDirectByteBuffer(2048); 
		        gl.glGetProgramInfoLog(programId, infoLogBuffer.limit(), infoLogLengthBuffer, infoLogBuffer); 
		        final byte[] infoLogBytes = new byte[infoLogLengthBuffer.get()]; 
		        infoLogBuffer.get(infoLogBytes); 
		        String infoLogString = new String(infoLogBytes);

		        // be verbose
		        Console.println("[" + programId + "]: failed: " + infoLogString);

				//
				return false;
			}
		}

		//
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#getProgramUniformLocation(int, java.lang.String)
	 */
	final public int getProgramUniformLocation(int programId, String name) {
		int uniformLocation = gl.glGetUniformLocationARB(programId, name);
		return uniformLocation;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformInteger(int, int)
	 */
	final public void setProgramUniformInteger(int uniformId, int value) {
		gl.glUniform1iARB(uniformId, value);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloat(int, float)
	 */
	final public void setProgramUniformFloat(int uniformId, float value) {
		gl.glUniform1fARB(uniformId, value);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloatMatrix3x3(int, float[])
	 */
	final public void setProgramUniformFloatMatrix3x3(int uniformId, float[] data) {
		gl.glUniformMatrix3fv(uniformId, 1, false, data, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloatMatrices4x4(int, int, java.nio.FloatBuffer)
	 */
	final public void setProgramUniformFloatMatrices4x4(int uniformId, int count, FloatBuffer data) {
		gl.glUniformMatrix4fv(uniformId, count, false, data);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloatMatrix4x4(int, float[])
	 */
	final public void setProgramUniformFloatMatrix4x4(int uniformId, float[] data) {
		gl.glUniformMatrix4fv(uniformId, 1, false, data, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloatVec4(int, float[])
	 */
	final public void setProgramUniformFloatVec4(int uniformId, float[] data) {
		gl.glUniform4fv(uniformId, 1, data, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramUniformFloatVec3(int, float[])
	 */
	final public void setProgramUniformFloatVec3(int uniformId, float[] data) {
		gl.glUniform3fv(uniformId, 1, data, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setProgramAttributeLocation(int, int, java.lang.String)
	 */
	final public void setProgramAttributeLocation(int programId, int location, String name) {
		gl.glBindAttribLocation(programId, location, name);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setViewPort(int, int, int, int)
	 */
	final public void setViewPort(int x, int y, int width, int height) {
		this.viewPortX = x;
		this.viewPortY = x;
		this.viewPortWidth = width;
		this.viewPortHeight = height;
		this.pointSize = width > height?width / 12f:height / 12f * 16 / 9;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#updateViewPort()
	 */
	final public void updateViewPort() {
		gl.glViewport(viewPortX, viewPortY, viewPortWidth, viewPortHeight);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#getTextureMatrix()
	 */
	final public Matrix4x4 getTextureMatrix() {
		return textureMatrix[activeTextureUnit];
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setClearColor(float, float, float, float)
	 */
	final public void setClearColor(float red, float green, float blue, float alpha) {
		gl.glClearColor(red, green, blue, alpha);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#enableCulling()
	 */
	final public void enableCulling() {
		gl.glEnable(GL2.GL_CULL_FACE);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disableCulling()
	 */
	final public void disableCulling() {
		gl.glDisable(GL2.GL_CULL_FACE);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#enableBlending()
	 */
	final public void enableBlending() {
		gl.glEnable(GL2.GL_BLEND);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disableBlending()
	 */
	final public void disableBlending() {
		gl.glDisable(GL2.GL_BLEND);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#enableDepthBuffer()
	 */
	final public void enableDepthBuffer() {
		gl.glDepthMask(true);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disableDepthBuffer()
	 */
	final public void disableDepthBuffer() {
		gl.glDepthMask(false);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setDepthFunction(int)
	 */
	final public void setDepthFunction(int depthFunction) {
		gl.glDepthFunc(depthFunction);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setColorMask(boolean, boolean, boolean, boolean)
	 */
	final public void setColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		gl.glColorMask(red, green, blue, alpha);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#clear(int)
	 */
	final public void clear(int mask) {
		gl.glClear(mask);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#setCullFace(int)
	 */
	final public void setCullFace(int cullFace) {
		gl.glCullFace(cullFace);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#setFrontFace(int)
	 */
	final public void setFrontFace(int frontFace) {
		gl.glFrontFace(frontFace);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createTexture()
	 */
	final public int createTexture() {
		// generate open gl texture
		final int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		int textureId = tmp[0]; // texture id
		return textureId;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createDepthTexture(int, int)
	 */
	final public int createDepthBufferTexture(int width, int height) {
		int depthTextureGlId;
		// create depth texture
		final int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		depthTextureGlId = tmp[0]; // texture id
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTextureGlId);

		// create depth texture
		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D,
			0,
			GL2.GL_DEPTH_COMPONENT,
			width,
			height,
			0,
			GL2.GL_DEPTH_COMPONENT,
			GL2.GL_FLOAT,
			null
		);

		// depth texture parameter
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

		// unbind, return
		gl.glBindTexture(GL2.GL_TEXTURE_2D, ID_NONE);
		return depthTextureGlId;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createColorBufferTexture(int, int)
	 */
	final public int createColorBufferTexture(int width, int height) {
		int colorBufferTextureGlId;
		// create depth texture
		final int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		colorBufferTextureGlId = tmp[0]; // texture id
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorBufferTextureGlId);

		// create color texture
		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D,
			0,
			GL2.GL_RGBA,
			width,
			height, 
			0,
			GL2.GL_RGBA,
			GL2.GL_UNSIGNED_BYTE,
			null
		);

		// color texture parameter
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

		// unbind, return
		gl.glBindTexture(GL2.GL_TEXTURE_2D, ID_NONE);
		return colorBufferTextureGlId;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#uploadTexture(net.drewke.tdme.engine.fileio.textures.Texture)
	 */
	final public void uploadTexture(Texture texture) {
		//
		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D,
			0,
			texture.getDepth() == 32?GL2.GL_RGBA:GL2.GL_RGB,
			texture.getTextureWidth(),
			texture.getTextureHeight(),
			0,
			texture.getDepth() == 32?GL2.GL_RGBA:GL2.GL_RGB,
			GL2.GL_UNSIGNED_BYTE,
			texture.getTextureData()
		);

		//
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

		//
		gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#resizeDepthTexture(int, int, int)
	 */
	final public void resizeDepthBufferTexture(int textureId, int width, int height) {
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureId);
		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D,
			0,
			GL2.GL_DEPTH_COMPONENT,
			width,
			height,
			0,
			GL2.GL_DEPTH_COMPONENT,
			GL2.GL_FLOAT,
			null
		);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#resizeColorBufferTexture(int, int, int)
	 */
	final public void resizeColorBufferTexture(int textureId, int width, int height) {
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureId);
		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D,
			0,
			GL2.GL_RGBA,
			width,
			height, 
			0,
			GL2.GL_RGBA,
			GL2.GL_UNSIGNED_BYTE,
			null
		);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindTexture(int)
	 */
	final public void bindTexture(int textureId) {
		// bind depth texture
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureId);

		//
		onBindTexture(textureId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disposeTexture(int)
	 */
	final public void disposeTexture(int textureId) {
		gl.glDeleteTextures(1, new int[] {textureId}, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createDepthFramebufferObject(int)
	 */
	final public int createFramebufferObject(int depthBufferTextureGlId, int colorBufferTextureGlId) {
		int frameBufferGlId;

		// create a frame buffer object
		int fboIds[] = new int[1];
		gl.glGenFramebuffers(1, fboIds, 0);
		frameBufferGlId = fboIds[0];
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferGlId);

		// attach the depth buffer texture to FBO
		if (depthBufferTextureGlId != ID_NONE) {
			gl.glFramebufferTexture2D(
				GL2.GL_FRAMEBUFFER,
				GL2.GL_DEPTH_ATTACHMENT,
				GL2.GL_TEXTURE_2D,
				depthBufferTextureGlId,
				0
			);
		}

		// attach the color buffer texture to FBO
		if (colorBufferTextureGlId != ID_NONE) {
			gl.glFramebufferTexture2D(
				GL2.GL_FRAMEBUFFER,
				GL2.GL_COLOR_ATTACHMENT0,
				GL2.GL_TEXTURE_2D,
				colorBufferTextureGlId,
				0
			);
			gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0);
			gl.glReadBuffer(GL2.GL_COLOR_ATTACHMENT0);
		} else {
			gl.glDrawBuffer(GL2.GL_NONE);
			gl.glReadBuffer(GL2.GL_NONE);
		}

		// check FBO status
		int fboStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if (fboStatus != GL2.GL_FRAMEBUFFER_COMPLETE) {
			Console.println("GL_FRAMEBUFFER_COMPLETE_EXT failed, CANNOT use FBO\n");
		}

		// switch back to window-system-provided framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);

		//
		return frameBufferGlId;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindFrameBuffer(int)
	 */
	final public void bindFrameBuffer(int frameBufferId) {
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disposeFrameBufferObject(int)
	 */
	final public void disposeFrameBufferObject(int frameBufferId) {
		gl.glDeleteFramebuffers(1, new int[] {frameBufferId}, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#createBufferObjects(int)
	 */
	final public int[] createBufferObjects(int buffers) {
		int[] bufferObjectIds = new int[buffers];
		gl.glGenBuffers(buffers, bufferObjectIds, 0);
		return bufferObjectIds;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#uploadBufferObject(int, int, java.nio.FloatBuffer)
	 */
	final public void uploadBufferObject(int bufferObjectId, int size, FloatBuffer data) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, size, data, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, ID_NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#uploadIndicesBufferObject(int, int, java.nio.ShortBuffer)
	 */
	final public void uploadIndicesBufferObject(int bufferObjectId, int size, ShortBuffer data) {
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferObjectId);
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, size, data, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ID_NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#uploadBufferObject(int, int, java.nio.ShortBuffer)
	 */
	final public void uploadBufferObject(int bufferObjectId, int size, ShortBuffer data) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, size, data, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, ID_NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindIndicesBufferObject(int)
	 */
	final public void bindIndicesBufferObject(int bufferObjectId) {
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferObjectId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindTextureCoordinatesBufferObject(int)
	 */
	final public void bindTextureCoordinatesBufferObject(int bufferObjectId) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0L);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindVerticesBufferObject(int)
	 */
	final public void bindVerticesBufferObject(int bufferObjectId) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0L);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindNormalsBufferObject(int)
	 */
	final public void bindNormalsBufferObject(int bufferObjectId) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glNormalPointer(GL2.GL_FLOAT, 0, 0L);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#bindColorsBufferObject(int)
	 */
	final public void bindColorsBufferObject(int bufferObjectId) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjectId);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, 0L);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindSkinningVerticesJointsBufferObject(int)
	 */
	final public void bindSkinningVerticesJointsBufferObject(int bufferObjectId) {
		Console.println("GL2Renderer::bindSkinningVerticesJointsBufferObject()::not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindSkinningVerticesVertexJointsIdxBufferObject(int)
	 */
	final public void bindSkinningVerticesVertexJointsIdxBufferObject(int bufferObjectId) {
		Console.println("GL2Renderer::bindSkinningVerticesVertexJointsIdxBufferObject()::not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#bindSkinningVerticesVertexJointsWeightBufferObject(int)
	 */
	final public void bindSkinningVerticesVertexJointsWeightBufferObject(int bufferObjectId) {
		Console.println("GL2Renderer::bindSkinningVerticesVertexJointsWeightBufferObject()::not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#bindTangentsBufferObject(int)
	 */
	final public void bindTangentsBufferObject(int bufferObjectId) {
		Console.println("GL2Renderer::bindTangentsBufferObject()::not implemented yet");	
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#bindBitangentsBufferObject(int)
	 */
	final public void bindBitangentsBufferObject(int bufferObjectId) {
		Console.println("GL2Renderer::bindBitangentsBufferObject()::not implemented yet");	
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#drawTrianglesFromBufferObjects(int, int)
	 */
	final public void drawIndexedTrianglesFromBufferObjects(int triangles, int trianglesOffset) {
		gl.glDrawElements(GL2.GL_TRIANGLES, triangles * 3, GL2.GL_UNSIGNED_SHORT, (long)trianglesOffset * 3L * 2L);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#drawTrianglesFromBufferObjects(int, int)
	 */
	final public void drawTrianglesFromBufferObjects(int triangles, int trianglesOffset) {
		gl.glDrawArrays(GL2.GL_TRIANGLES, trianglesOffset * 3, triangles * 3);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.renderer.GLRenderer#drawPointsFromBufferObjects(int, int)
	 */
	final public void drawPointsFromBufferObjects(int points, int pointsOffset) {
		gl.glDrawArrays(GL2.GL_POINTS, pointsOffset, points);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#unbindBufferObjects()
	 */
	final public void unbindBufferObjects() {
		// unbind buffers
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disposeBufferObjects(int[])
	 */
	final public void disposeBufferObjects(int[] bufferObjectIds) {
		gl.glDeleteBuffers(bufferObjectIds.length, bufferObjectIds, 0);	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#getTextureUnit()
	 */
	final public int getTextureUnit() {
		return activeTextureUnit;
	}

	final public void setTextureUnit(int textureUnit) {
		this.activeTextureUnit = textureUnit;
		gl.glActiveTexture(GL2.GL_TEXTURE0 + textureUnit);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#enableClientState(int)
	 */
	final public void enableClientState(int clientState) {
		gl.glEnableClientState(clientState);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#disableClientState(int)
	 */
	final public void disableClientState(int clientState) {
		gl.glDisableClientState(clientState);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#readPixelDepth(int, int)
	 */
	final public float readPixelDepth(int x, int y) {
		FloatBuffer pixelDepthBuffer = FloatBuffer.allocate(1);
		gl.glReadPixels(x, y, 1, 1, GL2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, pixelDepthBuffer);
		return pixelDepthBuffer.get();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#readPixels(int, int, int, int)
	 */
	final public ByteBuffer readPixels(int x, int y, int width, int height) {
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(width * height * Byte.SIZE * 4).order(ByteOrder.nativeOrder());
		gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
		gl.glReadPixels(x, y, width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, pixelBuffer);
		return pixelBuffer;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#initGuiMode()
	 */
	final public void initGuiMode() {
		setTextureUnit(0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, ID_NONE);
		gl.glEnable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glGetError();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.GLRenderer#doneGuiMode()
	 */
	final public void doneGuiMode() {
		gl.glGetError();
		gl.glBindTexture(GL2.GL_TEXTURE_2D, ID_NONE);
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE);
	}


}

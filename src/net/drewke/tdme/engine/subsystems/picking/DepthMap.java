package net.drewke.tdme.engine.subsystems.picking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.FrameBuffer;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.PointsParticleSystemEntity;
import net.drewke.tdme.engine.fileio.textures.PNG;
import net.drewke.tdme.engine.subsystems.object.Object3DVBORenderer;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;

/**
 * Depth map class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class DepthMap {

	protected final static int TEXTUREUNIT = 4; 

	private ArrayList<Object3D> visibleObjects;
	private ArrayList<PointsParticleSystemEntity> visiblePpses;

	private Engine engine;
	private GLRenderer renderer;
	private Camera camera;
	private FrameBuffer frameBuffer;

	/**
	 * Public constructor
	 * @param renderer
	 * @param engine
	 * @param width
	 * @param height
	 */
	public DepthMap(GLRenderer renderer, Engine engine) {
		this.renderer = renderer;
		this.engine = engine;
		visibleObjects = new ArrayList<Object3D>();
		visiblePpses = new ArrayList<PointsParticleSystemEntity>();
		camera = engine.getCamera();
		frameBuffer = new FrameBuffer(
			engine,
			512,
			512,
			FrameBuffer.FRAMEBUFFER_COLORBUFFER | FrameBuffer.FRAMEBUFFER_DEPTHBUFFER 
		);
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return frameBuffer.getWidth();
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return frameBuffer.getHeight();
	}

	/**
	 * Init frame buffer
	 */
	public void initialize() {
		frameBuffer.initialize();
	}

	/**
	 * Reshape frame buffer
	 * @param width
	 * @param height
	 */
	public void reshape(int width, int height) {
		// no op, we have a fixed depth map size
	}

	/**
	 * Disposes this shadow mapping
	 */
	protected void dispose() {
		frameBuffer.dispose();
	}

	/**
	 * @return framebuffer
	 */
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	/**
	 * Renders given objects to depth map
	 */
	public void render() {
		// clear visible objects
		visibleObjects.clear();

		// set up camera from engine
		Camera engineCamera = engine.getCamera();
		camera.setFovY(engineCamera.getFovY());
		camera.setZNear(engineCamera.getZNear());
		camera.setZFar(engineCamera.getZFar());
		camera.getLookFrom().set(engineCamera.getLookFrom());
		camera.getLookAt().set(engineCamera.getLookAt());
		camera.getUpVector().set(engineCamera.getUpVector());
		camera.update(frameBuffer.getWidth(), frameBuffer.getHeight());

		// Bind frame buffer to shadow map fbo id
		frameBuffer.enableFrameBuffer();

		// clear color buffer
		renderer.clear(renderer.CLEAR_COLOR_BUFFER_BIT | renderer.CLEAR_DEPTH_BUFFER_BIT);

		// determine visible objects and objects that should generate a shadow
		visibleObjects.clear();
		visiblePpses.clear();
		for (Entity entity: engine.getVisibleObjects()) {
			if (entity instanceof Object3D) {
				Object3D object = (Object3D)entity;
				if (object.isPickable() == false) continue;
				visibleObjects.add(object);
			} else
			if (entity instanceof PointsParticleSystemEntity) {
				PointsParticleSystemEntity ppse = (PointsParticleSystemEntity)entity;
				if (ppse.isPickable() == false) continue;
				visiblePpses.add(ppse);
			}
		}

		// object 3D VBO renderer
		Object3DVBORenderer object3DVBORenderer = engine.getObject3DVBORenderer();

		DepthMapShaderObjects depthMapShaderObjects = Engine.getDepthMapShaderObjects();

		// enable frame buffer
		frameBuffer.enableFrameBuffer();

		// bind program
		depthMapShaderObjects.useProgram();

		// only draw opaque face entities as shadows will not be produced from transparent objects
		object3DVBORenderer.render(visibleObjects, true, Object3DVBORenderer.DepthBufferMode.NORMAL, null);

		// unbind program
		depthMapShaderObjects.unUseProgram();

		// fetch pixel
		ByteBuffer pixels = renderer.readPixels(0, 0, getWidth(), getHeight());

		// lets test if this works
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(File.separator + "picking-depthmap.png");
			PNG.save(getWidth(), getHeight(), pixels, fos);
		} catch (IOException ioe) {
			Console.println("Engine::makeScreenshot(): failed: " + ioe.getMessage());
		} finally {
			if (fos != null) try { fos.close(); } catch (IOException ioe2) {}
		}

		// disable frame buffer
		FrameBuffer.disableFrameBuffer();

		// render points based particle systems 
		// object3DVBORenderer.render(visiblePpses);
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "DepthMap [frameBuffer=" + frameBuffer + "]";
	}

}

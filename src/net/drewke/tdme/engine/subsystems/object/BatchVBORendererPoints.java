package net.drewke.tdme.engine.subsystems.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Vector3;

/**
 * Batch VBO renderer
 * @author andreas.drewke
 * @version $Id$
 */
public final class BatchVBORendererPoints {

	private static int VERTEX_COUNT = 32768;

	private GLRenderer renderer;

	private int[] vboIds;
	private int id;
	private boolean acquired;

	private FloatBuffer fbVertices = ByteBuffer.allocateDirect(VERTEX_COUNT * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbColors = ByteBuffer.allocateDirect(VERTEX_COUNT * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	/**
	 * Public constructor
	 * @param renderer
	 */
	public BatchVBORendererPoints(GLRenderer renderer, int id) {
		this.id = id;
		this.renderer = renderer;
		this.acquired = false;
	}

	/**
	 * @return acquired
	 */
	public boolean isAcquired() {
		return acquired;
	}

	/**
	 * Acquire
	 */
	public boolean acquire() {
		if (acquired == true) return false;
		acquired = true;
		return true;
	}

	/**
	 * Release
	 */
	public void release() {
		acquired = false;
	}

	/**
	 * Init
	 */
	public void init() {
		// initialize if not yet done
		if (vboIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO("tdme.batchvborendererpoints." + id, 2);
			vboIds = vboManaged.getVBOGlIds();
		}
	}

	/**
	 * Render 
	 */
	protected void render() {
		fbVertices.flip();
		fbColors.flip();

		// skip if no vertex data exists
		if (fbVertices.limit() == 0 || fbColors.limit() == 0) return;

		// determine triangles count
		int points = fbVertices.limit() / 3;

		// upload vertices
		renderer.uploadBufferObject(
			vboIds[0],
			fbVertices.limit() * Float.SIZE / Byte.SIZE,
			fbVertices
		);

		// upload colors
		renderer.uploadBufferObject(
			vboIds[1],
			fbColors.limit() * Float.SIZE / Byte.SIZE,
			fbColors
		);

		// vertices
		renderer.bindVerticesBufferObject(vboIds[0]);

		// normals
		renderer.bindColorsBufferObject(vboIds[1]);

		// draw
		renderer.drawPointsFromBufferObjects(points, 0);
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		// 
		if (vboIds != null) {
			Engine.getInstance().getVBOManager().removeVBO("tdme.batchvborendererpoints." + id);
			vboIds = null;
		}
	}

	/**
	 * Clears this batch vbo renderer
	 */
	protected void clear() {
		fbVertices.clear();
		fbColors.clear();
	}

	/**
	 * Adds a transparent render point to this transparent render points
	 * @param transparent render point
	 */
	protected void addPoint(TransparentRenderPoint point) {
		fbVertices.put(point.point.getArray());
		fbColors.put(point.color.getArray());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "BatchVBORenderer [vboIds=" + Arrays.toString(vboIds) + ", id="
				+ id + ", acquired=" + acquired + ", fbVertices=" + fbVertices.position()
				+ ", fbNormals=" + fbColors.position() + "]";
	}

}

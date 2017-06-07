package net.drewke.tdme.engine.subsystems.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Vector3;

/**
 * Batch VBO renderer
 * @author andreas.drewke
 * @version $Id$
 */
public final class BatchVBORendererTriangles {

	private static int VERTEX_COUNT = 1024 * 3;

	private GLRenderer renderer;

	private int[] vboIds;
	private int id;
	private boolean acquired;

	private int vertices = 0;
	private FloatBuffer fbVertices = ByteBuffer.allocateDirect(VERTEX_COUNT * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbNormals = ByteBuffer.allocateDirect(VERTEX_COUNT * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbTextureCoordinates = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	private static final float[] TEXTURECOORDINATE_NONE = {0f,0f};

	/**
	 * Public constructor
	 * @param renderer
	 */
	public BatchVBORendererTriangles(GLRenderer renderer, int id) {
		this.id = id;
		this.renderer = renderer;
		this.acquired = false;
		this.vertices = 0;
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
	public void initialize() {
		// initialize if not yet done
		if (vboIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO("tdme.batchvborenderertriangles." + id, 3);
			vboIds = vboManaged.getVBOGlIds();
		}
	}

	/**
	 * Render 
	 */
	protected void render() {
		fbVertices.flip();
		fbNormals.flip();
		fbTextureCoordinates.flip();

		// skip if no vertex data exists
		if (fbVertices.limit() == 0 || fbNormals.limit() == 0 || fbTextureCoordinates.limit() == 0) return;

		// determine triangles count
		int triangles = fbVertices.limit() / 3 / 3;

		// upload vertices
		renderer.uploadBufferObject(
			vboIds[0],
			fbVertices.limit() * Float.SIZE / Byte.SIZE,
			fbVertices
		);

		// upload normals
		renderer.uploadBufferObject(
			vboIds[1],
			fbNormals.limit() * Float.SIZE / Byte.SIZE,
			fbNormals
		);

		// upload texture coordinates
		renderer.uploadBufferObject(
			vboIds[2],
			fbTextureCoordinates.limit() * Float.SIZE / Byte.SIZE,
			fbTextureCoordinates
		);

		// vertices
		renderer.bindVerticesBufferObject(vboIds[0]);

		// normals
		renderer.bindNormalsBufferObject(vboIds[1]);

		// texture coordinates
		renderer.bindTextureCoordinatesBufferObject(vboIds[2]);

		// draw
		renderer.drawTrianglesFromBufferObjects(triangles, 0);
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		// 
		if (vboIds != null) {
			Engine.getInstance().getVBOManager().removeVBO("tdme.batchvborenderertriangles." + id);
			vboIds = null;
		}
	}

	/**
	 * Clears this batch vbo renderer
	 */
	protected void clear() {
		vertices = 0;
		fbVertices.clear();
		fbNormals.clear();
		fbTextureCoordinates.clear();
	}

	/**
	 * Adds a vertex to this transparent render faces group
	 * @param vertex
	 * @param normal
	 * @param texture coordinate
	 * @return success
	 */
	protected boolean addVertex(Vector3 vertex, Vector3 normal, TextureCoordinate textureCoordinate) {
		// check if full
		if (vertices == VERTEX_COUNT) return false;

		// otherwise
		fbVertices.put(vertex.getArray());
		fbNormals.put(normal.getArray());
		if (textureCoordinate != null) {
			fbTextureCoordinates.put(textureCoordinate.getArray());
		} else {
			fbTextureCoordinates.put(TEXTURECOORDINATE_NONE);
		}
		vertices++;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "BatchVBORenderer [vboIds=" + Arrays.toString(vboIds) + ", id="
				+ id + ", acquired=" + acquired + ", fbVertices=" + fbVertices.position()
				+ ", fbNormals=" + fbNormals.position() + ", fbTextureCoordinates="
				+ fbTextureCoordinates.position() + "]";
	}

}

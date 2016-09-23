package net.drewke.tdme.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;

/**
 * GUI
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIRenderer {

	private final static int QUAD_COUNT = 16384; 

	private GLRenderer renderer;

	private int[] vboIds;

	private int quadCount;
	private FloatBuffer fbVertices = ByteBuffer.allocateDirect(QUAD_COUNT * 4 * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbColors = ByteBuffer.allocateDirect(QUAD_COUNT * 4 * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbTextureCoordinates = ByteBuffer.allocateDirect(QUAD_COUNT * 4 * 2 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	/**
	 * Constructor
	 * @param renderer
	 */
	protected GUIRenderer(GLRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Init
	 */
	protected void init() {
		// initialize if not yet done
		if (vboIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO("tdme.guirenderer", 3);
			vboIds = vboManaged.getVBOGlIds();
		}
	}

	/**
	 * Dispose
	 */
	protected void dispose() {
		// 
		if (vboIds != null) {
			Engine.getInstance().getVBOManager().removeVBO("tdme.guirenderer");
			vboIds = null;
		}
	}

	/**
	 * Clear
	 */
	protected void clear() {
		quadCount = 0;
		fbVertices.clear();
		fbColors.clear();
		fbTextureCoordinates.clear();
	}

	/**
	 * Add quad
	 * 
	 * @param x 1
	 * @param y 1
	 * @param z 1
	 * @param color red 1
	 * @param color green 1
	 * @param color blue 1
	 * @param color alpha 1
	 * @param texture u 1
	 * @param texture v 1
	 * @param x 2
	 * @param y 2
	 * @param z 2
	 * @param color red 2
	 * @param color green 2
	 * @param color blue 2
	 * @param color alpha 2
	 * @param texture u 2
	 * @param texture v 2
	 * @param x 3
	 * @param y 3
	 * @param z 3
	 * @param color red 3
	 * @param color green 3
	 * @param color blue 3
	 * @param color alpha 3
	 * @param texture u 3
	 * @param texture v 3
	 * @param x 4
	 * @param y 4
	 * @param z 4
	 * @param color red 4
	 * @param color green 4
	 * @param color blue 4
	 * @param color alpha 4
	 * @param texture u 4
	 * @param texture v 4
	 */
	protected void addQuad(
		float x1, float y1, float z1,
		float colorR1, float colorG1, float colorB1, float colorA1,
		float tu1, float tv1,
		float x2, float y2, float z2,
		float colorR2, float colorG2, float colorB2, float colorA2,
		float tu2, float tv2,
		float x3, float y3, float z3,
		float colorR3, float colorG3, float colorB3, float colorA3,
		float tu3, float tv3,
		float x4, float y4, float z4,
		float colorR4, float colorG4, float colorB4, float colorA4,
		float tu4, float tv4
	) {
		// check quad count limit
		if (quadCount > QUAD_COUNT) {
			System.out.println("GUIRenderer::addQuad()::too many quads");
			return;
		}

		// quad component 1
		fbVertices.put(x1);
		fbVertices.put(y1);
		fbVertices.put(z1);
		fbColors.put(colorR1);
		fbColors.put(colorG1);
		fbColors.put(colorB1);
		fbColors.put(colorA1);
		fbTextureCoordinates.put(tu1);
		fbTextureCoordinates.put(tv1);

		// quad component 2
		fbVertices.put(x2);
		fbVertices.put(y2);
		fbVertices.put(z2);
		fbColors.put(colorR2);
		fbColors.put(colorG2);
		fbColors.put(colorB2);
		fbColors.put(colorA2);
		fbTextureCoordinates.put(tu2);
		fbTextureCoordinates.put(tv2);

		// quad component 3
		fbVertices.put(x3);
		fbVertices.put(y3);
		fbVertices.put(z3);
		fbColors.put(colorR3);
		fbColors.put(colorG3);
		fbColors.put(colorB3);
		fbColors.put(colorA3);
		fbTextureCoordinates.put(tu3);
		fbTextureCoordinates.put(tv3);

		// quad component 4
		fbVertices.put(x4);
		fbVertices.put(y4);
		fbVertices.put(z4);
		fbColors.put(colorR4);
		fbColors.put(colorG4);
		fbColors.put(colorB4);
		fbColors.put(colorA4);
		fbTextureCoordinates.put(tu4);
		fbTextureCoordinates.put(tv4);

		//
		quadCount++;
	}

	/**
	 * Bind texture
	 * @param texture
	 */
	protected void bindTexture(int textureId) {
		Engine.getGUIShader().bindTexture(renderer, textureId);
	}

	/**
	 * Render 
	 */
	protected void render() {
		fbVertices.flip();
		fbColors.flip();
		fbTextureCoordinates.flip();

		// skip if no vertex data exists
		if (fbVertices.limit() == 0 || fbColors.limit() == 0 || fbTextureCoordinates.limit() == 0) return;

		// determine triangles count
		int triangles = fbVertices.limit() / 3 / 3;

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

		// upload texture coordinates
		renderer.uploadBufferObject(
			vboIds[2],
			fbTextureCoordinates.limit() * Float.SIZE / Byte.SIZE,
			fbTextureCoordinates
		);

		// vertices
		renderer.bindVerticesBufferObject(vboIds[0]);

		// normals
		renderer.bindColorsBufferObject(vboIds[1]);

		// texture coordinates
		renderer.bindTextureCoordinatesBufferObject(vboIds[2]);

		// draw
		renderer.drawTrianglesFromBufferObjects(triangles, 0);
	}

}

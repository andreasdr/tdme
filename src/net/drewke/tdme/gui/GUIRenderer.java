package net.drewke.tdme.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

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

	//
	protected GUI gui;
	private GLRenderer renderer;
	private int[] vboIds;
	private int quadCount;

	// buffers
	private ShortBuffer sbIndices = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * Short.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer();;
	private FloatBuffer fbVertices = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbColors = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbTextureCoordinates = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 2 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	// quad data
	private float[] quadVertices = 
		{
			0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f
		};
	private float[] quadColors = 
		{
			0.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 0.0f
		};
	private float[] quadTextureCoordinates = 
		{
			0.0f, 0.0f,
			0.0f, 0.0f,
			0.0f, 0.0f,
			0.0f, 0.0f
		};

	// font color
	private float[] fontColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

	// effect colors
	private float[] effectColorMul = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] effectColorAdd = new float[] {0.0f, 0.0f, 0.0f, 0.0f};

	/**
	 * Constructor
	 * @param renderer
	 */
	protected GUIRenderer(GUI gui, GLRenderer renderer) {
		this.gui = gui;
		this.renderer = renderer;
	}

	/**
	 * Init
	 */
	protected void init() {
		// initialize if not yet done
		if (vboIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO("tdme.guirenderer", 4);
			vboIds = vboManaged.getVBOGlIds();

			// set up indices
			for (int i = 0; i < QUAD_COUNT; i++) {
				sbIndices.put((short)(i * 4 + 0));
				sbIndices.put((short)(i * 4 + 1));
				sbIndices.put((short)(i * 4 + 2));
				sbIndices.put((short)(i * 4 + 2));
				sbIndices.put((short)(i * 4 + 3));
				sbIndices.put((short)(i * 4 + 0));
			}
			sbIndices.flip();

			// upload indices
			renderer.uploadIndicesBufferObject(
				vboIds[0],
				sbIndices.limit() * Short.SIZE / Byte.SIZE,
				sbIndices
			);
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
	 * Init rendering
	 */
	protected void initRendering() {
		// init gui
		Engine.getGUIShader().useProgram();
		renderer.enableClientState(renderer.CLIENTSTATE_VERTEX_ARRAY);
		renderer.enableClientState(renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);
		renderer.enableClientState(renderer.CLIENTSTATE_COLOR_ARRAY);

		// bind buffer objects
		//	indices
		renderer.bindIndicesBufferObject(vboIds[0]);
		// 	vertices
		renderer.bindVerticesBufferObject(vboIds[1]);
		// 	colors
		renderer.bindColorsBufferObject(vboIds[2]);
		// 	texture coordinates
		renderer.bindTextureCoordinatesBufferObject(vboIds[3]);
	}

	/**
	 * Done rendering
	 */
	protected void doneRendering() {
		// unbind buffers objects
		renderer.unbindBufferObjects();

		// done gui
		renderer.disableClientState(renderer.CLIENTSTATE_VERTEX_ARRAY);
		renderer.disableClientState(renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);
		renderer.disableClientState(renderer.CLIENTSTATE_COLOR_ARRAY);		
		Engine.getGUIShader().unUseProgram();
	}

	/**
	 * Set effect color mul
	 * @param color
	 */
	protected void setFontColor(GUIColor color) {
		System.arraycopy(color.getData(), 0, effectColorMul, 0, 4);
	}

	/**
	 * Set effect color mul
	 * @param color
	 */
	protected void setEffectColorMul(GUIColor color) {
		System.arraycopy(color.getData(), 0, effectColorMul, 0, 4);
	}

	/**
	 * Set effect color add
	 * @param color
	 */
	protected void setEffectColorAdd(GUIColor color) {
		System.arraycopy(color.getData(), 0, effectColorAdd, 0, 4);
	}

	/**
	 * Add quad
	 * 
	 * @param x 1
	 * @param y 1
	 * @param color red 1
	 * @param color green 1
	 * @param color blue 1
	 * @param color alpha 1
	 * @param texture u 1
	 * @param texture v 1
	 * @param x 2
	 * @param y 2
	 * @param color red 2
	 * @param color green 2
	 * @param color blue 2
	 * @param color alpha 2
	 * @param texture u 2
	 * @param texture v 2
	 * @param x 3
	 * @param y 3
	 * @param color red 3
	 * @param color green 3
	 * @param color blue 3
	 * @param color alpha 3
	 * @param texture u 3
	 * @param texture v 3
	 * @param x 4
	 * @param y 4
	 * @param color red 4
	 * @param color green 4
	 * @param color blue 4
	 * @param color alpha 4
	 * @param texture u 4
	 * @param texture v 4
	 */
	protected void addQuad(
		float x1, float y1,
		float colorR1, float colorG1, float colorB1, float colorA1,
		float tu1, float tv1,
		float x2, float y2,
		float colorR2, float colorG2, float colorB2, float colorA2,
		float tu2, float tv2,
		float x3, float y3,
		float colorR3, float colorG3, float colorB3, float colorA3,
		float tu3, float tv3,
		float x4, float y4,
		float colorR4, float colorG4, float colorB4, float colorA4,
		float tu4, float tv4
	) {
		// check quad count limit
		if (quadCount > QUAD_COUNT) {
			System.out.println("GUIRenderer::addQuad()::too many quads");
			return;
		}

		// quad component 1
		int quad = 0;
		quadVertices[quad * 3 + 0] = x1;
		quadVertices[quad * 3 + 1] = y1;
		quadVertices[quad * 3 + 2] = 0.0f;
		quadColors[quad * 4 + 0] = colorR1;
		quadColors[quad * 4 + 1] = colorG1;
		quadColors[quad * 4 + 2] = colorB1;
		quadColors[quad * 4 + 3] = colorA1;
		quadTextureCoordinates[quad * 2 + 0] = tu1;
		quadTextureCoordinates[quad * 2 + 1] = tv1;

		// quad component 2
		quad++;
		quadVertices[quad * 3 + 0] = x2;
		quadVertices[quad * 3 + 1] = y2;
		quadVertices[quad * 3 + 2] = 0.0f;
		quadColors[quad * 4 + 0] = colorR2;
		quadColors[quad * 4 + 1] = colorG2;
		quadColors[quad * 4 + 2] = colorB2;
		quadColors[quad * 4 + 3] = colorA2;
		quadTextureCoordinates[quad * 2 + 0] = tu2;
		quadTextureCoordinates[quad * 2 + 1] = tv2;

		// quad component 3
		quad++;
		quadVertices[quad * 3 + 0] = x3;
		quadVertices[quad * 3 + 1] = y3;
		quadVertices[quad * 3 + 2] = 0.0f;
		quadColors[quad * 4 + 0] = colorR3;
		quadColors[quad * 4 + 1] = colorG3;
		quadColors[quad * 4 + 2] = colorB3;
		quadColors[quad * 4 + 3] = colorA3;
		quadTextureCoordinates[quad * 2 + 0] = tu3;
		quadTextureCoordinates[quad * 2 + 1] = tv3;


		// quad component 4
		quad++;
		quadVertices[quad * 3 + 0] = x4;
		quadVertices[quad * 3 + 1] = y4;
		quadVertices[quad * 3 + 2] = 0.0f;
		quadColors[quad * 4 + 0] = colorR4;
		quadColors[quad * 4 + 1] = colorG4;
		quadColors[quad * 4 + 2] = colorB4;
		quadColors[quad * 4 + 3] = colorA4;
		quadTextureCoordinates[quad * 2 + 0] = tu4;
		quadTextureCoordinates[quad * 2 + 1] = tv4;

		// put quads to direct float buffers
		fbVertices.put(quadVertices);
		fbColors.put(quadColors);
		fbTextureCoordinates.put(quadTextureCoordinates);

		//
		quadCount++;
	}

	/**
	 * Bind texture
	 * @param texture
	 */
	protected void bindTexture(int textureId) {
		renderer.bindTexture(textureId);
	}

	/**
	 * Render 
	 */
	protected void render() {
		// skip if no vertex data exists
		if (quadCount == 0) return;

		//
		fbVertices.flip();
		fbColors.flip();
		fbTextureCoordinates.flip();

		// upload vertices
		renderer.uploadBufferObject(
			vboIds[1],
			fbVertices.limit() * Float.SIZE / Byte.SIZE,
			fbVertices
		);

		// upload colors
		renderer.uploadBufferObject(
			vboIds[2],
			fbColors.limit() * Float.SIZE / Byte.SIZE,
			fbColors
		);

		// upload texture coordinates
		renderer.uploadBufferObject(
			vboIds[3],
			fbTextureCoordinates.limit() * Float.SIZE / Byte.SIZE,
			fbTextureCoordinates
		);

		// effect
		renderer.setEffectColorMul(effectColorMul);
		renderer.setEffectColorAdd(effectColorAdd);
		renderer.onUpdateEffect();

		// draw
		renderer.drawIndexedTrianglesFromBufferObjects(quadCount * 2, 0);

		// reset
		quadCount = 0;
		fbVertices.clear();
		fbColors.clear();
		fbTextureCoordinates.clear();
		System.arraycopy(GUIColor.WHITE.getData(), 0, effectColorMul, 0, 4);
	}

}

package net.drewke.tdme.gui.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.gui.GUI;
import net.drewke.tdme.gui.nodes.GUIColor;

/**
 * GUI
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIRenderer {

	private final static int QUAD_COUNT = 16384; 

	//
	protected GUI gui;

	//
	private GLRenderer renderer;
	private int[] vboIds;
	private int quadCount;

	// buffers
	private ShortBuffer sbIndices = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * Short.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer();;
	private FloatBuffer fbVertices = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbColors = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer fbTextureCoordinates = ByteBuffer.allocateDirect(QUAD_COUNT * 6 * 2 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	// render area
	private float renderAreaLeft = 0f;
	private float renderAreaTop = 0f;
	private float renderAreaRight = 0f;
	private float renderAreaBottom = 0f;

	// render offset
	private float renderOffsetX = 0f;
	private float renderOffsetY = 0f;

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

	// effect color multiplication final
	private float[] effectColorMulFinal = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
	/**
	 * Constructor
	 * @param renderer
	 */
	public GUIRenderer(GLRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Set GUI
	 * @param gui
	 */
	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	/**
	 * @return GUI
	 */
	public GUI getGUI() {
		return gui;
	}

	/**
	 * Init
	 */
	public void init() {
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
	public void dispose() {
		// 
		if (vboIds != null) {
			Engine.getInstance().getVBOManager().removeVBO("tdme.guirenderer");
			vboIds = null;
		}
	}

	/**
	 * Init rendering
	 */
	public void initRendering() {
		// render to full screen
		setRenderAreaLeft(-1f);
		setRenderAreaTop(+1);
		setRenderAreaRight(+1);
		setRenderAreaBottom(-1f);

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
	public void doneRendering() {
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
	public void setFontColor(GUIColor color) {
		System.arraycopy(color.getData(), 0, fontColor, 0, 4);
	}

	/**
	 * Set effect color mul
	 * @param color
	 */
	public void setEffectColorMul(GUIColor color) {
		System.arraycopy(color.getData(), 0, effectColorMul, 0, 4);
	}

	/**
	 * Set effect color add
	 * @param color
	 */
	public void setEffectColorAdd(GUIColor color) {
		System.arraycopy(color.getData(), 0, effectColorAdd, 0, 4);
	}

	/**
	 * @return render area left
	 */
	public float getRenderAreaLeft() {
		return renderAreaLeft;
	}

	/**
	 * Set up render area left
	 * @param render area left
	 */
	public void setRenderAreaLeft(float renderAreaLeft) {
		this.renderAreaLeft = renderAreaLeft;
	}

	/**
	 * Set sub render area left
	 * @param render area left
	 */
	public void setSubRenderAreaLeft(float renderAreaLeft) {
		this.renderAreaLeft = renderAreaLeft > this.renderAreaLeft?renderAreaLeft:this.renderAreaLeft;
	}

	/**
	 * @return render area top
	 */
	public float getRenderAreaTop() {
		return renderAreaTop;
	}

	/**
	 * Set up render area top
	 * @param render area top
	 */
	public void setRenderAreaTop(float renderAreaTop) {
		this.renderAreaTop = renderAreaTop;
	}

	/**
	 * Set sub render area top
	 * @param render area top
	 */
	public void setSubRenderAreaTop(float renderAreaTop) {
		this.renderAreaTop = renderAreaTop < this.renderAreaTop?renderAreaTop:this.renderAreaTop;
	}

	/**
	 * @return render area right
	 */
	public float getRenderAreaRight() {
		return renderAreaRight;
	}

	/**
	 * Set up render area right
	 * @param render area right
	 */
	public void setRenderAreaRight(float renderAreaRight) {
		this.renderAreaRight = renderAreaRight;
	}

	/**
	 * Set sub render area right
	 * @param render area right
	 */
	public void setSubRenderAreaRight(float renderAreaRight) {
		this.renderAreaRight = renderAreaRight < this.renderAreaRight?renderAreaRight:this.renderAreaRight;
	}

	/**
	 * @return render area bottom
	 */
	public float getRenderAreaBottom() {
		return renderAreaBottom;
	}

	/**
	 * Set up render area bottom
	 * @param render area bottom
	 */
	public void setRenderAreaBottom(float renderAreaBottom) {
		this.renderAreaBottom = renderAreaBottom;
	}

	/**
	 * Set sub render area bottom
	 * @param render area bottom
	 */
	public void setSubRenderAreaBottom(float renderAreaBottom) {
		this.renderAreaBottom = renderAreaBottom > this.renderAreaBottom?renderAreaBottom:this.renderAreaBottom;
	}

	/**
	 * @return render offset x
	 */
	public float getRenderOffsetX() {
		return renderOffsetX;
	}

	/**
	 * Set render offset x
	 * @param render offset x
	 */
	public void setRenderOffsetX(float renderOffsetX) {
		this.renderOffsetX = renderOffsetX;
	}

	/**
	 * @return render offset y
	 */
	public float getRenderOffsetY() {
		return renderOffsetY;
	}

	/**
	 * Set render offset y
	 * @param render offset y
	 */
	public void setRenderOffsetY(float renderOffsetY) {
		this.renderOffsetY = renderOffsetY;
	}

	/**
	 * Add quad
	 *
	 * 	Note: quad vertices order
	 * 
	 * 		1    2
	 * 		+----+
	 * 		|    |
	 * 		|    |
	 * 		+----+
	 * 		4    3
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
	public void addQuad(
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

		// take render offset x and y into account
		x1-= renderOffsetX;
		x2-= renderOffsetX;
		x3-= renderOffsetX;
		x4-= renderOffsetX;
		y1+= renderOffsetY;
		y2+= renderOffsetY;
		y3+= renderOffsetY;
		y4+= renderOffsetY;

		// Note: 
		//	top = +1, bottom = -1 
		//	left = -1, right = +1
		// check if quad bottom is > render area top
		float quadBottom = y3;
		if (quadBottom > renderAreaTop) {
			// System.out.println("GUIRenderer::addQuad()::not in viewport(quadBottom > renderAreaTop)");
			return;			
		} 

		// check if quad top is < render area bottom
		float quadTop = y1;
		if (quadTop < renderAreaBottom) {
			// System.out.println("GUIRenderer::addQuad()::not in viewport(quadTop < renderAreaBottom)");
			return;			
		}

		// check if quad left > render area right
		float quadLeft = x1;
		if (quadLeft > renderAreaRight) {
			// System.out.println("GUIRenderer::addQuad()::not in viewport(quadLeft > renderAreaRight)");
			return;			
		} 

		// check if quad right < render area left
		float quadRight = x2;
		if (quadRight < renderAreaLeft) {
			// System.out.println("GUIRenderer::addQuad()::not in viewport(quadRight < renderAreaLeft)");
			return;			
		} 

		// clip 3,4 y values
		if (quadBottom < renderAreaBottom) {
			tv3 = tv1 + ((tv3 - tv1) * ((y1 - renderAreaBottom) / (y1 - y3)));
			tv4 = tv2 + ((tv4 - tv2) * ((y2 - renderAreaBottom) / (y1 - y4)));
			y3 = renderAreaBottom;
			y4 = renderAreaBottom;
		}

		// clip 1,2 y values
		if (quadTop > renderAreaTop) {
			tv1 = tv1 + ((tv3 - tv1) * ((y1 - renderAreaTop) / (y1 - y3)));
			tv2 = tv2 + ((tv4 - tv2) * ((y2 - renderAreaTop) / (y1 - y4)));
			y1 = renderAreaTop;
			y2 = renderAreaTop;
		}

		// clip 1,4 y values
		if (quadLeft < renderAreaLeft) {
			tu1 = tu1 + ((tu2 - tu1) * ((renderAreaLeft - x1) / (x2 - x1)));
			tu4 = tu4 + ((tu3 - tu4) * ((renderAreaLeft - x4) / (x3 - x4)));
			x1 = renderAreaLeft;
			x4 = renderAreaLeft;
		}

		// clip 1,4 y values
		if (quadRight > renderAreaRight) {
			tu2 = tu2 - ((tu2 - tu1) * ((x2 - renderAreaRight) / (x2 - x1)));
			tu3 = tu3 - ((tu3 - tu4) * ((x3 - renderAreaRight) / (x3 - x4)));
			x2 = renderAreaRight;
			x3 = renderAreaRight;
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

		// put quad to direct float buffers
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
	public void bindTexture(int textureId) {
		renderer.bindTexture(textureId);
	}

	/**
	 * Render 
	 */
	public void render() {
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
		effectColorMulFinal[0] = effectColorMul[0] * fontColor[0];
		effectColorMulFinal[1] = effectColorMul[1] * fontColor[1];
		effectColorMulFinal[2] = effectColorMul[2] * fontColor[2];
		effectColorMulFinal[3] = effectColorMul[3] * fontColor[3];
		renderer.setEffectColorMul(effectColorMulFinal);
		renderer.setEffectColorAdd(effectColorAdd);
		renderer.onUpdateEffect();

		// draw
		renderer.drawIndexedTrianglesFromBufferObjects(quadCount * 2, 0);

		// reset
		quadCount = 0;
		fbVertices.clear();
		fbColors.clear();
		fbTextureCoordinates.clear();
		System.arraycopy(GUIColor.WHITE.getData(), 0, fontColor, 0, 4);
		System.arraycopy(GUIColor.WHITE.getData(), 0, effectColorMul, 0, 4);
		System.arraycopy(GUIColor.BLACK.getData(), 0, effectColorAdd, 0, 4);
		effectColorAdd[3] = 0;
	}

}

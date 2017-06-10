package net.drewke.tdme.engine;


/**
 * Frame buffer class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class FrameBuffer {

	public final static int FRAMEBUFFER_DEPTHBUFFER = 1;
	public final static int FRAMEBUFFER_COLORBUFFER = 2;

	private int width;
	private int height;
	private int frameBufferId;
	private int depthBufferTextureId;
	private int colorBufferTextureId;
	private int buffers;

	/**
	 * Public constructor
	 * @param engine
	 * @param width
	 * @param height
	 * @param buffers (see FrameBuffer::FRAMEBUFFER_*)
	 */
	public FrameBuffer(int width, int height, int buffers) {
		this.width = width;
		this.height = height;
		this.buffers = buffers;
		frameBufferId = -1;
		depthBufferTextureId = Engine.renderer.ID_NONE;
		colorBufferTextureId = Engine.renderer.ID_NONE;
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
	 * Init the frame buffer
	 * 	currently only depth buffer rendering is enabled, can be extended to color buffer easily
	 * @param gl
	 */
	public void initialize() {
		if ((buffers & FRAMEBUFFER_DEPTHBUFFER) == FRAMEBUFFER_DEPTHBUFFER) depthBufferTextureId = Engine.renderer.createDepthBufferTexture(width, height);
		if ((buffers & FRAMEBUFFER_COLORBUFFER) == FRAMEBUFFER_COLORBUFFER) colorBufferTextureId = Engine.renderer.createColorBufferTexture(width, height);
		frameBufferId = Engine.renderer.createFramebufferObject(depthBufferTextureId, colorBufferTextureId);
	}

	/**
	 * Resize the frame buffer
	 * @param gl
	 */
	public void reshape(int width, int height) {
		if ((buffers & FRAMEBUFFER_DEPTHBUFFER) == FRAMEBUFFER_DEPTHBUFFER) Engine.renderer.resizeDepthBufferTexture(depthBufferTextureId, width, height);
		if ((buffers & FRAMEBUFFER_COLORBUFFER) == FRAMEBUFFER_COLORBUFFER) Engine.renderer.resizeColorBufferTexture(colorBufferTextureId, width, height);
		this.width = width;
		this.height = height;
	}

	/**
	 * Disposes this frame buffer
	 * @param gl
	 */
	public void dispose() {
		if ((buffers & FRAMEBUFFER_DEPTHBUFFER) == FRAMEBUFFER_DEPTHBUFFER) Engine.renderer.disposeTexture(depthBufferTextureId);
		if ((buffers & FRAMEBUFFER_COLORBUFFER) == FRAMEBUFFER_COLORBUFFER) Engine.renderer.disposeTexture(colorBufferTextureId);
		Engine.renderer.disposeFrameBufferObject(frameBufferId);
	}

	/**
	 * Enables this frame buffer to be rendered
	 * @param gl
	 */
	public void enableFrameBuffer() {
		Engine.renderer.bindFrameBuffer(frameBufferId);
		Engine.renderer.setViewPort(0, 0, width, height);
		Engine.renderer.updateViewPort();
	}

	/**
	 * Switches back to non offscreen main frame buffer to be rendered
	 * @param gl
	 */
	public static void disableFrameBuffer() {
		Engine.renderer.bindFrameBuffer(Engine.renderer.FRAMEBUFFER_DEFAULT);
		Engine.renderer.setViewPort(0, 0, Engine.instance.getWidth(), Engine.instance.getHeight());
		Engine.renderer.updateViewPort();
	}

	/**
	 * Bind depth texture
	 * @param gl
	 */
	public void bindDepthBufferTexture() {
		Engine.renderer.bindTexture(depthBufferTextureId);
	}

	/**
	 * @return color buffer texture id
	 */
	public int getColorBufferTextureId() {
		return colorBufferTextureId;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "FrameBuffer [width=" + width + ", height=" + height
				+ ", frameBufferId=" + frameBufferId
				+ ", depthBufferTextureId=" + depthBufferTextureId
				+ ", colorBufferTextureId=" + colorBufferTextureId
				+ ", buffers=" + buffers + "]";
	}

}

/*

Copyright (c) 2012, Jens Hohmuth
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.

*/

package net.drewke.tdme.engine.fileio.textures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

/**
 * Taken from Nifty-GUI 1.3.2 http://nifty-gui.lessvoid.com
 * This has been slightly modified by me.
 * 
 * @author kevin, Andreas Drewke
 */
public final class ImageIO extends ImageLoader implements Texture {

	private static final ColorModel glAlphaColorModel = new ComponentColorModel(
		ColorSpace.getInstance(ColorSpace.CS_sRGB),
		new int[] { 8, 8, 8, 8 }, true, false,
		ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE
	);

	private static final ColorModel glColorModel = new ComponentColorModel(
		ColorSpace.getInstance(ColorSpace.CS_sRGB),
		new int[] { 8, 8, 8, 0 }, false, false, ComponentColorModel.OPAQUE,
		DataBuffer.TYPE_BYTE
	);

	private String id;
	private int depth;
	private int height;
	private int width;
	private int texWidth;
	private int texHeight;
	private boolean edging = true;
	private ByteBuffer data;

	/**
	 * Loads a texture with given id from given input stream
	 * @param id
	 * @param input stream
	 * @return
	 * @throws IOException
	 */
	public static Texture loadTexture(String id, InputStream is) throws IOException {
		ImageIO imageIO = new ImageIO(id);
		imageIO.loadImage(is);
		return imageIO;
	}

	/**
	 * Public constructor
	 * @param id
	 */
	public ImageIO(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getDepth()
	 */
	public int getDepth() {
		return depth;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureHeight()
	 */
	public int getTextureHeight() {
		return texHeight;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureWidth()
	 */
	public int getTextureWidth() {
		return texWidth;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Loads an image
	 * @param fis
	 * @throws IOException
	 */
	public void  loadImage(InputStream fis) throws IOException {
		loadImage(fis, false, null);
	}

	/**
	 * Loads an image
	 * @param fis
	 * @param flipped
	 * @param transparent
	 * @return
	 * @throws IOException
	 */
	public void loadImage(InputStream fis, boolean flipped, int[] transparent) throws IOException {
		loadImage(fis, flipped, false, transparent);
	}

	/**
	 * Loads an image
	 * @param fis
	 * @param flipped
	 * @param forceAlpha
	 * @param transparent
	 * @throws IOException
	 */
	public void loadImage(InputStream fis, boolean flipped, boolean forceAlpha, int[] transparent) throws IOException {
		if (transparent != null) {
			forceAlpha = true;
		}

		BufferedImage bufferedImage = javax.imageio.ImageIO.read(fis);
		data = imageToByteBuffer(
			bufferedImage,
			flipped,
			forceAlpha,
			transparent,
			true,
			false
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureData()
	 */
	public ByteBuffer getTextureData() {
		return data.slice();
	}

	/**
	 * Loads an image to byte buffer
	 * @param image
	 * @param flipped
	 * @param forceAlpha
	 * @param transparent
	 * @param powerOfTwoSupport
	 * @param modeARGB
	 * @return byte buffer
	 */
	private ByteBuffer imageToByteBuffer(BufferedImage image, boolean flipped,
		boolean forceAlpha, int[] transparent, boolean powerOfTwoSupport,
		boolean modeARGB) {
		//
		ByteBuffer imageBuffer = null;
		WritableRaster raster;
		BufferedImage texImage;

		int texWidth = image.getWidth();
		int texHeight = image.getHeight();

		if (powerOfTwoSupport) {
			// find the closest power of 2 for the width and height
			// of the produced texture
			texWidth = 2;
			texHeight = 2;

			while (texWidth < image.getWidth()) {
				texWidth *= 2;
			}
			while (texHeight < image.getHeight()) {
				texHeight *= 2;
			}
		}

		this.width = image.getWidth();
		this.height = image.getHeight();
		this.texHeight = texHeight;
		this.texWidth = texWidth;

		// create a raster that can be used by OpenGL as a source
		// for a texture
		boolean useAlpha = image.getColorModel().hasAlpha() || forceAlpha;

		if (useAlpha) {
			depth = 32;
			raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE,
				texWidth,
				texHeight,
				4,
				null
			);
			texImage = new BufferedImage(
				glAlphaColorModel,
				raster,
				false,
				new Hashtable()
			);
		} else {
			depth = 24;
			raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE,
				texWidth,
				texHeight,
				3,
				null
			);
			texImage = new BufferedImage(
				glColorModel,
				raster,
				false,
				new Hashtable()
			);
		}

		// copy the source image into the produced image
		Graphics2D g = (Graphics2D) texImage.getGraphics();

		// only need to blank the image for mac compatibility if we're using
		// alpha
		if (useAlpha) {
			g.setColor(new Color(0f, 0f, 0f, 0f));
			g.fillRect(0, 0, texWidth, texHeight);
		}

		if (flipped) {
			g.scale(1, -1);
			g.drawImage(image, 0, -height, null);
		} else {
			g.drawImage(image, 0, 0, null);
		}

		if (edging) {
			if (height < texHeight - 1) {
				copyArea(texImage, 0, 0, width, 1, 0, texHeight - 1);
				copyArea(texImage, 0, height - 1, width, 1, 0, 1);
			}
			if (width < texWidth - 1) {
				copyArea(texImage, 0, 0, 1, height, texWidth - 1, 0);
				copyArea(texImage, width - 1, 0, 1, height, 1, 0);
			}
		}

		// build a byte buffer from the temporary image
		// that be used by OpenGL to produce a texture.
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
		if (transparent != null) {
			for (int i = 0; i < data.length; i += 4) {
				boolean match = true;
				for (int c = 0; c < 3; c++) {
					int value = data[i + c] < 0 ? 256 + data[i + c] : data[i + c];
					if (value != transparent[c]) {
						match = false;
					}
				}
				if (match) {
					data[i + 3] = 0;
				}
			}
		}
		if (modeARGB) {
			for (int i = 0; i < data.length; i += 4) {
				byte rr = data[i + 0];
				byte gg = data[i + 1];
				byte bb = data[i + 2];
				byte aa = data[i + 3];
				data[i + 0] = bb;
				data[i + 1] = gg;
				data[i + 2] = rr;
				data[i + 3] = aa;
			}
		}

		imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();
		g.dispose();

		return imageBuffer;
	}

	/**
	 * Copies an area
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	private void copyArea(BufferedImage image, int x, int y, int width, int height, int dx, int dy) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.drawImage(image.getSubimage(x, y, width, height), x + dx, y + dy, null);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ImageIO [id=" + id + ", depth=" + depth + ", texWidth="
				+ texWidth + ", texHeight=" + texHeight + "]";
	}

}
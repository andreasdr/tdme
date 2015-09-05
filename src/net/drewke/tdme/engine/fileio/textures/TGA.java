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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jogamp.common.nio.Buffers;

/**
 * Taken from Nifty-GUI 1.3.2 http://nifty-gui.lessvoid.com This has been
 * slightly modified by me
 * 
 * @author Kevin Glass, Julien Gouesse (port to JOGL 2), Andreas Drewke
 */
public final class TGA extends ImageLoader implements Texture {

	private String id;

	/** The width of the texture that needs to be generated */
	private int texWidth;

	/** The height of the texture that needs to be generated */
	private int texHeight;

	/** The width of the TGA image */
	private int width;

	/** The height of the TGA image */
	private int height;

	/** The bit depth of the image */
	private short pixelDepth;

	/** Image data */
	private ByteBuffer data;

	/**
	 * Loads a texture with given id from given input stream
	 * @param id
	 * @param input stream
	 * @return
	 * @throws IOException
	 */
	public static Texture loadTexture(String id, InputStream is) throws IOException {
		TGA tga = new TGA(id);
		tga.loadImage(is);
		return tga;
	}

	/**
	 * Public constructor
	 */
	public TGA(String id) {
		this.id = id;
	}

	/**
	 * Flip the endian-ness of the short
	 * 
	 * @param signedShort The short to flip
	 * @return The flipped short
	 */
	private short flipEndian(short signedShort) {
		int input = signedShort & 0xFFFF;
		return (short) (input << 8 | (input & 0xFF00) >>> 8);
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
		return pixelDepth;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getWidth()
	 */
	public int getWidth() {
		return width;
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
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureWidth()
	 */
	public int getTextureWidth() {
		return texWidth;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureHeight()
	 */
	public int getTextureHeight() {
		return texHeight;
	}

	/**
	 * loads an image
	 * @param 
	 * @throws IOException
	 */
	public void loadImage(InputStream fis) throws IOException {
		loadImage(fis, false, null);
	}

	/**
	 * loads an image
	 * @param fis
	 * @param flipped
	 * @param transparent
	 * @return
	 * @throws IOException
	 */
	public void loadImage(InputStream fis, boolean flipped, int[] transparent) throws IOException {
		loadImage(fis, flipped, true, transparent);
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
		byte red = 0;
		byte green = 0;
		byte blue = 0;
		byte alpha = 0;

		BufferedInputStream bis = new BufferedInputStream(fis, 100000);
		DataInputStream dis = new DataInputStream(bis);

		// Read in the Header
		short idLength = (short) dis.read();
		short colorMapType = (short) dis.read();
		short imageType = (short) dis.read();
		short cMapStart = flipEndian(dis.readShort());
		short cMapLength = flipEndian(dis.readShort());
		short cMapDepth = (short) dis.read();
		short xOffset = flipEndian(dis.readShort());
		short yOffset = flipEndian(dis.readShort());

		width = flipEndian(dis.readShort());
		height = flipEndian(dis.readShort());
		pixelDepth = (short) dis.read();
		if (pixelDepth == 32) {
			forceAlpha = false;
		}

		texWidth = get2Fold(width);
		texHeight = get2Fold(height);

		short imageDescriptor = (short) dis.read();
		if ((imageDescriptor & 0x0020) == 0) {
			flipped = !flipped;
		}

		// Skip image ID
		if (idLength > 0) {
			bis.skip(idLength);
		}

		byte[] rawData = null;
		if ((pixelDepth == 32) || (forceAlpha)) {
			pixelDepth = 32;
			rawData = new byte[texWidth * texHeight * 4];
		} else if (pixelDepth == 24) {
			rawData = new byte[texWidth * texHeight * 3];
		} else {
			throw new RuntimeException("Only 24 and 32 bit TGAs are supported");
		}

		if (pixelDepth == 24) {
			if (flipped) {
				for (int i = height - 1; i >= 0; i--) {
					for (int j = 0; j < width; j++) {
						blue = dis.readByte();
						green = dis.readByte();
						red = dis.readByte();

						int ofs = ((j + (i * texWidth)) * 3);
						rawData[ofs] = red;
						rawData[ofs + 1] = green;
						rawData[ofs + 2] = blue;
					}
				}
			} else {
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						blue = dis.readByte();
						green = dis.readByte();
						red = dis.readByte();

						int ofs = ((j + (i * texWidth)) * 3);
						rawData[ofs] = red;
						rawData[ofs + 1] = green;
						rawData[ofs + 2] = blue;
					}
				}
			}
		} else if (pixelDepth == 32) {
			if (flipped) {
				for (int i = height - 1; i >= 0; i--) {
					for (int j = 0; j < width; j++) {
						blue = dis.readByte();
						green = dis.readByte();
						red = dis.readByte();
						if (forceAlpha) {
							alpha = (byte) 255;
						} else {
							alpha = dis.readByte();
						}

						int ofs = ((j + (i * texWidth)) * 4);

						rawData[ofs] = red;
						rawData[ofs + 1] = green;
						rawData[ofs + 2] = blue;
						rawData[ofs + 3] = alpha;

						if (alpha == 0) {
							rawData[ofs + 2] = (byte) 0;
							rawData[ofs + 1] = (byte) 0;
							rawData[ofs] = (byte) 0;
						}
					}
				}
			} else {
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						blue = dis.readByte();
						green = dis.readByte();
						red = dis.readByte();
						if (forceAlpha) {
							alpha = (byte) 255;
						} else {
							alpha = dis.readByte();
						}

						int ofs = ((j + (i * texWidth)) * 4);

						if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
							rawData[ofs] = red;
							rawData[ofs + 1] = green;
							rawData[ofs + 2] = blue;
							rawData[ofs + 3] = alpha;
						} else {
							rawData[ofs] = red;
							rawData[ofs + 1] = green;
							rawData[ofs + 2] = blue;
							rawData[ofs + 3] = alpha;
						}

						if (alpha == 0) {
							rawData[ofs + 2] = 0;
							rawData[ofs + 1] = 0;
							rawData[ofs] = 0;
						}
					}
				}
			}
		}
		fis.close();

		if (transparent != null) {
			for (int i = 0; i < rawData.length; i += 4) {
				boolean match = true;
				for (int c = 0; c < 3; c++) {
					if (rawData[i + c] != transparent[c]) {
						match = false;
					}
				}

				if (match) {
					rawData[i + 3] = 0;
				}
			}
		}

		// Get a pointer to the image memory
		data = Buffers.newDirectByteBuffer(rawData.length);
		data.put(rawData);

		int perPixel = pixelDepth / 8;
		if (height < texHeight - 1) {
			int topOffset = (texHeight - 1) * (texWidth * perPixel);
			int bottomOffset = (height - 1) * (texWidth * perPixel);
			for (int x = 0; x < texWidth * perPixel; x++) {
				data.put(topOffset + x, data.get(x));
				data.put(
					bottomOffset + (texWidth * perPixel) + x,
					data.get((texWidth * perPixel) + x)
				);
			}
		}
		if (width < texWidth - 1) {
			for (int y = 0; y < texHeight; y++) {
				for (int i = 0; i < perPixel; i++) {
					data.put(
						((y + 1) * (texWidth * perPixel)) - perPixel + i,
						data.get(y * (texWidth * perPixel) + i)
					);
					data.put(
						(y * (texWidth * perPixel)) + (width * perPixel) + i,
						data.get((y * (texWidth * perPixel)) + ((width - 1) * perPixel) + i)
					);
				}
			}
		}

		data.flip();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.assets.textures.Texture#getTextureData()
	 */
	public ByteBuffer getTextureData() {
		return data.slice();
	}

	/**
	 * Get the closest greater power of 2 to the fold number
	 * 
	 * @param fold The target number
	 * @return The power of 2
	 */
	private int get2Fold(int fold) {
		int ret = 2;
		while (ret < fold) {
			ret *= 2;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "TGA [id=" + id + ", texWidth=" + texWidth + ", texHeight="
				+ texHeight + ", pixelDepth=" + pixelDepth + "]";
	}

}

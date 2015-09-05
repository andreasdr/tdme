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

import java.nio.ByteBuffer;

/**
 * Taken from Nifty-GUI 1.3.2 http://nifty-gui.lessvoid.com
 * This has been slightly modified by me
 * 
 * @version $Id$
 * @author Nifty GUI authors, Andreas Drewke
 */
public interface Texture {

	/**
	 * @return id
	 */
	public String getId();

	/**
	 * @return depth in bits per pixel
	 */
	public int getDepth();

	/**
	 * @return image width
	 */
	public int getWidth();

	/**
	 * @return image height
	 */
	public int getHeight();

	/**
	 * @return texture height
	 */
	public int getTextureHeight();

	/**
	 * @return texture width
	 */
	public int getTextureWidth();

	/**
	 * @return texture data wrapped in a byte buffer
	 */
	public ByteBuffer getTextureData();

}
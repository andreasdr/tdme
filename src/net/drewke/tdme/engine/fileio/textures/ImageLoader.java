package net.drewke.tdme.engine.fileio.textures;

import java.io.IOException;
import java.io.InputStream;

/**
 * Image loader implementation base class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class ImageLoader {

	/**
	 * Loads a texture
	 * @param id
	 * @param input stream
	 * @return texture
	 * @throws IOException
	 */
	public static Texture loadTexture(String id, InputStream is) throws IOException {
		return null;
	}

}

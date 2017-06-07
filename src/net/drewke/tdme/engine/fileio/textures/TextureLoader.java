package net.drewke.tdme.engine.fileio.textures;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * ImageLoader loader class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class TextureLoader {

	private static HashMap<String, Class> imageLoaders;

	/**
	 * Loads a texture
	 * @param path
	 * @param fileName
	 * @return texture data instance or null
	 */
	public static Texture loadTexture(String path, String fileName) {
		InputStream is = null;
		try {
			// determine image loader
			String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
			Class<ImageLoader> imageLoader = imageLoaders.get(extension);
			if (imageLoader == null) imageLoader = imageLoaders.get("*");

			// exit if none chosed 
			if (imageLoader == null) return null;

			// invoke load texture method
			Method loadTexture = imageLoader.getDeclaredMethod("loadTexture", new Class[] {String.class, InputStream.class});
			return (Texture)loadTexture.invoke(null, new Object[] {path + File.separator + fileName, FileSystem.getInstance().getInputStream(path, fileName)});
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception closeException) {}
			}
		}
	}

	/**
	 * Adds an image loader
	 * @param extension
	 * @param image loader
	 */
	public static void addImageLoader(String extension, Class imageLoader) {
		imageLoaders.put(extension, imageLoader);
	}

	// static initialization code
	static {
		imageLoaders = new HashMap<String, Class>();
		imageLoaders.put("tga", TGA.class);
		imageLoaders.put("*", net.drewke.tdme.engine.fileio.textures.ImageIO.class);
	}

}

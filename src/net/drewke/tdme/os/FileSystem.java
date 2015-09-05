package net.drewke.tdme.os;

/**
 * File system 
 * @author Andreas Drewke
 * @version $Id$
 */
public class FileSystem {

	private static FileSystemInterface fileSystem = null;

	/**
	 * Singleton instance to retrieve file system
	 * 	will use standard file system by default if not set up different explicitly
	 * @return
	 */
	public static FileSystemInterface getInstance() {
		if (fileSystem == null) {
			fileSystem = new StandardFileSystem();
		}
		return fileSystem;
	}

	/**
	 * Set up file system
	 * @param file system
	 */
	public static void setupFileSystem(FileSystemInterface fileSystem) {
		FileSystem.fileSystem = fileSystem; 
	}

}

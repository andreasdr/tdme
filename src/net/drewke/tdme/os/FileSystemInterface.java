package net.drewke.tdme.os;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface to file system
 * @author Andreas Drewke
 * @version $Id$
 */
public interface FileSystemInterface {

	/**
	 * Creates input stream by given path name and file name
	 * @param path
	 * @param file name
	 * @return input stream
	 * @throws IOException
	 */
	public InputStream getInputStream(String path, String fileName) throws IOException;

	/**
	 * Get file content
	 * @param path
	 * @param file name
	 * @throws IOException
	 */
	public String getContent(String path, String fileName) throws IOException;

	/**
	 * List files for given path and filter by a file name filter if not null 
	 * @param path
	 * @param filter or null
	 * @return file names 
	 */
	public String[] list(String path, FilenameFilter filter) throws IOException;

}

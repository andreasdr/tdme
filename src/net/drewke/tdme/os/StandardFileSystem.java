package net.drewke.tdme.os;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class StandardFileSystem implements FileSystemInterface {

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.os.FileSystemInterface#getInputStream(java.lang.String, java.lang.String)
	 */
	public InputStream getInputStream(String path, String fileName) throws IOException {
		// we only support unix style path names in jar files
		path = path.replace('\\', '/');
		// delete current working dir from path name to be able to use jar files
		String currentPath = new File("").getCanonicalFile().toString().replace('\\', '/');
		if (path.startsWith(currentPath)) {
			path = path.substring(currentPath.length() + 1); 
		}
		String _fileName = path + File.separator + fileName;

		// check file system first
		try {
			return new FileInputStream(_fileName);
		} catch (IOException ioe) {
			// no op
		}

		// check tdme jar next
		InputStream is;
		is = this.getClass().getResourceAsStream(path + "/" + fileName);
		if (is != null) return is;
		is = this.getClass().getClassLoader().getResourceAsStream(path + "/" + fileName);
		if (is != null) return is;
		throw new FileNotFoundException(path + "/" + fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.os.FileSystemInterface#listFiles(java.lang.String, java.io.FilenameFilter)
	 */
	public String[] list(String path, FilenameFilter filter) throws IOException {
		ArrayList<String> files = new ArrayList<String>();

		// list files in file system
		String[] fileSystemFiles = new File(path).list(filter);
		if (fileSystemFiles != null) {
			for (String fileName: fileSystemFiles) {
				files.add(fileName);
			}
		}

		// list file in associated jar from calling class
		try {
			// we only support unix style path names in jar files
			path = path.replace('\\', '/');

			//
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			CodeSource src = Class.forName(stackTraceElements[2].getClassName()).getProtectionDomain().getCodeSource();
	
			if (src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				while (true) {
					ZipEntry e = zip.getNextEntry();
					if (e == null) break;
					String name = e.getName();
					if (name.startsWith(path)) {
						String fileName = name.substring(path.length() + 1);
						if (filter.accept(new File(path), fileName)) files.add(fileName);
					}
				}
			}
		} catch (ClassNotFoundException cnfe) {
			//
		}


		// remove duplicate entries
		ArrayList<String> filesNoDuplicates = new ArrayList<String>();
		for (String file: files) {
			boolean duplicate = false;
			for (String _file: filesNoDuplicates) {
				if (file.equals(_file)) {
					duplicate = true;
					break;
				}
			}
			if (duplicate == false) filesNoDuplicates.add(file);
		}

		// 
		String[] _files = new String[filesNoDuplicates.size()];
		filesNoDuplicates.toArray(_files);
		return _files;
	}

}

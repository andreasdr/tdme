package net.drewke.tdme.tools.shared.controller;

/**
 * File dialog path
 * @author Andreas Drewke
 * @version $Id$
 */
public class FileDialogPath {

	private String path;

	/**
	 * Public constructor
	 */
	public FileDialogPath(String path) {
		this.path = path;
	}

	/**
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Set path
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}

}

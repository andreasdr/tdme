package net.drewke.tdme.tools.viewer.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.MutableString;

/**
 * File dialog popup controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class FileDialogPopUpController extends ScreenController implements GUIChangeListener {

	// file dialog pop up mode
	public enum FileDialogPopUpMode {LOAD, SAVE};

	// model library controller
	private ModelLibraryController modelLibraryController;

	//
	private boolean active;

	//  screen node
	private GUIScreenNode screenNode;

	// curent working dir, extensions
	private File cwd;
	private String[] extensions;

	// pop up mode
	private FileDialogPopUpMode mode;

	// gui elements
	private GUITextNode caption;
	private GUIElementNode fileName;
	private GUIElementNode files;

	//
	private MutableString value;

	/**
	 * Public constructor
	 * @param model library controller
	 */
	public FileDialogPopUpController(ModelLibraryController modelLibraryController) {
		this.active = false;
		this.modelLibraryController = modelLibraryController;
		try {
			this.cwd = new File(".").getCanonicalFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.value = new MutableString();
	}

	/**
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return path name
	 */
	public String getPathName() {
		try {
			return cwd.getCanonicalPath().toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		//
		return null;
	}

	/**
	 * @return file name
	 */
	public String getFileName() {
		return fileName.getController().getValue().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#init()
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/shared/gui", "filedialog.xml");
			screenNode.addActionListener(modelLibraryController);
			screenNode.addChangeListener(this);
			caption = (GUITextNode)screenNode.getNodeById("filedialog_caption");
			files = (GUIElementNode)screenNode.getNodeById("filedialog_files");
			fileName = (GUIElementNode)screenNode.getNodeById("filedialog_filename");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Set up file dialog list box
	 */
	private void setupFileDialogListBox() {
		// set up caption
		{
			String captionText = "";
			switch(mode) {
				case LOAD: captionText = "Load from "; break;
				case SAVE: captionText = "Save into "; break;
			}
			String directory = cwd.getAbsolutePath();
			if (directory.length() > 50) directory = "..." + directory.substring(directory.length() - 50 + 3);
			caption.getText().set(captionText).append(directory);
		}

		// list files
		String[] fileList = new String[0];
		try {
			String directory = cwd.getAbsolutePath();
			fileList = FileSystem.getInstance().list(directory, new FilenameFilter() {
				public boolean accept(File directory, String file) {
					if (new File(directory, file).isDirectory() == true) return true;
					for (String extension: extensions) {
						if (file.toLowerCase().endsWith("." + extension)) return true;
					}
					return false;
				}
			});
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// files inner
		GUIParentNode filesInnerNode = (GUIParentNode)(files.getScreenNode().getNodeById(files.getId() + "_inner"));

		// clear sub nodes
		filesInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 1;
		String filesInnerNodeSubNodesXML = "";
		filesInnerNodeSubNodesXML+= "<scrollarea width=\"100%\" height=\"100%\">\n";
		filesInnerNodeSubNodesXML+= "<selectbox-option text=\"..\" value=\"..\" />\n";
		for (String file: fileList) {
			filesInnerNodeSubNodesXML+= "<selectbox-option text=\"" + file + "\" value=\"" + file + "\" />\n";
		}
		filesInnerNodeSubNodesXML+= "</scrollarea>\n";

		// inject sub nodes
		try {
			GUIParser.parse(
				filesInnerNode,
				filesInnerNodeSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		filesInnerNode.getScreenNode().layout();
	}
	
	/**
	 * Shows the file dialog pop up
	 * @param mode
	 * @param extensions
	 * @throws IOException 
	 */
	public void show(FileDialogPopUpMode mode, String[] extensions) {
		this.mode = mode;
		this.extensions = extensions;
		setupFileDialogListBox();
		this.active = true;
	}

	/**
	 * Abort the file dialog pop up
	 */
	public void close() {
		this.active = false;
	}

	/**
	 * On value changed
	 */
	public void onValueChanged(GUIElementNode node) {
		try {
		if (node.getId().equals(files.getId()) == true) {
			String selectedFile = node.getController().getValue().toString();
			System.out.println(selectedFile);
			if (new File(cwd, selectedFile).isDirectory()) {
				cwd = new File(cwd, selectedFile);
				try { cwd = cwd.getCanonicalFile(); } catch (IOException ioe) {}
				setupFileDialogListBox();
			} else {
				fileName.getController().setValue(
					value.set(selectedFile)
				);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
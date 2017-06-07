package net.drewke.tdme.tools.shared.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.MutableString;

/**
 * File dialog screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class FileDialogScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	//  screen node
	private GUIScreenNode screenNode;

	// curent working dir, extensions
	private String cwd;
	private String[] extensions;
	private String captionText;

	// gui elements
	private GUITextNode caption;
	private GUIElementNode fileName;
	private GUIElementNode files;

	//
	private MutableString value;

	//
	private Action applyAction;

	/**
	 * Public constructor
	 * @param model library controller
	 */
	public FileDialogScreenController() {
		try {
			this.cwd = new File(".").getCanonicalFile().toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.value = new MutableString();
		this.applyAction = null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return path name
	 */
	public String getPathName() {
		return cwd;
	}

	/**
	 * @return file name
	 */
	public String getFileName() {
		return fileName.getController().getValue().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#initialize()
	 */
	public void initialize() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/shared/gui", "filedialog.xml");
			screenNode.setVisible(false);
			screenNode.addActionListener(this);
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
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Set up file dialog list box
	 */
	private void setupFileDialogListBox() {
		// set up caption
		{
			String directory = cwd;
			if (directory.length() > 50) directory = "..." + directory.substring(directory.length() - 50 + 3);
			caption.getText().set(captionText).append(directory);
		}

		// list files
		String[] fileList = new String[0];
		try {
			String directory = cwd;
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

		// construct XML for sub nodes
		int idx = 1;
		String filesInnerNodeSubNodesXML = "";
		filesInnerNodeSubNodesXML+= "<scrollarea width=\"100%\" height=\"100%\">\n";
		filesInnerNodeSubNodesXML+= "<selectbox-option text=\"..\" value=\"..\" />\n";
		for (String file: fileList) {
			filesInnerNodeSubNodesXML+= "<selectbox-option text=\"" + GUIParser.escapeQuotes(file) + "\" value=\"" + GUIParser.escapeQuotes(file) + "\" />\n";
		}
		filesInnerNodeSubNodesXML+= "</scrollarea>\n";

		// inject sub nodes
		try {
			filesInnerNode.replaceSubNodes(
				filesInnerNodeSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shows the file dialog pop up
	 * @param caption text
	 * @param extensions
	 * @param apply action
	 * @throws IOException 
	 */
	public void show(String cwd, String captionText, String[] extensions, String fileName, Action applyAction) {
		try {
			this.cwd = new File(".").getCanonicalPath().toString();
			this.cwd = new File(cwd).getCanonicalPath().toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.captionText = captionText;
		this.extensions = extensions;
		this.fileName.getController().setValue(value.set(fileName));
		setupFileDialogListBox();
		screenNode.setVisible(true);
		this.applyAction = applyAction;
	}

	/**
	 * Abort the file dialog pop up
	 */
	public void close() {
		screenNode.setVisible(false);
	}

	/**
	 * On value changed
	 */
	public void onValueChanged(GUIElementNode node) {
		try {
		if (node.getId().equals(files.getId()) == true) {
			String selectedFile = node.getController().getValue().toString();
			Console.println(selectedFile);
			Console.println(cwd + ":" + selectedFile);
			if (new File(cwd, selectedFile).isDirectory()) {
				File file = new File(cwd, selectedFile);
				try { cwd = file.getCanonicalFile().toString(); } catch (IOException ioe) {}
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		switch (type) {
			case PERFORMED: 
				{
					if (node.getId().equals("filedialog_apply")) {
						if (applyAction != null) applyAction.performAction();
					} else
					if (node.getId().equals("filedialog_abort")) {
						close();
					}
					break;
				}
			default:
				{
					break;
				}
		}
	}

}

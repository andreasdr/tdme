package net.drewke.tdme.tools.leveleditor.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import net.drewke.tdme.os.FileSystem;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public abstract class PopUpsController implements ScreenController {

	public enum FileDialogPopUpMode {LOAD, SAVE};
	private File cwd;

	private String[] extensions;
	private Nifty nifty;
	private Screen screen;
	private Element errorPopUp;
	private Element errorPopUpCaption;
	private Element errorPopUpMessage;
	private FileDialogPopUpMode fileDialogPopUpMode;
	private Element fileDialogPopUp;
	private Element fileDialogPopUpCaption;
	private TextField fileDialogPopupFilename;
	private ListBox<String> fileDialogPopUpFiles;
	
	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#bind(de.lessvoid.nifty.Nifty, de.lessvoid.nifty.screen.Screen)
	 */
	public void bind(Nifty nifty, Screen screen, File cwd, String extensions) {
		this.cwd = cwd;
		this.extensions = extensions.split("\\,");
		this.nifty = nifty;
		this.screen = screen;
		errorPopUp = nifty.createPopup("error_popup");
		errorPopUpCaption = errorPopUp.findElementById("error_popup_caption");
		errorPopUpMessage = errorPopUp.findElementById("error_popup_message");
		fileDialogPopUp = nifty.createPopup("filedialog_popup");
		fileDialogPopUpCaption = fileDialogPopUp.findElementById("filedialog_popup_caption");
		fileDialogPopUpFiles =(ListBox<String>) fileDialogPopUp.findNiftyControl("filedialog_popup_files", ListBox.class);
		fileDialogPopupFilename = fileDialogPopUp.findNiftyControl("filedialog_popup_filename", TextField.class);
	}

	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#onEndScreen()
	 */
	public void onEndScreen() {
	}

	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#onStartScreen()
	 */
	public void onStartScreen() {
	}

	/**
	 * Set up file dialog list box
	 */
	private void setupFileDialogListBox() {
		// set up caption
		{
			String caption = "";
			switch(fileDialogPopUpMode) {
				case LOAD: caption = "Load from "; break;
				case SAVE: caption = "Save into "; break;
			}
			String directory = cwd.getAbsolutePath();
			if (directory.length() > 70) directory = "..." + directory.substring(directory.length() - 70 + 3);
			fileDialogPopUpCaption.getRenderer(TextRenderer.class).setText(
				caption + directory
			);
		}

		// set up files
		fileDialogPopUpFiles.clear();
		try {
			String directory = cwd.getAbsolutePath();
			String[] files = FileSystem.getInstance().list(directory, new FilenameFilter() {
				public boolean accept(File directory, String file) {
					if (new File(directory, file).isDirectory() == true) return true;
					for (String extension: extensions) {
						if (file.toLowerCase().endsWith("." + extension)) return true;
					}
					return false;
				}
			});
			fileDialogPopUpFiles.addItem("..");
			for (String file: files) {
				fileDialogPopUpFiles.addItem(file);
			}
		} catch (IOException ioe) {
			showErrorPopUp("Error", ioe.getMessage());
		}
	}
	
	/**
	 * Shows the file dialog pop up
	 * @throws IOException 
	 */
	public void showFileDialogPopUp(FileDialogPopUpMode mode) {
		this.fileDialogPopUpMode = mode;
		setupFileDialogListBox();
		nifty.showPopup(screen, fileDialogPopUp.getId(), fileDialogPopupFilename.getElement());		
	}

	/**
	 * Abort the file dialog pop up
	 */
	public void abortFileDialogPopUp() {
		nifty.closePopup(fileDialogPopUp.getId());
	}

	/**
	 * Apply file dialog pop up
	 */
	public void applyFileDialogPopUp() {
		nifty.closePopup(fileDialogPopUp.getId());
		switch (fileDialogPopUpMode) {
			case LOAD:
				try {
					String pathName = cwd.getCanonicalPath();
					String fileName = fileDialogPopupFilename.getText();
					if (new File(pathName, fileName).isDirectory()) {
						throw new Exception("File is a directory");
					}
					loadFile(pathName, fileName);
				} catch (Exception e) {
					e.printStackTrace();
					showErrorPopUp("Error", e.getMessage());
				}
				break;
			case SAVE:
				try {
					String pathName = cwd.getCanonicalPath();
					String fileName = fileDialogPopupFilename.getText();
					if (fileName.toLowerCase().endsWith("." + extensions[0]) == false) {
						fileName+= "." + extensions[0];
					}
					if (new File(pathName, fileName).isDirectory()) {
						throw new Exception("File is a directory");
					}
					saveFile(pathName, fileName);
				} catch (Exception e) {
					e.printStackTrace();
					showErrorPopUp("Error", e.getMessage());
				}
				break;
		}
	}

	@NiftyEventSubscriber(id = "filedialog_popup_files")
	public void onFileDialogFileChangeEvent(final String id, final ListBoxSelectionChangedEvent<String> event) {
		String selectedFile = event.getListBox().getSelection().get(0);
		if (new File(cwd, selectedFile).isDirectory()) {
			cwd = new File(cwd, selectedFile);
			try { cwd = cwd.getCanonicalFile(); } catch (IOException ioe) {}
			setupFileDialogListBox();
		} else {
			fileDialogPopupFilename.setText(selectedFile);
		}
	}

	/**
	 * Save file
	 * @param path name
	 * @param file name
	 */
	public abstract void saveFile(String pathName, String fileName) throws Exception;

	/**
	 * Load file
	 * @param path name
	 * @param file name
	 */
	public abstract void loadFile(String pathName, String fileName) throws Exception;

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		errorPopUpCaption.getRenderer(TextRenderer.class).setText(caption);
		errorPopUpMessage.getRenderer(TextRenderer.class).setText(message);
		nifty.showPopup(screen, errorPopUp.getId(), null);
	}

	/**
	 * Closes the error pop up
	 */
	public void closeErrorPopUp() {
		nifty.closePopup(errorPopUp.getId());
	}

}

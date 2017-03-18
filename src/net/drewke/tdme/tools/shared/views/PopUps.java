package net.drewke.tdme.tools.shared.views;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.tools.shared.controller.FileDialogScreenController;
import net.drewke.tdme.tools.shared.controller.InfoDialogScreenController;

/**
 * Pop ups view
 * @author andreas
 *
 */
public class PopUps {

	private InfoDialogScreenController infoDialogScreenController;
	private FileDialogScreenController fileDialogScreenController;

	/**
	 * Public constructor
	 */
	public PopUps() {
	}

	/**
	 * @return file dialog screen controller
	 */
	public FileDialogScreenController getFileDialogScreenController() {
		return fileDialogScreenController;
	}

	/**
	 * @return info dialog scren controller
	 */
	public InfoDialogScreenController getInfoDialogScreenController() {
		return infoDialogScreenController;
	}

	/**
	 * Init
	 */
	public void init() {
		fileDialogScreenController = new FileDialogScreenController();
		fileDialogScreenController.init();
		infoDialogScreenController = new InfoDialogScreenController();
		infoDialogScreenController.init();
		Engine.getInstance().getGUI().addScreen(fileDialogScreenController.getScreenNode().getId(), fileDialogScreenController.getScreenNode());
		Engine.getInstance().getGUI().addScreen(infoDialogScreenController.getScreenNode().getId(), infoDialogScreenController.getScreenNode());
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		fileDialogScreenController.dispose();
		infoDialogScreenController.dispose();
	}
}

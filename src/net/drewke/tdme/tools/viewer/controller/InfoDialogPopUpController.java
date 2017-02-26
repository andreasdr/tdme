package net.drewke.tdme.tools.viewer.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.utils.MutableString;

/**
 * Info dialog pop up screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class InfoDialogPopUpController extends ScreenController {

	// model library controller
	private ModelLibraryController modelLibraryController;

	//
	private boolean active;

	//  screen node
	private GUIScreenNode screenNode;

	// gui elements
	private GUITextNode captionNode;
	private GUITextNode messageNode;

	//
	private MutableString value;

	/**
	 * Public constructor
	 * @param model library controller
	 */
	public InfoDialogPopUpController(ModelLibraryController modelLibraryController) {
		this.active = false;
		this.modelLibraryController = modelLibraryController;
		this.value = new MutableString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#init()
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/shared/gui", "infodialog.xml");
			screenNode.addActionListener(modelLibraryController);
			captionNode = (GUITextNode)screenNode.getNodeById("infodialog_caption");
			messageNode = (GUITextNode)screenNode.getNodeById("infodialog_message");
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
	 * Shows the pop up
	 */
	public void show(String caption, String message) {
		captionNode.getText().set(value.set(caption));
		messageNode.getText().set(value.set(message));
		screenNode.layout();
		active = true;
	}

	/**
	 * Closes the pop up
	 */
	public void close() {
		active = false;
	}

}

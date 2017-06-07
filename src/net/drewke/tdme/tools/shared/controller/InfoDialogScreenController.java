package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.tools.shared.views.ModelViewerView;
import net.drewke.tdme.tools.shared.views.View;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.utils.MutableString;

/**
 * Info dialog screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class InfoDialogScreenController extends ScreenController implements GUIActionListener {

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
	public InfoDialogScreenController() {
		this.value = new MutableString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init()
	 */
	public void initialize() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/shared/gui", "infodialog.xml");
			screenNode.setVisible(false);
			screenNode.addActionListener(this);
			captionNode = (GUITextNode)screenNode.getNodeById("infodialog_caption");
			messageNode = (GUITextNode)screenNode.getNodeById("infodialog_message");
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
	 * Shows the pop up
	 */
	public void show(String caption, String message) {
		screenNode.setVisible(true);
		captionNode.getText().set(value.set(caption));
		messageNode.getText().set(value.set(message));
		screenNode.layout();
	}

	/**
	 * Closes the pop up
	 */
	public void close() {
		screenNode.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		switch(type) {
			case PERFORMED: 
				{
					if (node.getId().equals("infodialog_ok")) {
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

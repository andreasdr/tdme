package net.drewke.tdme.tools.leveleditor.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.events.GUIActionListener.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.Tools;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.shared.controller.ScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.shared.views.View;

/**
 * Level editor model library screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorModelLibraryScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private GUIScreenNode screenNode;
	private GUIElementNode modelLibraryListBox;

	public LevelEditorModelLibraryScreenController() {
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
	public void init() {
		try {
			screenNode = GUIParser.parse("resources/tools/leveleditor/gui", "screen_leveleditor_modellibrary.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);

			// 
			modelLibraryListBox = (GUIElementNode)screenNode.getNodeById("model_library_listbox");
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
	 * Add a model to level editor view
	 * @param model
	 */
	public void setModelLibrary(LevelEditorModelLibrary modelLibrary) {
		// model properties list box inner
		GUIParentNode modelLibraryListBoxInnerNode = (GUIParentNode)(modelLibraryListBox.getScreenNode().getNodeById(modelLibraryListBox.getId() + "_inner"));

		// clear sub nodes
		modelLibraryListBoxInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 1;
		String modelLibraryListBoxSubNodesXML = "";
		modelLibraryListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + modelLibraryListBox.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (int i = 0; i < modelLibrary.getModelCount(); i++) {
			int objectId = modelLibrary.getModelAt(i).getId();
			String objectName = modelLibrary.getModelAt(i).getName();
			modelLibraryListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				GUIParser.escapeQuotes(objectName) + 
				"\" value=\"" + 
				objectId + 
				"\" " +
				(i == 0?"selected=\"true\" ":"") +
				"/>\n";
		}
		modelLibraryListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			GUIParser.parse(
				modelLibraryListBoxInnerNode,
				modelLibraryListBoxSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		modelLibraryListBoxInnerNode.getScreenNode().layout();

		//
		onModelSelectionChanged();
	}

	/**
	 * On model changed
	 */
	public void onModelSelectionChanged() {
		View view = TDMELevelEditor.getInstance().getView();
		if (view instanceof LevelEditorView) {
			LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().getModel(Tools.convertToIntSilent(modelLibraryListBox.getController().getValue().toString()));
			if (model != null) {
				((LevelEditorView)view).loadModelFromLibrary(model.getId());
			}
		}
	}

	/**
	 * place model button clicked
	 */
	public void onPlaceModel() {
		View view = TDMELevelEditor.getInstance().getView();
		if (view instanceof LevelEditorView) {
			((LevelEditorView)view).placeObject();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		if (node.getId().equals("model_library_listbox") == true) {
			onModelSelectionChanged();
		} else {
			System.out.println("LevelEditorModelLibraryScreenController::onValueChanged: " + node.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		if (type == Type.PERFORMED) {
			if (node.getId().equals("button_model_library_place") == true) {
				onPlaceModel();
			} else {
				System.out.println("LevelEditorScreenController::onActionPerformed: " + node.getId());
			}
		}
	}

}

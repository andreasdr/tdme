package net.drewke.tdme.tools.leveleditor.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.leveleditor.views.ModelViewerView;
import net.drewke.tdme.tools.leveleditor.views.TriggerView;
import net.drewke.tdme.tools.shared.controller.Action;
import net.drewke.tdme.tools.shared.controller.ScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;
import net.drewke.tdme.utils.MutableString;

/**
 * Level editor model library screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorModelLibraryScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private GUIScreenNode screenNode;
	private GUIElementNode modelLibraryListBox;
	private GUIElementNode buttonModelPlace;
	private GUIElementNode buttonLevelEdit;

	private MutableString modelLibraryListBoxSelection;
	private MutableString dropdownEntityActionReset; 

	private PopUps popUps;

	/**
	 * Public constructor
	 */
	public LevelEditorModelLibraryScreenController(PopUps popUps) {
		this.popUps = popUps;
		modelLibraryListBoxSelection = new MutableString();
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
			buttonModelPlace = (GUIElementNode)screenNode.getNodeById("button_model_place");
			buttonLevelEdit = (GUIElementNode)screenNode.getNodeById("button_level_edit");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		buttonModelPlace.getController().setDisabled(false);
		buttonLevelEdit.getController().setDisabled(true);

		//
		dropdownEntityActionReset = new MutableString("action");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Add a model to level editor view
	 */
	public void setModelLibrary() {
		// model library
		LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getModelLibrary();

		// store selection
		modelLibraryListBoxSelection.set(modelLibraryListBox.getController().getValue());

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

		// reset selection
		if (modelLibraryListBoxSelection.length() > 0) {
			modelLibraryListBox.getController().setValue(modelLibraryListBoxSelection);
		}

		//
		onModelSelectionChanged();

		//
		buttonModelPlace.getController().setDisabled(modelLibrary.getModelCount() == 0);
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
	 * On edit model
	 */
	public void onEditModel() {
		// check if we have a model selected
		LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().getModel(Tools.convertToIntSilent(modelLibraryListBox.getController().getValue().toString()));
		if (model == null) return;

		switch (model.getType()) {
			case MODEL:
				// switch to model library view if not yet done
				if (TDMELevelEditor.getInstance().getView() instanceof ModelViewerView == false) {
					TDMELevelEditor.getInstance().switchToModelViewer();
				}
		
				// set model
				((ModelViewerView)TDMELevelEditor.getInstance().getView()).setModel(model);
				break;
			case TRIGGER:
				// switch to model trigger view if not yet done
				if (TDMELevelEditor.getInstance().getView() instanceof TriggerView == false) {
					TDMELevelEditor.getInstance().switchToTriggerView();
				}
		
				// set model
				((TriggerView)TDMELevelEditor.getInstance().getView()).setModel(model);
				break;
		}

		// button enabled
		buttonModelPlace.getController().setDisabled(true);
		buttonLevelEdit.getController().setDisabled(false);
	}

	/**
	 * On library action
	 */
	public void onLevel() {
		TDMELevelEditor.getInstance().switchToLevelEditor();
		buttonModelPlace.getController().setDisabled(false);
		buttonLevelEdit.getController().setDisabled(true);
	}

	/**
	 * place object button clicked
	 */
	public void onPlaceEntity() {
		// check if we have a model selected
		LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().getModel(Tools.convertToIntSilent(modelLibraryListBox.getController().getValue().toString()));
		if (model == null) return;

		// place object
		View view = TDMELevelEditor.getInstance().getView();
		if (view instanceof LevelEditorView) {
			((LevelEditorView)view).placeObject();
		}
	}

	/**
	 * place model entity clicked
	 */
	public void onDeleteEntity() {
		// check if we have a model selected
		LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().getModel(Tools.convertToIntSilent(modelLibraryListBox.getController().getValue().toString()));
		if (model == null) return;

		//
		TDMELevelEditor.getInstance().getLevel().removeObjectsByModelId(model.getId());
		TDMELevelEditor.getInstance().getLevel().getModelLibrary().removeModel(model.getId());

		// set model library
		setModelLibrary();

		//
		View view = TDMELevelEditor.getInstance().getView();
		if (view instanceof LevelEditorView) {
			((LevelEditorView)view).loadLevel();
		} else {
			TDMELevelEditor.getInstance().switchToLevelEditor();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		if (node.getId().equals("model_library_listbox") == true) {
			onModelSelectionChanged();
		} else 
		if (node.getId().equals("dropdown_entity_action") == true) {
			if (node.getController().getValue().equals("edit") == true) {
				onEditModel();
			} else
			if (node.getController().getValue().equals("delete") == true) {
				onDeleteEntity();
			} else
			// model
			if (node.getController().getValue().equals("create_model") == true) {
				// model library
				final LevelEditorModelLibrary modelLibrary = TDMELevelEditor.getInstance().getModelLibrary();
				//
				popUps.getFileDialogScreenController().show(
						"Load from: ", 
						new String[]{"tmm", "dae", "tm"},
						popUps.getFileDialogScreenController().getFileName(),
						new Action() {
							public void performAction() {
								try {
									LevelEditorModel model = modelLibrary.addModel(	
										LevelEditorModelLibrary.ID_ALLOCATE,
										popUps.getFileDialogScreenController().getFileName(),
										"",
										popUps.getFileDialogScreenController().getPathName(),
										popUps.getFileDialogScreenController().getFileName(),
										new Vector3(0f, 0f, 0f)
									);
									setModelLibrary();
									modelLibraryListBox.getController().setValue(modelLibraryListBoxSelection.set(model.getId()));
									onEditModel();
								} catch (Exception exception) {
									popUps.getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
								}
								popUps.getFileDialogScreenController().close();
							}
						}
					);
			} else
			// trigger
			if (node.getController().getValue().equals("create_trigger") == true) {
				try {
					LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().createTrigger(	
						LevelEditorModelLibrary.ID_ALLOCATE,
						"New trigger",
						"",
						1f,
						1f,
						1f,
						new Vector3()
					);
					setModelLibrary();
					modelLibraryListBox.getController().setValue(modelLibraryListBoxSelection.set(model.getId()));
					onEditModel();
				} catch (Exception exception) {
					popUps.getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
				}
			} else
			// light
			if (node.getController().getValue().equals("create_light") == true) {
				
			} else
			// particle
			if (node.getController().getValue().equals("create_particle") == true) {
			
			} else {
				System.out.println("LevelEditorModelLibraryScreenController::onValueChanged: dropdown_model_create: " + node.getController().getValue());
			}

			// reset
			node.getController().setValue(dropdownEntityActionReset);
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
			if (node.getId().equals("button_model_place") == true) {
				onPlaceEntity();
			} else
			if (node.getId().equals("button_level_edit") == true) {
				onLevel();
			} else {
				System.out.println("LevelEditorScreenController::onActionPerformed: " + node.getId());
			}
		}
	}

}

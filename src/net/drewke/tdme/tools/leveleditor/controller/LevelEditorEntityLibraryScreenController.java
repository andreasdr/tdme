package net.drewke.tdme.tools.leveleditor.controller;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.views.EmptyView;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.leveleditor.views.ModelViewerView;
import net.drewke.tdme.tools.leveleditor.views.TriggerView;
import net.drewke.tdme.tools.shared.controller.ScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.tools.shared.views.View;
import net.drewke.tdme.utils.MutableString;

/**
 * Level editor model library screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorEntityLibraryScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private GUIScreenNode screenNode;
	private GUIElementNode entityLibraryListBox;
	private GUIElementNode buttonEntityPlace;
	private GUIElementNode buttonLevelEdit;

	private MutableString entityLibraryListBoxSelection;
	private MutableString dropdownEntityActionReset; 

	private PopUps popUps;

	private String modelPath;

	/**
	 * Public constructor
	 */
	public LevelEditorEntityLibraryScreenController(PopUps popUps) {
		this.popUps = popUps;
		this.modelPath = ".";
		entityLibraryListBoxSelection = new MutableString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return model path
	 */
	public String getModelPath() {
		return modelPath;
	}

	/**
	 * Set model path
	 * @param model path
	 */
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init()
	 */
	public void init() {
		try {
			screenNode = GUIParser.parse("resources/tools/leveleditor/gui", "screen_leveleditor_entitylibrary.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);

			// 
			entityLibraryListBox = (GUIElementNode)screenNode.getNodeById("entity_library_listbox");
			buttonEntityPlace = (GUIElementNode)screenNode.getNodeById("button_entity_place");
			buttonLevelEdit = (GUIElementNode)screenNode.getNodeById("button_level_edit");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		buttonEntityPlace.getController().setDisabled(false);
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
	 * Select entity by entity id
	 * @param entity id
	 */
	public void selectEntity(int entityId) {
		entityLibraryListBoxSelection.set(entityId);
		entityLibraryListBox.getController().setValue(entityLibraryListBoxSelection);
	}

	/**
	 * Set up complete entity library
	 */
	public void setEntityLibrary() {
		// model library
		LevelEditorEntityLibrary entityLibrary = TDMELevelEditor.getInstance().getEntityLibrary();

		// store selection
		entityLibraryListBoxSelection.set(entityLibraryListBox.getController().getValue());

		// entity library list box inner
		GUIParentNode entityLibraryListBoxInnerNode = (GUIParentNode)(entityLibraryListBox.getScreenNode().getNodeById(entityLibraryListBox.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 1;
		String entityLibraryListBoxSubNodesXML = "";
		entityLibraryListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + entityLibraryListBox.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (int i = 0; i < entityLibrary.getEntityCount(); i++) {
			int objectId = entityLibrary.getEntityAt(i).getId();
			String objectName = entityLibrary.getEntityAt(i).getName();
			entityLibraryListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				GUIParser.escapeQuotes(objectName) + 
				"\" value=\"" + 
				objectId + 
				"\" " +
				(i == 0?"selected=\"true\" ":"") +
				"/>\n";
		}
		entityLibraryListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			entityLibraryListBoxInnerNode.replaceSubNodes(
				entityLibraryListBoxSubNodesXML,
				false
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// reset selection
		if (entityLibraryListBoxSelection.length() > 0) {
			entityLibraryListBox.getController().setValue(entityLibraryListBoxSelection);
		}

		//
		onEntitySelectionChanged();

		//
		buttonEntityPlace.getController().setDisabled(entityLibrary.getEntityCount() == 0);
	}

	/**
	 * On entity selection changed
	 */
	public void onEntitySelectionChanged() {
		View view = TDMELevelEditor.getInstance().getView();
		if (view instanceof LevelEditorView) {
			LevelEditorEntity entity = TDMELevelEditor.getInstance().getEntityLibrary().getEntity(Tools.convertToIntSilent(entityLibraryListBox.getController().getValue().toString()));
			if (entity != null) {
				((LevelEditorView)view).loadEntityFromLibrary(entity.getId());
			}
		}
	}

	/**
	 * On edit entity
	 */
	public void onEditEntity() {
		// check if we have a entity selected
		LevelEditorEntity entity = TDMELevelEditor.getInstance().getEntityLibrary().getEntity(Tools.convertToIntSilent(entityLibraryListBox.getController().getValue().toString()));
		if (entity == null) return;

		switch (entity.getType()) {
			case MODEL:
				// switch to model library view if not yet done
				if (TDMELevelEditor.getInstance().getView() instanceof ModelViewerView == false) {
					TDMELevelEditor.getInstance().switchToModelViewer();
				}
		
				// set model
				((ModelViewerView)TDMELevelEditor.getInstance().getView()).setEntity(entity);
				break;
			case TRIGGER:
				// switch to model trigger view if not yet done
				if (TDMELevelEditor.getInstance().getView() instanceof TriggerView == false) {
					TDMELevelEditor.getInstance().switchToTriggerView();
				}
		
				// set model
				((TriggerView)TDMELevelEditor.getInstance().getView()).setEntity(entity);
				break;
			case EMPTY:
				// switch to model trigger view if not yet done
				if (TDMELevelEditor.getInstance().getView() instanceof EmptyView == false) {
					TDMELevelEditor.getInstance().switchToEmptyView();
				}
		
				// set model
				((EmptyView)TDMELevelEditor.getInstance().getView()).setEntity(entity);
				break;
		}

		// button enabled
		buttonEntityPlace.getController().setDisabled(true);
		buttonLevelEdit.getController().setDisabled(false);
	}

	/**
	 * On edit level
	 */
	public void onEditLevel() {
		TDMELevelEditor.getInstance().switchToLevelEditor();
		buttonEntityPlace.getController().setDisabled(false);
		buttonLevelEdit.getController().setDisabled(true);
	}

	/**
	 * place object button clicked
	 */
	public void onPlaceEntity() {
		// check if we have a model selected
		LevelEditorEntity entity = TDMELevelEditor.getInstance().getEntityLibrary().getEntity(Tools.convertToIntSilent(entityLibraryListBox.getController().getValue().toString()));
		if (entity == null) return;

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
		LevelEditorEntity entity = TDMELevelEditor.getInstance().getEntityLibrary().getEntity(Tools.convertToIntSilent(entityLibraryListBox.getController().getValue().toString()));
		if (entity == null) return;

		//
		TDMELevelEditor.getInstance().getLevel().removeObjectsByEntityId(entity.getId());
		TDMELevelEditor.getInstance().getLevel().getEntityLibrary().removeEntity(entity.getId());

		// set model library
		setEntityLibrary();

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
		if (node.getId().equals("entity_library_listbox") == true) {
			onEntitySelectionChanged();
		} else 
		if (node.getId().equals("dropdown_entity_action") == true) {
			if (node.getController().getValue().equals("edit") == true) {
				onEditEntity();
			} else
			if (node.getController().getValue().equals("delete") == true) {
				onDeleteEntity();
			} else
			// model
			if (node.getController().getValue().equals("create_model") == true) {
				// model library
				final LevelEditorEntityLibrary entityLibrary = TDMELevelEditor.getInstance().getEntityLibrary();
				//
				popUps.getFileDialogScreenController().show(
						modelPath,
						"Load from: ", 
						new String[]{"tmm", "dae", "tm"},
						"",
						new Action() {
							public void performAction() {
								try {
									// add model
									LevelEditorEntity entity = entityLibrary.addModel(	
										LevelEditorEntityLibrary.ID_ALLOCATE,
										popUps.getFileDialogScreenController().getFileName(),
										"",
										popUps.getFileDialogScreenController().getPathName(),
										popUps.getFileDialogScreenController().getFileName(),
										new Vector3(0f, 0f, 0f)
									);
									entity.setDefaultBoundingVolumes();
									setEntityLibrary();
									entityLibraryListBox.getController().setValue(entityLibraryListBoxSelection.set(entity.getId()));
									onEditEntity();
								} catch (Exception exception) {
									popUps.getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
								}
								modelPath = popUps.getFileDialogScreenController().getPathName();
								popUps.getFileDialogScreenController().close();
							}
						}
					);
			} else
			// trigger
			if (node.getController().getValue().equals("create_trigger") == true) {
				try {
					LevelEditorEntity model = TDMELevelEditor.getInstance().getEntityLibrary().addTrigger(	
						LevelEditorEntityLibrary.ID_ALLOCATE,
						"New trigger",
						"",
						1f,
						1f,
						1f
					);
					setEntityLibrary();
					entityLibraryListBox.getController().setValue(entityLibraryListBoxSelection.set(model.getId()));
					onEditEntity();
				} catch (Exception exception) {
					popUps.getInfoDialogScreenController().show("Error", "An error occurred: " + exception.getMessage());
				}
			} else
			// empty
			if (node.getController().getValue().equals("create_empty") == true) {
				try {
					LevelEditorEntity model = TDMELevelEditor.getInstance().getEntityLibrary().addEmpty(	
						LevelEditorEntityLibrary.ID_ALLOCATE,
						"New empty",
						""
					);
					setEntityLibrary();
					entityLibraryListBox.getController().setValue(entityLibraryListBoxSelection.set(model.getId()));
					onEditEntity();
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
				System.out.println("LevelEditorEntityLibraryScreenController::onValueChanged: dropdown_model_create: " + node.getController().getValue());
			}

			// reset
			node.getController().setValue(dropdownEntityActionReset);
		} else {
			System.out.println("LevelEditorEntityLibraryScreenController::onValueChanged: " + node.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		if (type == Type.PERFORMED) {
			if (node.getId().equals("button_entity_place") == true) {
				onPlaceEntity();
			} else
			if (node.getId().equals("button_level_edit") == true) {
				onEditLevel();
			} else {
				System.out.println("LevelEditorScreenController::onActionPerformed: " + node.getId());
			}
		}
	}

}

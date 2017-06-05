package net.drewke.tdme.tools.shared.controller;

import java.util.Collection;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.views.EntityBaseView;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.utils.MutableString;

/**
 * Entity base sub screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityBaseSubScreenController {

	private final static MutableString TEXT_EMPTY = new MutableString("");

	private EntityBaseView view;
	private PopUps popUps;

	private GUIElementNode entityName;
	private GUIElementNode entityDescription;
	private GUIElementNode entityApply;
	private GUIElementNode entityPropertyName;
	private GUIElementNode entityPropertyValue;
	private GUIElementNode entityPropertySave;
	private GUIElementNode entityPropertyAdd;
	private GUIElementNode entityPropertyRemove;
	private GUIElementNode entityPropertiesList;
	private GUIElementNode entityPropertyPresetApply;
	private GUIElementNode entityPropertiesPresets;
	private Action onSetEntityDataAction;

	private MutableString value = new MutableString();

	/**
	 * Public constructor
	 * @param view
	 * @param on set entity data action
	 */
	public EntityBaseSubScreenController(PopUps popUps, Action onSetEntityDataAction) {
		this.view = new EntityBaseView(this);
		this.popUps = popUps;
		this.onSetEntityDataAction = onSetEntityDataAction;
	}

	/**
	 * Init
	 * @param screen node
	 */
	public void init(GUIScreenNode screenNode) {
		// load screen node
		try {
			entityName = (GUIElementNode)screenNode.getNodeById("entity_name");
			entityDescription = (GUIElementNode)screenNode.getNodeById("entity_description");
			entityApply = (GUIElementNode)screenNode.getNodeById("button_entity_apply");
			entityPropertyName = (GUIElementNode)screenNode.getNodeById("entity_property_name");
			entityPropertyValue = (GUIElementNode)screenNode.getNodeById("entity_property_value");
			entityPropertySave = (GUIElementNode)screenNode.getNodeById("button_entity_properties_save");
			entityPropertyAdd = (GUIElementNode)screenNode.getNodeById("button_entity_properties_add");
			entityPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_entity_properties_remove");
			entityPropertiesList = (GUIElementNode)screenNode.getNodeById("entity_properties_listbox");
			entityPropertyPresetApply = (GUIElementNode)screenNode.getNodeById("button_entity_properties_presetapply");
			entityPropertiesPresets = (GUIElementNode)screenNode.getNodeById("entity_properties_presets");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up model properties presets
		setEntityPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());
	}

	/**
	 * Set up general entity data
	 * @param name
	 * @param description
	 */
	public void setEntityData(String name, String description) {
		entityName.getController().setDisabled(false);
		entityName.getController().getValue().set(name);
		entityDescription.getController().setDisabled(false);
		entityDescription.getController().getValue().set(description);
		entityApply.getController().setDisabled(false);
	}

	/**
	 * Unset entity data
	 */
	public void unsetEntityData() {
		entityName.getController().setValue(TEXT_EMPTY);
		entityName.getController().setDisabled(true);
		entityDescription.getController().setValue(TEXT_EMPTY);
		entityDescription.getController().setDisabled(true);
		entityApply.getController().setDisabled(true);
	}

	/**
	 * On entity data apply
	 * @param model
	 */
	public void onEntityDataApply(LevelEditorEntity model) {
		if (model == null) return;

		//
		view.setEntityData(
			model,
			entityName.getController().getValue().toString(), 
			entityDescription.getController().getValue().toString()
		);
		onSetEntityDataAction.performAction();
	}

	/**
	 * Set up entity property preset ids
	 * @param entity property preset ids
	 */
	public void setEntityPresetIds(Collection<String> entityPresetIds) {
		// model properties presets inner
		GUIParentNode entityPropertiesPresetsInnerNode = (GUIParentNode)(entityPropertiesPresets.getScreenNode().getNodeById(entityPropertiesPresets.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 0;
		String entityPropertiesPresetsInnerNodeSubNodesXML = "";
		entityPropertiesPresetsInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + entityPropertiesPresets.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100\">\n";
		for (String entityPresetId: entityPresetIds) {
			entityPropertiesPresetsInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(entityPresetId) + "\" value=\"" + GUIParser.escapeQuotes(entityPresetId) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}
		entityPropertiesPresetsInnerNodeSubNodesXML+= "</scrollarea-vertical>";

		// inject sub nodes
		try {
			entityPropertiesPresetsInnerNode.replaceSubNodes(
				entityPropertiesPresetsInnerNodeSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set up entity properties
	 * @param model
	 * @param preset id
	 * @param entity properties
	 * @param selected name
	 */
	public void setEntityProperties(LevelEditorEntity model, String presetId, Iterable<PropertyModelClass> entityProperties, String selectedName) {
		//
		entityPropertiesPresets.getController().setDisabled(false);
		entityPropertyPresetApply.getController().setDisabled(false);
		entityPropertiesList.getController().setDisabled(false);
		entityPropertyAdd.getController().setDisabled(false);
		entityPropertyRemove.getController().setDisabled(false);
		entityPropertySave.getController().setDisabled(true);
		entityPropertyName.getController().setDisabled(true);
		entityPropertyValue.getController().setDisabled(true);

		// set up preset
		entityPropertiesPresets.getController().setValue(presetId != null?value.set(presetId):value.set("none"));

		// model properties list box inner
		GUIParentNode entityPropertiesListBoxInnerNode = (GUIParentNode)(entityPropertiesList.getScreenNode().getNodeById(entityPropertiesList.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 1;
		String entityPropertiesListBoxSubNodesXML = "";
		entityPropertiesListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + entityPropertiesList.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (PropertyModelClass entityProperty: entityProperties) {
			entityPropertiesListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				GUIParser.escapeQuotes(entityProperty.getName()) + 
				": " + 
				GUIParser.escapeQuotes(entityProperty.getValue()) + 
				"\" value=\"" + 
				GUIParser.escapeQuotes(entityProperty.getName()) + 
				"\" " +
				(selectedName != null && entityProperty.getName().equals(selectedName)?"selected=\"true\" ":"") +
				"/>\n";
		}
		entityPropertiesListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			entityPropertiesListBoxInnerNode.replaceSubNodes(
				entityPropertiesListBoxSubNodesXML,
				false
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		onEntityPropertiesSelectionChanged(model);
	}

	/**
	 * Unset entity properties
	 */
	public void unsetEntityProperties() {
		GUIParentNode modelPropertiesListBoxInnerNode = (GUIParentNode)(entityPropertiesList.getScreenNode().getNodeById(entityPropertiesList.getId() + "_inner"));
		modelPropertiesListBoxInnerNode.clearSubNodes();
		entityPropertiesPresets.getController().setValue(value.set("none"));
		entityPropertiesPresets.getController().setDisabled(true);
		entityPropertyPresetApply.getController().setDisabled(true);
		entityPropertiesList.getController().setDisabled(true);
		entityPropertyAdd.getController().setDisabled(true);
		entityPropertyRemove.getController().setDisabled(true);
		entityPropertySave.getController().setDisabled(true);
		entityPropertyName.getController().setValue(TEXT_EMPTY);
		entityPropertyName.getController().setDisabled(true);
		entityPropertyValue.getController().setValue(TEXT_EMPTY);
		entityPropertyValue.getController().setDisabled(true);
	}

	/**
	 * On entity property save
	 * @param entity
	 */
	public void onEntityPropertySave(LevelEditorEntity entity) {
		if (view.entityPropertySave(
			entity,
			entityPropertiesList.getController().getValue().toString(),
			entityPropertyName.getController().getValue().toString(),
			entityPropertyValue.getController().getValue().toString()) == false) {
			//
			showErrorPopUp("Warning", "Saving entity property failed");
		}
	}

	/**
	 * On entity property add
	 * @param entity
	 */
	public void onEntityPropertyAdd(LevelEditorEntity entity) {
		if (view.entityPropertyAdd(entity) == false) {
			showErrorPopUp("Warning", "Adding new entity property failed");
		}
	}

	/**
	 * On entity property remove
	 * @param entity
	 */
	public void onEntityPropertyRemove(LevelEditorEntity entity) {
		if (view.entityPropertyRemove(entity, entityPropertiesList.getController().getValue().toString()) == false) {
			showErrorPopUp("Warning", "Removing entity property failed");
		}
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		popUps.getInfoDialogScreenController().show(caption, message);
	}

	/**
	 * On entity property preset apply
	 * @param model 
	 */
	public void onEntityPropertyPresetApply(LevelEditorEntity model) {
		view.entityPropertiesPreset(model, entityPropertiesPresets.getController().getValue().toString());
	}

	/**
	 * Event callback for entity properties selection
	 * @pafam entity
	 */
	public void onEntityPropertiesSelectionChanged(LevelEditorEntity entity) {
		entityPropertyName.getController().setDisabled(true);
		entityPropertyName.getController().setValue(TEXT_EMPTY);
		entityPropertyValue.getController().setDisabled(true);
		entityPropertyValue.getController().setValue(TEXT_EMPTY);
		entityPropertySave.getController().setDisabled(true);
		entityPropertyRemove.getController().setDisabled(true);
		PropertyModelClass entityProperty = entity.getProperty(entityPropertiesList.getController().getValue().toString());
		if (entityProperty != null) {
			entityPropertyName.getController().setValue(value.set(entityProperty.getName()));
			entityPropertyValue.getController().setValue(value.set(entityProperty.getValue()));
			entityPropertyName.getController().setDisabled(false);
			entityPropertyValue.getController().setDisabled(false);
			entityPropertySave.getController().setDisabled(false);
			entityPropertyRemove.getController().setDisabled(false);
		}
	}

	/**
	 * On value changed
	 * @param node
	 * @param model
	 */
	public void onValueChanged(GUIElementNode node, LevelEditorEntity model) {
		if (node == entityPropertiesList) {
			onEntityPropertiesSelectionChanged(model);
		} else {
			// Console.println("ModelViewerScreenController::onValueChanged(): id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
		}
	}

	/**
	 * On action performed
	 * @param type
	 * @param node
	 * @param entity
	 */
	public void onActionPerformed(GUIActionListener.Type type, GUIElementNode node, LevelEditorEntity entity) {
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_entity_apply")) {
						onEntityDataApply(entity);
					} else
					if (node.getId().equals("button_entity_properties_presetapply")) {
						onEntityPropertyPresetApply(entity);
					} else
					if (node.getId().equals("button_entity_properties_add")) {
						onEntityPropertyAdd(entity);
					} else
					if (node.getId().equals("button_entity_properties_remove")) {
						onEntityPropertyRemove(entity);
					} else
					if (node.getId().equals("button_entity_properties_save")) {
						onEntityPropertySave(entity);
					} else {
						// Console.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					}
					break;
				}
			case PERFORMING:
				{
					// Console.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					break;
				}
		}
	}

}

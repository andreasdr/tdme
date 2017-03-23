package net.drewke.tdme.tools.shared.controller;

import java.util.Collection;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.views.ModelBaseView;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.utils.MutableString;

/**
 * Model base sub screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelBaseSubScreenController {

	private final static MutableString TEXT_EMPTY = new MutableString("");

	private ModelBaseView view;
	private PopUps popUps;

	private GUIElementNode modelName;
	private GUIElementNode modelDescription;
	private GUIElementNode modelApply;
	private GUIElementNode modelPropertyName;
	private GUIElementNode modelPropertyValue;
	private GUIElementNode modelPropertySave;
	private GUIElementNode modelPropertyAdd;
	private GUIElementNode modelPropertyRemove;
	private GUIElementNode modelPropertiesList;
	private GUIElementNode modelPropertyPresetApply;
	private GUIElementNode modelPropertiesPresets;
	private Action onSetModelDataAction;

	private MutableString value = new MutableString();

	/**
	 * Public constructor
	 * @param view
	 */
	public ModelBaseSubScreenController(PopUps popUps, Action onSetModelDataAction) {
		this.view = new ModelBaseView(this);
		this.popUps = popUps;
		this.onSetModelDataAction = onSetModelDataAction;
	}

	/**
	 * Init
	 * @param screen node
	 */
	public void init(GUIScreenNode screenNode) {
		// load screen node
		try {
			modelName = (GUIElementNode)screenNode.getNodeById("model_name");
			modelDescription = (GUIElementNode)screenNode.getNodeById("model_description");
			modelApply = (GUIElementNode)screenNode.getNodeById("button_model_apply");
			modelPropertyName = (GUIElementNode)screenNode.getNodeById("model_property_name");
			modelPropertyValue = (GUIElementNode)screenNode.getNodeById("model_property_value");
			modelPropertySave = (GUIElementNode)screenNode.getNodeById("button_model_properties_save");
			modelPropertyAdd = (GUIElementNode)screenNode.getNodeById("button_model_properties_add");
			modelPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_model_properties_remove");
			modelPropertiesList = (GUIElementNode)screenNode.getNodeById("model_properties_listbox");
			modelPropertyPresetApply = (GUIElementNode)screenNode.getNodeById("button_model_properties_presetapply");
			modelPropertiesPresets = (GUIElementNode)screenNode.getNodeById("model_properties_presets");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set up model properties presets
		setModelPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());
	}

	/**
	 * Set up general model data
	 * @param name
	 * @param description
	 */
	public void setModelData(String name, String description) {
		modelName.getController().setDisabled(false);
		modelName.getController().getValue().set(name);
		modelDescription.getController().setDisabled(false);
		modelDescription.getController().getValue().set(description);
		modelApply.getController().setDisabled(false);
	}

	/**
	 * Unset model data
	 */
	public void unsetModelData() {
		modelName.getController().setValue(TEXT_EMPTY);
		modelName.getController().setDisabled(true);
		modelDescription.getController().setValue(TEXT_EMPTY);
		modelDescription.getController().setDisabled(true);
		modelApply.getController().setDisabled(true);
	}

	/**
	 * On model data apply
	 * @param model
	 */
	public void onModelDataApply(LevelEditorModel model) {
		if (model == null) return;

		//
		view.setModelData(
			model,
			modelName.getController().getValue().toString(), 
			modelDescription.getController().getValue().toString()
		);
		onSetModelDataAction.performAction();
	}

	/**
	 * Set up model property preset ids
	 * @param model property preset ids
	 */
	public void setModelPresetIds(Collection<String> modelPresetIds) {
		// model properties presets inner
		GUIParentNode modelPropertiesPresetsInnerNode = (GUIParentNode)(modelPropertiesPresets.getScreenNode().getNodeById(modelPropertiesPresets.getId() + "_inner"));

		// clear sub nodes
		modelPropertiesPresetsInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 0;
		String modelPropertiesPresetsInnerNodeSubNodesXML = "";
		modelPropertiesPresetsInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + modelPropertiesPresets.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100\">\n";
		for (String modelPresetId: modelPresetIds) {
			modelPropertiesPresetsInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(modelPresetId) + "\" value=\"" + GUIParser.escapeQuotes(modelPresetId) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}
		modelPropertiesPresetsInnerNodeSubNodesXML+= "</scrollarea-vertical>";

		// inject sub nodes
		try {
			GUIParser.parse(
				modelPropertiesPresetsInnerNode,
				modelPropertiesPresetsInnerNodeSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		modelPropertiesPresetsInnerNode.getScreenNode().layout();
	}

	/**
	 * Set up model properties
	 * @param model
	 * @param preset id
	 * @param model properties
	 * @param selected name
	 */
	public void setModelProperties(LevelEditorModel model, String presetId, Iterable<PropertyModelClass> modelProperties, String selectedName) {
		//
		modelPropertiesPresets.getController().setDisabled(false);
		modelPropertyPresetApply.getController().setDisabled(false);
		modelPropertiesList.getController().setDisabled(false);
		modelPropertyAdd.getController().setDisabled(false);
		modelPropertyRemove.getController().setDisabled(false);
		modelPropertySave.getController().setDisabled(true);
		modelPropertyName.getController().setDisabled(true);
		modelPropertyValue.getController().setDisabled(true);

		// set up preset
		modelPropertiesPresets.getController().setValue(presetId != null?value.set(presetId):value.set("none"));

		// model properties list box inner
		GUIParentNode modelPropertiesListBoxInnerNode = (GUIParentNode)(modelPropertiesList.getScreenNode().getNodeById(modelPropertiesList.getId() + "_inner"));
		
		// clear sub nodes
		modelPropertiesListBoxInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 1;
		String modelPropertiesListBoxSubNodesXML = "";
		modelPropertiesListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + modelPropertiesList.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (PropertyModelClass modelProperty: modelProperties) {
			modelPropertiesListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				GUIParser.escapeQuotes(modelProperty.getName()) + 
				": " + 
				GUIParser.escapeQuotes(modelProperty.getValue()) + 
				"\" value=\"" + 
				GUIParser.escapeQuotes(modelProperty.getName()) + 
				"\" " +
				(selectedName != null && modelProperty.getName().equals(selectedName)?"selected=\"true\" ":"") +
				"/>\n";
		}
		modelPropertiesListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			GUIParser.parse(
				modelPropertiesListBoxInnerNode,
				modelPropertiesListBoxSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		modelPropertiesListBoxInnerNode.getScreenNode().layout();

		//
		onModelPropertiesSelectionChanged(model);
	}

	/**
	 * Unset model properties
	 */
	public void unsetModelProperties() {
		GUIParentNode modelPropertiesListBoxInnerNode = (GUIParentNode)(modelPropertiesList.getScreenNode().getNodeById(modelPropertiesList.getId() + "_inner"));
		modelPropertiesListBoxInnerNode.clearSubNodes();
		modelPropertiesPresets.getController().setValue(value.set("none"));
		modelPropertiesPresets.getController().setDisabled(true);
		modelPropertyPresetApply.getController().setDisabled(true);
		modelPropertiesList.getController().setDisabled(true);
		modelPropertyAdd.getController().setDisabled(true);
		modelPropertyRemove.getController().setDisabled(true);
		modelPropertySave.getController().setDisabled(true);
		modelPropertyName.getController().setValue(TEXT_EMPTY);
		modelPropertyName.getController().setDisabled(true);
		modelPropertyValue.getController().setValue(TEXT_EMPTY);
		modelPropertyValue.getController().setDisabled(true);
	}

	/**
	 * On model property save
	 * @param model
	 */
	public void onModelPropertySave(LevelEditorModel model) {
		if (view.modelPropertySave(
			model,
			modelPropertiesList.getController().getValue().toString(),
			modelPropertyName.getController().getValue().toString(),
			modelPropertyValue.getController().getValue().toString()) == false) {
			//
			showErrorPopUp("Warning", "Saving model property failed");
		}
	}

	/**
	 * On model property add
	 * @param model
	 */
	public void onModelPropertyAdd(LevelEditorModel model) {
		if (view.modelPropertyAdd(model) == false) {
			showErrorPopUp("Warning", "Adding new model property failed");
		}
	}

	/**
	 * On model property remove
	 * @param model
	 */
	public void onModelPropertyRemove(LevelEditorModel model) {
		if (view.modelPropertyRemove(model, modelPropertiesList.getController().getValue().toString()) == false) {
			showErrorPopUp("Warning", "Removing model property failed");
		}
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		popUps.getInfoDialogScreenController().show(caption, message);
	}

	/**
	 * On model property preset apply
	 * @param model 
	 */
	public void onModelPropertyPresetApply(LevelEditorModel model) {
		view.modelPropertiesPreset(model, modelPropertiesPresets.getController().getValue().toString());
	}

	/**
	 * Event callback for model properties selection
	 * @pafam model
	 */
	public void onModelPropertiesSelectionChanged(LevelEditorModel model) {
		modelPropertyName.getController().setDisabled(true);
		modelPropertyName.getController().setValue(TEXT_EMPTY);
		modelPropertyValue.getController().setDisabled(true);
		modelPropertyValue.getController().setValue(TEXT_EMPTY);
		modelPropertySave.getController().setDisabled(true);
		modelPropertyRemove.getController().setDisabled(true);
		PropertyModelClass modelProperty = model.getProperty(modelPropertiesList.getController().getValue().toString());
		if (modelProperty != null) {
			modelPropertyName.getController().setValue(value.set(modelProperty.getName()));
			modelPropertyValue.getController().setValue(value.set(modelProperty.getValue()));
			modelPropertyName.getController().setDisabled(false);
			modelPropertyValue.getController().setDisabled(false);
			modelPropertySave.getController().setDisabled(false);
			modelPropertyRemove.getController().setDisabled(false);
		}
	}

	/**
	 * On value changed
	 * @param node
	 * @param model
	 */
	public void onValueChanged(GUIElementNode node, LevelEditorModel model) {
		if (node == modelPropertiesList) {
			onModelPropertiesSelectionChanged(model);
		} else {
			// System.out.println("ModelViewerScreenController::onValueChanged(): id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
		}
	}

	/**
	 * On action performed
	 * @param type
	 * @param node
	 * @param model
	 */
	public void onActionPerformed(GUIActionListener.Type type, GUIElementNode node, LevelEditorModel model) {
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_model_apply")) {
						onModelDataApply(model);
					} else
					if (node.getId().equals("button_model_properties_presetapply")) {
						onModelPropertyPresetApply(model);
					} else
					if (node.getId().equals("button_model_properties_add")) {
						onModelPropertyAdd(model);
					} else
					if (node.getId().equals("button_model_properties_remove")) {
						onModelPropertyRemove(model);
					} else
					if (node.getId().equals("button_model_properties_save")) {
						onModelPropertySave(model);
					} else {
						System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					}
					break;
				}
			case PERFORMING:
				{
					// System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					break;
				}
		}
	}

}

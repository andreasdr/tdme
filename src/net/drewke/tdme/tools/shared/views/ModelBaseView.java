package net.drewke.tdme.tools.shared.views;

import java.util.ArrayList;

import net.drewke.tdme.tools.shared.controller.ModelBaseScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;

/**
 * Model base biew
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelBaseView {

	private ModelBaseScreenController modelBaseScreenController;

	/**
	 * Public constructor
	 * @param model base controller
	 */
	public ModelBaseView(ModelBaseScreenController modelBaseScreenController) {
		this.modelBaseScreenController = modelBaseScreenController;
	}

	/**
	 * Init
	 */
	public void init() {
		// set up model properties presets
		modelBaseScreenController.setModelPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());
	}

	/**
	 * Apply model property preset
	 * @param model
	 * @param preset id
	 */
	public void modelPropertiesPreset(LevelEditorModel model, String presetId) {
		if (model == null) return;

		// clear model properties
		model.clearProperties();

		// add model properties by preset if missing
		ArrayList<PropertyModelClass> modelPropertyPresetVector = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (modelPropertyPresetVector != null) {
			for (PropertyModelClass modelPropertyPreset: modelPropertyPresetVector) {
				model.addProperty(modelPropertyPreset.getName(), modelPropertyPreset.getValue());
			}
		}

		// update model properties to gui
		modelBaseScreenController.setModelProperties(
			model,
			presetId,
			model.getProperties(),
			null
		);
	}

	/**
	 * Save a model property
	 * @param model
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean modelPropertySave(LevelEditorModel model, String oldName, String name, String value) {
		if (model == null) return false;

		// try to update property
		if (model.updateProperty(oldName, name, value) == true) {
			// reload model properties
			modelBaseScreenController.setModelProperties(
				model,
				null,
				model.getProperties(),
				name
			);

			// 
			return true;
		}

		//
		return false;
	}

	/**
	 * Add a model property
	 * @param model
	 * @return success
	 */
	public boolean modelPropertyAdd(LevelEditorModel model) {
		if (model == null) return false;

		// try to add property
		if (model.addProperty("new.property", "new.value")) {
			// reload model properties
			modelBaseScreenController.setModelProperties(
				model,
				null,
				model.getProperties(),
				"new.property"
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Remove a model property from model properties
	 * @param model
	 * @param name
	 * @return success
	 */
	public boolean modelPropertyRemove(LevelEditorModel model, String name) {
		if (model == null) return false;

		// try to remove property
		int idx = model.getPropertyIndex(name);
		if (idx != -1 && model.removeProperty(name) == true) {
			// get property first at index that was removed 
			PropertyModelClass property = model.getPropertyByIndex(idx);
			if (property == null) {
				// if current index does not work, take current one -1
				property = model.getPropertyByIndex(idx - 1);
			}

			// reload model properties
			modelBaseScreenController.setModelProperties(
				model,
				null,
				model.getProperties(),
				property == null?null:property.getName()
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Update current model data
	 * @param model
	 * @param name
	 * @param description
	 */
	public void setModelData(LevelEditorModel model, String name, String description) {
		if (model == null) return;
		model.setName(name);
		model.setDescription(description);
	}

}

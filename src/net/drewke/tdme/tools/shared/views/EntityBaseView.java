package net.drewke.tdme.tools.shared.views;

import net.drewke.tdme.tools.shared.controller.EntityBaseSubScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.utils.ArrayList;

/**
 * Model base biew
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityBaseView {

	private EntityBaseSubScreenController entityBaseSubScreenController;

	/**
	 * Public constructor
	 * @param model base sub screen controller
	 */
	public EntityBaseView(EntityBaseSubScreenController entityBaseSubScreenController) {
		this.entityBaseSubScreenController = entityBaseSubScreenController;
	}

	/**
	 * Init
	 */
	public void init() {
		// set up model properties presets
		entityBaseSubScreenController.setEntityPresetIds(LevelPropertyPresets.getInstance().getObjectPropertiesPresets().keySet());
	}

	/**
	 * Apply entity property preset
	 * @param entity
	 * @param preset id
	 */
	public void entityPropertiesPreset(LevelEditorEntity entity, String presetId) {
		if (entity == null) return;

		// clear entity properties
		entity.clearProperties();

		// add entity properties by preset if missing
		ArrayList<PropertyModelClass> entityPropertyPresetArrayList = LevelPropertyPresets.getInstance().getObjectPropertiesPresets().get(presetId);
		if (entityPropertyPresetArrayList != null) {
			for (PropertyModelClass entityPropertyPreset: entityPropertyPresetArrayList) {
				entity.addProperty(entityPropertyPreset.getName(), entityPropertyPreset.getValue());
			}
		}

		// update entity properties to gui
		entityBaseSubScreenController.setEntityProperties(
			entity,
			presetId,
			entity.getProperties(),
			null
		);
	}

	/**
	 * Save a entity property
	 * @param entity
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean entityPropertySave(LevelEditorEntity entity, String oldName, String name, String value) {
		if (entity == null) return false;

		// try to update property
		if (entity.updateProperty(oldName, name, value) == true) {
			// reload model properties
			entityBaseSubScreenController.setEntityProperties(
				entity,
				null,
				entity.getProperties(),
				name
			);

			// 
			return true;
		}

		//
		return false;
	}

	/**
	 * Add a entity property
	 * @param entity
	 * @return success
	 */
	public boolean entityPropertyAdd(LevelEditorEntity entity) {
		if (entity == null) return false;

		// try to add property
		if (entity.addProperty("new.property", "new.value")) {
			// reload model properties
			entityBaseSubScreenController.setEntityProperties(
				entity,
				null,
				entity.getProperties(),
				"new.property"
			);

			//
			return true;
		}

		//
		return false;
	}

	/**
	 * Remove a entity property from model properties
	 * @param entity
	 * @param name
	 * @return success
	 */
	public boolean entityPropertyRemove(LevelEditorEntity entity, String name) {
		if (entity == null) return false;

		// try to remove property
		int idx = entity.getPropertyIndex(name);
		if (idx != -1 && entity.removeProperty(name) == true) {
			// get property first at index that was removed 
			PropertyModelClass property = entity.getPropertyByIndex(idx);
			if (property == null) {
				// if current index does not work, take current one -1
				property = entity.getPropertyByIndex(idx - 1);
			}

			// reload model properties
			entityBaseSubScreenController.setEntityProperties(
				entity,
				null,
				entity.getProperties(),
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
	 * @param entity
	 * @param name
	 * @param description
	 */
	public void setEntityData(LevelEditorEntity entity, String name, String description) {
		if (entity == null) return;
		entity.setName(name);
		entity.setDescription(description);
	}

}

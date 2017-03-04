package net.drewke.tdme.tools.shared.model;

import net.drewke.tdme.engine.Transformations;

/**
 * Level Editor Object
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorObject extends Properties {

	private String id;
	private String description;
	private Transformations transformations;
	private LevelEditorModel model;

	/**
	 * Public constructor
	 * @param id
	 * @param transformations
	 * @param model
	 */
	public LevelEditorObject(String id, String description, Transformations transformations, LevelEditorModel model) {
		super();
		this.id = id;
		this.description = description;
		this.transformations = transformations;
		this.model = model;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return transformations
	 */
	public Transformations getTransformations() {
		return transformations;
	}

	/**
	 * @return model
	 */
	public LevelEditorModel getModel() {
		return model;
	}

	/**
	 * Set up model
	 * @param model
	 */
	public void setModel(LevelEditorModel model) {
		this.model = model;
	}

	/**
	 * @return merged properties from model and object
	 */
	public Properties getTotalProperties() {
		// total properties
		Properties properties = new Properties();

		// clone model properties
		for (PropertyModelClass modelProperty: getModel().getProperties()) {
			properties.addProperty(modelProperty.getName(), modelProperty.getValue());
		}

		// add object properties
		for (PropertyModelClass objectProperty: getProperties()) {
			PropertyModelClass property = properties.getProperty(objectProperty.getName());
			if (property != null) {
				properties.updateProperty(property.getName(), objectProperty.getName(), objectProperty.getValue());
			} else {
				properties.addProperty(objectProperty.getName(), objectProperty.getValue());
			}
		}

		//
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "LevelEditorObject [id=" + id + ", description="
				+ description + ", transformations="
				+ transformations + ", model=" + model + ", objectProperties="
				+ super.toString() + "]";
	}

}

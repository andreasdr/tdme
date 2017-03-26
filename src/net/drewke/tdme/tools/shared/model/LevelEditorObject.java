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
	private LevelEditorEntity entity;

	/**
	 * Public constructor
	 * @param id
	 * @param transformations
	 * @param entity
	 */
	public LevelEditorObject(String id, String description, Transformations transformations, LevelEditorEntity entity) {
		super();
		this.id = id;
		this.description = description;
		this.transformations = transformations;
		this.entity = entity;
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
	 * @return entity
	 */
	public LevelEditorEntity getEntity() {
		return entity;
	}

	/**
	 * Set up entity
	 * @param entity
	 */
	public void setEntity(LevelEditorEntity entity) {
		this.entity = entity;
	}

	/**
	 * @return merged properties from entity and object
	 */
	public Properties getTotalProperties() {
		// total properties
		Properties properties = new Properties();

		// clone entity properties
		for (PropertyModelClass entityProperty: getEntity().getProperties()) {
			properties.addProperty(entityProperty.getName(), entityProperty.getValue());
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
				+ transformations + ", entity=" + entity + ", objectProperties="
				+ super.toString() + "]";
	}

}

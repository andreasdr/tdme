package net.drewke.tdme.tools.leveleditor.model;

/**
 * Property model class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PropertyModelClass {

	private String name;
	private String value;

	/**
	 * Constructor
	 * @param name
	 * @param value
	 */
	public PropertyModelClass(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set up name 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set up value
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Clones this property model entity
	 */
	public PropertyModelClass clone() {
		return new PropertyModelClass(new String(name), new String(value));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name + ": " + value;
	}

}
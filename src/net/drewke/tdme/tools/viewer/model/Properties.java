package net.drewke.tdme.tools.viewer.model;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Properties
 * @author Andreas Drewke
 * @version $Id$
 */
public class Properties {

	private HashMap<String, PropertyModelClass> propertiesByName;
	private ArrayList<PropertyModelClass> properties;

	public Properties() {
		this.properties = new ArrayList<PropertyModelClass>();
		this.propertiesByName = new HashMap<String, PropertyModelClass>();
	}

	/**
	 * @return properties
	 */
	public Iterable<PropertyModelClass> getProperties() {
		return properties;
	}

	/**
	 * Clears properties
	 */
	public void clearProperties() {
		properties.clear();
		propertiesByName.clear();
	}

	/**
	 * Retrieve property by name
	 * @param name
	 * @return property or null
	 */
	public PropertyModelClass getProperty(String name) {
		return propertiesByName.get(name);
	}

	/**
	 * Add a property
	 * @param property
	 */
	public boolean addProperty(PropertyModelClass property) {
		if (propertiesByName.containsKey(property.getName())) return false;
		propertiesByName.put(property.getName(), property);
		properties.add(property);
		return true;
	}

	/**
	 * Update a property
	 * @param property
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean updateProperty(PropertyModelClass property, String name, String value) {
		// old property must exist in list
		if (propertiesByName.containsKey(property.getName()) == false) return false;
		// if property name has changed new property property key must be unused
		if (property.getName().equals(name) == false &&
			propertiesByName.containsKey(name) == true) {
			//
			return false;
		}
		propertiesByName.remove(property.getName());
		property.setName(name);
		property.setValue(value);
		propertiesByName.put(property.getName(), property);
		return true;
	}

	/**
	 * Removes a property
	 * @param property name
	 */
	public boolean removeProperty(String name) {
		if (propertiesByName.containsKey(name) == false) return false;
		PropertyModelClass property = propertiesByName.remove(name);
		properties.remove(property);
		return true;
	}

}

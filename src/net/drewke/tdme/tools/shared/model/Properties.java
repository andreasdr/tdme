package net.drewke.tdme.tools.shared.model;

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
	 * Get property index
	 * @param name
	 * @return index or -1 if not found
	 */
	public int getPropertyIndex(String name) {
		for (int i = 0; i < properties.size(); i++) {
			if (properties.get(i).getName().equals(name) == true) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get property by index
	 * @param idx
	 * @return property or null
	 */
	public PropertyModelClass getPropertyByIndex(int idx) {
		return
			idx >= 0 && idx < properties.size()?
				properties.get(idx):
				null;
	}

	/**
	 * Add a property
	 * @param property
	 */
	public boolean addProperty(String name, String value) {
		if (propertiesByName.containsKey(name) == true) return false;
		PropertyModelClass property = new PropertyModelClass(name, value);
		propertiesByName.put(name, property);
		properties.add(property);
		return true;
	}

	/**
	 * Update a property
	 * @param old name
	 * @param name
	 * @param value
	 * @return success
	 */
	public boolean updateProperty(String oldName, String name, String value) {
		// old property must exist in list
		if (propertiesByName.containsKey(oldName) == false) return false;
		// if property name has changed new property property key must be unused
		if (oldName.equals(name) == false &&
			propertiesByName.containsKey(name) == true) {
			//
			return false;
		}
		PropertyModelClass property = propertiesByName.remove(oldName);
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

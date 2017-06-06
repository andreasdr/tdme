package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.utils.ArrayList;

/**
 * GUI node conditions
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUINodeConditions {

	protected ArrayList<String> conditions;

	/**
	 * Constructor
	 */
	public GUINodeConditions() {
		this.conditions = new ArrayList<String>();
	}

	/**
	 * @return conditions
	 */
	public ArrayList<String> getConditions() {
		return conditions;
	}

	/**
	 * Add a condition
	 * @param condition
	 */
	public void add(String condition) {
		remove(condition);
		conditions.add(condition);
	}

	/**
	 * Remove a condition
	 * @param condition
	 */
	public void remove(String condition) {
		for (int i = 0; i < conditions.size(); i++) {
			if (conditions.get(i).equals(condition) == true) {
				conditions.remove(i);
				break;
			}
		}
	}

	/**
	 * Remove all
	 */
	public void removeAll() {
		conditions.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "GUINodeConditions [conditions=" + conditions + "]";
	}

}

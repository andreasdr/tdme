package net.drewke.tdme.gui;

import java.util.ArrayList;
import java.util.StringTokenizer;

import sun.util.locale.StringTokenIterator;

/**
 * GUI element child node
 * 
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIElementChildNode extends GUINode {

	private String[] showOn;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param requested constraints
	 */
	protected GUIElementChildNode(GUINode parentNode, String id, RequestedConstraints requestedConstraints, String[] showOn) {
		super(parentNode, id, requestedConstraints);
	}

	/**
	 * Create show on
	 * @param show on
	 */
	protected static String[] createShowOn(String showOn) {
		ArrayList<String> showOnArrayList = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(showOn, ",");
		while (t.hasMoreTokens()) {
			showOnArrayList.add(t.nextToken());
		}
		String[] showOnStringArray = new String[showOnArrayList.size()];
		return showOnArrayList.toArray(showOnStringArray);
	}

}

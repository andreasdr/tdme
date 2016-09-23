package net.drewke.tdme.gui;

import java.util.ArrayList;
import java.util.StringTokenizer;

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
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 */
	protected GUIElementChildNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, String[] showOn) {
		super(parentNode, id, alignments, requestedConstraints);
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
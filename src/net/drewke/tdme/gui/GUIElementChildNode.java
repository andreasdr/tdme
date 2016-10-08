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

	private ArrayList<String> showOn;
	private ArrayList<String> hideOn;

	/**
	 * Constructor
	 * @param parent node
	 * @param id
	 * @param alignments
	 * @param requested constraints
	 * @param show on
	 * @param hide on
	 */
	protected GUIElementChildNode(GUINode parentNode, String id, Alignments alignments, RequestedConstraints requestedConstraints, ArrayList<String> showOn, ArrayList<String> hideOn) {
		super(parentNode, id, alignments, requestedConstraints);
		this.showOn = showOn;
		this.hideOn = hideOn;
	}

	/**
	 * Create conditions
	 * @param conditions
	 */
	protected static ArrayList<String> createConditions(String conditions) {
		ArrayList<String> conditionsArrayList = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(conditions, ",");
		while (t.hasMoreTokens()) {
			conditionsArrayList.add(t.nextToken());
		}
		return conditionsArrayList;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	protected boolean checkConditions() {
		// check for on-show "always"
		for (int i = 0; i < showOn.size(); i++) {
			if (showOn.get(i).equals(GUIElementNode.CONDITION_ALWAYS)) return true;
		}

		// check for on-hide "always"
		for (int i = 0; i < hideOn.size(); i++) {
			if (hideOn.get(i).equals(GUIElementNode.CONDITION_ALWAYS)) return false;
		}

		// determine parent element node
		GUINode node = parentNode;
		while (node != null && node instanceof GUIElementNode == false) {
			node = node.parentNode;
		}

		// exit if no element node
		if (node == null) {
			return true;
		}

		GUIElementNode elementNode = (GUIElementNode)node;

		// check for on-show
		for (int i = 0; i < showOn.size(); i++) {
			for (int j = 0; j < elementNode.activeConditions.size(); j++) {
				if (showOn.get(i).equals(elementNode.activeConditions.get(j))) return true;
			}
		}

		// check for on-hide
		for (int i = 0; i < hideOn.size(); i++) {
			for (int j = 0; j < elementNode.activeConditions.size(); j++) {
				if (hideOn.get(i).equals(elementNode.activeConditions.get(j))) return false;
			}
		}

		// always is default if no show-on given
		return showOn.size() == 0;
	}

}

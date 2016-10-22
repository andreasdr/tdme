package net.drewke.tdme.gui;

import java.util.ArrayList;

/**
 * GUI select box controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUISelectBoxController extends GUINodeController {

	protected ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUISelectBoxController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Unselect all nodes
	 */
	protected void unselect() {
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.controller; 
			if (childController instanceof GUISelectBoxOptionController) {
				((GUISelectBoxOptionController)childController).unselect();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#handleEvent(net.drewke.tdme.gui.GUINode, net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUINode node, GUIMouseEvent event) {
	}

}

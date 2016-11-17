package net.drewke.tdme.gui.elements;

import java.util.ArrayList;

import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI tabs controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITabsController extends GUINodeController {

	private ArrayList<GUINode> childControllerNodes = new ArrayList<GUINode>();

	/**
	 * Constructor
	 * @param node
	 */
	protected GUITabsController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// child nodes
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);

		// select first tab in header
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUITabController) {
				((GUITabController)childController).setSelected(true);
				break;
			}
		}

		// select first content tab
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUITabContentController) {
				((GUITabContentController)childController).setSelected(true);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Unselect all tab nodes
	 */
	protected void unselect() {
		// unselect tabs (header)
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUITabController) {
				((GUITabController)childController).setSelected(false);
			}
		}
	}

	/**
	 * Set tab content selected
	 * @param id
	 */
	protected void setTabContentSelected(String id) {
		// select selected content tab, unselect other content tabs
		((GUIParentNode)node).getChildControllerNodes(childControllerNodes);
		for (int i = 0; i < childControllerNodes.size(); i++) {
			GUINode childControllerNode = childControllerNodes.get(i);
			GUINodeController childController = childControllerNode.getController(); 
			if (childController instanceof GUITabContentController) {
				((GUITabContentController)childController).setSelected(
					childController.getNode().getId().equals(id + "-content")
				);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#handleEvent(net.drewke.tdme.gui.GUINode, net.drewke.tdme.gui.GUIMouseEvent)
	 */
	public void handleEvent(GUINode node, GUIMouseEvent event) {
		// no op
	}

}

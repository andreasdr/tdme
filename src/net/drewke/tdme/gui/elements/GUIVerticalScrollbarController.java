package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;

/**
 * GUI vertical scroll bar controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIVerticalScrollbarController extends GUINodeController {

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIVerticalScrollbarController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#init()
	 */
	public void init() {
		final GUIParentNode contentNode = (GUIParentNode)node.getScreenNode().getNodeById(node.getId() + "_content_panel");
		final GUIElementNode upArrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_scrollbar_panel_up");
		final GUIElementNode downArrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_scrollbar_panel_down");
		node.getScreenNode().addActionListener(new GUIActionListener() {
			public void onActionPerformed(Type type, GUIElementNode node) {
				if (node == upArrowNode) {
					float renderOffsetY = contentNode.getRenderOffsetY() - 1f;
					if (renderOffsetY < 0f) renderOffsetY = 0f;
					contentNode.setRenderOffsetY(renderOffsetY);
				} else
				if (node == downArrowNode) {
					float contentHeight = contentNode.getComputedConstraints().height;
					float renderOffsetY = contentNode.getRenderOffsetY() + 1f;
					if (renderOffsetY > contentHeight) renderOffsetY = contentHeight;
					contentNode.setRenderOffsetY(renderOffsetY);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusGained()
	 */
	public void onFocusGained() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#hasValue()
	 */
	public boolean hasValue() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#getValue()
	 */
	public String getValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		// no op
	}

}

package net.drewke.tdme.gui.elements;

import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIVerticalScrollbarInternalController;

/**
 * GUI scroll area horizontal controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIScrollAreaHorizontalController extends GUINodeController {

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIScrollAreaHorizontalController(GUINode node) {
		super(node);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#init()
	 */
	public void init() {
		final GUIParentNode contentNode = (GUIParentNode)node.getScreenNode().getNodeById(node.getId() + "_scrollarea_content_layout");
		final GUIElementNode leftArrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_scrollbar_horizontal_layout_left");
		final GUIElementNode rightArrowNode = (GUIElementNode)node.getScreenNode().getNodeById(node.getId() + "_scrollbar_horizontal_layout_right");
		node.getScreenNode().addActionListener(new GUIActionListener() {
			public void onActionPerformed(Type type, GUIElementNode node) {
				if (node == leftArrowNode) {
					// determine scrollable width
					float elementWidth = contentNode.getComputedConstraints().width;
					float contentWidth = contentNode.getContentWidth();
					float scrollableWidth = contentWidth - elementWidth;

					// skip if no scrollable width
					if (scrollableWidth <= 0f) return;

					// set up children render offset X and clip it
					float childrenRenderOffsetX = contentNode.getChildrenRenderOffsetX() - 1f;
					if (childrenRenderOffsetX < 0f) childrenRenderOffsetX = 0f;
					contentNode.setChildrenRenderOffsetX(childrenRenderOffsetX);
				} else
				if (node == rightArrowNode) {
					// determine scrollable width
					float elementWidth = contentNode.getComputedConstraints().width;
					float contentWidth = contentNode.getContentWidth();
					float scrollableWidth = contentWidth - elementWidth;

					// skip if no scrollable width
					if (scrollableWidth <= 0f) return;

					// set up children render offset x and clip it
					float childrenRenderOffsetX = contentNode.getChildrenRenderOffsetX() + 1f;
					if (childrenRenderOffsetX > contentWidth - contentNode.getComputedConstraints().width) {
						childrenRenderOffsetX = contentWidth - contentNode.getComputedConstraints().width;
					}
					contentNode.setChildrenRenderOffsetX(childrenRenderOffsetX);
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
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#tick()
	 */
	public void tick() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusGained()
	 */
	public void onFocusGained() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#onFocusLost()
	 */
	public void onFocusLost() {
		// no op
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

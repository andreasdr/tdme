package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;

/**
 * GUI Scrollbar controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIVerticalScrollbarInternalController extends GUINodeController {


	private GUILayoutNode contentNode;
	private boolean dragging = false;
	private int mouseYOffset = -1;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIVerticalScrollbarInternalController(GUINode node) {
		super(node);
		this.contentNode = (GUILayoutNode)node.getScreenNode().getNodeById(node.getParentControllerNode().id + "_content_layout");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#init()
	 */
	public void init() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @return bar height
	 */
	protected float getBarHeight() {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		return (node.computedConstraints.height - node.border.top - node.border.bottom) * (elementHeight / contentHeight);
	}

	/**
	 * @return bar top
	 */
	protected float getBarTop() {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		float scrollableHeight = contentHeight - elementHeight;
		float childrenRenderOffsetY = contentNode.childrenRenderOffSetY;
		float barHeight = (node.computedConstraints.height - node.border.top - node.border.bottom) * (elementHeight / contentHeight);
		return node.computedConstraints.top + node.computedConstraints.alignmentTop + node.border.top + (childrenRenderOffsetY * ((node.computedConstraints.height - barHeight) / scrollableHeight));
	}

	/**
	 * Set dragged y
	 * @param dragged y
	 */
	protected void setDraggedY(float draggedY) {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		float scrollableHeight = contentHeight - elementHeight;
		float barHeight = getBarHeight();

		// determine render offset y
		float childrenRenderOffsetY =
			contentNode.getChildrenRenderOffSetY() +
			(draggedY * (scrollableHeight / (node.computedConstraints.height - barHeight)));

		// clip to min, max
		if (childrenRenderOffsetY < 0) childrenRenderOffsetY = 0;
		if (childrenRenderOffsetY > scrollableHeight) childrenRenderOffsetY = scrollableHeight;

		// set new children render offset Y
		contentNode.setChildrenRenderOffSetY(
			childrenRenderOffsetY
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		if (node == this.node &&
			event.getButton() == 1) {
			if (node.isEventBelongingToNode(event) == true &&
				event.getType() == Type.MOUSE_PRESSED) {
				//
				GUIVerticalScrollbarInternalNode verticalScrollbarInternalNode = ((GUIVerticalScrollbarInternalNode)node);
				float barTop = getBarTop();
				float barHeight = getBarHeight();
				if (event.getY() >= barTop &&
					event.getY() < barTop + barHeight) {
					mouseYOffset = (int)(event.getY() - barTop);
					dragging = true;
				}
			} else
			if (dragging == true &&
				event.getType() == Type.MOUSE_RELEASED ) {
				//
				mouseYOffset = -1;
				dragging = false;
			} else
			if (dragging == true &&
				event.getType() == Type.MOUSE_DRAGGED) {
				//
				GUIVerticalScrollbarInternalNode verticalScrollbarInternalNode = ((GUIVerticalScrollbarInternalNode)node);
				float barTop = getBarTop();
				float draggedY = event.getY() - barTop - mouseYOffset;
				setDraggedY(draggedY);
			}

			// set event processed
			event.setProcessed(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#tick()
	 */
	public void tick() {
		// TODO Auto-generated method stub
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

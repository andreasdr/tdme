package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI Scrollbar controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIHorizontalScrollbarInternalController extends GUINodeController {

	public enum State {NONE, MOUSEOVER, DRAGGING};

	private GUILayoutNode contentNode;
	private State state = State.NONE;
	private int mouseXOffset = -1;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIHorizontalScrollbarInternalController(GUINode node) {
		super(node);
		this.contentNode = (GUILayoutNode)node.getScreenNode().getNodeById(node.getParentControllerNode().id + "_inner");
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#isDisabled()
	 */
	public boolean isDisabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#init()
	 */
	public void init() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#dispose()
	 */
	public void dispose() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#postLayout()
	 */
	public void postLayout() {
		// no op
	}

	/**
	 * @return state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return bar width
	 */
	protected float getBarWidth() {
		float elementWidth = contentNode.computedConstraints.width;
		float contentWidth = contentNode.getContentWidth();
		float barWidthRelative = (elementWidth / contentWidth);
		if (barWidthRelative > 1.0f) barWidthRelative = 1f;
		return (node.computedConstraints.width - node.border.left - node.border.right) * barWidthRelative;
	}

	/**
	 * @return bar left
	 */
	protected float getBarLeft() {
		float elementWidth = contentNode.computedConstraints.width;
		float contentWidth = contentNode.getContentWidth();
		float scrollableWidth = contentWidth - elementWidth;
		float childrenRenderOffsetX = contentNode.childrenRenderOffsetX;
		float barWidth = (node.computedConstraints.width - node.border.left - node.border.right) * (elementWidth / contentWidth);
		return node.computedConstraints.left + node.computedConstraints.alignmentLeft + node.border.left + (childrenRenderOffsetX * ((node.computedConstraints.width - barWidth) / scrollableWidth));
	}

	/**
	 * Set dragged x
	 * @param dragged x
	 */
	protected void setDraggedX(float draggedX) {
		float elementWidth = contentNode.computedConstraints.width;
		float contentWidth = contentNode.getContentWidth();
		float scrollableWidth = contentWidth - elementWidth;

		// skip if no scrollable height
		if (scrollableWidth <= 0f) return;

		// bar height
		float barWidth = getBarWidth();

		// determine render offset x
		float childrenRenderOffsetX =
			contentNode.getChildrenRenderOffsetX() +
			(draggedX * (scrollableWidth / (node.computedConstraints.width - barWidth)));

		// clip to min, max
		if (childrenRenderOffsetX < 0) childrenRenderOffsetX = 0;
		if (childrenRenderOffsetX > scrollableWidth) childrenRenderOffsetX = scrollableWidth;

		// set new children render offset X
		contentNode.setChildrenRenderOffsetX(
			childrenRenderOffsetX
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// skip if not this node
		if (node != this.node) return;

		// mouse moved state
		if (node.isEventBelongingToNode(event) == true &&
			event.getType() == Type.MOUSE_MOVED) {
			//
			state = State.MOUSEOVER;

			// set event processed
			event.setProcessed(true);
		} else
		if (event.getButton() == 1) {
			if (node.isEventBelongingToNode(event) == true &&
				event.getType() == Type.MOUSE_PRESSED) {
				//
				float barLeft = getBarLeft();
				float barWidth  = getBarWidth();
				if (event.getX() < barLeft) {
					float elementWidth = contentNode.computedConstraints.width;
					float contentWidth = contentNode.getContentWidth();
					float scrollableWidth = contentWidth - elementWidth;
					setDraggedX(-elementWidth * ((node.computedConstraints.width - barWidth) / scrollableWidth));
				} else
				if (event.getX() > barLeft + barWidth) {
					float elementWidth = contentNode.computedConstraints.width;
					float contentWidth = contentNode.getContentWidth();
					float scrollableWidth = contentWidth - elementWidth;
					setDraggedX(+elementWidth * ((node.computedConstraints.width - barWidth) / scrollableWidth));
				} else
				if (event.getX() >= barLeft &&
					event.getX() < barLeft + barWidth) {
					mouseXOffset = (int)(event.getX() - barLeft);
					state = State.DRAGGING;
				}
			} else
			if (state == State.DRAGGING &&
				event.getType() == Type.MOUSE_RELEASED ) {
				//
				mouseXOffset = -1;
				state = State.NONE;
			} else
			if (state == State.DRAGGING &&
				event.getType() == Type.MOUSE_DRAGGED) {
				//
				float barLeft = getBarLeft();
				float draggedX = event.getX() - barLeft - mouseXOffset;
				setDraggedX(draggedX);
			}

			// set event processed
			event.setProcessed(true);
		} else {
			state = State.NONE;
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
	public MutableString getValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#setValue(net.drewke.tdme.utils.MutableString)
	 */
	public void setValue(MutableString value) {
		// no op
	}

}

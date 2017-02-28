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
public class GUIVerticalScrollbarInternalController extends GUINodeController {

	public enum State {NONE, MOUSEOVER, DRAGGING};

	private GUILayoutNode contentNode;
	private State state = State.NONE;
	private int mouseYOffset = -1;

	/**
	 * Constructor
	 * @param node
	 */
	protected GUIVerticalScrollbarInternalController(GUINode node) {
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
	 * @return bar height
	 */
	protected float getBarHeight() {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		float barHeightRelative = (elementHeight / contentHeight);
		if (barHeightRelative > 1.0f) barHeightRelative = 1f;
		return (node.computedConstraints.height - node.border.top - node.border.bottom) * barHeightRelative;
	}

	/**
	 * @return bar top
	 */
	protected float getBarTop() {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		float scrollableHeight = contentHeight - elementHeight;
		float childrenRenderOffsetY = contentNode.childrenRenderOffsetY;
		float barHeight = (node.computedConstraints.height - node.border.top - node.border.bottom) * (elementHeight / contentHeight);
		if (scrollableHeight > 0.0f) {
			return node.computedConstraints.top + node.computedConstraints.alignmentTop + node.border.top + (childrenRenderOffsetY * ((node.computedConstraints.height - barHeight) / scrollableHeight));
		} else {
			return node.computedConstraints.top + node.computedConstraints.alignmentTop + node.border.top;
		}
	}

	/**
	 * Set dragged y
	 * @param dragged y
	 */
	protected void setDraggedY(float draggedY) {
		float elementHeight = contentNode.computedConstraints.height;
		float contentHeight = contentNode.getContentHeight();
		float scrollableHeight = contentHeight - elementHeight;

		// skip if no scrollable height
		if (scrollableHeight <= 0f) return;

		// bar height
		float barHeight = getBarHeight();

		// determine render offset y
		float childrenRenderOffsetY =
			contentNode.getChildrenRenderOffsetY() +
			(draggedY * (scrollableHeight / (node.computedConstraints.height - barHeight)));

		// clip to min, max
		if (childrenRenderOffsetY < 0) childrenRenderOffsetY = 0;
		if (childrenRenderOffsetY > scrollableHeight) childrenRenderOffsetY = scrollableHeight;

		// set new children render offset Y
		contentNode.setChildrenRenderOffsetY(
			childrenRenderOffsetY
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
				float barTop = getBarTop();
				float barHeight = getBarHeight();
				if (event.getY() < barTop) {
					float elementHeight = contentNode.computedConstraints.height;
					float contentHeight = contentNode.getContentHeight();
					float scrollableHeight = contentHeight - elementHeight;
					setDraggedY(-elementHeight * ((node.computedConstraints.height - barHeight) / scrollableHeight));
				} else
				if (event.getY() > barTop + barHeight) {
					float elementHeight = contentNode.computedConstraints.height;
					float contentHeight = contentNode.getContentHeight();
					float scrollableHeight = contentHeight - elementHeight;
					setDraggedY(+elementHeight * ((node.computedConstraints.height - barHeight) / scrollableHeight));
				} else
				if (event.getY() >= barTop &&
					event.getY() < barTop + barHeight) {
					mouseYOffset = (int)(event.getY() - barTop);
					state = State.DRAGGING;
				}

				// set event processed
				event.setProcessed(true);
			} else
			if (state == State.DRAGGING &&
				event.getType() == Type.MOUSE_RELEASED ) {
				//
				mouseYOffset = -1;
				state = State.NONE;

				// set event processed
				event.setProcessed(true);
			} else
			if (state == State.DRAGGING &&
				event.getType() == Type.MOUSE_DRAGGED) {
				//
				float barTop = getBarTop();
				float draggedY = event.getY() - barTop - mouseYOffset;
				setDraggedY(draggedY);

				// set event processed
				event.setProcessed(true);
			}
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

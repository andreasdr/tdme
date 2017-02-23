package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.elements.GUIInputController;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUINode.Border;
import net.drewke.tdme.gui.nodes.GUINode.ComputedConstraints;
import net.drewke.tdme.gui.nodes.GUINode.Padding;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI input internal controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIInputInternalController extends GUINodeController {

	private final static long CURSOR_MODE_DURATION = 500L;
	private final static long DRAGGING_CALMDOWN = 50L;

	public enum CursorMode {SHOW, HIDE};

	private GUIElementNode inputNode;

	private long cursorModeStarted = -1L;
	private CursorMode cursorMode = CursorMode.SHOW;
	private int index;
	private int offset;
	private boolean isDragging;
	private int[] dragPosition;
	private long draggingTickLast = -1L;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUIInputInternalController(GUINode node) {
		super(node);
		this.index = 0;
		this.offset = 0;
		this.isDragging = false;
		this.dragPosition = new int[] {0,0};
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
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		inputNode = (GUIElementNode)node.getParentControllerNode();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#dispose()
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
	 * @return index
	 */
	protected int getIndex() {
		return index;
	}

	/**
	 * @return offset
	 */
	protected int getOffset() {
		return offset;
	}

	/**
	 * Reset cursor mode
	 */
	protected void resetCursorMode() {
		cursorModeStarted = System.currentTimeMillis();
		cursorMode = CursorMode.SHOW;
	}

	/**
	 * @return cursor mode
	 */
	protected CursorMode getCursorMode() {
		// if not yet started?
		if (cursorModeStarted == -1) {
			// reset
			resetCursorMode();
			return cursorMode;
		}

		// check if to toggle?
		if (System.currentTimeMillis() - cursorModeStarted > CURSOR_MODE_DURATION) {
			// yep, toggle cursor mode
			cursorMode = cursorMode == CursorMode.SHOW?CursorMode.HIDE:CursorMode.SHOW;
			// update when started
			cursorModeStarted = System.currentTimeMillis();
		}

		// return
		return cursorMode;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleMouseEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUINode node, GUIMouseEvent event) {
		// receive no events if disabled
		boolean disabled = ((GUIInputController)inputNode.getController()).isDisabled();
		if (disabled == true) {
			return;
		}

		// mouse released
		if (node == this.node &&
			event.getType() == Type.MOUSE_RELEASED == true) {
			isDragging = false;
		} else
		// mouse click, dragging on node
		if (node == this.node &&
			node.isEventBelongingToNode(event) == true &&
			(event.getType() == Type.MOUSE_PRESSED == true ||
			event.getType() == Type.MOUSE_DRAGGED == true) &&
			event.getButton() == 1) {

			//
			GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);
			index = textInputNode.getFont().getTextIndexByX(
				textInputNode.getText(), 
				offset,
				0,
				event.getX() - (textInputNode.computedConstraints.left + textInputNode.computedConstraints.alignmentLeft + textInputNode.border.left + textInputNode.padding.left)
			);
			resetCursorMode();

			// set event processed
			event.setProcessed(true);

			//
			isDragging = true;
			dragPosition[0] = 0;
			dragPosition[1] = 0;
		} else
		if (isDragging == true) {
			// determine new dragging position, if dragging
			node.getEventOffNodeRelativePosition(event, dragPosition);

			// set event processed
			event.setProcessed(true);
		}
	}

	/**
	 * Check and correct offset
	 */
	private void checkOffset() {
		// check offset left
		if (index < offset) {
			offset = index;
			return;
		}

		// check offset right
		GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);

		//
		ComputedConstraints textInputNodeConstraints = textInputNode.computedConstraints;
		Border textInputNodeBorder = textInputNode.border;
		Padding textInputNodePadding = textInputNode.padding;
		int textInputNodeWidth = textInputNodeConstraints.width - textInputNodeBorder.left - textInputNodeBorder.right - textInputNodePadding.left - textInputNodePadding.right;

		// determine chars max to render in input field beginning from offset
		int charsMax =
			textInputNode.getFont().getTextIndexByX(textInputNode.getText(), offset, 0, textInputNodeWidth) -
			offset;

		// correct offset
		if (index - offset >= charsMax) {
			offset = index - charsMax;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		// receive no events if disabled
		boolean disabled = ((GUIInputController)inputNode.getController()).isDisabled();
		if (disabled == true) {
			return;
		}

		//
		if (node == this.node) {
			GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);
			char keyChar = event.getKeyChar();
			if (disabled == false &&
				keyChar >= 32 && keyChar < 127) {
				// set event processed
				event.setProcessed(true);

				// check if key pressed
				if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
					if (textInputNode.getMaxLength() == 0 || 
						textInputNode.getText().length() < textInputNode.getMaxLength()) {
						//
						textInputNode.getText().insert(index, event.getKeyChar());
						index++;
						resetCursorMode();

						//
						checkOffset();

						// delegate change event
						node.getScreenNode().delegateValueChanged((GUIElementNode)node.getParentControllerNode());
					}
				}
			} else {
				switch (event.getKeyCode()) {
					case GUIKeyboardEvent.KEYCODE_LEFT:
						{
							// set event processed
							event.setProcessed(true);

							// check if key pressed
							if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
								if (index > 0) {
									index--;

									//
									checkOffset();

									//
									resetCursorMode();
								}
							}
						}
						break;
					case GUIKeyboardEvent.KEYCODE_RIGHT:
						{
							// set event processed
							event.setProcessed(true);

							// check if key pressed
							if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
								if (index < textInputNode.getText().length()) {
									index++;

									//
									checkOffset();

									//
									resetCursorMode();
								}
							}
						}
						break;
					case GUIKeyboardEvent.KEYCODE_BACKSPACE:
						{
							if (disabled == false) {
								// set event processed
								event.setProcessed(true);
	
								// check if key pressed
								if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
									if (index > 0) {
										textInputNode.getText().delete(index - 1, 1);
										index--;
	
										//
										checkOffset();
	
										//
										resetCursorMode();
	
										// delegate change event
										node.getScreenNode().delegateValueChanged((GUIElementNode)node.getParentControllerNode());
									}
								}
							}
						}
						break;
					case GUIKeyboardEvent.KEYCODE_DELETE:
						{
							if (disabled == false) {
								// set event processed
								event.setProcessed(true);
	
								// check if key pressed
								if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
									if (index < textInputNode.getText().length()) {
										textInputNode.getText().delete(index, 1);
										resetCursorMode();
	
										// delegate change event
										node.getScreenNode().delegateValueChanged((GUIElementNode)node.getParentControllerNode());
									}
								}
							}
						}
						break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#tick()
	 */
	public void tick() {
		// dragging
		if (isDragging == true) {
			long now = System.currentTimeMillis();

			// calm down
			if (draggingTickLast != -1L && now - draggingTickLast < DRAGGING_CALMDOWN) {
				return;
			}

			// dragging last time
			draggingTickLast = now;

			// do the job
			GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);

			// check if to scroll
			//	left?
			if (dragPosition[0] < 0) {
				if (index > 0) {
					index--;

					//
					checkOffset();
				}
			} else
			// 	right?
			if (dragPosition[0] > 0) {
				if (index < textInputNode.getText().length()) {
					index++;

					//
					checkOffset();
				}
			}
		}
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
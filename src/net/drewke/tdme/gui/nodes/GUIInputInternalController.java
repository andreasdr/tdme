package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.utils.MutableString;

/**
 * GUI input internal controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIInputInternalController extends GUINodeController {

	private final static long CURSOR_MODE_DURATION = 500L;

	public enum CursorMode {SHOW, HIDE};

	private long cursorModeStarted = -1L;
	private CursorMode cursorMode = CursorMode.SHOW;
	private int index;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUIInputInternalController(GUINode node) {
		super(node);
		this.index = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINodeController#init()
	 */
	public void init() {
		// no op
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
		if (node == this.node &&
			node.isEventBelongingToNode(event) && 
			(event.getType() == Type.MOUSE_PRESSED == true ||
			event.getType() == Type.MOUSE_RELEASED == true ||
			event.getType() == Type.MOUSE_DRAGGED == true) &&
			event.getButton() == 1) {

			//
			GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);
			index = textInputNode.getFont().getTextIndexByX(
				textInputNode.getText(), 
				event.getX() - (textInputNode.computedConstraints.left + textInputNode.computedConstraints.alignmentLeft + textInputNode.border.left + textInputNode.padding.left)
			);
			resetCursorMode();

			// set event processed
			event.setProcessed(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		if (node == this.node) {
			GUIInputInternalNode textInputNode = ((GUIInputInternalNode)node);
			char keyChar = event.getKeyChar();
			if (keyChar >= 32 && keyChar < 127) {
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
									resetCursorMode();
								}
							}
						}
						break;
					case GUIKeyboardEvent.KEYCODE_BACKSPACE:
						{
							// set event processed
							event.setProcessed(true);

							// check if key pressed
							if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
								if (index > 0) {
									textInputNode.getText().delete(index - 1, 1);
									index--;
									resetCursorMode();

									// delegate change event
									node.getScreenNode().delegateValueChanged((GUIElementNode)node.getParentControllerNode());
								}
							}
						}
						break;
					case GUIKeyboardEvent.KEYCODE_DELETE:
						{
							// set event processed
							event.setProcessed(true);

							// check if key pressed
							if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
								if (index < textInputNode.getText().length()) {
									textInputNode.getText().delete(index, 1);
									index--;
									resetCursorMode();

									// delegate change event
									node.getScreenNode().delegateValueChanged((GUIElementNode)node.getParentControllerNode());
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
		// no op
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
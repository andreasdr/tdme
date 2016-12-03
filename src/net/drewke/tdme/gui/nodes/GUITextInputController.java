package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;

/**
 * GUI input controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUITextInputController extends GUINodeController {

	private final static long CURSOR_MODE_DURATION = 500L;

	public enum CursorMode {SHOW, HIDE};

	private long cursorModeStarted = -1L;
	private CursorMode cursorMode = CursorMode.SHOW;
	private int index;

	/**
	 * GUI Checkbox controller
	 * @param node
	 */
	protected GUITextInputController(GUINode node) {
		super(node);
		this.index = 0;
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
	 * @return index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Reset cursor mode
	 */
	public void resetCursorMode() {
		cursorModeStarted = System.currentTimeMillis();
		cursorMode = CursorMode.SHOW;
	}

	/**
	 * @return cursor mode
	 */
	public CursorMode getCursorMode() {
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
			GUITextInputNode textInputNode = ((GUITextInputNode)node);
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
		GUITextInputNode textInputNode = ((GUITextInputNode)node);
		char keyChar = event.getKeyChar();
		if (keyChar >= 32 && keyChar < 127) {
			// set event processed
			event.setProcessed(true);

			// check if key pressed
			if (event.getType() == GUIKeyboardEvent.Type.KEY_PRESSED) {
				String text = textInputNode.getText();
				textInputNode.setText(
					text.substring(0, index) +
					keyChar +
					text.substring(index, text.length())
				);
				index++;
				resetCursorMode();
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
	
								// set event processed
								event.setProcessed(true);
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
	
								// set event processed
								event.setProcessed(true);
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
								String text = textInputNode.getText();
								textInputNode.setText(
									text.substring(0, index - 1) +
									text.substring(index, text.length())
								);
								index--;
								resetCursorMode();
	
								// set event processed
								event.setProcessed(true);
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
							String text = textInputNode.getText();
							if (index < text.length()) {
								textInputNode.setText(
									text.substring(0, index) +
									text.substring(index + 1, text.length())
								);
								index--;
								resetCursorMode();
	
								// set event processed
								event.setProcessed(true);
							}
						}
					}
					break;
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

}
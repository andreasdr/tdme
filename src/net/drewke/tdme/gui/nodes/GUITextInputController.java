package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;

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
			event.getType() == Type.MOUSE_RELEASED && 
			event.getButton() == 1) {
			//
			System.out.println(node.getId() + ": INPUT CLICKED");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINodeController#handleKeyboardEvent(net.drewke.tdme.gui.nodes.GUINode, net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUINode node, GUIKeyboardEvent event) {
		GUITextInputNode textInputNode = ((GUITextInputNode)node);
		switch (event.getType()) {
			case KEY_PRESSED:
				{
					char keyChar = event.getKeyChar();
					if (keyChar >= 32 && keyChar < 127) {
						String text = textInputNode.getText();
						textInputNode.setText(
							text.substring(0, index) +
							keyChar +
							text.substring(index, text.length())
						);
						index++;
						resetCursorMode();
					} else {
						switch (event.getKeyCode()) {
							case GUIKeyboardEvent.KEYCODE_LEFT:
								{
									if (index > 0) {
										index--;
										resetCursorMode();
									}
								}
								break;
							case GUIKeyboardEvent.KEYCODE_RIGHT:
								{
									if (index < textInputNode.getText().length()) {
										index++;
										resetCursorMode();
									}
								}
								break;
							case GUIKeyboardEvent.KEYCODE_BACKSPACE:
								{
									if (index > 0) {
										String text = textInputNode.getText();
										textInputNode.setText(
											text.substring(0, index - 1) +
											text.substring(index, text.length())
										);
										index--;
										resetCursorMode();
									}
								}
								break;
							case GUIKeyboardEvent.KEYCODE_DELETE:
								{
									String text = textInputNode.getText();
									if (index < text.length()) {
										textInputNode.setText(
											text.substring(0, index) +
											text.substring(index + 1, text.length())
										);
										index--;
										resetCursorMode();
									}
								}
								break;
						}
					}
					break;
				}
			case KEY_RELEASED:
				{
					break;
				}
			default:
				{
					break;
				}
		}
	}

}
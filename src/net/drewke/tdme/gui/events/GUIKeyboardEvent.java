package net.drewke.tdme.gui.events;

/**
 * GUI keyboard event
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIKeyboardEvent {

	public enum Type {NONE, KEY_PRESSED, KEY_RELEASED};

	private long time;

	private Type type;

	private int keyCode;
	private char keyChar;

	private boolean controlDown;
	private boolean altDown;
	private boolean shiftDown;

	private boolean processed;

	/**
	 * Public constructor
	 * 
	 */
	public GUIKeyboardEvent() {
		this.time = -1L;
		this.type = Type.NONE;
		this.keyCode = -1;
		this.keyChar = '\0';
		this.controlDown = false;
		this.altDown = false;
		this.shiftDown = false;
	}

	/**
	 * @return time in milliseconds
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Time in milliseconds
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set type
	 * @param type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return key code
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * Set key code
	 * @param code
	 */
	public void setKeyCode(int code) {
		this.keyCode = code;
	}

	/**
	 * @return key char
	 */
	public char getKeyChar() {
		return keyChar;
	}

	/**
	 * Set key char
	 * @param key char
	 */
	public void setKeyChar(char keyChar) {
		this.keyChar = keyChar;
	}

	/**
	 * @return control down
	 */
	public boolean isControlDown() {
		return controlDown;
	}

	/**
	 * Set control down
	 * @param control down
	 */
	public void setControlDown(boolean controlDown) {
		this.controlDown = controlDown;
	}

	/**
	 * @return is alt down
	 */
	public boolean isAltDown() {
		return altDown;
	}

	/**
	 * Set alt down
	 * @param alt down
	 */
	public void setAltDown(boolean altDown) {
		this.altDown = altDown;
	}

	/**
	 * @return is shift down
	 */
	public boolean isShiftDown() {
		return shiftDown;
	}

	/**
	 * Set shift down
	 * @param shiftDown
	 */
	public void setShiftDown(boolean shiftDown) {
		this.shiftDown = shiftDown;
	}

	/**
	 * @return event has been processed already
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * Set event processed
	 * @param processed
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	
}

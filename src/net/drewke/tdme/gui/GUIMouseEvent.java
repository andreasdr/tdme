package net.drewke.tdme.gui;

/**
 * GUI mouse event
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIMouseEvent {

	public enum Type {NONE, MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_MOVED};

	protected long time;

	protected Type type;

	protected int x;
	protected int y;
	protected int button;

	protected boolean processed;

	/**
	 * Public constructor
	 * @param type
	 * @param x
	 * @param y
	 * @param button
	 */
	public GUIMouseEvent() {
		this.time = System.currentTimeMillis();
		this.type = Type.NONE;
		this.x = -1;
		this.y = -1;
		this.button = -1;
		this.processed = false;
	}

	/**
	 * @return time in milliseconds
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Set time
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
	 * @return x
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set x
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return y
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set y
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return button
	 */
	public int getButton() {
		return button;
	}

	/**
	 * Set button
	 * @param button
	 */
	public void setButton(int button) {
		this.button = button;
	}

	/**
	 * @return processed
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * Set processed 
	 * @param processed
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "GUIMouseEvent [time=" + time + ", type=" + type + ", x=" + x
				+ ", y=" + y + ", button=" + button + "]";
	}

}
package net.drewke.tdme.gui.events;

/**
 * GUI mouse event
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIMouseEvent {

	public enum Type {NONE, MOUSE_WHEEL_MOVED, MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_MOVED, MOUSE_DRAGGED};

	private long time;

	private Type type;

	private int x;
	private int y;
	private int button;

	private float wheelX;
	private float wheelY;
	private float wheelZ;

	private boolean processed;

	/**
	 * Public constructor
	 * @param type
	 * @param x
	 * @param y
	 * @param button
	 */
	public GUIMouseEvent() {
		this.time = -1;
		this.type = Type.NONE;
		this.x = -1;
		this.y = -1;
		this.button = -1;
		this.wheelX = 0f;
		this.wheelY = 0f;
		this.wheelZ = 0f;
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
	 * @return wheel x
	 */
	public float getWheelX() {
		return wheelX;
	}

	/**
	 * Set up wheel x
	 * @param wheel x
	 */
	public void setWheelX(float wheelX) {
		this.wheelX = wheelX;
	}

	/**
	 * @return wheel y
	 */
	public float getWheelY() {
		return wheelY;
	}

	/**
	 * Set up wheel y
	 * @param wheel y
	 */
	public void setWheelY(float wheelY) {
		this.wheelY = wheelY;
	}

	/**
	 * @return wheel z
	 */
	public float getWheelZ() {
		return wheelZ;
	}

	/**
	 * Set up wheel z
	 * @param wheel z
	 */
	public void setWheelZ(float wheelZ) {
		this.wheelZ = wheelZ;
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
				+ ", y=" + y + ", button=" + button + ", wheelX=" + wheelX
				+ ", wheelY=" + wheelY + ", wheelZ=" + wheelZ + ", processed="
				+ processed + "]";
	}

}

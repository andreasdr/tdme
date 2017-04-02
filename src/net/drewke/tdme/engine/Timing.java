package net.drewke.tdme.engine;

/**
 * Timing
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Timing {

	final public static long UNDEFINED = -1;

	private int frame;
	private long startTime;

	private long lastFrameAtTime = UNDEFINED;
	private long currentFrameAtTime = UNDEFINED;
	private float currentFPS = UNDEFINED;

	/**
	 * Public default constructor
	 */
	protected Timing() {
		frame = 0;
		startTime = System.currentTimeMillis();

		lastFrameAtTime = UNDEFINED;
		currentFrameAtTime = UNDEFINED;
		currentFPS = 60f;
	}

	/**
	 * Updates timing
	 */
	protected void updateTiming() {
		lastFrameAtTime = currentFrameAtTime;
		currentFrameAtTime = System.currentTimeMillis();
		currentFPS = -1;
		if (lastFrameAtTime != UNDEFINED) {
			currentFPS = 1000f / ((currentFrameAtTime - lastFrameAtTime));
		}
		frame++;
	}

	/**
	 * @return frames that have been rendered
	 */
	public int getFrame() {
		return frame;
	}

	/**
	 * @return start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return time last frame has been rendered in ms
	 */
	public long getLastFrameAtTime() {
		return lastFrameAtTime;
	}

	/**
	 * @return time current frame has been rendered in ms
	 */
	public long getCurrentFrameAtTime() {
		return currentFrameAtTime;
	}

	/**
	 * Gets the time passed between last and current frame
	 * @return delta time
	 */
	public long getDeltaTime() {
		if (currentFrameAtTime == UNDEFINED || lastFrameAtTime == UNDEFINED) {
			return 0l;
		}
		return currentFrameAtTime - lastFrameAtTime;
	}

	/**
	 * @return current fps
	 */
	public float getCurrentFPS() {
		return currentFPS;
	}

	/**
	 * @return average fps
	 */
	public float getAverageFPS() {
		long seconds = (System.currentTimeMillis() - startTime) / 1000L;
		return frame / seconds;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Timing [frame=" + frame + ", startTime=" + startTime
				+ ", lastFrameAtTime=" + lastFrameAtTime
				+ ", currentFrameAtTime=" + currentFrameAtTime
				+ ", currentFPS=" + currentFPS + "]";
	}

}
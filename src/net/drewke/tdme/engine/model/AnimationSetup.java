package net.drewke.tdme.engine.model;

/**
 * AnimationSetup
 * @author Andreas Drewke
 * @version $Id$
 */
public final class AnimationSetup {

	private Model model;
	private String id;
	private int startFrame;
	private int endFrame;
	private int frames;
	private boolean loop;
	private String overlayFromGroupId;

	/**
	 * Public constructor
	 * @param model
	 * @param id
	 * @param start frame
	 * @param end frame
	 * @param loop
	 * @param overlay from group id / optional 
	 */
	public AnimationSetup(Model model, String id, int startFrame, int endFrame, boolean loop, String overlayFromGroupId) {
		this.model = model;
		this.id = id;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		this.frames = (endFrame - startFrame) + 1;
		this.loop = loop;
		this.overlayFromGroupId = overlayFromGroupId; 
	}

	/**
	 * @return model this animation belongs to
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return start frame
	 */
	public int getStartFrame() {
		return startFrame;
	}

	/**
	 * @return end frame
	 */
	public int getEndFrame() {
		return endFrame;
	}

	/**
	 * @return frames
	 */
	public int getFrames() {
		return frames;
	}

	/**
	 * @return looping enabled
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * If this is a overlay animation this returns a group id from which group the animation will start in the hierarchy
	 * @return group id from which the animation will start in the hierarchy
	 */
	public String getOverlayFromGroupId() {
		return overlayFromGroupId;
	}

	/**
	 * @return animation duration in milliseconds
	 */
	public long computeDuration() {
		return computeDuration(startFrame, endFrame);
	}

	/**
	 * @param frames
	 * @return animation duration in milliseconds
	 */
	public long computeDuration(int startFrame, int endFrame) {
		return (long)((float)(endFrame - startFrame + 1) / (float)model.getFPS() * 1000.0f);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "AnimationSetup [id=" + id + ", startFrame=" + startFrame
				+ ", endFrame=" + endFrame + ", frames=" + frames + ", loop="
				+ loop + ", overlayFromGroupId=" + overlayFromGroupId + "]";
	}

}

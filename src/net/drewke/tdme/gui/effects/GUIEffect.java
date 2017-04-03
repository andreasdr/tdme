package net.drewke.tdme.gui.effects;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.gui.renderer.GUIRenderer;

/**
 * GUI Effect base class
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIEffect {

	protected boolean active;

	protected float timeTotal;
	protected float timeLeft;
	protected float timePassed;

	/**
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return time total
	 */
	public float getTimeTotal() {
		return timeTotal;
	}

	/**
	 * Set time total
	 * @param time total
	 */
	public void setTimeTotal(float timeTotal) {
		this.timeTotal = timeTotal;
	}

	/**
	 * Reset this effect, set it to inactive
	 */
	public void reset() {
		timeLeft = timeTotal;
		active = false;
	}

	/**
	 * Start this effect
	 */
	public void start() {
		timeLeft = timeTotal;
		timePassed = 0f;
		active = true;
	}

	/**
	 * Updates the effect to GUI renderer and updates time
	 * @param gui renderer
	 */
	public void update(GUIRenderer guiRenderer) {
		timePassed = (float)(Engine.getInstance().getTiming().getDeltaTime()) / 1000f;
		timeLeft-= timePassed;
		if (timeLeft < 0.0f) {
			timeLeft = 0.0f;
			active = false;
		}
	}

}

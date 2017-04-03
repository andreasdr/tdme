package net.drewke.tdme.gui.effects;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.gui.events.Action;
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

	protected Action action;

	/**
	 * Public constructor
	 */
	public GUIEffect() {
		active = false;
		timeTotal = 0f;
		timeLeft = timeTotal;
		timePassed = 0f;
	}

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
	 * @return action to be performed on effect end
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Set action to be performed on effect end
	 * @param action
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Start this effect
	 */
	public void start() {
		active = true;
		timeLeft = timeTotal;
		timePassed = 0f;
	}

	/**
	 * Updates the effect to GUI renderer and updates time
	 * @param gui renderer
	 */
	public void update(GUIRenderer guiRenderer) {
		// time passed
		timePassed = (float)(Engine.getInstance().getTiming().getDeltaTime()) / 1000f;
		timeLeft-= timePassed;
		// time over
		if (timeLeft < 0.0f) {
			timeLeft = 0.0f;
			active = false;
			if (action != null) {
				// not sure if performing action in this application control flow is a good idea, will see
				action.performAction();
			}
		}
		// apply if active
		if (active == true) {
			apply(guiRenderer);
		}
	}

	/**
	 * Apply effect
	 * @param GUI renderer
	 */
	abstract public void apply(GUIRenderer guiRenderer);

}

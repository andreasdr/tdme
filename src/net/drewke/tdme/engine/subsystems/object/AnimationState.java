package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.model.AnimationSetup;

/**
 * Animation state class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class AnimationState {

	protected AnimationSetup setup = null;
	protected long currentAtTime = 0;
	protected long lastAtTime = 0;
	protected boolean finished = false;
	protected float time = 0.0f;		
}

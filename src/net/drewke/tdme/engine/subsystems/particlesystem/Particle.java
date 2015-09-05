package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;

/**
 * Particle entity
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Particle {

	protected boolean active = false;
	protected Vector3 velocity = new Vector3();
	protected Vector3 position = new Vector3();
	protected float mass;
	protected long lifeTimeMax;
	protected long lifeTimeCurrent;
	protected Color4 color = new Color4(1f,1f,1f,1f);
	protected Color4 colorAdd = new Color4(0f,0f,0f,0f);

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Particle [active=" + active + ", velocity=" + velocity
				+ ", position=" + position + ", mass=" + mass
				+ ", lifeTimeMax=" + lifeTimeMax + ", lifeTimeCurrent="
				+ lifeTimeCurrent + ", color=" + color + ", colorAdd="
				+ colorAdd + "]";
	}

}
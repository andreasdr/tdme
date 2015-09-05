package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;

/**
 * Particle Emitter Interface
 * @author Andreas Drewke
 * @version $Id$
 */
public interface ParticleEmitter {

	/**
	 * @return number of particles to emit in one second
	 */
	public int getCount();

	/**
	 * @return particle velocity
	 */
	public Vector3 getVelocity();

	/**
	 * @return particle velocity rnd
	 */
	public Vector3 getVelocityRnd();

	/**
	 * @return color start
	 */
	public Color4 getColorStart();

	/**
	 * @return color end
	 */
	public Color4 getColorEnd();

	/**
	 * Emits particles
	 * @param particle
	 */
	public void emit(Particle particle);

	/**
	 * Update transformation with given transformations
	 * @param transformations
	 */
	public void fromTransformations(Transformations transformations);


}

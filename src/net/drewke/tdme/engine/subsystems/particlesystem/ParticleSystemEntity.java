package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;

/**
 * Particle System Entity Interface
 * @author Andreas Drewke
 * @version $Id$
 */
public interface ParticleSystemEntity {

	/**************************************************************************
	 * General Data                                                           *
	 **************************************************************************/

	/**
	 * @return object id
	 */
	public String getId();

	/**
	 * @return true if enabled to be rendered
	 */
	public boolean isEnabled();

	/**
	 * Enable/disable rendering
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @return if object is pickable
	 */
	public boolean isPickable();

	/**
	 * Set this object pickable
	 * @param pickable
	 */
	public void setPickable(boolean pickable);

	/**************************************************************************
	 * Effect Color                                                           *
	 **************************************************************************/

	/**
	 * The effect color will be multiplied with fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorMul();

	/**
	 * The effect color will be added to fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorAdd();

	/**************************************************************************
	 * Particle Methods                                                       *
	 **************************************************************************/

	/**
	 * @return particle emitter
	 */
	public ParticleEmitter getParticleEmitter();

	/**
	 * Updates the particle entity
	 */
	public void updateParticles();

	/**
	 * Adds particles to this particle entity at given position
	 */
	public int emitParticles();

	/**
	 * Set up this transformations from given transformations
	 * @param transformations
	 */
	public void fromTransformations(Transformations transformations);

	/**
	 * Update transformations
	 */
	public void update();

}

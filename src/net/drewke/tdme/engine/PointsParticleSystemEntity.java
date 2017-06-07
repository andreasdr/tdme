package net.drewke.tdme.engine;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter;
import net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal;

/**
 * Point particle system entity to be used with engine class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PointsParticleSystemEntity extends PointsParticleSystemEntityInternal implements Entity {

	/**
	 * Public constructor
	 * @param id
	 * @param do collision tests
	 * @param emitter
	 * @param max points
	 * @param auto emit
	 */
	public PointsParticleSystemEntity(String id, boolean doCollisionTests, ParticleEmitter emitter, int maxPoints, boolean autoEmit) {
		super(id, doCollisionTests, emitter, maxPoints, autoEmit);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#initialize()
	 */
	public void initialize() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#getBoundingBoxTransformed()
	 */
	public BoundingBox getBoundingBoxTransformed() {
		return boundingBoxTransformed;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		super.fromTransformations(transformations);
		if (engine != null && enabled == true) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal#update()
	 */
	public void update() {
		super.update();
		if (engine != null && enabled == true) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		// return if enable state has not changed
		if (this.enabled == enabled) return;

		// otherwise add or remove from partition
		if (enabled == true) {
			if (engine != null) engine.partition.addEntity(this);
		} else {
			if (engine != null) engine.partition.removeEntity(this);
		}

		// call parent class::setEnabled()
		super.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ObjectParticleSystemEntityInternal#updateParticles()
	 */
	public void updateParticles() {
		super.updateParticles();
		if (engine != null && enabled == true) engine.partition.updateEntity(this);
	}

}

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
	 */
	public PointsParticleSystemEntity(String id, boolean doCollisionTests, ParticleEmitter emitter, int maxPoints) {
		super(id, doCollisionTests, emitter, maxPoints);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#init()
	 */
	public void init() {
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
		if (engine != null) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal#update()
	 */
	public void update() {
		super.update();
		if (engine != null) engine.partition.updateEntity(this);
	}

}

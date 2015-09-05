package net.drewke.tdme.engine;

import java.util.ArrayList;

import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.particlesystem.ObjectParticleSystemEntityInternal;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter;
import net.drewke.tdme.math.Vector3;

/**
 * Object particle system entity to be used with engine class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ObjectParticleSystemEntity extends ObjectParticleSystemEntityInternal implements Entity {

	/**
	 * Public constructor
	 * @param id
	 * @param model
	 * @param scale
	 * @param enable dynamic shadows
	 * @param max count
	 * @param emitter
	 */
	public ObjectParticleSystemEntity(String id, Model model, Vector3 scale, boolean enableDynamicShadows, int maxCount, ParticleEmitter emitter) {
		super(id, model, scale, enableDynamicShadows, maxCount, emitter);
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
		return boundingBox;
	}

	/**
	 * @return enabled objects
	 */
	public ArrayList<Object3D> getEnabledObjects() {
		return enabledObjects;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ObjectParticleSystemEntityInternal#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		super.fromTransformations(transformations);
		if (engine != null) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ObjectParticleSystemEntityInternal#update()
	 */
	public void update() {
		super.update();
		if (engine != null) engine.partition.updateEntity(this);
	}

}

package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.math.Vector3;

/**
 * Sphere particle emitter
 * @author Andreas Drewke
 * @version $Id$
 */
public final class SphereParticleEmitter implements ParticleEmitter {

	private int count;
	private long lifeTime;
	private long lifeTimeRnd;
	private float mass;
	private float massRnd;
	private Sphere sphere;
	private Sphere sphereTransformed;
	private Vector3 velocity;
	private Vector3 velocityRnd;
	private Color4 colorStart;
	private Color4 colorEnd;

	/**
	 * @param number of particles to emit in one second
	 * @param life time in milli seconds
	 * @param life time rnd in milli seconds
	 * @param mass in kg
	 * @param mass rnd in kg
	 * @param sphere
	 * @param velocity in meter / seconds
	 * @param velocity rnd in meter / seconds
	 * @param color start
	 * @param color end
	 */
	public SphereParticleEmitter(
		int count,
		long lifeTime, long lifeTimeRnd, 
		float mass, float massRnd,
		Sphere sphere,
		Vector3 velocity, Vector3 velocityRnd,
		Color4 colorStart, Color4 colorEnd) {
		this.count = count;
		this.lifeTime = lifeTime;
		this.lifeTimeRnd = lifeTimeRnd;
		this.mass = mass;
		this.massRnd = massRnd;
		this.sphere = sphere;
		this.sphereTransformed = (Sphere)sphere.clone();
		this.velocity = velocity;
		this.velocityRnd = velocityRnd;
		this.colorStart = colorStart;
		this.colorEnd = colorEnd;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleEmitter#getCount()
	 */
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getVelocity()
	 */
	public Vector3 getVelocity() {
		return velocity;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getVelocityRnd()
	 */
	public Vector3 getVelocityRnd() {
		return velocityRnd;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getColorStart()
	 */
	public Color4 getColorStart() {
		return colorStart;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getColorEnd()
	 */
	public Color4 getColorEnd() {
		return colorEnd;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleEmitter#emit(net.drewke.tdme.engine.Particle)
	 */
	public void emit(Particle particle) {
		float[] velocityXYZ = velocity.getArray();
		float[] velocityRndXYZ = velocityRnd.getArray();

		// set up particle
		particle.active = true;
		particle.position.
			set(
				(float)Math.random() * 2f - 1f,
				(float)Math.random() * 2f - 1f,
				(float)Math.random() * 2f - 1f
			).
			normalize().
			scale(sphereTransformed.getRadius());
		particle.position.add(sphereTransformed.getCenter());
		particle.velocity.set(
			velocityXYZ[0] + (float)(Math.random() * velocityRndXYZ[0] * (Math.random() > 0.5?+1.0f:-1.0f)),
			velocityXYZ[1] + (float)(Math.random() * velocityRndXYZ[1] * (Math.random() > 0.5?+1.0f:-1.0f)),
			velocityXYZ[2] + (float)(Math.random() * velocityRndXYZ[2] * (Math.random() > 0.5?+1.0f:-1.0f))
		);
		particle.mass = mass + (float)(Math.random() * (massRnd));
		particle.lifeTimeMax = lifeTime + (long)(Math.random() * lifeTimeRnd);
		particle.lifeTimeCurrent = 0l;
		particle.color.set(colorStart);
		particle.colorAdd.set(
			(colorEnd.getRed() - colorStart.getRed()) / particle.lifeTimeMax,
			(colorEnd.getGreen() - colorStart.getGreen()) / particle.lifeTimeMax,
			(colorEnd.getBlue() - colorStart.getBlue()) / particle.lifeTimeMax,
			(colorEnd.getAlpha() - colorStart.getAlpha()) / particle.lifeTimeMax
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		sphereTransformed.fromBoundingVolumeWithTransformations(sphere, transformations);
	}

}

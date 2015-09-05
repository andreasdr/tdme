package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Basic particle emitter
 * @author Andreas Drewke
 * @version $Id$
 */
public final class BasicParticleEmitter implements ParticleEmitter {

	private int count;
	private long lifeTime;
	private long lifeTimeRnd;
	private float mass;
	private float massRnd;
	private Vector3 position;
	private Vector3 positionTransformed;
	private Vector3 velocity;
	private Vector3 velocityRnd;
	private Vector3 zeroPosition;
	private Color4 colorStart;
	private Color4 colorEnd;

	/**
	 * @param number of particles to emit in one second
	 * @param life time in milli seconds
	 * @param life time rnd in milli seconds
	 * @param mass in kg
	 * @param mass rnd in kg
	 * @param velocity in meter / seconds
	 * @param velocity rnd in meter / seconds
	 */
	public BasicParticleEmitter(
		int count,
		long lifeTime, long lifeTimeRnd, 
		float mass, float massRnd,
		Vector3 position,
		Vector3 velocity, Vector3 velocityRnd,
		Color4 colorStart, Color4 colorEnd) {
		this.count = count;
		this.lifeTime = lifeTime;
		this.lifeTimeRnd = lifeTimeRnd;
		this.mass = mass;
		this.massRnd = massRnd;
		this.position = position;
		this.positionTransformed = new Vector3();
		this.positionTransformed.set(position);
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
		particle.position.set(positionTransformed);
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
		//
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();

		// apply translations
		transformationsMatrix.multiply(position, positionTransformed);
	}

}

package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.math.Vector3;

/**
 * Sphere particle emitter
 * @author Andreas Drewke
 * @version $Id$
 */
public final class BoundingBoxParticleEmitter implements ParticleEmitter {

	private int count;
	private long lifeTime;
	private long lifeTimeRnd;
	private float mass;
	private float massRnd;
	private OrientedBoundingBox obb;
	private OrientedBoundingBox obbTransformed;
	private Vector3 velocity;
	private Vector3 velocityRnd;
	private Color4 colorStart;
	private Color4 colorEnd;

	private Vector3 tmpAxis = new Vector3();

	/**
	 * @param particles to emit in one second
	 * @param life time in milli seconds
	 * @param life time rnd in milli seconds
	 * @param mass in kg
	 * @param mass rnd in kg
	 * @param oriented bounding box
	 * @param velocity in meter / seconds
	 * @param velocity rnd in meter / seconds
	 * @param color start
	 * @param color end 
	 */
	public BoundingBoxParticleEmitter(
		int count,
		long lifeTime, long lifeTimeRnd, 
		float mass, float massRnd,
		OrientedBoundingBox obb,
		Vector3 velocity, Vector3 velocityRnd,
		Color4 colorStart, Color4 colorEnd) {
		this.count = count;
		this.lifeTime = lifeTime;
		this.lifeTimeRnd = lifeTimeRnd;
		this.mass = mass;
		this.massRnd = massRnd;
		this.obb = obb;
		this.velocity = velocity;
		this.velocityRnd = velocityRnd;
		this.colorStart = colorStart;
		this.colorEnd = colorEnd;
		this.obbTransformed = (OrientedBoundingBox)obb.clone();
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

		Vector3[] obbAxes = obbTransformed.getAxes();
		float[] obbHalfExtensionXYZ = obbTransformed.getHalfExtension().getArray();

		// emit particle on oriented bounding box
		particle.position.set(0f,0f,0f);
		particle.position.add(
			tmpAxis.set(obbAxes[0]).
			scale(
				((float)Math.random() * obbHalfExtensionXYZ[0] * 2f) -
				obbHalfExtensionXYZ[0]	
			)
		);
		particle.position.add(
			tmpAxis.set(obbAxes[1]).
			scale(
				((float)Math.random() * obbHalfExtensionXYZ[1] * 2f) -
				obbHalfExtensionXYZ[1]				
			)
		);
		particle.position.add(
			tmpAxis.set(obbAxes[2]).
			scale(
				((float)Math.random() * obbHalfExtensionXYZ[2] * 2f) -
				obbHalfExtensionXYZ[2]
			)
		);
		particle.position.add(obbTransformed.getCenter());

		// compute velocity
		particle.velocity.set(
			velocityXYZ[0] + (float)(Math.random() * velocityRndXYZ[0] * (Math.random() > 0.5?+1.0f:-1.0f)),
			velocityXYZ[1] + (float)(Math.random() * velocityRndXYZ[1] * (Math.random() > 0.5?+1.0f:-1.0f)),
			velocityXYZ[2] + (float)(Math.random() * velocityRndXYZ[2] * (Math.random() > 0.5?+1.0f:-1.0f))
		);

		// mass
		particle.mass = mass + (float)(Math.random() * (massRnd));

		// life time
		particle.lifeTimeMax = lifeTime + (long)(Math.random() * lifeTimeRnd);
		particle.lifeTimeCurrent = 0l;

		// color
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
		obbTransformed.fromBoundingVolumeWithTransformations(obb, transformations);
	}

}

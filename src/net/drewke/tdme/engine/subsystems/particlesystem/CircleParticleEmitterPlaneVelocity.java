package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Circle particle emitter with velocity that lives in plane only 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class CircleParticleEmitterPlaneVelocity implements ParticleEmitter {

	private int count;
	private long lifeTime;
	private long lifeTimeRnd;
	private Vector3 axis0;
	private Vector3 axis1;
	private Vector3 center;
	private float radius;
	private float mass;
	private float massRnd;
	private float velocity;
	private float velocityRnd;
	private Color4 colorStart;
	private Color4 colorEnd;

	private Vector3 centerTransformed;
	private float radiusTransformed;
	private Vector3 axis0Transformed;
	private Vector3 axis1Transformed;

	private Vector3 side;
	private Vector3 cosOnAxis0;
	private Vector3 sinOnAxis1;

	/**
	 * Public constructor
	 * @param particles to emit in one second
	 * @param life time
	 * @param life time rnd
	 * @param axis 0
	 * @param axis 1
	 * @param center
	 * @param radius
	 * @param mass
	 * @param mass rnd
	 * @param velocity
	 * @param velocity rnd
	 * @param color start
	 * @param color end
	 */
	public CircleParticleEmitterPlaneVelocity(
			int count, long lifeTime, long lifeTimeRnd,
			Vector3 axis0, Vector3 axis1, Vector3 center, float radius,
			float mass, float massRnd, float velocity, float velocityRnd,
			Color4 colorStart, Color4 colorEnd) {
		this.count = count;
		this.lifeTime = lifeTime;
		this.lifeTimeRnd = lifeTimeRnd;
		this.axis0 = axis0.normalize();
		this.axis1 = axis1.normalize();
		this.center = center;
		this.radius = radius;
		this.mass = mass;
		this.massRnd = massRnd;
		this.velocity = velocity;
		this.velocityRnd = velocityRnd;
		this.colorStart = colorStart;
		this.colorEnd = colorEnd;
		this.centerTransformed = this.center.clone();
		this.radiusTransformed = radius;
		this.axis0Transformed = axis0.clone();
		this.axis1Transformed = axis1.clone();
		this.side = new Vector3();
		this.cosOnAxis0 = new Vector3();
		this.sinOnAxis1 = new Vector3();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getCount()
	 */
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getVelocity()
	 */
	public Vector3 getVelocity() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#getVelocityRnd()
	 */
	public Vector3 getVelocityRnd() {
		return null;
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
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter#emit(net.drewke.tdme.engine.subsystems.particlesystem.Particle)
	 */
	public void emit(Particle particle) {
		// set up particle
		particle.active = true;

		// emit particle on circle spanned on axis 0 and axis 1
		float rnd = (float)Math.random();
		cosOnAxis0.set(axis0Transformed).scale((float)Math.cos(Math.PI * 2 * rnd));
		sinOnAxis1.set(axis1Transformed).scale((float)Math.sin(Math.PI * 2 * rnd));
		particle.position.set(cosOnAxis0);
		particle.position.add(sinOnAxis1);
		particle.position.scale(radiusTransformed);
		particle.position.add(centerTransformed);

		// compute velocity
		particle.velocity.
			set(
				particle.position
			).
			sub(
				centerTransformed
			).
			normalize().
			scale(velocity + (float)(Math.random() * velocityRnd));

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
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();

		// apply rotation, scale, translation
		transformationsMatrix.multiply(center, centerTransformed);

		// apply transformations rotation + scale to axis
		transformationsMatrix.multiplyNoTranslation(axis0, axis0Transformed);
		transformationsMatrix.multiplyNoTranslation(axis1, axis1Transformed);

		// note:
		//	sphere radius can only be scaled the same on all axes
		//	thats why its enough to only take x axis to determine scaling
		side.set(axis0).scale(radius).add(center);
		transformationsMatrix.multiply(side, side);
		radius = side.sub(center).computeLength();
	}

}

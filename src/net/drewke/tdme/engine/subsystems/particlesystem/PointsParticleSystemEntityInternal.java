package net.drewke.tdme.engine.subsystems.particlesystem;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.object.TransparentRenderPointsPool;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Points particle system entity internal
 * @author Andreas Drewke
 * @version $Id$
 */
public class PointsParticleSystemEntityInternal extends Transformations implements ParticleSystemEntity {

	protected String id;
	protected Engine engine;
	protected GLRenderer renderer;
	protected boolean autoEmit;
	protected boolean enabled;
	protected boolean active;
	protected boolean doCollisionTests;
	protected ParticleEmitter emitter;
	protected Particle[] particles;
	protected int maxPoints;
	protected TransparentRenderPointsPool pointsRenderPool;

	protected Vector3 velocityForTime;
	private Vector3 point;

	protected BoundingBox boundingBox;
	protected BoundingBox boundingBoxTransformed;
	protected Transformations inverseTransformation;

	protected Color4 effectColorMul;
	protected Color4 effectColorAdd;
	protected boolean pickable;

	protected float particlesToSpawnRemainder;

	/**
	 * Public constructor 
	 * @param id
	 * @param do collision tests
	 * @param emitter
	 * @param max points
	 * @param auto emit
	 */
	public PointsParticleSystemEntityInternal(String id, boolean doCollisionTests, ParticleEmitter emitter, int maxPoints, boolean autoEmit) {
		this.id = id;
		this.enabled = true;
		this.doCollisionTests = doCollisionTests;
		// will be activated on emit and auto unactivated if no more active particles
		this.active = false;
		this.emitter = emitter;
		particles = new Particle[maxPoints];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle();
		}
		this.maxPoints = maxPoints;
		velocityForTime = new Vector3();
		point = new Vector3();
		boundingBox = new BoundingBox();
		boundingBoxTransformed = new BoundingBox();
		inverseTransformation = new Transformations();
		this.effectColorMul = new Color4(1.0f, 1.0f, 1.0f, 1.0f);
		this.effectColorAdd = new Color4(0.0f, 0.0f, 0.0f, 0.0f);
		this.pickable = false;
		this.autoEmit = autoEmit;
		this.particlesToSpawnRemainder = 0f;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#setRenderer(net.drewke.tdme.engine.subsystems.renderer.GLRenderer)
	 */
	public void setRenderer(GLRenderer renderer) {
		this.renderer = renderer;
		this.pointsRenderPool = new TransparentRenderPointsPool(maxPoints);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#setEngine(net.drewke.tdme.engine.Engine)
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#getEffectColorMul()
	 */
	public Color4 getEffectColorMul() {
		return effectColorMul;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#getEffectColorAdd()
	 */
	public Color4 getEffectColorAdd() {
		return effectColorAdd;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#isPickable()
	 */
	public boolean isPickable() {
		return pickable;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#setPickable(boolean)
	 */
	public void setPickable(boolean pickable) {
		this.pickable = pickable;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#isAutoEmit()
	 */
	public boolean isAutoEmit() {
		return autoEmit;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#setAutoEmit(boolean)
	 */
	public void setAutoEmit(boolean autoEmit) {
		this.autoEmit = autoEmit;
	}

	/**
	 * @return dynamic shadowing enabled
	 */
	public boolean isDynamicShadowingEnabled() {
		return false;
	}

	/**
	 * Enable/disable dynamic shadowing
	 * @param dynamicShadowing
	 */
	public void setDynamicShadowingEnabled(boolean dynamicShadowing) {
		// no op
	}

	/**
	 * Update transformations
	 */
	public void update() {
		super.update();
		emitter.fromTransformations(this);
		inverseTransformation.getTransformationsMatrix().set(this.getTransformationsMatrix()).invert();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		super.fromTransformations(transformations);
		emitter.fromTransformations(transformations);
		inverseTransformation.getTransformationsMatrix().set(this.getTransformationsMatrix()).invert();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#update(net.drewke.tdme.engine.Engine)
	 */
	public void updateParticles() {
		if (enabled == false || active == false) return;

		// bounding box transformed min, max xyz 
		float bbMinXYZ[] = boundingBoxTransformed.getMin().getArray();
		float bbMaxXYZ[] = boundingBoxTransformed.getMax().getArray();

		//
		boolean haveBoundingBox = false;

		//
		float distanceFromCamera;
		Matrix4x4 modelViewMatrix = renderer.getModelViewMatrix();

		// compute distance from camera
		distanceFromCamera = -point.getZ();

		// process particles
		pointsRenderPool.reset();
		int activeParticles = 0;
		long timeDelta = engine.getTiming().getDeltaTime();
		for (int i = 0; i < particles.length; i++) {
			Particle particle = particles[i];
			if (particle.active == false) continue;

			// life time
			particle.lifeTimeCurrent+= timeDelta;
			// crop to max life time
			if (particle.lifeTimeCurrent >= particle.lifeTimeMax) {
				particle.active = false;
				continue;
			}

			// add gravity if our particle have a noticeable mass
			if (particle.mass > MathTools.EPSILON) particle.velocity.subY(0.5f * MathTools.g * (float)timeDelta/1000f);

			// TODO:
			//	maybe take air resistance into account like a huge paper needs more time to fall than a sphere of paper
			//	or heat for smoke or fire, whereas having no mass for those particles works around this problem for now

			// translation
			particle.position.add(velocityForTime.set(particle.velocity).scale((float)timeDelta/1000f));

			// color
			float[] color = particle.color.getArray();
			float[] colorAdd = particle.colorAdd.getArray();
			color[0]+= colorAdd[0] * (float)timeDelta;
			color[1]+= colorAdd[1] * (float)timeDelta;
			color[2]+= colorAdd[2] * (float)timeDelta;
			color[3]+= colorAdd[3] * (float)timeDelta;

			// transform particle position to camera space
			modelViewMatrix.multiply(particle.position, point);

			// check for collision
			if (doCollisionTests == true) {
				for(Entity entity: engine.getPartition().getObjectsNearTo(particle.position)) {
					// skip on our self
					if (entity == this) continue;
	
					// skip on other particle systems
					if (entity instanceof ParticleSystemEntity) continue;
			
					// do we have a collision?
					if (entity.getBoundingBoxTransformed().containsPoint(particle.position)) {
						particle.active = false;
						continue;
					}
				}
			}

			// 
			activeParticles++;

			// compute distance from camera
			distanceFromCamera = -point.getZ();

			// set up bounding box
			float[] positionXYZ = particle.position.getArray();
			if (haveBoundingBox == false) {
				System.arraycopy(positionXYZ, 0, bbMinXYZ, 0, 3);
				System.arraycopy(positionXYZ, 0, bbMaxXYZ, 0, 3);
				haveBoundingBox = true;
			} else {
				if (positionXYZ[0] < bbMinXYZ[0]) bbMinXYZ[0] = positionXYZ[0];
				if (positionXYZ[1] < bbMinXYZ[1]) bbMinXYZ[1] = positionXYZ[1];
				if (positionXYZ[2] < bbMinXYZ[2]) bbMinXYZ[2] = positionXYZ[2];
				if (positionXYZ[0] > bbMaxXYZ[0]) bbMaxXYZ[0] = positionXYZ[0];
				if (positionXYZ[1] > bbMaxXYZ[1]) bbMaxXYZ[1] = positionXYZ[1];
				if (positionXYZ[2] > bbMaxXYZ[2]) bbMaxXYZ[2] = positionXYZ[2];
			}

			// 
			pointsRenderPool.addPoint(point, particle.color, distanceFromCamera);
		}

		// auto disable particle system if no more active particles
		if (activeParticles == 0) {
			active = false;
			return;
		}

		// scale a bit up to make picking work better
		boundingBoxTransformed.getMin().sub(0.1f);
		boundingBoxTransformed.getMax().add(0.1f);

		// compute bounding boxes
		boundingBoxTransformed.update();
		boundingBox.fromBoundingVolumeWithTransformations(boundingBoxTransformed, inverseTransformation);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#dispose()
	 */
	public void dispose() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#getParticleEmitter()
	 */
	public ParticleEmitter getParticleEmitter() {
		return emitter;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#emit()
	 */
	public int emitParticles() {
		// enable particle system
		active = true;

		// delta time
		long timeDelta = engine.getTiming().getDeltaTime();

		// determine particles to spawn
		int particlesToSpawnInteger = 0;
		if (autoEmit == true) {
			float particlesToSpawn = emitter.getCount() * engine.getTiming().getDeltaTime() / 1000f;
			particlesToSpawnInteger = (int)particlesToSpawn;
			particlesToSpawnRemainder+= particlesToSpawn - particlesToSpawnInteger;
			if (particlesToSpawnRemainder > 1.0f) {
				particlesToSpawn+= 1.0f;
				particlesToSpawnInteger++;
				particlesToSpawnRemainder-= 1f;
			}
		} else {
			particlesToSpawnInteger = emitter.getCount();
		}

		// skip if nothing to spawn
		if (particlesToSpawnInteger == 0) return 0;

		// spawn
		int particlesSpawned = 0;
		for (int i = 0; i < particles.length; i++) {
			Particle particle = particles[i];
			if (particle.active == true) continue;

			// emit particle
			emitter.emit(particle);

			// add gravity if our particle have a noticable mass, add translation
			// add some movement with a min of 0 time delta and a max of engine time delta
			long timeDeltaRnd = (long)(Math.random() * (double)timeDelta);
			if (particle.mass > MathTools.EPSILON) particle.velocity.subY(0.5f * MathTools.g * (float)timeDeltaRnd/1000f);
			particle.position.add(velocityForTime.set(particle.velocity).scale((float)timeDeltaRnd/1000f));

			//
			particlesSpawned++;
			if (particlesSpawned == particlesToSpawnInteger) break;
		}

		return particlesSpawned;
	}

	/**
	 * @return render points pool
	 */
	public TransparentRenderPointsPool getRenderPointsPool() {
		return pointsRenderPool;
	}

}

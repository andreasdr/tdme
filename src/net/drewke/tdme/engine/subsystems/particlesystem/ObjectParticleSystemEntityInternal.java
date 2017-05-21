package net.drewke.tdme.engine.subsystems.particlesystem;

import java.util.ArrayList;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;

/**
 * Particle system which displays objects as particles
 * @author Andreas Drewke
 * @version $Id$
 */
public class ObjectParticleSystemEntityInternal extends Transformations implements ParticleSystemEntity {

	protected Engine engine;
	protected String id;
	protected boolean enabled;
	protected Model model;
	protected boolean autoEmit;
	protected boolean enableDynamicShadows;
	protected Particle[] particles;
	protected Object3D[] objects;
	protected ArrayList<Object3D> enabledObjects;
	protected BoundingBox boundingBox;
	protected BoundingBox boundingBoxTransformed;
	protected Transformations inverseTransformation;
	protected ParticleEmitter emitter;
	protected boolean pickable;
	protected Color4 effectColorMul;
	protected Color4 effectColorAdd;

	protected Vector3 velocityForTime;

	protected float particlesToSpawnRemainder;

	/**
	 * Public constructor
	 * @param id
	 * @param model
	 * @param scale
	 * @param auto emit
	 * @param enable dynamic shadows
	 * @param maxCount
	 * @param emitter
	 */
	public ObjectParticleSystemEntityInternal(
		String id,
		Model model, 	
		Vector3 scale,
		boolean autoEmit,
		boolean enableDynamicShadows,
		int maxCount,
		ParticleEmitter emitter) {
		this.id = id;
		this.enabled = true;
		this.model = model;
		this.autoEmit = autoEmit;
		this.enableDynamicShadows = enableDynamicShadows;
		this.enabledObjects = new ArrayList<Object3D>();
		particles = new Particle[maxCount];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle();
		}
		objects = new Object3D[maxCount];
		for (int i = 0; i < objects.length; i++) {
			objects[i] = new Object3D("tdme.opse." + this.hashCode() + ":" + i, model);
			objects[i].setEnabled(false);
			objects[i].getScale().set(scale);
			objects[i].setDynamicShadowingEnabled(enableDynamicShadows);
			objects[i].setPickable(false);
		}
		this.boundingBox = new BoundingBox();
		this.boundingBoxTransformed = new BoundingBox();
		this.inverseTransformation = new Transformations();
		this.emitter = emitter;
		this.velocityForTime = new Vector3();
		this.effectColorMul = new Color4(1.0f, 1.0f, 1.0f, 1.0f);
		this.effectColorAdd = new Color4(0.0f, 0.0f, 0.0f, 0.0f);
		this.pickable = false;
		this.particlesToSpawnRemainder = 0f;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleSystemEntity#getId()
	 */
	public String getId() {
		return id;
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
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#setRenderer(net.drewke.tdme.engine.subsystems.renderer.GLRenderer)
	 */
	public void setRenderer(GLRenderer renderer) {
		// no op
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
		return enabledObjects.size() > 0;
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
		return enableDynamicShadows;
	}

	/**
	 * Enable/disable dynamic shadowing
	 * @param dynamicShadowing
	 */
	public void setDynamicShadowingEnabled(boolean dynamicShadowing) {
		enableDynamicShadows = dynamicShadowing;
		for (int i = 0; i < objects.length; i++) {
			objects[i].setDynamicShadowingEnabled(enableDynamicShadows);
		}		
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
	 * @see net.drewke.tdme.engine.subsystems.particlesystem.ParticleSystemEntity#getParticleEmitter()
	 */
	public ParticleEmitter getParticleEmitter() {
		return emitter;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleSystemEntity#emit(net.drewke.tdme.math.Vector3)
	 */
	public int emitParticles() {
		// determine particles to spawn
		float particlesToSpawn = emitter.getCount() * engine.getTiming().getDeltaTime() / 1000f;

		// particles to spawn
		int particlesToSpawnInteger = 0;
		if (autoEmit == true) {
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

			// enable object
			Object3D object = objects[i];
			object.getTranslation().set(particle.position);
			object.update();
			object.setEnabled(true);
			object.getEffectColorAdd().set(effectColorAdd);
			object.getEffectColorMul().set(effectColorMul);
			enabledObjects.add(object);

			// all particles spawned?
			particlesSpawned++;
			if (particlesSpawned == particlesToSpawnInteger) break;
		}

		// done
		return particlesSpawned;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleSystemEntity#update()
	 */
	public void updateParticles() {
		boolean first = true;
		float bbMinXYZ[] = boundingBoxTransformed.getMin().getArray();
		float bbMaxXYZ[] = boundingBoxTransformed.getMax().getArray();
		long timeDelta = engine.getTiming().getDeltaTime();
		for (int i = 0; i < particles.length; i++) {
			Particle particle = particles[i];
			if (particle.active == false) continue;

			//
			Object3D object = objects[i];

			// life time
			particle.lifeTimeCurrent+= timeDelta;
			if (particle.lifeTimeCurrent >= particle.lifeTimeMax) {
				particle.active = false;
				object.setEnabled(false);
				enabledObjects.remove(object);
				continue;
			}

			// add gravity if our particle have a noticable mass
			if (particle.mass > MathTools.EPSILON) particle.velocity.subY(0.5f * MathTools.g * (float)timeDelta/1000f);

			// TODO:
			//	maybe take air resistance into account like a huge paper needs more time to fall than a sphere of paper
			//	or heat for smoke or fire, whereas having no mass for those particles works around this problem for now

			// update up effect colors
			object.getEffectColorAdd().set(effectColorAdd);
			object.getEffectColorMul().set(effectColorMul);

			// translation
			object.getTranslation().add(velocityForTime.set(particle.velocity).scale((float)timeDelta/1000f));
			object.update();
			if (first == true) {
				boundingBoxTransformed.getMin().set(object.getBoundingBoxTransformed().getMin());
				boundingBoxTransformed.getMax().set(object.getBoundingBoxTransformed().getMax());
				first = false;
			} else {
				float objBbMinXYZ[] = object.getBoundingBoxTransformed().getMin().getArray();
				float objBbMaxXYZ[] = object.getBoundingBoxTransformed().getMax().getArray();
				if (objBbMinXYZ[0] < bbMinXYZ[0]) bbMinXYZ[0] = objBbMinXYZ[0];
				if (objBbMinXYZ[1] < bbMinXYZ[1]) bbMinXYZ[1] = objBbMinXYZ[1];
				if (objBbMinXYZ[2] < bbMinXYZ[2]) bbMinXYZ[2] = objBbMinXYZ[2];
				if (objBbMaxXYZ[0] > bbMaxXYZ[0]) bbMaxXYZ[0] = objBbMaxXYZ[0];
				if (objBbMaxXYZ[1] > bbMaxXYZ[1]) bbMaxXYZ[1] = objBbMaxXYZ[1];
				if (objBbMaxXYZ[2] > bbMaxXYZ[2]) bbMaxXYZ[2] = objBbMaxXYZ[2];
			}
		}

		// compute bounding boxes
		boundingBoxTransformed.update();
		boundingBox.fromBoundingVolumeWithTransformations(boundingBoxTransformed, inverseTransformation);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.ParticleSystemEntity#dispose()
	 */
	public void dispose() {
		for (int i = 0; i < objects.length; i++) {
			objects[i].dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Transformations#toString()
	 */
	public String toString() {
		return "ObjectParticleSystemEntityInternal [id="
				+ id + ", enabled=" + enabled + ", model=" + model
				+ ", autoEmit=" + autoEmit + ", enableDynamicShadows="
				+ enableDynamicShadows + ", particles="
				+ Arrays.toString(particles) + ", objects="
				+ Arrays.toString(objects) + ", enabledObjects="
				+ enabledObjects + ", boundingBox=" + boundingBox
				+ ", boundingBoxTransformed=" + boundingBoxTransformed
				+ ", inverseTransformation=" + inverseTransformation
				+ ", emitter=" + emitter + ", pickable=" + pickable
				+ ", effectColorMul=" + effectColorMul + ", effectColorAdd="
				+ effectColorAdd + ", velocityForTime=" + velocityForTime
				+ ", particlesToSpawnRemainder=" + particlesToSpawnRemainder
				+ "]";
	}

}
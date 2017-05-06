package net.drewke.tdme.tools.leveleditor.logic;

import java.util.ArrayList;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.ObjectParticleSystemEntity;
import net.drewke.tdme.engine.PointsParticleSystemEntity;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.RigidBody;
import net.drewke.tdme.engine.physics.World;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.subsystems.particlesystem.ParticleEmitter;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.BoundingBoxParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitterPlaneVelocity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.ObjectParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.PointParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.PointParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.SphereParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.utils.MutableString;

/**
 * Level Editor Level Logic
 * @author Andreas Drewke
 * @version $Id$
 */
public class Level {

	public final static int RIGIDBODY_TYPEID_STATIC = 1;
	public final static int RIGIDBODY_TYPEID_PLAYER = 2;  

	private static MutableString compareMutableString = new MutableString();
	private static Transformations transformations = new Transformations();

	/**
	 * Set lights from level
	 * @param engine
	 * @param level
	 * @param translation
	 */
	public static void setLight(Engine engine, LevelEditorLevel level, Vector3 translation) {
		// load lights
		for (int i = 0; i < level.getLightCount(); i++) {
			engine.getLightAt(i).getAmbient().set(level.getLightAt(i).getAmbient());
			engine.getLightAt(i).getDiffuse().set(level.getLightAt(i).getDiffuse());
			engine.getLightAt(i).getSpecular().set(level.getLightAt(i).getSpecular());
			engine.getLightAt(i).getPosition().set(level.getLightAt(i).getPosition());
			engine.getLightAt(i).getSpotDirection().set(level.getLightAt(i).getSpotDirection());
			engine.getLightAt(i).setSpotExponent(level.getLightAt(i).getSpotExponent());
			engine.getLightAt(i).setSpotCutOff(level.getLightAt(i).getSpotCutOff());
			engine.getLightAt(i).setConstantAttenuation(level.getLightAt(i).getConstantAttenuation());
			engine.getLightAt(i).setLinearAttenuation(level.getLightAt(i).getLinearAttenuation());
			engine.getLightAt(i).setQuadraticAttenuation(level.getLightAt(i).getQuadraticAttenuation());
			engine.getLightAt(i).setEnabled(level.getLightAt(i).isEnabled());
			if (translation != null) {
				engine.getLightAt(i).getPosition().setX(engine.getLightAt(i).getPosition().getX() + translation.getX());	
				engine.getLightAt(i).getPosition().setY(engine.getLightAt(i).getPosition().getY() + translation.getY());
				engine.getLightAt(i).getPosition().setZ(engine.getLightAt(i).getPosition().getZ() + translation.getZ());
			}
		}
	}

	/**
	 * Create particle system
	 * @param level editor entity particle system
	 * @param id
	 * @param enable dynamic shadows
	 * @return engine entity
	 */
	public static Entity createParticleSystem(LevelEditorEntityParticleSystem particleSystem, String id, boolean enableDynamicShadows) {
		// create emitter for engine
		ParticleEmitter engineEmitter = null;

		//
		switch (particleSystem.getEmitter()) {
			case NONE:
				{
					return null;
				}
			case POINT_PARTICLE_EMITTER:
				{
					PointParticleEmitter emitter = particleSystem.getPointParticleEmitter();
					engineEmitter = new net.drewke.tdme.engine.subsystems.particlesystem.PointParticleEmitter(
						emitter.getCount(),
						emitter.getLifeTime(),
						emitter.getLifeTimeRnd(),
						emitter.getMass(),
						emitter.getMassRnd(),
						emitter.getPosition(),
						emitter.getVelocity(),
						emitter.getVelocityRnd(),
						emitter.getColorStart(),
						emitter.getColorEnd()
					);
					break;
				}
			case BOUNDINGBOX_PARTICLE_EMITTER:
				{
					BoundingBoxParticleEmitter emitter = particleSystem.getBoundingBoxParticleEmitters();
					engineEmitter = new net.drewke.tdme.engine.subsystems.particlesystem.BoundingBoxParticleEmitter(
						emitter.getCount(),
						emitter.getLifeTime(),
						emitter.getLifeTimeRnd(),
						emitter.getMass(),
						emitter.getMassRnd(),
						new OrientedBoundingBox(
							emitter.getObbCenter(),
							emitter.getObbAxis0(),
							emitter.getObbAxis1(),
							emitter.getObbAxis2(),
							emitter.getObbHalfextension()
						),
						emitter.getVelocity(),
						emitter.getVelocityRnd(),
						emitter.getColorStart(),
						emitter.getColorEnd()
					);
					break;
				}
			case CIRCLE_PARTICLE_EMITTER:
				{
					CircleParticleEmitter emitter = particleSystem.getCircleParticleEmitter();
					engineEmitter = new net.drewke.tdme.engine.subsystems.particlesystem.CircleParticleEmitter(
						emitter.getCount(),
						emitter.getLifeTime(),
						emitter.getLifeTimeRnd(),
						emitter.getAxis0(),
						emitter.getAxis1(),
						emitter.getCenter(),
						emitter.getRadius(),
						emitter.getMass(),
						emitter.getMassRnd(),
						emitter.getVelocity(),
						emitter.getVelocityRnd(),
						emitter.getColorStart(),
						emitter.getColorEnd()
					);
					break;
				}
			case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY: 
				{
					CircleParticleEmitterPlaneVelocity emitter = particleSystem.getCircleParticleEmitterPlaneVelocity();
					engineEmitter = new net.drewke.tdme.engine.subsystems.particlesystem.CircleParticleEmitterPlaneVelocity(
						emitter.getCount(),
						emitter.getLifeTime(),
						emitter.getLifeTimeRnd(),
						emitter.getAxis0(),
						emitter.getAxis1(),
						emitter.getCenter(),
						emitter.getRadius(),
						emitter.getMass(),
						emitter.getMassRnd(),
						emitter.getVelocity(),
						emitter.getVelocityRnd(),
						emitter.getColorStart(),
						emitter.getColorEnd()
					);
					break;
				}
			case SPHERE_PARTICLE_EMITTER:
				{
					SphereParticleEmitter emitter = particleSystem.getSphereParticleEmitter();
					engineEmitter = new net.drewke.tdme.engine.subsystems.particlesystem.SphereParticleEmitter(
						emitter.getCount(),
						emitter.getLifeTime(),
						emitter.getLifeTimeRnd(),
						emitter.getMass(),
						emitter.getMassRnd(),
						new Sphere(
							emitter.getCenter(),
							emitter.getRadius()
						),
						emitter.getVelocity(),
						emitter.getVelocityRnd(),
						emitter.getColorStart(),
						emitter.getColorEnd()
					);
					break;
				}
			default:
				System.out.println("Level::createParticleSystem(): unknown particle system emitter '" + particleSystem.getEmitter() + "'");
				return null;
		}

		//
		switch (particleSystem.getType()) {
			case NONE:
				return null;
			case OBJECT_PARTICLE_SYSTEM:
				ObjectParticleSystem objectParticleSystem = particleSystem.getObjectParticleSystem();
				return new ObjectParticleSystemEntity(
					id,
					objectParticleSystem.getModel(), 
					objectParticleSystem.getScale(),
					enableDynamicShadows,
					objectParticleSystem.getMaxCount(),
					engineEmitter
				);
			case POINT_PARTICLE_SYSTEM:
				PointParticleSystem pointParticleSystem = particleSystem.getPointParticleSystem();
				return new PointsParticleSystemEntity(
					id,
					false,
					engineEmitter,
					pointParticleSystem.getMaxPoints()
				);
			default:
				System.out.println("Level::createParticleSystem(): unknown particle system type '" + particleSystem.getType() + "'");
				return null;
		}
	}

	/**
	 * Add level to engine
	 * @param engine
	 * @param level
	 * @param add empties
	 * @param add trigger
	 * @param dynamic shadowing 
	 * @param pickable
	 * @param translation
	 */
	public static void addLevel(Engine engine, LevelEditorLevel level, boolean addEmpties, boolean addTrigger, boolean dynamicShadowing, boolean pickable, Vector3 translation) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);
			// skip on empties or trigger
			if (addEmpties == false && object.getEntity().getType() == EntityType.EMPTY) continue;
			if (addTrigger == false && object.getEntity().getType() == EntityType.TRIGGER) continue;
			// add to 3d engine
			Entity entity = new Object3D(object.getId(), object.getEntity().getModel());
			// apply transformations
			entity.fromTransformations(object.getTransformations());
			// apply translation
			if (translation != null) {
				entity.getTranslation().add(translation);
			}
			// pickable
			entity.setPickable(pickable);
			entity.setDynamicShadowingEnabled(dynamicShadowing);
			// do not scale empties
			if (object.getEntity().getType() == EntityType.EMPTY) {
				entity.getScale().set(
					MathTools.sign(entity.getScale().getX()), 
					MathTools.sign(entity.getScale().getY()),
					MathTools.sign(entity.getScale().getZ())
				);
			}
			// update
			entity.update();
			// add
			engine.addEntity(entity);
		}
	}

	/**
	 * Add level to engine
	 * @param world
	 * @param level
	 * @param rigid bodies (will be filled by logic)
	 * @param translation
	 */
	public static void addLevel(World world, LevelEditorLevel level, ArrayList<RigidBody> rigidBodies, Vector3 translation) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);

			// skip on empties or trigger
			if (object.getEntity().getType() == EntityType.EMPTY) continue;
			if (object.getEntity().getType() == EntityType.TRIGGER) continue;

			//
			for (int j = 0; j < object.getEntity().getBoundingVolumeCount(); j++) {
				LevelEditorEntityBoundingVolume entityBv = object.getEntity().getBoundingVolumeAt(j);

				// keep track of world ids
				String worldId = object.getId() + ".bv." + j;

				// TODO: apply transformations
				Transformations transformations = new Transformations();
				// apply transformations
				transformations.fromTransformations(object.getTransformations());
				// apply translation
				if (translation != null) {
					transformations.getTranslation().add(translation);
					transformations.update();
				}

				// add to physics world
				RigidBody rigidBody = world.addStaticRigidBody(
					worldId,
					true,
					RIGIDBODY_TYPEID_STATIC,
					transformations,
					entityBv.getBoundingVolume(),
					1.0f
				);
				rigidBody.setCollisionTypeIds(
					RIGIDBODY_TYPEID_STATIC |
					RIGIDBODY_TYPEID_PLAYER
				);

				// add to rigid bodies
				rigidBodies.add(rigidBody);
			}
		}
	}

	/**
	 * Disable level in engine
	 * @param engine
	 * @param level
	 */
	public static void disableLevel(Engine engine, LevelEditorLevel level) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);
			// get from 3d engine
			Entity entity = engine.getEntity(object.getId());
			// skip if entity not found
			if (entity == null) continue;
			// disable
			entity.setEnabled(false);
		}
	}

	/**
	 * Disable level in physics world
	 * @param world
	 * @param rigid bodies
	 */
	public static void disableLevel(World world, ArrayList<RigidBody> rigidBodies) {
		// load level objects
		for (int i = 0; i < rigidBodies.size(); i++) {
			// disable
			rigidBodies.get(i).setEnabled(false);
		}
	}

	/**
	 * Enable disabled level in engine
	 * @param engine
	 * @param level
	 * @param translation
	 */
	public static void enableLevel(Engine engine, LevelEditorLevel level, Vector3 translation) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);
			// get from 3d engine
			Entity entity = engine.getEntity(object.getId());
			// skip if entity not found
			if (entity == null) continue;
			// apply transformations
			entity.fromTransformations(object.getTransformations());
			// apply transformatio
			if (translation != null) {
				entity.getTranslation().add(translation);
			}
			// do not scale empties
			if (object.getEntity().getType() == EntityType.EMPTY) {
				entity.getScale().set(
					MathTools.sign(entity.getScale().getX()), 
					MathTools.sign(entity.getScale().getY()),
					MathTools.sign(entity.getScale().getZ())
				);
			}
			// update
			entity.update();
			// enable
			entity.setEnabled(true);
		}
	}

	/**
	 * Enable disabled level in engine
	 * @param world
	 * @param level
	 * @param rigid bodies
	 * @param translation
	 */
	public static void enableLevel(World world, LevelEditorLevel level, ArrayList<RigidBody> rigidBodies, Vector3 translation) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);

			// iterate object bounding volumes
			// TODO: this is nearly O(N^3), fix this
			for (int j = 0; j < object.getEntity().getBoundingVolumeCount(); j++) {
				for (int k = 0; k < rigidBodies.size(); k++) {
					// world id
					RigidBody rigidBody = rigidBodies.get(k);

					// check if world id belong to current object
					compareMutableString.set(object.getId());
					compareMutableString.append(".bv.");
					compareMutableString.append(j);
					if (compareMutableString.equals(rigidBody.getId()) == false) continue;
		
					// set new transformations
					transformations.fromTransformations(object.getTransformations());
					//	apply translations
					if (translation != null) {
						transformations.getTranslation().add(translation);
						transformations.update();
					}
					rigidBody.synch(transformations);

					// enable
					rigidBody.setEnabled(true);
				}
			}
		}
	}

}

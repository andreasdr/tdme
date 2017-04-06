package net.drewke.tdme.tools.leveleditor.logic;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;

/**
 * Level Editor Level Logic
 * @author Andreas Drewke
 * @version $Id$
 */
public class Level {

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
	 * Add level to engine
	 * @param engine
	 * @param level
	 * @param add empties
	 * @param add trigger 
	 * @param pickable
	 * @param translation
	 */
	public static void addLevel(Engine engine, LevelEditorLevel level, boolean addEmpties, boolean addTrigger, boolean pickable, Vector3 translation) {
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
	 * Disable level in engine
	 * @param engine
	 * @param level
	 */
	public static void disableLevel(Engine engine, LevelEditorLevel level) {
		// load level objects
		for (int i = 0; i < level.getObjectCount(); i++) {
			LevelEditorObject object = level.getObjectAt(i);
			// add to 3d engine
			Entity entity = engine.getEntity(object.getId());
			// skip if entity not found
			if (entity == null) continue;
			// disable
			entity.setEnabled(false);
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
			// add to 3d engine
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

}

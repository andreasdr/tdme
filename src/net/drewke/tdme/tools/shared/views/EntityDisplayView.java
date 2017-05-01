package net.drewke.tdme.tools.shared.views;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.tools.shared.controller.EntityDisplaySubScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;

/**
 * Entity display view
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityDisplayView {

	private final static String[] MODEL_BOUNDINGVOLUME_IDS = {
		"model_bv.0",
		"model_bv.1",
		"model_bv.2",
		"model_bv.3",
		"model_bv.4",
		"model_bv.5",
		"model_bv.6",
		"model_bv.7",
	};

	private Engine engine;

	private EntityDisplaySubScreenController entityDisplaySubScreenController;

	private boolean displayGroundPlate = false;
	private boolean displayShadowing = false;
	private boolean displayBoundingVolume = false;

	/**
	 * Public constructor
	 * @param entity display sub screen controller
	 */
	public EntityDisplayView(EntityDisplaySubScreenController entityDisplaySubScreenController) {
		this.engine = Engine.getInstance();
		this.entityDisplaySubScreenController = entityDisplaySubScreenController;
	}

	/**
	 * @return display ground plate
	 */
	public boolean isDisplayGroundPlate() {
		return displayGroundPlate;
	}

	/**
	 * Set up ground plate visibility
	 * @param ground plate visible
	 */
	public void setDisplayGroundPlate(boolean groundPlate) {
		this.displayGroundPlate = groundPlate;
	}

	/**
	 * @return display shadowing
	 */
	public boolean isDisplayShadowing() {
		return displayShadowing;
	}

	/**
	 * Set up shadow rendering
	 * @param shadow rendering
	 */
	public void setDisplayShadowing(boolean shadowing) {
		this.displayShadowing = shadowing;
	}

	/**
	 * @return display bounding volume
	 */
	public boolean isDisplayBoundingVolume() {
		return displayBoundingVolume;
	}

	/**
	 * Set up bounding volume visibility
	 * @param bounding volume
	 */
	public void setDisplayBoundingVolume(boolean displayBoundingVolume) {
		this.displayBoundingVolume = displayBoundingVolume;
	}

	/**
	 * Display
	 * @param entity
	 */
	public void display(LevelEditorEntity entity) {
		// apply settings from gui
		if (entity != null) {
			Entity model = engine.getEntity("model");
			Entity ground = engine.getEntity("ground");
			model.setDynamicShadowingEnabled(displayShadowing);
			ground.setEnabled(displayGroundPlate);
			for (int i = 0; i < MODEL_BOUNDINGVOLUME_IDS.length; i++) {
				Entity modelBoundingVolume = engine.getEntity(MODEL_BOUNDINGVOLUME_IDS[i]);
				if (modelBoundingVolume != null) {
					modelBoundingVolume.setEnabled(displayBoundingVolume);
				}
			}
		}
	}

}

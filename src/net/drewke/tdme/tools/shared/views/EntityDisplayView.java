package net.drewke.tdme.tools.shared.views;

import net.drewke.tdme.tools.shared.controller.EntityDisplaySubScreenController;

/**
 * Entity display view
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityDisplayView {

	private EntityDisplaySubScreenController entityDisplaySubScreenController;

	private boolean displayGroundPlate = false;
	private boolean displayShadowing = false;
	private boolean displayBoundingVolume = false;

	/**
	 * Public constructor
	 * @param entity display sub screen controller
	 */
	public EntityDisplayView(EntityDisplaySubScreenController entityDisplaySubScreenController) {
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

}

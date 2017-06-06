package net.drewke.tdme.tools.shared.model;

import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;

/**
 * Level Editor Model
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorEntity extends Properties {

	public enum EntityType {TRIGGER, MODEL, EMPTY, PARTICLESYSTEM};

	public final static int ID_NONE = -1; 

	protected int id;
	protected EntityType type;
	protected String name;
	protected String description;
	protected String entityFileName;
	protected String fileName;
	protected String thumbnail;
	protected Model model;
	protected Vector3 pivot;
	protected LevelEditorEntityParticleSystem particleSystem;
	protected ArrayList<LevelEditorEntityBoundingVolume> boundingVolumes;

	/**
	 * Creates a level editor model
	 * @param id
	 * @param entity type
	 * @param name
	 * @param description
	 * @param entity file name
	 * @param file name
	 * @param thumbnail
	 * @param model
	 * @param pivot
	 */
	public LevelEditorEntity(int id, EntityType entityType, String name, String description, String entityFileName, String fileName, String thumbnail, Model model, Vector3 pivot) {
		this.id = id;
		this.type = entityType;
		this.name = name;
		this.description = description;
		this.entityFileName = entityFileName;
		this.fileName = fileName;
		this.thumbnail = thumbnail;
		this.model = model;
		this.pivot = pivot;
		if (this.type == EntityType.PARTICLESYSTEM) {
			this.particleSystem = new LevelEditorEntityParticleSystem();
		}
		this.boundingVolumes = new ArrayList<LevelEditorEntityBoundingVolume>();
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return entity type
	 */
	public EntityType getType() {
		return type;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set up model name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set up model description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return entity file name
	 */
	public String getEntityFileName() {
		return entityFileName;
	}

	/**
	 * Set entity file name
	 * @param entity file name
	 */
	public void setEntityFileName(String entityFileName) {
		this.entityFileName = entityFileName;
	}

	/**
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	/**
	 * @return model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return bounding volumes
	 */
	protected ArrayList<LevelEditorEntityBoundingVolume> getBoundingVolumes() {
		return boundingVolumes;
	}

	/**
	 * @return pivot
	 */
	public Vector3 getPivot() {
		return pivot;
	}

	/**
	 * @return bounding volume count
	 */
	public int getBoundingVolumeCount() {
		return boundingVolumes.size();
	}

	/**
	 * Get bounding volume at
	 * @param idx
	 * @return level editor object bounding volume
	 */
	public LevelEditorEntityBoundingVolume getBoundingVolumeAt(int idx) {
		return boundingVolumes.get(idx);
	}

	/**
	 * Add bounding volume
	 * @param idx
	 * @param level editor entity bounding volume
	 * @return level editor bounding volume
	 */
	public boolean addBoundingVolume(int idx, LevelEditorEntityBoundingVolume levelEditorEntityBoundingVolume) {
		if (idx < 0) return false;
		if (idx > boundingVolumes.size()) return false;
		if (idx == boundingVolumes.size()) {
			boundingVolumes.add(levelEditorEntityBoundingVolume);
		}
		return false;
	}

	/**
	 * Set default (up to 8) bounding volumes, to be used with LevelEditor
	 */
	public void setDefaultBoundingVolumes() {
		// add up to 8 bvs
		for (int i = boundingVolumes.size(); i < 8; i++) {
			LevelEditorEntityBoundingVolume bv = new LevelEditorEntityBoundingVolume(i, this);
			addBoundingVolume(i, bv);
		}
	}

	/**
	 * @return level editor entity particle system
	 */
	public LevelEditorEntityParticleSystem getParticleSystem() {
		return particleSystem;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.model.Properties#toString()
	 */
	public String toString() {
		return "LevelEditorEntity [id=" + id + ", type=" + type + ", name="
				+ name + ", description=" + description + ", entityFileName="
				+ entityFileName + ", fileName=" + fileName + ", thumbnail="
				+ thumbnail + ", model=" + model + ", boundingVolumes="
				+ boundingVolumes + ", pivot=" + pivot + "]";
	}
	
}

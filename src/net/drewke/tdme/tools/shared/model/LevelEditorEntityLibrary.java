package net.drewke.tdme.tools.shared.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;

/**
 * Model Editor Entity Library
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorEntityLibrary {

	public final static int ID_ALLOCATE = -1;

	private LevelEditorLevel level;
	private HashMap<Integer, LevelEditorEntity> entitiesById;
	private ArrayList<LevelEditorEntity> entities;
	private int entityIdx;

	/**
	 * Public constructor
	 */
	public LevelEditorEntityLibrary(LevelEditorLevel level) {
		this.level = level;
		this.entitiesById = new HashMap<Integer, LevelEditorEntity>();
		this.entities = new ArrayList<LevelEditorEntity>();
		this.entityIdx = 0;
	}

	/**
	 * Clears this model library
	 */
	public void clear() {
		// remove models from model cache too
		this.entitiesById.clear();
		this.entities.clear();
		this.entityIdx = 0;		
	}

	/**
	 * Allocata a unique entity idx
	 * @return
	 */
	private int allocateEntityId() {
		return entityIdx++;
	}

	/**
	 * Adds a model
	 * @param id
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @param pivot
	 * @return level editor entity
	 * @throws Exception
	 */
	public LevelEditorEntity addModel(int id, String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		File modelFile = new File(pathName + File.separator + fileName);
		LevelEditorEntity levelEditorEntity = null;

		// parse models
		if (modelFile.getName().toLowerCase().endsWith(".dae")) {
			Model model = DAEReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			levelEditorEntity = new LevelEditorEntity(
				id == ID_ALLOCATE?allocateEntityId():id,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				null,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				new Vector3(0f,0f,0f)
			);
		} else
		if (modelFile.getName().toLowerCase().endsWith(".tm")) {
			Model model = TMReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
			levelEditorEntity = new LevelEditorEntity(
				id == ID_ALLOCATE?allocateEntityId():id,
				LevelEditorEntity.EntityType.MODEL,
				name,
				description,
				null,
				pathName + File.separator + fileName,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				new Vector3(0f,0f,0f)
			);
		} else
		if (modelFile.getName().toLowerCase().endsWith(".tmm")) {
			levelEditorEntity = ModelMetaDataFileImport.doImport(
				id == ID_ALLOCATE?allocateEntityId():id,
				pathName, 
				fileName
			);
		} else {
			throw new Exception(pathName + "/" + fileName + ": Unknown model file format");
		}

		// add model
		addEntity(levelEditorEntity);

		//
		return levelEditorEntity;
	}

	/**
	 * Add a trigger
	 * @param name
	 * @param description
	 * @param width
	 * @param height
	 * @param depth
	 * @return level editor entity
	 * @throws Exception
	 */
	public LevelEditorEntity addTrigger(int id, String name, String description, float width, float height, float depth) throws Exception {
		String cacheId = "leveleditor.trigger." + width + "mx" + height + "mx" + depth + "m";
		LevelEditorEntity levelEditorEntity = null;
		BoundingBox boundingBox = new BoundingBox(
			new Vector3(-width / 2f, 0f, -depth / 2f),
			new Vector3(+width / 2f, height, +depth / 2f)
		);
		Model model = PrimitiveModel.createModel(boundingBox, cacheId + "_bv");
		levelEditorEntity = new LevelEditorEntity(
			id == ID_ALLOCATE?allocateEntityId():id,
			EntityType.TRIGGER,
			name,
			description,
			null,
			cacheId,
			model.getId().
				replace("\\", "_").
				replace("/", "_").
				replace(":", "_") +
				".png",
			model,
			new Vector3()
		);
		levelEditorEntity.addBoundingVolume(0, new LevelEditorEntityBoundingVolume(0, levelEditorEntity));
		levelEditorEntity.getBoundingVolumeAt(0).setupAabb(boundingBox.getMin(), boundingBox.getMax());
		id = levelEditorEntity.getId();

		// add trigger
		addEntity(levelEditorEntity);

		//
		return levelEditorEntity;
	}

	/**
	 * Add a empty
	 * @param name
	 * @param description
	 * @return level editor entity
	 * @throws Exception
	 */
	public LevelEditorEntity addEmpty(int id, String name, String description) throws Exception {
		String cacheId = "leveleditor.empty";
		LevelEditorEntity levelEditorEntity = null;

		// create entity
		Model model = DAEReader.read("resources/tools/leveleditor/models", "arrow.dae");
		levelEditorEntity = new LevelEditorEntity(
			id == ID_ALLOCATE?allocateEntityId():id,
			EntityType.EMPTY,
			name,
			description,
			null,
			cacheId,
			model.getId().
				replace("\\", "_").
				replace("/", "_").
				replace(":", "_") +
				".png",
			model,
			new Vector3()
		);

		// add empty
		addEntity(levelEditorEntity);

		//
		return levelEditorEntity;
	}

	/**
	 * Add a empty
	 * @param name
	 * @param description
	 * @return level editor entity
	 * @throws Exception
	 */
	public LevelEditorEntity addParticleSystem(int id, String name, String description) throws Exception {
		// create entity
		LevelEditorEntity levelEditorEntity = new LevelEditorEntity(
			id == ID_ALLOCATE?allocateEntityId():id,
			EntityType.PARTICLESYSTEM,
			name,
			description,
			null,
			null,
			null,
			null,
			new Vector3()
		);

		// add particle system
		addEntity(levelEditorEntity);

		//
		return levelEditorEntity;
	}

	/**
	 * Add a entity
	 * @param model
	 */
	public void addEntity(LevelEditorEntity levelEditorEntity) throws Exception {
		// add model
		if (entitiesById.get(new Integer(levelEditorEntity.getId())) != null) {
			throw new Exception("Entity id already in use");
		}

		// add
		entities.add(levelEditorEntity);
		entitiesById.put(levelEditorEntity.getId(), levelEditorEntity);

		// global entity idx
		if (levelEditorEntity.getId() >= entityIdx) entityIdx = levelEditorEntity.getId() + 1;
	}

	/**
	 * @param idx
	 * @return entity 
	 */
	public LevelEditorEntity getEntityAt(int idx) {
		return entities.get(idx);
	}

	/**
	 * Retrieve a entity
	 * @param id
	 * @return level editor entity
	 */
	public LevelEditorEntity getEntity(int id) {
		return entitiesById.get(new Integer(id));
	}

	/**
	 * Remove a entity
	 * @param id
	 */
	public void removeEntity(int id) {
		LevelEditorEntity _model = entitiesById.remove(new Integer(id));
		if (_model != null) {
			entities.remove(_model);
		}
	}

	/**
	 * @return entity count 
	 */
	public int getEntityCount() {
		return entities.size();
	}

}

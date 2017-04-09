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
		if (entitiesById.get(new Integer(id)) != null) {
			throw new Exception(pathName + "/" + fileName + ": Model id already in use");
		}
		addModel(levelEditorEntity);
		if (levelEditorEntity.getId() >= entityIdx) entityIdx = levelEditorEntity.getId() + 1;

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
		Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
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
		id = levelEditorEntity.getId();

		// add model
		if (entitiesById.get(new Integer(id)) != null) {
			throw new Exception("Model id already in use");
		}
		addModel(levelEditorEntity);
		if (levelEditorEntity.getId() >= entityIdx) entityIdx = levelEditorEntity.getId() + 1;

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
		Model modelBoundingVolume = PrimitiveModel.createModel(model.getBoundingBox(), model.getId() + "_bv");;
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
		id = levelEditorEntity.getId();

		// add model
		if (entitiesById.get(new Integer(id)) != null) {
			throw new Exception("Model id already in use");
		}
		addModel(levelEditorEntity);
		if (levelEditorEntity.getId() >= entityIdx) entityIdx = levelEditorEntity.getId() + 1;

		//
		return levelEditorEntity;
	}

	/**
	 * Add a model
	 * @param model
	 */
	protected void addModel(LevelEditorEntity model) {
		LevelEditorEntity _model = entitiesById.put(model.getId(), model);
		if (_model != null) entities.remove(_model);
		entities.add(model);
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

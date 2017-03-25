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
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.ModelType;

/**
 * Model Editor Entity Library
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorEntityLibrary {

	/**
	 * Model Cache Entity
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public class ModelCacheEntity {
		protected Model model;
		protected Model modelBoundingVolume;
		String boundingModelMeshFile;
		protected BoundingVolume boundingVolume;
		protected int referenceCounter;
	}

	public final static int ID_ALLOCATE = -1;

	HashMap<String, ModelCacheEntity> entityCache;
	HashMap<Integer, LevelEditorEntity> entitiesById;
	ArrayList<LevelEditorEntity> entities;
	int entityIdx;

	/**
	 * Public constructor
	 */
	public LevelEditorEntityLibrary() {
		this.entityCache = new HashMap<String, LevelEditorEntityLibrary.ModelCacheEntity>();
		this.entitiesById = new HashMap<Integer, LevelEditorEntity>();
		this.entities = new ArrayList<LevelEditorEntity>();
		this.entityIdx = 0;
	}

	/**
	 * Clears this model library
	 */
	public void clear() {
		this.entityCache.clear();
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
		String cacheId = modelFile.getCanonicalPath();
		ModelCacheEntity cacheEntity = entityCache.get(cacheId);
		LevelEditorEntity levelEditorEntity = null;

		// check if we already have loaded this model
		if (cacheEntity != null) {
			levelEditorEntity = new LevelEditorEntity(
				id == ID_ALLOCATE?allocateEntityId():id,
				LevelEditorEntity.ModelType.MODEL,
				name,
				description,
				pathName + File.separator + fileName,
				cacheEntity.model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				cacheEntity.model,
				cacheEntity.boundingModelMeshFile,
				PrimitiveModel.createModel(cacheEntity.boundingVolume, cacheEntity.model.getId() + "_bv"),
				cacheEntity.boundingVolume,
				pivot
			);
			id = levelEditorEntity.getId();
			cacheEntity.referenceCounter++;
		} else {
			if (modelFile.getName().toLowerCase().endsWith(".dae")) {
				Model model = DAEReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
				BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
				Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
				levelEditorEntity = new LevelEditorEntity(
					id == ID_ALLOCATE?allocateEntityId():id,
					LevelEditorEntity.ModelType.MODEL,
					name,
					description,
					pathName + File.separator + fileName,
					model.getId().
						replace("\\", "_").
						replace("/", "_").
						replace(":", "_") +
						".png",
					model,
					null,
					modelBoundingVolume,
					boundingBox,
					pivot
				);
			} else
			if (modelFile.getName().toLowerCase().endsWith(".tm")) {
				Model model = TMReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
				BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
				Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
				levelEditorEntity = new LevelEditorEntity(
					id == ID_ALLOCATE?allocateEntityId():id,
					LevelEditorEntity.ModelType.MODEL,
					name,
					description,
					pathName + File.separator + fileName,
					model.getId().
						replace("\\", "_").
						replace("/", "_").
						replace(":", "_") +
						".png",
					model,
					null,
					modelBoundingVolume,
					boundingBox,
					pivot
				);
			} else
			if (modelFile.getName().toLowerCase().endsWith(".tmm")) {
				levelEditorEntity = ModelMetaDataFileImport.doImport(
					id == ID_ALLOCATE?allocateEntityId():id,
					pathName, 
					fileName
				);
			} else {
				throw new Exception("Unknown model file format");
			}

			id = levelEditorEntity.getId();
			// add to cache
			cacheEntity = new ModelCacheEntity();
			cacheEntity.model = levelEditorEntity.getModel();
			cacheEntity.modelBoundingVolume = levelEditorEntity.getModelBoundingVolume();
			cacheEntity.boundingVolume = levelEditorEntity.getBoundingVolume();
			cacheEntity.boundingModelMeshFile = levelEditorEntity.getBoundingModelMeshFile();
			cacheEntity.referenceCounter = 1;
			entityCache.put(cacheId, cacheEntity);
		}

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
	 * Add a trigger
	 * @param name
	 * @param description
	 * @param width
	 * @param height
	 * @param depth
	 * @param pivot
	 * @return level editor entity
	 * @throws Exception
	 */
	public LevelEditorEntity addTrigger(int id, String name, String description, float width, float height, float depth, Vector3 pivot) throws Exception {
		String cacheId = "leveleditor.trigger." + width + "mx" + height + "mx" + depth + "m";
		ModelCacheEntity cacheEntity = entityCache.get(cacheId);
		LevelEditorEntity levelEditorEntity = null;

		// check if we already have loaded this model
		if (cacheEntity != null) {
			levelEditorEntity = new LevelEditorEntity(
				id == ID_ALLOCATE?allocateEntityId():id,
				ModelType.TRIGGER,
				name,
				description,
				cacheId,
				cacheEntity.model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				cacheEntity.model,
				null,
				PrimitiveModel.createModel(cacheEntity.boundingVolume, cacheEntity.model.getId() + "_bv"),
				cacheEntity.boundingVolume,
				pivot
			);
			id = levelEditorEntity.getId();
			cacheEntity.referenceCounter++;
		} else {
			BoundingBox boundingBox = new BoundingBox(
				new Vector3(-width / 2f, 0f, -depth / 2f),
				new Vector3(+width / 2f, height, +depth / 2f)
			);
			Model model = PrimitiveModel.createModel(boundingBox, cacheId + "_bv");
			Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
			levelEditorEntity = new LevelEditorEntity(
				id == ID_ALLOCATE?allocateEntityId():id,
				ModelType.TRIGGER,
				name,
				description,
				cacheId,
				model.getId().
					replace("\\", "_").
					replace("/", "_").
					replace(":", "_") +
					".png",
				model,
				null,
				modelBoundingVolume,
				boundingBox,
				pivot
			);
			id = levelEditorEntity.getId();
			// add to cache
			cacheEntity = new ModelCacheEntity();
			cacheEntity.model = model;
			cacheEntity.modelBoundingVolume = modelBoundingVolume;
			cacheEntity.boundingVolume = boundingBox;
			cacheEntity.referenceCounter = 1;
			entityCache.put(cacheId, cacheEntity);
		}

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
			// get associated cache entity
			String cacheId = _model.getFileName();
			ModelCacheEntity cacheEntity = entityCache.get(cacheId);
			// we should have one
			if (cacheEntity != null) {
				cacheEntity.referenceCounter--;
				// removed last model with this cache entity
				if (cacheEntity.referenceCounter == 0) {
					entityCache.remove(cacheId);
				}
			} else {
				System.out.println("Warning: No cache entity: " + cacheId);
			}
		}
	}

	/**
	 * @return entity count 
	 */
	public int getEntityCount() {
		return entities.size();
	}

}

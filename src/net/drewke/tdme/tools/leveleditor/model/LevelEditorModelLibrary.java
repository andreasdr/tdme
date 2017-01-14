package net.drewke.tdme.tools.leveleditor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel.ModelType;

/**
 * Model Editor Model Library
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorModelLibrary {

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

	HashMap<String, ModelCacheEntity> modelsCache;
	HashMap<Integer, LevelEditorModel> modelsById;
	ArrayList<LevelEditorModel> models;
	int modelIdx;

	/**
	 * Public constructor
	 */
	public LevelEditorModelLibrary() {
		this.modelsCache = new HashMap<String, LevelEditorModelLibrary.ModelCacheEntity>();
		this.modelsById = new HashMap<Integer, LevelEditorModel>();
		this.models = new ArrayList<LevelEditorModel>();
		this.modelIdx = 0;
	}

	/**
	 * Clears this model library
	 */
	public void clear() {
		this.modelsCache.clear();
		this.modelsById.clear();
		this.models.clear();
		this.modelIdx = 0;		
	}

	/**
	 * Allocata a unique model idx
	 * @return
	 */
	private int allocateModelId() {
		return modelIdx++;
	}

	/**
	 * Adds a model
	 * @param id
	 * @param name
	 * @param description
	 * @param path name
	 * @param file name
	 * @param pivot
	 * @return level editor model
	 * @throws Exception
	 */
	public LevelEditorModel addModel(int id, String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		File modelFile = new File(pathName + File.separator + fileName);
		String cacheId = modelFile.getCanonicalPath();
		ModelCacheEntity cacheEntity = modelsCache.get(cacheId);
		LevelEditorModel levelEditorModel = null;

		// check if we already have loaded this model
		if (cacheEntity != null) {
			levelEditorModel = new LevelEditorModel(
				id == ID_ALLOCATE?allocateModelId():id,
				LevelEditorModel.ModelType.MODEL,
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
			id = levelEditorModel.getId();
			cacheEntity.referenceCounter++;
		} else {
			if (modelFile.getName().toLowerCase().endsWith(".dae")) {
				Model model = DAEReader.read(modelFile.getParentFile().getCanonicalPath(), modelFile.getName());
				BoundingBox boundingBox = ModelUtilities.createBoundingBox(model);
				Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
				levelEditorModel = new LevelEditorModel(
					id == ID_ALLOCATE?allocateModelId():id,
					LevelEditorModel.ModelType.MODEL,
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
				levelEditorModel = ModelMetaDataFileImport.doImport(
					id == ID_ALLOCATE?allocateModelId():id,
					pathName, 
					fileName
				);
			} else {
				throw new Exception("Unknown model file format");
			}

			id = levelEditorModel.getId();
			// add to cache
			cacheEntity = new ModelCacheEntity();
			cacheEntity.model = levelEditorModel.getModel();
			cacheEntity.modelBoundingVolume = levelEditorModel.getModelBoundingVolume();
			cacheEntity.boundingVolume = levelEditorModel.getBoundingVolume();
			cacheEntity.boundingModelMeshFile = levelEditorModel.getBoundingModelMeshFile();
			cacheEntity.referenceCounter = 1;
			modelsCache.put(cacheId, cacheEntity);
		}

		// add model
		if (modelsById.get(new Integer(id)) != null) {
			throw new Exception("Model id already in use");
		}
		addModel(levelEditorModel);
		if (levelEditorModel.getId() >= modelIdx) modelIdx = levelEditorModel.getId() + 1;

		//
		return levelEditorModel;
	}

	/**
	 * Creates a trigger
	 * @param name
	 * @param description
	 * @param width
	 * @param height
	 * @param depth
	 * @param pivot
	 * @return level editor model
	 * @throws Exception
	 */
	public LevelEditorModel createTrigger(int id, String name, String description, float width, float height, float depth, Vector3 pivot) throws Exception {
		String cacheId = "leveleditor.trigger." + width + "mx" + height + "mx" + depth + "m";
		ModelCacheEntity cacheEntity = modelsCache.get(cacheId);
		LevelEditorModel levelEditorModel = null;

		// check if we already have loaded this model
		if (cacheEntity != null) {
			levelEditorModel = new LevelEditorModel(
				id == ID_ALLOCATE?allocateModelId():id,
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
			id = levelEditorModel.getId();
			cacheEntity.referenceCounter++;
		} else {
			BoundingBox boundingBox = new BoundingBox(
				new Vector3(-width / 2f, 0f, -depth / 2f),
				new Vector3(+width / 2f, height, +depth / 2f)
			);
			Model model = PrimitiveModel.createModel(boundingBox, cacheId + "_bv");
			Model modelBoundingVolume = PrimitiveModel.createModel(boundingBox, model.getId() + "_bv");
			levelEditorModel = new LevelEditorModel(
				id == ID_ALLOCATE?allocateModelId():id,
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
			id = levelEditorModel.getId();
			// add to cache
			cacheEntity = new ModelCacheEntity();
			cacheEntity.model = model;
			cacheEntity.modelBoundingVolume = modelBoundingVolume;
			cacheEntity.boundingVolume = boundingBox;
			cacheEntity.referenceCounter = 1;
			modelsCache.put(cacheId, cacheEntity);
		}

		// add model
		if (modelsById.get(new Integer(id)) != null) {
			throw new Exception("Model id already in use");
		}
		addModel(levelEditorModel);
		if (levelEditorModel.getId() >= modelIdx) modelIdx = levelEditorModel.getId() + 1;

		//
		return levelEditorModel;
	}

	/**
	 * Add a model
	 * @param model
	 */
	protected void addModel(LevelEditorModel model) {
		LevelEditorModel _model = modelsById.put(model.getId(), model);
		if (_model != null) models.remove(_model);
		models.add(model);
	}

	/**
	 * @param idx
	 * @return model 
	 */
	public LevelEditorModel getModelAt(int idx) {
		return models.get(idx);
	}

	/**
	 * Retrieve a model
	 * @param id
	 * @return level editor model
	 */
	public LevelEditorModel getModel(int id) {
		return modelsById.get(new Integer(id));
	}

	/**
	 * Remove a model
	 * @param id
	 */
	public void removeModel(int id) {
		LevelEditorModel _model = modelsById.remove(new Integer(id));
		if (_model != null) {
			models.remove(_model);
			// get associated cache entity
			String cacheId = _model.getFileName();
			ModelCacheEntity cacheEntity = modelsCache.get(cacheId);
			// we should have one
			if (cacheEntity != null) {
				cacheEntity.referenceCounter--;
				// removed last model with this cache entity
				if (cacheEntity.referenceCounter == 0) {
					modelsCache.remove(cacheId);
				}
			} else {
				System.out.println("Warning: No cache entity: " + cacheId);
			}
		}
	}

	/**
	 * @return model count 
	 */
	public int getModelCount() {
		return models.size();
	}

}

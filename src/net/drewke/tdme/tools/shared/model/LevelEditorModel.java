package net.drewke.tdme.tools.shared.model;

import java.io.File;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.math.Vector3;

/**
 * Level Editor Model
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorModel extends Properties {

	public enum ModelType {TRIGGER, MODEL};
	public final static int ID_NONE = -1;

	private int id;
	private ModelType type;
	private String name;
	private String description;
	private String fileName;
	private String thumbnail;
	private Model model;
	private String boundingModelMeshFile;
	private Model modelBoundingVolume;
	private BoundingVolume boundingVolume;
	private BoundingBox boundingBox;
	private Vector3 pivot;

	/**
	 * Creates a level editor model
	 * @param id
	 * @param model type
	 * @param file name
	 * @param thumbnail
	 * @param model
	 * @param bounding model mesh file
	 * @param model bounding volume
	 * @param bounding box
	 * @param pivot 
	 */
	public LevelEditorModel(int id, ModelType modelType, String name, String description, String fileName, String thumbnail, Model model, String boundingModelMeshFile, Model modelBoundingVolume, BoundingVolume boundingVolume, Vector3 pivot) {
		this.id = id;
		this.type = modelType;
		this.name = name;
		this.description = description;
		this.fileName = fileName;
		this.thumbnail = thumbnail;
		this.model = model;
		this.boundingModelMeshFile = boundingModelMeshFile;
		this.modelBoundingVolume = modelBoundingVolume;
		this.boundingVolume = boundingVolume;
		this.boundingBox = Engine.getModelBoundingBox(model);
		this.pivot = pivot;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return model type
	 */
	public ModelType getType() {
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
	 * @return bounding model mesh file
	 */
	public String getBoundingModelMeshFile() {
		return boundingModelMeshFile;
	}

	/**
	 * Set up bounding volume mesh file
	 * @param boundingModelMeshFile
	 */
	public void setBoundingModelMeshFile(String boundingModelMeshFile) {
		this.boundingModelMeshFile = boundingModelMeshFile;
	}

	/**
	 * @return bounding volume model
	 */
	public Model getModelBoundingVolume() {
		return modelBoundingVolume;
	}

	/**
	 * @return bounding box
	 */
	public BoundingVolume getBoundingVolume() {
		return boundingVolume;
	}

	/**
	 * @return pivot
	 */
	public Vector3 getPivot() {
		return pivot;
	}

	/**
	 * Setup bounding volume none
	 */
	public void setupBoundingVolumeNone() {
		boundingVolume = null;
		modelBoundingVolume = null;
	}

	/**
	 * Setup bounding volume sphere
	 * @param center
	 * @param radius
	 */
	public void setupBoundingVolumeSphere(Vector3 center, float radius) {
		boundingVolume = new Sphere(center, radius);
		modelBoundingVolume = PrimitiveModel.createModel(boundingVolume, model.getId() + "_model_bv_" + System.currentTimeMillis());
	}

	/**
	 * Setup bounding volume capsule
	 * @param a
	 * @param b
	 * @param radius
	 */
	public void setupBoundingVolumeCapsule(Vector3 a, Vector3 b, float radius) {
		boundingVolume = new Capsule(a, b, radius);
		modelBoundingVolume = PrimitiveModel.createModel(boundingVolume, model.getId() + "_model_bv" + System.currentTimeMillis());
	}

	/**
	 * Setup bounding volume oriented bounding box
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 */
	public void setupBoundingVolumeObb(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		boundingVolume = new OrientedBoundingBox(center, axis0, axis1, axis2, halfExtension);
		modelBoundingVolume = PrimitiveModel.createModel(boundingVolume, model.getId() + "_model_bv" + System.currentTimeMillis());
	}

	/**
	 * Setup bounding volume bounding box
	 * @param min
	 * @param max
	 */
	public void setupBoundingVolumeAabb(Vector3 min, Vector3 max) {
		boundingVolume = new BoundingBox(min, max);
		modelBoundingVolume = PrimitiveModel.createModel(boundingVolume, model.getId() + "_model_bv" + System.currentTimeMillis());
	}

	/**
	 * Setup bounding volume sphere
	 * @param file
	 */
	public void setupBoundingVolumeConvexMesh(String file) {
		boundingModelMeshFile = file;
		try {
			Model convexMeshModel = DAEReader.read(new File(fileName).getAbsoluteFile().getCanonicalFile().getParentFile().getPath(), file);

			// take original as bounding volume
			boundingVolume = new ConvexMesh(new Object3DModel(convexMeshModel));

			// prepare convex mesh model to be displayed
			convexMeshModel.setId(convexMeshModel.getId() + "_model_bv" + System.currentTimeMillis());
			convexMeshModel.getImportTransformationsMatrix().scale(1.01f);
			PrimitiveModel.setupConvexMeshModel(convexMeshModel);
			modelBoundingVolume = convexMeshModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public String toString() {
		return "LevelEditorModel [id=" + id + ", type=" + type + ", name="
				+ name + ", description=" + description + ", fileName="
				+ fileName + ", thumbnail=" + thumbnail // + ", model=" + model
				// + ", boundingModelMeshFile=" + boundingModelMeshFile
				// + ", modelBoundingVolume=" + modelBoundingVolume
				+ ", boundingVolume=" + boundingVolume + ", boundingBox="
				+ boundingBox + ", pivot=" + pivot + "]";
	}

	
	
}

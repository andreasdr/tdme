package net.drewke.tdme.tools.shared.model;

import java.io.File;
import java.util.ArrayList;

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
 * Level Editor Entity Bouning Volume
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorEntityBoundingVolume {

	private int id; 
	private LevelEditorEntity levelEditorEntity;
	private String modelMeshFile;
	private Model model;
	private BoundingVolume boundingVolume;

	/**
	 * Public constructor
	 * @param id
	 * @param level editor entity
	 */
	public LevelEditorEntityBoundingVolume(int id, LevelEditorEntity levelEditorEntity) {
		this.id = id;
		this.levelEditorEntity = levelEditorEntity;
		modelMeshFile = null;
		model = null;
		boundingVolume = null;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return level editor entity
	 */
	public LevelEditorEntity getLevelEditorEntity() {
		return levelEditorEntity;
	}

	/**
	 * @return model mesh file
	 */
	public String getModelMeshFile() {
		return modelMeshFile;
	}

	/**
	 * @return model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return bounding volume
	 */
	public BoundingVolume getBoundingVolume() {
		return boundingVolume;
	}

	/**
	 * Setup bounding volume none
	 * @param idx
	 */
	public void setupNone() {
		boundingVolume = null;
		model = null;
		modelMeshFile = null;
	}

	/**
	 * Setup bounding volume sphere
	 * @param center
	 * @param radius
	 */
	public void setupSphere(Vector3 center, float radius) {
		boundingVolume = new Sphere(center, radius);
		model = PrimitiveModel.createModel(boundingVolume, levelEditorEntity.getModel().getId() + "_model_bv_" + System.currentTimeMillis());
		modelMeshFile = null;
	}

	/**
	 * Setup bounding volume capsule
	 * @param a
	 * @param b
	 * @param radius
	 */
	public void setupCapsule(Vector3 a, Vector3 b, float radius) {
		boundingVolume = new Capsule(a, b, radius);
		model = PrimitiveModel.createModel(boundingVolume, levelEditorEntity.getModel().getId() + "_model_bv" + System.currentTimeMillis());
		modelMeshFile = null;
	}

	/**
	 * Setup bounding volume oriented bounding box
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 */
	public void setupObb(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		boundingVolume = new OrientedBoundingBox(center, axis0, axis1, axis2, halfExtension);
		model = PrimitiveModel.createModel(boundingVolume, levelEditorEntity.getModel().getId() + "_model_bv" + System.currentTimeMillis());
		modelMeshFile = null;
	}

	/**
	 * Setup bounding volume bounding box
	 * @param min
	 * @param max
	 */
	public void setupAabb(Vector3 min, Vector3 max) {
		boundingVolume = new BoundingBox(min, max);
		model = PrimitiveModel.createModel(boundingVolume, levelEditorEntity.getModel().getId() + "_model_bv" + System.currentTimeMillis());
		modelMeshFile = null;
	}

	/**
	 * Setup bounding volume sphere
	 * @param file
	 */
	public void setupConvexMesh(String file) {
		modelMeshFile = file;
		try {
			Model convexMeshModel = DAEReader.read(new File(levelEditorEntity.getFileName()).getAbsoluteFile().getCanonicalFile().getParentFile().getPath(), file);

			// take original as bounding volume
			boundingVolume = new ConvexMesh(new Object3DModel(convexMeshModel));

			// prepare convex mesh model to be displayed
			convexMeshModel.setId(convexMeshModel.getId() + "_model_bv" + System.currentTimeMillis());
			convexMeshModel.getImportTransformationsMatrix().scale(1.01f);
			PrimitiveModel.setupConvexMeshModel(convexMeshModel);
			model = convexMeshModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "LevelEditorEntityBoundingVolume [id=" + id + ", modelMeshFile="
				+ modelMeshFile + ", model=" + (model != null?model.getId():null) + ", boundingVolume="
				+ boundingVolume + "]";
	}

}

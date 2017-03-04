package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModel.ModelType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * TDME Model meta data file import
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ModelMetaDataFileImport {

	/**
	 * Imports a level from a TDME level file to Level Editor
	 * @param id or LevelEditorModel.ID_NONE
	 * @param path name
	 * @param file name
	 */
	public static LevelEditorModel doImport(int id, String pathName, String fileName) throws Exception {
		pathName = pathName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		fileName = fileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		JSONObject jRoot = null;
		InputStream is = null;
		try {
			jRoot = new JSONObject(new JSONTokener(Tools.readStringFromFile(FileSystem.getInstance().getInputStream(pathName, fileName))));
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (is != null) try { is.close(); } catch (IOException ioei) {}
		}

		LevelEditorModel levelEditorModel;

		// bounding volume
		String boundingVolumeMeshFile = null;
		Model modelBoundingVolume = null;
		BoundingVolume boundingVolume = null;

		// check for version
		float version = Float.parseFloat(jRoot.getString("version"));

		// pivot
		Vector3 pivot = new Vector3(
			(float)jRoot.getDouble("px"),
			(float)jRoot.getDouble("py"),
			(float)jRoot.getDouble("pz")
		);

		// String thumbnail = jRoot.getString("thumbnail");
		ModelType modelType = LevelEditorModel.ModelType.valueOf(jRoot.getString("type"));
		String modelFile = jRoot.getString("file");
		String modelThumbnail = jRoot.getString("thumbnail");
		String name = jRoot.getString("name");
		String description = jRoot.getString("descr");

		JSONObject jBv = jRoot.getJSONObject("bv");
		String bvTypeString = jBv.getString("type");
		if (bvTypeString.equalsIgnoreCase("none") == true) {
			boundingVolume = null;
		} else
		if (bvTypeString.equalsIgnoreCase("sphere") == true) {
			boundingVolume = new Sphere(
				new Vector3(
					(float)jBv.getDouble("cx"),
					(float)jBv.getDouble("cy"),
					(float)jBv.getDouble("cz")
				),
				(float)jBv.getDouble("r")
			);
		} else
		if (bvTypeString.equalsIgnoreCase("capsule") == true) {
			boundingVolume = new Capsule(
				new Vector3(
					(float)jBv.getDouble("ax"),
					(float)jBv.getDouble("ay"),
					(float)jBv.getDouble("az")
				),
				new Vector3(
					(float)jBv.getDouble("bx"),
					(float)jBv.getDouble("by"),
					(float)jBv.getDouble("bz")
				),
				(float)jBv.getDouble("r")
			);
		} else
		if (bvTypeString.equalsIgnoreCase("aabb") == true) {
			boundingVolume = new BoundingBox(
				new Vector3(
					(float)jBv.getDouble("mix"),
					(float)jBv.getDouble("miy"),
					(float)jBv.getDouble("miz")
				),
				new Vector3(
					(float)jBv.getDouble("max"),
					(float)jBv.getDouble("may"),
					(float)jBv.getDouble("maz")
				)
			);
		} else
		if (bvTypeString.equalsIgnoreCase("obb") == true) {
			boundingVolume = new OrientedBoundingBox(
				new Vector3(
					(float)jBv.getDouble("cx"),
					(float)jBv.getDouble("cy"),
					(float)jBv.getDouble("cz")
				),
				new Vector3(
					(float)jBv.getDouble("a0x"),
					(float)jBv.getDouble("a0y"),
					(float)jBv.getDouble("a0z")
				),
				new Vector3(
					(float)jBv.getDouble("a1x"),
					(float)jBv.getDouble("a1y"),
					(float)jBv.getDouble("a1z")
				),
				new Vector3(
					(float)jBv.getDouble("a2x"),
					(float)jBv.getDouble("a2y"),
					(float)jBv.getDouble("a2z")
				),
				new Vector3(
					(float)jBv.getDouble("hex"),
					(float)jBv.getDouble("hey"),
					(float)jBv.getDouble("hez")
				)
			);
		} else
		if (bvTypeString.equalsIgnoreCase("convexmesh") == true) {
			try {
				boundingVolumeMeshFile = jBv.getString("file");
				Model convexMeshModel = DAEReader.read(pathName, boundingVolumeMeshFile);

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

		// load model
		Model model = DAEReader.read(pathName, jRoot.getString("file"));

		// create bounding volume model
		if (boundingVolume != null && modelBoundingVolume == null) {
			modelBoundingVolume = PrimitiveModel.createModel(boundingVolume, model.getId() + "_model_bv");
		}

		// load level editor model
		levelEditorModel = new LevelEditorModel(
			id,
			modelType,
			name,
			description,
			new File(pathName, fileName).getCanonicalPath(),
			modelThumbnail,
			model,
			boundingVolumeMeshFile,
			modelBoundingVolume,
			boundingVolume,
			pivot
		);

		// parse properties
		JSONArray jMapProperties = jRoot.getJSONArray("properties");
		for (int i = 0; i < jMapProperties.length(); i++) {
			JSONObject jMapProperty = jMapProperties.getJSONObject(i);
			levelEditorModel.addProperty(
				jMapProperty.getString("name"),
				jMapProperty.getString("value")
			);
		}

		// done
		return levelEditorModel;
	}

}

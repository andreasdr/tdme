package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
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
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
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
	 * @param game root
	 * @param id or LevelEditorEntity.ID_NONE
	 * @param path name
	 * @param file name
	 */
	public static LevelEditorEntity doImport(int id, String pathName, String fileName) throws Exception {
		fileName = fileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		JSONObject jRoot = null;
		InputStream is = null;
		try {
			jRoot = new JSONObject(new JSONTokener(FileSystem.getInstance().getContent(pathName, fileName)));
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (is != null) try { is.close(); } catch (IOException ioei) {}
		}

		LevelEditorEntity levelEditorEntity;

		// check for version
		float version = Float.parseFloat(jRoot.getString("version"));

		// pivot
		Vector3 pivot = new Vector3(
			(float)jRoot.getDouble("px"),
			(float)jRoot.getDouble("py"),
			(float)jRoot.getDouble("pz")
		);

		// String thumbnail = jRoot.getString("thumbnail");
		EntityType modelType = LevelEditorEntity.EntityType.valueOf(jRoot.getString("type"));
		String modelFile = new File(pathName, jRoot.getString("file")).getCanonicalPath();
		String modelThumbnail = jRoot.getString("thumbnail");
		String name = jRoot.getString("name");
		String description = jRoot.getString("descr");

		// load model
		Model model = null;
		String modelGameRoot = Tools.getGameRootPath(pathName);
		String modelRelativeFileName = Tools.getRelativeResourcesFileName(modelGameRoot, modelFile);
		if (modelFile.toLowerCase().endsWith(".dae")) {
			model = DAEReader.read(modelGameRoot + "/" + Tools.getPath(modelRelativeFileName), Tools.getFileName(modelRelativeFileName));
		} else
		if (modelFile.toLowerCase().endsWith(".tm")) {
			model = TMReader.read(modelGameRoot + "/" + Tools.getPath(modelRelativeFileName), Tools.getFileName(modelRelativeFileName));
		} else {
			throw new Exception("Unsupported mode file: " + modelFile);
		}

		// load level editor model
		levelEditorEntity = new LevelEditorEntity(
			id,
			modelType,
			name,
			description,
			pathName + "/" + fileName,
			new File(modelGameRoot, modelRelativeFileName).getCanonicalPath(),
			modelThumbnail,
			model,
			pivot
		);

		// parse properties
		JSONArray jProperties = jRoot.getJSONArray("properties");
		for (int i = 0; i < jProperties.length(); i++) {
			JSONObject jProperty = jProperties.getJSONObject(i);
			levelEditorEntity.addProperty(
				jProperty.getString("name"),
				jProperty.getString("value")
			);
		}

		// old: optional bounding volume
		if (jRoot.has("bv") == true) {
			levelEditorEntity.addBoundingVolume(0, parseBoundingVolume(0, levelEditorEntity, jRoot.getJSONObject("bv")));
		} else
		// new: optional bounding volumeS
		if (jRoot.has("bvs") == true) {
			JSONArray jBoundingVolumes = jRoot.getJSONArray("bvs");
			for (int i = 0; i < jBoundingVolumes.length(); i++) {
				JSONObject jBv = jBoundingVolumes.getJSONObject(i);
				levelEditorEntity.addBoundingVolume(i, parseBoundingVolume(i, levelEditorEntity, jBv));
			}
		}

		// done
		return levelEditorEntity;
	}

	/**
	 * Parse bounding volume
	 * @param idx
	 * @param level editor entity
	 * @param JSON bounding volume node
	 * @return level editor entity bounding volume
	 * @throws JSONException
	 */
	private static LevelEditorEntityBoundingVolume parseBoundingVolume(int idx, LevelEditorEntity levelEditorEntity, JSONObject jBv) throws JSONException {
		LevelEditorEntityBoundingVolume entityBoundingVolume = new LevelEditorEntityBoundingVolume(idx, levelEditorEntity); 
		BoundingVolume bv;
		String bvTypeString = jBv.getString("type");
		if (bvTypeString.equalsIgnoreCase("none") == true) {
			entityBoundingVolume.setupNone();
		} else
		if (bvTypeString.equalsIgnoreCase("sphere") == true) {
			entityBoundingVolume.setupSphere(
				new Vector3(
					(float)jBv.getDouble("cx"),
					(float)jBv.getDouble("cy"),
					(float)jBv.getDouble("cz")
				),
				(float)jBv.getDouble("r")
			);
		} else
		if (bvTypeString.equalsIgnoreCase("capsule") == true) {
			entityBoundingVolume.setupCapsule(
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
			entityBoundingVolume.setupAabb(
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
			entityBoundingVolume.setupObb(
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
				entityBoundingVolume.setupConvexMesh(jBv.getString("file"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// done
		return entityBoundingVolume;
	}

}

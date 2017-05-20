package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorLight;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.tools.Tools;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * TDME Level Editor File Export
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelFileImport {

	/**
	 * Imports a level from a TDME level file to Level Editor
	 * @param game root
	 * @param path name
	 * @param file name
	 * @param level
	 */
	public static void doImport(String pathName, String fileName, LevelEditorLevel level) throws Exception {
		doImport(pathName, fileName, level, null);
	}

	/**
	 * Imports a level from a TDME level file to Level Editor
	 * @param path name
	 * @param file name
	 * @param level
	 * @param object id prefix
	 */
	public static void doImport(String pathName, String fileName, LevelEditorLevel level, String objectIdPrefix) throws Exception {
		pathName = pathName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
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

		// game root path
		level.setGameRoot(Tools.getGameRootPath(pathName));

		// check for version
		float version = Float.parseFloat(jRoot.getString("version"));

		// new: rotation order
		level.setRotationOrder(jRoot.has("ro") == true?RotationOrder.valueOf(jRoot.getString("ro")):RotationOrder.XYZ);

		// map properties
		level.clearProperties();
		// parse properties
		JSONArray jMapProperties = jRoot.getJSONArray("properties");
		for (int i = 0; i < jMapProperties.length(); i++) {
			JSONObject jMapProperty = jMapProperties.getJSONObject(i);
			level.addProperty(
				jMapProperty.getString("name"),
				jMapProperty.getString("value")
			);
		}
		// lights
		if (jRoot.has("lights") == true) {
			JSONArray jLights = jRoot.getJSONArray("lights");
			for (int i = 0; i < jLights.length(); i++) {
				JSONObject jLight = jLights.getJSONObject(i);
				LevelEditorLight light = level.getLightAt(jLight.has("id")?jLight.getInt("id"):i);
				// set up light in level
				light.getAmbient().set(
					(float)jLight.getDouble("ar"),
					(float)jLight.getDouble("ag"),
					(float)jLight.getDouble("ab"),
					(float)jLight.getDouble("aa")
				);
				light.getDiffuse().set(
					(float)jLight.getDouble("dr"),
					(float)jLight.getDouble("dg"),
					(float)jLight.getDouble("db"),
					(float)jLight.getDouble("da")
				);
				light.getSpecular().set(
					(float)jLight.getDouble("sr"),
					(float)jLight.getDouble("sg"),
					(float)jLight.getDouble("sb"),
					(float)jLight.getDouble("sa")
				);
				light.getPosition().set(
					(float)jLight.getDouble("px"),
					(float)jLight.getDouble("py"),
					(float)jLight.getDouble("pz"),
					(float)jLight.getDouble("pw")
				);
				light.setConstantAttenuation((float)jLight.getDouble("ca"));
				light.setLinearAttenuation((float)jLight.getDouble("la"));
				light.setQuadraticAttenuation((float)jLight.getDouble("qa"));
				light.getSpotTo().set(
					(float)jLight.getDouble("stx"),
					(float)jLight.getDouble("sty"),
					(float)jLight.getDouble("stz")
				);
				light.getSpotDirection().set(
					(float)jLight.getDouble("sdx"),
					(float)jLight.getDouble("sdy"),
					(float)jLight.getDouble("sdz")
				);
				light.setSpotExponent((float)jLight.getDouble("se"));
				light.setSpotCutOff((float)jLight.getDouble("sco"));
				light.setEnabled(jLight.getBoolean("e"));
			}
		}

		// entities
		level.getEntityLibrary().clear();
		JSONArray jModels = jRoot.getJSONArray("models");
		for (int i = 0; i < jModels.length(); i++) {
			JSONObject jModel = jModels.getJSONObject(i);
			// add model to library
			LevelEditorEntity levelEditorEntity = ModelMetaDataFileImport.doImportFromJSON(jModel.getInt("id"), null, jModel.getJSONObject("entity"));

			// do we have a valid entity?
			if (levelEditorEntity == null) {
				throw new Exception("Invalid entity");
			}
			// add entity
			level.getEntityLibrary().addEntity(levelEditorEntity);

			// parse optional model properties
			if (jModel.has("properties")) {
				JSONArray jModelProperties = jModel.getJSONArray("properties");
				for (int j = 0; j < jModelProperties.length(); j++) {
					JSONObject jModelProperty = jModelProperties.getJSONObject(j);
					levelEditorEntity.addProperty(
						jModelProperty.getString("name"),
						jModelProperty.getString("value")
					);
				}
			}
		}
		// objects
		level.clearObjects();
		JSONArray jObjects = jRoot.getJSONArray("objects");
		for (int i = 0; i < jObjects.length(); i++) {
			JSONObject jObject = jObjects.getJSONObject(i);
			LevelEditorEntity model = level.getEntityLibrary().getEntity(jObject.getInt("mid"));
			Transformations transformations = new Transformations();
			transformations.getPivot().set(model.getPivot());
			transformations.getTranslation().set(
				(float)jObject.getDouble("tx"),
				(float)jObject.getDouble("ty"),
				(float)jObject.getDouble("tz")
			);
			transformations.getScale().set(
				(float)jObject.getDouble("sx"),
				(float)jObject.getDouble("sy"),
				(float)jObject.getDouble("sz")
			);
			Vector3 rotation = new Vector3(
				(float)jObject.getDouble("rx"),
				(float)jObject.getDouble("ry"),
				(float)jObject.getDouble("rz")
			);
			transformations.getRotations().add(
				new Rotation(rotation.getArray()[level.getRotationOrder().getAxis0VectorIndex()], level.getRotationOrder().getAxis0())
			);
			transformations.getRotations().add(
				new Rotation(rotation.getArray()[level.getRotationOrder().getAxis1VectorIndex()], level.getRotationOrder().getAxis1())
			);
			transformations.getRotations().add(
				new Rotation(rotation.getArray()[level.getRotationOrder().getAxis2VectorIndex()], level.getRotationOrder().getAxis2())
			);
			transformations.update();
			LevelEditorObject levelEditorObject = new LevelEditorObject(
				objectIdPrefix != null?objectIdPrefix + jObject.getString("id"):jObject.getString("id"),
				jObject.has("descr")?jObject.getString("descr"):"",
				transformations,
				model
			);
			levelEditorObject.clearProperties();
			// parse optional object properties, new in LE 0.3a
			if (jObject.has("properties")) {
				JSONArray jObjectProperties = jObject.getJSONArray("properties");
				for (int j = 0; j < jObjectProperties.length(); j++) {
					JSONObject jObjectProperty = jObjectProperties.getJSONObject(j);
					levelEditorObject.addProperty(
						jObjectProperty.getString("name"),
						jObjectProperty.getString("value")
					);
				} 
			}

			// check if entity already exists
			//	small workaround for a bug that would allow to place to objects at same place with same model
			/*
			boolean skipObject = false;
			for (int j = 0; j < level.getObjectCount(); j++) {
				LevelEditorObject _levelEditorObject = level.getObjectAt(j);
				if (_levelEditorObject.getModel() == levelEditorObject.getModel() &&
					_levelEditorObject.getTransformations().getTranslation().equals(levelEditorObject.getTransformations().getTranslation())) {
					System.out.println("Skipping '" + levelEditorObject + "' as we already have a object with this model and translation.");
					// we already have a object with selected model on this translation
					skipObject = true;
					break;
				}
			}

			// skip on object if requested
			if (skipObject == true) continue;
			*/

			// otherwise add
			level.addObject(levelEditorObject);
		}
		level.setObjectIdx(jRoot.getInt("objects_eidx"));
		level.setPathName(pathName);
		level.setFileName(fileName);
		level.computeDimension();
	}

}

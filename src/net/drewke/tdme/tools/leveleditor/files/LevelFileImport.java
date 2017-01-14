package net.drewke.tdme.tools.leveleditor.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorLevel;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorLight;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorObject;
import net.drewke.tdme.tools.leveleditor.model.PropertyModelClass;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel.ModelType;

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
	 * @param model root
	 * @param path name
	 * @param file name
	 * @param level
	 */
	public static void doImport(String modelRoot, String gameRoot, String pathName, String fileName, LevelEditorLevel level) throws Exception {
		modelRoot = modelRoot.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		gameRoot = gameRoot.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
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

		// check for version
		float version = Float.parseFloat(jRoot.getString("version"));

		// map properties
		level.clearProperties();
		// parse properties
		JSONArray jMapProperties = jRoot.getJSONArray("properties");
		for (int i = 0; i < jMapProperties.length(); i++) {
			JSONObject jMapProperty = jMapProperties.getJSONObject(i);
			level.addProperty(
				new PropertyModelClass(
					jMapProperty.getString("name"),
					jMapProperty.getString("value")
				)
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


		// models
		level.getModelLibrary().clear();
		JSONArray jModels = jRoot.getJSONArray("models");
		for (int i = 0; i < jModels.length(); i++) {
			JSONObject jModel = jModels.getJSONObject(i);
			String modelFileName = jModel.getString("file");
			File modelFile = new File(modelFileName);
			// check if file exists on this computer
			if (modelFile.exists() == false) {
				// nope, try to get file via structure knowledge of game
				modelFileName = modelFileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
				if (modelRoot != null) {
					int modelRootIdx = modelFileName.lastIndexOf(modelRoot);
					if (modelRootIdx == -1) modelRootIdx = modelFileName.lastIndexOf("resources\\models\\");
					if (modelRootIdx != -1) modelFileName = modelFileName.substring(modelRootIdx);
				}
				modelFile = new File(pathName + gameRoot + modelFileName);
			}
			// add model to library
			LevelEditorModel levelEditorModel = null;
			if (jModel.has("type") == false || LevelEditorModel.ModelType.valueOf(jModel.getString("type")) == ModelType.MODEL) {
				levelEditorModel = level.getModelLibrary().addModel(	
					jModel.getInt("id"),
					jModel.has("name")?jModel.getString("name"):"unknown",
					jModel.has("descr")?jModel.getString("descr"):"",
					modelFile.getParentFile().getCanonicalPath(),
					modelFile.getName(),
					new Vector3(
						jModel.has("px")?(float)jModel.getDouble("px"):0.0f,
						jModel.has("py")?(float)jModel.getDouble("py"):0.0f,
						jModel.has("pz")?(float)jModel.getDouble("pz"):0.0f
					)
				);
			} else 
			if (jModel.has("type") == true && LevelEditorModel.ModelType.valueOf(jModel.getString("type")) == ModelType.TRIGGER) {
				JSONObject jBv = jModel.getJSONObject("bv");
				BoundingBox boundingBox = new BoundingBox(
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
				levelEditorModel = level.getModelLibrary().createTrigger(	
					jModel.getInt("id"),
					jModel.has("name")?jModel.getString("name"):"unknown",
					jModel.has("descr")?jModel.getString("descr"):"",
					boundingBox.getMax().getX() - boundingBox.getMin().getX(),
					boundingBox.getMax().getY() - boundingBox.getMin().getY(),
					boundingBox.getMax().getZ() - boundingBox.getMin().getZ(),
					new Vector3(
						jModel.has("px")?(float)jModel.getDouble("px"):0.0f,
						jModel.has("py")?(float)jModel.getDouble("py"):0.0f,
						jModel.has("pz")?(float)jModel.getDouble("pz"):0.0f
					)
				);				
			}
			if (levelEditorModel == null) {
				throw new Exception("Invalid model");
			}
			// parse optional model properties
			if (jModel.has("properties")) {
				JSONArray jModelProperties = jModel.getJSONArray("properties");
				for (int j = 0; j < jModelProperties.length(); j++) {
					JSONObject jModelProperty = jModelProperties.getJSONObject(j);
					levelEditorModel.addProperty(
						new PropertyModelClass(
							jModelProperty.getString("name"),
							jModelProperty.getString("value")
						)
					);
				}
			}
		}
		// objects
		level.clearObjects();
		JSONArray jObjects = jRoot.getJSONArray("objects");
		for (int i = 0; i < jObjects.length(); i++) {
			JSONObject jObject = jObjects.getJSONObject(i);
			LevelEditorModel model = level.getModelLibrary().getModel(jObject.getInt("mid"));
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
			transformations.getRotations().add(
				new Rotation((float)jObject.getDouble("rx"), new Vector3(1.0f, 0.0f, 0.0f))
			);
			transformations.getRotations().add(
				new Rotation((float)jObject.getDouble("ry"), new Vector3(0.0f, 1.0f, 0.0f))
			);
			transformations.getRotations().add(
				new Rotation((float)jObject.getDouble("rz"), new Vector3(0.0f, 0.0f, 1.0f))
			);
			transformations.update();
			LevelEditorObject levelEditorObject = new LevelEditorObject(
				jObject.getString("id"),
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
						new PropertyModelClass(
							jObjectProperty.getString("name"),
							jObjectProperty.getString("value")
						)
					);
				}
			}

			// check if entity already exists
			//	small workaround for a bug that would allow to place to objects at same place with same model
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

			// otherwise add
			level.addObject(levelEditorObject);
		}
		level.setObjectIdx(jRoot.getInt("objects_eidx"));
		level.setPathName(pathName);
		level.setFileName(fileName);
		level.computeDimension();
	}

}

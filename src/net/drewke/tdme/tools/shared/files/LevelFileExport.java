package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorLight;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TDME Level Editor File Export
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelFileExport {

	/**
	 * Exports a level to a TDME level file
	 * @param file name
	 */
	public static void export(String fileName, LevelEditorLevel level) throws Exception {
		FileOutputStream fos = null;
	    PrintStream fops = null;
	    level.setFileName(new File(fileName).getName());
		try {
			// generate json
			LevelEditorEntityLibrary entityLibrary = level.getEntityLibrary();
			JSONObject jRoot = new JSONObject();
			jRoot.put("version", "0.99");
			jRoot.put("ro", level.getRotationOrder().toString());
			JSONArray jLights = new JSONArray();
			for (int i = 0; i < level.getLightCount(); i++) {
				LevelEditorLight light = level.getLightAt(i); 
				JSONObject jLight = new JSONObject();
				jLight.put("id", i);
				jLight.put("ar", light.getAmbient().getRed());
				jLight.put("ag", light.getAmbient().getGreen());
				jLight.put("ab", light.getAmbient().getBlue());
				jLight.put("aa", light.getAmbient().getAlpha());
				jLight.put("dr", light.getDiffuse().getRed());
				jLight.put("dg", light.getDiffuse().getGreen());
				jLight.put("db", light.getDiffuse().getBlue());
				jLight.put("da", light.getDiffuse().getAlpha());
				jLight.put("sr", light.getSpecular().getRed());
				jLight.put("sg", light.getSpecular().getGreen());
				jLight.put("sb", light.getSpecular().getBlue());
				jLight.put("sa", light.getSpecular().getAlpha());
				jLight.put("px", light.getPosition().getX());
				jLight.put("py", light.getPosition().getY());
				jLight.put("pz", light.getPosition().getZ());
				jLight.put("pw", light.getPosition().getW());
				jLight.put("stx", light.getSpotTo().getX());
				jLight.put("sty", light.getSpotTo().getY());
				jLight.put("stz", light.getSpotTo().getZ());
				jLight.put("sdx", light.getSpotDirection().getX());
				jLight.put("sdy", light.getSpotDirection().getY());
				jLight.put("sdz", light.getSpotDirection().getZ());
				jLight.put("se", light.getSpotExponent());
				jLight.put("sco", light.getSpotCutOff());
				jLight.put("ca", light.getConstantAttenuation());
				jLight.put("la", light.getLinearAttenuation());
				jLight.put("qa", light.getQuadraticAttenuation());
				jLight.put("e", light.isEnabled());
				jLights.put(jLight);
			}
			jRoot.put("lights", jLights);
			JSONArray jEntityLibrary = new JSONArray();
			for (int i = 0; i < entityLibrary.getEntityCount(); i++) {
				LevelEditorEntity entity = entityLibrary.getEntityAt(i);
				JSONObject jModel = new JSONObject();
				jModel.put("id", entity.getId());
				jModel.put("type", entity.getType());
				jModel.put("name", entity.getName());
				jModel.put("descr", entity.getDescription());
				jModel.put("entity", ModelMetaDataFileExport.exportToJSON(entity));
				jEntityLibrary.put(jModel);
			}
			JSONArray jMapProperties = new JSONArray();
			for (PropertyModelClass mapProperty: level.getProperties()) {
				JSONObject jMapProperty = new JSONObject();
				jMapProperty.put("name", mapProperty.getName());
				jMapProperty.put("value", mapProperty.getValue());
				jMapProperties.put(jMapProperty);
			}
			jRoot.put("properties", jMapProperties);
			jRoot.put("models", jEntityLibrary);
			JSONArray jObjects = new JSONArray();
			for (int i = 0; i < level.getObjectCount(); i++) {
				LevelEditorObject levelEditorObject = level.getObjectAt(i);
				JSONObject jObject = new JSONObject();
				Transformations transformations = levelEditorObject.getTransformations();
				Vector3 translation = transformations.getTranslation();
				Vector3 scale = transformations.getScale();
				Rotation rotationAroundXAxis = transformations.getRotations().get(level.getRotationOrder().getAxisXIndex());
				Rotation rotationAroundYAxis = transformations.getRotations().get(level.getRotationOrder().getAxisYIndex());
				Rotation rotationAroundZAxis = transformations.getRotations().get(level.getRotationOrder().getAxisZIndex());
				jObject.put("id", levelEditorObject.getId());
				jObject.put("descr", levelEditorObject.getDescription());
				jObject.put("mid", levelEditorObject.getEntity().getId());
				jObject.put("tx", translation.getX());
				jObject.put("ty", translation.getY());
				jObject.put("tz", translation.getZ());
				jObject.put("sx", scale.getX());
				jObject.put("sy", scale.getY());
				jObject.put("sz", scale.getZ());
				jObject.put("rx", rotationAroundXAxis.getAngle());
				jObject.put("ry", rotationAroundYAxis.getAngle());
				jObject.put("rz", rotationAroundZAxis.getAngle());
				JSONArray jObjectProperties = new JSONArray();
				for (PropertyModelClass objectProperty: levelEditorObject.getProperties()) {
					JSONObject jObjectProperty = new JSONObject();
					jObjectProperty.put("name", objectProperty.getName());
					jObjectProperty.put("value", objectProperty.getValue());
					jObjectProperties.put(jObjectProperty);					
				}
				jObject.put("properties", jObjectProperties);
				jObjects.put(jObject);
			}
			jRoot.put("objects", jObjects);
			jRoot.put("objects_eidx", level.getObjectIdx());

			// save to file
			fos = new FileOutputStream(new File(fileName)); 
	        fops = new PrintStream(fos);
	        fops.print(jRoot.toString(2));  
		} catch (JSONException je) {
			je.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fops != null) fops.close();
			if (fos != null) try { fos.close(); } catch (IOException ioe) {}
		}
	}

}

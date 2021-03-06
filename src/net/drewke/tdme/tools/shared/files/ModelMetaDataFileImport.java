package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.BoundingBoxParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitterPlaneVelocity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.Emitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.ObjectParticleSystem;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.PointParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.SphereParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.Type;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.utils.Console;

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
	 * Imports a model meta data file from file
	 * @param id or LevelEditorEntity.ID_NONE
	 * @param path name
	 * @param file name
	 */
	public static LevelEditorEntity doImport(int id, String pathName, String fileName) throws Exception {
		fileName = fileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		JSONObject jEntityRoot = null;
		InputStream is = null;
		try {
			jEntityRoot = new JSONObject(new JSONTokener(FileSystem.getInstance().getContent(pathName, fileName)));
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (is != null) try { is.close(); } catch (IOException ioei) {}
		}

		// do the work
		LevelEditorEntity levelEditorEntity = doImportFromJSON(id, pathName, jEntityRoot);
		levelEditorEntity.setEntityFileName(pathName + "/" + fileName);
		return levelEditorEntity;
	}

	/**
	 * Imports a model meta data file from JSON object
	 * @param id or LevelEditorEntity.ID_NONE
	 * @param path name or null
	 * @param JSON entity root
	 */
	public static LevelEditorEntity doImportFromJSON(int id, String pathName, JSONObject jEntityRoot) throws Exception {
		LevelEditorEntity levelEditorEntity;

		// check for version
		float version = Float.parseFloat(jEntityRoot.getString("version"));

		// pivot
		Vector3 pivot = new Vector3(
			(float)jEntityRoot.getDouble("px"),
			(float)jEntityRoot.getDouble("py"),
			(float)jEntityRoot.getDouble("pz")
		);

		// String thumbnail = jRoot.getString("thumbnail");
		EntityType modelType = LevelEditorEntity.EntityType.valueOf(jEntityRoot.getString("type"));
		String modelFile = 
			jEntityRoot.has("file") == true?
				new File(jEntityRoot.getString("file")).getCanonicalPath():
				null;
		String modelThumbnail = jEntityRoot.has("thumbnail") == true?jEntityRoot.getString("thumbnail"):null;
		String name = jEntityRoot.getString("name");
		String description = jEntityRoot.getString("descr");

		// load model
		Model model = null;

		// having a model file?
		String gameRoot = Tools.getGameRootPath(pathName);
		String modelRelativeFileName = null;
		if (modelFile != null) {
			// yep, load it
			modelRelativeFileName = Tools.getRelativeResourcesFileName(gameRoot, modelFile);
			String modelPath = (gameRoot.length() > 0?gameRoot + "/":"") + Tools.getPath(modelRelativeFileName);
			if (modelFile.toLowerCase().endsWith(".dae")) {
				model = DAEReader.read(modelPath, Tools.getFileName(modelRelativeFileName));
			} else
			if (modelFile.toLowerCase().endsWith(".tm")) {
				model = TMReader.read(modelPath, Tools.getFileName(modelRelativeFileName));
			} else {
				throw new Exception("Unsupported mode file: " + modelFile);
			}
		} else
		// load model for empty
		//	TODO: only load if required e.g. for LevelEditor
		if (modelType == EntityType.EMPTY) {
			model = DAEReader.read("resources/tools/leveleditor/models", "arrow.dae");
		}

		// load level editor model
		levelEditorEntity = new LevelEditorEntity(
			id,
			modelType,
			name,
			description,
			null,
			modelFile != null?new File(gameRoot, modelRelativeFileName).getCanonicalPath():null,
			modelThumbnail,
			model,
			pivot
		);

		// parse properties
		JSONArray jProperties = jEntityRoot.getJSONArray("properties");
		for (int i = 0; i < jProperties.length(); i++) {
			JSONObject jProperty = jProperties.getJSONObject(i);
			levelEditorEntity.addProperty(
				jProperty.getString("name"),
				jProperty.getString("value")
			);
		}

		// old: optional bounding volume
		if (jEntityRoot.has("bv") == true) {
			levelEditorEntity.addBoundingVolume(0, parseBoundingVolume(0, levelEditorEntity, jEntityRoot.getJSONObject("bv")));
		} else
		// new: optional bounding volumeS
		if (jEntityRoot.has("bvs") == true) {
			JSONArray jBoundingVolumes = jEntityRoot.getJSONArray("bvs");
			for (int i = 0; i < jBoundingVolumes.length(); i++) {
				JSONObject jBv = jBoundingVolumes.getJSONObject(i);
				levelEditorEntity.addBoundingVolume(i, parseBoundingVolume(i, levelEditorEntity, jBv));
			}
		}
		// parse particle system 
		if (modelType == EntityType.PARTICLESYSTEM) {
			LevelEditorEntityParticleSystem particleSystem = levelEditorEntity.getParticleSystem();
			JSONObject jParticleSystem = jEntityRoot.getJSONObject("ps");

			// type
			particleSystem.setType(Type.valueOf(jParticleSystem.getString("t")));
			switch (particleSystem.getType()) {
				case NONE:
					{
						break;
					}
				case OBJECT_PARTICLE_SYSTEM: 
					{
						JSONObject jObjectParticleSystem = jParticleSystem.getJSONObject("ops");
						ObjectParticleSystem objectParticleSystem = particleSystem.getObjectParticleSystem();
						objectParticleSystem.setMaxCount(jObjectParticleSystem.getInt("mc"));
						objectParticleSystem.getScale().setX((float)jObjectParticleSystem.getDouble("sx"));
						objectParticleSystem.getScale().setY((float)jObjectParticleSystem.getDouble("sy"));
						objectParticleSystem.getScale().setZ((float)jObjectParticleSystem.getDouble("sz"));
						objectParticleSystem.setAutoEmit(jObjectParticleSystem.getBoolean("ae"));
						try {
							String particleModelFile = jObjectParticleSystem.getString("mf");
							String particleModelRelativeFileName = Tools.getRelativeResourcesFileName(gameRoot, particleModelFile);
							String particleModelPath = (gameRoot.length() > 0?gameRoot + "/":"") + Tools.getPath(particleModelRelativeFileName);
							objectParticleSystem.setModelFile(particleModelPath + "/" + Tools.getFileName(particleModelRelativeFileName));
						} catch (Exception exception) {
							exception.printStackTrace();
							Console.println("ModelMetaDataFileImport::doImport(): Failed to set model file: " + exception.getMessage());
						}
						break;
					}
				case POINT_PARTICLE_SYSTEM:
					{
						JSONObject jPointParticleSystem = jParticleSystem.getJSONObject("pps");
						particleSystem.getPointParticleSystem().setMaxPoints(jPointParticleSystem.getInt("mp"));
						particleSystem.getPointParticleSystem().setAutoEmit(jPointParticleSystem.getBoolean("ae"));
						break;
					}
				default:
					{
						Console.println("ModelMetaDataFileExport::export(): unknown particle system type '" + particleSystem.getType() + "'");
						break;
					}
			}

			// emitter
			particleSystem.setEmitter(Emitter.valueOf(jParticleSystem.getString("e")));
			switch (particleSystem.getEmitter()) {
				case NONE:
					{
						break;
					}
				case POINT_PARTICLE_EMITTER:
					{
						JSONObject jPointParticleEmitter = jParticleSystem.getJSONObject("ppe");
						PointParticleEmitter emitter = particleSystem.getPointParticleEmitter();
						emitter.setCount(jPointParticleEmitter.getInt("c"));
						emitter.setLifeTime(jPointParticleEmitter.getLong("lt"));
						emitter.setLifeTimeRnd(jPointParticleEmitter.getLong("ltrnd"));
						emitter.setMass((float)jPointParticleEmitter.getDouble("m"));
						emitter.setMassRnd((float)jPointParticleEmitter.getDouble("mrnd"));
						emitter.getPosition().setX((float)jPointParticleEmitter.getDouble("px"));
						emitter.getPosition().setY((float)jPointParticleEmitter.getDouble("py"));
						emitter.getPosition().setZ((float)jPointParticleEmitter.getDouble("pz"));
						emitter.getVelocity().setX((float)jPointParticleEmitter.getDouble("vx"));
						emitter.getVelocity().setY((float)jPointParticleEmitter.getDouble("vy"));
						emitter.getVelocity().setZ((float)jPointParticleEmitter.getDouble("vz"));
						emitter.getVelocityRnd().setX((float)jPointParticleEmitter.getDouble("vrndx"));
						emitter.getVelocityRnd().setY((float)jPointParticleEmitter.getDouble("vrndy"));
						emitter.getVelocityRnd().setZ((float)jPointParticleEmitter.getDouble("vrndz"));
						emitter.getColorStart().setRed((float)jPointParticleEmitter.getDouble("csr"));
						emitter.getColorStart().setGreen((float)jPointParticleEmitter.getDouble("csg"));
						emitter.getColorStart().setBlue((float)jPointParticleEmitter.getDouble("csb"));
						emitter.getColorStart().setAlpha((float)jPointParticleEmitter.getDouble("csa"));
						emitter.getColorEnd().setRed((float)jPointParticleEmitter.getDouble("cer"));
						emitter.getColorEnd().setGreen((float)jPointParticleEmitter.getDouble("ceg"));
						emitter.getColorEnd().setBlue((float)jPointParticleEmitter.getDouble("ceb"));
						emitter.getColorEnd().setAlpha((float)jPointParticleEmitter.getDouble("cea"));
						break;
					}
				case BOUNDINGBOX_PARTICLE_EMITTER:
					{
						JSONObject jBoundingBoxParticleEmitter = jParticleSystem.getJSONObject("bbpe");
						BoundingBoxParticleEmitter emitter = particleSystem.getBoundingBoxParticleEmitters();
						emitter.setCount(jBoundingBoxParticleEmitter.getInt("c"));
						emitter.setLifeTime(jBoundingBoxParticleEmitter.getLong("lt"));
						emitter.setLifeTimeRnd(jBoundingBoxParticleEmitter.getLong("ltrnd"));
						emitter.setMass((float)jBoundingBoxParticleEmitter.getDouble("m"));
						emitter.setMassRnd((float)jBoundingBoxParticleEmitter.getDouble("mrnd"));
						emitter.getVelocity().setX((float)jBoundingBoxParticleEmitter.getDouble("vx"));
						emitter.getVelocity().setY((float)jBoundingBoxParticleEmitter.getDouble("vy"));
						emitter.getVelocity().setZ((float)jBoundingBoxParticleEmitter.getDouble("vz"));
						emitter.getVelocityRnd().setX((float)jBoundingBoxParticleEmitter.getDouble("vrndx"));
						emitter.getVelocityRnd().setY((float)jBoundingBoxParticleEmitter.getDouble("vrndy"));
						emitter.getVelocityRnd().setZ((float)jBoundingBoxParticleEmitter.getDouble("vrndz"));
						emitter.getColorStart().setRed((float)jBoundingBoxParticleEmitter.getDouble("csr"));
						emitter.getColorStart().setGreen((float)jBoundingBoxParticleEmitter.getDouble("csg"));
						emitter.getColorStart().setBlue((float)jBoundingBoxParticleEmitter.getDouble("csb"));
						emitter.getColorStart().setAlpha((float)jBoundingBoxParticleEmitter.getDouble("csa"));
						emitter.getColorEnd().setRed((float)jBoundingBoxParticleEmitter.getDouble("cer"));
						emitter.getColorEnd().setGreen((float)jBoundingBoxParticleEmitter.getDouble("ceg"));
						emitter.getColorEnd().setBlue((float)jBoundingBoxParticleEmitter.getDouble("ceb"));
						emitter.getColorEnd().setAlpha((float)jBoundingBoxParticleEmitter.getDouble("cea"));
						emitter.getObbCenter().setX((float)jBoundingBoxParticleEmitter.getDouble("ocx"));
						emitter.getObbCenter().setY((float)jBoundingBoxParticleEmitter.getDouble("ocy"));
						emitter.getObbCenter().setZ((float)jBoundingBoxParticleEmitter.getDouble("ocz"));
						emitter.getObbHalfextension().setX((float)jBoundingBoxParticleEmitter.getDouble("ohex"));
						emitter.getObbHalfextension().setY((float)jBoundingBoxParticleEmitter.getDouble("ohey"));
						emitter.getObbHalfextension().setZ((float)jBoundingBoxParticleEmitter.getDouble("ohez"));
						emitter.getObbAxis0().setX((float)jBoundingBoxParticleEmitter.getDouble("oa0x"));
						emitter.getObbAxis0().setY((float)jBoundingBoxParticleEmitter.getDouble("oa0y"));
						emitter.getObbAxis0().setZ((float)jBoundingBoxParticleEmitter.getDouble("oa0z"));
						emitter.getObbAxis1().setX((float)jBoundingBoxParticleEmitter.getDouble("oa1x"));
						emitter.getObbAxis1().setY((float)jBoundingBoxParticleEmitter.getDouble("oa1y"));
						emitter.getObbAxis1().setZ((float)jBoundingBoxParticleEmitter.getDouble("oa1z"));
						emitter.getObbAxis2().setX((float)jBoundingBoxParticleEmitter.getDouble("oa2x"));
						emitter.getObbAxis2().setY((float)jBoundingBoxParticleEmitter.getDouble("oa2y"));
						emitter.getObbAxis2().setZ((float)jBoundingBoxParticleEmitter.getDouble("oa2z"));
						break;
					}
				case CIRCLE_PARTICLE_EMITTER:
					{
						JSONObject jCircleParticleEmitter = jParticleSystem.getJSONObject("cpe");
						CircleParticleEmitter emitter = particleSystem.getCircleParticleEmitter();
						emitter.setCount(jCircleParticleEmitter.getInt("c"));
						emitter.setLifeTime(jCircleParticleEmitter.getLong("lt"));
						emitter.setLifeTimeRnd(jCircleParticleEmitter.getLong("ltrnd"));
						emitter.setMass((float)jCircleParticleEmitter.getDouble("m"));
						emitter.setMassRnd((float)jCircleParticleEmitter.getDouble("mrnd"));
						emitter.getVelocity().setX((float)jCircleParticleEmitter.getDouble("vx"));
						emitter.getVelocity().setY((float)jCircleParticleEmitter.getDouble("vy"));
						emitter.getVelocity().setZ((float)jCircleParticleEmitter.getDouble("vz"));
						emitter.getVelocityRnd().setX((float)jCircleParticleEmitter.getDouble("vrndx"));
						emitter.getVelocityRnd().setY((float)jCircleParticleEmitter.getDouble("vrndy"));
						emitter.getVelocityRnd().setZ((float)jCircleParticleEmitter.getDouble("vrndz"));
						emitter.getColorStart().setRed((float)jCircleParticleEmitter.getDouble("csr"));
						emitter.getColorStart().setGreen((float)jCircleParticleEmitter.getDouble("csg"));
						emitter.getColorStart().setBlue((float)jCircleParticleEmitter.getDouble("csb"));
						emitter.getColorStart().setAlpha((float)jCircleParticleEmitter.getDouble("csa"));
						emitter.getColorEnd().setRed((float)jCircleParticleEmitter.getDouble("cer"));
						emitter.getColorEnd().setGreen((float)jCircleParticleEmitter.getDouble("ceg"));
						emitter.getColorEnd().setBlue((float)jCircleParticleEmitter.getDouble("ceb"));
						emitter.getColorEnd().setAlpha((float)jCircleParticleEmitter.getDouble("cea"));
						emitter.getCenter().setX((float)jCircleParticleEmitter.getDouble("cx"));
						emitter.getCenter().setY((float)jCircleParticleEmitter.getDouble("cy"));
						emitter.getCenter().setZ((float)jCircleParticleEmitter.getDouble("cz"));
						emitter.setRadius((float)jCircleParticleEmitter.getDouble("r"));
						emitter.getAxis0().setX((float)jCircleParticleEmitter.getDouble("a0x"));
						emitter.getAxis0().setY((float)jCircleParticleEmitter.getDouble("a0y"));
						emitter.getAxis0().setZ((float)jCircleParticleEmitter.getDouble("a0z"));
						emitter.getAxis1().setX((float)jCircleParticleEmitter.getDouble("a1x"));
						emitter.getAxis1().setY((float)jCircleParticleEmitter.getDouble("a1y"));
						emitter.getAxis1().setZ((float)jCircleParticleEmitter.getDouble("a1z"));
						break;
					}
				case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY: 
					{
						JSONObject jCircleParticleEmitterPlaneVelocity = jParticleSystem.getJSONObject("cpeev");
						CircleParticleEmitterPlaneVelocity emitter = particleSystem.getCircleParticleEmitterPlaneVelocity();
						emitter.setCount(jCircleParticleEmitterPlaneVelocity.getInt("c"));
						emitter.setLifeTime(jCircleParticleEmitterPlaneVelocity.getLong("lt"));
						emitter.setLifeTimeRnd(jCircleParticleEmitterPlaneVelocity.getLong("ltrnd"));
						emitter.setMass((float)jCircleParticleEmitterPlaneVelocity.getDouble("m"));
						emitter.setMassRnd((float)jCircleParticleEmitterPlaneVelocity.getDouble("mrnd"));
						emitter.setVelocity((float)jCircleParticleEmitterPlaneVelocity.getDouble("v"));
						emitter.setVelocityRnd((float)jCircleParticleEmitterPlaneVelocity.getDouble("vrnd"));
						emitter.getColorStart().setRed((float)jCircleParticleEmitterPlaneVelocity.getDouble("csr"));
						emitter.getColorStart().setGreen((float)jCircleParticleEmitterPlaneVelocity.getDouble("csg"));
						emitter.getColorStart().setBlue((float)jCircleParticleEmitterPlaneVelocity.getDouble("csb"));
						emitter.getColorStart().setAlpha((float)jCircleParticleEmitterPlaneVelocity.getDouble("csa"));
						emitter.getColorEnd().setRed((float)jCircleParticleEmitterPlaneVelocity.getDouble("cer"));
						emitter.getColorEnd().setGreen((float)jCircleParticleEmitterPlaneVelocity.getDouble("ceg"));
						emitter.getColorEnd().setBlue((float)jCircleParticleEmitterPlaneVelocity.getDouble("ceb"));
						emitter.getColorEnd().setAlpha((float)jCircleParticleEmitterPlaneVelocity.getDouble("cea"));
						emitter.getCenter().setX((float)jCircleParticleEmitterPlaneVelocity.getDouble("cx"));
						emitter.getCenter().setY((float)jCircleParticleEmitterPlaneVelocity.getDouble("cy"));
						emitter.getCenter().setZ((float)jCircleParticleEmitterPlaneVelocity.getDouble("cz"));
						emitter.setRadius((float)jCircleParticleEmitterPlaneVelocity.getDouble("r"));
						emitter.getAxis0().setX((float)jCircleParticleEmitterPlaneVelocity.getDouble("a0x"));
						emitter.getAxis0().setY((float)jCircleParticleEmitterPlaneVelocity.getDouble("a0y"));
						emitter.getAxis0().setZ((float)jCircleParticleEmitterPlaneVelocity.getDouble("a0z"));
						emitter.getAxis1().setX((float)jCircleParticleEmitterPlaneVelocity.getDouble("a1x"));
						emitter.getAxis1().setY((float)jCircleParticleEmitterPlaneVelocity.getDouble("a1y"));
						emitter.getAxis1().setZ((float)jCircleParticleEmitterPlaneVelocity.getDouble("a1z"));
						break;
					}
				case SPHERE_PARTICLE_EMITTER:
					{
						JSONObject jSphereParticleEmitter = jParticleSystem.getJSONObject("spe");
						SphereParticleEmitter emitter = particleSystem.getSphereParticleEmitter();
						emitter.setCount(jSphereParticleEmitter.getInt("c"));
						emitter.setLifeTime(jSphereParticleEmitter.getLong("lt"));
						emitter.setLifeTimeRnd(jSphereParticleEmitter.getLong("ltrnd"));
						emitter.setMass((float)jSphereParticleEmitter.getDouble("m"));
						emitter.setMassRnd((float)jSphereParticleEmitter.getDouble("mrnd"));
						emitter.getVelocity().setX((float)jSphereParticleEmitter.getDouble("vx"));
						emitter.getVelocity().setY((float)jSphereParticleEmitter.getDouble("vy"));
						emitter.getVelocity().setZ((float)jSphereParticleEmitter.getDouble("vz"));
						emitter.getVelocityRnd().setX((float)jSphereParticleEmitter.getDouble("vrndx"));
						emitter.getVelocityRnd().setY((float)jSphereParticleEmitter.getDouble("vrndy"));
						emitter.getVelocityRnd().setZ((float)jSphereParticleEmitter.getDouble("vrndz"));
						emitter.getColorStart().setRed((float)jSphereParticleEmitter.getDouble("csr"));
						emitter.getColorStart().setGreen((float)jSphereParticleEmitter.getDouble("csg"));
						emitter.getColorStart().setBlue((float)jSphereParticleEmitter.getDouble("csb"));
						emitter.getColorStart().setAlpha((float)jSphereParticleEmitter.getDouble("csa"));
						emitter.getColorEnd().setRed((float)jSphereParticleEmitter.getDouble("cer"));
						emitter.getColorEnd().setGreen((float)jSphereParticleEmitter.getDouble("ceg"));
						emitter.getColorEnd().setBlue((float)jSphereParticleEmitter.getDouble("ceb"));
						emitter.getColorEnd().setAlpha((float)jSphereParticleEmitter.getDouble("cea"));
						emitter.getCenter().setX((float)jSphereParticleEmitter.getDouble("cx"));
						emitter.getCenter().setY((float)jSphereParticleEmitter.getDouble("cy"));
						emitter.getCenter().setZ((float)jSphereParticleEmitter.getDouble("cz"));
						emitter.setRadius((float)jSphereParticleEmitter.getDouble("r"));
						break;
					}
				default:
					Console.println("ModelMetaDataFileExport::export(): unknown particle system emitter '" + particleSystem.getEmitter() + "'");
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

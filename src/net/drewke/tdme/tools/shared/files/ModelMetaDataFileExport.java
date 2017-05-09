package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import net.drewke.tdme.engine.fileio.models.TMWriter;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.primitives.Triangle;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.BoundingBoxParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.CircleParticleEmitterPlaneVelocity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.PointParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem.SphereParticleEmitter;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityParticleSystem;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TDME Model meta data file export
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ModelMetaDataFileExport {

	/**
	 * Copy file
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFile(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) try { is.close(); } catch (IOException ioe) {}
			if (os != null) try { os.close(); } catch (IOException ioe) {}
		}
	}

	/**
	 * Exports a level to a TDME level file
	 * @param file name
	 * @param entity
	 */
	public static void export(String fileName, LevelEditorEntity entity) throws Exception {
		FileOutputStream fos = null;
	    PrintStream fops = null;
		try {
			// set entity file name
			String entityFileName = new File(fileName).getCanonicalPath();
			entity.setEntityFileName(entityFileName);

			// generate json
			JSONObject jRoot = new JSONObject();

			// re-convert to tm if having a model file
			if (entity.getFileName() != null) {
				String modelPathName = Tools.getPath(entity.getFileName());
				String modelFileName = Tools.getFileName(entity.getFileName()) + (entity.getFileName().endsWith(".tm") == false?".tm":"");
				TMWriter.write(
					entity.getModel(),
					modelPathName,
					modelFileName
				);
				jRoot.put("file", modelPathName + "/" + modelFileName);

				// try to copy thumbnail
				try {
					String thumbnail = modelFileName + ".png";
					jRoot.put("thumbnail", thumbnail);
					copyFile(new File("./tmp", entity.getThumbnail()), new File(Tools.getPath(fileName), thumbnail));
				} catch (IOException ioe) {
					System.out.println("ModelMetaDataFileExport::export(): Could not copy thumbnail for '" + fileName + "'");
				}
			}

			// general data
			jRoot.put("version", "0.11");
			jRoot.put("type", entity.getType());
			jRoot.put("name", entity.getName());
			jRoot.put("descr", entity.getDescription());
			jRoot.put("px", entity.getPivot().getX());
			jRoot.put("py", entity.getPivot().getY());
			jRoot.put("pz", entity.getPivot().getZ());

			// export particle system
			if (entity.getType() == EntityType.PARTICLESYSTEM) {
				LevelEditorEntityParticleSystem particleSystem = entity.getParticleSystem();
				JSONObject jParticleSystem = new JSONObject();

				// type
				jParticleSystem.put("t", particleSystem.getType().toString());
				switch (particleSystem.getType()) {
					case NONE:
						{
							break;
						}
					case OBJECT_PARTICLE_SYSTEM: 
						{
							JSONObject jObjectParticleSystem = new JSONObject();
							// do we have a model file name?
							if (particleSystem.getObjectParticleSystem().getModelFile() != null &&
								particleSystem.getObjectParticleSystem().getModelFile().length() > 0) {
								// yep, convert to .tm
								String modelPathName = Tools.getPath(particleSystem.getObjectParticleSystem().getModelFile());
								String modelFileName = Tools.getFileName(
									particleSystem.getObjectParticleSystem().getModelFile() + 
									(particleSystem.getObjectParticleSystem().getModelFile().endsWith(".tm") == false?".tm":"")
								);
								TMWriter.write(
									particleSystem.getObjectParticleSystem().getModel(),
									modelPathName,
									modelFileName
								);
								// and store to file
								particleSystem.getObjectParticleSystem().setModelFile(modelPathName + "/" + modelFileName);
							}
							jObjectParticleSystem.put("mc", particleSystem.getObjectParticleSystem().getMaxCount());
							jObjectParticleSystem.put("sx", particleSystem.getObjectParticleSystem().getScale().getX());
							jObjectParticleSystem.put("sy", particleSystem.getObjectParticleSystem().getScale().getY());
							jObjectParticleSystem.put("sz", particleSystem.getObjectParticleSystem().getScale().getZ());
							jObjectParticleSystem.put("mf", particleSystem.getObjectParticleSystem().getModelFile());
							jObjectParticleSystem.put("ae", particleSystem.getObjectParticleSystem().isAutoEmit());
							jParticleSystem.put("ops", jObjectParticleSystem);
							break;
						}
					case POINT_PARTICLE_SYSTEM:
						{
							JSONObject jPointParticleSystem = new JSONObject();
							jPointParticleSystem.put("mp", particleSystem.getPointParticleSystem().getMaxPoints());
							jPointParticleSystem.put("ae", particleSystem.getPointParticleSystem().isAutoEmit());
							jParticleSystem.put("pps", jPointParticleSystem);
							break;
						}
					default:
						{
							System.out.println("ModelMetaDataFileExport::export(): unknown particle system type '" + particleSystem.getType() + "'");
							break;
						}
				}

				// emitter
				jParticleSystem.put("e", particleSystem.getEmitter().toString());
				switch (particleSystem.getEmitter()) {
					case NONE:
						{
							break;
						}
					case POINT_PARTICLE_EMITTER:
						{
							JSONObject jPointParticleEmitter = new JSONObject();
							PointParticleEmitter emitter = particleSystem.getPointParticleEmitter();
							jPointParticleEmitter.put("c", emitter.getCount());
							jPointParticleEmitter.put("lt", emitter.getLifeTime());
							jPointParticleEmitter.put("ltrnd", emitter.getLifeTimeRnd());
							jPointParticleEmitter.put("m", emitter.getMass());
							jPointParticleEmitter.put("mrnd", emitter.getMassRnd());
							jPointParticleEmitter.put("px", emitter.getPosition().getX());
							jPointParticleEmitter.put("py", emitter.getPosition().getY());
							jPointParticleEmitter.put("pz", emitter.getPosition().getZ());
							jPointParticleEmitter.put("vx", emitter.getVelocity().getX());
							jPointParticleEmitter.put("vy", emitter.getVelocity().getY());
							jPointParticleEmitter.put("vz", emitter.getVelocity().getZ());
							jPointParticleEmitter.put("vrndx", emitter.getVelocityRnd().getX());
							jPointParticleEmitter.put("vrndy", emitter.getVelocityRnd().getY());
							jPointParticleEmitter.put("vrndz", emitter.getVelocityRnd().getZ());
							jPointParticleEmitter.put("csr", emitter.getColorStart().getRed());
							jPointParticleEmitter.put("csg", emitter.getColorStart().getGreen());
							jPointParticleEmitter.put("csb", emitter.getColorStart().getBlue());
							jPointParticleEmitter.put("csa", emitter.getColorStart().getAlpha());
							jPointParticleEmitter.put("cer", emitter.getColorEnd().getRed());
							jPointParticleEmitter.put("ceg", emitter.getColorEnd().getGreen());
							jPointParticleEmitter.put("ceb", emitter.getColorEnd().getBlue());
							jPointParticleEmitter.put("cea", emitter.getColorEnd().getAlpha());
							jParticleSystem.put("ppe", jPointParticleEmitter);
							break;
						}
					case BOUNDINGBOX_PARTICLE_EMITTER:
						{
							JSONObject jBoundingBoxParticleEmitter = new JSONObject();
							BoundingBoxParticleEmitter emitter = particleSystem.getBoundingBoxParticleEmitters();
							jBoundingBoxParticleEmitter.put("c", emitter.getCount());
							jBoundingBoxParticleEmitter.put("lt", emitter.getLifeTime());
							jBoundingBoxParticleEmitter.put("ltrnd", emitter.getLifeTimeRnd());
							jBoundingBoxParticleEmitter.put("m", emitter.getMass());
							jBoundingBoxParticleEmitter.put("mrnd", emitter.getMassRnd());
							jBoundingBoxParticleEmitter.put("vx", emitter.getVelocity().getX());
							jBoundingBoxParticleEmitter.put("vy", emitter.getVelocity().getY());
							jBoundingBoxParticleEmitter.put("vz", emitter.getVelocity().getZ());
							jBoundingBoxParticleEmitter.put("vrndx", emitter.getVelocityRnd().getX());
							jBoundingBoxParticleEmitter.put("vrndy", emitter.getVelocityRnd().getY());
							jBoundingBoxParticleEmitter.put("vrndz", emitter.getVelocityRnd().getZ());
							jBoundingBoxParticleEmitter.put("csr", emitter.getColorStart().getRed());
							jBoundingBoxParticleEmitter.put("csg", emitter.getColorStart().getGreen());
							jBoundingBoxParticleEmitter.put("csb", emitter.getColorStart().getBlue());
							jBoundingBoxParticleEmitter.put("csa", emitter.getColorStart().getAlpha());
							jBoundingBoxParticleEmitter.put("cer", emitter.getColorEnd().getRed());
							jBoundingBoxParticleEmitter.put("ceg", emitter.getColorEnd().getGreen());
							jBoundingBoxParticleEmitter.put("ceb", emitter.getColorEnd().getBlue());
							jBoundingBoxParticleEmitter.put("cea", emitter.getColorEnd().getAlpha());
							jBoundingBoxParticleEmitter.put("ocx", emitter.getObbCenter().getX());
							jBoundingBoxParticleEmitter.put("ocy", emitter.getObbCenter().getY());
							jBoundingBoxParticleEmitter.put("ocz", emitter.getObbCenter().getZ());
							jBoundingBoxParticleEmitter.put("ohex", emitter.getObbHalfextension().getX());
							jBoundingBoxParticleEmitter.put("ohey", emitter.getObbHalfextension().getY());
							jBoundingBoxParticleEmitter.put("ohez", emitter.getObbHalfextension().getZ());
							jBoundingBoxParticleEmitter.put("oa0x", emitter.getObbAxis0().getX());
							jBoundingBoxParticleEmitter.put("oa0y", emitter.getObbAxis0().getY());
							jBoundingBoxParticleEmitter.put("oa0z", emitter.getObbAxis0().getZ());
							jBoundingBoxParticleEmitter.put("oa1x", emitter.getObbAxis1().getX());
							jBoundingBoxParticleEmitter.put("oa1y", emitter.getObbAxis1().getY());
							jBoundingBoxParticleEmitter.put("oa1z", emitter.getObbAxis1().getZ());
							jBoundingBoxParticleEmitter.put("oa2x", emitter.getObbAxis2().getX());
							jBoundingBoxParticleEmitter.put("oa2y", emitter.getObbAxis2().getY());
							jBoundingBoxParticleEmitter.put("oa2z", emitter.getObbAxis2().getZ());
							jParticleSystem.put("bbpe", jBoundingBoxParticleEmitter);
							break;
						}
					case CIRCLE_PARTICLE_EMITTER:
						{
							JSONObject jCircleParticleEmitter = new JSONObject();
							CircleParticleEmitter emitter = particleSystem.getCircleParticleEmitter();
							jCircleParticleEmitter.put("c", emitter.getCount());
							jCircleParticleEmitter.put("lt", emitter.getLifeTime());
							jCircleParticleEmitter.put("ltrnd", emitter.getLifeTimeRnd());
							jCircleParticleEmitter.put("m", emitter.getMass());
							jCircleParticleEmitter.put("mrnd", emitter.getMassRnd());
							jCircleParticleEmitter.put("vx", emitter.getVelocity().getX());
							jCircleParticleEmitter.put("vy", emitter.getVelocity().getY());
							jCircleParticleEmitter.put("vz", emitter.getVelocity().getZ());
							jCircleParticleEmitter.put("vrndx", emitter.getVelocityRnd().getX());
							jCircleParticleEmitter.put("vrndy", emitter.getVelocityRnd().getY());
							jCircleParticleEmitter.put("vrndz", emitter.getVelocityRnd().getZ());
							jCircleParticleEmitter.put("csr", emitter.getColorStart().getRed());
							jCircleParticleEmitter.put("csg", emitter.getColorStart().getGreen());
							jCircleParticleEmitter.put("csb", emitter.getColorStart().getBlue());
							jCircleParticleEmitter.put("csa", emitter.getColorStart().getAlpha());
							jCircleParticleEmitter.put("cer", emitter.getColorEnd().getRed());
							jCircleParticleEmitter.put("ceg", emitter.getColorEnd().getGreen());
							jCircleParticleEmitter.put("ceb", emitter.getColorEnd().getBlue());
							jCircleParticleEmitter.put("cea", emitter.getColorEnd().getAlpha());
							jCircleParticleEmitter.put("cx", emitter.getCenter().getX());
							jCircleParticleEmitter.put("cy", emitter.getCenter().getY());
							jCircleParticleEmitter.put("cz", emitter.getCenter().getZ());
							jCircleParticleEmitter.put("r", emitter.getRadius());
							jCircleParticleEmitter.put("a0x", emitter.getAxis0().getX());
							jCircleParticleEmitter.put("a0y", emitter.getAxis0().getY());
							jCircleParticleEmitter.put("a0z", emitter.getAxis0().getZ());
							jCircleParticleEmitter.put("a1x", emitter.getAxis1().getX());
							jCircleParticleEmitter.put("a1y", emitter.getAxis1().getY());
							jCircleParticleEmitter.put("a1z", emitter.getAxis1().getZ());
							jParticleSystem.put("cpe", jCircleParticleEmitter);
							break;
						}
					case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY: 
						{
							JSONObject jCircleParticleEmitterPlaneVelocity = new JSONObject();
							CircleParticleEmitterPlaneVelocity emitter = particleSystem.getCircleParticleEmitterPlaneVelocity();
							jCircleParticleEmitterPlaneVelocity.put("c", emitter.getCount());
							jCircleParticleEmitterPlaneVelocity.put("lt", emitter.getLifeTime());
							jCircleParticleEmitterPlaneVelocity.put("ltrnd", emitter.getLifeTimeRnd());
							jCircleParticleEmitterPlaneVelocity.put("m", emitter.getMass());
							jCircleParticleEmitterPlaneVelocity.put("mrnd", emitter.getMassRnd());
							jCircleParticleEmitterPlaneVelocity.put("v", emitter.getVelocity());
							jCircleParticleEmitterPlaneVelocity.put("vrnd", emitter.getVelocityRnd());
							jCircleParticleEmitterPlaneVelocity.put("csr", emitter.getColorStart().getRed());
							jCircleParticleEmitterPlaneVelocity.put("csg", emitter.getColorStart().getGreen());
							jCircleParticleEmitterPlaneVelocity.put("csb", emitter.getColorStart().getBlue());
							jCircleParticleEmitterPlaneVelocity.put("csa", emitter.getColorStart().getAlpha());
							jCircleParticleEmitterPlaneVelocity.put("cer", emitter.getColorEnd().getRed());
							jCircleParticleEmitterPlaneVelocity.put("ceg", emitter.getColorEnd().getGreen());
							jCircleParticleEmitterPlaneVelocity.put("ceb", emitter.getColorEnd().getBlue());
							jCircleParticleEmitterPlaneVelocity.put("cea", emitter.getColorEnd().getAlpha());
							jCircleParticleEmitterPlaneVelocity.put("cx", emitter.getCenter().getX());
							jCircleParticleEmitterPlaneVelocity.put("cy", emitter.getCenter().getY());
							jCircleParticleEmitterPlaneVelocity.put("cz", emitter.getCenter().getZ());
							jCircleParticleEmitterPlaneVelocity.put("r", emitter.getRadius());
							jCircleParticleEmitterPlaneVelocity.put("a0x", emitter.getAxis0().getX());
							jCircleParticleEmitterPlaneVelocity.put("a0y", emitter.getAxis0().getY());
							jCircleParticleEmitterPlaneVelocity.put("a0z", emitter.getAxis0().getZ());
							jCircleParticleEmitterPlaneVelocity.put("a1x", emitter.getAxis1().getX());
							jCircleParticleEmitterPlaneVelocity.put("a1y", emitter.getAxis1().getY());
							jCircleParticleEmitterPlaneVelocity.put("a1z", emitter.getAxis1().getZ());
							jParticleSystem.put("cpeev", jCircleParticleEmitterPlaneVelocity);
							break;
						}
					case SPHERE_PARTICLE_EMITTER:
						{
							JSONObject jSphereParticleEmitter = new JSONObject();
							SphereParticleEmitter emitter = particleSystem.getSphereParticleEmitter();
							jSphereParticleEmitter.put("c", emitter.getCount());
							jSphereParticleEmitter.put("lt", emitter.getLifeTime());
							jSphereParticleEmitter.put("ltrnd", emitter.getLifeTimeRnd());
							jSphereParticleEmitter.put("m", emitter.getMass());
							jSphereParticleEmitter.put("mrnd", emitter.getMassRnd());
							jSphereParticleEmitter.put("vx", emitter.getVelocity().getX());
							jSphereParticleEmitter.put("vy", emitter.getVelocity().getY());
							jSphereParticleEmitter.put("vz", emitter.getVelocity().getZ());
							jSphereParticleEmitter.put("vrndx", emitter.getVelocityRnd().getX());
							jSphereParticleEmitter.put("vrndy", emitter.getVelocityRnd().getY());
							jSphereParticleEmitter.put("vrndz", emitter.getVelocityRnd().getZ());
							jSphereParticleEmitter.put("csr", emitter.getColorStart().getRed());
							jSphereParticleEmitter.put("csg", emitter.getColorStart().getGreen());
							jSphereParticleEmitter.put("csb", emitter.getColorStart().getBlue());
							jSphereParticleEmitter.put("csa", emitter.getColorStart().getAlpha());
							jSphereParticleEmitter.put("cer", emitter.getColorEnd().getRed());
							jSphereParticleEmitter.put("ceg", emitter.getColorEnd().getGreen());
							jSphereParticleEmitter.put("ceb", emitter.getColorEnd().getBlue());
							jSphereParticleEmitter.put("cea", emitter.getColorEnd().getAlpha());
							jSphereParticleEmitter.put("cx", emitter.getCenter().getX());
							jSphereParticleEmitter.put("cy", emitter.getCenter().getY());
							jSphereParticleEmitter.put("cz", emitter.getCenter().getZ());
							jSphereParticleEmitter.put("r", emitter.getRadius());
							jParticleSystem.put("spe", jSphereParticleEmitter);
							break;
						}
					default:
						System.out.println("ModelMetaDataFileExport::export(): unknown particle system emitter '" + particleSystem.getEmitter() + "'");
				}

				// add to file
				jRoot.put("ps", jParticleSystem);
			}

			// bounding volume
			JSONArray jBoundingVolumes = new JSONArray();
			for (int i = 0; i < entity.getBoundingVolumeCount(); i++) {
				LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(i);
				BoundingVolume bv = entityBoundingVolume.getBoundingVolume();
				if (bv == null) continue;
				JSONObject jBoundingVolume = new JSONObject();
				if (bv == null) {
					jBoundingVolume.put("type", "none");
					jBoundingVolumes.put(jBoundingVolume);
				} else
				if (bv instanceof Sphere) {
					Sphere sphere = (Sphere)bv;
					jBoundingVolume.put("type", "sphere");
					jBoundingVolume.put("cx", sphere.getCenter().getX());
					jBoundingVolume.put("cy", sphere.getCenter().getY());
					jBoundingVolume.put("cz", sphere.getCenter().getZ());
					jBoundingVolume.put("r", sphere.getRadius());
					jBoundingVolumes.put(jBoundingVolume);
				} else
				if (bv instanceof Capsule) {
					Capsule capsule = (Capsule)bv;
					jBoundingVolume.put("type", "capsule");
					jBoundingVolume.put("ax", capsule.getA().getX());
					jBoundingVolume.put("ay", capsule.getA().getY());
					jBoundingVolume.put("az", capsule.getA().getZ());
					jBoundingVolume.put("bx", capsule.getB().getX());
					jBoundingVolume.put("by", capsule.getB().getY());
					jBoundingVolume.put("bz", capsule.getB().getZ());
					jBoundingVolume.put("r", capsule.getRadius());
					jBoundingVolumes.put(jBoundingVolume);
				} else
				if (bv instanceof BoundingBox) {
					BoundingBox aabb = (BoundingBox)bv;
					jBoundingVolume.put("type", "aabb");
					jBoundingVolume.put("mix", aabb.getMin().getX());
					jBoundingVolume.put("miy", aabb.getMin().getY());
					jBoundingVolume.put("miz", aabb.getMin().getZ());
					jBoundingVolume.put("max", aabb.getMax().getX());
					jBoundingVolume.put("may", aabb.getMax().getY());
					jBoundingVolume.put("maz", aabb.getMax().getZ());
					jBoundingVolumes.put(jBoundingVolume);
				} else
				if (bv instanceof OrientedBoundingBox) {
					OrientedBoundingBox obb = (OrientedBoundingBox)bv;
					jBoundingVolume.put("type", "obb");
					jBoundingVolume.put("cx", obb.getCenter().getX());
					jBoundingVolume.put("cy", obb.getCenter().getY());
					jBoundingVolume.put("cz", obb.getCenter().getZ());
					jBoundingVolume.put("a0x", obb.getAxes()[0].getX());
					jBoundingVolume.put("a0y", obb.getAxes()[0].getY());
					jBoundingVolume.put("a0z", obb.getAxes()[0].getZ());
					jBoundingVolume.put("a1x", obb.getAxes()[1].getX());
					jBoundingVolume.put("a1y", obb.getAxes()[1].getY());
					jBoundingVolume.put("a1z", obb.getAxes()[1].getZ());
					jBoundingVolume.put("a2x", obb.getAxes()[2].getX());
					jBoundingVolume.put("a2y", obb.getAxes()[2].getY());
					jBoundingVolume.put("a2z", obb.getAxes()[2].getZ());
					jBoundingVolume.put("hex", obb.getHalfExtension().getX());
					jBoundingVolume.put("hey", obb.getHalfExtension().getY());
					jBoundingVolume.put("hez", obb.getHalfExtension().getZ());
					jBoundingVolumes.put(jBoundingVolume);
				} else
				if (bv instanceof ConvexMesh) {
					ConvexMesh mesh = (ConvexMesh)bv;
					jBoundingVolume.put("type", "convexmesh");
					jBoundingVolume.put("file", entityBoundingVolume.getModelMeshFile());
					JSONArray jMeshTriangles = new JSONArray();
					int triangleIdx = 0;
					for (Triangle triangle: mesh.getTriangles()) {
						JSONArray jMeshTriangleVertices = new JSONArray();
						int vertexIdx = 0;
						for (Vector3 vertex: triangle.getVertices()) {
							JSONArray jMeshTriangleVertex = new JSONArray();
							for (int vcIdx = 0; vcIdx < 3; vcIdx++) {
								jMeshTriangleVertex.put(vcIdx, vertex.getArray()[vcIdx]);
							}
							jMeshTriangleVertices.put(vertexIdx++, jMeshTriangleVertex);
						}
						jMeshTriangles.put(triangleIdx++, jMeshTriangleVertices);
					}
					jBoundingVolume.put("t", jMeshTriangles);
					jBoundingVolumes.put(jBoundingVolume);
				}
			}
			jRoot.put("bvs", jBoundingVolumes);

			// model properties
			JSONArray jModelProperties = new JSONArray();
			for (PropertyModelClass modelProperty: entity.getProperties()) {
				JSONObject jObjectProperty = new JSONObject();
				jObjectProperty.put("name", modelProperty.getName());
				jObjectProperty.put("value", modelProperty.getValue());
				jModelProperties.put(jObjectProperty);					
			}
			jRoot.put("properties", jModelProperties);

			// save to file
			fos = new FileOutputStream(new File(entity.getEntityFileName())); 
	        fops = new PrintStream(fos);
	        fops.print(jRoot.toString(2));  
		} catch (JSONException je) {
			throw je;
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (fops != null) fops.close();
			if (fos != null) try { fos.close(); } catch (IOException ioe) {
				throw ioe;
			}
		}
	}

}

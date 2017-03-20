package net.drewke.tdme.tools.shared.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.primitives.Triangle;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;

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
			try { is.close(); } catch (IOException ioe) {}
			try { os.close(); } catch (IOException ioe) {}
		}
	}

	/**
	 * Exports a level to a TDME level file
	 * @param file name
	 */
	public static void export(String fileName, LevelEditorModel model) throws Exception {
		FileOutputStream fos = null;
	    PrintStream fops = null;
		try {
			// generate json
			JSONObject jRoot = new JSONObject();

			// general data
			jRoot.put("version", "0.11");
			jRoot.put("type", model.getType());
			jRoot.put("name", model.getName());
			jRoot.put("descr", model.getDescription());
			jRoot.put("px", model.getPivot().getX());
			jRoot.put("py", model.getPivot().getY());
			jRoot.put("pz", model.getPivot().getZ());
			jRoot.put("file", new File(model.getFileName()).getName());

			String thumbnail = new File(model.getFileName()).getName() + ".png";
			jRoot.put("thumbnail", thumbnail);
			copyFile(new File("./tmp", model.getThumbnail()), new File(new File(fileName).getAbsoluteFile().getParent(), thumbnail));

			// bounding volume
			BoundingVolume bv = model.getBoundingVolume();
			JSONObject jBoundingVolume = new JSONObject();
			if (bv == null) {
				jBoundingVolume.put("type", "none");
				jRoot.put("bv", jBoundingVolume);
			} else
			if (bv instanceof Sphere) {
				Sphere sphere = (Sphere)bv;
				jBoundingVolume.put("type", "sphere");
				jBoundingVolume.put("cx", sphere.getCenter().getX());
				jBoundingVolume.put("cy", sphere.getCenter().getY());
				jBoundingVolume.put("cz", sphere.getCenter().getZ());
				jBoundingVolume.put("r", sphere.getRadius());
				jRoot.put("bv", jBoundingVolume);
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
				jRoot.put("bv", jBoundingVolume);
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
				jRoot.put("bv", jBoundingVolume);
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
				jRoot.put("bv", jBoundingVolume);
			} else
			if (bv instanceof ConvexMesh) {
				ConvexMesh mesh = (ConvexMesh)bv;
				jBoundingVolume.put("type", "convexmesh");
				jBoundingVolume.put("file", model.getBoundingModelMeshFile());
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
				jRoot.put("bv", jBoundingVolume);
			}

			// model properties
			JSONArray jModelProperties = new JSONArray();
			for (PropertyModelClass modelProperty: model.getProperties()) {
				JSONObject jObjectProperty = new JSONObject();
				jObjectProperty.put("name", modelProperty.getName());
				jObjectProperty.put("value", modelProperty.getValue());
				jModelProperties.put(jObjectProperty);					
			}
			jRoot.put("properties", jModelProperties);

			// save to file
			fos = new FileOutputStream(new File(fileName)); 
	        fops = new PrintStream(fos);
	        fops.print(jRoot.toString(2));  
		} catch (JSONException je) {
		} catch (IOException ioe) {
		} finally {
			if (fops != null) fops.close();
			if (fos != null) try { fos.close(); } catch (IOException ioe) {}
		}
	}

}
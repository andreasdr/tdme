package net.drewke.tdme.engine.fileio.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Model.UpVector;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * Wavefront object reader
 * @author andreas.drewke
 * @version $Id$
 */
public final class WFObjReader {

	/**
	 * Reads a wave front object file
	 * @param path name
	 * @param file name
	 * @return model
	 * @throws IOException
	 * @throws ModelIOException
	 */
	public static Model read(String pathName, String fileName) throws IOException, ModelFileIOException {
		// create object
		Model model = new Model(pathName + File.separator + fileName, fileName, UpVector.Y_UP, RotationOrder.XYZ, null);	

		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();

		HashMap<String, Material> materials = model.getMaterials();
		HashMap<String, Group> subGroups = model.getSubGroups();
		HashMap<String, Group> groups = model.getGroups();

		// current group
		Group group = null;

		// model vertices -> group vertices mapping
		HashMap<Integer, Integer> modelGroupVerticesMapping = null;
		// model texture coordinates -> group texture coordinates mapping
		HashMap<Integer, Integer> modelGroupTextureCoordinatesMapping = null;

		// current group data
		ArrayList<Face> groupFacesEntityFaces = null;
		ArrayList<Vector3> groupVertices = null;
		ArrayList<Vector3> groupNormals = null;
		ArrayList<TextureCoordinate> groupTextureCoordinates = null;

		// current group's faces entity
		ArrayList<FacesEntity> groupFacesEntities = null;
		FacesEntity groupFacesEntity = null;

		//
		DataInputStream inputStream = new DataInputStream(FileSystem.getInstance().getInputStream(pathName, fileName));
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		//
		try {
			//
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// skip on comments
				if (line.startsWith("#")) {
					continue;
				}

				// determine index of first ' ' which will separate command from arguments
				int commandEndIdx = line.indexOf(' ');
				if (commandEndIdx == -1) commandEndIdx = line.length();

				// determine command
				String command = line.substring(0, commandEndIdx).trim().toLowerCase();

				// determine arguments if any exist
				String arguments = command.length() + 1 > line.length()?"":line.substring(command.length() + 1);

				// parse
				if (command.equals("mtllib")) {
					String materialFileName = arguments;
					materials = WFObjReader.readMaterials(pathName, materialFileName);
				} else
				if (command.equals("v")) {
					StringTokenizer t = new StringTokenizer(arguments, " ");
					float x = Float.parseFloat(t.nextToken());
					float y = Float.parseFloat(t.nextToken());
					float z = Float.parseFloat(t.nextToken());
					// add vertex
					vertices.add(new Vector3(x, y, z));
				} else
				if (command.equals("vt")) {
					StringTokenizer t = new StringTokenizer(arguments, " ");
					float u = Float.parseFloat(t.nextToken());
					float v = Float.parseFloat(t.nextToken());
					textureCoordinates.add(new TextureCoordinate(u, v));
				} else
				if (command.equals("f")) {
					StringTokenizer t2;
					StringTokenizer t = new StringTokenizer(arguments, " ");
					int v0 = -1;
					int v1 = -1;
					int v2 = -1;
					int vt0 = -1;
					int vt1 = -1;
					int vt2 = -1;

					// parse vertex index 0, vertex texture index 0
					t2 = new StringTokenizer(t.nextToken(), "/");
					v0 = Integer.parseInt(t2.nextToken()) - 1;
					if (t2.hasMoreTokens()) {
						vt0 = Integer.parseInt(t2.nextToken()) - 1;
					}
					// parse vertex index 1, vertex texture index 1
					t2 = new StringTokenizer(t.nextToken(), "/");
					v1 = Integer.parseInt(t2.nextToken()) - 1;
					if (t2.hasMoreTokens()) {
						vt1 = Integer.parseInt(t2.nextToken()) - 1;
					}
					// parse vertex index 2, vertex texture index 2
					t2 = new StringTokenizer(t.nextToken(), "/");
					v2 = Integer.parseInt(t2.nextToken()) - 1;
					if (t2.hasMoreTokens()) {
						vt2 = Integer.parseInt(t2.nextToken()) - 1;
					}
	
					// check if triangulated
					if (t.hasMoreTokens()) {
						throw new ModelFileIOException("We only support triangulated meshes");
					}

					Integer mappedVertex = null;

					// map v0 to group
					mappedVertex = modelGroupVerticesMapping.get(v0);
					if (mappedVertex == null) {
						groupVertices.add(vertices.get(v0).clone());
						v0 = groupVertices.size() - 1;
					} else {
						v0 = mappedVertex.intValue();
					}

					// map v1 to group
					mappedVertex = modelGroupVerticesMapping.get(v1);
					if (mappedVertex == null) {
						groupVertices.add(vertices.get(v1).clone());
						v1 = groupVertices.size() - 1;
					} else {
						v1 = mappedVertex.intValue();
					}

					// map v2 to group
					mappedVertex = modelGroupVerticesMapping.get(v2);
					if (mappedVertex == null) {
						groupVertices.add(vertices.get(v2).clone());
						v2 = groupVertices.size() - 1;
					} else {
						v2 = mappedVertex.intValue();
					}

					//
					Integer mappedTextureCoordinate = null;

					// map vt0 to group
					mappedTextureCoordinate = modelGroupTextureCoordinatesMapping.get(vt0);
					if (mappedTextureCoordinate == null) {
						groupTextureCoordinates.add(textureCoordinates.get(vt0).clone());
						vt0 = groupTextureCoordinates.size() - 1;
					} else {
						vt0 = mappedTextureCoordinate.intValue();
					}

					// map vt1 to group
					mappedTextureCoordinate = modelGroupTextureCoordinatesMapping.get(vt1);
					if (mappedTextureCoordinate == null) {
						groupTextureCoordinates.add(textureCoordinates.get(vt1).clone());
						vt1 = groupTextureCoordinates.size() - 1;
					} else {
						vt1 = mappedTextureCoordinate.intValue();
					}

					// map vt2 to group
					mappedTextureCoordinate = modelGroupTextureCoordinatesMapping.get(vt2);
					if (mappedTextureCoordinate == null) {
						groupTextureCoordinates.add(textureCoordinates.get(vt2).clone());
						vt2 = groupTextureCoordinates.size() - 1;
					} else {
						vt2 = mappedTextureCoordinate.intValue();
					}

					// compute vertex normal
					Vector3[] faceVertexNormals = ModelHelper.computeNormals(new Vector3[] {groupVertices.get(v0), groupVertices.get(v1), groupVertices.get(v2)});					

					// store group normals
					int n0 = groupNormals.size();
					groupNormals.add(faceVertexNormals[0]);
					int n1 = groupNormals.size();
					groupNormals.add(faceVertexNormals[1]);
					int n2 = groupNormals.size();
					groupNormals.add(faceVertexNormals[2]);

					// create face with vertex indices
					//	we only support triangulated faces
					Face face = new Face(group, v0, v1, v2, n0, n1, n2);
					if (vt0 != -1 && vt1 != -1 && vt2 != -1) {
						// set optional texture coordinate index
						face.setTextureCoordinateIndices(vt0, vt1, vt2);
					}
					groupFacesEntityFaces.add(face);
				} else
				if (command.equals("g")) {
					if (group != null) {
						// current faces entity
						if (groupFacesEntityFaces.isEmpty() == false) {
							groupFacesEntity.setFaces(groupFacesEntityFaces);
							groupFacesEntities.add(groupFacesEntity);
						}

						// group
						group.setVertices(groupVertices);
						group.setNormals(groupNormals);
						group.setTextureCoordinates(groupTextureCoordinates);
						group.setFacesEntities(groupFacesEntities);
						group.determineFeatures();
					}
					StringTokenizer t = new StringTokenizer(arguments, " ");
					String name = t.nextToken();
					groupVertices = new ArrayList<Vector3>();
					groupNormals = new ArrayList<Vector3>();
					groupTextureCoordinates = new ArrayList<TextureCoordinate>();
					groupFacesEntityFaces = new ArrayList<Face>();
					group = new Group(
						model,
						name,
						name
					);
					groupFacesEntity = new FacesEntity(group, name);
					groupFacesEntities = new ArrayList<FacesEntity>();
					modelGroupVerticesMapping = new HashMap<Integer, Integer>();
					modelGroupTextureCoordinatesMapping = new HashMap<Integer, Integer>();
					subGroups.put(name, group);
					groups.put(name, group);
				} else
				if (command.equals("usemtl")) {
					if (group != null) {
						// current faces entity
						if (groupFacesEntityFaces.isEmpty() == false) {
							groupFacesEntity.setFaces(groupFacesEntityFaces);
							groupFacesEntities.add(groupFacesEntity);
						}

						// set up new one
						groupFacesEntity = new FacesEntity(group, "#" + groupFacesEntities.size());
						groupFacesEntityFaces = new ArrayList<Face>();
					}
					groupFacesEntity.setMaterial(materials.get(arguments));
				} else {
					// not supported
				}
	
			}
	
			// finish last group
			if (group != null) {
				// current faces entity
				if (groupFacesEntityFaces.isEmpty() == false) {
					groupFacesEntity.setFaces(groupFacesEntityFaces);
					groupFacesEntities.add(groupFacesEntity);
				}

				// group
				group.setVertices(groupVertices);
				group.setNormals(groupNormals);
				group.setTextureCoordinates(groupTextureCoordinates);
				group.setFacesEntities(groupFacesEntities);
				group.determineFeatures();
			}
		} finally {
			// close resouces
			reader.close();
			inputStream.close();
		}

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Reads a wavefront object material library
	 * @param path name
	 * @param file name
	 * @return
	 */
	private static HashMap<String, Material> readMaterials(String pathName, String fileName) throws IOException {
		HashMap<String, Material> materials = new HashMap<String, Material>();
		Material current = null;
		//
		DataInputStream inputStream = new DataInputStream(FileSystem.getInstance().getInputStream(pathName, fileName));
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		float alpha = 1.0f;
		while ((line = reader.readLine()) != null) {
			line = line.trim();

			// skip on comments
			if (line.startsWith("#")) {
				continue;
			}

			// determine index of first ' ' which will separate command from arguments
			int commandEndIdx = line.indexOf(' ');
			if (commandEndIdx == -1) commandEndIdx = line.length();

			// determine command
			String command = line.substring(0, commandEndIdx).trim().toLowerCase();

			// determine arguments if any exist
			String arguments = command.length() + 1 > line.length()?"":line.substring(command.length() + 1);

			// parse
			if (command.equals("newmtl")) {
				String name = arguments;
				current = new Material(name);
				materials.put(name, current);
			} else
			if (command.equals("map_ka")) {
				current.setDiffuseTexture(pathName, arguments);
			} else
			if (command.equals("map_kd")) {
				current.setDiffuseTexture(pathName, arguments);
			} else
			if (command.equals("ka")) {
				StringTokenizer t = new StringTokenizer(arguments, " ");
				current.getAmbientColor().set(
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					alpha
				);
			} else
			if (command.equals("kd")) {
				StringTokenizer t = new StringTokenizer(arguments, " ");
				current.getDiffuseColor().set(
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					alpha
				);
			} else
			if (command.equals("ks")) {
				StringTokenizer t = new StringTokenizer(arguments, " ");
				current.getSpecularColor().set(
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					Float.parseFloat(t.nextToken()),
					alpha
				);
			} else
			if (command.equals("tr")) {
				alpha = Float.parseFloat(arguments);
				current.getAmbientColor().setAlpha(alpha);
				current.getDiffuseColor().setAlpha(alpha);
				current.getSpecularColor().setAlpha(alpha);
				current.getEmissionColor().setAlpha(alpha);
			} else
			if (command.equals("d")) {
				alpha = Float.parseFloat(arguments);
				current.getAmbientColor().setAlpha(alpha);
				current.getDiffuseColor().setAlpha(alpha);
				current.getSpecularColor().setAlpha(alpha);
				current.getEmissionColor().setAlpha(alpha);
			}
		}

		// Close the input stream
		inputStream.close();

		//
		return materials;
	}

}

package net.drewke.tdme.engine.model;

import java.util.ArrayList;

import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector2;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;

/**
 * Model Helper
 * @author Andreas Drewke
 */
public final class ModelHelper {

	public enum VertexOrder {CLOCKWISE, COUNTERCLOCKWISE};

	/**
	 * Determines vertex order of face
	 * @param vertices
	 * @return vertex order
	 */
	public static VertexOrder determineVertexOrder(Vector3[] vertices) {
		int edgeSum = 0;
		for (int i = 0; i < vertices.length; i++) {
			float[] currentVertexXYZ = vertices[i].getArray();
			float[] nextVertexXYZ = vertices[(i + 1) % vertices.length].getArray();
			edgeSum+=
				(nextVertexXYZ[0] - currentVertexXYZ[0]) *
				(nextVertexXYZ[1] - currentVertexXYZ[1]) *
				(nextVertexXYZ[2] - currentVertexXYZ[0]);
		}
		if (edgeSum >= 0) {
			return VertexOrder.CLOCKWISE;
		} else {
			return VertexOrder.COUNTERCLOCKWISE;
		}
	}

	/**
	 * Computes face normal for given face vertices
	 * @param face vertices
	 * @return face normal
	 */
	public static Vector3 computeNormal(Vector3[] vertices) {
		// face normal
		return Vector3.computeCrossProduct(
			vertices[1].clone().sub(vertices[0]),
			vertices[2].clone().sub(vertices[0])
		).normalize();
	}

	/**
	 * Computes face normals for given face vertices
	 * 	these normals will not be smooth
	 * @param face vertices
	 * @return face normals
	 */
	public static Vector3[] computeNormals(Vector3[] vertices) {
		// face normal
		Vector3 normal = computeNormal(vertices);

		// compute vertex normal
		Vector3[] normals = new Vector3[3];
		for (int i = 0; i < vertices.length; i++) {
			// TODO: vertex normals for smooth shading is currently broken
			//			but its only used for primitives models and wf obj parser
			normals[i] = normal.clone(); // vertices[i].clone().add(normal).normalize();
		}

		//
		return normals;
	}

	/**
	 * Create normal tangents and bitangents for groups with normal mapping
	 * @see http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-13-normal-mapping/
	 * @param group
	 */
	public static void createNormalTangentsAndBitangents(Group group) {
		// what we need
		ArrayList<Vector3> tangentsArrayList = new ArrayList<Vector3>();
		ArrayList<Vector3> bitangentsArrayList = new ArrayList<Vector3>();

		// temporary variables
		Vector2 uv0 = new Vector2();
		Vector2 uv1 = new Vector2();
		Vector2 uv2 = new Vector2();
		Vector3 deltaPos1 = new Vector3();
		Vector3 deltaPos2 = new Vector3();
		Vector2 deltaUV1 = new Vector2();
		Vector2 deltaUV2 = new Vector2();
		Vector3 tmpVector3 = new Vector3();

		// create it
		Vector3[] vertices = group.getVertices();
		Vector3[] normals = group.getNormals();
		TextureCoordinate[] textureCoordinates = group.getTextureCoordinates();
		for(FacesEntity faceEntity: group.getFacesEntities())
		if (faceEntity.getMaterial() != null && 
			faceEntity.getMaterial().hasNormalTexture() == true) {
			for (Face face: faceEntity.getFaces()) {
				// Shortcuts for vertices
				int[] verticesIndexes = face.getVertexIndices();
				Vector3 v0 = vertices[verticesIndexes[0]];
				Vector3 v1 = vertices[verticesIndexes[1]];
				Vector3 v2 = vertices[verticesIndexes[2]];

				// shortcuts for UVs
				int[] textureCoordinatesIndexes = face.getTextureCoordinateIndices();
				uv0.set(textureCoordinates[textureCoordinatesIndexes[0]].getArray());
				uv0.setY(1f - uv0.getY());
				uv1.set(textureCoordinates[textureCoordinatesIndexes[1]].getArray());
				uv1.setY(1f - uv1.getY());
				uv2.set(textureCoordinates[textureCoordinatesIndexes[2]].getArray());
				uv2.setY(1f - uv2.getY());

				// edges of the triangle : position delta
				deltaPos1.set(v1).sub(v0);
				deltaPos2.set(v2).sub(v0);

				// UV delta
				deltaUV1.set(uv1).sub(uv0);
				deltaUV2.set(uv2).sub(uv0);

				// compute tangent and bitangent
				float r = 1.0f / (deltaUV1.getX() * deltaUV2.getY() - deltaUV1.getY() * deltaUV2.getX());
				Vector3 tangent = 
					new Vector3(deltaPos1).
					scale(deltaUV2.getY()).
					sub(
						tmpVector3.set(deltaPos2).
						scale(deltaUV1.getY())
					).
					scale(r);
				Vector3 bitangent =
					new Vector3(deltaPos2).
					scale(deltaUV1.getX()).
					sub(
						tmpVector3.set(deltaPos1).
						scale(deltaUV2.getX())
					).
					scale(r);

				// set up tangent face indices
				face.setTangentIndices(
					tangentsArrayList.size() + 0,
					tangentsArrayList.size() + 1,
					tangentsArrayList.size() + 2
				);

				// set up bitangent face indices
				face.setBitangentIndices(
					bitangentsArrayList.size() + 0,
					bitangentsArrayList.size() + 1,
					bitangentsArrayList.size() + 2
				);

				// add to group tangents, bitangents
				tangentsArrayList.add(tangent);
				tangentsArrayList.add(tangent);
				tangentsArrayList.add(tangent);
				bitangentsArrayList.add(bitangent);
				bitangentsArrayList.add(bitangent);
				bitangentsArrayList.add(bitangent);
			}
		}
		
		// set up tangents and bitangents if we have any
		if (tangentsArrayList.size() > 0 && 
			bitangentsArrayList.size() > 0) {
			group.setTangents(tangentsArrayList);
			group.setBitangents(bitangentsArrayList);

			// going further
			Vector3[] tangents = group.getTangents();
			Vector3[] bitangents = group.getBitangents();
			for(FacesEntity faceEntity: group.getFacesEntities())
			if (faceEntity.getMaterial() != null && 
				faceEntity.getMaterial().hasNormalTexture() == true) {
				for (Face face: faceEntity.getFaces())
				for (int i = 0; i < 3; i++) {
					Vector3 normal = normals[face.getNormalIndices()[i]];
					Vector3 tangent = tangents[face.getTangentIndices()[i]];
					Vector3 bitangent = bitangents[face.getBitangentIndices()[i]];
					tangent.sub(
						tmpVector3.set(normal).
						scale(
							Vector3.computeDotProduct(normal, tangent)
						)
					).normalize();
					if (Vector3.computeDotProduct(Vector3.computeCrossProduct(normal, tangent, tmpVector3), bitangent) < 0f) {
						tangent.scale(-1f);
					}
					bitangent.normalize();
				}
			}
		}
	}

	/**
	 * Prepare for indexed rendering
	 * @param model
	 */
	public static void prepareForIndexedRendering(Model model) {
		prepareForIndexedRendering(model.getSubGroups());
	}

	/**
	 * Prepares this group for indexed rendering
	 * @param groups
	 */
	private static void prepareForIndexedRendering(HashMap<String, Group> groups) {
		// we need to prepare the group for indexed rendering
		for (Group group: groups.getValuesIterator()) {
			Vector3[] groupVertices = group.getVertices();
			Vector3[] groupNormals = group.getNormals();
			TextureCoordinate[] groupTextureCoordinates = group.getTextureCoordinates();
			Vector3[] groupTangents = group.getTangents();
			Vector3[] groupBitangents = group.getBitangents();
			int groupFaceCount = group.getFaceCount();
			int[] verticeMapping = new int[groupFaceCount * 3];
			Vector3[] indexedVertices = new Vector3[groupFaceCount * 3];
			Vector3[] indexedNormals = new Vector3[groupFaceCount * 3];
			TextureCoordinate[] indexedTextureCoordinates = groupTextureCoordinates != null?new TextureCoordinate[groupFaceCount * 3]:null;
			Vector3[] indexedTangents = groupTangents != null?new Vector3[groupFaceCount * 3]:null;
			Vector3[] indexedBitangents = groupBitangents != null?new Vector3[groupFaceCount * 3]:null;

			// construct indexed vertex data suitable for GL
			int preparedIndices = 0;
			for (FacesEntity facesEntity: group.getFacesEntities()) {
				for (Face face: facesEntity.getFaces()) {
					int[] faceVertexIndices = face.getVertexIndices();
					int[] faceNormalIndices = face.getNormalIndices();
					int[] faceTextureIndices = face.getTextureCoordinateIndices();
					int[] faceTangentIndices = face.getTangentIndices();
					int[] faceBitangentIndices = face.getBitangentIndices();
					int[] indexedFaceVertexIndices = new int[3];
					for (int idx = 0; idx < 3; idx++) {
						int groupVertexIndex = faceVertexIndices[idx];
						int groupNormalIndex = faceNormalIndices[idx];
						int groupTextureCoordinateIndex = faceTextureIndices != null?faceTextureIndices[idx]:0;
						int groupTangentIndex = faceTangentIndices != null?faceTangentIndices[idx]:0;
						int groupBitangentIndex = faceBitangentIndices != null?faceBitangentIndices[idx]:0;

						Vector3 vertex = groupVertices[groupVertexIndex];
						Vector3 normal = groupNormals[groupNormalIndex];
						TextureCoordinate textureCoordinate = 
							groupTextureCoordinates != null?
							groupTextureCoordinates[groupTextureCoordinateIndex]:
							null;
						Vector3 tangent = 
							groupTangents != null?
							groupTangents[groupTangentIndex]:
							null;
						Vector3 bitangent = 
							groupBitangents != null?
							groupBitangents[groupBitangentIndex]:
							null;

						// check for match
						int newIndex = preparedIndices;
						for (int i = 0; i < preparedIndices; i++)
						if (indexedVertices[i].equals(vertex) &&
							indexedNormals[i].equals(normal) &&
							(groupTextureCoordinates == null || indexedTextureCoordinates[i].equals(textureCoordinate)) &&
							(groupTangents == null || indexedTangents[i].equals(tangent)) &&
							(groupBitangents == null || indexedBitangents[i].equals(bitangent))) {
							//
							newIndex = i;
							break;
						}
						if (newIndex == preparedIndices) preparedIndices++;
						verticeMapping[newIndex] = groupVertexIndex; 
						indexedVertices[newIndex] = vertex;
						indexedNormals[newIndex] = normal;
						if (groupTextureCoordinates != null) indexedTextureCoordinates[newIndex] = textureCoordinate;
						if (groupTangents != null) indexedTangents[newIndex] = tangent;
						if (groupBitangents != null) indexedBitangents[newIndex] = bitangent;
						indexedFaceVertexIndices[idx] = newIndex;
					}
					face.setIndexedRenderingIndices(indexedFaceVertexIndices);
				}	
			}

			// remap skinning
			Skinning skinning = group.getSkinning();
			if (skinning != null) {
				prepareForIndexedRendering(skinning, verticeMapping, preparedIndices);
			}

			// realign vertex data to new size
			Vector3[] vertices = new Vector3[preparedIndices];
			System.arraycopy(indexedVertices, 0, vertices, 0, preparedIndices);
			group.setVertices(vertices);

			Vector3[] normals = new Vector3[preparedIndices];
			System.arraycopy(indexedNormals, 0, normals, 0, preparedIndices);
			group.setNormals(normals);
	
			if (groupTextureCoordinates != null) {
				TextureCoordinate[] textureCoordinates = new TextureCoordinate[preparedIndices];
				System.arraycopy(indexedTextureCoordinates, 0, textureCoordinates, 0, preparedIndices);
				group.setTextureCoordinates(textureCoordinates);
			}
	
			if (groupTangents != null && groupBitangents != null) {
				Vector3[] tangents = new Vector3[preparedIndices];
				System.arraycopy(indexedTangents, 0, tangents, 0, preparedIndices);
				group.setTangents(tangents);
				Vector3[] bitangents = new Vector3[preparedIndices];
				System.arraycopy(indexedBitangents, 0, bitangents, 0, preparedIndices);
				group.setBitangents(bitangents);
			}
			
			// process sub groups
			prepareForIndexedRendering(group.getSubGroups());
		}
	}
	
	/**
	 * Maps original vertices to new vertice mapping
	 * @param skinning
	 * @param vertice mapping / new vertex index to old vertex index
	 * @param vertice count
	 */
	private static void prepareForIndexedRendering(Skinning skinning, int[] vertexMapping, int vertices) {
		JointWeight[][] originalVerticesJointsWeights = skinning.getVerticesJointsWeights();
		JointWeight[][] verticesJointsWeights = new JointWeight[vertices][];
		for (int i = 0; i < vertices; i++) {
			int vertexOriginalMappedToIdx =  vertexMapping[i];
			verticesJointsWeights[i] = new JointWeight[originalVerticesJointsWeights[vertexOriginalMappedToIdx].length];
			for (int j = 0; j < verticesJointsWeights[i].length; j++) {
				verticesJointsWeights[i][j] = originalVerticesJointsWeights[vertexOriginalMappedToIdx][j];
			}
		}
		skinning.setVerticesJointsWeights(verticesJointsWeights);
	}

	/**
	 * Set up joints for skinning groups
	 * @param model
	 */
	public static void setupJoints(Model model) {
		// determine joints and mark them as joints
		HashMap<String,Group> groups = model.getGroups();
		for (Group group: model.getSubGroups().getValuesIterator()) {
			Skinning skinning = group.getSkinning();
			// do we have a skinning
			if (skinning != null) {
				// yep
				for(Joint joint: skinning.getJoints()) {
					setJoint(groups.get(joint.getGroupId()));
				}
			}
		}
	}

	/**
	 * Sets up a group as joint taking all subgroups into account
	 * @param group
	 */
	private static void setJoint(Group root) {
		root.setJoint(true);
		for (Group group: root.getSubGroups().getValuesIterator()) {
			setJoint(group);
		}
	}

	/**
	 * Fix animation length
	 * @param model
	 */
	public static void fixAnimationLength(Model model) {
		// fix animation length
		AnimationSetup defaultAnimation = model.getAnimationSetup(Model.ANIMATIONSETUP_DEFAULT);
		if (defaultAnimation != null) {
			for (Group group: model.getSubGroups().getValuesIterator()) {
				fixAnimationLength(group, defaultAnimation.getFrames());
			}
		}
	}

	/**
	 * Fixes animation length as sometimes they are only given partially, which is not supported by engine
	 * @param group
	 * @param frames
	 */
	private static void fixAnimationLength(Group root, int frames) {
		Animation animation = root.getAnimation();
		Matrix4x4[] transformationsMatrices = new Matrix4x4[0];
		if (animation != null) {
			transformationsMatrices = root.getAnimation().getTransformationsMatrices();
		}
		animation = root.createAnimation(frames);
		for (int i = 0; i < transformationsMatrices.length; i++) {
			animation.getTransformationsMatrices()[i].set(transformationsMatrices[i]);
		}
		for (Group group: root.getSubGroups().getValuesIterator()) {
			fixAnimationLength(group, frames);
		}
	}

	/**
	 * Create default animation
	 * @param model
	 * @param frames
	 */
	public static void createDefaultAnimation(Model model, int frames) {
		// add default model animation setup
		AnimationSetup defaultAnimation = model.getAnimationSetup(Model.ANIMATIONSETUP_DEFAULT);
		if (defaultAnimation == null) {
			model.addAnimationSetup(Model.ANIMATIONSETUP_DEFAULT, 0, frames - 1, true);
		} else {
			// check default animation setup
			if (defaultAnimation.getStartFrame() != 0 || defaultAnimation.getEndFrame() != frames - 1) {
				Console.println("Warning: default animation mismatch");
			}
			if (frames - 1 > defaultAnimation.getEndFrame()) {
				Console.println("Warning: default animation mismatch, will be fixed");
				model.addAnimationSetup(Model.ANIMATIONSETUP_DEFAULT, 0, frames - 1, true);
			}
		}
	}

}

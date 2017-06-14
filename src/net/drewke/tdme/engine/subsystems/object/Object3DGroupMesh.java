package net.drewke.tdme.engine.subsystems.object;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Joint;
import net.drewke.tdme.engine.model.JointWeight;
import net.drewke.tdme.engine.model.Skinning;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.HashMap;

public final class Object3DGroupMesh {

	protected static final int MAX_VERTEX_JOINTS = 5;

	protected Group group;
	protected int faceCount;

	protected short indices[] = null;
	protected Vector3 transformedVertices[] = null;
	protected Vector3 transformedNormals[] = null;
	protected Vector3 transformedTangents[] = null;
	protected Vector3 transformedBitangents[] = null;

	protected TextureCoordinate textureCoordinates[] = null;

	protected Engine.AnimationProcessingTarget animationProcessingTarget;
	protected ArrayList<Matrix4x4> gSkinningJointBindMatrices = null;
	protected Matrix4x4 gFbSkinningTransformationMatrix = null;

	private int cSkinningMaxVertexWeights = -1;
	private float cSkinningJointWeight[][] = null;

	protected Matrix4x4 cGroupTransformationsMatrix = null;
	private Matrix4x4 cSkinningJointBindMatrices[][] = null;
	private Matrix4x4 cSkinningJointTransformationsMatrices[][] = null;
	private Matrix4x4 cTransformationsMatrix = new Matrix4x4();
	private Vector3 tmpVector3;


	protected boolean skinning = false;
	protected int skinningJoints = -1;

	private boolean recreatedBuffers;

	/**
	 * Creates a object3d group mesh from group
	 * @param animation processing target
	 * @param group
	 * @param transformationm matrices
	 * @return object 3d group mesh
	 */
	protected static Object3DGroupMesh createMesh(Engine.AnimationProcessingTarget animationProcessingTarget, Group group, HashMap<String, Matrix4x4> transformationMatrices) {
		Object3DGroupMesh mesh = new Object3DGroupMesh();

		//
		mesh.group = group;

		// group data
		Vector3[] groupVertices = group.getVertices();
		Vector3[] groupNormals = group.getNormals();
		Vector3[] groupTangents = group.getTangents();
		Vector3[] groupBitangents = group.getBitangents();

		// determine face count
		mesh.faceCount = group.getFaceCount();

		// animation processing target
		mesh.animationProcessingTarget = animationProcessingTarget;

		// transformations for skinned meshes
		Skinning skinning = group.getSkinning();
		mesh.skinning = skinning != null;

		// transformed mesh vertices
		mesh.transformedVertices = new Vector3[groupVertices.length];
		for(int j = 0; j < mesh.transformedVertices.length; j++) {
			mesh.transformedVertices[j] = new Vector3().set(groupVertices[j]);
		}

		// transformed mesh normals
		mesh.transformedNormals = new Vector3[groupNormals.length];
		for(int j = 0; j < mesh.transformedNormals.length; j++) {
			mesh.transformedNormals[j] = new Vector3().set(groupNormals[j]);
		}

		// transformed mesh tangents
		if (groupTangents != null) {
			mesh.transformedTangents = new Vector3[groupTangents.length];
			for(int j = 0; j < mesh.transformedTangents.length; j++) {
				mesh.transformedTangents[j] = new Vector3().set(groupTangents[j]);
			}			
		}

		// transformed mesh bitangents
		if (groupBitangents != null) {
			mesh.transformedBitangents = new Vector3[groupBitangents.length];
			for(int j = 0; j < mesh.transformedBitangents.length; j++) {
				mesh.transformedBitangents[j] = new Vector3().set(groupBitangents[j]);
			}			
		}

		// indices
		int indicesCount = 0; 
		for (FacesEntity facesEntity: group.getFacesEntities()) {
			indicesCount+= 3 * facesEntity.getFaces().length;
		}
		mesh.indices = new short[indicesCount];
		{
			int j = 0;
			// create face vertex indices
			for (FacesEntity facesEntity: group.getFacesEntities())
			for (Face face: facesEntity.getFaces())
			for (int vertexIndex: face.getVertexIndices()) {
				mesh.indices[j++] = (short)vertexIndex;
			}
		}

		//
		mesh.recreatedBuffers = false;

		// group transformations matrix
		if (mesh.animationProcessingTarget == Engine.AnimationProcessingTarget.CPU ||
			mesh.animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {

			// group transformations matrix 
			mesh.cGroupTransformationsMatrix = transformationMatrices.get(group.getId());
		}

		// skinning
		if (skinning != null) {
			// skinning computation caches if computing skinning on CPU
			if (mesh.animationProcessingTarget == Engine.AnimationProcessingTarget.CPU ||
				mesh.animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
				mesh.cSkinningJointWeight = new float[groupVertices.length][];
				mesh.cSkinningJointBindMatrices = new Matrix4x4[groupVertices.length][];
				mesh.cSkinningJointTransformationsMatrices = new Matrix4x4[groupVertices.length][];
				mesh.cTransformationsMatrix = new Matrix4x4();
	
				// compute joint weight caches
				Joint[] joints = skinning.getJoints();
				float[] weights = skinning.getWeights();
				JointWeight[][] jointsWeights = skinning.getVerticesJointsWeights();
				for (int vertexIndex = 0; vertexIndex < groupVertices.length; vertexIndex++) {
					int vertexJointWeights = jointsWeights[vertexIndex].length;
					if (vertexJointWeights > mesh.cSkinningMaxVertexWeights) mesh.cSkinningMaxVertexWeights = vertexJointWeights;
					mesh.cSkinningJointWeight[vertexIndex] = new float[vertexJointWeights];
					mesh.cSkinningJointBindMatrices[vertexIndex] = new Matrix4x4[vertexJointWeights];
					mesh.cSkinningJointTransformationsMatrices[vertexIndex] = new Matrix4x4[vertexJointWeights];
					int jointWeightIdx = 0;
					for (JointWeight jointWeight : jointsWeights[vertexIndex]) {
						Joint joint = joints[jointWeight.getJointIndex()];
						// 
						mesh.cSkinningJointWeight[vertexIndex][jointWeightIdx] = weights[jointWeight.getWeightIndex()]; 
						mesh.cSkinningJointBindMatrices[vertexIndex][jointWeightIdx] = joint.getBindMatrix();
						mesh.cSkinningJointTransformationsMatrices[vertexIndex][jointWeightIdx] = transformationMatrices.get(joint.getGroupId());
	
						// next
						jointWeightIdx++;
					}
				}
			}
		}

		// temp vector3
		mesh.tmpVector3 = new Vector3();

		// issue a recreate buffer and upload to graphics board
		mesh.recreateBuffers();

		//
		return mesh;
	}

	/**
	 * Computes mesh transformations
	 * @param group
	 */
	protected void computeTransformations(Group group) {
		Vector3[] groupVertices = group.getVertices();
		Vector3[] groupNormals = group.getNormals();
		Vector3[] groupTangent = group.getTangents();
		Vector3[] groupBitangent = group.getBitangents();

		// transformations for skinned meshes
		Skinning skinning = group.getSkinning();
		if (skinning != null) {
			// compute skinning on CPU if required
			if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU ||
				animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
				JointWeight[][] jointsWeights = skinning.getVerticesJointsWeights();
				Vector3 vertex;
				Vector3 transformedVertex;
				Vector3 normal;
				Vector3 transformedNormal;
				Vector3 tangent;
				Vector3 transformedTangent;
				Vector3 bitangent;
				Vector3 transformedBitangent;
				float totalWeights;
				float weightNormalized;
				for (int vertexIndex = 0; vertexIndex < groupVertices.length; vertexIndex++) {
					// do vertices
					vertex = groupVertices[vertexIndex];
					transformedVertex = transformedVertices[vertexIndex].set(0f,0f,0f);
					normal = groupNormals[vertexIndex];
					transformedNormal = transformedNormals[vertexIndex].set(0f,0f,0f);
					tangent = groupTangent != null?groupTangent[vertexIndex]:null;
					transformedTangent = transformedTangents != null?transformedTangents[vertexIndex].set(0f,0f,0f):null;
					bitangent = groupTangent != null?groupBitangent[vertexIndex]:null;
					transformedBitangent = transformedBitangents != null?transformedBitangents[vertexIndex].set(0f,0f,0f):null;
		
					// compute every influence on vertex and vertex normals
					totalWeights = 0f;
					for (int vertexJointWeightIdx = 0; vertexJointWeightIdx < jointsWeights[vertexIndex].length; vertexJointWeightIdx++) {
						float weight = cSkinningJointWeight[vertexIndex][vertexJointWeightIdx];
	
						// 
						cTransformationsMatrix.set(cSkinningJointBindMatrices[vertexIndex][vertexJointWeightIdx]);
						cTransformationsMatrix.multiply(cSkinningJointTransformationsMatrices[vertexIndex][vertexJointWeightIdx]);
	
						// vertex
						transformedVertex.add(cTransformationsMatrix.multiply(vertex, tmpVector3).scale(weight));
	
						// normals
						transformedNormal.add(cTransformationsMatrix.multiplyNoTranslation(normal, tmpVector3).scale(weight));

						// tangent
						if (tangent != null) {
							transformedTangent.add(cTransformationsMatrix.multiplyNoTranslation(tangent, tmpVector3).scale(weight));
						}

						// bitangent
						if (bitangent != null) {
							transformedBitangent.add(cTransformationsMatrix.multiplyNoTranslation(bitangent, tmpVector3).scale(weight));
						}

						//
						totalWeights += weight;
					}
	
					// scale to full weight
					if (totalWeights != 1f) {
						weightNormalized = 1f / totalWeights;
	
						// vertex
						transformedVertex.scale(weightNormalized);
	
						// normals
						transformedNormal.scale(weightNormalized);
						
						// tangent
						if (transformedTangent != null) {
							transformedTangent.scale(weightNormalized);
						}

						// bitangent
						if (transformedBitangent != null) {
							transformedBitangent.scale(weightNormalized);
						}
					}
	
					// normalize normal
					transformedNormal.normalize();
				}

				// recreate buffers
				recreateBuffers();
			}
		} else {
			if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) {
				// transformations for non skinned rendering
				//	vertices
				for (int vertexIndex = 0; vertexIndex < groupVertices.length; vertexIndex++) {
					transformedVertices[vertexIndex].set(cGroupTransformationsMatrix.multiply(groupVertices[vertexIndex], tmpVector3));
				}

				//	normals
				for (int normalIndex = 0; normalIndex < groupNormals.length; normalIndex++) {
					transformedNormals[normalIndex].set(cGroupTransformationsMatrix.multiplyNoTranslation(groupNormals[normalIndex], tmpVector3).normalize());
				}

				// recreate buffers
				recreateBuffers();
			}
		}
	}

	/**
	 * Recreates group float buffers
	 */
	protected void recreateBuffers() {
		recreatedBuffers = true;
	}

	/**
	 * @return if buffers has been recreated and unsets state
	 */
	protected boolean hasRecreatedBuffers() {
		if (recreatedBuffers == true) {
			recreatedBuffers = false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set up vertex indices buffer
	 * @return vertex indices buffer
	 */
	protected ShortBuffer setupVertexIndicesBuffer() {
		ShortBuffer sbIndices = Buffer.getByteBuffer(faceCount * 3 * Short.SIZE / Byte.SIZE).asShortBuffer();

		// create face vertex indices, will never be changed in engine
		for (short index: indices) {
			sbIndices.put(index);
		}

		// done
		sbIndices.flip();
		return sbIndices;
	}

	/**
	 * Set up texture coordinates buffer
	 * @return texture coordinates buffer
	 */
	protected FloatBuffer setupTextureCoordinatesBuffer() {
		// check if we have texture coordinates
		TextureCoordinate[] groupTextureCoordinates = group.getTextureCoordinates();
		if (groupTextureCoordinates == null) return null;

		// create texture coordinates buffer, will never be changed in engine
		FloatBuffer fbTextureCoordinates = Buffer.getByteBuffer(groupTextureCoordinates.length * 2 * Float.SIZE / Byte.SIZE).asFloatBuffer();

		// construct texture coordinates byte buffer as this will not change usually
		for (TextureCoordinate textureCoordinate: groupTextureCoordinates) {
			fbTextureCoordinates.put(textureCoordinate.getArray());
		}

		// done
		fbTextureCoordinates.flip();
		return fbTextureCoordinates;
	}

	/**
	 * Set up vertices buffer
	 * @return vertices buffer
	 */
	protected FloatBuffer setupVerticesBuffer() {
		FloatBuffer fbVertices = Buffer.getByteBuffer(transformedVertices.length * 3 * Float.SIZE / Byte.SIZE).asFloatBuffer();

		// create vertices buffers
		for (Vector3 vertex: transformedVertices) {
			fbVertices.put(vertex.getArray());
		}

		// done
		fbVertices.flip();
		return fbVertices;
	}

	/**
	 * Set up normals buffer
	 * @return normals buffer
	 */
	protected FloatBuffer setupNormalsBuffer() {
		FloatBuffer fbNormals = Buffer.getByteBuffer(transformedNormals.length * 3 * Float.SIZE / Byte.SIZE).asFloatBuffer();

		// create vertices buffers
		for (Vector3 normal: transformedNormals) {
			fbNormals.put(normal.getArray());
		}

		// done
		fbNormals.flip();
		return fbNormals;
	}

	/**
	 * Set up tangents buffer
	 * @return tangents buffer
	 */
	protected FloatBuffer setupTangentsBuffer() {
		// check if we have tangents
		if (transformedTangents == null) {
			return null;
		}

		//
		FloatBuffer fbTangents = Buffer.getByteBuffer(transformedTangents.length * 3 * Float.SIZE / Byte.SIZE).asFloatBuffer();

		// create tangents buffers
		for (Vector3 tangent: transformedTangents) {
			fbTangents.put(tangent.getArray());
		}

		// done
		fbTangents.flip();
		return fbTangents;
	}

	/**
	 * Set up bitangents buffer
	 * @return bitangents buffer
	 */
	protected FloatBuffer setupBitangentsBuffer() {
		// check if we have tangents
		if (transformedBitangents == null) {
			return null;
		}

		//
		FloatBuffer fbBitangents = Buffer.getByteBuffer(transformedBitangents.length * 3 * Float.SIZE / Byte.SIZE).asFloatBuffer();

		// create tangents buffers
		for (Vector3 bitangent: transformedBitangents) {
			fbBitangents.put(bitangent.getArray());
		}

		// done
		fbBitangents.flip();
		return fbBitangents;
	}

}

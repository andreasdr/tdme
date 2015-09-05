package net.drewke.tdme.engine.subsystems.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

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
import net.drewke.tdme.utils.HashMap;

public final class Object3DGroupMesh {

	protected static final int MAX_VERTEX_JOINTS = 5;

	private int faces;

	protected short indices[] = null;
	protected Vector3 transformedVertices[] = null;
	protected Vector3 transformedNormals[] = null;
	protected Vector3 transformedTangents[] = null;
	protected Vector3 transformedBitangents[] = null;

	protected TextureCoordinate textureCoordinates[] = null;

	protected Engine.AnimationProcessingTarget animationProcessingTarget;
	protected ShortBuffer sbIndices = null;
	protected FloatBuffer fbVertices = null;
	protected FloatBuffer fbNormals = null;
	protected FloatBuffer fbTextureCoordinates = null;
	protected FloatBuffer fbTangents = null;
	protected FloatBuffer fbBitangents = null;
	protected FloatBuffer gIbSkinningVerticesJoints = null;
	protected FloatBuffer gFbSkinningVerticesVertexJointsIdxs = null;
	protected FloatBuffer gFbSkinningVerticesVertexJointsWeights = null;
	protected FloatBuffer gFbSkinningJointsTransformationsMatrices = null;
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

		// group data
		Vector3[] groupVertices = group.getVertices();
		Vector3[] groupNormals = group.getNormals();
		TextureCoordinate[] groupTextureCoordinates = group.getTextureCoordinates();
		Vector3[] groupTangents = group.getTangents();
		Vector3[] groupBitangents = group.getBitangents();

		// determine face count
		int faceCount = group.getFaceCount();

		// set up face count
		mesh.faces = faceCount;

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

		// texture coordinates
		if (groupTextureCoordinates != null) {
			mesh.textureCoordinates = new TextureCoordinate[groupTextureCoordinates.length];
			for(int j = 0; j < mesh.textureCoordinates.length; j++) {
				mesh.textureCoordinates[j] = new TextureCoordinate(groupTextureCoordinates[j]);
			}
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

		// create mesh upload buffers
		if (mesh.animationProcessingTarget != Engine.AnimationProcessingTarget.CPU_NORENDERING) {
			mesh.sbIndices = ByteBuffer.allocateDirect(mesh.faces * 3 * Short.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer();
			mesh.fbVertices = ByteBuffer.allocateDirect(groupVertices.length * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
			mesh.fbNormals = ByteBuffer.allocateDirect(groupNormals.length * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
			mesh.fbTextureCoordinates = groupTextureCoordinates != null?ByteBuffer.allocateDirect(groupTextureCoordinates.length * 2 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer():null;
			mesh.fbTangents = groupTangents != null?ByteBuffer.allocateDirect(groupTangents.length * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer():null;
			mesh.fbBitangents = groupBitangents != null?ByteBuffer.allocateDirect(groupBitangents.length * 3 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer():null;

			// create face vertex indices, will never be changed in engine
			for (FacesEntity facesEntity: group.getFacesEntities())
			for (Face face: facesEntity.getFaces())
			for (int vertexIndex: face.getVertexIndices()) {
				mesh.sbIndices.put((short)vertexIndex);
			}
			mesh.sbIndices.flip();

			// create texture coordinates buffer, will never be changed in engine
			if (mesh.fbTextureCoordinates != null) {
				// construct texture coordinates byte buffer as this will not change usually
				for (TextureCoordinate textureCoordinate: groupTextureCoordinates) {
					mesh.fbTextureCoordinates.put(textureCoordinate.getArray());
				}
				mesh.fbTextureCoordinates.flip();
			}
		}

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
			} else
			// GPU setup
			if (mesh.animationProcessingTarget == Engine.AnimationProcessingTarget.GPU) {
				// create skinning buffers
				mesh.gIbSkinningVerticesJoints = ByteBuffer.allocateDirect(groupVertices.length * 1 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
				mesh.gFbSkinningVerticesVertexJointsIdxs = ByteBuffer.allocateDirect(groupVertices.length * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
				mesh.gFbSkinningVerticesVertexJointsWeights = ByteBuffer.allocateDirect(groupVertices.length * 4 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
				mesh.gFbSkinningJointsTransformationsMatrices = ByteBuffer.allocateDirect(60 * 16 * Float.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
				mesh.gFbSkinningTransformationMatrix = new Matrix4x4();

				// fill skinning buffers, joint bind matrices
				mesh.skinningJoints = skinning.getJoints().length;
				mesh.gSkinningJointBindMatrices = new ArrayList<Matrix4x4>();
				for (Joint joint: skinning.getJoints()) {
					mesh.gSkinningJointBindMatrices.add(joint.getBindMatrix());					
				}

				//
				JointWeight[][] jointsWeights = skinning.getVerticesJointsWeights();
				float[] weights = skinning.getWeights();
				for (int groupVertexIndex = 0; groupVertexIndex < groupVertices.length; groupVertexIndex++) {
					int vertexJoints = jointsWeights[groupVertexIndex].length;

					// put number of joints
					mesh.gIbSkinningVerticesJoints.put((float)vertexJoints);
 
					// vertex joint idx 1..4
					for (int i = 0; i < 4; i++) {
						mesh.gFbSkinningVerticesVertexJointsIdxs.put((float)(vertexJoints > i?jointsWeights[groupVertexIndex][i].getJointIndex():-1));
					}

					// vertex joint weight 1..4
					for (int i = 0; i < 4; i++) {
						mesh.gFbSkinningVerticesVertexJointsWeights.put(vertexJoints > i?weights[jointsWeights[groupVertexIndex][i].getWeightIndex()]:0.0f);
					}
				}

				// put number of joints
				mesh.gIbSkinningVerticesJoints.flip();

				// vertex joint idx 1..4
				mesh.gFbSkinningVerticesVertexJointsIdxs.flip();

				// vertex joint weight 1..4
				mesh.gFbSkinningVerticesVertexJointsWeights.flip();
			}
		}

		// temp vector3
		mesh.tmpVector3 = new Vector3();

		// issue a recreate buffer and upload to graphics board
		mesh.recreateBuffers(group);

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
				recreateBuffers(group);
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
				recreateBuffers(group);
			}
		}
	}

	/**
	 * Set ups or fills skinning transformation matrices
	 */
	protected void setupSkinningTransformationsMatrices(ArrayList<Matrix4x4> gSkinningJointTransformationsMatrices) {
		gFbSkinningJointsTransformationsMatrices.clear();
		for (int jointIdx = 0; jointIdx < skinningJoints; jointIdx++) {
			gFbSkinningTransformationMatrix.set(gSkinningJointBindMatrices.get(jointIdx));
			gFbSkinningTransformationMatrix.multiply(gSkinningJointTransformationsMatrices.get(jointIdx));
			gFbSkinningJointsTransformationsMatrices.put(
				gFbSkinningTransformationMatrix.getArray()
			);
		}
		gFbSkinningJointsTransformationsMatrices.flip();
	}

	/**
	 * Recreates group float buffers
	 * @param group
	 */
	protected void recreateBuffers(Group group) {
		if (animationProcessingTarget == Engine.AnimationProcessingTarget.CPU_NORENDERING) return;

		// flip buffers for reusage
		fbVertices.clear();
		fbNormals.clear();

		// (re)create buffers
		//	vertices, normals
		int vertices = transformedVertices.length;
		for (int vertexIndex = 0; vertexIndex < vertices; vertexIndex++) {
			fbVertices.put(transformedVertices[vertexIndex].getArray());
			fbNormals.put(transformedNormals[vertexIndex].getArray());
		}
		//	tangents, bitangents
		if (fbTangents != null && fbBitangents != null)
		for (int vertexIndex = 0; vertexIndex < vertices; vertexIndex++) {
			fbTangents.put(transformedTangents[vertexIndex].getArray());
			fbBitangents.put(transformedBitangents[vertexIndex].getArray());
		}

		// flip buffers
		fbVertices.flip();
		fbNormals.flip();
		if (fbTangents != null) fbTangents.flip();
		if (fbBitangents != null) fbBitangents.flip();

		//
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
	 * @return transformed vertices
	 */
	public Vector3[] getTransformedVertices() {
		return transformedVertices;
	}

	/**
	 * @return number of skinning joints
	 */
	public int getSkinningJoints() {
		return skinningJoints;
	}

	/**
	 * @return skinning joints transformations matrices float buffer
	 */
	public FloatBuffer getSkinningJointsTransformationsMatricesFloatBuffer() {
		return gFbSkinningJointsTransformationsMatrices;
	}

}
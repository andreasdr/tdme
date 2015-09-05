package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.subsystems.manager.VBOManager;

/**
 * Object 3D group render 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Object3DGroupVBORenderer {

	protected Object3DGroup object3DGroup;
	protected int[] vboBaseIds;
	protected int[] vboTangentBitangentIds;
	protected int[] vboSkinningIds;

	/**
	 * Constructor
	 * @param object 3D group
	 */
	protected Object3DGroupVBORenderer(Object3DGroup object3DGroup) {
		this.object3DGroup = object3DGroup;
		this.vboBaseIds = null;
		this.vboTangentBitangentIds = null;
		this.vboSkinningIds = null;
	}

	/*
	 * Do prerender processing
	 * @param object 3d vbo renderer
	 */
	protected void preRender(Object3DVBORenderer object3DVBORenderer) {
		boolean meshUploaded = true;

		// initialize if not yet done
		if (vboBaseIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO(object3DGroup.id, object3DGroup.mesh.fbTextureCoordinates != null?4:3);
			vboBaseIds = vboManaged.getVBOGlIds();
			meshUploaded = vboManaged.isUploaded();
		}
		
		// initialize tangents, bitangents
		if (object3DVBORenderer.renderer.isNormalMappingAvailable() &&
			object3DGroup.mesh.fbTangents != null &&
			object3DGroup.mesh.fbBitangents != null &&
			vboTangentBitangentIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO(object3DGroup.id + ".tangentbitangent", 2);
			vboTangentBitangentIds = vboManaged.getVBOGlIds();
		}

		// initialize skinning if not yet done
		if (Engine.animationProcessingTarget == Engine.AnimationProcessingTarget.GPU &&
			object3DGroup.mesh.skinning == true &&
			vboSkinningIds == null) {
			//
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO(object3DGroup.id + ".skinning", 3);
			vboSkinningIds = vboManaged.getVBOGlIds();

			// upload if required
			if (vboManaged.isUploaded() == false) {
				// upload skinning buffers
				object3DVBORenderer.renderer.uploadBufferObject(
					vboSkinningIds[0],
					object3DGroup.mesh.gIbSkinningVerticesJoints.capacity() * Float.SIZE / Byte.SIZE,
					object3DGroup.mesh.gIbSkinningVerticesJoints
				);
				object3DVBORenderer.renderer.uploadBufferObject(
					vboSkinningIds[1],
					object3DGroup.mesh.gFbSkinningVerticesVertexJointsIdxs.capacity() * Float.SIZE / Byte.SIZE,
					object3DGroup.mesh.gFbSkinningVerticesVertexJointsIdxs
				);				
				object3DVBORenderer.renderer.uploadBufferObject(
					vboSkinningIds[2],
					object3DGroup.mesh.gFbSkinningVerticesVertexJointsWeights.capacity() * Float.SIZE / Byte.SIZE,
					object3DGroup.mesh.gFbSkinningVerticesVertexJointsWeights
				);
			}
		}

		// check if to upload new mesh
		if (object3DGroup.mesh.hasRecreatedBuffers() == true || meshUploaded == false) {
			if (meshUploaded == false) {
				// upload indices
				object3DVBORenderer.renderer.uploadIndicesBufferObject(
					vboBaseIds[0],
					object3DGroup.mesh.sbIndices.capacity() * Short.SIZE / Byte.SIZE,
					object3DGroup.mesh.sbIndices
				);

				// upload texture coordinates
				if (object3DGroup.mesh.fbTextureCoordinates != null) {
					object3DVBORenderer.renderer.uploadBufferObject(
						vboBaseIds[3],
						object3DGroup.mesh.fbTextureCoordinates.capacity() * Float.SIZE / Byte.SIZE,
						object3DGroup.mesh.fbTextureCoordinates
					);
				}
			}

			// upload vertices
			object3DVBORenderer.renderer.uploadBufferObject(
				vboBaseIds[1],
				object3DGroup.mesh.fbVertices.capacity() * Float.SIZE / Byte.SIZE,
				object3DGroup.mesh.fbVertices
			);

			// upload normals
			object3DVBORenderer.renderer.uploadBufferObject(
				vboBaseIds[2],
				object3DGroup.mesh.fbNormals.capacity() * Float.SIZE / Byte.SIZE,
				object3DGroup.mesh.fbNormals
			);

			// tangents, bitangents
			if (vboTangentBitangentIds != null) {
				// upload tangents
				object3DVBORenderer.renderer.uploadBufferObject(
					vboTangentBitangentIds[0],
					object3DGroup.mesh.fbTangents.capacity() * Float.SIZE / Byte.SIZE,
					object3DGroup.mesh.fbTangents
				);
				// upload bitangents
				object3DVBORenderer.renderer.uploadBufferObject(
					vboTangentBitangentIds[1],
					object3DGroup.mesh.fbBitangents.capacity() * Float.SIZE / Byte.SIZE,
					object3DGroup.mesh.fbBitangents
				);
			}
		}
	}

	/**
	 * Disposes the object 3d group
	 * @param gl
	 */
	protected void dispose() {
		// 
		if (vboBaseIds != null) {
			Engine.getInstance().getVBOManager().removeVBO(object3DGroup.id);
			vboBaseIds = null;
		}

		// initialize skinning if not yet done
		if (vboSkinningIds != null) {
			Engine.getInstance().getVBOManager().removeVBO(object3DGroup.id + ".skinning");
			vboSkinningIds = null;
		}
	}

}

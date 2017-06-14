package net.drewke.tdme.engine.subsystems.object;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO(object3DGroup.id, object3DGroup.mesh.group.getTextureCoordinates() != null?4:3);
			vboBaseIds = vboManaged.getVBOGlIds();
			meshUploaded = vboManaged.isUploaded();
		}

		// initialize tangents, bitangents
		if (object3DVBORenderer.renderer.isNormalMappingAvailable() &&
			object3DGroup.mesh.group.getTangents() != null &&
			object3DGroup.mesh.group.getBitangents() != null &&
			vboTangentBitangentIds == null) {
			VBOManager.VBOManaged vboManaged = Engine.getInstance().getVBOManager().addVBO(object3DGroup.id + ".tangentbitangent", 2);
			vboTangentBitangentIds = vboManaged.getVBOGlIds();
		}

		// check if to upload new mesh
		if (object3DGroup.mesh.hasRecreatedBuffers() == true || meshUploaded == false) {
			if (meshUploaded == false) {
				// upload indices
				ShortBuffer sbIndices = object3DGroup.mesh.setupVertexIndicesBuffer();
				object3DVBORenderer.renderer.uploadIndicesBufferObject(
					vboBaseIds[0],
					sbIndices.remaining() * Short.SIZE / Byte.SIZE,
					sbIndices
				);

				// upload texture coordinates
				if (object3DGroup.mesh.group.getTextureCoordinates() != null) {
					FloatBuffer fbTextureCoordinates = object3DGroup.mesh.setupTextureCoordinatesBuffer(); 
					object3DVBORenderer.renderer.uploadBufferObject(
						vboBaseIds[3],
						fbTextureCoordinates.remaining() * Float.SIZE / Byte.SIZE,
						fbTextureCoordinates
					);
				}
			}

			// upload vertices
			FloatBuffer fbVertices = object3DGroup.mesh.setupVerticesBuffer();
			object3DVBORenderer.renderer.uploadBufferObject(
				vboBaseIds[1],
				fbVertices.remaining() * Float.SIZE / Byte.SIZE,
				fbVertices
			);

			// upload normals
			FloatBuffer fbNormals = object3DGroup.mesh.setupNormalsBuffer();
			object3DVBORenderer.renderer.uploadBufferObject(
				vboBaseIds[2],
				fbNormals.remaining() * Float.SIZE / Byte.SIZE,
				fbNormals
			);

			// tangents, bitangents
			if (vboTangentBitangentIds != null) {
				// upload tangents
				FloatBuffer fbTangents = object3DGroup.mesh.setupTangentsBuffer();
				object3DVBORenderer.renderer.uploadBufferObject(
					vboTangentBitangentIds[0],
					fbTangents.remaining() * Float.SIZE / Byte.SIZE,
					fbTangents
				);
				// upload bitangents
				FloatBuffer fbBitangents = object3DGroup.mesh.setupBitangentsBuffer();
				object3DVBORenderer.renderer.uploadBufferObject(
					vboTangentBitangentIds[1],
					fbBitangents.remaining() * Float.SIZE / Byte.SIZE,
					fbBitangents
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

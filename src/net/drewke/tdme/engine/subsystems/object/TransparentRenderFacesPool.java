package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.Pool;

/**
 * Transparent render faces pool
 * @author andreas.drewke
 * @version $Id$
 */
public final class TransparentRenderFacesPool {

	private final static int FACES_MAX = 16384;

	private ArrayList<TransparentRenderFace> transparentRenderFaces = null;
	private Pool<TransparentRenderFace> transparentRenderFacesPool = null;

	private Vector3 tmpVector3;

	/**
	 * Default constructor
	 */
	protected TransparentRenderFacesPool() {
		tmpVector3 = new Vector3();
		transparentRenderFaces = new ArrayList<TransparentRenderFace>();
		transparentRenderFacesPool = new Pool<TransparentRenderFace>() {
			public TransparentRenderFace instantiate() {
				return new TransparentRenderFace();
			}
		};
	}

	/**
	 * Creates an array of transparent render faces from
	 * @param model view matrix
	 * @param object3D group
	 * @param faces entity index
	 * @param face index
	 * @param face index for texture coordinates
	 * @return
	 */
	protected void createTransparentRenderFaces(Matrix4x4 modelViewMatrix, Object3DGroup object3DGroup, int facesEntityIdx, int faceIdx) {
		// retrieve objects we need
		FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities();
		FacesEntity facesEntity = facesEntities[facesEntityIdx];
		Face[] faces = facesEntity.getFaces();		
		Vector3[] groupTransformedVertices = object3DGroup.mesh.transformedVertices;

		// objects we will use for calculations
		float distanceFromCamera;

		// create transparent render faces
		for (int i = 0; i < faces.length; i++) {
			// check for pool overflow
			if (size() >= FACES_MAX) {
				Console.println("TransparentRenderFacesPool::createTransparentRenderFaces(): Too many transparent render faces");
				break;
			}

			// set up face
			int[] faceVertexIndices = faces[i].getVertexIndices();
			tmpVector3.set(0.0f, 0.0f, 0.0f); 
			tmpVector3.add(groupTransformedVertices[faceVertexIndices[0]]); 
			tmpVector3.add(groupTransformedVertices[faceVertexIndices[1]]);
			tmpVector3.add(groupTransformedVertices[faceVertexIndices[2]]);
			tmpVector3.scale(1.0f / 3.0f);
			modelViewMatrix.multiply(tmpVector3, tmpVector3);
			distanceFromCamera = -tmpVector3.getZ();

			// create transparent render face
			TransparentRenderFace transparentRenderFace = transparentRenderFacesPool.allocate();
			transparentRenderFace.object3DGroup = object3DGroup;
			transparentRenderFace.facesEntityIdx = facesEntityIdx;
			transparentRenderFace.faceIdx = faceIdx;
			transparentRenderFace.distanceFromCamera = distanceFromCamera;
			transparentRenderFaces.add(transparentRenderFace);
			faceIdx++;
		}
	}

	/**
	 * @return allocated faces
	 */
	public int size() {
		return transparentRenderFacesPool.size();
	}

	/**
	 * Reset
	 */
	protected void reset() {
		transparentRenderFacesPool.reset();
		transparentRenderFaces.clear();
	}

	/**
	 * @return transparent render faces vector
	 */
	protected ArrayList<TransparentRenderFace> getTransparentRenderFaces() {
		return transparentRenderFaces;
	}

}

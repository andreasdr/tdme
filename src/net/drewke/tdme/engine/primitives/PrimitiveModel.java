package net.drewke.tdme.engine.primitives;

import java.util.ArrayList;

import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Quaternion;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.HashMap;

/**
 * Class to create primitives model
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PrimitiveModel {

	// segments
	private final static int SPHERE_SEGMENTS_X = 20;
	private final static int SPHERE_SEGMENTS_Y = 20;
	private final static int CAPSULE_SEGMENTS_X = 20;
	private final static int CAPSULE_SEGMENTS_Y = 20;

	/**
	 * Creates a model from bounding box
	 * @param bounding box
	 * @param id
	 * @return model
	 */
	public static Model createBoundingBoxModel(BoundingBox boundingBox, String id) {
		// ground model
		Model model = new Model(id, id);

		// material
		Material material = new Material("tdme.primitive.material");
		material.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		material.getDiffuseColor().set(1.0f, 0.5f, 0.5f, 0.5f);
		model.getMaterials().put(material.getId(), material);

		// group
		Group group = new Group(model, "group", "group");

		// faces entity
		FacesEntity groupFacesEntity = new FacesEntity(group, "faces entity");
		groupFacesEntity.setMaterial(material);

		// faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntity);

		// triangle vertices indexes
		int[][] fvi = OrientedBoundingBox.facesVerticesIndexes;

		// vertices
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		for (Vector3 vertex: boundingBox.getVertices()) {
			vertices.add(vertex.clone());
		}

		// normals
		ArrayList<Vector3> normals = new ArrayList<Vector3>();
		normals.add(new Vector3(-1.0f, 0.0f, 0.0f));
		normals.add(new Vector3(+1.0f, 0.0f, 0.0f));
		normals.add(new Vector3(0.0f, -1.0f, 0.0f));
		normals.add(new Vector3(0.0f, +1.0f, 0.0f));
		normals.add(new Vector3(0.0f, 0.0f, -1.0f));
		normals.add(new Vector3(0.0f, 0.0f, +1.0f));

		// faces
		ArrayList<Face> faces = new ArrayList<Face>();
		//	left
		faces.add(new Face(group,fvi[0][0],fvi[0][1],fvi[0][2],0,0,0));
		faces.add(new Face(group,fvi[1][0],fvi[1][1],fvi[1][2],0,0,0));
		//	right
		faces.add(new Face(group,fvi[2][0],fvi[2][1],fvi[2][2],1,1,1));
		faces.add(new Face(group,fvi[3][0],fvi[3][1],fvi[3][2],1,1,1));
		//	top
		faces.add(new Face(group,fvi[4][0],fvi[4][1],fvi[4][2],2,2,2));
		faces.add(new Face(group,fvi[5][0],fvi[5][1],fvi[5][2],2,2,2));
		//	bottom
		faces.add(new Face(group,fvi[6][0],fvi[6][1],fvi[6][2],3,3,3));
		faces.add(new Face(group,fvi[7][0],fvi[7][1],fvi[7][2],3,3,3));
		//	near
		faces.add(new Face(group,fvi[8][0],fvi[8][1],fvi[8][2],4,4,4));
		faces.add(new Face(group,fvi[9][0],fvi[9][1],fvi[9][2],4,4,4));
		//	far
		faces.add(new Face(group,fvi[10][0],fvi[10][1],fvi[10][2],5,5,5));
		faces.add(new Face(group,fvi[11][0],fvi[11][1],fvi[11][2],5,5,5));

		// set up faces entity
		groupFacesEntity.setFaces(faces);

		// setup group vertex data
		group.setVertices(vertices);
		group.setNormals(normals);
		group.setFacesEntities(groupFacesEntities);

		// determine features
		group.determineFeatures();

		// register group
		model.getGroups().put("group", group);
		model.getSubGroups().put("group", group);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Creates a model from oriented bounding box
	 * @param bounding box
	 * @param id
	 * @return model
	 */
	public static Model createOrientedBoundingBoxModel(OrientedBoundingBox orientedBoundingBox, String id) {
		// ground model
		Model model = new Model(id, id);

		// material
		Material material = new Material("tdme.primitive.material");
		material.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		material.getDiffuseColor().set(1.0f, 0.5f, 0.5f, 0.5f);
		model.getMaterials().put(material.getId(), material);

		// group
		Group group = new Group(model, "group", "group");

		// faces entity
		FacesEntity groupFacesEntity = new FacesEntity(group, "faces entity");
		groupFacesEntity.setMaterial(material);

		// faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntity);

		// triangle vertices indexes
		int[][] fvi = OrientedBoundingBox.facesVerticesIndexes;

		// vertices
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		for (Vector3 vertex: orientedBoundingBox.vertices) {
			vertices.add(vertex.clone());
		}

		Vector3[] axes = orientedBoundingBox.axes;

		// normals
		ArrayList<Vector3> normals = new ArrayList<Vector3>();
		normals.add(axes[0].clone().scale(-1f));
		normals.add(axes[0].clone());
		normals.add(axes[1].clone().scale(-1f));
		normals.add(axes[1].clone());
		normals.add(axes[2].clone().scale(-1f));
		normals.add(axes[2].clone());

		// faces
		ArrayList<Face> faces = new ArrayList<Face>();
		//	left
		faces.add(new Face(group,fvi[0][0],fvi[0][1],fvi[0][2],0,0,0));
		faces.add(new Face(group,fvi[1][0],fvi[1][1],fvi[1][2],0,0,0));
		//	right
		faces.add(new Face(group,fvi[2][0],fvi[2][1],fvi[2][2],1,1,1));
		faces.add(new Face(group,fvi[3][0],fvi[3][1],fvi[3][2],1,1,1));
		//	top
		faces.add(new Face(group,fvi[4][0],fvi[4][1],fvi[4][2],2,2,2));
		faces.add(new Face(group,fvi[5][0],fvi[5][1],fvi[5][2],2,2,2));
		//	bottom
		faces.add(new Face(group,fvi[6][0],fvi[6][1],fvi[6][2],3,3,3));
		faces.add(new Face(group,fvi[7][0],fvi[7][1],fvi[7][2],3,3,3));
		//	near
		faces.add(new Face(group,fvi[8][0],fvi[8][1],fvi[8][2],4,4,4));
		faces.add(new Face(group,fvi[9][0],fvi[9][1],fvi[9][2],4,4,4));
		//	far
		faces.add(new Face(group,fvi[10][0],fvi[10][1],fvi[10][2],5,5,5));
		faces.add(new Face(group,fvi[11][0],fvi[11][1],fvi[11][2],5,5,5));

		// set up faces entity
		groupFacesEntity.setFaces(faces);

		// setup group vertex data
		group.setVertices(vertices);
		group.setNormals(normals);
		group.setFacesEntities(groupFacesEntities);

		// determine features
		group.determineFeatures();

		// register group
		model.getGroups().put("group", group);
		model.getSubGroups().put("group", group);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Creates a model from oriented bounding box
	 * @param sphere
	 * @param id
	 * @param number of x segments
	 * @param number of y segments
	 * @return model
	 */
	public static Model createSphereModel(Sphere sphere, String id, int segmentsX, int segmentsY) {
		// sphere properties
		float radius = sphere.radius;
		Vector3 center = sphere.center;

		// ground model
		Model model = new Model(id, id);

		// material
		Material material = new Material("tdme.primitive.material");
		material.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		material.getDiffuseColor().set(1.0f, 0.5f, 0.5f, 0.5f);
		model.getMaterials().put(material.getId(), material);

		// group
		Group group = new Group(model, "group", "group");

		// faces entity
		FacesEntity groupFacesEntity = new FacesEntity(group, "faces entity");
		groupFacesEntity.setMaterial(material);

		// faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntity);

		// vertices
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		for (int i = 0; i < (segmentsY + 1) * segmentsX; i++) vertices.add(null);
		for (int ySegment = 0; ySegment <= segmentsY; ySegment++)
		for (int xSegment = 0; xSegment < segmentsX; xSegment++) {
			Vector3 vertex =
				new Vector3(
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.cos(Math.PI * 2 * xSegment / segmentsX)),
					(float)(Math.cos(Math.PI * ySegment / segmentsY)),
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.sin(Math.PI * 2 * xSegment / segmentsX))
				).
				scale(radius).
				add(center);
			vertices.set(ySegment * segmentsX + xSegment, vertex);
		}

		// normals
		ArrayList<Vector3> normals = new ArrayList<Vector3>();

		// faces
		ArrayList<Face> faces = new ArrayList<Face>();
		int vi0, vi1, vi2;
		int ni;
		for (int y = 0; y <= segmentsY; y++) {
			for (int x = 0; x < segmentsX; x++) {
				vi0 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				vi1 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				vi2 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				ni = normals.size();
				for (Vector3 normal: ModelHelper.computeNormals(new Vector3[]{vertices.get(vi0), vertices.get(vi1), vertices.get(vi2)})) {
					normals.add(normal);
				}
				faces.add(new Face(group, vi0, vi1, vi2, ni + 0, ni + 1, ni + 2));
				vi0 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				vi1 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				vi2 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				ni = normals.size();
				for (Vector3 normal: ModelHelper.computeNormals(new Vector3[]{vertices.get(vi0), vertices.get(vi1), vertices.get(vi2)})) {
					normals.add(normal);
				}
				faces.add(new Face(group, vi0, vi1, vi2, ni + 0, ni + 1, ni + 2));
			}
		}

		// set up faces entity
		groupFacesEntity.setFaces(faces);

		// setup group vertex data
		group.setVertices(vertices);
		group.setNormals(normals);
		group.setFacesEntities(groupFacesEntities);

		// determine features
		group.determineFeatures();

		// register group
		model.getGroups().put("group", group);
		model.getSubGroups().put("group", group);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Creates a model from capsule
	 * @param sphere
	 * @param id
	 * @param number of x segments
	 * @param number of y segments
	 * @return model
	 */
	public static Model createCapsuleModel(Capsule capsule, String id, int segmentsX, int segmentsY) {
		// capsule properties
		float radius = capsule.radius;
		Vector3 a = capsule.a;
		Vector3 b = capsule.b;

		// rotation quaternion
		Quaternion rotationQuaternion = new Quaternion();
		rotationQuaternion.identity();

		// angle between a and b
		Vector3 yAxis = new Vector3(0f,-1f,0f);
		Vector3 abNormalized = a.clone().sub(b).normalize();
		float[] abNormalizedVectorXYZ = abNormalized.getArray();
		Vector3 rotationAxis;
		if (Math.abs(abNormalizedVectorXYZ[0]) < MathTools.EPSILON && Math.abs(abNormalizedVectorXYZ[2]) < MathTools.EPSILON) {
			rotationAxis = new Vector3(abNormalizedVectorXYZ[1], 0f, 0f);
		} else {
			rotationAxis = Vector3.computeCrossProduct(yAxis, abNormalized).normalize();
		}
		float angle = Vector3.computeAngle(yAxis, abNormalized, yAxis);
		rotationQuaternion.rotate(angle, rotationAxis);

		// ground model
		Model model = new Model(id, id);

		// material
		Material material = new Material("tdme.primitive.material");
		material.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		material.getDiffuseColor().set(1.0f, 0.5f, 0.5f, 0.5f);
		model.getMaterials().put(material.getId(), material);

		// group
		Group group = new Group(model, "group", "group");

		// faces entity
		FacesEntity groupFacesEntity = new FacesEntity(group, "faces entity");
		groupFacesEntity.setMaterial(material);

		// faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntity);

		// vertices
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		for (int i = 0; i < (segmentsY + 2) * segmentsX; i++) vertices.add(null);
		//	top half sphere
		for (int ySegment = segmentsY / 2; ySegment <= segmentsY; ySegment++)
		for (int xSegment = 0; xSegment < segmentsX; xSegment++) {
			Vector3 vertex = new Vector3(); 
			rotationQuaternion.multiply(
				new Vector3(
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.cos(Math.PI * 2 * xSegment / segmentsX)),
					(float)(Math.cos(Math.PI * ySegment / segmentsY)),
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.sin(Math.PI * 2 * xSegment / segmentsX))
				),
				vertex
			);
			vertex.scale(radius);
			vertex.add(a);
			vertices.set(ySegment * segmentsX + xSegment, vertex);
		}

		//	bottom half sphere
		for (int i = 0; i < (segmentsY + 1) * segmentsX; i++) vertices.add(null);
		for (int ySegment = 0; ySegment <= segmentsY / 2; ySegment++)
		for (int xSegment = 0; xSegment < segmentsX; xSegment++) {
			Vector3 vertex = new Vector3();
			rotationQuaternion.multiply(
				new Vector3(
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.cos(Math.PI * 2 * xSegment / segmentsX)),
					(float)(Math.cos(Math.PI * ySegment / segmentsY)),
					(float)(Math.sin(Math.PI * ySegment / segmentsY) * Math.sin(Math.PI * 2 * xSegment / segmentsX))
				),
				vertex
			);
			vertex.scale(radius);
			vertex.add(b);
			vertices.set(ySegment * segmentsX + xSegment, vertex);
		}

		// normals
		ArrayList<Vector3> normals = new ArrayList<Vector3>();

		// faces
		ArrayList<Face> faces = new ArrayList<Face>();
		int vi0, vi1, vi2;
		int ni;
		for (int y = 0; y <= segmentsY + 1; y++) {
			for (int x = 0; x < segmentsX; x++) {
				vi0 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				vi1 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				vi2 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				ni = normals.size();
				for (Vector3 normal: ModelHelper.computeNormals(new Vector3[]{vertices.get(vi0), vertices.get(vi1), vertices.get(vi2)})) {
					normals.add(normal);
				}
				faces.add(new Face(group, vi0, vi1, vi2, ni + 0, ni + 1, ni + 2));
				vi0 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 0) % (segmentsX));
				vi1 = ((y + 0) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				vi2 = ((y + 1) % (segmentsY + 1)) * segmentsX + ((x + 1) % (segmentsX));
				ni = normals.size();
				for (Vector3 normal: ModelHelper.computeNormals(new Vector3[]{vertices.get(vi0), vertices.get(vi1), vertices.get(vi2)})) {
					normals.add(normal);
				}
				faces.add(new Face(group, vi0, vi1, vi2, ni + 0, ni + 1, ni + 2));
			}
		}

		// set up faces entity
		groupFacesEntity.setFaces(faces);

		// setup group vertex data
		group.setVertices(vertices);
		group.setNormals(normals);
		group.setFacesEntities(groupFacesEntities);

		// determine features
		group.determineFeatures();

		// register group
		model.getGroups().put("group", group);
		model.getSubGroups().put("group", group);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Set up a convex mesh model
	 * @param model
	 */
	public static void setupConvexMeshModel(Model model) {
		// material
		Material material = new Material("tdme.primitive.material");
		material.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		material.getDiffuseColor().set(1.0f, 0.5f, 0.5f, 0.5f);
		model.getMaterials().put(material.getId(), material);
		setupConvexMeshMaterial(model.getSubGroups(), material);
	}

	/**
	 * Set up convex mesh material
	 * @param groups
	 */
	private static void setupConvexMeshMaterial(HashMap<String, Group> groups, Material material) {
		for (Group group: groups.getValuesIterator()) {
			for (FacesEntity faceEntity: group.getFacesEntities()) {
				faceEntity.setMaterial(material);
			}
			// process sub groups
			setupConvexMeshMaterial(group.getSubGroups(), material);
		}
	}

	/**
	 * Creates a model from bounding volume
	 * @param bounding box
	 * @param id
	 * @return model
	 */
	public static Model createModel(BoundingVolume boundingVolume, String id) {
		if (boundingVolume instanceof BoundingBox) {
			return PrimitiveModel.createBoundingBoxModel((BoundingBox)boundingVolume, id);
		} else
		if (boundingVolume instanceof OrientedBoundingBox) {
			return PrimitiveModel.createOrientedBoundingBoxModel((OrientedBoundingBox)boundingVolume, id);
		} else
		if (boundingVolume instanceof Sphere) {
			return PrimitiveModel.createSphereModel((Sphere)boundingVolume, id, SPHERE_SEGMENTS_X, SPHERE_SEGMENTS_Y);
		} else
		if (boundingVolume instanceof Capsule) {
			return PrimitiveModel.createCapsuleModel((Capsule)boundingVolume, id, CAPSULE_SEGMENTS_X, CAPSULE_SEGMENTS_Y);
		} else {
			System.out.println("PrimitiveModel::createModel(): unsupported bounding volume");
			return null;
		}
	}


}

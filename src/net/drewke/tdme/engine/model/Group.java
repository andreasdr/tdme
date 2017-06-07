package net.drewke.tdme.engine.model;

import java.util.Arrays;

import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.HashMap;

/**
 * 3d object group
 * @author andreas.drewke
 * @version $Id$
 */
public final class Group {

	private Model model;
	private Group parentGroup;
	private String id;
	private String name;
	private boolean isJoint;
	private Matrix4x4 transformationsMatrix;
	private Vector3[] vertices;
	private Vector3[] normals;
	private TextureCoordinate[] textureCoordinates;
	private Vector3[] tangents;
	private Vector3[] bitangents;
	private Animation animation;
	private Skinning skinning;
	private FacesEntity[] facesEntities;
	private HashMap<String, Group> subGroups;

	/**
	 * Public constructor
	 * @param model
	 * @param parent group 
	 * @param id
	 * @param name
	 */
	public Group(Model model, Group parentGroup, String id, String name) {
		this.model = model;
		this.parentGroup = parentGroup;
		this.id = id;
		this.name = name;
		this.transformationsMatrix = new Matrix4x4().identity();
		this.vertices = new Vector3[0];
		this.normals = new Vector3[0];
		this.textureCoordinates = null;
		this.tangents = null;
		this.bitangents = null;
		this.animation = null;
		this.skinning = null;
		this.facesEntities = new FacesEntity[0];
		this.subGroups = new HashMap<String, Group>();
	}

	/**
	 * @return model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return parent group
	 */
	public Group getParentGroup() {
		return parentGroup;
	}

	/**
	 * Returns id
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return group's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return if this group is a joint/bone
	 */
	public boolean isJoint() {
		return isJoint;
	}

	/**
	 * Sets up if this group is a joint or not 
	 * @param isbone
	 */
	public void setJoint(boolean isJoint) {
		this.isJoint = isJoint;
	}

	/**
	 * @return transformations matrix related to parent group
	 */
	public Matrix4x4 getTransformationsMatrix() {
		return transformationsMatrix;
	}

	/**
	 * Set up vertices
	 * @param vertices
	 */
	public void setVertices(ArrayList<Vector3> vertices) {
		this.vertices = vertices.toArray(new Vector3[vertices.size()]);
	}

	/**
	 * @return vertices
	 */
	public Vector3[] getVertices() {
		return vertices;
	}
	
	/**
	 * Set vertices
	 * @param vertices
	 */
	public void setVertices(Vector3[] vertices) {
		this.vertices = vertices;
	}

	/**
	 * @return normals
	 */
	public Vector3[] getNormals() {
		return normals;
	}

	/**
	 * Set normals
	 * @param normals
	 */
	public void setNormals(Vector3[] normals) {
		this.normals = normals;
	}

	/**
	 * Set up normals
	 * @param normals
	 */
	public void setNormals(ArrayList<Vector3> normals) {
		this.normals = normals.toArray(new Vector3[normals.size()]);
	}

	/**
	 * @return texture coordinates or null (optional)
	 */
	public TextureCoordinate[] getTextureCoordinates() {
		return textureCoordinates;
	}

	/**
	 * Set texture coordinates
	 * @param texture coordinates
	 */
	public void setTextureCoordinates(TextureCoordinate[] textureCoordinates) {
		this.textureCoordinates = textureCoordinates;
	}

	/**
	 * Set texture coordinates
	 * @param textureCoordinates
	 */
	public void setTextureCoordinates(ArrayList<TextureCoordinate> textureCoordinates) {
		this.textureCoordinates = textureCoordinates.toArray(new TextureCoordinate[textureCoordinates.size()]);
	}

	/**
	 * @return tangents
	 */
	public Vector3[] getTangents() {
		return tangents;
	}

	/**
	 * Set tangents
	 * @param tangents
	 */
	public void setTangents(Vector3[] tangents) {
		this.tangents = tangents;
	}

	/**
	 * Set up tangents
	 * @param tangents
	 */
	public void setTangents(ArrayList<Vector3> tangents) {
		this.tangents = tangents.toArray(new Vector3[tangents.size()]);
	}

	/**
	 * @return bitangents
	 */
	public Vector3[] getBitangents() {
		return bitangents;
	}

	/**
	 * Set bitangents
	 * @param bitangents
	 */
	public void setBitangents(Vector3[] bitangents) {
		this.bitangents = bitangents;
	}

	/**
	 * Set up bitangents
	 * @param bitangents
	 */
	public void setBitangents(ArrayList<Vector3> bitangents) {
		this.bitangents = bitangents.toArray(new Vector3[bitangents.size()]);
	}

	/**
	 * @return animation
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Creates an empty animation object
	 * @param frames
	 * @return animation
	 */
	public Animation createAnimation(int frames) {
		animation = new Animation(frames);
		return animation;
	}

	/**
	 * @return skinning or null
	 */
	public Skinning getSkinning() {
		return skinning;
	}

	/**
	 * Creates an empty skinning object
	 * @return skinning
	 */
	public Skinning createSkinning() {
		skinning = new Skinning();
		return skinning;
	}

	/**
	 * @return number of faces in group
	 */
	public int getFaceCount() {
		// determine face count
		int faceCount = 0;
		for (FacesEntity facesEntity: facesEntities) {
			faceCount+= facesEntity.getFaces().length;
		}
		return faceCount;
	}

	/**
	 * @return faces entities
	 */
	public FacesEntity[] getFacesEntities() {
		return facesEntities;
	}

	/**
	 * Set up faces entities
	 * @param faces entity
	 */
	public void setFacesEntities(ArrayList<FacesEntity> facesEntities) {
		this.facesEntities = facesEntities.toArray(new FacesEntity[facesEntities.size()]);
	}

	/**
	 * Set up faces entities
	 * @param faces entity
	 */
	public void setFacesEntities(FacesEntity[] facesEntities) {
		this.facesEntities = facesEntities;
	}

	/**
	 * @return sub sub groups of this group
	 */
	public HashMap<String, Group> getSubGroups() {
		return subGroups;
	}

	/**
	 * Returns a sub group by id
	 * @param groupId
	 * @return sub group or null
	 */
	public Group getSubGroupById(String groupId) {
		return subGroups.get(groupId);
	}

	/**
	 * Post set up faces
	 * TODO: move me into model helper
	 */
	public void determineFeatures() {
		// post set up faces
		for (FacesEntity facesEntity: facesEntities) {
			facesEntity.determineFeatures();
		}
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return
			"ObjectGroup [id=" + id +
			", name=" + name +
			", faces entities=" + Arrays.toString(facesEntities) +
			", vertices =" + Arrays.toString(vertices) +
			", normals =" + Arrays.toString(normals) +
			", texture coordinates =" + Arrays.toString(textureCoordinates) +
			", tangents =" + Arrays.toString(tangents) +
			", bitangents =" + Arrays.toString(bitangents) +
			", subgroups =" + subGroups +
			"]";
	}	

}

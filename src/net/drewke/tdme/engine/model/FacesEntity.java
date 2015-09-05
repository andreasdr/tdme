package net.drewke.tdme.engine.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Group faces entity
 * 	A group can have multiple entities containing faces and a applied material
 * @author Andreas Drewke
 * @version $Id$
 */
public final class FacesEntity {

	private String id;
	private Group group;
	private Material material;
	private Face[] faces;
	private boolean textureCoordinatesAvailable;
	private boolean tangentBitangentAvailable;

	/**
	 * Public constructor
	 * @param id 
	 * @param group
	 */
	public FacesEntity(Group group, String id) {
		this.id = id;
		this.group = group;
		this.material = null;
		this.faces = new Face[0];
		this.textureCoordinatesAvailable = false;
		this.tangentBitangentAvailable = false;
	}

	/**
	 * @return faces entity id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set up the entity's material
	 * @param material
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * @return entity's material
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * @return entity's faces
	 */
	public Face[] getFaces() {
		return faces;
	}

	/**
	 * Set up entity's faces
	 * @param faces
	 */
	public void setFaces(ArrayList<Face> faces) {
		this.faces = faces.toArray(new Face[faces.size()]);
		// determine if the hole entity has texture coordinates available
		this.textureCoordinatesAvailable = true;
		this.tangentBitangentAvailable = true;
	}

	/**
	 * Post set up faces
	 */
	public void determineFeatures() {
		textureCoordinatesAvailable = true;
		tangentBitangentAvailable = true;
		for (Face face: this.faces) {
			if (face.getTangentIndices() == null || face.getBitangentIndices() == null) {
				tangentBitangentAvailable = false;
			}
			if (face.getTextureCoordinateIndices() == null) {
				textureCoordinatesAvailable = false;
			}
		}		
	}

	/**
	 * @return if texture coordinates are available for the whole entity
	 */
	public boolean isTextureCoordinatesAvailable() {
		return textureCoordinatesAvailable;
	}
	
	/**
	 * @return if tangents and bitangents are available for the whole entity
	 */
	public boolean isTangentBitangentAvailable() {
		return tangentBitangentAvailable;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "FacesEntity [id=" + id + ", group=" + group.getName()
				+ ", material=" + material + ", faces="
				+ Arrays.toString(faces) + "]";
	}

}
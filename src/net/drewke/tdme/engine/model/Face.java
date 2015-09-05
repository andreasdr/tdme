package net.drewke.tdme.engine.model;

import java.util.Arrays;

import net.drewke.tdme.math.Vector3;

/**
 * Represents a object group face, consisting of vertex indices and texture coordinate indices
 * @author andreas.drewke
 * @version $Id$
 */
public final class Face {

	private Group group;
	private int vertexIndices[] = null;
	private int normalIndices[] = null;
	private int textureCoordinateIndices[] = null;
	private int tangentIndices[] = null;
	private int bitangentIndices[] = null;

	/**
	 * Public constructor, requires vertex, normals indices
	 * 	we only support triangulated faces
	 * @param model
	 * @param vertex index 0
	 * @param vertex index 1
	 * @param vertex index 2
	 * @param normal index 0
	 * @param normal index 1
	 * @param normal index 2
	 */
	public Face(Group group, int vi0, int vi1, int vi2, int ni0, int ni1, int ni2) {
		this.group = group;

		// store vertex indices
		vertexIndices = new int[3];
		vertexIndices[0] = vi0;
		vertexIndices[1] = vi1;
		vertexIndices[2] = vi2;

		// normal indices
		normalIndices = new int [3];
		normalIndices[0] = ni0;
		normalIndices[1] = ni1;
		normalIndices[2] = ni2;
	}

	/**
	 * Public constructor, requires vertex, normals indices, texture coordinate indices
	 * 	we only support triangulated faces
	 * @param model
	 * @param vertex index 0
	 * @param vertex index 1
	 * @param vertex index 2
	 * @param normal index 0
	 * @param normal index 1
	 * @param normal index 2
	 * @param texture coordinate index 0
	 * @param texture coordinate index 1
	 * @param texture coordinate index 2
	 */
	public Face(
		Group group,
		int vi0, int vi1, int vi2,
		int ni0, int ni1, int ni2,
		int vt0, int vt1, int vt2) {
		this.group = group;

		// store vertex indices
		vertexIndices = new int[3];
		vertexIndices[0] = vi0;
		vertexIndices[1] = vi1;
		vertexIndices[2] = vi2;

		// normal indices
		normalIndices = new int [3];
		normalIndices[0] = ni0;
		normalIndices[1] = ni1;
		normalIndices[2] = ni2;

		// texture coordinate indices
		textureCoordinateIndices = new int[3];
		textureCoordinateIndices[0] = vt0;
		textureCoordinateIndices[1] = vt1;
		textureCoordinateIndices[2] = vt2;
	}

	/**
	 * @return group
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * @return vertex indices
	 */
	public int[] getVertexIndices() {
		return vertexIndices;
	}

	/**
	 * @return normal indices
	 */
	public int[] getNormalIndices() {
		return normalIndices;
	}

	/**
	 * Set up optional texture coordinate indices
	 * @param vtX
	 * @param vtY
	 * @param vtZ
	 */
	public void setTextureCoordinateIndices(int vt0, int vt1, int vt2) {
		textureCoordinateIndices = new int[3];
		textureCoordinateIndices[0] = vt0;
		textureCoordinateIndices[1] = vt1;
		textureCoordinateIndices[2] = vt2;
	}

	/**
	 * @return texture coordinate indices or null (optional)
	 */
	public int[] getTextureCoordinateIndices() {
		return textureCoordinateIndices;
	}

	/**
	 * Set tangent indices
	 * @param ti0
	 * @param ti1
	 * @param ti2
	 */
	public void setTangentIndices(int ti0, int ti1, int ti2) {
		tangentIndices = new int[3];
		tangentIndices[0] = ti0;
		tangentIndices[1] = ti1;
		tangentIndices[2] = ti2;
	}

	/**
	 * @return tangent indices
	 */
	public int[] getTangentIndices() {
		return tangentIndices;
	}	

	/**
	 * Set bitangent indices
	 * @param bi0
	 * @param bi1
	 * @param bi2
	 */
	public void setBitangentIndices(int bi0, int bi1, int bi2) {
		bitangentIndices = new int[3];
		bitangentIndices[0] = bi0;
		bitangentIndices[1] = bi1;
		bitangentIndices[2] = bi2;
	}

	/**
	 * @return bi tangent indices
	 */
	public int[] getBitangentIndices() {
		return bitangentIndices;
	}	

	/**
	 * Prepared this face for indexed rendering
	 * @param new face vertex indices
	 */
	protected void setIndexedRenderingIndices(int[] faceVertexIndices) {
		vertexIndices = faceVertexIndices;
		normalIndices = faceVertexIndices;
		if (textureCoordinateIndices != null) textureCoordinateIndices = faceVertexIndices;
		if (tangentIndices != null && bitangentIndices != null) {
			tangentIndices = faceVertexIndices;
			bitangentIndices = faceVertexIndices;
		} else {
			tangentIndices = null;
			bitangentIndices = null;
		}
	}

	/**
	 * string representation
	 */
	public String toString() {
		return "vI = "
				+ Arrays.toString(vertexIndices)
				+ ", nI = " + Arrays.toString(normalIndices)
				+ ", vtI = "
				+ Arrays.toString(textureCoordinateIndices);
	}
	
}

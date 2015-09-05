package net.drewke.tdme.engine.model;

import net.drewke.tdme.math.Matrix4x4;

/**
 * Joint / Bone
 * @author andreas.drewke
 */
public final class Joint {

	private String groupId;
	private Matrix4x4 bindMatrix;

	/**
	 * Public constructor
	 * @param group id
	 * @param bind matrix
	 */
	public Joint(String groupId) {
		this.groupId = groupId;
		this.bindMatrix = new Matrix4x4().identity();
	}

	/**
	 * Associated group or bone id
	 * @return group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Bind matrix
	 * @return matrix
	 */
	public Matrix4x4 getBindMatrix() {
		return bindMatrix;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "Joint [groupId=" + groupId + ", bindMatrix=" + bindMatrix
				+ "]";
	}
	
}
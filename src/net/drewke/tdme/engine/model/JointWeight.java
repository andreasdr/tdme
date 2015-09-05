package net.drewke.tdme.engine.model;

/**
 * @author andreas.drewke
 * @version $Id$
 */
public final class JointWeight {

	private int jointIndex;
	private int weightIndex;

	/**
	 * Public constructor
	 * @param joint index
	 * @param weight index
	 */
	public JointWeight(int jointIndex, int weightIndex) {
		this.jointIndex = jointIndex;
		this.weightIndex = weightIndex;
	}

	/**
	 * @returns joint index
	 */
	public int getJointIndex() {
		return jointIndex;
	}

	/**
	 * @returns weight index
	 */
	public int getWeightIndex() {
		return weightIndex;
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "JointWeight [jointIndex=" + jointIndex + ", weightIndex="
				+ weightIndex + "]";
	}

}
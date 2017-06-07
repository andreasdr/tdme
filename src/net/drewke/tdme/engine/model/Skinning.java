package net.drewke.tdme.engine.model;

import java.util.Arrays;

import net.drewke.tdme.utils.ArrayList;

/**
 * Skinning definition for groups
 * @author andreas.drewke
 * @version $Id$
 */
public final class Skinning {

	// weights
	private float[] weights;

	// joints
	private Joint[] joints;

	// for each vertex we have a set of joints with a corresponding weight
	private JointWeight[][] verticesJointsWeights;

	/**
	 * Public constructor
	 */
	public Skinning() {
		weights = new float[0];
		joints = new Joint[0];
		verticesJointsWeights = new JointWeight[0][0];
	}

	/**
	 * @return weights
	 */
	public float[] getWeights() {
		return weights;
	}

	/**
	 * Set up weights
	 * @param weights
	 */
	public void setWeights(float[] weights) {
		this.weights = weights;
	}

	/**
	 * Set up weights
	 * @param weights
	 */
	public void setWeights(ArrayList<Float> weights) {
		this.weights = new float[weights.size()];
		for(int i = 0; i < this.weights.length; i++) {
			this.weights[i] = weights.get(i);
		}
	}

	/**
	 * @return all joints
	 */
	public Joint[] getJoints() {
		return joints;
	}

	/**
	 * Set up joints
	 * @param joints
	 */
	public void setJoints(Joint[] joints) {
		this.joints = joints;
	}

	/**
	 * Set up joints
	 * @param joints
	 */
	public void setJoints(ArrayList<Joint> joints) {
		this.joints = joints.toArray(new Joint[joints.size()]);
	}

	/**
	 * @return all vertex joints
	 */
	public JointWeight[][] getVerticesJointsWeights() {
		return verticesJointsWeights;
	}

	/**
	 * Set vertices joints weight
	 * @param verticesJointsWeights
	 */
	public void setVerticesJointsWeights(JointWeight[][] verticesJointsWeights) {
		this.verticesJointsWeights = verticesJointsWeights;
	}

	/**
	 * Sets up vertices joints weights 
	 * @param verticesJointsWeights
	 */
	public void setVerticesJointsWeights(ArrayList<ArrayList<JointWeight>> verticesJointsWeights) {
		this.verticesJointsWeights = new JointWeight[verticesJointsWeights.size()][];
		for (int i = 0; i < verticesJointsWeights.size(); i++) {
			this.verticesJointsWeights[i] = new JointWeight[verticesJointsWeights.get(i).size()];
			for (int j = 0; j < verticesJointsWeights.get(i).size(); j++) {
				this.verticesJointsWeights[i][j] = verticesJointsWeights.get(i).get(j);
			}
		}
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "Skinning [weights=" + Arrays.toString(weights) + ", joints="
				+ Arrays.toString(joints) + ", verticesJointsWeights="
				+ Arrays.toString(verticesJointsWeights) + "]";
	}

}

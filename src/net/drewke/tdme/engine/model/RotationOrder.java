package net.drewke.tdme.engine.model;

import net.drewke.tdme.math.Vector3;

/**
 * Rotation order
 * @author Andreas Drewke
 * @version $Id$
 */
public enum RotationOrder {

	XYZ(new Vector3(1f,0f,0f), new Vector3(0f,1f,0f), new Vector3(0f,0f,1f), 0,1,2, 0,1,2),
	YZX(new Vector3(0f,1f,0f), new Vector3(0f,0f,1f), new Vector3(1f,0f,0f), 1,2,0, 2,0,1),
	ZYX(new Vector3(0f,0f,1f), new Vector3(0f,1f,0f), new Vector3(1f,0f,0f), 2,1,0, 2,1,0);

	private Vector3 axis0;
	private Vector3 axis1;
	private Vector3 axis2;

	private int axis0VectorIndex;
	private int axis1VectorIndex;
	private int axis2VectorIndex;

	private int axisXIndex;
	private int axisYIndex;
	private int axisZIndex;

	/**
	 * Constructor
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param axis 0 vector (data) index
	 * @param axis 1 vector (data) index
	 * @param axis 2 vector (data) index
	 * @param axis X index
	 * @param axis Y index
	 * @param axis Z index
	 */
	private RotationOrder(
		Vector3 axis0,
		Vector3 axis1,
		Vector3 axis2,
		int axis0VectorIndex,
		int axis1VectorIndex,
		int axis2VectorIndex,
		int axisXIndex,
		int axisYIndex,
		int axisZIndex) {
		//
		this.axis0 = axis0;
		this.axis1 = axis1;
		this.axis2 = axis2;
		this.axis0VectorIndex = axis0VectorIndex;
		this.axis1VectorIndex = axis1VectorIndex;
		this.axis2VectorIndex = axis2VectorIndex;
		this.axisXIndex = axisXIndex;
		this.axisYIndex = axisYIndex;
		this.axisZIndex = axisZIndex;
	}

	/**
	 * @return axis 0
	 */
	public Vector3 getAxis0() {
		return axis0;
	}

	/**
	 * @return axis 1
	 */
	public Vector3 getAxis1() {
		return axis1;
	}

	/**
	 * @return axis 2
	 */
	public Vector3 getAxis2() {
		return axis2;
	}

	/**
	 * @return axis 0 vector index
	 */
	public int getAxis0VectorIndex() {
		return axis0VectorIndex;
	}

	/**
	 * @return axis 1 vector index
	 */
	public int getAxis1VectorIndex() {
		return axis1VectorIndex;
	}

	/**
	 * @return axis 2 vector index
	 */
	public int getAxis2VectorIndex() {
		return axis2VectorIndex;
	}

	/**
	 * @return axis x index
	 */
	public int getAxisXIndex() {
		return axisXIndex;
	}

	/**
	 * @return axis y index
	 */
	public int getAxisYIndex() {
		return axisYIndex;
	}

	/**
	 * @return axis z index
	 */
	public int getAxisZIndex() {
		return axisZIndex;
	}

}
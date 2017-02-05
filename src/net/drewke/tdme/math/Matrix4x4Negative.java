package net.drewke.tdme.math;

/**
 * Class to check if matrix is negative
 * @author Andreas Drewke
 * @version $Id$
 */
public class Matrix4x4Negative {

	protected Vector3 xAxis;
	protected Vector3 yAxis;
	protected Vector3 zAxis;
	protected Vector3 tmpAxis;

	/**
	 * Public constructor
	 */
	public Matrix4x4Negative() {
		xAxis = new Vector3();
		yAxis = new Vector3();
		zAxis = new Vector3();
		tmpAxis = new Vector3();
	}

	/**
	 * Check if matrix is negative
	 * @param matrix
	 * @return negative
	 */
	public boolean isNegative(Matrix4x4 matrix) {
		// check if negative scale and rotation
		float[] transformationsMatrixData = matrix.getArray();

		// copy into x,y,z axes
		System.arraycopy(transformationsMatrixData, 0, xAxis.getArray(), 0, 3);
		System.arraycopy(transformationsMatrixData, 4, yAxis.getArray(), 0, 3);
		System.arraycopy(transformationsMatrixData, 8, zAxis.getArray(), 0, 3);

		// check if inverted/negative transformation
		return Vector3.computeDotProduct(Vector3.computeCrossProduct(xAxis, yAxis, tmpAxis), zAxis) < 0.0f;
	}

}

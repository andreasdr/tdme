package net.drewke.tdme.engine;

import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * Transformations
 * @author Andreas Drewke
 * @version $Id$
 */
public class Transformations {

	protected Vector3 translation;
	protected Matrix4x4 translationMatrix;

	protected Vector3 scale;
	protected Matrix4x4 scaleMatrix;

	protected Rotations rotations;
	protected Matrix4x4 rotationsQuaternionMatrix;
	protected Matrix4x4 rotationsMatrix;
	protected Vector3 rotationsPivot;
	protected Matrix4x4 rotationsTranslationsMatrix;
	protected Vector3 pivot;

	protected Matrix4x4 transformationsMatrix;

	protected Vector3 xAxis;
	protected Vector3 yAxis;
	protected Vector3 zAxis;
	protected Vector3 tmpAxis;

	protected boolean negative;

	/**
	 * Public constructor
	 */
	public Transformations() {
		transformationsMatrix = new Matrix4x4().identity();
		translation = new Vector3();
		translationMatrix = new Matrix4x4();
		scale = new Vector3(1f,1f,1f);
		scaleMatrix = new Matrix4x4();
		rotations = new Rotations();
		rotationsQuaternionMatrix = new Matrix4x4();
		rotationsMatrix = new Matrix4x4().identity();
		rotationsPivot = new Vector3();
		rotationsTranslationsMatrix = new Matrix4x4().identity();
		pivot = new Vector3();
		xAxis = new Vector3();
		yAxis = new Vector3();
		zAxis = new Vector3();
		tmpAxis = new Vector3();
		negative = false;
	}

	/**
	 * @return object translation
	 */
	public Vector3 getTranslation() {
		return translation;
	}

	/** 
	 * @return object scale
	 */
	public Vector3 getScale() {
		return scale;
	}

	/**
	 * @return pivot or center of rotations
	 */
	public Vector3 getPivot() {
		return pivot;
	}

	/**
	 * @return object rotations
	 */
	public Rotations getRotations() {
		return rotations;
	}

	/**
	 * @return this transformations matrix
	 */
	public Matrix4x4 getTransformationsMatrix() {
		return transformationsMatrix; 
	}

	/** 
	 * @return Negative/Inverted transformation
	 */
	public boolean isNegative() {
		return negative;
	}

	/**
	 * Set up this transformations from given transformations
	 * @param transformations
	 */
	public void fromTransformations(Transformations transformations) {
		// translation
		translation.set(transformations.translation);

		// scale
		scale.set(transformations.scale);

		// rotations
		pivot.set(transformations.getPivot());
		int rotationIdx = 0;
		for(; rotationIdx < transformations.rotations.size(); rotationIdx++) {
			Rotation rotation = transformations.rotations.get(rotationIdx);

			// do we have a rotation to reuse?
			Rotation _rotation = rotationIdx < rotations.size()?rotations.get(rotationIdx):null;
			//	nope?
			if (_rotation == null) {
				// add it
				_rotation = new Rotation();
				rotations.add(_rotation);
			}
			// copy
			_rotation.fromRotation(rotation);
		}

		// remove unused rotations
		while (rotationIdx < rotations.size()) {
			rotations.remove(rotations.size() - 1);
		}

		// copy matrices
		translationMatrix.set(transformations.translationMatrix);
		scaleMatrix.set(transformations.scaleMatrix);
		transformationsMatrix.set(transformations.transformationsMatrix);
		negative = transformations.negative;
	}

	/**
	 * Computes transformation matrix
	 */
	public void update() {
		// transformation matrix identity
		transformationsMatrix.identity();

		// set up translation matrix
		translationMatrix.identity().translate(translation);

		// set up scale matrix
		scaleMatrix.identity().scale(scale);

		// create and multiply rotations
		rotations.update();

		// apply rotations
		rotationsMatrix.identity();
		rotationsPivot.set(pivot).scale(-1.0f);
		//	pivot
		rotationsMatrix.translate(rotationsPivot);
		//	rotatations
		rotations.quaternion.computeMatrix(rotationsQuaternionMatrix);
		rotationsMatrix.multiply(
			rotationsQuaternionMatrix
		);
		//	pivot
		rotationsTranslationsMatrix.identity().translate(pivot);
		rotationsMatrix.multiply(rotationsTranslationsMatrix);

		// apply to transformation matrices
		transformationsMatrix.multiply(scaleMatrix);

		//	apply to transformations matrix
		transformationsMatrix.multiply(rotationsMatrix);

		// apply translation
		transformationsMatrix.multiply(translationMatrix);
		
		//
		// check if negative scale and rotation
		float[] transformationsMatrixData = transformationsMatrix.getArray();

		// copy into x,y,z axes
		System.arraycopy(transformationsMatrixData, 0, xAxis.getArray(), 0, 3);
		System.arraycopy(transformationsMatrixData, 4, yAxis.getArray(), 0, 3);
		System.arraycopy(transformationsMatrixData, 8, zAxis.getArray(), 0, 3);

		// check if inverted/negative transformation
		negative = Vector3.computeDotProduct(Vector3.computeCrossProduct(xAxis, yAxis, tmpAxis), zAxis) < 0.0f;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Transformations [translation=" + translation
				+ ", translationMatrix=" + translationMatrix + ", scale="
				+ scale + ", scaleMatrix=" + scaleMatrix + ", rotations="
				+ rotations + ", transformationsMatrix="
				+ transformationsMatrix + "]";
	}

}

package net.drewke.tdme.engine;

import net.drewke.tdme.math.Quaternion;
import net.drewke.tdme.math.Vector3;

/**
 * Rotation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Rotation {

	public final static Vector3 X_AXIS = new Vector3(1f, 0f, 0f);
	public final static Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
	public final static Vector3 Z_AXIS = new Vector3(0f, 0f, 1f);

	private float angle;
	private Vector3 axis;
	private Quaternion quaternion;

	/**
	 * Public default constructor
	 */
	public Rotation() {
		this.angle = 0f;
		this.axis = new Vector3(0f, 0f, 0f);		
		this.quaternion = new Quaternion().identity();
	}

	/**
	 * Public constructor
	 * @param angle
	 * @param axis
	 */
	public Rotation(float angle, Vector3 axis) {
		this.angle = angle;
		this.axis = axis;
		this.quaternion = new Quaternion().identity();
	}

	/**
	 * @return angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @param angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * @return axis
	 */
	public Vector3 getAxix() {
		return axis;
	}

	/**
	 * @return quaternion
	 */
	public Quaternion getQuaternion() {
		return quaternion;
	}

	/**
	 * Sets up this rotation from another rotation
	 * @param rotation
	 */
	public void fromRotation(Rotation rotation) {
		angle = rotation.angle;
		axis.set(rotation.axis);
		quaternion.set(rotation.quaternion);
	}

	/**
	 * Sets up this rotation from quaternion, current quaternion will be lost, needs to get updated
	 * @param q
	 */
	public void fromQuaternion(Quaternion q) {
		quaternion.set(q);
		quaternion.normalize();
		float[] quaterionXYZ = quaternion.getArray();
		this.angle = 2f * (float)Math.acos(quaterionXYZ[3]) / 3.14159265f * 180.0f;
		float s = (float)Math.sqrt(1f - quaterionXYZ[3] * quaterionXYZ[3]);
		if (s < 0.001f) {
			this.axis.set(quaterionXYZ[0], -quaterionXYZ[1], quaterionXYZ[2]);
		} else {
			this.axis.set(quaterionXYZ[0] / s, -quaterionXYZ[1] / s, quaterionXYZ[2] / s);
		}
	}

	/**
	 * Computes rotation matrix
	 */
	public void update() {
		quaternion.identity();
		quaternion.rotate(angle, axis);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Rotation [angle=" + angle + ", axis=" + axis + ", quaternion="
				+ quaternion + "]";
	}

}
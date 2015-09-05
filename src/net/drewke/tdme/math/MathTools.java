package net.drewke.tdme.math;

/**
 * Additional mathematical functions
 * @author Andreas Drewke
 * @version $Id$
 */
public final class MathTools {

	public static final float EPSILON = 0.00001f;
	public static final float DEG2RAD = 3.141593f / 180;
	public static final float g = 9.80665f;

	/**
	 * Clamps a float value to min or max value
	 * @param value
	 * @param min value
	 * @param max value
	 * @return clamped value
	 */
	public static float clamp(float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Returns sign of value
	 * @param value
	 * @return -1 if value is negative or +1 if positive
	 */
	public static float sign(float value) {
		return value / Math.abs(value);
	}

	/**
	 * Do the square product
	 * @param value
	 * @return
	 */
	public static float square(float value) {
		return value * value;
	}

}

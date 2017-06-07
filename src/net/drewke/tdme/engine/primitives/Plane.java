package net.drewke.tdme.engine.primitives;

import net.drewke.tdme.math.Vector3;

/**
 * Plane
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Plane {

	protected Vector3 normal;
	protected float distance;

	/**
	 * Public default constructor
	 */
	public Plane() {
		normal = new Vector3();
		distance = 0f;
	}

	/**
	 * Public constructor
	 * @param normal
	 * @param distance
	 */
	public Plane(Vector3 normal, float distance) {
		this.normal = normal;
		this.distance = distance;
	}

	/**
	 * @return float distance from origin
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Set up distance from origin
	 * @param distance
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * @return normal
	 */
	public Vector3 getNormal() {
		return normal;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Plane [normal=" + normal + ", distance=" + distance + "]";
	}

}

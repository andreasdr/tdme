package net.drewke.tdme.engine;

import net.drewke.tdme.math.Quaternion;
import net.drewke.tdme.utils.ArrayList;

/**
 * Rotations
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Rotations {

	protected Quaternion quaternion;
	protected ArrayList<Rotation> rotations;

	public Rotations() {
		rotations = new ArrayList<Rotation>();
		quaternion = new Quaternion();
	}

	/**
	 * @return number of rotations
	 */
	public int size() {
		return rotations.size();
	}

	/**
	 * Get rotation at given index
	 * @param index
	 * @return rotation
	 */
	public Rotation get(int index) {
		return rotations.get(index);
	}

	/**
	 * Add rotation
	 * @param e
	 * @return
	 */
	public void add(Rotation rotation) {
		rotations.add(rotation);
	}

	/**
	 * Set up a rotation at given index
	 * @param index
	 * @param element
	 * @return rotation
	 */
	public void set(int index, Rotation rotation) {
		rotations.set(index, rotation);
	}

	/**
	 * Remove rotation at given index
	 * @param index
	 * @return rotation
	 */
	public Rotation remove(int index) {
		return rotations.remove(index);
	}

	/**
	 * @return rotation quaternion
	 */
	public Quaternion getQuaternion() {
		return quaternion;
	}

	/**
	 * Set up this transformations from given transformations
	 * @param transformations
	 */
	public void fromRotations(Rotations transformations) {
		// rotations
		int rotationIdx = 0;
		for(; rotationIdx < transformations.size(); rotationIdx++) {
			Rotation rotation = transformations.get(rotationIdx);

			// do we have a rotation to reuse?
			Rotation _rotation =
				rotationIdx < this.rotations.size()?
				this.rotations.get(rotationIdx):
				null;
			//	nope?
			if (_rotation == null) {
				// add it
				_rotation = new Rotation();
				this.rotations.add(_rotation);
			}
			// copy
			_rotation.fromRotation(rotation);
		}

		// remove unused rotations
		while (rotationIdx < this.rotations.size()) {
			this.rotations.remove(this.rotations.size() - 1);
		}

		// update quaternion
		this.quaternion.set(transformations.quaternion);
	}

	/**
	 * Update rotation quaternion
	 */
	public void update() {
		// create and multiply rotations
		quaternion.identity();
		for(int i = 0; i < rotations.size(); i++) {
			Rotation rotation = rotations.get(i);
			rotation.update();
			quaternion.multiply(rotation.getQuaternion());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Rotations [rotations=" + rotations + ", quaternion=" + quaternion + "]";
	}

}

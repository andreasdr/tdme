package net.drewke.tdme.engine.physics;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * Partition none implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionNone extends Partition {

	private ArrayList<RigidBody> bodies = new ArrayList<RigidBody>();
	private ArrayListIteratorMultiple<RigidBody> arrayListIteratorMultiple = new ArrayListIteratorMultiple<RigidBody>();

	/**
	 * Constructor
	 */
	protected PartitionNone() {
	}

	/**
	 * Reset
	 */
	protected void reset() {
	}

	/**
	 * Adds a object
	 * @param rigidBody
	 */
	protected void addRigidBody(RigidBody rigidBody) {
		if (bodies.contains(rigidBody)) return;
		bodies.add(rigidBody);
	}

	/**
	 * Updates a object
	 * @param rigidBody
	 */
	protected void updateRigidBody(RigidBody rigidBody) {
	}

	/**
	 * Removes a rigidBody
	 * @param rigidBody
	 */
	protected void removeRigidBody(RigidBody rigidBody) {
		bodies.remove(rigidBody);
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(BoundingVolume cbv) {
		arrayListIteratorMultiple.clear();
		arrayListIteratorMultiple.addArrayList(bodies);
		return arrayListIteratorMultiple;
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(Vector3 center) {
		arrayListIteratorMultiple.clear();
		arrayListIteratorMultiple.addArrayList(bodies);
		return arrayListIteratorMultiple;
	}

}

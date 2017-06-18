package net.drewke.tdme.engine.physics;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * Physics partition interface
 * @author Andreas Drewke
 * @version $Id$
 */
abstract public class Partition {

	/**
	 * Reset
	 */
	abstract protected void reset();

	/**
	 * Adds a object
	 * @param rigidBody
	 */
	abstract protected void addRigidBody(RigidBody rigidBody);

	/**
	 * Updates a object
	 * @param rigidBody
	 */
	abstract protected void updateRigidBody(RigidBody rigidBody);

	/**
	 * Removes a rigidBody
	 * @param rigidBody
	 */
	abstract protected void removeRigidBody(RigidBody rigidBody);

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	abstract public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(BoundingVolume cbv);

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	abstract public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(Vector3 center);

}

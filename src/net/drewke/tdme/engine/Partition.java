package net.drewke.tdme.engine;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIterator;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * Partition interface
 * @author Andreas Drewke
 * @version $Id$
 */
abstract public class Partition {

	/**
	 * Reset
	 */
	abstract protected void reset();

	/**
	 * Adds a entity
	 * @param entity
	 */
	abstract protected void addEntity(Entity entity);

	/**
	 * Updates a entity
	 * @param entity
	 */
	abstract protected void updateEntity(Entity entity);

	/**
	 * Removes a entity
	 * @param entity
	 */
	abstract protected void removeEntity(Entity entity);

	/**
	 * Get static bounding volumes iterator
	 * @param frustum
	 * @return vector iterator
	 */
	abstract public ArrayListIterator<Entity> getVisibleEntities(Frustum frustum);

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	abstract public ArrayListIteratorMultiple<Entity> getObjectsNearTo(BoundingVolume cbv);

	/**
	 * Get objects near to
	 * @param center
	 * @return objects near to center
	 */
	abstract public ArrayListIteratorMultiple<Entity> getObjectsNearTo(Vector3 center);

}

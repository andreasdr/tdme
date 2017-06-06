package net.drewke.tdme.engine;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * PartitionQuadTree interface
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
	 * Get visible entities
	 * @param frustum
	 * @return visible entities
	 */
	abstract public ArrayList<Entity> getVisibleEntities(Frustum frustum);

	/**
	 * Get objects near to bounding volume
	 * @param cbv
	 * @return objects near to cbv
	 */
	abstract public ArrayListIteratorMultiple<Entity> getObjectsNearTo(BoundingVolume cbv);

	/**
	 * Get objects near to given world position
	 * @param center
	 * @return objects near to given world position
	 */
	abstract public ArrayListIteratorMultiple<Entity> getObjectsNearTo(Vector3 center);

}

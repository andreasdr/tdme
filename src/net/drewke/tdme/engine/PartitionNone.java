package net.drewke.tdme.engine;

import java.util.ArrayList;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIterator;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * Partition none implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionNone extends Partition {

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayListIterator<Entity> arrayListIterator = new ArrayListIterator<Entity>(entities);
	private ArrayListIteratorMultiple<Entity> arrayListIteratorMultiple = new ArrayListIteratorMultiple<Entity>();

	/**
	 * Constructor
	 */
	public PartitionNone() {
		arrayListIteratorMultiple.addVector(entities);
	}

	/**
	 * Reset
	 */
	protected void reset() {
	}


	/**
	 * Adds a object
	 * @param entity
	 */
	protected void addEntity(Entity entity) {
		if (entities.contains(entity)) return;
		entities.add(entity);
	}

	/**
	 * Updates a object
	 * @param entity
	 */
	protected void updateEntity(Entity entity) {
	}

	/**
	 * Removes a entity
	 * @param entity
	 */
	protected void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	/**
	 * Get static bounding volumes iterator
	 * @param frustum
	 * @return vector iterator
	 */
	public ArrayListIterator<Entity> getVisibleEntities(Frustum frustum) {
		return arrayListIterator;
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(BoundingVolume cbv) {
		return arrayListIteratorMultiple;
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(Vector3 center) {
		return arrayListIteratorMultiple;
	}

}

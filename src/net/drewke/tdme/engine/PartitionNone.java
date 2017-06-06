package net.drewke.tdme.engine;

import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;

/**
 * PartitionQuadTree none implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionNone extends Partition {

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayListIteratorMultiple<Entity> arrayListIteratorMultiple = new ArrayListIteratorMultiple<Entity>();

	/**
	 * Constructor
	 */
	public PartitionNone() {
		arrayListIteratorMultiple.addArrayList(entities);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#reset()
	 */
	protected void reset() {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#addEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void addEntity(Entity entity) {
		if (entities.contains(entity)) return;
		entities.add(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#updateEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void updateEntity(Entity entity) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#removeEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getVisibleEntities(net.drewke.tdme.engine.Frustum)
	 */
	public ArrayList<Entity> getVisibleEntities(Frustum frustum) {
		return entities;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getObjectsNearTo(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(BoundingVolume cbv) {
		return arrayListIteratorMultiple;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getObjectsNearTo(net.drewke.tdme.math.Vector3)
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(Vector3 center) {
		return arrayListIteratorMultiple;
	}

}

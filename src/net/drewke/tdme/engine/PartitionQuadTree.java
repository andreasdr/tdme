package net.drewke.tdme.engine;

import java.util.ArrayList;

import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIterator;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;
import net.drewke.tdme.utils.Pool;

/**
 * PartitionQuadTree implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionQuadTree extends Partition {

	/**
	 * PartitionQuadTree tree node
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	private static class PartitionTreeNode {
		// partition size
		private float partitionSize;

		// x, y, z position
		private int x;
		private int y;
		private int z;

		// parent node
		private PartitionTreeNode parent;

		// our tree node has a bounding volume
		private BoundingBox bv;

		// it has sub nodes of quad tree nodes
		private ArrayList<PartitionTreeNode> subNodes;

		// it has sub nodes of quad tree nodes
		private HashMap<Key, PartitionTreeNode> subNodesByCoordinate;

		// or finally our static bounding volumes
		private ArrayList<Entity> partitionObjects;
	}

	private Key key;
	private ArrayListIteratorMultiple<Entity> entityIterator;

	private BoundingBox boundingBox;
	private Vector3 halfExtension;
	private Vector3 sideVector;
	private Vector3 forwardVector;
	private Vector3 upVector;
	private Pool<ArrayList<PartitionTreeNode>> objectPartitionNodesPool;
	private HashMap<String, ArrayList<PartitionTreeNode>> objectPartitionNodes;
	private ArrayList<Entity> visibleEntities;
	private ArrayListIterator<Entity> visibleEntitiesIterator;
	private PartitionTreeNode treeRoot = null;

	public final static float PARTITION_SIZE_MIN = 4f;
	public final static float PARTITION_SIZE_MID = 8f;
	public final static float PARTITION_SIZE_MAX = 16f;

	/**
	 * Constructor
	 */
	public PartitionQuadTree() {
		this.key = new Key();
		this.entityIterator = new ArrayListIteratorMultiple<Entity>();
		this.boundingBox = new BoundingBox();
		this.halfExtension = new Vector3();
		this.sideVector = new Vector3(1f,0f,0f);
		this.upVector = new Vector3(0f,1f,0f);
		this.forwardVector = new Vector3(0f,0f,1f);
		reset();
	}

	/**
	 * Reset
	 */
	protected void reset() {
		this.objectPartitionNodesPool = new Pool<ArrayList<PartitionTreeNode>>() {
			public ArrayList<PartitionTreeNode> instantiate() {
				return new ArrayList<PartitionTreeNode>();
			}
		};
		this.objectPartitionNodes = new HashMap<String, ArrayList<PartitionTreeNode>>();
		this.visibleEntities = new ArrayList<Entity>();
		this.visibleEntitiesIterator = new ArrayListIterator<Entity>(visibleEntities);
		this.treeRoot = new PartitionTreeNode();
		this.treeRoot.partitionSize = -1;
		this.treeRoot.x = -1;
		this.treeRoot.y = -1;
		this.treeRoot.z = -1;
		this.treeRoot.parent = null;
		this.treeRoot.bv = null;
		this.treeRoot.subNodes = new ArrayList<PartitionTreeNode>();
		this.treeRoot.subNodesByCoordinate = new HashMap<Key, PartitionTreeNode>();
		this.treeRoot.partitionObjects = null;		
	}

	/**
	 * Creates a partition
	 * @param parent
	 * @param x
	 * @param y
	 * @param z
	 * @param partition size
	 * @return partition tree node
	 */
	public PartitionTreeNode createPartition(PartitionTreeNode parent, int x, int y, int z, float partitionSize) {
		PartitionTreeNode node; 
		node = new PartitionTreeNode();
		node.partitionSize = partitionSize;
		node.x = x;
		node.y = y;
		node.z = z;
		node.parent = parent;
		node.bv =
			new BoundingBox(
				new Vector3(
					x * partitionSize,
					y * partitionSize,
					z * partitionSize
				),
				new Vector3(
					x * partitionSize + partitionSize,
					y * partitionSize + partitionSize,
					z * partitionSize + partitionSize
				)
			);
		node.subNodes = null;
		node.subNodesByCoordinate = null;
		node.partitionObjects = null;

		// register in parent sub nodes
		if (parent.subNodes == null) {
			parent.subNodes = new ArrayList<PartitionTreeNode>();
		}
		parent.subNodes.add(node);

		// register in parent sub nodes by coordinate 
		if (parent.subNodesByCoordinate == null) {
			parent.subNodesByCoordinate = new HashMap<Key, PartitionTreeNode>();
		}
		Key key = new Key();
		key.reset();
		key.append(node.x);
		key.append(",");
		key.append(node.y);
		key.append(",");
		key.append(node.z);
		parent.subNodesByCoordinate.put(key, node);

		// create sub nodes
		if (partitionSize > PARTITION_SIZE_MIN) {
			for (int _y = 0; _y < 2; _y++)
			for (int _x = 0; _x < 2; _x++)
			for (int _z = 0; _z < 2; _z++) {
				createPartition(
					node,
					(int)((x * partitionSize) / (partitionSize / 2f)) + _x,
					(int)((y * partitionSize) / (partitionSize / 2f)) + _y,
					(int)((z * partitionSize) / (partitionSize / 2f)) + _z,
					partitionSize / 2f
				);
			}
		} else {
			node.partitionObjects = new ArrayList<Entity>();
		}

		//
		return node;
	}

	/**
	 * Adds a object
	 * @param entity
	 */
	protected void addEntity(Entity entity) {
		// update if already exists
		ArrayList<PartitionTreeNode> objectPartitionsVector = objectPartitionNodes.get(entity.getId());
		if (objectPartitionsVector != null) {
			while (objectPartitionsVector.size() > 0) {
				int lastIdx = objectPartitionsVector.size() - 1; 
				objectPartitionsVector.get(lastIdx).partitionObjects.remove(entity);
				objectPartitionsVector.remove(lastIdx);
			}
		}

		// determine max first level partition dimension
		// convert to aabb for fast collision tests
		BoundingVolume cbv = entity.getBoundingBoxTransformed();
		Vector3 center = cbv.getCenter();
		halfExtension.set(
			cbv.computeDimensionOnAxis(sideVector) + 0.2f,
			cbv.computeDimensionOnAxis(upVector) + 0.2f,
			cbv.computeDimensionOnAxis(forwardVector) + 0.2f
		).scale(0.5f);
		boundingBox.getMin().set(center);
		boundingBox.getMin().sub(halfExtension);
		boundingBox.getMax().set(center);
		boundingBox.getMax().add(halfExtension);
		boundingBox.update();

		// find, create root nodes if not exists
		int minXPartition = (int)Math.floor(boundingBox.getMin().getX() / PARTITION_SIZE_MAX);
		int minYPartition = (int)Math.floor(boundingBox.getMin().getY() / PARTITION_SIZE_MAX);
		int minZPartition = (int)Math.floor(boundingBox.getMin().getZ() / PARTITION_SIZE_MAX);
		int maxXPartition = (int)Math.floor(boundingBox.getMax().getX() / PARTITION_SIZE_MAX);
		int maxYPartition = (int)Math.floor(boundingBox.getMax().getY() / PARTITION_SIZE_MAX);
		int maxZPartition = (int)Math.floor(boundingBox.getMax().getZ() / PARTITION_SIZE_MAX);
		for (int yPartition = minYPartition; yPartition <= maxYPartition; yPartition++)
		for (int xPartition = minXPartition; xPartition <= maxXPartition; xPartition++)
		for (int zPartition = minZPartition; zPartition <= maxZPartition; zPartition++) {

			// try to find node by key
			key.reset();
			key.append(xPartition);
			key.append(",");
			key.append(yPartition);
			key.append(",");
			key.append(zPartition);
			PartitionTreeNode node = treeRoot.subNodesByCoordinate.get(key);

			if (node == null) {
				node = createPartition(
					treeRoot,
					xPartition,
					yPartition,
					zPartition,
					PARTITION_SIZE_MAX
				);
			}
		}

		// add entity to tree
		addToPartitionTree(entity, boundingBox);
	}

	/**
	 * Updates a object
	 * @param entity
	 */
	protected void updateEntity(Entity entity) {
		addEntity(entity);
	}

	/**
	 * Removes a entity
	 * @param entity
	 */
	protected void removeEntity(Entity entity) {
		ArrayList<PartitionTreeNode> objectPartitionsVector = objectPartitionNodes.remove(entity.getId());
		if (objectPartitionsVector == null) {
			Console.println("FrustumPartition::removeObject3D(): '" + entity.getId() + "' not registered");
			return;
		}
		while (objectPartitionsVector.size() > 0) {
			int lastIdx = objectPartitionsVector.size() - 1;
			objectPartitionsVector.get(lastIdx).partitionObjects.remove(entity);
			objectPartitionsVector.remove(lastIdx);
		}
		objectPartitionNodesPool.release(objectPartitionsVector);
	}

	/**
	 * Do partition tree lookup
	 * @param frustum
	 * @param node
	 * @param visible entities
	 * @return
	 */
	private int doPartitionTreeLookUpVisibleObjects(Frustum frustum, PartitionTreeNode node, ArrayList<Entity> visibleEntities) {
		int lookUps = 1;
		// check if given cbv collides with partition node bv
		if (frustum.isVisible(node.bv) == false) {
			return lookUps;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionObjects != null) {
			for (int i = 0; i < node.partitionObjects.size(); i++) {
				Entity entity = node.partitionObjects.get(i);
				boolean hasEntity = false;
				for (int j = 0; j < visibleEntities.size(); j++) {
					if (visibleEntities.get(j) == entity) {
						hasEntity = true;
						break;
					}
				}
				if (hasEntity == true) continue;
				lookUps++;
				if (frustum.isVisible(entity.getBoundingBoxTransformed()) == false) continue;
				visibleEntities.add(entity);
			}
			return lookUps;
		} else
		if (node.subNodes != null) {
			// otherwise check sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				lookUps+= doPartitionTreeLookUpVisibleObjects(frustum, node.subNodes.get(i), visibleEntities);
			}
			return lookUps;
		}

		// done
		return lookUps;
	}

	/**
	 * Get static bounding volumes iterator
	 * @param frustum
	 * @return vector iterator
	 */
	public ArrayListIterator<Entity> getVisibleEntities(Frustum frustum) {
		// convert to aabb for fast collision tests
		visibleEntities.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpVisibleObjects(frustum, treeRoot.subNodes.get(i), visibleEntities); 
		}
		return visibleEntitiesIterator;
	}

	/**
	 * Do partition tree lookup
	 * @param node
	 * @param cbv
	 * @param cbvsIterator
	 */
	private void addToPartitionTree(PartitionTreeNode node, Entity entity, BoundingBox cbv) {
		// check if given cbv collides with partition node bv
		if (CollisionDetection.doCollideAABBvsAABBFast(node.bv, cbv) == false) {
			return;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionObjects != null) {
			node.partitionObjects.add(entity);
			ArrayList<PartitionTreeNode> objectPartitionNodesVector = objectPartitionNodes.get(entity.getId());
			if (objectPartitionNodesVector == null) {
				objectPartitionNodesVector = objectPartitionNodesPool.allocate();
				objectPartitionNodes.put(entity.getId(), objectPartitionNodesVector);
			}
			objectPartitionNodesVector.add(node); 
		} else
		if (node.subNodes != null) {
			// otherwise check sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				addToPartitionTree(node.subNodes.get(i), entity, cbv);
			}
		}
	}

	/**
	 * Add entity to tree
	 */
	protected void addToPartitionTree(Entity entity, BoundingBox cbv) {
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			addToPartitionTree(treeRoot.subNodes.get(i), entity, cbv); 
		}
	}

	/**
	 * Do partition tree lookup for near entities to cbv
	 * @param node
	 * @param cbv
	 * @param entity iterator
	 */
	private int doPartitionTreeLookUpNearEntities(PartitionTreeNode node, BoundingBox cbv, ArrayListIteratorMultiple<Entity> objectsIterator) {
		// check if given cbv collides with partition node bv
		if (CollisionDetection.doCollideAABBvsAABBFast(cbv, node.bv) == false) {
			return 1;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionObjects != null) {
			objectsIterator.addVector(node.partitionObjects);
			return 1;
		} else {
			int lookUps = 1;
			// otherwise check sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				lookUps+= doPartitionTreeLookUpNearEntities(node.subNodes.get(i), cbv, objectsIterator);
			}
			return lookUps;
		}
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(BoundingVolume cbv) {
		Vector3 center = cbv.getCenter();
		halfExtension.set(
			cbv.computeDimensionOnAxis(sideVector) + 0.2f,
			cbv.computeDimensionOnAxis(upVector) + 0.2f,
			cbv.computeDimensionOnAxis(forwardVector) + 0.2f
		).scale(0.5f);
		boundingBox.getMin().set(center);
		boundingBox.getMin().sub(halfExtension);
		boundingBox.getMax().set(center);
		boundingBox.getMax().add(halfExtension);
		boundingBox.update();
		entityIterator.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpNearEntities(treeRoot.subNodes.get(i), boundingBox, entityIterator); 
		}
		return entityIterator;
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<Entity> getObjectsNearTo(Vector3 center) {
		halfExtension.set(
			0.2f,
			0.2f,
			0.2f
		).scale(0.5f);
		boundingBox.getMin().set(center);
		boundingBox.getMin().sub(halfExtension);
		boundingBox.getMax().set(center);
		boundingBox.getMax().add(halfExtension);
		boundingBox.update();
		entityIterator.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpNearEntities(treeRoot.subNodes.get(i), boundingBox, entityIterator); 
		}
		return entityIterator;
	}

}

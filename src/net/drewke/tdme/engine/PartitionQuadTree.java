package net.drewke.tdme.engine;

import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;
import net.drewke.tdme.utils.Pool;

/**
 * Partition oct tree implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionQuadTree extends Partition {

	/**
	 * Partition oct tree node
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

		// node bounding volume
		private BoundingBox bv;

		// sub nodes of oct tree nodes
		private ArrayList<PartitionTreeNode> subNodes;

		// sub nodes of oct tree nodes by partition coordinate, only used in root node
		private HashMap<Key, PartitionTreeNode> subNodesByCoordinate;

		// or finally our partition entities
		private ArrayList<Entity> partitionEntities;
	}

	private Key key;
	private ArrayListIteratorMultiple<Entity> entityIterator;
	private BoundingBox boundingBox;
	private Vector3 halfExtension;
	private Vector3 sideVector;
	private Vector3 forwardVector;
	private Vector3 upVector;
	private Pool<ArrayList<PartitionTreeNode>> entityPartitionNodesPool;
	private Pool<BoundingBox> boundingBoxPool;
	private Pool<PartitionTreeNode> partitionTreeNodePool;
	private Pool<ArrayList<PartitionTreeNode>> subNodesPool;
	private Pool<ArrayList<Entity>> partitionEntitiesPool;
	private Pool<Key> keyPool;
	private HashMap<String, ArrayList<PartitionTreeNode>> entityPartitionNodes;
	private ArrayList<Entity> visibleEntities;
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#reset()
	 */
	protected void reset() {
		this.entityPartitionNodesPool = new Pool<ArrayList<PartitionTreeNode>>() {
			protected ArrayList<PartitionTreeNode> instantiate() {
				return new ArrayList<PartitionTreeNode>();
			}
		};
		this.boundingBoxPool = new Pool<BoundingBox>() {
			protected BoundingBox instantiate() {
				return new BoundingBox();
			}
		};
		this.partitionTreeNodePool = new Pool<PartitionQuadTree.PartitionTreeNode>() {
			protected PartitionTreeNode instantiate() {
				return new PartitionTreeNode();
			}
		};
		this.subNodesPool = new Pool<ArrayList<PartitionTreeNode>>() {
			protected ArrayList<PartitionTreeNode> instantiate() {
				return new ArrayList<PartitionTreeNode>();
			}
			
		};
		this.partitionEntitiesPool = new Pool<ArrayList<Entity>>() {
			protected ArrayList<Entity> instantiate() {
				return new ArrayList<Entity>();
			}
		};
		this.keyPool = new Pool<Key>() {
			protected Key instantiate() {
				return new Key();
			}
		};
		this.entityPartitionNodes = new HashMap<String, ArrayList<PartitionTreeNode>>();
		this.visibleEntities = new ArrayList<Entity>();
		this.treeRoot = new PartitionTreeNode();
		this.treeRoot.partitionSize = -1;
		this.treeRoot.x = -1;
		this.treeRoot.y = -1;
		this.treeRoot.z = -1;
		this.treeRoot.parent = null;
		this.treeRoot.bv = null;
		this.treeRoot.subNodes = new ArrayList<PartitionTreeNode>();
		this.treeRoot.subNodesByCoordinate = new HashMap<Key, PartitionTreeNode>();
		this.treeRoot.partitionEntities = null;		
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
		PartitionTreeNode node = partitionTreeNodePool.allocate();
		node.partitionSize = partitionSize;
		node.x = x;
		node.y = y;
		node.z = z;
		node.parent = parent;
		node.bv = boundingBoxPool.allocate();
		node.bv.getMin().set(
			x * partitionSize,
			y * partitionSize,
			z * partitionSize
		);
		node.bv.getMax().set(
			x * partitionSize + partitionSize,
			y * partitionSize + partitionSize,
			z * partitionSize + partitionSize
		);
		node.bv.update();
		node.subNodes = null;
		node.subNodesByCoordinate = null;
		node.partitionEntities = null;

		// register in parent sub nodes
		if (parent.subNodes == null) {
			parent.subNodes = subNodesPool.allocate();
		}
		parent.subNodes.add(node);

		// register in parent sub nodes by coordinate, if root node
		if (parent == treeRoot) {
			Key key = keyPool.allocate();
			key.reset();
			key.append(node.x);
			key.append(",");
			key.append(node.y);
			key.append(",");
			key.append(node.z);
			parent.subNodesByCoordinate.put(key, node);
		}

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
			node.partitionEntities = partitionEntitiesPool.allocate();
		}

		//
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#addEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void addEntity(Entity entity) {
		// update if already exists
		ArrayList<PartitionTreeNode> thisEntityPartitions = entityPartitionNodes.get(entity.getId());
		if (thisEntityPartitions != null && thisEntityPartitions.isEmpty() == false) {
			removeEntity(entity);
		}

		// frustum bounding box
		BoundingBox boundingBox = entity.getBoundingBoxTransformed();

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
			// check if first level node has been created already
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#updateEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void updateEntity(Entity entity) {
		addEntity(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#removeEntity(net.drewke.tdme.engine.Entity)
	 */
	protected void removeEntity(Entity entity) {
		// check if we have entity in quad tree
		ArrayList<PartitionTreeNode> objectPartitionsVector = entityPartitionNodes.remove(entity.getId());
		if (objectPartitionsVector == null || objectPartitionsVector.isEmpty() == true) {
			Console.println("PartitionOctTree::removeEntity(): '" + entity.getId() + "' not registered");
			return;
		}
		while (objectPartitionsVector.size() > 0) {
			// remove object from assigned partitions
			int lastIdx = objectPartitionsVector.size() - 1;
			PartitionTreeNode partitionTreeNode = objectPartitionsVector.get(lastIdx);
			ArrayList<Entity> partitionObjects = partitionTreeNode.partitionEntities;
			partitionObjects.remove(entity);
			objectPartitionsVector.remove(lastIdx);

			// check if whole top level partition is empty
			if (partitionObjects.isEmpty() == true) {
				PartitionTreeNode rootPartitionTreeNode = partitionTreeNode.parent.parent;
				// yep, remove it
				if (isPartitionNodeEmpty(rootPartitionTreeNode) == true) {
					removePartitionNode(rootPartitionTreeNode);
					treeRoot.subNodes.remove(rootPartitionTreeNode);
					key.reset();
					key.append(rootPartitionTreeNode.x);
					key.append(",");
					key.append(rootPartitionTreeNode.y);
					key.append(",");
					key.append(rootPartitionTreeNode.z);
					keyPool.release(treeRoot.subNodesByCoordinate.getKey(key));
					treeRoot.subNodesByCoordinate.remove(key);
				}
			}
		}
		entityPartitionNodesPool.release(objectPartitionsVector);
	}

	/**
	 * Is partition empty
	 * @param node
	 * @return partition empty
	 */
	private boolean isPartitionNodeEmpty(PartitionTreeNode node) {
		// lowest level node has objects attached?
		if (node.partitionEntities != null) {
			return node.partitionEntities.size() == 0;
		} else {
			// otherwise check top level node sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				if (isPartitionNodeEmpty(node.subNodes.get(i)) == false) return false;
			}
			return true;
		}
	}

	/**
	 * Remove partition node, should be empty
	 * @param node
	 */
	private void removePartitionNode(PartitionTreeNode node) {
		// lowest level node has objects attached?
		if (node.partitionEntities != null) {
			if (node.partitionEntities.size() > 0) {
				Console.println("PartitionOctTree::removePartitionNode(): partition has objects attached!!!");
				node.partitionEntities.clear();
			}
			partitionEntitiesPool.release(node.partitionEntities);
			node.partitionEntities = null;
		} else {
			// otherwise check top level node sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				PartitionTreeNode subNode = node.subNodes.get(i);
				removePartitionNode(subNode);
			}
			// release sub nodes
			node.subNodes.clear();
			subNodesPool.release(node.subNodes);
			node.subNodes = null;
		}
		// release bv
		boundingBoxPool.release(node.bv);
		node.bv = null;
		// release node itself
		partitionTreeNodePool.release(node);
	}

	/**
	 * Do partition tree lookup
	 * @param frustum
	 * @param node
	 * @param visible entities
	 * @return number of look ups
	 */
	private int doPartitionTreeLookUpVisibleObjects(Frustum frustum, PartitionTreeNode node, ArrayList<Entity> visibleEntities) {
		int lookUps = 1;
		// check if given cbv collides with partition node bv
		if (frustum.isVisible(node.bv) == false) {
			return lookUps;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionEntities != null) {
			for (int i = 0; i < node.partitionEntities.size(); i++) {
				Entity entity = node.partitionEntities.get(i);
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getVisibleEntities(net.drewke.tdme.engine.Frustum)
	 */
	public ArrayList<Entity> getVisibleEntities(Frustum frustum) {
		visibleEntities.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpVisibleObjects(frustum, treeRoot.subNodes.get(i), visibleEntities); 
		}
		return visibleEntities;
	}

	/**
	 * Do partition tree lookup
	 * @param node
	 * @param cbv
	 * @param cbvsIterator
	 */
	private void addToPartitionTree(PartitionTreeNode node, Entity entity, BoundingBox cbv) {
		if (CollisionDetection.doCollideAABBvsAABBFast(node.bv, cbv) == false) {
			return;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionEntities != null) {
			node.partitionEntities.add(entity);
			ArrayList<PartitionTreeNode> objectPartitionNodesVector = entityPartitionNodes.get(entity.getId());
			if (objectPartitionNodesVector == null) {
				objectPartitionNodesVector = entityPartitionNodesPool.allocate();
				entityPartitionNodes.put(entity.getId(), objectPartitionNodesVector);
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
	private void addToPartitionTree(Entity entity, BoundingBox cbv) {
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
	private int doPartitionTreeLookUpNearEntities(PartitionTreeNode node, BoundingBox cbv, ArrayListIteratorMultiple<Entity> entityIterator) {
		// check if given cbv collides with partition node bv
		if (CollisionDetection.doCollideAABBvsAABBFast(cbv, node.bv) == false) {
			return 1;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionEntities != null) {
			entityIterator.addArrayList(node.partitionEntities);
			return 1;
		} else {
			int lookUps = 1;
			// otherwise check sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				lookUps+= doPartitionTreeLookUpNearEntities(node.subNodes.get(i), cbv, entityIterator);
			}
			return lookUps;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getObjectsNearTo(net.drewke.tdme.engine.primitives.BoundingVolume)
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Partition#getObjectsNearTo(net.drewke.tdme.math.Vector3)
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

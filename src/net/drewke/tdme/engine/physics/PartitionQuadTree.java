package net.drewke.tdme.engine.physics;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.ArrayListIteratorMultiple;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;
import net.drewke.tdme.utils.Pool;

/**
 * Partition quad tree implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class PartitionQuadTree {

	/**
	 * Partition tree node
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	private final static class PartitionTreeNode {
		// partition size
		private float partitionSize;

		// x, y position
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
		private ArrayList<RigidBody> partitionRidigBodies;
	}

	private Key key;
	private ArrayListIteratorMultiple<RigidBody> rigidBodyIterator;

	private BoundingBox boundingBox;
	private Vector3 halfExtension;
	private Vector3 sideVector;
	private Vector3 forwardVector;
	private Vector3 upVector;
	private Pool<ArrayList<PartitionTreeNode>> rigidBodyPartitionNodesPool;
	private HashMap<String, ArrayList<PartitionTreeNode>> rigidBodyPartitionNodes;
	private PartitionTreeNode treeRoot = null;

	public final static float PARTITION_SIZE_MIN = 4f;
	public final static float PARTITION_SIZE_MAX = 16f;

	/**
	 * Constructor
	 */
	protected PartitionQuadTree() {
		this.key = new Key();
		this.rigidBodyIterator = new ArrayListIteratorMultiple<RigidBody>();
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
		this.rigidBodyPartitionNodesPool = new Pool<ArrayList<PartitionTreeNode>>() {
			public ArrayList<PartitionTreeNode> instantiate() {
				return new ArrayList<PartitionTreeNode>();
			}
		};
		this.rigidBodyPartitionNodes = new HashMap<String, ArrayList<PartitionTreeNode>>();
		this.treeRoot = new PartitionTreeNode();
		this.treeRoot.partitionSize = -1;
		this.treeRoot.x = -1;
		this.treeRoot.y = -1;
		this.treeRoot.z = -1;
		this.treeRoot.parent = null;
		this.treeRoot.bv = null;
		this.treeRoot.subNodes = new ArrayList<PartitionTreeNode>();
		this.treeRoot.subNodesByCoordinate = new HashMap<Key, PartitionTreeNode>();
		this.treeRoot.partitionRidigBodies = null;		
	}

	/**
	 * Creates a partition
	 * @param parent
	 * @param x
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
		node.partitionRidigBodies = null;

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
			node.partitionRidigBodies = new ArrayList<RigidBody>();
		}

		//
		return node;
	}

	/**
	 * Adds a object
	 * @param rigidBody
	 */
	protected void addRigidBody(RigidBody rigidBody) {
		// update if already exists
		ArrayList<PartitionTreeNode> objectPartitionsVector = rigidBodyPartitionNodes.get(rigidBody.getId());
		if (objectPartitionsVector != null) {
			while (objectPartitionsVector.size() > 0) {
				int lastIdx = objectPartitionsVector.size() - 1; 
				objectPartitionsVector.get(lastIdx).partitionRidigBodies.remove(rigidBody);
				objectPartitionsVector.remove(lastIdx);
			}
		}

		// determine max first level partition dimension
		// convert to aabb for fast collision tests
		BoundingVolume cbv = rigidBody.cbv;
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

		// add rigid body to tree
		addToPartitionTree(rigidBody, boundingBox);
	}

	/**
	 * Updates a object
	 * @param rigidBody
	 */
	protected void updateRigidBody(RigidBody rigidBody) {
		addRigidBody(rigidBody);
	}

	/**
	 * Removes a rigidBody
	 * @param rigidBody
	 */
	protected void removeRigidBody(RigidBody rigidBody) {
		ArrayList<PartitionTreeNode> objectPartitionsVector = rigidBodyPartitionNodes.remove(rigidBody.getId());
		if (objectPartitionsVector == null) {
			// rigid body not registered
			return;
		}
		while (objectPartitionsVector.size() > 0) {
			int lastIdx = objectPartitionsVector.size() - 1;
			objectPartitionsVector.get(lastIdx).partitionRidigBodies.remove(rigidBody);
			objectPartitionsVector.remove(lastIdx);
		}
		rigidBodyPartitionNodesPool.release(objectPartitionsVector);
	}

	/**
	 * Do partition tree lookup
	 * @param node
	 * @param cbv
	 * @param cbvsIterator
	 */
	private void addToPartitionTree(PartitionTreeNode node, RigidBody rigidBody, BoundingBox cbv) {
		// check if given cbv collides with partition node bv
		if (CollisionDetection.doCollideAABBvsAABBFast(node.bv, cbv) == false) {
			return;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionRidigBodies != null) {
			node.partitionRidigBodies.add(rigidBody);
			ArrayList<PartitionTreeNode> rigidBodyPartitionNodesVector = rigidBodyPartitionNodes.get(rigidBody.getId());
			if (rigidBodyPartitionNodesVector == null) {
				rigidBodyPartitionNodesVector = rigidBodyPartitionNodesPool.allocate();
				rigidBodyPartitionNodes.put(rigidBody.getId(), rigidBodyPartitionNodesVector);
			}
			rigidBodyPartitionNodesVector.add(node);
		} else
		if (node.subNodes != null) {
			// otherwise check sub nodes
			for (int i = 0; i < node.subNodes.size(); i++) {
				addToPartitionTree(node.subNodes.get(i), rigidBody, cbv);
			}
		}
	}

	/**
	 * Add rigidBody to tree
	 */
	protected void addToPartitionTree(RigidBody rigidBody, BoundingBox cbv) {
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			addToPartitionTree(treeRoot.subNodes.get(i), rigidBody, cbv); 
		}
	}

	/**
	 * Do partition tree lookup for near entities to cbv
	 * @param node
	 * @param cbv
	 * @param rigidBody iterator
	 */
	private int doPartitionTreeLookUpNearEntities(PartitionTreeNode node, BoundingBox cbv, ArrayListIteratorMultiple<RigidBody> objectsIterator) {
		// check if given cbv collides with partition node bv
		if (CollisionDetection.doCollideAABBvsAABBFast(cbv, node.bv) == false) {
			return 1;
		}

		// if this node already has the partition cbvs add it to the iterator
		if (node.partitionRidigBodies != null) {
			objectsIterator.addVector(node.partitionRidigBodies);
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
	public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(BoundingVolume cbv) {
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
		rigidBodyIterator.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpNearEntities(treeRoot.subNodes.get(i), boundingBox, rigidBodyIterator); 
		}
		return rigidBodyIterator;
	}

	/**
	 * Get objects near to
	 * @param cbv
	 * @return objects near to cbv
	 */
	public ArrayListIteratorMultiple<RigidBody> getObjectsNearTo(Vector3 center) {
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
		rigidBodyIterator.clear();
		int lookUps = 0;
		for (int i = 0; i < treeRoot.subNodes.size(); i++) {
			lookUps+=doPartitionTreeLookUpNearEntities(treeRoot.subNodes.get(i), boundingBox, rigidBodyIterator); 
		}
		return rigidBodyIterator;
	}

	/**
	 * To string
	 * @param indent
	 * @param node
	 * @return string representation of node
	 */
	public String toString(String indent, PartitionTreeNode node) {
		String result = indent + node.x + "/" + node.y + "/" + node.z + ", size " + node.partitionSize + " / " + node.bv + "\n";
		if (node.partitionRidigBodies != null) {
			result+= indent + "  ";
			for (RigidBody rigidBody: node.partitionRidigBodies) {
				result+= rigidBody.id + ",";
			}
			result+= "\n";
		}
		if (node.subNodes != null) {
			for (PartitionTreeNode subNode: node.subNodes) {
				result += toString(indent + "  ", subNode);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "PartitionQuadTree\n";
		for (PartitionTreeNode subNode: treeRoot.subNodes) {
			result += toString("  ", subNode);
		}
		return result;
	}

}

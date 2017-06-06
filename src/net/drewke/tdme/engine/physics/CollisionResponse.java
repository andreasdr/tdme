package net.drewke.tdme.engine.physics;

import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;

/**
 * Collision response
 * @author Andreas Drewke
 * @version $Id$
 */
public final class CollisionResponse {

	public final static int ENTITY_COUNT = 15;
	public final static int HITPOINT_COUNT = 30;

	/**
	 * Collision Response Entity
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public final static class Entity {
		protected float distance;
		protected Vector3 normal;
		protected ArrayList<Vector3> hitPoints;
		protected int hitPointsCount;

		/**
		 * @return distance
		 */
		public float getDistance() {
			return distance;
		}

		/**
		 * Set distance
		 * @param distance
		 */
		public void setDistance(float distance) {
			this.distance = distance;
		}

		/**
		 * @return penetration
		 */
		public float getPenetration() {
			return -distance;
		}

		/**
		 * @return normal
		 */
		public Vector3 getNormal() {
			return normal;
		}

		/**
		 * Adds a hit point
		 * @param hit point
		 */
		public void addHitPoint(Vector3 hitPoint) {
			// check if we already have this hit point
			for (int i = 0; i < hitPointsCount; i++) {
				if (hitPoints.get(i).equals(hitPoint, 0.1f)) return;
			}
			if (hitPointsCount == HITPOINT_COUNT) {
				Console.println("CollisionResponse::Entity::too many hit points");
				return;
			}
			hitPoints.get(hitPointsCount++).set(hitPoint);
		}

		/**
		 * @return hit points count
		 */
		public int getHitPointsCount() {
			return hitPointsCount;
		}

		/**
		 * Get hit point of given index 
		 * @param i
		 * @return hit point for given hit points index
		 */
		public Vector3 getHitPointAt(int i) {
			return hitPoints.get(i);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String tmp = new String();
			for (int i = 0; i < hitPointsCount; i++) {
				if (tmp.length() > 0) tmp+=",";
				tmp+= hitPoints.get(i).toString();
			}
			tmp+="]";

			return "Entity [distance=" + distance + ", normal=" + normal + ", hasPenetration() = " + (distance < 0f) +", hit points=" + tmp + "]";
		}
	}

	protected ArrayList<Entity> entities;
	protected int entityCount;
	protected Entity selectedEntity = null;

	/**
	 * Public constructor
	 * @param distance
	 * @param normal
	 */
	public CollisionResponse() {
		selectedEntity = null;
		entityCount = 0;
		entities = new ArrayList<CollisionResponse.Entity>();
		for (int i = 0; i < ENTITY_COUNT; i++) {
			Entity entity = new Entity();
			entity.distance = 0f;
			entity.normal = new Vector3();
			entity.hitPoints = new ArrayList<Vector3>();
			entity.hitPointsCount = 0;
			for (int j = 0; j < HITPOINT_COUNT; j++) {
				entity.hitPoints.add(new Vector3());
			}
			entities.add(entity);
		}
	}

	/**
	 * Reset
	 */
	public void reset() {
		for (int i = 0; i < entityCount; i++) {
			Entity entity = entities.get(i);
			entity.getNormal().set(0f,0f,0f);
			entity.setDistance(0f);
			for (int j = 0; j < entity.hitPointsCount; j++) {
				entity.hitPoints.get(j).set(0f,0f,0f);
			}
			entity.hitPointsCount = 0;
		}
		entityCount = 0;
		selectedEntity = null;
	}

	/**
	 * Adds a collision response entity 
	 * @param distance
	 * @return Entity or null
	 */
	public Entity addResponse(float distance) {
		if (entityCount == ENTITY_COUNT) {
			Console.println("CollisionResponse::too many entities");
			return null;
		}

		Entity entity = entities.get(entityCount);

		// otherwise use slot
		entity.distance = distance;

		// select entity with smallest penetration by default
		if (selectedEntity == null || distance > selectedEntity.distance) {
			selectedEntity = entity;
		}

		// we are done
		entityCount++;
		return entity;
	}

	/**
	 * @return entity count
	 */
	public int getEntityCount() {
		return entityCount;
	}

	/**
	 * @return selected entity
	 */
	public CollisionResponse.Entity getSelectedEntity() {
		return selectedEntity;
	}

	/**
	 * Selects entity at given index
	 * @param idx
	 * @return
	 */
	public CollisionResponse.Entity getEntityAt(int idx) {
		if (idx < 0 || idx >= entityCount) return null;
		return entities.get(idx);
	}

	/**
	 * Selects entity at given index
	 * @param idx
	 * @return
	 */
	public CollisionResponse selectEntityAt(int idx) {
		if (idx < 0 || idx >= entityCount) return this;
		selectedEntity = entities.get(idx);
		return this; 
	}

	/**
	 * Select entity with least penetration but exclude given axis
	 * @param axis
	 * @param respect direction
	 * @return
	 */
	public CollisionResponse selectEntityExcludeAxis(Vector3 axis, boolean respectDirection) {
		selectedEntity = null;
		for (int i = 0; i < entityCount; i++) {
			Entity entity = entities.get(i);
			// check if normal maps on axis
			float distanceOnAxis = Vector3.computeDotProduct(entity.normal, axis);
			if (respectDirection == false) distanceOnAxis = Math.abs(distanceOnAxis);

			// skip if we have penetration on exclude axis
			if (distanceOnAxis > MathTools.EPSILON) continue;

			//
			if (selectedEntity == null || entity.distance > selectedEntity.distance) {
				selectedEntity = entity;
			}			
		}
		return this;
	}

	/**
	 * Select entity on given axis with least penetration
	 * @param axis
	 * @param respect direction
	 * @return
	 */
	public CollisionResponse selectEntityOnAxis(Vector3 axis, boolean respectDirection) {
		selectedEntity = null;
		float selectedEntityDistanceOnAxis = 0f;
		for (int i = 0; i < entityCount; i++) {
			Entity entity = entities.get(i);
			// check if normal maps on axis
			float distanceOnAxis = Vector3.computeDotProduct(entity.normal, axis);
			if (respectDirection == false) distanceOnAxis = Math.abs(distanceOnAxis);

			// skip if we have no penetration on this axis
			if (distanceOnAxis < MathTools.EPSILON) continue;

			// take distance into account
			distanceOnAxis*= entity.distance;

			//
			if (selectedEntity == null || distanceOnAxis > selectedEntityDistanceOnAxis) {
				selectedEntity = entity;
				selectedEntityDistanceOnAxis = distanceOnAxis;
			}			
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.physics.CollisionResponse#hasEntitySelected()
	 */
	public boolean hasEntitySelected() {
		return selectedEntity != null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.physics.CollisionResponse#getDistance()
	 */
	public float getDistance() {
		if (selectedEntity == null) return 0f;
		return selectedEntity.distance;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.physics.CollisionResponse#hasPenetration()
	 */
	public boolean hasPenetration() {
		if (selectedEntity == null) return false;
		return selectedEntity.distance < -MathTools.EPSILON;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.physics.CollisionResponse#getPenetration()
	 */
	public float getPenetration() {
		if (selectedEntity == null) return 0f;
		return -selectedEntity.distance;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.physics.CollisionResponse#getNormal()
	 */
	public Vector3 getNormal() {
		if (selectedEntity == null) return null;
		return selectedEntity.normal;
	}

	/**
	 * @return hit points count
	 */
	public int getHitPointsCount() {
		if (selectedEntity == null) return 0;
		return selectedEntity.hitPointsCount;
	}

	/**
	 * Get hit point of given index 
	 * @param i
	 * @return hit point for given hit points index
	 */
	public Vector3 getHitPointAt(int i) {
		if (selectedEntity == null) return null;
		return selectedEntity.hitPoints.get(i);
	}

	/**
	 * Invert normals
	 */
	protected void invertNormals() {
		for (int i = 0; i < entityCount; i++) {
			entities.get(i).getNormal().scale(-1f);
		}
	}

	/**
	 * Set up response from given collision response
	 * @param response
	 */
	public CollisionResponse fromResponse(CollisionResponse response) {
		// reset this response
		reset();

		// clone response
		//	entity count
		entityCount = response.entityCount;
		for (int i = 0; i < response.entityCount; i++) {
			Entity srcEntity = response.entities.get(i);
			Entity dstEntity = entities.get(i);

			//	selected entity
			if (srcEntity == response.selectedEntity) {
				selectedEntity = dstEntity;
			}

			//	entity distance
			dstEntity.distance = srcEntity.distance;

			//	entity normal
			dstEntity.normal.set(srcEntity.normal);

			//	hit points
			dstEntity.hitPointsCount = srcEntity.hitPointsCount;
			for (int j = 0; j < srcEntity.hitPointsCount; j++) {
				dstEntity.hitPoints.get(j).set(srcEntity.hitPoints.get(j));
			}
		}

		//
		return this;
	}

	/**
	 * Set up response from given collision response
	 * @param response
	 */
	public CollisionResponse mergeResponse(CollisionResponse response) {
		// merge response
		for (int i = 0; i < response.entityCount; i++) {
			// src entity to merge
			Entity srcEntity = response.entities.get(i);

			// no entity with matching normal, add if no entity exists or not greater than selected entity
			Entity dstEntity = null;
			if (entityCount > 0) dstEntity = entities.get(0);

			// entity distance
			if (dstEntity == null ||
				srcEntity.distance > dstEntity.distance) {
				// create entity if not yet done
				if (dstEntity == null) dstEntity = entities.get(entityCount++);

				// set up distance and normal
				dstEntity.distance = srcEntity.distance;
	
				//	entity normal
				dstEntity.normal.set(srcEntity.normal);
			}

			//
			selectedEntity = entities.get(0);

			//	(add) hit points
			for (int j = 0; j < srcEntity.hitPointsCount; j++) {
				dstEntity.addHitPoint(srcEntity.hitPoints.get(j));
			}
		}

		//
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = new String();
		for (int i = 0; i < entityCount; i++) {
			if (tmp.length() > 0) tmp+=",";
			tmp+= entities.get(i).toString();
		}
		tmp+="]";
		return "CollisionResponseMultiple [selected=" + selectedEntity + ", entities=" + tmp + "]";
	}

}

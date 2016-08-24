package net.drewke.tdme.engine.physics;

/**
 * Rigid body collision listener
 * @author Andreas Drewke
 * @version $Id$
 */
public interface CollisionListener {

	/**
	 * On collision
	 * 
	 * 	Note: 
	 * 		The collision response will only live while calling this method. 
	 * 		If you need it somewhere else you need to clone it
	 * 
	 * @param rigid body 1
	 * @param rigid body 2
	 * @param collision response
	 * 
	 */
	public void onCollision(RigidBody rigidBody1, RigidBody rigidBody2, CollisionResponse collisionResponse);

	/**
	 * On collision begin
	 * 
	 * 	Note: 
	 * 		The collision response will only live while calling this method. 
	 * 		If you need it somewhere else you need to clone it
	 * 
	 * @param rigid body 1
	 * @param rigid body 2
	 * @param collision response
	 * 
	 */
	public void onCollisionBegin(RigidBody rigidBody1, RigidBody rigidBody2, CollisionResponse collisionResponse);

	/**
	 * On collision end
	 * 
	 * @param rigid body 1
	 * @param rigid body 2
	 * 
	 */
	public void onCollisionEnd(RigidBody rigidBody1, RigidBody rigidBody2);

}

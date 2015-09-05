package net.drewke.tdme.engine.physics;

import java.util.ArrayList;
import java.util.Comparator;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.LineSegment;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.engine.primitives.Triangle;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.SeparatingAxisTheorem;
import net.drewke.tdme.math.TriangleTriangleIntersection;
import net.drewke.tdme.math.Vector3;

/**
 * Collision detection
 * @author Andreas Drewke
 * @version $Id$
 */
public final class CollisionDetection {

	private final static boolean VERBOSE = false;
	private final static float HITPOINT_TOLERANCE = 0.1f;
	private final static int SAT_AXES_TEST_MAX = 20;
	private final static int TRIANGLES_TEST_MAX = 10000;

	// current thread id
	private long threadId;

	// collision detection instance -> thread mapping
	private static ArrayList<CollisionDetection> instances = new ArrayList<CollisionDetection>(); 
	private static CollisionDetection instanceLast = null;
	private static Object synchronizeObject = new Object();

	private static Vector3 zeroVector = new Vector3();
	private LineSegment lineSegment = new LineSegment();
	private Vector3 closestPointOnCapsule1 = new Vector3();
	private Vector3 closestPointOnCapsule2 = new Vector3();
	private Vector3 closestPoint = new Vector3();
	private Vector3 axis = new Vector3();
	private OrientedBoundingBox obbExtended = new OrientedBoundingBox();
	private Vector3 contactMin = new Vector3();
	private Vector3 contactMax = new Vector3();
	private Vector3 contactAvg = new Vector3();
	private Vector3 contactAvgCollisionNormal = new Vector3();
	private Vector3 contactAvgSubContactMin = new Vector3();
	private Vector3 contactOptimal = new Vector3();
	private Sphere sphere1 = new Sphere();
	private Sphere sphere2 = new Sphere();

	private OrientedBoundingBox obbConverted1 = new OrientedBoundingBox();
	private OrientedBoundingBox obbConverted2 = new OrientedBoundingBox();

	private Vector3 pointOnFaceNearest = new Vector3();
	private Vector3 pointOnFaceOpposite = new Vector3();

	private Vector3 triangle1Edge1 = new Vector3();
	private Vector3 triangle1Edge2 = new Vector3();
	private Vector3 triangle1Edge3 = new Vector3();
	private Vector3 triangle1Normal = new Vector3();
	private Vector3 triangle2Edge1 = new Vector3();
	private Vector3 triangle2Edge2 = new Vector3();
	private Vector3 triangle2Edge3 = new Vector3();
	private Vector3 triangle2Normal = new Vector3();

	private Vector3[] closestPointsOnCapsuleSegment = {
		new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3()
	};
	private Vector3[] closestPointsOnTriangleSegments = {
		new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3()
	};

	// test triangles
	private int testTriangleCount;
	private Triangle[][] testTriangles;

	// sat axes to test
	private Vector3 satAxis = new Vector3();
	private int satAxesCount = 0;
	private float[] satPenetrations;
	private Vector3[] satAxes;

	private boolean haveSatAxisBestFit = false;
	private Vector3 satAxisBestFit = new Vector3();
	private float satAxisBestFitPenetration = 0f;

	private SeparatingAxisTheorem separatingAxisTheorem = new SeparatingAxisTheorem();

	private Vector3 hitPoint = new Vector3();

	private TriangleTriangleIntersection triangleTriangleIntersection = new TriangleTriangleIntersection();
	private Triangle triangle1 = new Triangle(new Vector3(), new Vector3(), new Vector3());
	private Triangle triangle2 = new Triangle(new Vector3(), new Vector3(), new Vector3());
	private Vector3 hitPointTriangle1 = new Vector3();
	private Vector3 hitPointTriangle2 = new Vector3();

	private CollisionResponse collision1 = new CollisionResponse();
	private CollisionResponse collision2 = new CollisionResponse();
	private Comparator<CollisionResponse> collisionResponseComparator = new Comparator<CollisionResponse>() {
		public int compare(CollisionResponse r1, CollisionResponse r2) {
			if (r1.getPenetration() > r2.getPenetration()) return -1; else
			if (r1.getPenetration() < r2.getPenetration()) return +1; else
				return 0;
		}
	};
	// private ArrayList<CollisionResponse> collisions = new ArrayList<CollisionResponse>();
	

	private final static boolean CHECK_COLLISIONRESPONSE = false;

	/**
	 * Singleton method
	 * @return collision detection for current thread
	 */
	public static CollisionDetection getInstance() {
		// try to use last used thread
		long currentThreadId = Thread.currentThread().getId();
		if (instanceLast != null &&
			instanceLast.threadId == currentThreadId) {
			return instanceLast;
		}

		// otherwise find again collision detection instance by thread id
		synchronized(synchronizeObject) {
			// check if we already have this thread
			for (int i = 0; i < instances.size(); i++) {
				CollisionDetection instance = instances.get(i); 
				if (instance.threadId == currentThreadId) {
					instanceLast = instance;
					return instance;
				}
			}
	
			// nope, add this one
			CollisionDetection instance = new CollisionDetection(currentThreadId);
			instances.add(instance);
			instanceLast = instance;
			return instance;
		}
	}

	/**
	 * Reset
	 */
	public static synchronized void reset() {
		instances.clear();
	}

	/**
	 * Constructor
	 */
	private CollisionDetection(long threadId) {
		this.threadId = threadId;

		// sat test variables
		satPenetrations = new float[SAT_AXES_TEST_MAX];
		satAxes = new Vector3[SAT_AXES_TEST_MAX];
		for (int i = 0; i < satAxes.length; i++) satAxes[i] = new Vector3();
		satAxesCount = 0;

		//
		testTriangleCount = 0;
		testTriangles = new Triangle[TRIANGLES_TEST_MAX][2];
		for (int i = 0; i < testTriangles.length; i++) testTriangles[i] = new Triangle[2];
	}

	/**
	 * Reset SAT axes
	 */
	private void resetSATAxes() {
		haveSatAxisBestFit = false;
		satAxesCount = 0;
	}

	/**
	 * Add axis to SAT test
	 * @param axis
	 */
	private void addSATAxis(Vector3 axis) {
		float[] axisXYZ = axis.getArray();

		// return if axis contains NaN component
		if (Float.isNaN(axisXYZ[0]) ||
			Float.isNaN(axisXYZ[1]) ||
			Float.isNaN(axisXYZ[2])) {
			return;
		}

		// check if axis has no length
		if (Math.abs(axisXYZ[0]) < MathTools.EPSILON &&
			Math.abs(axisXYZ[1]) < MathTools.EPSILON &&
			Math.abs(axisXYZ[2]) < MathTools.EPSILON) {
			return;
		}

		// check if axis already exists
		for (int i = 0; i < satAxesCount; i++) {
			if (satAxes[i].equals(axis, 0.1f)) return;
		}

		// nope, add
		satAxes[satAxesCount++].set(axis);
	}

	/**
	 * Determine sat axis best fit
	 */
	private void determineSatAxisBestFit() {
		// determine entity with least penetration
		for (int i = 0; i < satAxesCount; i++) {
			// skip on NaN penetrations or zero penetration
			if (Float.isNaN(satPenetrations[i]) ||
				Math.abs(satPenetrations[i]) < MathTools.EPSILON) {
				continue;
			}
			if (haveSatAxisBestFit == false || -satPenetrations[i] > satAxisBestFitPenetration) {
				haveSatAxisBestFit = true;
				satAxisBestFit.set(satAxes[i]);
				satAxisBestFitPenetration = -satPenetrations[i];
			}
		}
	}

	/**
	 * Reset triangles to test
	 */
	private void resetTriangles() {
		testTriangleCount = 0;
	}

	/**
	 * Returns if axis aligned bounding boxes do collide
	 * 	Will not provide hit points
	 * @param axis aligned bounding box 1
	 * @param axis aligned bounding box 2
	 * @param movement
	 * @param collision response
	 * @return collision 
	 */
	public static boolean doCollideAABBvsAABBFast(BoundingBox b1, BoundingBox b2) {
		// see
		//	http://www.gamedev.net/topic/567310-platform-game-collision-detection/
		float b1MinXYZ[] = b1.getMin().getArray();
		float b1MaxXYZ[] = b1.getMax().getArray();
		float b2MinXYZ[] = b2.getMin().getArray();
		float b2MaxXYZ[] = b2.getMax().getArray();

		// face distances
		if (b2MaxXYZ[0] - b1MinXYZ[0] < 0f) return false; // b2 collides into b1 on x
		if (b1MaxXYZ[0] - b2MinXYZ[0] < 0f) return false; // b1 collides into b2 on x
		if (b2MaxXYZ[1] - b1MinXYZ[1] < 0f) return false; // b2 collides into b1 on y
		if (b1MaxXYZ[1] - b2MinXYZ[1] < 0f) return false; // b1 collides into b2 on y
		if (b2MaxXYZ[2] - b1MinXYZ[2] < 0f) return false; // b2 collides into b1 on z
		if (b1MaxXYZ[2] - b2MinXYZ[2] < 0f) return false;  // b1 collides into b2 on z

		//
		return true;
	}

	/**
	 * Returns if axis aligned bounding boxes do collide
	 * @param axis aligned bounding box 1
	 * @param axis aligned bounding box 2
	 * @param movement
	 * @param collision response
	 * @return collision 
	 */
	public boolean doCollide(BoundingBox b1, BoundingBox b2, Vector3 movement, CollisionResponse collision) {
		obbConverted1.fromBoundingBox(b1);
		obbConverted2.fromBoundingBox(b2);
		return doCollide(obbConverted1, obbConverted2, movement, collision);
	}

	/**
	 * Checks if axis aligned bounding box collides with sphere
	 * @param axis aligned bounding box
	 * @param sphere
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(BoundingBox aabb, Sphere sphere, Vector3 movement, CollisionResponse collision) {
		obbConverted1.fromBoundingBox(aabb);
		return doCollide(obbConverted1, sphere, movement, collision);
	}

	/**
	 * Checks if sphere is colliding with axis aligned bounding box
	 * @param axis aligned bounding box
	 * @param sphere
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Sphere sphere, BoundingBox aabb, Vector3 movement, CollisionResponse collision) {
		obbConverted1.fromBoundingBox(aabb);
		return doCollide(sphere, obbConverted1, movement, collision);
	}

	/**
	 * Checks if axis aligned bounding box collides with capsule
	 * @param bounding box
	 * @param capsule
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(BoundingBox aabb, Capsule capsule, Vector3 movement, CollisionResponse collision) {
		obbConverted1.fromBoundingBox(aabb);
		return doCollide(obbConverted1, capsule, movement, collision);
	}

	/**
	 * Checks if capsule collides with axis aligned bounding box
	 * @param axis aligned bounding box
	 * @param capsule
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Capsule capsule, BoundingBox aabb, Vector3 movement, CollisionResponse collision) {
		obbConverted1.fromBoundingBox(aabb);
		return doCollide(capsule, obbConverted1, movement, collision);
	}

	/**
	 * Checks if 2 spheres are colliding 
	 * @param sphere 1
	 * @param sphere 2
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Sphere s1, Sphere s2, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		//
		axis.set(s2.getCenter()).sub(s1.getCenter());

		// check if to use movement fallback
		if (checkMovementFallback(axis, movement, collision) == true) {
			CollisionResponse.Entity collisionEntity = collision.getEntityAt(0);
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(s1.getRadius()).add(s1.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(-s2.getRadius()).add(s2.getCenter()));
			return true;
		}

		//
		float distance = axis.computeLength();
		float _distance = distance - (s1.getRadius() + s2.getRadius());
		if (_distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(_distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(s1.getRadius()).add(s1.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(-s2.getRadius()).add(s2.getCenter()));
			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if 2 capsules do collide
	 * @param capsule 1
	 * @param capsule 2
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Capsule c1, Capsule c2, Vector3 movement, CollisionResponse collision) {
		// do broad test
		if (doBroadTest(c1, c2) == false) return false;

		// compute closest points on capsules to each other
		lineSegment.computeClosestPointsOnLineSegments(
			c1.getA(),
			c1.getB(),
			c2.getA(),
			c2.getB(),
			closestPointOnCapsule1,
			closestPointOnCapsule2
		);

		// do the sphere collision tests
		return
			doCollide(
				sphere1.set(closestPointOnCapsule1, c1.getRadius()),
				sphere2.set(closestPointOnCapsule2, c2.getRadius()),
				movement,
				collision
			);
	}

	/**
	 * Checks if capsule is colliding with sphere
	 * @param capsule
	 * @param sphere
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Capsule c, Sphere s, Vector3 movement, CollisionResponse collision) {
		//
		collision.reset();

		// do broad test
		if (doBroadTest(c, s) == false) return false;

		// compute closest points from C1 to C2.A and C2.B
		Vector3 sphereCenter = s.getCenter();
		c.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(sphereCenter).sub(closestPoint);

		// check if to use movement fallback
		if (checkMovementFallback(axis, movement, collision) == true) {
			CollisionResponse.Entity collisionEntity = collision.getEntityAt(0);
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(-s.getRadius()).add(s.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(c.getRadius()).add(closestPoint));			
			return true;
		}

		//
		float distance = axis.computeLength();
		float _distance = distance - (c.getRadius() + s.getRadius());
		if (_distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(_distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(-s.getRadius()).add(s.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(c.getRadius()).add(closestPoint));
			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if sphere is colliding with capsule
	 * @param sphere
	 * @param capsule
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Sphere s, Capsule c, Vector3 movement, CollisionResponse collision) {
		//
		collision.reset();

		// do broad test
		if (doBroadTest(s, c) == false) return false;

		// compute closest points from C1 to C2.A and C2.B
		Vector3 sphereCenter = s.getCenter();
		c.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(closestPoint).sub(sphereCenter);

		// check if to use movement fallback
		if (checkMovementFallback(axis, movement, collision) == true) {
			CollisionResponse.Entity collisionEntity = collision.getEntityAt(0);
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(s.getRadius()).add(s.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(movement).normalize().scale(-c.getRadius()).add(closestPoint));
			return true;
		}

		//
		float distance = axis.computeLength();
		float _distance = distance - (c.getRadius() + s.getRadius());
		if (_distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(_distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(s.getRadius()).add(s.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(-c.getRadius()).add(closestPoint));
			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if oriented bounding box 1 collides with oriented bounding box 2
	 * 	ported from "game physics - a practical introduction/ben kenwright"
	 * @param oriented bounding box 1
	 * @param oriented bounding box 2
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(OrientedBoundingBox obb1, OrientedBoundingBox obb2, Vector3 movement, CollisionResponse collision) {
		//
		collision.reset();

		// do broad test
		if (doBroadTest(obb1, obb2) == false) return false;

		//
		Vector3[] axes1 = obb1.getAxes();
		Vector3[] axes2 = obb2.getAxes();

		// compute obb 1,2 vertices
		Vector3[] obb1Vertices = obb1.getVertices();
		Vector3[] obb2Vertices = obb2.getVertices();

		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[0].set(axes1[0]), satPenetrations, 0) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[1].set(axes1[1]), satPenetrations, 1) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[2].set(axes1[2]), satPenetrations, 2) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[3].set(axes2[0]), satPenetrations, 3) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[4].set(axes2[1]), satPenetrations, 4) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, satAxes[5].set(axes2[2]), satPenetrations, 5) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[0], axes2[0], satAxes[6]), satPenetrations, 6) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[0], axes2[1], satAxes[7]), satPenetrations, 7) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[0], axes2[2], satAxes[8]), satPenetrations, 8) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[1], axes2[0], satAxes[9]), satPenetrations, 9) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[1], axes2[1], satAxes[10]), satPenetrations, 10) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[1], axes2[2], satAxes[11]), satPenetrations, 11) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[2], axes2[0], satAxes[12]), satPenetrations, 12) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[2], axes2[1], satAxes[13]), satPenetrations, 13) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(obb1Vertices, obb2Vertices, Vector3.computeCrossProduct(axes1[2], axes2[2], satAxes[14]), satPenetrations, 14) == false) return false;

		// determine entity with least penetration
		int selectedEntityIdx = -1;
		float selectedEntityDistance = 0f;
		for (int i = 0; i < 15; i++) {
			if (Float.isNaN(satPenetrations[i])) continue;
			if (selectedEntityIdx == -1 || -satPenetrations[i] > selectedEntityDistance) {
				selectedEntityDistance = -satPenetrations[i];
				selectedEntityIdx = i;
			}
		}

		// create single collision response
		CollisionResponse.Entity entity = collision.addResponse(-satPenetrations[selectedEntityIdx]);
		entity.getNormal().set(satAxes[selectedEntityIdx]);
		computeHitPoints(obb1, obb2, entity);

		// we have a collision, return collision response
		return collision.getEntityCount() > 0;
	}

	/**
	 * Checks if oriented bounding box collides with axis aligned bounding box
	 * @param oriented bounding box
	 * @param axis aligned bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(OrientedBoundingBox obb, BoundingBox aabb, Vector3 movement, CollisionResponse collision) {
		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);
		
		// use obb->obb collision detection
		return doCollide(obb, obbConverted1, movement, collision);
	}

	/**
	 * Checks if axis aligned bounding box collides with oriented bounding box
	 * @param axis aligned bounding box
	 * @param oriented bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(BoundingBox aabb, OrientedBoundingBox obb, Vector3 movement, CollisionResponse collision) {
		// TODO: do broad test

		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);

		// use obb->obb collision detection
		return doCollide(obbConverted1, obb, movement, collision);
	}

	/**
	 * Checks if oriented bounding box collides with sphere
	 * @param oriented bounding box
	 * @param sphere
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(OrientedBoundingBox obb, Sphere sphere, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(obb, sphere) == false) return false;

		//
		Vector3 sphereCenter = sphere.getCenter();
		obb.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(sphereCenter).sub(closestPoint);

		// check if to use movement fallback
		float distance;
		if (axis.computeLength() < MathTools.EPSILON) {
			obb.computeNearestPointOnFaceBoundingVolume(closestPoint, pointOnFaceNearest);
			obb.computeOppositePointOnFaceBoundingVolume(closestPoint, pointOnFaceOpposite);
			obb.computeNearestPointOnFaceBoundingVolume(pointOnFaceNearest, closestPoint);
			axis.set(sphereCenter).sub(pointOnFaceNearest);
			distance = -axis.computeLength() - sphere.getRadius();
			axis.set(pointOnFaceNearest).sub(pointOnFaceOpposite);
		} else {
			//
			distance = axis.computeLength() - sphere.getRadius();
		}
		if (distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(closestPoint);
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(-sphere.getRadius()).add(sphereCenter));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if sphere collides with oriented bounding box
	 * @param sphere
	 * @param oriented bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Sphere sphere, OrientedBoundingBox obb, Vector3 movement, CollisionResponse collision) {
		//
		collision.reset();

		// do broad test
		if (doBroadTest(sphere, obb) == false) return false;

		//
		Vector3 sphereCenter = sphere.getCenter();
		obb.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(closestPoint).sub(sphereCenter);

		// check if to use movement fallback
		float distance;
		if (axis.computeLength() < MathTools.EPSILON) {
			obb.computeNearestPointOnFaceBoundingVolume(closestPoint, pointOnFaceNearest);
			obb.computeOppositePointOnFaceBoundingVolume(closestPoint, pointOnFaceOpposite);
			obb.computeNearestPointOnFaceBoundingVolume(pointOnFaceNearest, closestPoint);
			axis.set(sphereCenter).sub(pointOnFaceNearest);
			distance = axis.computeLength() - sphere.getRadius();
			axis.set(pointOnFaceOpposite).sub(pointOnFaceNearest);
		} else {
			//
			distance = axis.computeLength() - sphere.getRadius();
		}
		if (distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(sphere.getRadius()).add(sphereCenter));
			collisionEntity.addHitPoint(closestPoint);
			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if oriented bounding box collides with capsule
	 * @param oriented bounding box
	 * @param capsule
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(OrientedBoundingBox obb, Capsule capsule, Vector3 movement, CollisionResponse collision) {
		//
		collision.reset();

		// do broad test
		if (doBroadTest(obb, capsule) == false) return false;

		//
		float r = capsule.getRadius();

		/*
		// test capsule A against obb
		doCollide(
			obb,
			sphere1.set(
				capsule.getA(),
				r
			),
			movement,
			collision1
		);

		// test capsule B against obb
		doCollide(
			obb,
			sphere1.set(
				capsule.getB(),
				r
			),
			movement,
			collision2
		);
		*/

		// clone obb into obb extended
		obbExtended.fromOrientedBoundingBox(obb);

		// extend with capsule
		obbExtended.getHalfExtension().add(r);
		obbExtended.update();

		// test collision with capsule extended obb and capsule line segment 
		if (lineSegment.doesOrientedBoundingBoxCollideWithLineSegment(
			obbExtended,
			capsule.getA(),
			capsule.getB(),
			contactMin,
			contactMax
		) == true) {
			// trying to choose optimal contact point
			axis.set(capsule.getB()).sub(capsule.getA()).normalize();
			contactAvg.set(contactMin).add(contactMax).scale(0.5f);
			obb.computeClosestPointOnBoundingVolume(contactAvg, closestPoint);
			// if contact point equals closest point on aabb
			if (contactAvg.equals(closestPoint) == true) {
				// normal will be broken if contact point equals closest point on obb
				// in this case just take contact average for now
				contactOptimal.set(contactAvg);
			} else {
				// trying to choose optimal contact point
				//	this solution is not perfect, but works(penetration is sometime too less)
				//	TODO: actually the contact point should be one that is deepest in bounding box
				contactAvgCollisionNormal.set(contactAvg).sub(closestPoint).normalize();
				float collisionNormalDotABNormalized = Vector3.computeDotProduct(contactAvgCollisionNormal, axis);
				contactAvgSubContactMin.set(contactAvg).sub(contactMin);
				contactOptimal.set(contactAvg).sub(contactAvgSubContactMin.scale(collisionNormalDotABNormalized));
			}
			// TODO: this is a sphere <-> obb test, means penetration must be extended in a few cases
			doCollide(
				obb,
				sphere1.set(
					contactOptimal,
					r
				),
				movement,
				collision
			);
			if (collision.hasEntitySelected() == true) return true;
		}

		// we have 3 collision response, chose the one with max penetration
		/*
		collisions.clear();
		if (collision1.hasEntitySelected()) collisions.add(collision1);
		if (collision2.hasEntitySelected()) collisions.add(collision2);
		if (collision.hasEntitySelected()) collisions.add(collision);
		if (collisions.size() > 0) {
			QuickSort.sort(collisions, collisionResponseComparator);
			if (collisions.get(0) == collision) {
				return true;
			} else {
				collision.fromResponse(collisions.get(0));
				return true;
			}
		}
		*/

		// no collision
		return false;
	}

	/**
	 * Checks if capsule collides with oriented bounding box
	 * @param capsule
	 * @param oriented bounding box
	 * @param movement
	 * @param collision
	 * @return collision 
	 */
	public boolean doCollide(Capsule capsule, OrientedBoundingBox obb, Vector3 movement, CollisionResponse collision) {
		if (doCollide(obb, capsule, movement, collision) == true) {
			collision.invertNormals();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if triangle collides with sphere
	 * @param triangle
	 * @param sphere
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Triangle triangle, Sphere sphere, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		//
		Vector3 sphereCenter = sphere.getCenter();
		triangle.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(sphereCenter).sub(closestPoint);

		// check if to use movement fallback
		if (checkMovementFallback(axis, movement, collision) == true) {
			// TODO: Hit points
			return true;
		}

		//
		float distance = axis.computeLength() - sphere.getRadius();
		if (distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(-sphere.getRadius()).add(sphere.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(closestPoint));

			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if sphere collides with triangle
	 * @param sphere
	 * @param triangle
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Sphere sphere, Triangle triangle, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		//
		Vector3 sphereCenter = sphere.getCenter();
		triangle.computeClosestPointOnBoundingVolume(sphereCenter, closestPoint);
		axis.set(closestPoint).sub(sphereCenter);

		// check if to use movement fallback
		if (checkMovementFallback(axis, movement, collision) == true) {
			// TODO: Hit points
			return true;
		}

		//
		float distance = axis.computeLength() - sphere.getRadius();
		if (distance < 0f) {
			CollisionResponse.Entity collisionEntity = collision.addResponse(distance);
			collisionEntity.getNormal().set(axis).normalize();
			collisionEntity.addHitPoint(hitPoint.set(axis).normalize().scale(sphere.getRadius()).add(sphere.getCenter()));
			collisionEntity.addHitPoint(hitPoint.set(closestPoint));

			if (CHECK_COLLISIONRESPONSE) checkCollision(collision);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if sphere collides with mesh 
	 * @param sphere
	 * @param convex mesh
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(Sphere sphere, ConvexMesh mesh, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh, sphere) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle: mesh.getTriangles()) {
			if (doCollide(sphere, triangle, movement, collision1) == true) {
				//
				collision.mergeResponse(collision1);
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if sphere collides with mesh 
	 * @param convex mesh
	 * @param sphere
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh, Sphere sphere, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh, sphere) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle: mesh.getTriangles()) {
			if (doCollide(triangle, sphere, movement, collision1) == true) {
				//
				collision.mergeResponse(collision1);
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if triangle collides with bounding box
	 * @param triangle
	 * @param bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Triangle triangle, BoundingBox aabb, Vector3 movement, CollisionResponse collision) {
		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);
		return doCollide(triangle, obbConverted1, movement, collision);
	}

	/**
	 * Check if bounding box collides with triangle
	 * @param bounding box
	 * @param triangle
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(BoundingBox aabb, Triangle triangle, Vector3 movement, CollisionResponse collision) {
		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);

		//
		return doCollide(obbConverted1, triangle, movement, collision);
	}

	/**
	 * Check if triangle collides with oriented bounding box
	 * @param triangle
	 * @param oriented bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Triangle triangle, OrientedBoundingBox obb, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(triangle, obb) == false) return false;

		//
		Vector3[] obbVertices = obb.getVertices();
		Vector3[] obbAxes = obb.getAxes();

		Vector3[] triangleVertices = triangle.getVertices();
		triangle1Edge1.set(triangleVertices[1]).sub(triangleVertices[0]).normalize();
		triangle1Edge2.set(triangleVertices[2]).sub(triangleVertices[1]).normalize();
		triangle1Edge3.set(triangleVertices[0]).sub(triangleVertices[2]).normalize();
		triangle1Normal = Vector3.computeCrossProduct(triangle1Edge1, triangle1Edge2).normalize();

		// compute penetrations
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, satAxes[0].set(triangle1Normal), satPenetrations, 0) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, satAxes[1].set(obbAxes[0]), satPenetrations, 1) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, satAxes[2].set(obbAxes[1]), satPenetrations, 2) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, satAxes[3].set(obbAxes[2]), satPenetrations, 3) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[0], triangle1Edge1, satAxes[4]), satPenetrations, 4) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[0], triangle1Edge2, satAxes[5]), satPenetrations, 5) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[0], triangle1Edge3, satAxes[6]), satPenetrations, 6) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[1], triangle1Edge1, satAxes[7]), satPenetrations, 7) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[1], triangle1Edge2, satAxes[8]), satPenetrations, 8) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[1], triangle1Edge3, satAxes[9]), satPenetrations, 9) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[2], triangle1Edge1, satAxes[10]), satPenetrations, 10) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[2], triangle1Edge2, satAxes[11]), satPenetrations, 11) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangleVertices, obbVertices, Vector3.computeCrossProduct(obbAxes[2], triangle1Edge3, satAxes[12]), satPenetrations, 12) == false) return false;

		// determine entity with least penetration
		int selectedEntityIdx = -1;
		float selectedEntityDistance = 0f;
		for (int i = 0; i < 13; i++) {
			// skip on NaN penetrations or zero penetration
			if (Float.isNaN(satPenetrations[i]) ||
				Math.abs(satPenetrations[i]) < MathTools.EPSILON) {
				continue;
			}
			if (selectedEntityIdx == -1 || -satPenetrations[i] > selectedEntityDistance) {
				selectedEntityDistance = -satPenetrations[i];
				selectedEntityIdx = i;
			}
		}

		// create single collision response
		CollisionResponse.Entity entity = collision.addResponse(-satPenetrations[selectedEntityIdx]);
		entity.getNormal().set(satAxes[selectedEntityIdx]);
		computeHitPoints(triangle, obb, entity);

		// we have a collision, return collision response
		return true;
	}

	/**
	 * Check if oriented bounding collides with triangle box
	 * @param triangle
	 * @param oriented bounding box
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(OrientedBoundingBox obb, Triangle triangle, Vector3 movement, CollisionResponse collision) {
		if (doCollide(triangle, obb, movement, collision) == true) {
			collision.invertNormals();
			return true;
		} else {
			return false;
		}
	}


	/**
	 * Check if mesh collides with axis aligned bounding box
	 * @param convex mesh
	 * @param axis aligned bounding box
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh, BoundingBox aabb, Vector3 movement, CollisionResponse collision) {
		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);

		// do test
		return doCollide(mesh, obbConverted1, movement, collision);
	}

	/**
	 * Check if axis aligned bounding box collides with mesh 
	 * @param axis aligned bounding box
	 * @param convex mesh
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(BoundingBox aabb, ConvexMesh mesh, Vector3 movement, CollisionResponse collision) {
		// transform aabb to obb
		obbConverted1.fromBoundingBox(aabb);

		// do test
		return doCollide(obbConverted1, mesh, movement, collision);
	}

	/**
	 * Check if mesh collides with oriented bounding box
	 * @param convex mesh
	 * @param oriented bounding box
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh, OrientedBoundingBox obb, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh, obb) == false) return false;

		//
		Vector3[] obbVertices = obb.getVertices();
		Vector3[] obbAxes = obb.getAxes();
		Vector3[] meshVertices = mesh.getVertices();

		// compute axes and hit points
		resetTriangles();
		for (Triangle triangle: mesh.getTriangles()) {
			collision1.reset();

			// do broad test
			if (doBroadTest(triangle, obb) == false) continue;

			//
			Vector3[] triangleVertices = triangle.getVertices();
			triangle1Edge1.set(triangleVertices[1]).sub(triangleVertices[0]).normalize();
			triangle1Edge2.set(triangleVertices[2]).sub(triangleVertices[1]).normalize();
			triangle1Edge3.set(triangleVertices[0]).sub(triangleVertices[2]).normalize();
			Vector3.computeCrossProduct(triangle1Edge1, triangle1Edge2, triangle1Normal).normalize();

			// compute axes to test
			resetSATAxes();
			addSATAxis(satAxis.set(triangle1Normal));
			addSATAxis(satAxis.set(obbAxes[0]));
			addSATAxis(satAxis.set(obbAxes[1]));
			addSATAxis(satAxis.set(obbAxes[2]));
			addSATAxis(satAxis.set(triangle1Edge1));
			addSATAxis(satAxis.set(triangle1Edge2));
			addSATAxis(satAxis.set(triangle1Edge3));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[0], triangle1Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[0], triangle1Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[0], triangle1Edge3, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[1], triangle1Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[1], triangle1Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[1], triangle1Edge3, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[2], triangle1Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[2], triangle1Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(obbAxes[2], triangle1Edge3, satAxis));

			// do separating axis test for axes
			for (int satAxisIdx = 0; satAxisIdx < satAxesCount; satAxisIdx++) {
				// compute penetrations
				if (separatingAxisTheorem.doSpanIntersect(meshVertices, obbVertices, satAxes[satAxisIdx], satPenetrations, satAxisIdx) == false) {
					resetSATAxes();
					resetTriangles();
					return false;
				}
			}

			// determine sat axis best fit
			determineSatAxisBestFit();

			// store triangles to test
			testTriangles[testTriangleCount++][0] = triangle;
		}

		// create single collision response
		if (haveSatAxisBestFit == true) {
			CollisionResponse.Entity entity = collision.addResponse(satAxisBestFitPenetration);
			entity.getNormal().set(satAxisBestFit);
			for (int i = 0; i < testTriangleCount; i++) {
				computeHitPoints(testTriangles[i][0], obb, entity);
			}
		}

		// reset
		resetTriangles();
		resetSATAxes();

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if oriented bounding box collides with mesh 
	 * @param oriented bounding box
	 * @param convex mesh
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(OrientedBoundingBox obb, ConvexMesh mesh, Vector3 movement, CollisionResponse collision) {
		if (doCollide(mesh, obb, movement, collision) == true) {
			collision.invertNormals();
			return true;
		} else {
			return false;
		}		
	}

	/**
	 * Check if triangle collides with another triangle
	 * @param triangle 1
	 * @param triangle 2
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(Triangle triangle1, Triangle triangle2, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(triangle1, triangle2) == false) return false;

		//
		Vector3[] triangle1Vertices = triangle1.getVertices();
		triangle1Edge1.set(triangle1Vertices[1]).sub(triangle1Vertices[0]).normalize();
		triangle1Edge2.set(triangle1Vertices[2]).sub(triangle1Vertices[1]).normalize();
		triangle1Edge3.set(triangle1Vertices[0]).sub(triangle1Vertices[2]).normalize();
		Vector3.computeCrossProduct(triangle1Edge1, triangle1Edge2, triangle1Normal).normalize();

		Vector3[] triangle2Vertices = triangle2.getVertices();
		triangle2Edge1.set(triangle2Vertices[1]).sub(triangle2Vertices[0]).normalize();
		triangle2Edge2.set(triangle2Vertices[2]).sub(triangle2Vertices[1]).normalize();
		triangle2Edge3.set(triangle2Vertices[0]).sub(triangle2Vertices[2]).normalize();
		Vector3.computeCrossProduct(triangle2Edge1, triangle2Edge2, triangle2Normal).normalize();

		// compute penetrations
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, satAxes[0].set(triangle1Normal), satPenetrations, 0) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, satAxes[1].set(triangle2Normal), satPenetrations, 1) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge1, satAxes[2]), satPenetrations, 2) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge2, satAxes[3]), satPenetrations, 3) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge3, satAxes[4]), satPenetrations, 4) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge1, satAxes[5]), satPenetrations, 5) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge2, satAxes[6]), satPenetrations, 6) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge3, satAxes[7]), satPenetrations, 7) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge1, satAxes[8]), satPenetrations, 8) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge2, satAxes[9]), satPenetrations, 9) == false) return false;
		if (separatingAxisTheorem.doSpanIntersect(triangle1Vertices, triangle2Vertices, Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge3, satAxes[10]), satPenetrations, 10) == false) return false;

		// determine entity with least penetration
		int selectedEntityIdx = -1;
		float selectedEntityDistance = 0f;
		for (int i = 0; i < 11; i++) {
			// skip if NaN penetration or zero penetration
			if (Float.isNaN(satPenetrations[i]) ||
				Math.abs(satPenetrations[i]) < MathTools.EPSILON) {
				//
				continue;
			}
			if (selectedEntityIdx == -1 || -satPenetrations[i] > selectedEntityDistance) {
				selectedEntityDistance = -satPenetrations[i];
				selectedEntityIdx = i;
			}
		}

		if (selectedEntityIdx == -1) return false;

		// create single collision response
		CollisionResponse.Entity entity = collision.addResponse(-satPenetrations[selectedEntityIdx]);
		entity.getNormal().set(satAxes[selectedEntityIdx]);
		computeHitPoints(triangle1, triangle2, entity);

		// we have a collision, return collision response
		return true;
	}

	/**
	 * Check if mesh collides with another mesh
	 * 	Will not yet provide hit points
	 * @param convex mesh 1
	 * @param convex mesh 2
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh1, ConvexMesh mesh2, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh1, mesh2) == false) return false;

		// do triangle triangle test
		Vector3[] mesh1Vertices = mesh1.getVertices();
		Vector3[] mesh2Vertices = mesh2.getVertices();

		// compute axes and hit points
		resetTriangles();
		for (Triangle triangle1: mesh1.getTriangles())
		for (Triangle triangle2: mesh2.getTriangles()) {
			// do broad test
			if (doBroadTest(triangle1, triangle2) == false) continue;
	
			//
			Vector3[] triangle1Vertices = triangle1.getVertices();
			triangle1Edge1.set(triangle1Vertices[1]).sub(triangle1Vertices[0]).normalize();
			triangle1Edge2.set(triangle1Vertices[2]).sub(triangle1Vertices[1]).normalize();
			triangle1Edge3.set(triangle1Vertices[0]).sub(triangle1Vertices[2]).normalize();
			Vector3.computeCrossProduct(triangle1Edge1, triangle1Edge2, triangle1Normal).normalize();
	
			Vector3[] triangle2Vertices = triangle2.getVertices();
			triangle2Edge1.set(triangle2Vertices[1]).sub(triangle2Vertices[0]).normalize();
			triangle2Edge2.set(triangle2Vertices[2]).sub(triangle2Vertices[1]).normalize();
			triangle2Edge3.set(triangle2Vertices[0]).sub(triangle2Vertices[2]).normalize();
			Vector3.computeCrossProduct(triangle2Edge1, triangle2Edge2, triangle2Normal).normalize();
	
			// compute axes
			resetSATAxes();
			addSATAxis(satAxis.set(triangle1Normal));
			addSATAxis(satAxis.set(triangle2Normal));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge1, triangle2Edge3, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge2, triangle2Edge3, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge1, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge2, satAxis));
			addSATAxis(Vector3.computeCrossProduct(triangle1Edge3, triangle2Edge3, satAxis));
			
			// do separating axis test for axes
			for (int satAxisIdx = 0; satAxisIdx < satAxesCount; satAxisIdx++) {
				// compute penetrations
				if (separatingAxisTheorem.doSpanIntersect(mesh1Vertices, mesh2Vertices, satAxes[satAxisIdx], satPenetrations, satAxisIdx) == false) {
					resetSATAxes();
					resetTriangles();
					return false;
				}
			}

			// determine best fit
			determineSatAxisBestFit();

			// store triangles
			testTriangles[testTriangleCount][0] = triangle1;
			testTriangles[testTriangleCount][1] = triangle2;
			testTriangleCount++;
		}

		// 
		if (haveSatAxisBestFit == true) {
			// create single collision response
			CollisionResponse.Entity entity = collision.addResponse(satAxisBestFitPenetration);
			entity.getNormal().set(satAxisBestFit);
			for (int i = 0; i < testTriangleCount; i++) {
				computeHitPoints(testTriangles[i][0], testTriangles[i][1], entity);
			}
		}

		// reset
		resetSATAxes();
		resetTriangles();

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if triangle collides with mesh
	 * @param triangle
	 * @param convex mesh
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(Triangle triangle, ConvexMesh mesh, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(triangle, mesh) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle2: mesh.getTriangles()) {
			if (doCollide(triangle, triangle2, movement, collision1) == true) {
				if (collision.hasEntitySelected() == false ||
					collision1.getPenetration() > collision.getPenetration()) {
					collision.fromResponse(collision1);
				}
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if mesh collides with triangle
	 * @param convex mesh
	 * @param triangle
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh, Triangle triangle, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(triangle, mesh) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle2: mesh.getTriangles()) {
			if (doCollide(triangle2, triangle, movement, collision1) == true) {
				if (collision.hasEntitySelected() == false ||
					collision1.getPenetration() > collision.getPenetration()) {
					collision.fromResponse(collision1);
				}
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if triangle collides with capsule
	 * @param triangle
	 * @param capsule
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Triangle triangle, Capsule capsule, Vector3 movement, CollisionResponse collision) {
		// TODO: check me, improve me!

		// do broad test
		if (doBroadTest(triangle, capsule) == false) return false;

		// triangle vertices
		Vector3[] triangleVertices = triangle.getVertices();

		// check capsule segment against triangle segment 0
		lineSegment.computeClosestPointsOnLineSegments(
			capsule.getA(), capsule.getB(),
			triangleVertices[1], triangleVertices[0],
			closestPointsOnCapsuleSegment[0], closestPointsOnTriangleSegments[0]
		);

		// check capsule segment against triangle segment 1
		lineSegment.computeClosestPointsOnLineSegments(
			capsule.getA(), capsule.getB(),
			triangleVertices[2], triangleVertices[1],
			closestPointsOnCapsuleSegment[1], closestPointsOnTriangleSegments[1]
		);

		// check capsule segment against triangle segment 2
		lineSegment.computeClosestPointsOnLineSegments(
			capsule.getA(), capsule.getB(),
			triangleVertices[0], triangleVertices[2],
			closestPointsOnCapsuleSegment[2], closestPointsOnTriangleSegments[2]
		);

		// check endpoint of capsule A against triangle closest point
		closestPointsOnCapsuleSegment[3].set(capsule.getA()); 
		triangle.computeClosestPointOnBoundingVolume(
			closestPointsOnCapsuleSegment[3],
			closestPointsOnTriangleSegments[3]
		);

		// check endpoint of capsule B against triangle closest point
		closestPointsOnCapsuleSegment[4].set(capsule.getB()); 
		triangle.computeClosestPointOnBoundingVolume(
			closestPointsOnCapsuleSegment[4],
			closestPointsOnTriangleSegments[4]
		);

		// determine capsule and triangle segment N with minimum distance
		//	will be the best fit to do triangle <-> sphere collision test
		float bestFitLength =
			closestPoint.set(
				closestPointsOnCapsuleSegment[0]
			).sub(
				closestPointsOnTriangleSegments[0]
			).computeLengthSquared();
		int bestFitTest = 0;
		for (int i = 1; i < 5; i++) {			
			float testLength =
				closestPoint.set(
					closestPointsOnCapsuleSegment[i]
				).sub(
					closestPointsOnTriangleSegments[i]
				).computeLengthSquared();
			if (testLength < bestFitLength) {
				bestFitLength = testLength;
				bestFitTest = i;
			}
		}

		// do the triangle sphere collision test
		return doCollide(
			triangle,
			sphere1.set(
				closestPointsOnCapsuleSegment[bestFitTest],
				capsule.getRadius()
			),
			movement,
			collision
		);
	}

	/**
	 * Check if capsule collides with triangle
	 * @param capsule
	 * @param triangle
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	public boolean doCollide(Capsule capsule, Triangle triangle, Vector3 movement, CollisionResponse collision) {
		if (doCollide(triangle, capsule, movement, collision) == true) {
			collision.invertNormals();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if capsule collides with mesh 
	 * @param capsule
	 * @param convex mesh
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(Capsule capsule, ConvexMesh mesh, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh, capsule) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle: mesh.getTriangles()) {
			if (doCollide(capsule, triangle, movement, collision1) == true) {
				if (collision.hasEntitySelected() == false ||
					collision1.getPenetration() > collision.getPenetration()) {
					collision.fromResponse(collision1);
				}
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Check if mesh collides with capsule 
	 * @param convex mesh
	 * @param capsule
	 * @param movement
	 * @param collision response
	 * @return if collided
	 */
	public boolean doCollide(ConvexMesh mesh, Capsule capsule, Vector3 movement, CollisionResponse collision) {
		collision.reset();

		// do broad test
		if (doBroadTest(mesh, capsule) == false) return false;

		// do mesh1 triangles <-> mesh2 triangles collision detection
		for (Triangle triangle: mesh.getTriangles()) {
			if (doCollide(triangle, capsule, movement, collision1) == true) {
				if (collision.hasEntitySelected() == false ||
					collision1.getPenetration() > collision.getPenetration()) {
					collision.fromResponse(collision1);
				}
			}
		}

		// we have a collision, return collision response
		return collision.hasEntitySelected();
	}

	/**
	 * Compute hit points for intersecting obb1 with obb2
	 * @param obb1
	 * @param obb2
	 * @param collision entity
	 */
	public void computeHitPoints(OrientedBoundingBox obb1, OrientedBoundingBox obb2, CollisionResponse.Entity collisionEntity) {
		Vector3[] obb1Vertices = obb1.getVertices();
		int[][] obb1FacesVerticesIndexes = obb1.getFacesVerticesIndexes(); 
		Vector3[] obb2Vertices = obb2.getVertices();
		int[][] obb2FacesVerticesIndexes = obb2.getFacesVerticesIndexes();
		for (int triangleObb1Idx = 0; triangleObb1Idx < obb1FacesVerticesIndexes.length; triangleObb1Idx++)
		for (int triangleObb2Idx = 0; triangleObb2Idx < obb2FacesVerticesIndexes.length; triangleObb2Idx++) {
			TriangleTriangleIntersection.ReturnValue tritriReturn = triangleTriangleIntersection.computeTriangleTriangleIntersection(
				obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][0]],
				obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][1]],
				obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][2]],
				obb2Vertices[obb2FacesVerticesIndexes[triangleObb2Idx][0]],
				obb2Vertices[obb2FacesVerticesIndexes[triangleObb2Idx][1]],
				obb2Vertices[obb2FacesVerticesIndexes[triangleObb2Idx][2]],
				hitPointTriangle1,
				hitPointTriangle2
			);
			switch (tritriReturn) {
				case NOINTERSECTION:
					break;
				case INTERSECTION:
					collisionEntity.addHitPoint(hitPointTriangle1);
					collisionEntity.addHitPoint(hitPointTriangle2);
					break;
				case COPLANAR_INTERSECTION:
					triangle1.getVertices()[0].set(obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][0]]);
					triangle1.getVertices()[1].set(obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][1]]);
					triangle1.getVertices()[2].set(obb1Vertices[obb1FacesVerticesIndexes[triangleObb1Idx][2]]);
					triangle1.update();
					triangle2.getVertices()[0].set(obb2Vertices[obb1FacesVerticesIndexes[triangleObb2Idx][0]]);
					triangle2.getVertices()[1].set(obb2Vertices[obb1FacesVerticesIndexes[triangleObb2Idx][1]]);
					triangle2.getVertices()[2].set(obb2Vertices[obb1FacesVerticesIndexes[triangleObb2Idx][2]]);
					triangle2.update();
					for (int i = 0; i < 3; i++) {
						triangle1.computeClosestPointOnBoundingVolume(triangle2.getVertices()[i], hitPoint);
						collisionEntity.addHitPoint(hitPoint);
					}
					/*
					for (int i = 0; i < 3; i++) {
						triangle2.computeClosestPointOnBoundingVolume(triangle1.getVertices()[i], hitPoint);
						collisionEntity.addHitPoint(hitPoint);
					}
					*/
					break;
			}
		}
	}

	/**
	 * Compute hit points for intersecting obb1 with obb2
	 * @param obb1
	 * @param obb2
	 * @param collision entity
	 */
	public void computeHitPoints(Triangle triangle, OrientedBoundingBox obb, CollisionResponse.Entity collisionEntity) {
		Vector3[] triangleVertices = triangle.getVertices(); 
		Vector3[] obbVertices = obb.getVertices();
		int[][] obbFacesVerticesIndexes = obb.getFacesVerticesIndexes(); 
		for (int triangleObbIdx = 0; triangleObbIdx < obbFacesVerticesIndexes.length; triangleObbIdx++) {
			TriangleTriangleIntersection.ReturnValue tritriReturn = triangleTriangleIntersection.computeTriangleTriangleIntersection(
				triangleVertices[0],
				triangleVertices[1],
				triangleVertices[2],
				obbVertices[obbFacesVerticesIndexes[triangleObbIdx][0]],
				obbVertices[obbFacesVerticesIndexes[triangleObbIdx][1]],
				obbVertices[obbFacesVerticesIndexes[triangleObbIdx][2]],
				hitPointTriangle1,
				hitPointTriangle2
			);
			switch (tritriReturn) {
				case NOINTERSECTION:
					break;
				case INTERSECTION:
					collisionEntity.addHitPoint(hitPointTriangle1);
					collisionEntity.addHitPoint(hitPointTriangle2);
					break;
				case COPLANAR_INTERSECTION:
					triangle1.getVertices()[0].set(triangleVertices[0]);
					triangle1.getVertices()[1].set(triangleVertices[1]);
					triangle1.getVertices()[2].set(triangleVertices[2]);
					triangle1.update();
					triangle2.getVertices()[0].set(obbVertices[obbFacesVerticesIndexes[triangleObbIdx][0]]);
					triangle2.getVertices()[1].set(obbVertices[obbFacesVerticesIndexes[triangleObbIdx][1]]);
					triangle2.getVertices()[2].set(obbVertices[obbFacesVerticesIndexes[triangleObbIdx][2]]);
					triangle2.update();
					for (int i = 0; i < 3; i++) {
						triangle1.computeClosestPointOnBoundingVolume(triangle2.getVertices()[i], hitPoint);
						collisionEntity.addHitPoint(hitPoint);
					}
					/*
					for (int i = 0; i < 3; i++) {
						triangle2.computeClosestPointOnBoundingVolume(triangle1.getVertices()[i], hitPoint);
						collisionEntity.addHitPoint(hitPoint);
					}
					*/
					break;
			}
		}
	}

	/**
	 * Compute hit points for intersecting obb1 with obb2
	 * @param triangle 1
	 * @param triangle 2
	 * @param collision entity
	 */
	public void computeHitPoints(Triangle triangle1, Triangle triangle2, CollisionResponse.Entity collisionEntity) {
		Vector3[] triangle1Vertices = triangle1.getVertices(); 
		Vector3[] triangle2Vertices = triangle2.getVertices();
		TriangleTriangleIntersection.ReturnValue tritriReturn = triangleTriangleIntersection.computeTriangleTriangleIntersection(
			triangle1Vertices[0],
			triangle1Vertices[1],
			triangle1Vertices[2],
			triangle2Vertices[0],
			triangle2Vertices[1],
			triangle2Vertices[2],
			hitPointTriangle1,
			hitPointTriangle2
		);
		switch (tritriReturn) {
			case NOINTERSECTION:
				break;
			case INTERSECTION:
				collisionEntity.addHitPoint(hitPointTriangle1);
				collisionEntity.addHitPoint(hitPointTriangle2);
				break;
			case COPLANAR_INTERSECTION:
				triangle1.getVertices()[0].set(triangle1Vertices[0]);
				triangle1.getVertices()[1].set(triangle1Vertices[1]);
				triangle1.getVertices()[2].set(triangle1Vertices[2]);
				triangle1.update();
				triangle2.getVertices()[0].set(triangle2Vertices[0]);
				triangle2.getVertices()[1].set(triangle2Vertices[1]);
				triangle2.getVertices()[2].set(triangle2Vertices[2]);
				triangle2.update();
				for (int i = 0; i < 3; i++) {
					triangle1.computeClosestPointOnBoundingVolume(triangle2.getVertices()[i], hitPoint);
					collisionEntity.addHitPoint(hitPoint);
				}
				/*
				for (int i = 0; i < 3; i++) {
					triangle2.computeClosestPointOnBoundingVolume(triangle1.getVertices()[i], hitPoint);
					collisionEntity.addHitPoint(hitPoint);
				}
				*/
				break;
		}
	}

	/**
	 * Compact hit points
	 * @param collision response entity
	 */
	public void compactHitPoints(CollisionResponse collision) {
		/*
		CollisionResponse.Entity entity = collision.selectedEntity;
		if (entity == null) return;
		if (entity.hitPointsCount < 2) return;
		hitPoint.set(entity.hitPoints.get(0));
		float y = hitPoint.getY();
		for (int i = 1; i < entity.hitPointsCount; i++) {
			Vector3 currentHitPoint = entity.hitPoints.get(i); 
			if (Math.abs(currentHitPoint.getY() - y) > HITPOINT_TOLERANCE) {
				return;
			}
			hitPoint.add(currentHitPoint);
		}
		hitPoint.scale(1f / entity.hitPointsCount);
		entity.hitPointsCount = 1;
		entity.hitPoints.get(0).set(hitPoint);
		*/
	}

	/**
	 * Do broad test collision detection
	 * @param bounding volume 1
	 * @param bounding volume 2
	 * @return
	 */
	public boolean doBroadTest(BoundingVolume bv1, BoundingVolume bv2) {
		// do broad test
		return
			axis.set(
				bv1.getCenter()
			).
			sub(
				bv2.getCenter()
			).
			computeLengthSquared() <=
			(bv1.getSphereRadius() + bv2.getSphereRadius()) * (bv1.getSphereRadius() + bv2.getSphereRadius());
		
	}

	/**
	 * Checks if movement candidate if valid otherwise take movement for collision response computation
	 * @param normal candidate
	 * @param movement
	 * @param collision
	 * @return collision
	 */
	private static boolean checkMovementFallback(Vector3 normalCandidate, Vector3 movement, CollisionResponse collision) {
		// we can not act with no movement provided
		if (movement == null) {
			if (VERBOSE) {
				System.out.println("checkMovementFallback::fallback::movement = null");
				StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
				for (int i = 2; i < 10 && i < stackTraceElement.length; i++) {
					System.out.println(stackTraceElement[i].getClassName() + ":" + stackTraceElement[i].getLineNumber());
				}
			}
			return false;
		}

		// check if normal candidate is valid
		if (normalCandidate.equals(zeroVector) == true) {
			if (VERBOSE) {
				System.out.println("checkMovementFallback::fallback");
				StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
				for (int i = 2; i < 10 && i < stackTraceElement.length; i++) {
					System.out.println(stackTraceElement[i].getClassName() + ":" + stackTraceElement[i].getLineNumber());
				}
			}
			collision.reset();
			collision.addResponse(-movement.computeLength()).getNormal().set(movement).scale(-1f).normalize();
			return true;
		}

		// yep, return null
		return false;
	}

	/**
	 * Check collision validity
	 * @param collision
	 */
	private static void checkCollision(CollisionResponse collision) {
		float normalXYZ[] = collision.getNormal().getArray();
		if (Float.isNaN(normalXYZ[0]) == true ||
			Float.isNaN(normalXYZ[1]) == true ||
			Float.isNaN(normalXYZ[2]) == true) {
			//
			System.out.print("CollisionDetection::checkCollision(): BROKEN NORMAL @ ");
			StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
			for (int i = 2; i < 10 && i < stackTraceElement.length; i++) {
				System.out.println(stackTraceElement[i].getClassName() + ":" + stackTraceElement[i].getLineNumber());
			}
			System.out.println();
		}
	}

}
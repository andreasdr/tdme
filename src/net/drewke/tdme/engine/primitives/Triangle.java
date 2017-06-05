package net.drewke.tdme.engine.primitives;

import java.util.Arrays;

import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.physics.CollisionDetection;
import net.drewke.tdme.engine.physics.CollisionResponse;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;

/**
 * Triangle primitive
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Triangle implements BoundingVolume {

	protected Vector3[] vertices;
	protected Vector3 center;
	protected Vector3 closestPoint;
	protected Vector3 distanceVector;
	protected float sphereRadius;

	private Vector3 edge0;
	private Vector3 edge1;
	private Vector3 v0Point;

	/**
	 * Creates a triangle bounding volume
	 * @param vertex 0
	 * @param vertex 1
	 * @param vertex 2
	 * @return bounding volume
	 */
	public static BoundingVolume createBoundingVolume(Vector3 vertex0, Vector3 vertex1, Vector3 vertex2) {
		return new Triangle(vertex0, vertex1, vertex2);
	}

	/**
	 * Public constructor
	 * 	you should use the new bounding volume interface when using bounding volumes
	 * 	and not instantiate bounding volume classes directly
	 * @param vertices
	 */
	public Triangle(Vector3 vertex0, Vector3 vertex1, Vector3 vertex2) {
		this.vertices = new Vector3[3];
		this.vertices[0] = vertex0;
		this.vertices[1] = vertex1;
		this.vertices[2] = vertex2;
		this.center = new Vector3();
		this.closestPoint = new Vector3();
		this.distanceVector = new Vector3();
		edge0 = new Vector3();
		edge1 = new Vector3();
		v0Point = new Vector3();
		update();
	}

	/**
	 * @return triangle vertices
	 */
	public Vector3[] getVertices() {
		return vertices;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#fromBoundingVolume(net.drewke.tdme.engine.primitives.BoundingVolume)
	 */
	public void fromBoundingVolume(BoundingVolume original) {
		// check for same type of original
		if (original instanceof Triangle == false) {
			Console.println("Triangle::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Triangle triangle = (Triangle)original;

		//
		for (int i = 0; i < vertices.length; i++) vertices[i].set(triangle.vertices[i]);
		center.set(triangle.center);
		sphereRadius = triangle.sphereRadius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#fromBoundingVolumeWithTransformations(net.drewke.tdme.primitives.BoundingVolume, net.drewke.tdme.engine.Transformations)
	 */
	public void fromBoundingVolumeWithTransformations(BoundingVolume original, Transformations transformations) {
		// check for same type of original
		if (original instanceof Triangle == false) {
			Console.println("Triangle::fromBoundingVolumeWithTransformations(): original is not of same type");
			return;
		}

		//
		Triangle triangle = (Triangle)original;
		Matrix4x4 transformationsMatrix = transformations.getTransformationsMatrix();
		for (int i = 0; i < 3; i++) {
			transformationsMatrix.multiply(triangle.vertices[i], vertices[i]);
		}

		//
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeClosestPointOnBoundingVolume(net.drewke.tdme.math.Vector3, net.drewke.tdme.math.Vector3)
	 * 
	 * based on http://www.gamedev.net/topic/552906-closest-point-on-triangle/
	 */
	public void computeClosestPointOnBoundingVolume(Vector3 point, Vector3 closestPoint) {
		edge0.set(vertices[1]).sub(vertices[0]);
		edge1.set(vertices[2]).sub(vertices[0]);
		v0Point.set(vertices[0]).sub(point);

		float a = Vector3.computeDotProduct(edge0, edge0);
		float b = Vector3.computeDotProduct(edge0, edge1);
		float c = Vector3.computeDotProduct(edge1, edge1);
		float d = Vector3.computeDotProduct(edge0, v0Point);
		float e = Vector3.computeDotProduct(edge1, v0Point);

		float det = a * c - b * b;
		float s = b * e - c * d;
		float t = b * d - a * e;

		if (s + t < det) {
			if (s < 0.0f) {
				if (t < 0.0f) {
					if (d < 0.f) {
						s = MathTools.clamp(-d / a, 0.0f, 1.0f);
						t = 0.0f;
					} else {
						s = 0.0f;
						t = MathTools.clamp(-e / c, 0.0f, 1.0f);
					}
				} else {
					s = 0.0f;
					t = MathTools.clamp(-e / c, 0.0f, 1.0f);
				}
			} else if (t < 0.0f) {
				s = MathTools.clamp(-d / a, 0.0f, 1.0f);
				t = 0.0f;
			} else {
				float invDet = 1.0f / det;
				s *= invDet;
				t *= invDet;
			}
		} else {
			if (s < 0.0f) {
				float tmp0 = b + d;
				float tmp1 = c + e;
				if (tmp1 > tmp0) {
					float numer = tmp1 - tmp0;
					float denom = a - 2 * b + c;
					s = MathTools.clamp(numer / denom, 0.0f, 1.0f);
					t = 1 - s;
				} else {
					t = MathTools.clamp(-e / c, 0.0f, 1.0f);
					s = 0.0f;
				}
			} else if (t < 0.0f) {
				if (a + d > b + e) {
					float numer = c + e - b - d;
					float denom = a - 2 * b + c;
					s = MathTools.clamp(numer / denom, 0.0f, 1.f);
					t = 1 - s;
				} else {
					s = MathTools.clamp(-e / c, 0.0f, 1.0f);
					t = 0.f;
				}
			} else {
				float numer = c + e - b - d;
				float denom = a - 2 * b + c;
				s = MathTools.clamp(numer / denom, 0.0f, 1.f);
				t = 1.0f - s;
			}
		}
		closestPoint.
			set(vertices[0]). 
			add(edge0.scale(s)).
			add(edge1.scale(t));
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.primitives.BoundingVolume#containsPoint(net.drewke.tdme.math.Vector3)
	 */
	public boolean containsPoint(Vector3 point) {
		computeClosestPointOnBoundingVolume(point, closestPoint);
		return closestPoint.equals(point);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#doesCollideWith(net.drewke.tdme.engine.primitives.BoundingVolume, net.drewke.tdme.math.Vector3, net.drewke.tdme.engine.physics.CollisionResponse)
	 */
	public boolean doesCollideWith(BoundingVolume bv2, Vector3 movement, CollisionResponse collision) {
		if (bv2 instanceof BoundingBox) {
			return CollisionDetection.getInstance().doCollide(this, (BoundingBox)bv2, movement, collision);
		} else
		if (bv2 instanceof OrientedBoundingBox) {
			return CollisionDetection.getInstance().doCollide(this, (OrientedBoundingBox)bv2, movement, collision);
		} else
		if (bv2 instanceof Sphere) {
			return CollisionDetection.getInstance().doCollide(this, (Sphere)bv2, movement, collision);
		} else
		if (bv2 instanceof Capsule) {
			return CollisionDetection.getInstance().doCollide(this, (Capsule)bv2, movement, collision);
		} else
		if (bv2 instanceof Triangle) {
			return CollisionDetection.getInstance().doCollide(this, (Triangle)bv2, movement, collision);
		} else
		if (bv2 instanceof ConvexMesh) {
			return CollisionDetection.getInstance().doCollide(this, (ConvexMesh)bv2, movement, collision);
		} else {
			Console.println("Triangle::doesCollideWith(): unsupported bounding volume 2: " + bv2);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getCenter()
	 */
	public Vector3 getCenter() {
		return center;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#getSphereRadius()
	 */
	public float getSphereRadius() {
		return sphereRadius;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#computeDimensionOnAxis(net.drewke.tdme.math.Vector3)
	 */
	public float computeDimensionOnAxis(Vector3 axis) {
		float vertexOnAxis = Vector3.computeDotProduct(vertices[0], axis);
		float min = vertexOnAxis;
		float max = vertexOnAxis;
		for (int i = 1; i < vertices.length; i++) {
			vertexOnAxis = Vector3.computeDotProduct(vertices[i], axis);
			if (vertexOnAxis < min) min = vertexOnAxis;
			if (vertexOnAxis > max) max = vertexOnAxis;
		}
		return Math.abs(max-min);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.primitives.BoundingVolume#update()
	 */
	public void update() {
		// center
		this.center.
			set(vertices[0]).
			add(vertices[1]).
			add(vertices[2]).
			scale(1f / 3f);

		// sphere radius
		this.sphereRadius = 0f;
		for (int i = 0; i < vertices.length; i++) {
			float _sphereRadius = distanceVector.set(center).sub(vertices[i]).computeLength();
			if (_sphereRadius > sphereRadius) sphereRadius = _sphereRadius;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BoundingVolume clone() {
		return new Triangle(
			vertices[0].clone(),
			vertices[1].clone(),
			vertices[2].clone()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Triangle [vertices=" + Arrays.toString(vertices) + "]";
	}

}

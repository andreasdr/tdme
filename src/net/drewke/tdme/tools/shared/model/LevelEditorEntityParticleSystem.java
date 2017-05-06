package net.drewke.tdme.tools.shared.model;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;

/**
 * Level editor entity particle system
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorEntityParticleSystem {

	// type
	public enum Type {
		NONE,
		OBJECT_PARTICLE_SYSTEM, 
		POINT_PARTICLE_SYSTEM
	};

	/**
	 * Object particle system
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class ObjectParticleSystem {

		private Vector3 scale;
		private int maxCount;
		private String model;

		/**
		 * Public constructor
		 */
		public ObjectParticleSystem() {
			scale = new Vector3(1f, 1f, 1f);
			maxCount = 10;
			model = null;
		}

		/**
		 * @return scale
		 */
		public Vector3 getScale() {
			return scale;
		}

		/**
		 * Set scale
		 * @param scale
		 */
		public void setScale(Vector3 scale) {
			this.scale = scale;
		}

		/**
		 * @return max count
		 */
		public int getMaxCount() {
			return maxCount;
		}

		/**
		 * Set max count
		 * @param max count
		 */
		public void setMaxCount(int maxCount) {
			this.maxCount = maxCount;
		}

		/**
		 * @return model
		 */
		public String getModel() {
			return model;
		}

		/**
		 * Set model
		 * @param model
		 */
		public void setModel(String model) {
			this.model = model;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "ObjectParticleSystem [scale=" + scale + ", maxCount="
					+ maxCount + ", model=" + model + "]";
		}
	}

	/**
	 * Point particle system
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class PointParticleSystem {

		private int maxPoints;

		/**
		 * Public constructor
		 */
		public PointParticleSystem() {
			maxPoints = 4000;
		}

		/**
		 * @return max points
		 */
		public int getMaxPoints() {
			return maxPoints;
		}

		/**
		 * Set max points
		 * @param max points
		 */
		public void setMaxPoints(int maxPoints) {
			this.maxPoints = maxPoints;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "PointParticleSystem [maxPoints=" + maxPoints + "]";
		}
	}

	// emitter
	public enum Emitter{
		NONE,
		POINT_PARTICLE_EMITTER,
		BOUNDINGBOX_PARTICLE_EMITTER,
		CIRCLE_PARTICLE_EMITTER,
		CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY,
		SPHERE_PARTICLE_EMITTER
	};

	/**
	 * Point particle emitter
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class PointParticleEmitter {

		private int count;
		private long lifeTime;
		private long lifeTimeRnd;
		private float mass;
		private float massRnd;
		private Vector3 position;
		private Vector3 velocity;
		private Vector3 velocityRnd;
		private Color4 colorStart;
		private Color4 colorEnd;

		/**
		 * Public constructor
		 */
		public PointParticleEmitter() {
			count = 2000;
			lifeTime = 1500;
			lifeTimeRnd = 500;
			mass = 0.001f;
			massRnd = 0.001f;
			position = new Vector3(0f,0f,0f);
			velocity = new Vector3(1f,1f,1f);
			velocityRnd = new Vector3(0.5f,0.5f,0.5f);
			colorStart = new Color4(1f, 1f, 1f, 1f);
			colorEnd = new Color4(1f, 1f, 1f, 1f);
		}

		/**
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Set count
		 * @param count
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return life time
		 */
		public long getLifeTime() {
			return lifeTime;
		}

		/**
		 * Set life time
		 * @param life time
		 */
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * @return life time rnd
		 */
		public long getLifeTimeRnd() {
			return lifeTimeRnd;
		}

		/**
		 * Set life time rnd
		 * @param life time rnd
		 */
		public void setLifeTimeRnd(long lifeTimeRnd) {
			this.lifeTimeRnd = lifeTimeRnd;
		}

		/**
		 * @return mass
		 */
		public float getMass() {
			return mass;
		}

		/**
		 * Set mass
		 * @param mass
		 */
		public void setMass(float mass) {
			this.mass = mass;
		}

		/**
		 * @return mass rnd
		 */
		public float getMassRnd() {
			return massRnd;
		}

		/**
		 * Set mass rnd
		 * @param mass rnd
		 */
		public void setMassRnd(float massRnd) {
			this.massRnd = massRnd;
		}

		/**
		 * @return position
		 */
		public Vector3 getPosition() {
			return position;
		}

		/**
		 * @return velocity
		 */
		public Vector3 getVelocity() {
			return velocity;
		}

		/**
		 * @return velocity rnd
		 */
		public Vector3 getVelocityRnd() {
			return velocityRnd;
		}

		/**
		 * @return color start
		 */
		public Color4 getColorStart() {
			return colorStart;
		}

		/**
		 * @return color end
		 */
		public Color4 getColorEnd() {
			return colorEnd;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "PointParticleEmitter [count=" + count + ", lifeTime="
					+ lifeTime + ", lifeTimeRnd=" + lifeTimeRnd + ", mass="
					+ mass + ", massRnd=" + massRnd + ", position=" + position
					+ ", velocity=" + velocity + ", velocityRnd=" + velocityRnd
					+ ", colorStart=" + colorStart + ", colorEnd=" + colorEnd
					+ "]";
		}
	}

	/**
	 * Bounding box particle emitter 
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class BoundingBoxParticleEmitter {

		private int count;
		private long lifeTime;
		private long lifeTimeRnd;
		private float mass;
		private float massRnd;
		private Vector3 velocity;
		private Vector3 velocityRnd;
		private Color4 colorStart;
		private Color4 colorEnd;
		private Vector3 obbCenter;
		private Vector3 obbHalfextension;
		private Vector3 obbAxis0;
		private Vector3 obbAxis1;
		private Vector3 obbAxis2;

		/**
		 * Public constructor
		 */
		public BoundingBoxParticleEmitter() {
			count = 2000;
			lifeTime = 1500;
			lifeTimeRnd = 500;
			mass = 0.001f;
			massRnd = 0.001f;
			velocity = new Vector3(1f,1f,1f);
			velocityRnd = new Vector3(0.5f,0.5f,0.5f);
			colorStart = new Color4(1f, 1f, 1f, 1f);
			colorEnd = new Color4(1f, 1f, 1f, 1f);
			obbCenter = new Vector3(0f,0f,0f);
			obbHalfextension = new Vector3(0.5f, 0.5f, 0.5f);
			obbAxis0 = new Vector3(1f,0f,0f);
			obbAxis1 = new Vector3(0f,1f,0f);
			obbAxis2 = new Vector3(0f,0f,1f);
		}

		/**
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Set count
		 * @param count
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return life time
		 */
		public long getLifeTime() {
			return lifeTime;
		}

		/**
		 * Set life time
		 * @param life time
		 */
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * @return life time rnd
		 */
		public long getLifeTimeRnd() {
			return lifeTimeRnd;
		}

		/**
		 * Set life time rnd
		 * @param life time rnd
		 */
		public void setLifeTimeRnd(long lifeTimeRnd) {
			this.lifeTimeRnd = lifeTimeRnd;
		}

		/**
		 * @return mass
		 */
		public float getMass() {
			return mass;
		}

		/**
		 * Set mass
		 * @param mass
		 */
		public void setMass(float mass) {
			this.mass = mass;
		}

		/**
		 * @return mass rnd
		 */
		public float getMassRnd() {
			return massRnd;
		}

		/**
		 * Set mass rnd
		 * @param mass rnd
		 */
		public void setMassRnd(float massRnd) {
			this.massRnd = massRnd;
		}

		/**
		 * @return velocity
		 */
		public Vector3 getVelocity() {
			return velocity;
		}

		/**
		 * @return velocity rnd
		 */
		public Vector3 getVelocityRnd() {
			return velocityRnd;
		}

		/**
		 * @return color start
		 */
		public Color4 getColorStart() {
			return colorStart;
		}

		/**
		 * @return color end
		 */
		public Color4 getColorEnd() {
			return colorEnd;
		}

		/**
		 * @return obb center
		 */
		public Vector3 getObbCenter() {
			return obbCenter;
		}

		/**
		 * @return obb half extension
		 */
		public Vector3 getObbHalfextension() {
			return obbHalfextension;
		}

		/**
		 * @return obb axis 0
		 */
		public Vector3 getObbAxis0() {
			return obbAxis0;
		}

		/**
		 * @return obb axis 1
		 */
		public Vector3 getObbAxis1() {
			return obbAxis1;
		}

		/**
		 * @return obb axis 2
		 */
		public Vector3 getObbAxis2() {
			return obbAxis2;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "BoundingBoxParticleEmitter [count=" + count + ", lifeTime="
					+ lifeTime + ", lifeTimeRnd=" + lifeTimeRnd + ", mass="
					+ mass + ", massRnd=" + massRnd + ", velocity=" + velocity
					+ ", velocityRnd=" + velocityRnd + ", colorStart="
					+ colorStart + ", colorEnd=" + colorEnd + ", obbCenter="
					+ obbCenter + ", obbHalfextension=" + obbHalfextension
					+ ", obbAxis0=" + obbAxis0 + ", obbAxis1=" + obbAxis1
					+ ", obbAxis2=" + obbAxis2 + "]";
		}
	}

	/**
	 * Circle particle emitter
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class CircleParticleEmitter {

		private int count;
		private long lifeTime;
		private long lifeTimeRnd;
		private float mass;
		private float massRnd;
		private Vector3 velocity;
		private Vector3 velocityRnd;
		private Color4 colorStart;
		private Color4 colorEnd;
		private Vector3 center;
		private float radius;
		private Vector3 axis0;
		private Vector3 axis1;

		/**
		 * Public constructor
		 */
		public CircleParticleEmitter() {
			count = 2000;
			lifeTime = 1500;
			lifeTimeRnd = 500;
			mass = 0.001f;
			massRnd = 0.001f;
			velocity = new Vector3(1f,1f,1f);
			velocityRnd = new Vector3(0.5f,0.5f,0.5f);
			colorStart = new Color4(1f, 1f, 1f, 1f);
			colorEnd = new Color4(1f, 1f, 1f, 1f);
			center = new Vector3(0f,0f,0f);
			radius = 0.5f;
			axis0 = new Vector3(1f,0f,0f);
			axis1 = new Vector3(0f,0f,1f);
		}

		/**
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Set count
		 * @param count
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return life time
		 */
		public long getLifeTime() {
			return lifeTime;
		}

		/**
		 * Set life time
		 * @param life time
		 */
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * @return life time rnd
		 */
		public long getLifeTimeRnd() {
			return lifeTimeRnd;
		}

		/**
		 * Set life time rnd
		 * @param life time rnd
		 */
		public void setLifeTimeRnd(long lifeTimeRnd) {
			this.lifeTimeRnd = lifeTimeRnd;
		}

		/**
		 * @return mass
		 */
		public float getMass() {
			return mass;
		}

		/**
		 * Set mass
		 * @param mass
		 */
		public void setMass(float mass) {
			this.mass = mass;
		}

		/**
		 * @return mass rnd
		 */
		public float getMassRnd() {
			return massRnd;
		}

		/**
		 * Set mass rnd
		 * @param mass rnd
		 */
		public void setMassRnd(float massRnd) {
			this.massRnd = massRnd;
		}

		/**
		 * @return velocity
		 */
		public Vector3 getVelocity() {
			return velocity;
		}

		/**
		 * @return velocity rnd
		 */
		public Vector3 getVelocityRnd() {
			return velocityRnd;
		}

		/**
		 * @return color start
		 */
		public Color4 getColorStart() {
			return colorStart;
		}

		/**
		 * @return color end
		 */
		public Color4 getColorEnd() {
			return colorEnd;
		}

		/**
		 * @return center
		 */
		public Vector3 getCenter() {
			return center;
		}

		/**
		 * @return radius
		 */
		public float getRadius() {
			return radius;
		}

		/**
		 * Set radius
		 * @param radius
		 */
		public void setRadius(float radius) {
			this.radius = radius;
		}

		/**
		 * @return axis 0
		 */
		public Vector3 getAxis0() {
			return axis0;
		}

		/**
		 * @return axis 1
		 */
		public Vector3 getAxis1() {
			return axis1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "CircleParticleEmitter [count=" + count + ", lifeTime="
					+ lifeTime + ", lifeTimeRnd=" + lifeTimeRnd + ", mass="
					+ mass + ", massRnd=" + massRnd + ", velocity=" + velocity
					+ ", velocityRnd=" + velocityRnd + ", colorStart="
					+ colorStart + ", colorEnd=" + colorEnd + ", center="
					+ center + ", radius=" + radius + ", axis0=" + axis0
					+ ", axis1=" + axis1 + "]";
		}
	}

	/**
	 * Circle particle emitter plane velocity
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class CircleParticleEmitterPlaneVelocity {

		private int count;
		private long lifeTime;
		private long lifeTimeRnd;
		private float mass;
		private float massRnd;
		private Vector3 velocity;
		private Vector3 velocityRnd;
		private Color4 colorStart;
		private Color4 colorEnd;
		private Vector3 center;
		private float radius;
		private Vector3 axis0;
		private Vector3 axis1;

		/**
		 * Public constructor
		 */
		public CircleParticleEmitterPlaneVelocity() {
			count = 2000;
			lifeTime = 1500;
			lifeTimeRnd = 500;
			mass = 0.001f;
			massRnd = 0.001f;
			velocity = new Vector3(1f,1f,1f);
			velocityRnd = new Vector3(0.5f,0.5f,0.5f);
			colorStart = new Color4(1f, 1f, 1f, 1f);
			colorEnd = new Color4(1f, 1f, 1f, 1f);
			center = new Vector3(0f,0f,0f);
			radius = 0.5f;
			axis0 = new Vector3(1f,0f,0f);
			axis1 = new Vector3(0f,0f,1f);
		}

		/**
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Set count
		 * @param count
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return life time
		 */
		public long getLifeTime() {
			return lifeTime;
		}

		/**
		 * Set life time
		 * @param life time
		 */
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * @return life time rnd
		 */
		public long getLifeTimeRnd() {
			return lifeTimeRnd;
		}

		/**
		 * Set life time rnd
		 * @param life time rnd
		 */
		public void setLifeTimeRnd(long lifeTimeRnd) {
			this.lifeTimeRnd = lifeTimeRnd;
		}

		/**
		 * @return mass
		 */
		public float getMass() {
			return mass;
		}

		/**
		 * Set mass
		 * @param mass
		 */
		public void setMass(float mass) {
			this.mass = mass;
		}

		/**
		 * @return mass rnd
		 */
		public float getMassRnd() {
			return massRnd;
		}

		/**
		 * Set mass rnd
		 * @param mass rnd
		 */
		public void setMassRnd(float massRnd) {
			this.massRnd = massRnd;
		}

		/**
		 * @return velocity
		 */
		public Vector3 getVelocity() {
			return velocity;
		}

		/**
		 * @return velocity rnd
		 */
		public Vector3 getVelocityRnd() {
			return velocityRnd;
		}

		/**
		 * @return color start
		 */
		public Color4 getColorStart() {
			return colorStart;
		}

		/**
		 * @return color end
		 */
		public Color4 getColorEnd() {
			return colorEnd;
		}

		/**
		 * @return center
		 */
		public Vector3 getCenter() {
			return center;
		}

		/**
		 * @return radius
		 */
		public float getRadius() {
			return radius;
		}

		/**
		 * Set radius
		 * @param radius
		 */
		public void setRadius(float radius) {
			this.radius = radius;
		}

		/**
		 * @return axis 0
		 */
		public Vector3 getAxis0() {
			return axis0;
		}

		/**
		 * @return axis 1
		 */
		public Vector3 getAxis1() {
			return axis1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "CircleParticleEmitterPlaneVelocity [count=" + count + ", lifeTime="
					+ lifeTime + ", lifeTimeRnd=" + lifeTimeRnd + ", mass="
					+ mass + ", massRnd=" + massRnd + ", velocity=" + velocity
					+ ", velocityRnd=" + velocityRnd + ", colorStart="
					+ colorStart + ", colorEnd=" + colorEnd + ", center="
					+ center + ", radius=" + radius + ", axis0=" + axis0
					+ ", axis1=" + axis1 + "]";
		}

	}

	/**
	 * Sphere particle emitter
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	public static class SphereParticleEmitter {

		private int count;
		private long lifeTime;
		private long lifeTimeRnd;
		private float mass;
		private float massRnd;
		private Vector3 velocity;
		private Vector3 velocityRnd;
		private Color4 colorStart;
		private Color4 colorEnd;
		private Vector3 center;
		private float radius;

		/**
		 * Public constructor
		 */
		public SphereParticleEmitter() {
			count = 2000;
			lifeTime = 1500;
			lifeTimeRnd = 500;
			mass = 0.001f;
			massRnd = 0.001f;
			velocity = new Vector3(1f,1f,1f);
			velocityRnd = new Vector3(0.5f,0.5f,0.5f);
			colorStart = new Color4(1f, 1f, 1f, 1f);
			colorEnd = new Color4(1f, 1f, 1f, 1f);
			center = new Vector3(0f,0f,0f);
			radius = 0.5f;
		}

		/**
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Set count
		 * @param count
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return life time
		 */
		public long getLifeTime() {
			return lifeTime;
		}

		/**
		 * Set life time
		 * @param life time
		 */
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * @return life time rnd
		 */
		public long getLifeTimeRnd() {
			return lifeTimeRnd;
		}

		/**
		 * Set life time rnd
		 * @param life time rnd
		 */
		public void setLifeTimeRnd(long lifeTimeRnd) {
			this.lifeTimeRnd = lifeTimeRnd;
		}

		/**
		 * @return mass
		 */
		public float getMass() {
			return mass;
		}

		/**
		 * Set mass
		 * @param mass
		 */
		public void setMass(float mass) {
			this.mass = mass;
		}

		/**
		 * @return mass rnd
		 */
		public float getMassRnd() {
			return massRnd;
		}

		/**
		 * Set mass rnd
		 * @param mass rnd
		 */
		public void setMassRnd(float massRnd) {
			this.massRnd = massRnd;
		}

		/**
		 * @return velocity
		 */
		public Vector3 getVelocity() {
			return velocity;
		}

		/**
		 * @return velocity rnd
		 */
		public Vector3 getVelocityRnd() {
			return velocityRnd;
		}

		/**
		 * @return color start
		 */
		public Color4 getColorStart() {
			return colorStart;
		}

		/**
		 * @return color end
		 */
		public Color4 getColorEnd() {
			return colorEnd;
		}

		/**
		 * @return center
		 */
		public Vector3 getCenter() {
			return center;
		}

		/**
		 * @return radius
		 */
		public float getRadius() {
			return radius;
		}

		/**
		 * Set radius
		 * @param radius
		 */
		public void setRadius(float radius) {
			this.radius = radius;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "SphereParticleEmitter [count=" + count + ", lifeTime="
					+ lifeTime + ", lifeTimeRnd=" + lifeTimeRnd + ", mass="
					+ mass + ", massRnd=" + massRnd + ", velocity=" + velocity
					+ ", velocityRnd=" + velocityRnd + ", colorStart="
					+ colorStart + ", colorEnd=" + colorEnd + ", center="
					+ center + ", radius=" + radius + "]";
		}

	}

	private Type type;
	private ObjectParticleSystem ops;
	private PointParticleSystem pps;

	private Emitter emitter;
	private PointParticleEmitter ppe;
	private BoundingBoxParticleEmitter bbpe;
	private CircleParticleEmitter cpe;
	private CircleParticleEmitterPlaneVelocity cpepv;
	private SphereParticleEmitter spe;

	/**
	 * Public constructor
	 */
	public LevelEditorEntityParticleSystem() {
		type = Type.NONE;
		ops = null;
		pps = null;
		emitter = Emitter.NONE;
		ppe = null;
		bbpe = null;
		cpe = null;
		cpepv = null;
		spe = null;
	}

	/**
	 * @return particle system type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set particle system type
	 * @param type
	 */
	public void setType(Type type) {
		// unset old particle system type entity
		switch (this.type) {
			case NONE:
				break;
			case OBJECT_PARTICLE_SYSTEM:
				ops = null;
				break;
			case POINT_PARTICLE_SYSTEM:
				pps = null;
				break;
			default:
				System.out.println("LevelEditorEntityParticleSystem::setType(): unknown type '" + this.type + "'");
		}

		// new type
		this.type = type;

		// set new particle system type entity
		switch (this.type) {
			case NONE:
				break;
			case OBJECT_PARTICLE_SYSTEM:
				ops = new ObjectParticleSystem();
				break;
			case POINT_PARTICLE_SYSTEM:
				pps = new PointParticleSystem();
				break;
			default:
				System.out.println("LevelEditorEntityParticleSystem::setType(): unknown type '" + this.type + "'");
		}
	}

	/**
	 * @return object particle system
	 */
	public ObjectParticleSystem getObjectParticleSystem() {
		return ops;
	}

	/**
	 * @return point particle system
	 */
	public PointParticleSystem getPointParticleSystem() {
		return pps;
	}

	/**
	 * @return particle system emitter
	 */
	public Emitter getEmitter() {
		return emitter;
	}

	/**
	 * Set emitter
	 * @param emitter
	 */
	public void setEmitter(Emitter emitter) {
		// unset old emitter
		switch (this.emitter) {
			case NONE:
				break;
			case POINT_PARTICLE_EMITTER:
				ppe = null;
				break;
			case BOUNDINGBOX_PARTICLE_EMITTER:
				bbpe = null;
				break;
			case CIRCLE_PARTICLE_EMITTER:
				cpe = null;
				break;
			case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY:
				cpepv = null;
				break;
			case SPHERE_PARTICLE_EMITTER:
				spe = null;
				break;
			default:
				System.out.println("LevelEditorEntityParticleSystem::setEmitter(): unknown emitter '" + this.emitter + "'");
		}

		// new emitter
		this.emitter = emitter;

		// set new emitter
		switch (this.emitter) {
			case NONE:
				break;
			case POINT_PARTICLE_EMITTER:
				ppe = new PointParticleEmitter();
				break;
			case BOUNDINGBOX_PARTICLE_EMITTER:
				bbpe = new BoundingBoxParticleEmitter();
				break;
			case CIRCLE_PARTICLE_EMITTER:
				cpe = new CircleParticleEmitter();
				break;
			case CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY:
				cpepv = new CircleParticleEmitterPlaneVelocity();
				break;
			case SPHERE_PARTICLE_EMITTER:
				spe = new SphereParticleEmitter();
				break;
			default:
				System.out.println("LevelEditorEntityParticleSystem::setEmitter(): unknown emitter '" + this.emitter + "'");
			}
	}

	/**
	 * @return point particle emitter
	 */
	public PointParticleEmitter getPointParticleEmitter() {
		return ppe;
	}

	/**
	 * @return bounding box particle emitter
	 */
	public BoundingBoxParticleEmitter getBoundingBoxParticleEmitters() {
		return bbpe;
	}

	/**
	 * @return circle particle emitter
	 */
	public CircleParticleEmitter getCircleParticleEmitter() {
		return cpe;
	}

	/**
	 * @return circle particle emitter plane velocity
	 */
	public CircleParticleEmitterPlaneVelocity getCircleParticleEmitterPlaneVelocity() {
		return cpepv;
	}

	/**
	 * @return sphere particle emitter
	 */
	public SphereParticleEmitter getSphereParticleEmitter() {
		return spe;
	}

}

package net.drewke.tdme.tools.shared.model;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;

/**
 * Level editor entity particle system
 * @author Andreas Drewke
 * @version $Id$
 */
public class LevelEditorEntityParticleSystem {

	public enum Type {
		OBJECT_PARTICLE_SYSTEM, 
		POINT_PARTICLE_SYSTEM
	};

	public static class ObjectParticleSystem {
		private Vector3 scale;
		private int maxCount;
		private String model;
	}

	public static class PointParticleSystem {
		private int maxPoints;
	}

	public enum Emitter{
		POINT_PARTICLE_EMITTER,
		BOUNDINGBOX_PARTICLE_EMITTER,
		CIRCLE_PARTICLE_EMITTER,
		CIRCLE_PARTICLE_EMITTER_PLANE_VELOCITY,
		SPHERE_PARTICLE_EMITTER
	};

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
	}

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
	}

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
	}

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
	}

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

}

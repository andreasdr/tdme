package net.drewke.tdme.engine;

import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.subsystems.object.Object3DInternal;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;

/**
 * Object 3D
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Object3D extends Object3DInternal implements Entity {

	private Engine engine;

	/**
	 * Public constructor
	 * @param id
	 * @param model
	 */
	public Object3D(String id, Model model) {
		super(id, model);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#setEngine(net.drewke.tdme.engine.Engine)
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.Entity#setRenderer(net.drewke.tdme.engine.subsystems.renderer.GLRenderer)
	 */
	public void setRenderer(GLRenderer renderer) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.object.Object3DInternal#fromTransformations(net.drewke.tdme.engine.Transformations)
	 */
	public void fromTransformations(Transformations transformations) {
		super.fromTransformations(transformations);
		if (engine != null) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.object.Object3DInternal#update()
	 */
	public void update() {
		super.update();
		if (engine != null) engine.partition.updateEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.engine.subsystems.object.Object3DInternal#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == true) {
			if (engine != null) engine.partition.addEntity(this);
		} else {
			if (engine != null) engine.partition.removeEntity(this);
		}
	}

}

package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Engine.AnimationProcessingTarget;
import net.drewke.tdme.engine.model.Model;

/**
 * Object 3D Model
 * 	To be used in non engine context
 * @author Andreas Drewke
 * @version $Id$
 */
public class Object3DModelInternal extends Object3DBase {

	/**
	 * Public constructor
	 * @param model
	 */
	public Object3DModelInternal(Model model) {
		super(model, false, Engine.AnimationProcessingTarget.CPU_NORENDERING);
	}

}

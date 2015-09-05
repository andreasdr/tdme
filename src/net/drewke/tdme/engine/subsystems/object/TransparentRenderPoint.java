package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;

/**
 * Transparent point to be rendered
 * @author Andreas Drewke
 * @version $Id$
 */
public final class TransparentRenderPoint implements Comparable<TransparentRenderPoint> {

	protected boolean acquired;
	protected Vector3 point;
	protected Color4 color;
	protected float distanceFromCamera;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TransparentRenderPoint point2) {
		if (acquired == false && point2.acquired == false) return 0; else
		if (acquired == false) return +1; else
		if (point2.acquired == false) return -1; else
		if (distanceFromCamera > point2.distanceFromCamera) return -1; else
		if (distanceFromCamera < point2.distanceFromCamera) return +1; else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "TransparentPoint [acquired=" + acquired + ", point=" + point
				+ ", color=" + color + ", distanceFromCamera="
				+ distanceFromCamera + "]";
	}

}

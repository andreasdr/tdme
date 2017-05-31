package net.drewke.tdme.engine.subsystems.object;

/**
 * Transparent face to be rendered
 * @author Andreas Drewke
 * @version $Id$
 */
public final class TransparentRenderFace implements Comparable<TransparentRenderFace> {

	protected Object3DGroup object3DGroup;
	protected int facesEntityIdx;
	protected int faceIdx;
	protected float distanceFromCamera;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TransparentRenderFace face2) {
		if (distanceFromCamera > face2.distanceFromCamera) return -1; else
		if (distanceFromCamera < face2.distanceFromCamera) return +1; else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "TransparentRenderFace [group=" + object3DGroup.group.getName()
				+ ", facesEntityIdx=" + facesEntityIdx + ", faceIdx=" + faceIdx
				+ ", distanceFromCamera=" + distanceFromCamera + "]";
	}

}

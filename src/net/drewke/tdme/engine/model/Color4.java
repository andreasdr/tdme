package net.drewke.tdme.engine.model;

/**
 * Color 4 definition
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Color4 extends Color4Base {

	/**
	 * Public constructor
	 */
	public Color4() {
		super();
	}

	/**
	 * Public constructor
	 * @param color
	 */
	public Color4(Color4 color) {
		super(color);
	}

	/**
	 * Public constructor
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public Color4(float r, float g, float b, float a) {
		super(r, g, b, a);
	}

	/**
	 * Public constructor
	 * @param color
	 */
	public Color4(float[] color) {
		super(color);
	}

}

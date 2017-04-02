package net.drewke.tdme.engine.model;

import java.util.Arrays;

import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;

/**
 * Color 4 base definition
 * @author Andreas Drewke
 * @version $Id$
 */
public class Color4Base {

	protected float[] data;

	/**
	 * Public constructor
	 */
	public Color4Base() {
		this.data = new float[4];
	}

	/**
	 * Public constructor
	 * @param color
	 */
	public Color4Base(Color4Base color) {
		this.data = new float[4];
		System.arraycopy(color.data, 0, data, 0, Math.min(color.data.length, data.length));
	}

	/**
	 * Public constructor
	 * @param color
	 */
	public Color4Base(float[] color) {
		this.data = new float[4];
		System.arraycopy(color, 0, this.data, 0, Math.min(color.length, data.length));
	}

	/**
	 * Public constructor
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public Color4Base(float r, float g, float b, float a) {
		data = new float[4];
		data[0] = r;
		data[1] = g;
		data[2] = b;
		data[3] = a;
	}

	/**
	 * Set up color
	 * @param color float array
	 */
	public void set(float[] color) {
		System.arraycopy(color, 0, this.data, 0, Math.min(color.length, data.length));
	}

	/**
	 * Set up color
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void set(float r, float g, float b, float a) {
		data[0] = r;
		data[1] = g;
		data[2] = b;
		data[3] = a;
	}

	/**
	 * Sets up this color with given color
	 * @param color
	 */
	public void set(Color4Base color) {
		System.arraycopy(color.data, 0, data, 0, 4);
	}

	/**
	 * Add to color
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void add(float r, float g, float b, float a) {
		data[0]+= r;
		data[1]+= g;
		data[2]+= b;
		data[3]+= a;
	}

	/**
	 * @return red component
	 */
	public float getRed() {
		return data[0];
	}

	/**
	 * @param red component
	 */
	public void setRed(float red) {
		data[0] = red;
	}

	/**
	 * @return green component
	 */
	public float getGreen() {
		return data[1];
	}

	/**
	 * @param green component
	 */
	public void setGreen(float green) {
		data[1] = green;
	}

	/**
	 * @return blue component
	 */
	public float getBlue() {
		return data[2];
	}

	/**
	 * @param blue component
	 */
	public void setBlue(float blue) {
		data[2] = blue;
	}

	/**
	 * @return alpha component
	 */
	public float getAlpha() {
		return data[3];
	}

	/**
	 * @param alpha component
	 */
	public void setAlpha(float alpha) {
		data[3] = alpha;
	}

	/**
	 * @return rgba as float array 
	 */
	public float[] getArray() {
		return data;
	}

	/**
	 * Compares this color with given color
	 * @return equality
	 */
	public boolean equals(Color4Base c) {
		return
			(this == c) ||
			(Math.abs(data[0] - c.data[0]) < MathTools.EPSILON &&
			Math.abs(data[1] - c.data[1]) < MathTools.EPSILON &&
			Math.abs(data[2] - c.data[2]) < MathTools.EPSILON &&
			Math.abs(data[3] - c.data[3]) < MathTools.EPSILON);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Color4 [data=" + Arrays.toString(data) + "]";
	}

}

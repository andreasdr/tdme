package net.drewke.tdme.tools.leveleditor.model;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;

/**
 * Light 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorLight {

	private int id;
	private boolean enabled = false;
	private Color4 ambient = new Color4(0f,0f,0f,1f);
	private Color4 diffuse = new Color4(1f,1f,1f,1f);
	private Color4 specular = new Color4(1f,1f,1f,1f);
	private Vector4 position = new Vector4(0f,0f,0f,0f);
	private Vector3 spotTo = new Vector3(0f,0f,-1f);
	private Vector3 spotDirection = new Vector3(0f,0f,-1f);
	private float spotExponent = 0f;
	private float spotCutOff = 180f;
	private float constantAttenuation = 1f;
	private float linearAttenuation = 0f;
	private float quadraticAttenuation = 0f;

	/**
	 * Public default constructor
	 * @param id
	 */
	public LevelEditorLight(int id) {
		this.id = id;
	}

	/**
	 * @return light id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set enabled
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return ambient light
	 */
	public Color4 getAmbient() {
		return ambient;
	}

	/**
	 * Diffuse light 
	 * @return diffuse light
	 */
	public Color4 getDiffuse() {
		return diffuse;
	}

	/**
	 * @return specular light
	 */
	public Color4 getSpecular() {
		return specular;
	}

	/**
	 * @return position of light
	 */
	public Vector4 getPosition() {
		return position;
	}

	/**
	 * @return spot to
	 */
	public Vector3 getSpotTo() {
		return spotTo;
	}

	/**
	 * @return spot direction
	 */
	public Vector3 getSpotDirection() {
		return spotDirection;
	}

	/**
	 * @return spot exponent
	 */
	public float getSpotExponent() {
		return spotExponent;
	}

	/**
	 * set up spot exponent 
	 * @param spot exponent
	 */
	public void setSpotExponent(float spotExponent) {
		this.spotExponent = spotExponent;
	}

	/**
	 * @return spot cutoff
	 */
	public float getSpotCutOff() {
		return spotCutOff;
	}

	/**
	 * set spot cut off
	 * @param spot cut off
	 */
	public void setSpotCutOff(float spotCutOff) {
		this.spotCutOff = spotCutOff;
	}

	/**
	 * @return constant attenuation
	 */
	public float getConstantAttenuation() {
		return constantAttenuation;
	}

	/**
	 * set up constant attenuation
	 * @param constant attenuation
	 */
	public void setConstantAttenuation(float constantAttenuation) {
		this.constantAttenuation = constantAttenuation;
	}

	/**
	 * @return linear attenuation
	 */
	public float getLinearAttenuation() {
		return linearAttenuation;
	}

	/**
	 * set up linear attenuation
	 * @param linarAttenuation
	 */
	public void setLinearAttenuation(float linarAttenuation) {
		this.linearAttenuation = linarAttenuation;
	}

	/**
	 * @return quadratic attenuation
	 */
	public float getQuadraticAttenuation() {
		return quadraticAttenuation;
	}

	/**
	 * set up quadratic attenuation
	 * @param quadraticAttenuation
	 */
	public void setQuadraticAttenuation(float quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "LevelEditorLight [id=" + id + ", enabled=" + enabled
				+ ", ambient=" + ambient + ", diffuse=" + diffuse
				+ ", specular=" + specular + ", position=" + position
				+ ", spotDirection=" + spotDirection + ", spotExponent="
				+ spotExponent + ", spotCutOff=" + spotCutOff
				+ ", constantAttenuation=" + constantAttenuation
				+ ", linearAttenuation=" + linearAttenuation
				+ ", quadraticAttenuation=" + quadraticAttenuation + "]";
	}

}
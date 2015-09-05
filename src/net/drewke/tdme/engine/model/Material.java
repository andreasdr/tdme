package net.drewke.tdme.engine.model;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.math.MathTools;

/**
 * Represents a object material
 * @author andreas.drewke
 * @version $Id$
 */
public final class Material {

	private final static String defaultMaterialId = "tdme.default_material";
	private final static Material defaultMaterial = new Material(defaultMaterialId); 

	/**
	 * @return default material
	 */
	public static Material getDefaultMaterial() {
		return defaultMaterial;
	}

	private String id;
	private Color4 ambientColor;
	private Color4 diffuseColor;
	private Color4 specularColor;
	private Color4 emissionColor;
	private float shininess;
	private Texture diffuseTexture;
	private boolean diffuseTextureTransparency;
	private Texture normalTexture;
	private Texture displacementTexture;

	/**
	 * Public constructor
	 * @param name
	 */
	public Material(String id) {
		this.id = id;
		ambientColor = new Color4(0.2f, 0.2f, 0.2f, 0.0f);
		diffuseColor = new Color4(0.8f, 0.8f, 0.8f, 1.0f);
		specularColor = new Color4(0.0f, 0.0f, 0.0f, 0.0f);
		emissionColor = new Color4(0.0f, 0.0f, 0.0f, 0.0f);
		shininess = 0.0f;
		diffuseTexture = null;
		diffuseTextureTransparency = false;
		normalTexture = null;
		displacementTexture = null;
	}

	/**
	 * @return material id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return ambient color
	 */
	public Color4 getAmbientColor() {
		return ambientColor;
	}

	/**
	 * @return diffuse color
	 */
	public Color4 getDiffuseColor() {
		return diffuseColor;
	}

	/**
	 * @return specular color
	 */
	public Color4 getSpecularColor() {
		return specularColor;
	}

	/**
	 * @return emission color
	 */
	public Color4 getEmissionColor() {
		return emissionColor;
	}

	/**
	 * @return shininess
	 */
	public float getShininess() {
		return shininess;
	}

	/**
	 * Set up material shininess
	 * @param shininess
	 */
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

	/**
	 * Set up a diffuse texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setDiffuseTexture(String pathName, String fileName) {
		diffuseTexture = net.drewke.tdme.engine.fileio.textures.TextureLoader.loadTexture(pathName, fileName);
		checkDiffuseTextureTransparency();
	}

	/**
	 * @return if material has a diffuse texture
	 */
	public boolean hasDiffuseTexture() {
		return diffuseTexture != null;
	}

	/**
	 * @return the material's diffuse texture
	 */
	public Texture getDiffuseTexture() {
		return diffuseTexture;
	}

	/**
	 * Checks and set ups diffuse texture transparency
	 */
	private void checkDiffuseTextureTransparency() {
		diffuseTextureTransparency = false;
		if (diffuseTexture != null && diffuseTexture.getDepth() == 32) {
			ByteBuffer textureData = diffuseTexture.getTextureData();
			for (int i = 0; i < diffuseTexture.getTextureWidth() * diffuseTexture.getTextureHeight(); i++) {
				byte alpha = textureData.get(i * 4 + 3);
				if (alpha != (byte)0xFF) {
					diffuseTextureTransparency = true;
					break;
				}
			}
		}
	}

	/**
	 * Set up a normal texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setNormalTexture(String pathName, String fileName) {
		normalTexture = net.drewke.tdme.engine.fileio.textures.TextureLoader.loadTexture(pathName, fileName);
	}

	/**
	 * @return if material has a normal texture
	 */
	public boolean hasNormalTexture() {
		return normalTexture != null;
	}

	/**
	 * @return the material's normal texture
	 */
	public Texture getNormalTexture() {
		return normalTexture;
	}

	/**
	 * Set up a displacement texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setDisplacementTexture(String pathName, String fileName) {
		displacementTexture = net.drewke.tdme.engine.fileio.textures.TextureLoader.loadTexture(pathName, fileName);
	}

	/**
	 * @return if material has a displacement texture
	 */
	public boolean hasDisplacementTexture() {
		return displacementTexture != null;
	}

	/**
	 * @return the material's displacement texture
	 */
	public Texture getDisplacementTexture() {
		return displacementTexture;
	}

	/**
	 * @return if material is transparent
	 */
	public boolean hasTransparency() {
		return
			diffuseColor.getAlpha() < 1.0f - MathTools.EPSILON ||
			diffuseTextureTransparency;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Material [id=" + id + ", ambientColor=" + ambientColor
				+ ", diffuseColor=" + diffuseColor + ", specularColor="
				+ specularColor + ", emissionColor=" + emissionColor
				+ ", shininess=" + shininess + ", diffuseTexture="
				+ diffuseTexture + ", diffuseTextureTransparency="
				+ diffuseTextureTransparency + ", normalTexture="
				+ normalTexture + ", displacementTexture="
				+ displacementTexture + "]";
	}

}
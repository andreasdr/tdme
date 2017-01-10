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
	private String diffuseTexturePathName;
	private String diffuseTextureFileName;
	private Texture diffuseTexture;
	private boolean diffuseTextureTransparency;
	private String specularTexturePathName;
	private String specularTextureFileName;
	private Texture specularTexture;
	private String normalTexturePathName;
	private String normalTextureFileName;
	private Texture normalTexture;
	private String displacementTexturePathName;
	private String displacementTextureFileName;
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
		diffuseTexturePathName = null;
		diffuseTextureFileName = null;
		diffuseTexture = null;
		diffuseTextureTransparency = false;
		specularTexturePathName = null;
		specularTextureFileName = null;
		specularTexture = null;
		normalTexturePathName = null;
		normalTextureFileName = null;
		normalTexture = null;
		displacementTexturePathName = null;
		displacementTextureFileName = null;
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
	 * @return diffuse texture path name
	 */
	public String getDiffuseTexturePathName() {
		return diffuseTexturePathName;
	}

	/**
	 * @return diffuse texture file name
	 */
	public String getDiffuseTextureFileName() {
		return diffuseTextureFileName;
	}

	/**
	 * Set up a diffuse texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setDiffuseTexture(String pathName, String fileName) {
		diffuseTexturePathName = pathName;
		diffuseTextureFileName = fileName;
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
	 * @return texture has transparent pixels
	 */
	public boolean hasDiffuseTextureTransparency() {
		return diffuseTextureTransparency;
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
	 * @return specular texture path name
	 */
	public String getSpecularTexturePathName() {
		return specularTexturePathName;
	}

	/**
	 * @return specular texture file name
	 */
	public String getSpecularTextureFileName() {
		return specularTextureFileName;
	}

	/**
	 * Set up a specular texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setSpecularTexture(String pathName, String fileName) {
		specularTexturePathName = pathName;
		specularTextureFileName = fileName;
		specularTexture = net.drewke.tdme.engine.fileio.textures.TextureLoader.loadTexture(pathName, fileName);
	}

	/**
	 * @return if material has a specular texture
	 */
	public boolean hasSpecularTexture() {
		return specularTexture != null;
	}

	/**
	 * @return the material's specular texture
	 */
	public Texture getSpecularTexture() {
		return specularTexture;
	}

	/**
	 * @return normal texture path name
	 */
	public String getNormalTexturePathName() {
		return normalTexturePathName;
	}

	/**
	 * @return normal texture file name
	 */
	public String getNormalTextureFileName() {
		return normalTextureFileName;
	}

	/**
	 * Set up a normal texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setNormalTexture(String pathName, String fileName) {
		normalTexturePathName = pathName;
		normalTextureFileName = fileName;
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
	 * @return displacement texture path name
	 */
	public String getDisplacementTexturePathName() {
		return displacementTexturePathName;
	}

	/**
	 * @return displacement texture file name
	 */
	public String getDisplacementTextureFileName() {
		return displacementTextureFileName;
	}

	/**
	 * Set up a displacement texture
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public void setDisplacementTexture(String pathName, String fileName) {
		displacementTexturePathName = pathName;
		displacementTextureFileName = fileName;
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
				+ diffuseTextureTransparency + ", specularTexture="
				+ specularTexture + ", normalTexture=" + normalTexture
				+ ", displacementTexture=" + displacementTexture + "]";
	}

}
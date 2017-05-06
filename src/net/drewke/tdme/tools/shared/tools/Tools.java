package net.drewke.tdme.tools.shared.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import net.drewke.tdme.engine.Camera;
import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Entity;
import net.drewke.tdme.engine.Light;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.PartitionNone;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Model.UpVector;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;
import net.drewke.tdme.tools.leveleditor.logic.Level;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity.EntityType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * Thumbnail generator
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Tools {

	private static Engine osEngine = null;
	private static Transformations oseLookFromRotations = null;
	private static float oseScale;

	/**
	 * Formats a float to a human readable format
	 * @param value
	 * @return value as string
	 */
	public static String formatFloat(float value) {
		return String.format(Locale.ENGLISH, "%.3f", value);
	}

	/**
	 * Formats a vector3 to a human readable format
	 * @param value
	 * @return value as string
	 */
	public static String formatVector3(Vector3 value) {
		return
			formatFloat(value.getX()) + ", " +
			formatFloat(value.getY()) + ", " +
			formatFloat(value.getZ());
	}

	/**
	 * Formats a color4 to a human readable format
	 * @param value
	 * @return value as string
	 */
	public static String formatColor4(Color4 value) {
		return
			formatFloat(value.getRed()) + ", " +
			formatFloat(value.getGreen()) + ", " +
			formatFloat(value.getBlue()) + ", " +
			formatFloat(value.getAlpha());
	}

	/**
	 * Convert string to array
	 * @param text
	 * @param length
	 * @param array
	 */
	public static void convertToArray(String text, int length, float[] array) throws NumberFormatException {
		int i = 0;
		StringTokenizer t = new StringTokenizer(text, ",");
		while (t.hasMoreTokens() && i < length) {
			array[i++] = Float.parseFloat(t.nextToken());
		}
	}

	/**
	 * Convert to vector 3
	 * @param text
	 * @return vector3
	 */
	public static Vector3 convertToVector3(String text) throws NumberFormatException {
		Vector3 v = new Vector3();
		convertToArray(text, 3, v.getArray());
		return v;
	}

	/**
	 * Convert to vector 4
	 * @param text
	 * @return vector4
	 */
	public static Vector4 convertToVector4(String text) throws NumberFormatException {
		Vector4 v = new Vector4();
		convertToArray(text, 4, v.getArray());
		return v;
	}

	/**
	 * Convert to color 4
	 * @param text
	 * @return color4
	 */
	public static Color4 convertToColor4(String text) throws NumberFormatException {
		Color4 color = new Color4();
		convertToArray(text, 4, color.getArray());
		return color;
	}

	/**
	 * Convert string to float
	 * @param text
	 * @return float
	 */
	public static float convertToFloat(String text) throws NumberFormatException {
		return Float.parseFloat(text);
	}

	/**
	 * Convert string to int
	 * @param text
	 * @return int
	 */
	public static int convertToInt(String text) throws NumberFormatException {
		return Integer.parseInt(text);
	}

	/**
	 * Convert string to int
	 * @param text
	 * @return int
	 */
	public static int convertToIntSilent(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	/**
	 * Set up given engine light with default light
	 * @param light
	 */
	public static void setDefaultLight(Light light) {
		light.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
		light.getDiffuse().set(0.5f,0.5f,0.5f,1f);
		light.getSpecular().set(1f,1f,1f,1f);
		light.getPosition().set(
			0f,
			20000f,
			0f,
			1f
		);
		light.getSpotDirection().set(0f,0f,0f).sub(new Vector3(light.getPosition().getArray()));
		light.setConstantAttenuation(0.5f);
		light.setLinearAttenuation(0f);
		light.setQuadraticAttenuation(0f);
		light.setSpotExponent(0f);
		light.setSpotCutOff(180f);
		light.setEnabled(true);

	}

	/**
	 * Init off screen engine for making thumbails
	 * @param drawable
	 */
	public static void oseInit(GLAutoDrawable drawable) {
		osEngine = Engine.createOffScreenInstance(drawable, 128, 128);
		osEngine.setPartition(new PartitionNone());
		setDefaultLight(osEngine.getLightAt(0));
		oseScale = 0.75f;
		oseLookFromRotations = new Transformations();
		oseLookFromRotations.getRotations().add(new Rotation(-45f, new Vector3(0f, 1f, 0f)));
		oseLookFromRotations.getRotations().add(new Rotation(-45f, new Vector3(1f, 0f, 0f)));
		oseLookFromRotations.getRotations().add(new Rotation(0f, new Vector3(0f, 0f, 1f)));
		oseLookFromRotations.update();
	}

	/**
	 * Dispose off screen engine
	 * @param drawable
	 */
	public static void oseDispose(GLAutoDrawable drawable) {
		osEngine.dispose(drawable);
	}

	/**
	 * Make a thumbnail of given model with off screen engine
	 * @param drawable
	 * @param model
	 */
	public static void oseThumbnail(GLAutoDrawable drawable, LevelEditorEntity model) {
		Tools.setupEntity(model, osEngine, oseLookFromRotations, oseScale);

		// make thumbnail
		osEngine.getSceneColor().set(0.5f, 0.5f, 0.5f, 1.0f);
		osEngine.display(drawable);
		osEngine.makeScreenshot(
			"tmp",
			model.getThumbnail()
		);

		// make thumbnail if selected
		osEngine.getSceneColor().set(0.8f, 0.0f, 0.0f, 1.0f);
		osEngine.display(drawable);
		osEngine.makeScreenshot(
			"tmp",
			"selected_" + model.getThumbnail()
		);

		// reset off screen engine
		osEngine.reset();
	}

	/**
	 * Compute max axis dimension b given bounding box
	 * @param model bounding box
	 * @return max axis dimension
	 */
	public static float computeMaxAxisDimension(BoundingVolume modelBoundingVolume) {
		float maxAxisDimension = 0.0f;

		// model dimension
		Vector3 dimension = new Vector3(
			modelBoundingVolume.computeDimensionOnAxis(new Vector3(1f,0f,0f)),
			modelBoundingVolume.computeDimensionOnAxis(new Vector3(0f,1f,0f)),
			modelBoundingVolume.computeDimensionOnAxis(new Vector3(0f,0f,1f))
		);

		// determine max axis dimension
		if (dimension.getX() > maxAxisDimension) maxAxisDimension = dimension.getX(); 
		if (dimension.getY() > maxAxisDimension) maxAxisDimension = dimension.getY();
		if (dimension.getZ() > maxAxisDimension) maxAxisDimension = dimension.getZ();

		return maxAxisDimension;
	}

	/**
	 * Creates a ground plate
	 * @param width
	 * @param depth
	 * @param float y
	 * @return ground model
	 */
	public static Model createGroundModel(float width, float depth, float y) {
		// ground model
		Model ground = new Model("ground", "ground", UpVector.Y_UP, RotationOrder.XYZ, null);

		//	material
		Material groundMaterial = new Material("ground");
		groundMaterial.getSpecularColor().set(0f,0f,0f,1f);
		ground.getMaterials().put("ground", groundMaterial);

		//	group
		Group groundGroup = new Group(ground, null, "ground", "ground");

		//	faces entity
		//		ground
		FacesEntity groupFacesEntityGround = new FacesEntity(groundGroup, "ground group faces entity ground");
		groupFacesEntityGround.setMaterial(groundMaterial);

		//	faces entity 
		ArrayList<FacesEntity> groupFacesEntities = new ArrayList<FacesEntity>();
		groupFacesEntities.add(groupFacesEntityGround);

		//	vertices
		ArrayList<Vector3> groundVertices = new ArrayList<Vector3>();
		// left, near, ground
		groundVertices.add(new Vector3(-width, y, -depth));
		// left, far, ground
		groundVertices.add(new Vector3(-width, y, +depth));
		// right far, ground
		groundVertices.add(new Vector3(+width, y, +depth));
		// right, near, ground
		groundVertices.add(new Vector3(+width, y, -depth));

		//	normals
		ArrayList<Vector3> groundNormals = new ArrayList<Vector3>();
		//		ground
		groundNormals.add(new Vector3(0f, 1f, 0f));

		// texture coordinates
		ArrayList<TextureCoordinate> groundTextureCoordinates = new ArrayList<TextureCoordinate>();
		groundTextureCoordinates.add(new TextureCoordinate(0f, 0f));
		groundTextureCoordinates.add(new TextureCoordinate(0f, 1f));
		groundTextureCoordinates.add(new TextureCoordinate(1f, 1f));
		groundTextureCoordinates.add(new TextureCoordinate(1f, 0f));

		//	faces ground
		ArrayList<Face> groundFacesGround = new ArrayList<Face>();
		groundFacesGround.add(new Face(groundGroup,0,1,2,0,0,0,0,1,2));
		groundFacesGround.add(new Face(groundGroup,2,3,0,0,0,0,2,3,0));

		// set up faces entity
		groupFacesEntityGround.setFaces(groundFacesGround);

		// setup ground group
		groundGroup.setVertices(groundVertices);
		groundGroup.setNormals(groundNormals);
		groundGroup.setTextureCoordinates(groundTextureCoordinates);
		groundGroup.setFacesEntities(groupFacesEntities);

		// register group
		ground.getGroups().put("ground", groundGroup);
		ground.getSubGroups().put("ground", groundGroup);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(ground);

		//
		return ground;
	}

	/**
	 * Set up entity in given engine with look from rotations and scale
	 * @param entity
	 * @param engine
	 * @param look from rotations
	 * @param scale
	 */
	public static void setupEntity(LevelEditorEntity entity, Engine engine, Transformations lookFromRotations, float scale) {
		if (entity == null) return;

		// entity bounding box
		BoundingBox entityBoundingBox = null;

		// add model to engine
		if (entity.getType() == EntityType.PARTICLESYSTEM) {
			entityBoundingBox = new BoundingBox(new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(0.5f, 0.5f, 0.5f));
			Entity particleSystemObject = Level.createParticleSystem(entity.getParticleSystem(), "model", true);
			if (particleSystemObject != null) {
				engine.addEntity(particleSystemObject);
			}
		} else {
			entityBoundingBox = entity.getModel().getBoundingBox();
			Object3D modelObject = new Object3D("model", entity.getModel());
			modelObject.setDynamicShadowingEnabled(true);
			engine.addEntity(modelObject);
		}

		// create ground object
		Model ground = createGroundModel(
			(entityBoundingBox.getMax().getX() - entityBoundingBox.getMin().getX()) * 1f,
			(entityBoundingBox.getMax().getZ() - entityBoundingBox.getMin().getZ()) * 1f,
			entityBoundingBox.getMin().getY() - MathTools.EPSILON
		);

		// add ground to engine
		Object3D groundObject = new Object3D("ground", ground);
		groundObject.setEnabled(false);
		engine.addEntity(groundObject);

		// add bounding volume if we have any
		for (int i = 0; i < entity.getBoundingVolumeCount(); i++) {
			LevelEditorEntityBoundingVolume boundingVolume = entity.getBoundingVolumeAt(i);
			if (boundingVolume.getModel() == null) continue;
			Object3D modelBoundingVolumeObject = new Object3D("model_bv." + i, boundingVolume.getModel());
			modelBoundingVolumeObject.setEnabled(false);
			engine.addEntity(modelBoundingVolumeObject);
		}

		// set up lights
		for (Light light: engine.getLights()) light.setEnabled(false);
		Light light0 = engine.getLightAt(0);
		light0.getAmbient().set(1.0f, 1.0f, 1.0f, 1.0f);
		light0.getDiffuse().set(0.5f,0.5f,0.5f,1f);
		light0.getSpecular().set(1f,1f,1f,1f);
		light0.getPosition().set(
			entityBoundingBox.getMin().getX() + ((entityBoundingBox.getMax().getX() - entityBoundingBox.getMin().getX()) / 2f),
			//modelBoundingBox.getMax().getY(),
			entityBoundingBox.getMin().getY() + ((entityBoundingBox.getMax().getY() - entityBoundingBox.getMin().getY()) / 2f),
			-entityBoundingBox.getMin().getZ() * 4f,
			1f
		);
		light0.getSpotDirection().set(0f,0f,0f).sub(new Vector3(light0.getPosition().getArray()));
		light0.setConstantAttenuation(0.5f);
		light0.setLinearAttenuation(0f);
		light0.setQuadraticAttenuation(0f);
		light0.setSpotExponent(0f);
		light0.setSpotCutOff(180f);
		light0.setEnabled(true);

		// model dimension
		Vector3 dimension = 
			entityBoundingBox.getMax().clone().
			sub(entityBoundingBox.getMin());

		// determine max dimension on each axis
		float maxAxisDimension = computeMaxAxisDimension(entityBoundingBox); 

		// set up cam
		Camera cam = engine.getCamera();
		cam.setZNear(maxAxisDimension / 5000f);
		cam.setZFar(maxAxisDimension);

		// look at
		Vector3 lookAt =
			entityBoundingBox.getMin().clone().add(
				dimension.clone().scale(0.5f)
			);
		cam.getLookAt().set(lookAt);

		// look at -> look to vector
		Vector3 lookAtToFromVector =
			new Vector3(
				0f,
				0f,
				+(maxAxisDimension * 1.2f)
			);

		// apply look from rotations
		Vector3 lookAtToFromVectorTransformed = new Vector3();
		Vector3 lookAtToFromVectorScaled = new Vector3();
		Vector3 upVector = new Vector3();
		lookFromRotations.getTransformationsMatrix().multiply(lookAtToFromVector, lookAtToFromVectorTransformed);
		lookAtToFromVectorScaled.set(lookAtToFromVectorTransformed).scale(scale);
		lookFromRotations.getRotations().get(2).getQuaternion().multiply(new Vector3(0f,1f,0f), upVector);

		// look from with rotations
		Vector3 lookFrom = lookAt.clone().add(lookAtToFromVectorScaled);
		cam.getLookFrom().set(lookFrom);

		// up vector
		cam.getUpVector().set(upVector);
	}

	/**
	 * Get relative resources file name
	 * @param game root
	 * @param file name
	 * @return relative resources file name
	 */
	public static String getRelativeResourcesFileName(String gameRoot, String fileName) {
		fileName = fileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		// try to cut by game root
		int filesRootIdx = gameRoot.length() > 0?fileName.lastIndexOf(gameRoot):-1;
		if (filesRootIdx != -1) {
			// yep, works
			filesRootIdx+= gameRoot.length();
		}
		// did not work, then try to do by /resources/
		if (filesRootIdx == -1) {
			filesRootIdx = fileName.lastIndexOf("/resources/");
			// do we have resources?
			if (filesRootIdx != -1) {
				// yep
				filesRootIdx+= "/resources/".length();
			}
		}
		if (filesRootIdx != -1) {
			fileName = fileName.substring(filesRootIdx + 1);
		}
		return fileName;
	}

	/**
	 * Get game root path
	 * @param file name
	 * @return game root path
	 */
	public static String getGameRootPath(String fileName) {
		fileName = fileName.replace(File.separatorChar == '/'?'\\':'/', File.separatorChar);
		int filesRootIdx = fileName.lastIndexOf("/resources/");
		if (filesRootIdx != -1) fileName = fileName.substring(0, filesRootIdx);
		return fileName;
	}

	/**
	 * Get path
	 * @param file name
	 * @return path
	 */
	public static String getPath(String fileName) {
		String path = new File(fileName).getParent();
		return path == null?"":path;
	}

	/**
	 * Get file name of given path
	 * @param file name
	 * @return file name
	 */
	public static String getFileName(String fileName) {
		return new File(fileName).getName();
	}

}
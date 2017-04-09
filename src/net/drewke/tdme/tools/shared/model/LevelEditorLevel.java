package net.drewke.tdme.tools.shared.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.math.Vector3;

/**
 * Level
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelEditorLevel extends Properties {

	private String gameRoot;
	private String pathName;
	private String fileName;
	private RotationOrder rotationOrder;
	private ArrayList<LevelEditorLight> lights;
	private LevelEditorEntityLibrary entityLibrary;
	private HashMap<String, LevelEditorObject> objectsById;
	private ArrayList<LevelEditorObject> objects;
	private int objectIdx;
	private BoundingBox boundingBox;
	private Vector3 dimension;

	/**
	 * Public constructor
	 * @param default map properties or null
	 */
	public LevelEditorLevel() {
		gameRoot = "";
		pathName = ".";
		fileName = "untitled.tl";
		rotationOrder = RotationOrder.XYZ;
		lights = new ArrayList<LevelEditorLight>();
		lights.add(new LevelEditorLight(0));
		lights.add(new LevelEditorLight(1));
		lights.add(new LevelEditorLight(2));
		lights.add(new LevelEditorLight(3));

		// set up default light
		LevelEditorLight light = lights.get(0);
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
		light.getSpotTo().set(light.getPosition().getArray()).add(light.getSpotDirection());
		light.setConstantAttenuation(0.5f);
		light.setLinearAttenuation(0f);
		light.setQuadraticAttenuation(0f);
		light.setSpotExponent(0f);
		light.setSpotCutOff(180f);
		light.setEnabled(true);

		//
		entityLibrary = new LevelEditorEntityLibrary(this);
		objectsById = new HashMap<String, LevelEditorObject>();
		objects = new ArrayList<LevelEditorObject>();
		objectIdx = 0;
		dimension = new Vector3();
		boundingBox = new BoundingBox();
	}

	/**
	 * @return game root
	 */
	public String getGameRoot() {
		return gameRoot;
	}

	/**
	 * Set game root
	 * @param gameRoot
	 */
	public void setGameRoot(String gameRoot) {
		this.gameRoot = gameRoot;
	}

	/**
	 * @return path name
	 */
	public String getPathName() {
		return pathName;
	}

	/**
	 * Set up path name
	 * @param pathName
	 */
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	/**
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Set up level file name
	 * @param file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return rotation order
	 */
	public RotationOrder getRotationOrder() {
		return rotationOrder;
	}

	/**
	 * Set rotation order
	 * @param rotation order
	 */
	public void setRotationOrder(RotationOrder rotationOrder) {
		this.rotationOrder = rotationOrder;
	}

	/**
	 * @return number of lights
	 */
	public int getLightCount() {
		return lights.size();
	}

	/**
	 * Get light at index i
	 * @param i
	 * @return
	 */
	public LevelEditorLight getLightAt(int i) {
		return lights.get(i);
	}

	/**
	 * @return lights
	 */
	public ArrayList<LevelEditorLight> getLights() {
		return lights;
	}

	/**
	 * @return entity library
	 */
	public LevelEditorEntityLibrary getEntityLibrary() {
		return entityLibrary;
	}

	/**
	 * @return dimension
	 */
	public Vector3 getDimension() {
		return dimension;
	}

	/**
	 * Compute level dimension
	 */
	public void computeDimension() {
		computeBoundingBox();
	}

	/**
	 * @return level bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Computes level bounding box
	 */
	public void computeBoundingBox() {
		boolean haveDimension = false;
		float left = 0.0f;
		float right = 0.0f;
		float near = 0.0f;
		float far = 0.0f;
		float top = 0.0f;
		float bottom = 0.0f;
		Vector3 bbDimension = new Vector3();
		Vector3 bbMin = new Vector3();
		Vector3 bbMax = new Vector3();
		for (LevelEditorObject levelEditorObject: objects) {
			BoundingBox bv = levelEditorObject.getEntity().getModel().getBoundingBox();
			BoundingVolume cbv = bv.clone();
			cbv.fromBoundingVolumeWithTransformations(bv, levelEditorObject.getTransformations());
			bbDimension.set(
				cbv.computeDimensionOnAxis(new Vector3(1f,0f,0f)),
				cbv.computeDimensionOnAxis(new Vector3(0f,1f,0f)),
				cbv.computeDimensionOnAxis(new Vector3(0f,0f,1f))
			);
			bbDimension.scale(0.5f);
			bbMin.set(cbv.getCenter());
			bbMin.sub(bbDimension);
			bbMax.set(cbv.getCenter());
			bbMax.add(bbDimension);
			float objectLeft = bbMin.getX();
			float objectRight = bbMax.getX();
			float objectNear = bbMin.getZ();
			float objectFar = bbMax.getZ();
			float objectBottom = bbMin.getY();
			float objectTop = bbMax.getY();
			if (haveDimension == false) {
				left = objectLeft;
				right = objectRight;
				near = objectNear;
				far = objectFar;
				top = objectTop;
				bottom = objectBottom;
				haveDimension = true;
			} else {
				if (objectLeft < left) left = objectLeft;
				if (objectRight > right) right = objectRight;
				if (objectNear < near) near = objectNear;
				if (objectFar > far) far = objectFar;
				if (objectTop > top) top = objectTop;
				if (objectBottom < bottom) bottom = objectBottom;
			}
		}
		boundingBox.getMin().set(left, bottom, near);
		boundingBox.getMax().set(right, top, far);
		boundingBox.update();
		dimension.setX(right - left);
		dimension.setZ(far - near);
		dimension.setY(top - bottom);
	}

	/**
	 * @return level center
	 */
	public Vector3 computeCenter() {
		Vector3 center = new Vector3();
		int objectCount = 0;
		for (LevelEditorObject levelEditorObject: objects) {
			center.add(levelEditorObject.getTransformations().getTranslation());
			objectCount++;
		}
		if (objectCount != 0) center.scale(1f / objectCount);
		return center;
	}

	/**
	 * @return new object idx
	 */
	public int allocateObjectId() {
		return objectIdx++;
	}

	/**
	 * @return object idx
	 */
	public int getObjectIdx() {
		return objectIdx;
	}

	/**
	 * Set entity idx
	 * @param objectIdx
	 */
	public void setObjectIdx(int entityIdx) {
		this.objectIdx = entityIdx;
	}

	/**
	 * Clears all level objects
	 */
	public void clearObjects() {
		objectsById.clear();
		objects.clear();
		objectIdx = 0;			
	}

	/**
	 * Remove objects with given entity id
	 * @param entity id
	 */
	public void removeObjectsByEntityId(int entityId) {
		ArrayList<String> objectsToRemove = new ArrayList<String>();
		for (LevelEditorObject object: objects) {
			if (object.getEntity().getId() == entityId) {
				objectsToRemove.add(object.getId());
			}
		}
		for (String objectId: objectsToRemove) {
			removeObject(objectId);
		}
	}

	/**
	 * Replace entity
	 * @param search model id
	 * @param replace model id 
	 */
	public void replaceEntity(int searchEntityId, int replaceEntityId) {
		LevelEditorEntity replaceEntity = getEntityLibrary().getEntity(replaceEntityId);
		if (replaceEntity == null) return;

		for (LevelEditorObject object: objects) {
			if (object.getEntity().getId() == searchEntityId) {
				object.setEntity(replaceEntity);
			}
		}
	}

	/**
	 * Updates pivot
	 * @param model id
	 */
	public void updatePivot(int modelId, Vector3 pivot) {
		for (LevelEditorObject object: objects) {
			if (object.getEntity().getId() == modelId) {
				object.getTransformations().getPivot().set(pivot);
				object.getTransformations().update();
			}
		}
	}

	/**
	 * Adds an object to level
	 * @param object
	 */
	public void addObject(LevelEditorObject object) {
		LevelEditorObject _entity = objectsById.put(object.getId(), object);
		if (_entity != null) {
			objects.remove(_entity);
			System.out.println("LevelEditorLevel::addObject():: object with id '" + object.getId() + "' already exists");
		}
		objects.add(object);
	}

	/**
	 * Removes an object from level 
	 * @param id
	 */
	public void removeObject(String id) {
		LevelEditorObject _entity = objectsById.remove(id);
		objects.remove(_entity);
	}

	/**
	 * Returns level editor object by id
	 * @param id
	 * @return level editor object or null
	 */
	public LevelEditorObject getObjectById(String id) {
		return objectsById.get(id);
	}

	/**
	 * @return number of objects
	 */
	public int getObjectCount() {
		return objects.size();
	}

	/**
	 * Returns object at idx  
	 * @param idx
	 * @return level object
	 */
	public LevelEditorObject getObjectAt(int idx) {
		return objects.get(idx);
	}

	/**
	 * @return object keys enumerator
	 */
	public Iterator<String> getObjectIdsIterator() {
		return objectsById.keySet().iterator();
	}

}

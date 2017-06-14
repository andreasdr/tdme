package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.Object3D;
import net.drewke.tdme.engine.PointsParticleSystemEntity;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.subsystems.lighting.LightingShader;
import net.drewke.tdme.engine.subsystems.particlesystem.PointsParticleSystemEntityInternal;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.engine.subsystems.shadowmapping.ShadowMapping;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Matrix4x4Negative;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.Key;
import net.drewke.tdme.utils.Pool;
import net.drewke.tdme.utils.QuickSort;

/**
 * VBO renderer class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Object3DVBORenderer {

	public enum DepthBufferMode{IGNORE, FORCE, NORMAL};

	private static final int BATCHVBORENDERER_MAX = 256;

	protected Engine engine;
	protected GLRenderer renderer;

	protected ArrayList<BatchVBORendererTriangles> trianglesBatchVBORenderers;

	private HashMap<String, ArrayList<Object3D>> visibleObjectsByModels = null;
	private Pool<Key> keyPool = null;
	private ArrayList<TransparentRenderFace> groupTransparentRenderFaces = null;
	private Pool<TransparentRenderFacesGroup> transparentRenderFacesGroupPool;
	private TransparentRenderFacesPool transparentRenderFacesPool = null;
	private HashMap<Key, TransparentRenderFacesGroup> transparentRenderFacesGroups = null;

	private Pool<Key> pseKeyPool = new Pool<Key>() {
		public Key instantiate() {
			return new Key();
		}
	};
	private ArrayList<Key> pseKeys = new ArrayList<Key>();
	private TransparentRenderPointsPool pseTransparentRenderPointsPool = null;
	private BatchVBORendererPoints psePointBatchVBORenderer = null;

	private Matrix4x4 modelViewMatrixBackup = new Matrix4x4();
	private Matrix4x4 modelViewMatrix = new Matrix4x4();
	private Vector3 transformedVertex = new Vector3();
	private Vector3 transformedNormal = new Vector3();

	private Matrix4x4Negative matrix4x4Negative = new Matrix4x4Negative();

	/**
	 * Public constructor 
	 * @param renderer
	 */
	public Object3DVBORenderer(Engine engine, GLRenderer renderer) {
		this.engine = engine;
		this.renderer = renderer;
		trianglesBatchVBORenderers = new ArrayList<BatchVBORendererTriangles>();
		visibleObjectsByModels = new HashMap<String, ArrayList<Object3D>>();
		keyPool = new Pool<Key>() {
			public Key instantiate() {
				return new Key();
			}
		};
		groupTransparentRenderFaces = new ArrayList<TransparentRenderFace>();
		transparentRenderFacesGroupPool = new Pool<TransparentRenderFacesGroup>() {
			public TransparentRenderFacesGroup instantiate() {
				return new TransparentRenderFacesGroup();
			}
			
		};
		transparentRenderFacesPool = new TransparentRenderFacesPool();
		transparentRenderFacesGroups = new HashMap<Key, TransparentRenderFacesGroup>();
		pseTransparentRenderPointsPool = new TransparentRenderPointsPool(16384);
		psePointBatchVBORenderer = new BatchVBORendererPoints(renderer, 0);
	}

	/**
	 * Init
	 */
	public void initialize() {
		psePointBatchVBORenderer.initialize();
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		// dispose batch vbo renderer
		for (BatchVBORendererTriangles batchVBORenderer: trianglesBatchVBORenderers) {
			batchVBORenderer.dispose();
			batchVBORenderer.release();
		}
		psePointBatchVBORenderer.dispose();
	}

	/**
	 * @return batch vbo renderer for triangles
	 */
	public BatchVBORendererTriangles acquireTrianglesBatchVBORenderer() {
		// check for free batch vbo renderer 
		int i = 0;
		for (BatchVBORendererTriangles batchVBORenderer: trianglesBatchVBORenderers) {
			if (batchVBORenderer.acquire()) return batchVBORenderer;
			i++;
		}

		// try to add one
		if (i < BATCHVBORENDERER_MAX) {
			BatchVBORendererTriangles batchVBORenderer = new BatchVBORendererTriangles(renderer, i);
			batchVBORenderer.initialize();
			trianglesBatchVBORenderers.add(batchVBORenderer);
			if (batchVBORenderer.acquire()) return batchVBORenderer;
		}

		// nope
		Console.println("Object3DVBORenderer::acquireTrianglesBatchVBORenderer()::failed");
		return null;
	}

	/**
	 * Resets the object 3d vbo renderer
	 */
	public void reset() {
		visibleObjectsByModels.clear();
	}

	/**
	 * Renders all given objects
	 * @param objects
	 * @param render transparent faces
	 * @param depth buffer mode  
	 */
	public void render(ArrayList<Object3D> objects, boolean renderTransparentFaces, DepthBufferMode depthBufferMode) {
		// clear transparent render faces data
		transparentRenderFacesPool.reset();
		releaseTransparentFacesGroups();

		// sort objects by model
		for (int objectIdx = 0; objectIdx < objects.size(); objectIdx++) {
			Object3D object = objects.get(objectIdx);
			String modelId = object.getModel().getId();
			ArrayList<Object3D> visibleObjectsByModel = visibleObjectsByModels.get(modelId);
			if (visibleObjectsByModel == null) {
				visibleObjectsByModel = new ArrayList<Object3D>();
				visibleObjectsByModels.put(modelId, visibleObjectsByModel);
			}
			visibleObjectsByModel.add(object);
		}

		// render objects
		for (ArrayList<Object3D> objectsByModel: visibleObjectsByModels.getValuesIterator()) {
			if (objectsByModel.size() > 0) {
				renderObjectsOfSameType(objectsByModel, renderTransparentFaces);
				objectsByModel.clear();
			}
		}

		// render transparent render faces if any exist
		ArrayList<TransparentRenderFace> transparentRenderFaces = transparentRenderFacesPool.getTransparentRenderFaces();
		if (transparentRenderFaces.size() > 0) {
			// sort transparent render faces from far to near
			QuickSort.sort(transparentRenderFaces);

			// second render pass, draw color buffer for transparent objects
			// 	set up blending, but no culling and no depth buffer
			renderer.disableDepthBuffer();
			renderer.disableCulling();
			renderer.enableBlending();
			// actually this should not make any difference as culling is disabled
			// but having a fixed value is not a bad idea except that it is a GL call
			// TODO: confirm this
			renderer.setFrontFace(renderer.FRONTFACE_CCW);

			//
			for (TransparentRenderFace transparentRenderFace: transparentRenderFacesPool.getTransparentRenderFaces()) {
				// do we have any faces yet?
				if (groupTransparentRenderFaces.size() == 0) {
					// nope, so add this one
					groupTransparentRenderFaces.add(transparentRenderFace);
				} else
				// do we have more than face already?
				if (groupTransparentRenderFaces.size() > 0) {
					// check if we have more of first type
					if (groupTransparentRenderFaces.get(0).object3DGroup == transparentRenderFace.object3DGroup) {
						// yep, we can add this one
						groupTransparentRenderFaces.add(transparentRenderFace);
					} else {
						// no, render grouped faces
						prepareTransparentFaces(groupTransparentRenderFaces);
						// reset
						groupTransparentRenderFaces.clear();
						// add current face
						groupTransparentRenderFaces.add(transparentRenderFace);
					}
				}
			}
	
			// 	check if there are any left overs
			if (groupTransparentRenderFaces.size() > 0) {
				prepareTransparentFaces(groupTransparentRenderFaces);
				groupTransparentRenderFaces.clear();
			}

			// render transparent faces groups
			renderTransparentFacesGroups(transparentRenderFacesGroups, depthBufferMode);

			//	no blending, but culling and depth buffer
			renderer.disableBlending();
			renderer.enableCulling();
			renderer.enableDepthBuffer();
			// done!
		}
	}

	/**
	 * Renders transparent faces
	 * 	TODO:	guess this should be optimized regarding GL commands
	 * 			skinned mesh is not supported when using GPU
	 * @param transparent render faces
	 */
	protected void prepareTransparentFaces(ArrayList<TransparentRenderFace> transparentRenderFaces) {
		// all those faces should share the object and object 3d group, ...
		Object3DGroup object3DGroup = transparentRenderFaces.get(0).object3DGroup;
		Object3D object3D = (Object3D)object3DGroup.object;

		// model view matrix to be used with given transparent render faces
		modelViewMatrix =
			(object3DGroup.mesh.skinning == true?
				modelViewMatrix.identity():
				modelViewMatrix.set(object3DGroup.groupTransformationsMatrix)
			).
			multiply(object3D.transformationsMatrix).
			multiply(renderer.getModelViewMatrix());

		//
		Model model = ((Object3D)object3DGroup.object).getModel();
		FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities();
		FacesEntity facesEntity = null;

		// attributes we collect for a transparent render face group
		boolean depthBuffer = false;
		Color4 effectColorAdd = ((Object3D)object3D).getEffectColorAdd();
		Color4 effectColorMul = ((Object3D)object3D).getEffectColorMul();
		Material material = null;
		boolean textureCoordinates = false;

		// render transparent faces
		Key transparentRenderFacesGroupKey = keyPool.allocate();
		for (int i = 0; i < transparentRenderFaces.size(); i++) {
			TransparentRenderFace transparentRenderFace = transparentRenderFaces.get(i);
			int facesEntityIdx = transparentRenderFace.facesEntityIdx;

			// check if to use depth buffer
			depthBuffer = ((Object3D)transparentRenderFace.object3DGroup.object).isPickable();

			// determine if faces entity and so material did switch between last face and current face
			if (facesEntity != facesEntities[facesEntityIdx]) {
				facesEntity = facesEntities[facesEntityIdx];
				material = facesEntity.getMaterial();
			}
			textureCoordinates = facesEntity.isTextureCoordinatesAvailable();

			// create group key
			TransparentRenderFacesGroup.createKey(
				transparentRenderFacesGroupKey,
				model,
				object3DGroup,
				facesEntityIdx,
				effectColorAdd,
				effectColorMul,
				depthBuffer,
				material,
				textureCoordinates
			);

			// get group
			TransparentRenderFacesGroup trfGroup = transparentRenderFacesGroups.get(transparentRenderFacesGroupKey);
			if (trfGroup == null) {
				// we do not have the group, create group
				trfGroup = transparentRenderFacesGroupPool.allocate();
				trfGroup.set(
					this,
					model,
					object3DGroup,
					facesEntityIdx,
					effectColorAdd,
					effectColorMul,
					depthBuffer,
					material,
					textureCoordinates
				);
				Key hashtableKey = keyPool.allocate();
				transparentRenderFacesGroupKey.cloneInto(hashtableKey);
				if (transparentRenderFacesGroups.put(
					hashtableKey,
					trfGroup
				) != null) {
					Console.println("Object3DVBORenderer::prepareTransparentFaces::key already exists");
					Console.println("-->" + transparentRenderFacesGroupKey);
					Console.println("-->" + hashtableKey);
				}
			}

			// add face vertices
			for (int vertexIdx = 0; vertexIdx < 3; vertexIdx++) {
				short arrayIdx = transparentRenderFace.object3DGroup.mesh.indices[transparentRenderFace.faceIdx * 3 + vertexIdx];
				trfGroup.addVertex(
					modelViewMatrix.multiply(transparentRenderFace.object3DGroup.mesh.transformedVertices[arrayIdx], transformedVertex),
					modelViewMatrix.multiplyNoTranslation(transparentRenderFace.object3DGroup.mesh.transformedNormals[arrayIdx], transformedNormal),
					transparentRenderFace.object3DGroup.mesh.textureCoordinates != null?transparentRenderFace.object3DGroup.mesh.textureCoordinates[arrayIdx]:null
				);
			}
		}
		keyPool.release(transparentRenderFacesGroupKey);
	}

	/**
	 * Render transparent faces groups
	 * @param transparent render faces groups
	 * @param depth buffer mode
	 */
	protected void renderTransparentFacesGroups(HashMap<Key, TransparentRenderFacesGroup> transparentRenderFacesGroups, DepthBufferMode depthBufferMode) {
		for (TransparentRenderFacesGroup transparentRenderFacesGroup: transparentRenderFacesGroups.getValuesIterator()) {
			transparentRenderFacesGroup.render(renderer, depthBufferMode);
		}
	}

	/**
	 * Render transparent faces groups
	 * @param transparent render faces groups
	 * @param depth buffer mode
	 */
	protected void releaseTransparentFacesGroups() {
		for (Key trfgKey: transparentRenderFacesGroups.getKeysIterator()) {
			keyPool.release(trfgKey);
		}
		for (TransparentRenderFacesGroup trfg: transparentRenderFacesGroups.getValuesIterator()) {
			transparentRenderFacesGroupPool.release(trfg);
		}
		transparentRenderFacesGroups.clear();
	}

	/**
	 * Renders multiple objects of same type(with same model)
	 * @param engine
	 * @param objects of same type/ with same models
	 * @param collect render faces
	 * @param skinning shader
	 */
	protected void renderObjectsOfSameType(ArrayList<Object3D> objects, boolean collectTransparentFaces) {
		// do pre render steps
		for (int i = 0; i < objects.size(); i++) {
			Object3D object = objects.get(i);
			for (int j = 0; j < object.object3dGroups.length; j++) {
				Object3DGroup object3DGroup = object.object3dGroups[j];
				((Object3DGroupVBORenderer)object3DGroup.renderer).preRender(this);
			}
		}

		//
		ShadowMapping shadowMapping = engine.getShadowMapping();
		modelViewMatrixBackup.set(renderer.getModelViewMatrix());

		// render faces entities
		int currentFrontFace = -1;
		Object3D firstObject = objects.get(0);

		// all objects share the same object 3d group structure, so we just take the first one
		int[] boundVBOBaseIds = null;
		int[] boundVBOTangentBitangentIds = null;
		int[] boundSkinningIds = null;
		for (int object3DGroupIdx = 0; object3DGroupIdx < firstObject.object3dGroups.length; object3DGroupIdx++) {
			Object3DGroup object3DGroup = firstObject.object3dGroups[object3DGroupIdx];

			// render each faces entity
			FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities();
			int faceIdx = 0;
			int facesEntityIdxCount = facesEntities.length;
			for (int faceEntityIdx = 0; faceEntityIdx < facesEntityIdxCount; faceEntityIdx++) {
				FacesEntity facesEntity = facesEntities[faceEntityIdx];
				boolean isTextureCoordinatesAvailable = facesEntity.isTextureCoordinatesAvailable();
				int faces = facesEntity.getFaces().length;

				// material
				Material material = facesEntity.getMaterial();

				// determine if transparent
				boolean transparentFacesEntity = false;
				//	via material
				if (material != null) { 
					if (material.hasTransparency() == true) transparentFacesEntity = true;
				}
	
				// skip, if requested
				if (transparentFacesEntity == true) {
					// add to transparent render faces, if requested
					int objectCount = objects.size();
					for (int objectIdx = 0; objectIdx < objectCount; objectIdx++) {
						Object3D object = objects.get(objectIdx);
						Object3DGroup _object3DGroup = object.object3dGroups[object3DGroupIdx];

						// set up textures
						Object3DGroup.setupTextures(renderer, object3DGroup, faceEntityIdx);

						// set up transparent render faces
						if (collectTransparentFaces == true) {
							transparentRenderFacesPool.createTransparentRenderFaces(
								(_object3DGroup.mesh.skinning == true?
									modelViewMatrix.identity():
									modelViewMatrix.set(_object3DGroup.groupTransformationsMatrix)
								).
								multiply(object.transformationsMatrix).
								multiply(modelViewMatrixBackup),
								object.object3dGroups[object3DGroupIdx],
								faceEntityIdx,
								faceIdx
							);
						}
					}
	
					// keep track of rendered faces
					faceIdx+= faces;
	
					// skip to next entity
					continue;
				}
	
				// optional texture coordinates
				if (isTextureCoordinatesAvailable == true) {
					// enable texturing client state if not yet done
					if (renderer.renderingTexturingClientState == false) {
						renderer.enableClientState(renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);
						renderer.renderingTexturingClientState = true;
					}
				} else {
					// disable texturing client state if not yet done
					if (renderer.renderingTexturingClientState == true) {
						renderer.disableClientState(renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);
						renderer.renderingTexturingClientState = false;
					}				
				}

				// draw this faces entity for each object
				int objectCount = objects.size();
				for (int objectIdx = 0; objectIdx < objectCount; objectIdx++) {
					Object3D object = objects.get(objectIdx);
					Object3DGroup _object3DGroup = object.object3dGroups[object3DGroupIdx];

					// set up material on first object
					if (objectIdx == 0) {
						// set up material
						setupMaterial(_object3DGroup, faceEntityIdx);						
					} else {
						// only set up textures
						Object3DGroup.setupTextures(renderer, _object3DGroup, faceEntityIdx);
					}

					//	check transparency via effect
					if (object.effectColorMul.getAlpha() < 1.0f - MathTools.EPSILON ||
						object.effectColorAdd.getAlpha() < -MathTools.EPSILON) {
						// add to transparent render faces, if requested
						if (collectTransparentFaces == true) {
							transparentRenderFacesPool.createTransparentRenderFaces(
								(_object3DGroup.mesh.skinning == true?
									modelViewMatrix.identity():
									modelViewMatrix.set(_object3DGroup.groupTransformationsMatrix)
								).
								multiply(object.transformationsMatrix).
								multiply(modelViewMatrixBackup),
								_object3DGroup,
								faceEntityIdx,
								faceIdx
							);
						}

						// skip to next object
						continue;
					}

					// bind buffer base objects if not bound yet
					int[] currentVBOGlIds = ((Object3DGroupVBORenderer)_object3DGroup.renderer).vboBaseIds;
					if (boundVBOBaseIds != currentVBOGlIds) {
						boundVBOBaseIds = currentVBOGlIds;

						//	texture coordinates
						if (isTextureCoordinatesAvailable == true) {
							renderer.bindTextureCoordinatesBufferObject(currentVBOGlIds[3]);
						}

						// 	vertices
						renderer.bindVerticesBufferObject(currentVBOGlIds[1]);

						// 	normals
						renderer.bindNormalsBufferObject(currentVBOGlIds[2]);

						// indices
						renderer.bindIndicesBufferObject(currentVBOGlIds[0]);
					}
					
					// bind tangent, bitangend buffers if not yet bound
					int[] currentVBOTangentBitangentIds = ((Object3DGroupVBORenderer)_object3DGroup.renderer).vboTangentBitangentIds;
					if (renderer.isNormalMappingAvailable() &&
						currentVBOTangentBitangentIds != null && currentVBOTangentBitangentIds != boundVBOTangentBitangentIds) {
						// tangent
						renderer.bindTangentsBufferObject(currentVBOTangentBitangentIds[0]);

						// bitangent
						renderer.bindBitangentsBufferObject(currentVBOTangentBitangentIds[1]);
					}

					// set up local -> world transformations matrix
					renderer.getModelViewMatrix().set(
						(_object3DGroup.mesh.skinning == true?
							modelViewMatrix.identity():
							modelViewMatrix.set(_object3DGroup.groupTransformationsMatrix)
						).
						multiply(object.transformationsMatrix).
						multiply(modelViewMatrixBackup)
					);
					renderer.onUpdateModelViewMatrix();

					// set up front face
					int objectFrontFace = matrix4x4Negative.isNegative(renderer.getModelViewMatrix()) == false?renderer.FRONTFACE_CCW:renderer.FRONTFACE_CW;
					if (objectFrontFace != currentFrontFace) {
						renderer.setFrontFace(objectFrontFace);
						currentFrontFace = objectFrontFace; 
					}

					// set up effect color
					renderer.setEffectColorMul(object.effectColorMul.getArray());
					renderer.setEffectColorAdd(object.effectColorAdd.getArray());
					renderer.onUpdateEffect();

					// do transformation start to shadow mapping
					if (shadowMapping != null) {
						shadowMapping.startObjectTransformations(
							(_object3DGroup.mesh.skinning == true?
								modelViewMatrix.identity():
								modelViewMatrix.set(_object3DGroup.groupTransformationsMatrix)
							).
							multiply(object.transformationsMatrix)
						);
					}

					// draw
					renderer.drawIndexedTrianglesFromBufferObjects(faces, faceIdx);
	
					// do transformations end to shadow mapping
					if (shadowMapping != null) {
						shadowMapping.endObjectTransformations();
					}
				}

				// keep track of rendered faces
				faceIdx+= faces;
	
			}
		}

		// unbind buffers
		renderer.unbindBufferObjects();

		// restore model view matrix / view matrix
		renderer.getModelViewMatrix().set(
			modelViewMatrixBackup
		);
	}

	/**
	 * Set ups a material for rendering
	 * @param object 3d group
	 * @param faces entity idx
	 */
	protected void setupMaterial(Object3DGroup object3DGroup, int facesEntityIdx) {
		FacesEntity[] facesEntities = object3DGroup.group.getFacesEntities(); 
		Material material = facesEntities[facesEntityIdx].getMaterial();

		// get material or use default
		if (material == null) material = Material.getDefaultMaterial();

		// setup textures
		Object3DGroup.setupTextures(renderer, object3DGroup, facesEntityIdx);

		// apply materials
		renderer.setMaterialAmbient(material.getAmbientColor().getArray());
		renderer.setMaterialDiffuse(material.getDiffuseColor().getArray());
		renderer.setMaterialSpecular(material.getSpecularColor().getArray());
		renderer.setMaterialEmission(material.getEmissionColor().getArray());
		renderer.setMaterialShininess(material.getShininess());
		renderer.onUpdateMaterial();

		// bind diffuse texture
		renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DIFFUSE);
		renderer.bindTexture(
			object3DGroup.dynamicDiffuseTextureIdsByEntities[facesEntityIdx] != Object3DGroup.GLTEXTUREID_NONE?
			object3DGroup.dynamicDiffuseTextureIdsByEntities[facesEntityIdx]:
			object3DGroup.materialDiffuseTextureIdsByEntities[facesEntityIdx]
		);

		// bind specular texture
		if (renderer.isSpecularMappingAvailable() == true) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_SPECULAR);
			renderer.bindTexture(
				object3DGroup.materialSpecularTextureIdsByEntities[facesEntityIdx]
			);
		}

		// bind displacement texture
		if (renderer.isDisplacementMappingAvailable() == true) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DISPLACEMENT);
			renderer.bindTexture(
				object3DGroup.materialDisplacementTextureIdsByEntities[facesEntityIdx]
			);
		}

		// bind normal texture
		if (renderer.isNormalMappingAvailable() == true) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_NORMAL);
			renderer.bindTexture(
				object3DGroup.materialNormalTextureIdsByEntities[facesEntityIdx]
			);
		}

		// bind texture
		renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DIFFUSE);
	}

	/**
	 * Clear material for rendering
	 * @param gl
	 * @param material
	 */
	protected void clearMaterial() {
		// unbind diffuse texture
		renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DIFFUSE);
		renderer.bindTexture(renderer.ID_NONE);
		// unbind specular texture
		if (renderer.isSpecularMappingAvailable() == true) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_SPECULAR);
			renderer.bindTexture(renderer.ID_NONE);
		}
		// unbind displacement texture
		if (renderer.isDisplacementMappingAvailable() == true) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DISPLACEMENT);
			renderer.bindTexture(renderer.ID_NONE);
		}
		// unbind normal texture
		if (renderer.isNormalMappingAvailable()) {
			renderer.setTextureUnit(LightingShader.TEXTUREUNIT_NORMAL);
			renderer.bindTexture(renderer.ID_NONE);
		}
		// set diffuse texture unit
		renderer.setTextureUnit(LightingShader.TEXTUREUNIT_DIFFUSE);
	}

	/**
	 * Creates a particle system entity key
	 * @param effect color add
	 * @param effect color mul
	 * @param depthBuffer
	 * @param sort
	 */
	private static void createPseKey(Key key, Color4 effectColorAdd, Color4 effectColorMul, boolean depthBuffer, boolean sort) {
		float[] efcaData = effectColorAdd.getArray();
		float[] efcmData = effectColorMul.getArray();
		key.reset();
		key.append(efcmData[0]);
		key.append(efcmData[1]);
		key.append(efcmData[2]);
		key.append(efcmData[3]);
		key.append(",");
		key.append(efcaData[0]);
		key.append(efcaData[1]);
		key.append(efcaData[2]);
		key.append(efcaData[3]);
		key.append(",");
		key.append((depthBuffer == true?"DBT":"DBF"));
		key.append(",");
		key.append((sort == true?"DS":"NS"));
	}

	/**
	 * Render batch VBO renderer points entities
	 * @param points batch VBO renderer points
	 */
	public void render(ArrayList<PointsParticleSystemEntity> visiblePses) {
		if (visiblePses.size() == 0) return;

		// store model view matrix
		modelViewMatrix.set(renderer.getModelViewMatrix());

		//
		boolean depthBuffer = false;

		// set up GL state
		renderer.enableBlending();
		renderer.disableDepthBuffer();
		renderer.disableClientState(renderer.CLIENTSTATE_NORMAL_ARRAY);
		renderer.enableClientState(renderer.CLIENTSTATE_COLOR_ARRAY);

		// 	disable texturing client state if not yet done
		if (renderer.renderingTexturingClientState == false) {
			renderer.enableClientState(renderer.CLIENTSTATE_TEXTURECOORD_ARRAY);
			renderer.renderingTexturingClientState = true;
		}

		// 	model view matrix
		renderer.getModelViewMatrix().identity();
		renderer.onUpdateModelViewMatrix();

		// find all keys which differentiate with effect colors and depth buffer
		for (int i = 0; i < visiblePses.size(); i++) {
			PointsParticleSystemEntityInternal ppse = visiblePses.get(i);
			Key key = pseKeyPool.allocate();
			createPseKey(
				key,
				ppse.getEffectColorAdd(),
				ppse.getEffectColorMul(),
				ppse.isPickable(),
				ppse.getParticleEmitter().getColorStart().equals(ppse.getParticleEmitter().getColorEnd()) == false
			);
			if (pseKeys.contains(key) == false) {
				pseKeys.add(key);
			} else {
				pseKeyPool.release(key);
			}
		}

		// process each key in inner loop
		Key innerPseKey = pseKeyPool.allocate();
		for (int i = 0; i < pseKeys.size(); i++) {
			// fetch key from available keys
			Key pseKey = pseKeys.get(i);
			boolean pseSort = false;
			PointsParticleSystemEntityInternal currentPse = null;

			// iterate all pses
			for (int j = 0; j < visiblePses.size(); j++) {
				PointsParticleSystemEntityInternal ppse = visiblePses.get(j);
				// check if ppse belongs to current key, otherwise skip this pse
				createPseKey(
					innerPseKey,
					ppse.getEffectColorAdd(),
					ppse.getEffectColorMul(),
					ppse.isPickable(),
					ppse.getParticleEmitter().getColorStart().equals(ppse.getParticleEmitter().getColorEnd()) == false
				);
				if (pseKey.equals(innerPseKey) == false) {
					continue;
				} else {
					currentPse = visiblePses.get(j);
					pseSort = ppse.getParticleEmitter().getColorStart().equals(ppse.getParticleEmitter().getColorEnd()) == false; 
				}

				// merge ppse pool
				pseTransparentRenderPointsPool.merge(ppse.getRenderPointsPool());
			}

			// sort
			if (pseSort == true) pseTransparentRenderPointsPool.sort();
	
			// put sorted points into batch renderer
			for (TransparentRenderPoint point: pseTransparentRenderPointsPool.getTransparentRenderPoints()) {
				if (point.acquired == false) break;
				psePointBatchVBORenderer.addPoint(point);
			}

			//
			renderer.setEffectColorAdd(currentPse.getEffectColorAdd().getArray());
			renderer.setEffectColorMul(currentPse.getEffectColorMul().getArray());
			renderer.onUpdateEffect();

			depthBuffer = currentPse.isPickable();
			if (depthBuffer) {
				renderer.enableDepthBuffer();
			} else {
				renderer.disableDepthBuffer();
			}
	
			// render, clear
			psePointBatchVBORenderer.render();
			psePointBatchVBORenderer.clear();
	
			// reset pool
			pseTransparentRenderPointsPool.reset();
		}

		// release pse keys and current pse key
		for (int i = 0; i < pseKeys.size(); i++) {
			pseKeyPool.release(pseKeys.get(i));
		}
		pseKeys.clear();
		pseKeyPool.release(innerPseKey);

		// restore gl state
		renderer.disableBlending();
		if (depthBuffer == false) renderer.enableDepthBuffer();
		renderer.unbindBufferObjects();
		renderer.enableClientState(renderer.CLIENTSTATE_NORMAL_ARRAY);
		renderer.disableClientState(renderer.CLIENTSTATE_COLOR_ARRAY);
		renderer.getModelViewMatrix().set(modelViewMatrix);
	}

}

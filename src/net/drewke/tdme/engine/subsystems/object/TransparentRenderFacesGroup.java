package net.drewke.tdme.engine.subsystems.object;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayList;
import net.drewke.tdme.utils.Console;
import net.drewke.tdme.utils.Key;

/**
 * Transparent render faces group
 * @author andreas.drewke
 * @version $Id$
 */
public final class TransparentRenderFacesGroup {

	//
	static private Matrix4x4 modelViewMatrix = new Matrix4x4();
	private Object3DVBORenderer object3DVBORenderer;
	private ArrayList<BatchVBORendererTriangles> batchVBORenderers;
	private Model model;
	private Object3DGroup object3DGroup;
	private int facesEntityIdx;
	protected Color4 effectColorAdd;
	protected Color4 effectColorMul;
	private boolean depthBuffer;
	private Material material;
	private boolean textureCoordinates;

	/**
	 * Protected constructor
	 */
	protected TransparentRenderFacesGroup() {
		this.object3DVBORenderer = null;
		this.batchVBORenderers = new ArrayList<BatchVBORendererTriangles>();
		this.model = null;
		this.object3DGroup = null;
		this.facesEntityIdx = -1;
		this.effectColorAdd = null;
		this.effectColorMul = null;
		this.depthBuffer = false;
		this.material = null;
		this.textureCoordinates = false;
	}

	/**
	 * Set transparent render faces group
	 * @param object3D VBO renderer
	 * @param batch VBO renderer
	 * @param model
	 * @param object 3D group
	 * @param faces entity idx
	 * @param effect color add
	 * @param effect color mul
	 * @param depth buffer
	 * @param material
	 * @param texture coordinates
	 */
	protected void set(Object3DVBORenderer object3DVBORenderer, Model model, Object3DGroup object3DGroup, int facesEntityIdx, Color4 effectColorAdd, Color4 effectColorMul, boolean depthBuffer, Material material, boolean textureCoordinates) {
		this.object3DVBORenderer = object3DVBORenderer;
		this.batchVBORenderers.clear();
		this.model = model;
		this.object3DGroup = object3DGroup;
		this.facesEntityIdx = facesEntityIdx;
		this.effectColorAdd = effectColorAdd;
		this.effectColorMul = effectColorMul;
		this.depthBuffer = depthBuffer;
		this.material = material;
		this.textureCoordinates = textureCoordinates;
	}

	/**
	 * Creates a key for given transparent render faces group attributes
	 * @param model
	 * @param object 3D group
	 * @param faces entity idx
	 * @param effect color add
	 * @param effect color mul
	 * @param depth buffer
	 * @param material
	 * @param texture coordinates
	 * @return
	 */
	protected static void createKey(Key key, Model model, Object3DGroup object3DGroup, int facesEntityIdx, Color4 effectColorAdd, Color4 effectColorMul, boolean depthBuffer, Material material, boolean textureCoordinates) {
		float[] efcmData = effectColorMul.getArray();
		float[] efcaData = effectColorAdd.getArray();
		key.reset();
		key.append(model.getId());
		key.append(",");
		key.append(object3DGroup.id);
		key.append(",");
		key.append(facesEntityIdx);
		key.append(",");
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
		key.append((material == null?"tdme.material.none":material.getId()));
		key.append(",");
		key.append((textureCoordinates == true?"TCT":"TCF"));		
	}

	/**
	 * Adds a vertex to this transparent render faces group
	 * @param vertex
	 * @param normal
	 * @param texture coordinate
	 */
	protected void addVertex(Vector3 vertex, Vector3 normal, TextureCoordinate textureCoordinate) {
		// check if we have a batch renderer already? 
		if (batchVBORenderers.size() == 0) {
			// nope, add first one
			BatchVBORendererTriangles batchVBORendererTriangles = object3DVBORenderer.acquireTrianglesBatchVBORenderer();
			if (batchVBORendererTriangles == null) {
				Console.println("TransparentRenderFacesGroup::addVertex(): could not acquire triangles batch vbo renderer");
				return;
			}
			batchVBORenderers.add(batchVBORendererTriangles);
		}

		// try to add vertex
		BatchVBORendererTriangles batchVBORendererTriangles = batchVBORenderers.get(batchVBORenderers.size() - 1);
		if (batchVBORendererTriangles.addVertex(vertex, normal, textureCoordinate) == true) return;

		// failed, acquire additionally one
		batchVBORendererTriangles = object3DVBORenderer.acquireTrianglesBatchVBORenderer();
		if (batchVBORendererTriangles == null) {
			Console.println("TransparentRenderFacesGroup::addVertex(): could not acquire triangles batch vbo renderer");
			return;
		}
		// 	add it
		batchVBORenderers.add(batchVBORendererTriangles);			
		// 	add vertex
		batchVBORendererTriangles.addVertex(vertex, normal, textureCoordinate);
	}

	/**
	 * Render this transparent render faces group
	 * @param renderer
	 * @param depth buffer mode
	 */
	protected void render(GLRenderer renderer, Object3DVBORenderer.DepthBufferMode depthBufferMode) {
		// store model view matrix
		modelViewMatrix.set(renderer.getModelViewMatrix());

		// texture coordinate
		if (textureCoordinates == true) {
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

		// effect
		renderer.setEffectColorMul(effectColorMul.getArray());
		renderer.setEffectColorAdd(effectColorAdd.getArray());
		renderer.onUpdateEffect();

		// depth buffer
		if (depthBufferMode != Object3DVBORenderer.DepthBufferMode.IGNORE) {
			if (depthBuffer || depthBufferMode == Object3DVBORenderer.DepthBufferMode.FORCE) {
				renderer.enableDepthBuffer();
			} else {
				renderer.disableDepthBuffer();
			}
		}

		// material
		object3DVBORenderer.setupMaterial(object3DGroup, facesEntityIdx);

		// model view matrix
		renderer.getModelViewMatrix().identity();
		renderer.onUpdateModelViewMatrix();

		// render, reset
		for (int i = 0; i < batchVBORenderers.size(); i++) {
			BatchVBORendererTriangles batchVBORendererTriangles = batchVBORenderers.get(i);
			batchVBORendererTriangles.render();
			batchVBORendererTriangles.clear();
			batchVBORendererTriangles.release();
		}
		batchVBORenderers.clear();

		// restore gl state
		if (depthBufferMode != Object3DVBORenderer.DepthBufferMode.IGNORE) {
			renderer.disableDepthBuffer();
		}
		renderer.unbindBufferObjects();
		renderer.getModelViewMatrix().set(modelViewMatrix);
		renderer.onUpdateModelViewMatrix();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "TransparentRenderFacesGroup [model=" + model.getId()
				+ ", object3DGroup=" + object3DGroup.id;
	}

}

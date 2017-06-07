package net.drewke.tdme.engine;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.subsystems.renderer.GLRenderer;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;

/**
 * TDME engine entity
 * @author Andreas Drewke
 * @version $Id$
 */
public interface Entity {


	/**************************************************************************
	 * Engine/Renderer                                                        *
	 **************************************************************************/

	/**
	 * Set up engine
	 * @param engine
	 */
	public void setEngine(Engine engine);

	/**
	 * Set up renderer
	 * @param renderer
	 */
	public void setRenderer(GLRenderer renderer);

	/**************************************************************************
	 * General Data                                                           *
	 **************************************************************************/

	/**
	 * @return object id
	 */
	public String getId();

	/**
	 * @return true if enabled to be rendered
	 */
	public boolean isEnabled();

	/**
	 * Enable/disable rendering
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @return if object is pickable
	 */
	public boolean isPickable();

	/**
	 * Set this object pickable
	 * @param pickable
	 */
	public void setPickable(boolean pickable);

	/**
	 * @return dynamic shadowing enabled
	 */
	public boolean isDynamicShadowingEnabled();

	/**
	 * Enable/disable dynamic shadowing
	 * @param dynamicShadowing
	 */
	public void setDynamicShadowingEnabled(boolean dynamicShadowing);

	/**************************************************************************
	 * Effect Color                                                           *
	 **************************************************************************/

	/**
	 * The effect color will be multiplied with fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorMul();

	/**
	 * The effect color will be added to fragment color
	 * @return effect color
	 */
	public Color4 getEffectColorAdd();


	/**************************************************************************
	 * Init/Dispose/Rendering                                                 *
	 **************************************************************************/

	/**
	 * Initiates this object 3d 
	 */
	public void initialize();

	/**
	 * Dispose this object 3d
	 */
	public void dispose();

	/**
	 * @return bounding box / in model coordinate space
	 */
	public BoundingBox getBoundingBox();

	/**
	 * @return bounding box transformed / in world coordinate space
	 */
	public BoundingBox getBoundingBoxTransformed();

	/**************************************************************************
	 * Transformations                                                        *
	 **************************************************************************/

	/**
	 * @return object translation
	 */
	public Vector3 getTranslation();

	/** 
	 * @return object scale
	 */
	public Vector3 getScale();

	/**
	 * @return pivot or center of rotations
	 */
	public Vector3 getPivot();

	/**
	 * @return object rotations
	 */
	public Rotations getRotations();

	/**
	 * @return this transformations matrix
	 */
	public Matrix4x4 getTransformationsMatrix();

	/**
	 * Set up this transformations from given transformations
	 * @param transformations
	 */
	public void fromTransformations(Transformations transformations);

	/**
	 * Update transformations
	 */
	public void update();

}

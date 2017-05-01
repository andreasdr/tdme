package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener.Type;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.controller.ModelViewerScreenController.BoundingVolumeType;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.EntityBoundingVolumeView;
import net.drewke.tdme.tools.shared.views.PopUps;
import net.drewke.tdme.utils.MutableString;

/**
 * Entity bounding volume sub screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public class EntityBoundingVolumeSubScreenController {

	private FileDialogPath modelPath;

	private EntityBoundingVolumeView view;

	private GUIElementNode[] boundingVolumeTypeDropDown;
	private GUIElementNode[] boundingVolumeNoneApply;
	private GUIElementNode[] boundingVolume;
	private GUIElementNode[] boundingvolumeSphereCenter;
	private GUIElementNode[] boundingvolumeSphereRadius;
	private GUIElementNode[] boundingvolumeCapsuleA;
	private GUIElementNode[] boundingvolumeCapsuleB;
	private GUIElementNode[] boundingvolumeCapsuleRadius;
	private GUIElementNode[] boundingvolumeBoundingBoxMin;
	private GUIElementNode[] boundingvolumeBoundingBoxMax;
	private GUIElementNode[] boundingvolumeObbCenter;
	private GUIElementNode[] boundingvolumeObbHalfextension;
	private GUIElementNode[] boundingvolumeObbRotationX;
	private GUIElementNode[] boundingvolumeObbRotationY;
	private GUIElementNode[] boundingvolumeObbRotationZ;
	private GUIElementNode[] boundingvolumeConvexMeshFile;

	private MutableString value;

	/**
	 * Public constructor
	 * @param pop ups
	 * @param model viewer screen controller
	 */
	public EntityBoundingVolumeSubScreenController(PopUps popUps, FileDialogPath modelPath) {
		this.modelPath = modelPath;
		this.view = new EntityBoundingVolumeView(this, popUps);
	}

	/**
	 * @return view
	 */
	public EntityBoundingVolumeView getView() {
		return view;
	}

	/**
	 * Init
	 * @param screen node
	 */
	public void init(GUIScreenNode screenNode) {
		value = new MutableString();

		// load screen node
		try {
			// we have fixed 8 BVs per object currently
			boundingVolumeTypeDropDown = new GUIElementNode[8];
			boundingVolumeNoneApply = new GUIElementNode[8];
			boundingVolume = new GUIElementNode[8];
			boundingvolumeSphereCenter = new GUIElementNode[8];
			boundingvolumeSphereRadius = new GUIElementNode[8];
			boundingvolumeCapsuleA = new GUIElementNode[8];
			boundingvolumeCapsuleB = new GUIElementNode[8];
			boundingvolumeCapsuleRadius = new GUIElementNode[8];
			boundingvolumeBoundingBoxMin = new GUIElementNode[8];
			boundingvolumeBoundingBoxMax = new GUIElementNode[8];
			boundingvolumeObbCenter = new GUIElementNode[8];
			boundingvolumeObbCenter = new GUIElementNode[8];
			boundingvolumeObbHalfextension = new GUIElementNode[8];
			boundingvolumeObbRotationX = new GUIElementNode[8];
			boundingvolumeObbRotationY = new GUIElementNode[8];
			boundingvolumeObbRotationZ = new GUIElementNode[8];
			boundingvolumeConvexMeshFile = new GUIElementNode[8];
			for (int i = 0; i < 8; i++) {
				boundingVolumeTypeDropDown[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_type_" + i);
				boundingVolumeNoneApply[i] = (GUIElementNode)screenNode.getNodeById("button_boundingvolume_apply_" + i);
				boundingVolume[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_" + i);
				boundingvolumeSphereCenter[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_sphere_center_" + i);
				boundingvolumeSphereRadius[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_sphere_radius_" + i);
				boundingvolumeCapsuleA[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_a_" + i);
				boundingvolumeCapsuleB[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_b_" + i);
				boundingvolumeCapsuleRadius[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_radius_" + i);
				boundingvolumeBoundingBoxMin[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_aabb_min_" + i);
				boundingvolumeBoundingBoxMax[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_aabb_max_" + i);
				boundingvolumeObbCenter[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_center_" + i);
				boundingvolumeObbCenter[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_center_" + i);
				boundingvolumeObbHalfextension[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_halfextension_" + i);
				boundingvolumeObbRotationX[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_rotation_x_" + i);
				boundingvolumeObbRotationY[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_rotation_y_" + i);
				boundingvolumeObbRotationZ[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_rotation_z_" + i);
				boundingvolumeConvexMeshFile[i] = (GUIElementNode)screenNode.getNodeById("boundingvolume_convexmesh_file_" + i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disable bounding volume
	 */
	public void disableBoundingVolume(int idx) {
		view.selectBoundingVolumeType(idx, 0);
		boundingVolumeTypeDropDown[idx].getController().setDisabled(true);
		boundingVolumeNoneApply[idx].getController().setDisabled(true);
	}

	/**
	 * Enable bounding volume
	 * @param idx
	 */
	public void enableBoundingVolume(int idx) {
		boundingVolumeTypeDropDown[idx].getController().setDisabled(false);
		boundingVolumeNoneApply[idx].getController().setDisabled(false);
	}

	/**
	 * Set up model bounding volume type
	 * @param entity
	 * @param idx
	 */
	public void setupModelBoundingVolumeType(LevelEditorEntity entity, int idx) {
		// model
		if (entity == null) {
			// no model, select bv none
			view.selectBoundingVolumeType(idx, 0);
			return;
		}

		// entity bounding volume @ idx
		LevelEditorEntityBoundingVolume entityBoundingVolume = entity.getBoundingVolumeAt(idx);
		if (entityBoundingVolume == null) {
			// no entity bounding volume, select bv none
			view.selectBoundingVolumeType(idx, 0);
		} else {
			BoundingVolume bv = entityBoundingVolume.getBoundingVolume();
			// none
			if (bv == null) {
				view.selectBoundingVolumeType(idx, 0);
			} else
			// sphere
			if (bv instanceof Sphere) {
				view.selectBoundingVolumeType(idx, 1);
			} else
			// capsule
			if (bv instanceof Capsule) {
				view.selectBoundingVolumeType(idx, 2);
			} else
			// bounding box
			if (bv instanceof BoundingBox) {
				view.selectBoundingVolumeType(idx, 3);
			} else
			// oriented bounding box
			if (bv instanceof OrientedBoundingBox) {
				view.selectBoundingVolumeType(idx, 4);
			} else
			// convex mesh
			if (bv instanceof ConvexMesh) {
				view.selectBoundingVolumeType(idx, 5);
			} else {
				// none known
				System.out.println("ModelViewerScreenController::onTabSelected(): invalid bounding volume@" + idx + ": " + bv);
			}
		}
	}
	
	/**
	 * Set up bounding volume types
	 * @param idx
	 * @param bounding volume types
	 */
	public void setupBoundingVolumeTypes(int idx, String[] boundingVolumeTypes) {
		// bounding volume types drop downs inner
		GUIParentNode boundingVolumeTypeDropDownInnerNode = (GUIParentNode)(boundingVolumeTypeDropDown[idx].getScreenNode().getNodeById(boundingVolumeTypeDropDown[idx].getId() + "_inner"));

		// construct XML for sub nodes
		int bvIdx = 0;
		String boundingVolumeTypeDropDownSubNodesXML = "";
		boundingVolumeTypeDropDownSubNodesXML+= "<scrollarea-vertical width=\"100%\" height=\"80\">";
		for (String bvType: boundingVolumeTypes) {
			boundingVolumeTypeDropDownSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(bvType) + "\" value=\"" +  + (bvIdx++) + "\" />\n";
		}
		boundingVolumeTypeDropDownSubNodesXML+= "</scrollarea-vertical>";

		// inject sub nodes
		try {
			boundingVolumeTypeDropDownInnerNode.replaceSubNodes(
				boundingVolumeTypeDropDownSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display given bounding volume GUI elements
	 * @param bvType
	 */
	public void selectBoundingVolume(int idx, BoundingVolumeType bvType) {
		boundingVolume[idx].getActiveConditions().remove("sphere");
		boundingVolume[idx].getActiveConditions().remove("capsule");
		boundingVolume[idx].getActiveConditions().remove("aabb");
		boundingVolume[idx].getActiveConditions().remove("obb");
		boundingVolume[idx].getActiveConditions().remove("convexmesh");
		switch (bvType) {
			case NONE:
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("0"));
				break;
			case SPHERE:
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("1"));
				boundingVolume[idx].getActiveConditions().add("sphere");
				break;
			case CAPSULE:
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("2"));
				boundingVolume[idx].getActiveConditions().add("capsule");
				break;
			case BOUNDINGBOX: 
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("3"));
				boundingVolume[idx].getActiveConditions().add("aabb");
				break;
			case ORIENTEDBOUNDINGBOX: 
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("4"));
				boundingVolume[idx].getActiveConditions().add("obb");
				break;
			case CONVEXMESH:
				boundingVolumeTypeDropDown[idx].getController().setValue(value.set("5"));
				boundingVolume[idx].getActiveConditions().add("convexmesh");
				break;
		}
	}

	/**
	 * Setup sphere bounding volume
	 * @param idx
	 * @param center
	 * @param radius
	 */
	public void setupSphere(int idx, Vector3 center, float radius) {
		selectBoundingVolume(idx, BoundingVolumeType.SPHERE);
		boundingvolumeSphereCenter[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(center.getX())).
			append(", ").
			append(Tools.formatFloat(center.getY())).
			append(", ").
			append(Tools.formatFloat(center.getZ()))
		);
		boundingvolumeSphereRadius[idx].getController().setValue(
			value.set(Tools.formatFloat(radius))
		);
	}

	/**
	 * Setup capsule bounding volume
	 * @param idx
	 * @param center
	 * @param radius
	 */
	public void setupCapsule(int idx, Vector3 a, Vector3 b, float radius) {
		selectBoundingVolume(idx, BoundingVolumeType.CAPSULE);
		boundingvolumeCapsuleA[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(a.getX())).
			append(", ").
			append(Tools.formatFloat(a.getY())).
			append(", ").
			append(Tools.formatFloat(a.getZ()))
		);
		boundingvolumeCapsuleB[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(b.getX())).
			append(", ").
			append(Tools.formatFloat(b.getY())).
			append(", ").
			append(Tools.formatFloat(b.getZ()))
		);
		boundingvolumeCapsuleRadius[idx].getController().setValue(
			value.set(Tools.formatFloat(radius))
		);
	}

	/**
	 * Setup AABB bounding volume
	 * @param idx
	 * @param min
	 * @param max
	 */
	public void setupBoundingBox(int idx, Vector3 min, Vector3 max) {
		selectBoundingVolume(idx, BoundingVolumeType.BOUNDINGBOX);
		boundingvolumeBoundingBoxMin[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(min.getX())).
			append(", ").
			append(Tools.formatFloat(min.getY())).
			append(", ").
			append(Tools.formatFloat(min.getZ()))
		);
		boundingvolumeBoundingBoxMax[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(max.getX())).
			append(", ").
			append(Tools.formatFloat(max.getY())).
			append(", ").
			append(Tools.formatFloat(max.getZ()))
		);
	}

	/**
	 * Setup oriented bounding box
	 * @param idx
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 */
	public void setupOrientedBoundingBox(int idx, Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		// set up rotation maxtrix
		Vector3 rotation = new Vector3();
		Matrix4x4 rotationMatrix = new Matrix4x4().identity();
		rotationMatrix.setAxes(axis0, axis1, axis2);
		rotationMatrix.computeEulerAngles(rotation);

		// set up obb in screen
		selectBoundingVolume(idx, BoundingVolumeType.ORIENTEDBOUNDINGBOX);
		boundingvolumeObbCenter[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(center.getX())).
			append(", ").
			append(Tools.formatFloat(center.getY())).
			append(", ").
			append(Tools.formatFloat(center.getZ()))
		);
		boundingvolumeObbHalfextension[idx].getController().setValue(
			value.reset().
			append(Tools.formatFloat(halfExtension.getX())).
			append(", ").
			append(Tools.formatFloat(halfExtension.getY())).
			append(", "). 
			append(Tools.formatFloat(halfExtension.getZ()))
		);
		boundingvolumeObbRotationX[idx].getController().setValue(
			value.set(Tools.formatFloat(rotation.getX()))
		);
		boundingvolumeObbRotationY[idx].getController().setValue(
			value.set(Tools.formatFloat(rotation.getY()))
		);
		boundingvolumeObbRotationZ[idx].getController().setValue(
			value.set(Tools.formatFloat(rotation.getZ()))
		);
	}

	/**
	 * Setup convex mesh bounding volume
	 * @param idx
	 * @param file
	 */
	public void setupConvexMesh(int idx, String file) {
		selectBoundingVolume(idx, BoundingVolumeType.CONVEXMESH);
		boundingvolumeConvexMeshFile[idx].getController().setValue(
			value.set(file)
		);
	}

	/**
	 * On pivot apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeTypeApply(LevelEditorEntity entity, int idx) {
		int boundingVolumeTypeId = Tools.convertToIntSilent(boundingVolumeTypeDropDown[idx].getController().getValue().toString());
		view.selectBoundingVolumeType(idx, boundingVolumeTypeId);
		switch(boundingVolumeTypeId) {
			case(0): onBoundingVolumeNoneApply(entity, idx); break;
			case(1): onBoundingVolumeSphereApply(entity, idx); break;
			case(2): onBoundingVolumeCapsuleApply(entity, idx); break;
			case(3): onBoundingVolumeAabbApply(entity, idx); break;
			case(4): onBoundingVolumeObbApply(entity, idx); break;
			case(5): onBoundingVolumeConvexMeshApply(entity, idx); break;
		}
	}

	/**
	 * On bounding volume none apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeNoneApply(LevelEditorEntity entity, int idx) {
		view.applyBoundingVolumeNone(entity, idx);
		view.resetBoundingVolume(entity, idx);
	}

	/**
	 * On bounding volume sphere apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeSphereApply(LevelEditorEntity entity, int idx) {
		try {
			view.applyBoundingVolumeSphere(
				entity,
				idx,
				Tools.convertToVector3(boundingvolumeSphereCenter[idx].getController().getValue().toString()),
				Tools.convertToFloat(boundingvolumeSphereRadius[idx].getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume capsule apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeCapsuleApply(LevelEditorEntity entity, int idx) {
		try {
			view.applyBoundingVolumeCapsule(
				entity,
				idx,
				Tools.convertToVector3(boundingvolumeCapsuleA[idx].getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeCapsuleB[idx].getController().getValue().toString()),
				Tools.convertToFloat(boundingvolumeCapsuleRadius[idx].getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume AABB apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeAabbApply(LevelEditorEntity entity, int idx) {
		try {
			view.applyBoundingVolumeAabb(
				entity,
				idx,
				Tools.convertToVector3(boundingvolumeBoundingBoxMin[idx].getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeBoundingBoxMax[idx].getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume OBB apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeObbApply(LevelEditorEntity entity, int idx) {
		try {
			// rotation axes by rotation angle for x,y,z
			Transformations rotations = new Transformations();
			rotations.getRotations().add(new Rotation(Tools.convertToFloat(boundingvolumeObbRotationZ[idx].getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Z));
			rotations.getRotations().add(new Rotation(Tools.convertToFloat(boundingvolumeObbRotationY[idx].getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_Y));
			rotations.getRotations().add(new Rotation(Tools.convertToFloat(boundingvolumeObbRotationX[idx].getController().getValue().toString()), OrientedBoundingBox.AABB_AXIS_X));
			rotations.update();

			// extract axes from matrix
			Vector3 xAxis = new Vector3();
			Vector3 yAxis = new Vector3();
			Vector3 zAxis = new Vector3();
			rotations.getTransformationsMatrix().getAxes(xAxis, yAxis, zAxis);

			// delegate to view
			view.applyBoundingVolumeObb(
				entity,
				idx,
				Tools.convertToVector3(boundingvolumeObbCenter[idx].getController().getValue().toString()),
				xAxis,
				yAxis,
				zAxis,
				Tools.convertToVector3(boundingvolumeObbHalfextension[idx].getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume convex mesh apply
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeConvexMeshApply(LevelEditorEntity entity, int idx) {
		view.applyBoundingVolumeConvexMesh(
			entity,
			idx,
			boundingvolumeConvexMeshFile[idx].getController().getValue().toString()
		);
	}

	/**
	 * On bounding volume convex mesh file clicked
	 * @param entity
	 * @param idx
	 */
	public void onBoundingVolumeConvexMeshFile(LevelEditorEntity entity, int idx) {
		final int idxFinal = idx;
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath.getPath(),
			"Load from: ", 
			new String[]{"dae", "tm"},
			entity.getBoundingVolumeAt(idx).getModelMeshFile() != null?entity.getBoundingVolumeAt(idx).getModelMeshFile():entity.getFileName(),
			new Action() {
				public void performAction() {
					boundingvolumeConvexMeshFile[idxFinal].getController().setValue(value.set(
						view.getPopUpsViews().getFileDialogScreenController().getFileName())
					);
					onBoundingVolumeConvexMeshApply(entity, idxFinal);
					modelPath.setPath(view.getPopUpsViews().getFileDialogScreenController().getPathName());
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
			}
		);
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		view.getPopUpsViews().getInfoDialogScreenController().show(caption, message);
	}

	/**
	 * On action performed
	 * @param type
	 * @param node
	 * @param entity
	 */
	public void onActionPerformed(Type type, GUIElementNode node, LevelEditorEntity entity) {
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().startsWith("button_boundingvolume_apply_")) {
						onBoundingVolumeTypeApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_sphere_apply_")) {
						onBoundingVolumeSphereApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_capsule_apply_")) {
						onBoundingVolumeCapsuleApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_obb_apply_")) {
						onBoundingVolumeObbApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_aabb_apply_")) {
						onBoundingVolumeAabbApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_convexmesh_apply_")) {
						onBoundingVolumeConvexMeshApply(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} 
					if (node.getId().startsWith("button_boundingvolume_convexmesh_file_")) {
						onBoundingVolumeConvexMeshFile(entity, Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else {
						System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					}
					break;
				}
			case PERFORMING:
				{
					// System.out.println("ModelViewerScreenController::onActionPerformed()::unknown, type='" + type + "', id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
					break;
				}
		}
	}

}

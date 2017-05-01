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
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Quaternion;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityBoundingVolume;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.tools.shared.views.ModelViewerView;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.utils.MutableString;

/**
 * Model viewer screen controller
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ModelViewerScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	public enum BoundingVolumeType {NONE, SPHERE, CAPSULE, BOUNDINGBOX, ORIENTEDBOUNDINGBOX, CONVEXMESH};

	private final static MutableString TEXT_EMPTY = new MutableString("");

	private EntityBaseSubScreenController entityBaseSubScreenController;
	private EntityDisplaySubScreenController entityDisplaySubScreenController;

	private final ModelViewerView view;

	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode modelReload;
	private GUIElementNode modelSave;
	private GUIElementNode pivotX;
	private GUIElementNode pivotY;
	private GUIElementNode pivotZ;
	private GUIElementNode pivotApply;
	private GUIElementNode statsOpaqueFaces;
	private GUIElementNode statsTransparentFaces;
	private GUIElementNode statsMaterialCount;
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

	private String modelPath = ".";

	/**
	 * Public constructor
	 * @param view
	 */
	public ModelViewerScreenController(ModelViewerView view) {
		this.view = view;
		final ModelViewerView finalView = view;
		this.entityBaseSubScreenController = new EntityBaseSubScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				finalView.updateGUIElements();
				finalView.onSetEntityData();
			}
		});
		this.entityDisplaySubScreenController = new EntityDisplaySubScreenController();
	}

	/**
	 * @return entity display sub screen controller
	 */
	public EntityDisplaySubScreenController getEntityDisplaySubScreenController() {
		return entityDisplaySubScreenController;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return model path
	 */
	public String getModelPath() {
		return modelPath;
	}

	/**
	 * Set model path
	 * @param model path
	 */
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/viewer/gui", "screen_modelviewer.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			modelReload = (GUIElementNode)screenNode.getNodeById("button_model_reload");
			modelSave = (GUIElementNode)screenNode.getNodeById("button_model_save");
			pivotX = (GUIElementNode)screenNode.getNodeById("pivot_x");
			pivotY = (GUIElementNode)screenNode.getNodeById("pivot_y");
			pivotZ = (GUIElementNode)screenNode.getNodeById("pivot_z");
			pivotApply = (GUIElementNode)screenNode.getNodeById("button_pivot_apply");

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
			statsOpaqueFaces = (GUIElementNode)screenNode.getNodeById("stats_opaque_faces");
			statsTransparentFaces = (GUIElementNode)screenNode.getNodeById("stats_transparent_faces");
			statsMaterialCount = (GUIElementNode)screenNode.getNodeById("stats_material_count");
			statsOpaqueFaces.getController().setDisabled(true);
			statsTransparentFaces.getController().setDisabled(true);
			statsMaterialCount.getController().setDisabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init entity base screen controller
		entityBaseSubScreenController.init(screenNode);

		// init entity base screen controller
		entityDisplaySubScreenController.init(screenNode);

		//
		value = new MutableString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Set screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getText().set(text);
		screenNode.layout(screenCaption);
	}

	/**
	  * Set up general entity data
	  * @param name
	  * @param description
	  */
	public void setEntityData(String name, String description) {
		entityBaseSubScreenController.setEntityData(name, description);
		modelReload.getController().setDisabled(false);
		modelSave.getController().setDisabled(false);
	}

	/**
	 * Unset entity data
	 */
	public void unsetEntityData() {
		entityBaseSubScreenController.unsetEntityData();
		modelReload.getController().setDisabled(true);
		modelSave.getController().setDisabled(true);
	}

	/**
	 * Set up entity properties
	 * @param preset id
	 * @param entity properties
	 * @param selected name
	 */
	public void setEntityProperties(String presetId, Iterable<PropertyModelClass> entityProperties, String selectedName) {
		entityBaseSubScreenController.setEntityProperties(view.getEntity(), presetId, entityProperties, selectedName);
	}

	/**
 	 * Unset entity properties
	 */
	public void unsetEntityProperties() {
		entityBaseSubScreenController.unsetEntityProperties();
	}

	/**
	 * Set pivot tab
	 * @param pivot
	 */
	public void setPivot(Vector3 pivot) {
		pivotX.getController().setDisabled(false);
		pivotX.getController().getValue().set(Tools.formatFloat(pivot.getX()));
		pivotY.getController().setDisabled(false);
		pivotY.getController().getValue().set(Tools.formatFloat(pivot.getY()));
		pivotZ.getController().setDisabled(false);
		pivotZ.getController().getValue().set(Tools.formatFloat(pivot.getZ()));
		pivotApply.getController().setDisabled(false);
	}

	/**
	 * Unset pivot tab
	 */
	public void unsetPivot() {
		pivotX.getController().setDisabled(true);
		pivotX.getController().getValue().set(TEXT_EMPTY);
		pivotY.getController().setDisabled(true);
		pivotY.getController().getValue().set(TEXT_EMPTY);
		pivotZ.getController().setDisabled(true);
		pivotZ.getController().getValue().set(TEXT_EMPTY);
		pivotApply.getController().setDisabled(true);
	}

	/**
	 * Set up model statistics
	 * @param stats opaque faces
	 * @param stats transparent faces
	 * @param stats material count
	 */
	public void setStatistics(int statsOpaqueFaces, int statsTransparentFaces, int statsMaterialCount) {
		this.statsOpaqueFaces.getController().setValue(value.set(statsOpaqueFaces));
		this.statsTransparentFaces.getController().setValue(value.set(statsTransparentFaces));
		this.statsMaterialCount.getController().setValue(value.set(statsMaterialCount));
	}

	/**
	 * On quit
	 */
	public void onQuit() {
		TDMEViewer.getInstance().quit();
	}

	/**
	 * On model load
	 */
	public void onModelLoad() {
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath,
			"Load from: ", 
			new String[]{"tmm", "dae", "tm"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.loadFile(
						view.getPopUpsViews().getFileDialogScreenController().getPathName(),
						view.getPopUpsViews().getFileDialogScreenController().getFileName()
					);
					modelPath = view.getPopUpsViews().getFileDialogScreenController().getPathName();
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On model save
	 */
	public void onModelSave() {
		// try to use entity file name
		String fileName = view.getEntity().getEntityFileName();
		// do we have a entity name?
		if (fileName == null) {
			// nope, take file model filename
			fileName = view.getFileName();
			// and add .tmm
			if (fileName.toLowerCase().endsWith(".tmm") == false) {
				fileName+= ".tmm";
			}
		}
		// we only want the file name and not path
		fileName = Tools.getFileName(fileName);

		//
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath,
			"Save from: ", 
			new String[]{"tmm"},
			fileName,
			new Action() {
				public void performAction() {
					try {
						view.saveFile(
							view.getPopUpsViews().getFileDialogScreenController().getPathName(),
							view.getPopUpsViews().getFileDialogScreenController().getFileName()
						);
						modelPath = view.getPopUpsViews().getFileDialogScreenController().getPathName();
						view.getPopUpsViews().getFileDialogScreenController().close();
					} catch (Exception ioe) {
						showErrorPopUp("Warning", ioe.getMessage());
					}
				}
				
			}
		);
	}

	/**
	 * On model reload
	 */
	public void onModelReload() {
		view.reloadFile();
	}

	/**
	 * On pivot apply
	 */
	public void onPivotApply() {
		try {
			float x = Float.parseFloat(pivotX.getController().getValue().toString());
			float y = Float.parseFloat(pivotY.getController().getValue().toString());
			float z = Float.parseFloat(pivotZ.getController().getValue().toString());
			view.pivotApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
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
	 * @param idx
	 */
	public void setupModelBoundingVolumeType(int idx) {
		// model
		LevelEditorEntity model = view.getEntity();
		if (model == null) {
			// no model, select bv none
			view.selectBoundingVolumeType(idx, 0);
			return;
		}

		// entity bounding volume @ idx
		LevelEditorEntityBoundingVolume entityBoundingVolume = model.getBoundingVolumeAt(idx);
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
	 */
	public void onBoundingVolumeTypeApply(int idx) {
		int boundingVolumeTypeId = Tools.convertToIntSilent(boundingVolumeTypeDropDown[idx].getController().getValue().toString());
		view.selectBoundingVolumeType(idx, boundingVolumeTypeId);
		switch(boundingVolumeTypeId) {
			case(0): onBoundingVolumeNoneApply(idx); break;
			case(1): onBoundingVolumeSphereApply(idx); break;
			case(2): onBoundingVolumeCapsuleApply(idx); break;
			case(3): onBoundingVolumeAabbApply(idx); break;
			case(4): onBoundingVolumeObbApply(idx); break;
			case(5): onBoundingVolumeConvexMeshApply(idx); break;
		}
	}

	/**
	 * On bounding volume none apply
	 * @param idx
	 */
	public void onBoundingVolumeNoneApply(int idx) {
		view.applyBoundingVolumeNone(idx);
		view.resetBoundingVolume(idx);
	}

	/**
	 * On bounding volume sphere apply
	 * @param idx
	 */
	public void onBoundingVolumeSphereApply(int idx) {
		try {
			view.applyBoundingVolumeSphere(
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
	 * @param idx
	 */
	public void onBoundingVolumeCapsuleApply(int idx) {
		try {
			view.applyBoundingVolumeCapsule(
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
	 * @param idx
	 */
	public void onBoundingVolumeAabbApply(int idx) {
		try {
			view.applyBoundingVolumeAabb(
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
	 * @param idx
	 */
	public void onBoundingVolumeObbApply(int idx) {
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
	 * @param idx
	 */
	public void onBoundingVolumeConvexMeshApply(int idx) {
		view.applyBoundingVolumeConvexMesh(
			idx,
			boundingvolumeConvexMeshFile[idx].getController().getValue().toString()
		);
	}

	/**
	 * On bounding volume convex mesh file clicked
	 * @param idx
	 */
	public void onBoundingVolumeConvexMeshFile(int idx) {
		final int idxFinal = idx;
		view.getPopUpsViews().getFileDialogScreenController().show(
			modelPath,
			"Load from: ", 
			new String[]{"dae", "tm"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					boundingvolumeConvexMeshFile[idxFinal].getController().setValue(value.set(
						view.getPopUpsViews().getFileDialogScreenController().getFileName())
					);
					onBoundingVolumeConvexMeshApply(idxFinal);
					modelPath = view.getPopUpsViews().getFileDialogScreenController().getPathName();
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
			}
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.PopUpsController#saveFile(java.lang.String, java.lang.String)
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		view.saveFile(pathName, fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.PopUpsController#loadFile(java.lang.String, java.lang.String)
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		view.loadFile(pathName, fileName);
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		view.getPopUpsViews().getInfoDialogScreenController().show(caption, message);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		// delegate to model base screen controller
		entityBaseSubScreenController.onValueChanged(node, view.getEntity());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// delegate to model base sub screen controller
		entityBaseSubScreenController.onActionPerformed(type, node, view.getEntity());
		// delegate to display sub screen controller
		entityDisplaySubScreenController.onActionPerformed(type, node);
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_model_load")) {
						onModelLoad();
					} else
					if (node.getId().equals("button_model_reload")) {
						onModelReload();
					} else
					if (node.getId().equals("button_model_save")) {
						onModelSave();
					} else
					if (node.getId().equals("button_pivot_apply")) {
						onPivotApply();
					} else
					if (node.getId().startsWith("button_boundingvolume_apply_")) {
						onBoundingVolumeTypeApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_sphere_apply_")) {
						onBoundingVolumeSphereApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_capsule_apply_")) {
						onBoundingVolumeCapsuleApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_obb_apply_")) {
						onBoundingVolumeObbApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_aabb_apply_")) {
						onBoundingVolumeAabbApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} else
					if (node.getId().startsWith("button_boundingvolume_convexmesh_apply_")) {
						onBoundingVolumeConvexMeshApply(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
					} 
					if (node.getId().startsWith("button_boundingvolume_convexmesh_file_")) {
						onBoundingVolumeConvexMeshFile(Tools.convertToIntSilent(node.getId().substring(node.getId().lastIndexOf('_') + 1)));
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
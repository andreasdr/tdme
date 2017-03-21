package net.drewke.tdme.tools.shared.controller;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
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

	private final static MutableString CHECKBOX_CHECKED = new MutableString("1");
	private final static MutableString CHECKBOX_UNCHECKED = new MutableString("");
	private final static MutableString TEXT_EMPTY = new MutableString("");

	private ModelBaseScreenController modelBaseScreenController;

	private ModelViewerView view;
	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode displayBoundingVolume;
	private GUIElementNode displayShadowing;
	private GUIElementNode displayGround;
	private GUIElementNode pivotX;
	private GUIElementNode pivotY;
	private GUIElementNode pivotZ;
	private GUIElementNode pivotApply;
	private GUIElementNode boundingVolumeTypeDropDown;
	private GUIElementNode statsOpaqueFaces;
	private GUIElementNode statsTransparentFaces;
	private GUIElementNode statsMaterialCount;
	private GUIElementNode boundingVolumeNoneApply;
	private GUIElementNode boundingVolume;
	private GUIElementNode boundingvolumeSphereCenter;
	private GUIElementNode boundingvolumeSphereRadius;
	private GUIElementNode boundingvolumeCapsuleA;
	private GUIElementNode boundingvolumeCapsuleB;
	private GUIElementNode boundingvolumeCapsuleRadius;
	private GUIElementNode boundingvolumeBoundingBoxMin;
	private GUIElementNode boundingvolumeBoundingBoxMax;
	private GUIElementNode boundingvolumeObbCenter;
	private GUIElementNode boundingvolumeObbHalfextension;
	private GUIElementNode boundingvolumeObbAxis0;
	private GUIElementNode boundingvolumeObbAxis1;
	private GUIElementNode boundingvolumeObbAxis2;
	private GUIElementNode boundingvolumeConvexMeshFile;

	private MutableString value;

	/**
	 * Public constructor
	 * @param view
	 */
	public ModelViewerScreenController(ModelViewerView view) {
		this.view = view;
		this.modelBaseScreenController = new ModelBaseScreenController(view.getPopUpsViews(), new Action() {
			public void performAction() {
				view.updateGUIElements();
				view.onSetModelData();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/shared/gui", "screen_modelviewer.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			displayBoundingVolume = (GUIElementNode)screenNode.getNodeById("display_boundingvolume");
			displayShadowing = (GUIElementNode)screenNode.getNodeById("display_shadowing");
			displayGround = (GUIElementNode)screenNode.getNodeById("display_ground");
			pivotX = (GUIElementNode)screenNode.getNodeById("pivot_x");
			pivotY = (GUIElementNode)screenNode.getNodeById("pivot_y");
			pivotZ = (GUIElementNode)screenNode.getNodeById("pivot_z");
			pivotApply = (GUIElementNode)screenNode.getNodeById("button_pivot_apply");
			boundingVolumeTypeDropDown = (GUIElementNode)screenNode.getNodeById("boundingvolume_type");
			boundingVolumeNoneApply = (GUIElementNode)screenNode.getNodeById("button_boundingvolume_apply");
			boundingVolume = (GUIElementNode)screenNode.getNodeById("boundingvolume");
			boundingvolumeSphereCenter = (GUIElementNode)screenNode.getNodeById("boundingvolume_sphere_center");
			boundingvolumeSphereRadius = (GUIElementNode)screenNode.getNodeById("boundingvolume_sphere_radius");
			boundingvolumeCapsuleA = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_a");
			boundingvolumeCapsuleB = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_b");
			boundingvolumeCapsuleRadius = (GUIElementNode)screenNode.getNodeById("boundingvolume_capsule_radius");
			boundingvolumeBoundingBoxMin = (GUIElementNode)screenNode.getNodeById("boundingvolume_aabb_min");
			boundingvolumeBoundingBoxMax = (GUIElementNode)screenNode.getNodeById("boundingvolume_aabb_max");
			boundingvolumeObbCenter = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_center");
			boundingvolumeObbCenter = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_center");
			boundingvolumeObbHalfextension = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_halfextension");
			boundingvolumeObbAxis0 = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_axis0");
			boundingvolumeObbAxis1 = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_axis1");
			boundingvolumeObbAxis2 = (GUIElementNode)screenNode.getNodeById("boundingvolume_obb_axis2");
			boundingvolumeConvexMeshFile = (GUIElementNode)screenNode.getNodeById("boundingvolume_convexmesh_file");
			statsOpaqueFaces = (GUIElementNode)screenNode.getNodeById("stats_opaque_faces");
			statsTransparentFaces = (GUIElementNode)screenNode.getNodeById("stats_transparent_faces");
			statsMaterialCount = (GUIElementNode)screenNode.getNodeById("stats_material_count");
			statsOpaqueFaces.getController().setDisabled(true);
			statsTransparentFaces.getController().setDisabled(true);
			statsMaterialCount.getController().setDisabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init model base view
		modelBaseScreenController.init(screenNode);

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
	 * Set up display section
	 */
	public void setupDisplay() {
		displayShadowing.getController().setValue(view.isDisplayShadowing() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayGround.getController().setValue(view.isDisplayGroundPlate() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayBoundingVolume.getController().setValue(view.isDisplayBoundingVolume() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
	}

	/**
	 * On display apply button event
	 */
	public void onDisplayApply() {
		view.setDisplayShadowing(displayShadowing.getController().getValue().equals(CHECKBOX_CHECKED));
		view.setDisplayGroundPlate(displayGround.getController().getValue().equals(CHECKBOX_CHECKED));
		view.setDisplayBoundingVolume(displayBoundingVolume.getController().getValue().equals(CHECKBOX_CHECKED));
	}

	/**
	 * Set screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getText().set(text);
		// TODO: fix me to only layout relevant nodes
		screenNode.layout();
	}

	/**
	  * Set up general model data
	  * @param name
	  * @param description
	  */
	public void setModelData(String name, String description) {
		modelBaseScreenController.setModelData(name, description);
	}

	/**
	 * Unset model data
	 */
	public void unsetModelData() {
		modelBaseScreenController.unsetModelData();
	}

	/**
	 * Set up model properties
	 * @param preset id
	 * @param model properties
	 * @param selected name
	 */
	public void setModelProperties(String presetId, Iterable<PropertyModelClass> modelProperties, String selectedName) {
		modelBaseScreenController.setModelProperties(view.getModel(), presetId, modelProperties, selectedName);
	}

	/**
 	 * Unset model properties
	 */
	public void unsetModelProperties() {
		modelBaseScreenController.unsetModelProperties();
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
	 * @return display shadowing checked
	 */
	public boolean getDisplayShadowing() {
		return displayShadowing.getController().getValue().equals(CHECKBOX_CHECKED);
	}

	/**
	 * @return display ground checked
	 */
	public boolean getDisplayGround() {
		return displayGround.getController().getValue().equals(CHECKBOX_CHECKED);
	}

	/**
	 * @return display bounding volume checked
	 */
	public boolean getDisplayBoundingVolume() {
		return displayBoundingVolume.getController().getValue().equals(CHECKBOX_CHECKED);
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
			"Load from: ", 
			new String[]{"tmm", "dae", "tm"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.loadFile(
						view.getPopUpsViews().getFileDialogScreenController().getPathName(),
						view.getPopUpsViews().getFileDialogScreenController().getFileName()
					);
					view.getPopUpsViews().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On model save
	 */
	public void onModelSave() {
		String fileName = view.getFileName();
		if (fileName.toLowerCase().endsWith(".tmm") == false) {
			fileName+= ".tmm";
		}
		view.getPopUpsViews().getFileDialogScreenController().show(
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
	 * Unset bounding volume
	 */
	public void unsetBoundingVolume() {
		view.selectBoundingVolumeType(0);
		boundingVolumeTypeDropDown.getController().setDisabled(true);
		boundingVolumeNoneApply.getController().setDisabled(true);
	}

	/**
	 * Set up bounding volume
	 */
	public void setBoundingVolume() {
		boundingVolumeTypeDropDown.getController().setDisabled(false);
		boundingVolumeNoneApply.getController().setDisabled(false);
	}

	/**
	 * Set up model bounding volume
	 */
	public void setupModelBoundingVolume() {
		LevelEditorModel model = view.getModel();
		if (model == null) {
			view.selectBoundingVolumeType(0);
			return;
		}
		BoundingVolume bv = model.getBoundingVolume();
		if (bv == null) {
			view.selectBoundingVolumeType(0);
		} else
		if (bv instanceof Sphere) {
			view.selectBoundingVolumeType(1);
		} else
		if (bv instanceof Capsule) {
			view.selectBoundingVolumeType(2);
		} else
		if (bv instanceof BoundingBox) {
			view.selectBoundingVolumeType(3);
		} else
		if (bv instanceof OrientedBoundingBox) {
			view.selectBoundingVolumeType(4);
		} else
		if (bv instanceof ConvexMesh) {
			view.selectBoundingVolumeType(5);
		} else {
			System.out.println("ModelViewerScreenController::onTabSelected(): invalid bounding volume: " + bv);
		}
	}

	/**
	 * Set up bounding volume types
	 * @param bounding volume types
	 */
	public void setupBoundingVolumeTypes(String[] boundingVolumeTypes) {
		// bounding volume types drop downs inner
		GUIParentNode boundingVolumeTypeDropDownInnerNode = (GUIParentNode)(boundingVolumeTypeDropDown.getScreenNode().getNodeById(boundingVolumeTypeDropDown.getId() + "_inner"));

		// clear sub nodes
		boundingVolumeTypeDropDownInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 0;
		String boundingVolumeTypeDropDownSubNodesXML = "";
		for (String bvType: boundingVolumeTypes) {
			boundingVolumeTypeDropDownSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(bvType) + "\" value=\"" +  + (idx++) + "\" />\n";
		}

		// inject sub nodes
		try {
			GUIParser.parse(
				boundingVolumeTypeDropDownInnerNode,
				boundingVolumeTypeDropDownSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		boundingVolumeTypeDropDownInnerNode.getScreenNode().layout();
	}

	/**
	 * Display given bounding volume GUI elements
	 * @param bvType
	 */
	public void selectBoundingVolume(BoundingVolumeType bvType) {
		boundingVolume.getActiveConditions().remove("sphere");
		boundingVolume.getActiveConditions().remove("capsule");
		boundingVolume.getActiveConditions().remove("aabb");
		boundingVolume.getActiveConditions().remove("obb");
		boundingVolume.getActiveConditions().remove("convexmesh");
		switch (bvType) {
			case NONE:
				boundingVolumeTypeDropDown.getController().setValue(value.set("0"));
				break;
			case SPHERE:
				boundingVolumeTypeDropDown.getController().setValue(value.set("1"));
				boundingVolume.getActiveConditions().add("sphere");
				break;
			case CAPSULE:
				boundingVolumeTypeDropDown.getController().setValue(value.set("2"));
				boundingVolume.getActiveConditions().add("capsule");
				break;
			case BOUNDINGBOX: 
				boundingVolumeTypeDropDown.getController().setValue(value.set("3"));
				boundingVolume.getActiveConditions().add("aabb");
				break;
			case ORIENTEDBOUNDINGBOX: 
				boundingVolumeTypeDropDown.getController().setValue(value.set("4"));
				boundingVolume.getActiveConditions().add("obb");
				break;
			case CONVEXMESH:
				boundingVolumeTypeDropDown.getController().setValue(value.set("5"));
				boundingVolume.getActiveConditions().add("convexmesh");
				break;
		}
	}

	/**
	 * Setup sphere bounding volume
	 * @param center
	 * @param radius
	 */
	public void setupSphere(Vector3 center, float radius) {
		selectBoundingVolume(BoundingVolumeType.SPHERE);
		boundingvolumeSphereCenter.getController().setValue(
			value.reset().
			append(Tools.formatFloat(center.getX())).
			append(", ").
			append(Tools.formatFloat(center.getY())).
			append(", ").
			append(Tools.formatFloat(center.getZ()))
		);
		boundingvolumeSphereRadius.getController().setValue(
			value.set(Tools.formatFloat(radius))
		);
	}

	/**
	 * Setup capsule bounding volume
	 * @param center
	 * @param radius
	 */
	public void setupCapsule(Vector3 a, Vector3 b, float radius) {
		selectBoundingVolume(BoundingVolumeType.CAPSULE);
		boundingvolumeCapsuleA.getController().setValue(
			value.reset().
			append(Tools.formatFloat(a.getX())).
			append(", ").
			append(Tools.formatFloat(a.getY())).
			append(", ").
			append(Tools.formatFloat(a.getZ()))
		);
		boundingvolumeCapsuleB.getController().setValue(
			value.reset().
			append(Tools.formatFloat(b.getX())).
			append(", ").
			append(Tools.formatFloat(b.getY())).
			append(", ").
			append(Tools.formatFloat(b.getZ()))
		);
		boundingvolumeCapsuleRadius.getController().setValue(
			value.set(Tools.formatFloat(radius))
		);
	}

	/**
	 * Setup AABB bounding volume
	 * @param min
	 * @param max
	 */
	public void setupBoundingBox(Vector3 min, Vector3 max) {
		selectBoundingVolume(BoundingVolumeType.BOUNDINGBOX);
		boundingvolumeBoundingBoxMin.getController().setValue(
			value.reset().
			append(Tools.formatFloat(min.getX())).
			append(", ").
			append(Tools.formatFloat(min.getY())).
			append(", ").
			append(Tools.formatFloat(min.getZ()))
		);
		boundingvolumeBoundingBoxMax.getController().setValue(
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
	 * @param center
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param half extension
	 */
	public void setupOrientedBoundingBox(Vector3 center, Vector3 axis0, Vector3 axis1, Vector3 axis2, Vector3 halfExtension) {
		selectBoundingVolume(BoundingVolumeType.ORIENTEDBOUNDINGBOX);
		boundingvolumeObbCenter.getController().setValue(
			value.reset().
			append(Tools.formatFloat(center.getX())).
			append(", ").
			append(Tools.formatFloat(center.getY())).
			append(", ").
			append(Tools.formatFloat(center.getZ()))
		);
		boundingvolumeObbHalfextension.getController().setValue(
			value.reset().
			append(Tools.formatFloat(halfExtension.getX())).
			append(", ").
			append(Tools.formatFloat(halfExtension.getY())).
			append(", "). 
			append(Tools.formatFloat(halfExtension.getZ()))
		);
		boundingvolumeObbAxis0.getController().setValue(
			value.reset().
			append(Tools.formatFloat(axis0.getX())).
			append(", ").
			append(Tools.formatFloat(axis0.getY())).
			append(", " ).
			append(Tools.formatFloat(axis0.getZ()))
		);
		boundingvolumeObbAxis1.getController().setValue(
			value.reset().
			append(Tools.formatFloat(axis1.getX())).
			append(", ").
			append(Tools.formatFloat(axis1.getY())).
			append(", ").
			append(Tools.formatFloat(axis1.getZ()))
		);
		boundingvolumeObbAxis2.getController().setValue(
			value.reset().
			append(Tools.formatFloat(axis2.getX())).
			append(", ").
			append(Tools.formatFloat(axis2.getY())).
			append(", ").
			append(Tools.formatFloat(axis2.getZ()))
		);
	}

	/**
	 * Setup convex mesh bounding volume
	 * @param file
	 */
	public void setupConvexMesh(String file) {
		selectBoundingVolume(BoundingVolumeType.CONVEXMESH);
		boundingvolumeConvexMeshFile.getController().setValue(
			value.set(file)
		);
	}

	/**
	 * On pivot apply
	 */
	public void onBoundingVolumeTypeApply() {
		int boundingVolumeTypeId = Tools.convertToIntSilent(boundingVolumeTypeDropDown.getController().getValue().toString());
		view.selectBoundingVolumeType(boundingVolumeTypeId);
		switch(boundingVolumeTypeId) {
			case(0): onBoundingVolumeNoneApply(); break;
			case(1): onBoundingVolumeSphereApply(); break;
			case(2): onBoundingVolumeCapsuleApply(); break;
			case(3): onBoundingVolumeAabbApply(); break;
			case(4): onBoundingVolumeObbApply(); break;
			case(5): onBoundingVolumeConvexMeshApply(); break;
		}
	}

	/**
	 * On bounding volume sphere apply
	 */
	public void onBoundingVolumeNoneApply() {
		view.applyBoundingVolumeNone();
	}

	/**
	 * On bounding volume sphere apply
	 */
	public void onBoundingVolumeSphereApply() {
		try {
			view.applyBoundingVolumeSphere(
				Tools.convertToVector3(boundingvolumeSphereCenter.getController().getValue().toString()),
				Tools.convertToFloat(boundingvolumeSphereRadius.getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume capsule apply
	 */
	public void onBoundingVolumeCapsuleApply() {
		try {
			view.applyBoundingVolumeCapsule(
				Tools.convertToVector3(boundingvolumeCapsuleA.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeCapsuleB.getController().getValue().toString()),
				Tools.convertToFloat(boundingvolumeCapsuleRadius.getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume AABB apply
	 */
	public void onBoundingVolumeAabbApply() {
		try {
			view.applyBoundingVolumeAabb(
				Tools.convertToVector3(boundingvolumeBoundingBoxMin.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeBoundingBoxMax.getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume OBB apply
	 */
	public void onBoundingVolumeObbApply() {
		try {
			view.applyBoundingVolumeObb(
				Tools.convertToVector3(boundingvolumeObbCenter.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeObbAxis0.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeObbAxis1.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeObbAxis2.getController().getValue().toString()),
				Tools.convertToVector3(boundingvolumeObbHalfextension.getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume convex mesh apply
	 */
	public void onBoundingVolumeConvexMeshApply() {
		view.applyBoundingVolumeConvexMesh(
			boundingvolumeConvexMeshFile.getController().getValue().toString()
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
		modelBaseScreenController.onValueChanged(node, view.getModel());	
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		// delegate to model base screen controller
		modelBaseScreenController.onActionPerformed(type, node, view.getModel());
		// handle own actions
		switch (type) {
			case PERFORMED:
				{
					if (node.getId().equals("button_display_apply")) {
						onDisplayApply();
					} else
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
					if (node.getId().equals("button_boundingvolume_apply")) {
						onBoundingVolumeTypeApply();
					} else
					if (node.getId().equals("button_boundingvolume_sphere_apply")) {
						onBoundingVolumeSphereApply();
					} else
					if (node.getId().equals("button_boundingvolume_capsule_apply")) {
						onBoundingVolumeCapsuleApply();
					} else
					if (node.getId().equals("button_boundingvolume_obb_apply")) {
						onBoundingVolumeObbApply();
					} else
					if (node.getId().equals("button_boundingvolume_aabb_apply")) {
						onBoundingVolumeAabbApply();
					} else
					if (node.getId().equals("button_boundingvolume_convexmesh_apply")) {
						onBoundingVolumeConvexMeshApply();
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
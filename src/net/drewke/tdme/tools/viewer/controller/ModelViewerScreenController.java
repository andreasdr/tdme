package net.drewke.tdme.tools.viewer.controller;

import java.util.Collection;

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
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.tools.viewer.Tools;
import net.drewke.tdme.tools.viewer.model.LevelEditorModel;
import net.drewke.tdme.tools.viewer.model.PropertyModelClass;
import net.drewke.tdme.tools.viewer.views.ModelViewerView;
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

	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode displayBoundingVolume;
	private GUIElementNode displayShadowing;
	private GUIElementNode displayGround;
	private GUIElementNode modelName;
	private GUIElementNode modelDescription;
	private GUIElementNode modelApply;
	private GUIElementNode modelReload;
	private GUIElementNode modelSave;
	private GUIElementNode modelPropertyName;
	private GUIElementNode modelPropertyValue;
	private GUIElementNode modelPropertySave;
	private GUIElementNode modelPropertyAdd;
	private GUIElementNode modelPropertyRemove;
	private GUIElementNode modelPropertiesList;
	private GUIElementNode modelPropertyPresetApply;
	private GUIElementNode modelPropertiesPresets;
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

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#init(net.drewke.tdme.gui.events.GUIActionListener, net.drewke.tdme.gui.events.GUIChangeListener)
	 */
	public void init() {
		// load screen node
		try {
			screenNode = GUIParser.parse("resources/tools/viewer/gui", "screen_modelviewer.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			displayBoundingVolume = (GUIElementNode)screenNode.getNodeById("display_boundingvolume");
			displayShadowing = (GUIElementNode)screenNode.getNodeById("display_shadowing");
			displayGround = (GUIElementNode)screenNode.getNodeById("display_ground");
			modelName = (GUIElementNode)screenNode.getNodeById("model_name");
			modelDescription = (GUIElementNode)screenNode.getNodeById("model_description");
			modelApply = (GUIElementNode)screenNode.getNodeById("button_model_apply");
			modelReload = (GUIElementNode)screenNode.getNodeById("button_model_reload");
			modelSave = (GUIElementNode)screenNode.getNodeById("button_model_save");
			modelPropertyName = (GUIElementNode)screenNode.getNodeById("model_property_name");
			modelPropertyValue = (GUIElementNode)screenNode.getNodeById("model_property_value");
			modelPropertySave = (GUIElementNode)screenNode.getNodeById("button_model_properties_save");
			modelPropertyAdd = (GUIElementNode)screenNode.getNodeById("button_model_properties_add");
			modelPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_model_properties_remove");
			modelPropertiesList = (GUIElementNode)screenNode.getNodeById("model_properties_listbox");
			modelPropertyPresetApply = (GUIElementNode)screenNode.getNodeById("button_model_properties_presetapply");
			modelPropertiesPresets = (GUIElementNode)screenNode.getNodeById("model_properties_presets");
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

		//
		value = new MutableString();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.ScreenController#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Set up display section
	 */
	public void setupDisplay() {
		displayShadowing.getController().setValue(((ModelViewerView)TDMEViewer.getInstance().getView()).isDisplayShadowing() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayGround.getController().setValue(((ModelViewerView)TDMEViewer.getInstance().getView()).isDisplayGroundPlate() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		displayBoundingVolume.getController().setValue(((ModelViewerView)TDMEViewer.getInstance().getView()).isDisplayBoundingVolume() == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
	}

	/**
	 * On display apply button event
	 */
	public void onDisplayApply() {
		((ModelViewerView)TDMEViewer.getInstance().getView()).setDisplayShadowing(displayShadowing.getController().getValue().equals(CHECKBOX_CHECKED));
		((ModelViewerView)TDMEViewer.getInstance().getView()).setDisplayGroundPlate(displayGround.getController().getValue().equals(CHECKBOX_CHECKED));
		((ModelViewerView)TDMEViewer.getInstance().getView()).setDisplayBoundingVolume(displayBoundingVolume.getController().getValue().equals(CHECKBOX_CHECKED));
	}

	/**
	 * Set screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getText().set(text);
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
	 * Set up general model data
	 * @param name
	 * @param description
	 */
	public void setModelData(String name, String description) {
		modelName.getController().setDisabled(false);
		modelName.getController().getValue().set(name);
		modelDescription.getController().setDisabled(false);
		modelDescription.getController().getValue().set(description);
		modelApply.getController().setDisabled(false);
		modelReload.getController().setDisabled(false);
		modelSave.getController().setDisabled(false);
	}

	/**
	 * Unset model data
	 */
	public void unsetModelData() {
		modelName.getController().setValue(TEXT_EMPTY);
		modelName.getController().setDisabled(true);
		modelDescription.getController().setValue(TEXT_EMPTY);
		modelDescription.getController().setDisabled(true);
		modelApply.getController().setDisabled(true);
		modelReload.getController().setDisabled(true);
		modelSave.getController().setDisabled(true);
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
	 * Set up model property preset ids
	 * @param model property preset ids
	 */
	public void setModelPresetIds(Collection<String> modelPresetIds) {
		// model properties presets inner
		GUIParentNode modelPropertiesPresetsInnerNode = (GUIParentNode)(modelPropertiesPresets.getScreenNode().getNodeById(modelPropertiesPresets.getId() + "_inner"));

		// clear sub nodes
		modelPropertiesPresetsInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 0;
		String modelPropertiesPresetsInnerNodeSubNodesXML = "";
		for (String modelPresetId: modelPresetIds) {
			modelPropertiesPresetsInnerNodeSubNodesXML+= "<dropdown-option text=\"" + modelPresetId + "\" value=\"" + modelPresetId + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}

		// inject sub nodes
		try {
			GUIParser.parse(
				modelPropertiesPresetsInnerNode,
				modelPropertiesPresetsInnerNodeSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		modelPropertiesPresetsInnerNode.getScreenNode().layout();
	}

	/**
	 * Set up model properties
	 * @param has properties
	 * @param preset id
	 * @param model properties
	 */
	public void setModelProperties(String presetId, Iterable<PropertyModelClass> modelProperties, String selectedValue) {
		//
		modelPropertiesPresets.getController().setDisabled(false);
		modelPropertyPresetApply.getController().setDisabled(false);
		modelPropertiesList.getController().setDisabled(false);
		modelPropertyAdd.getController().setDisabled(false);
		modelPropertyRemove.getController().setDisabled(false);
		modelPropertySave.getController().setDisabled(true);
		modelPropertyName.getController().setDisabled(true);
		modelPropertyValue.getController().setDisabled(true);

		// set up preset
		if (presetId != null) {
			modelPropertiesPresets.getController().setValue(value.set(presetId));
		}

		// model properties list box inner
		GUIParentNode modelPropertiesListBoxInnerNode = (GUIParentNode)(modelPropertiesList.getScreenNode().getNodeById(modelPropertiesList.getId() + "_inner"));
		
		// clear sub nodes
		modelPropertiesListBoxInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 1;
		String modelPropertiesListBoxSubNodesXML = "";
		modelPropertiesListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + modelPropertiesList.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (PropertyModelClass modelProperty: modelProperties) {
			modelPropertiesListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				modelProperty.getName() + 
				": " + 
				modelProperty.getValue() + 
				"\" value=\"" + 
				modelProperty.getName() + 
				"\" " +
				(selectedValue != null && modelProperty.getName().equals(selectedValue)?"selected=\"true\" ":"") +
				"/>\n";
		}
		modelPropertiesListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			GUIParser.parse(
				modelPropertiesListBoxInnerNode,
				modelPropertiesListBoxSubNodesXML
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// relayout
		modelPropertiesListBoxInnerNode.getScreenNode().layout();

		//
		onModelPropertiesSelectionChanged();
	}

	/**
	 * Unset model properties
	 */
	public void unsetModelProperties() {
		GUIParentNode modelPropertiesListBoxInnerNode = (GUIParentNode)(modelPropertiesList.getScreenNode().getNodeById(modelPropertiesList.getId() + "_inner"));
		modelPropertiesListBoxInnerNode.clearSubNodes();
		modelPropertiesPresets.getController().setValue(value.set("none"));
		modelPropertiesPresets.getController().setDisabled(true);
		modelPropertyPresetApply.getController().setDisabled(true);
		modelPropertiesList.getController().setDisabled(true);
		modelPropertyAdd.getController().setDisabled(true);
		modelPropertyRemove.getController().setDisabled(true);
		modelPropertySave.getController().setDisabled(true);
		modelPropertyName.getController().setValue(TEXT_EMPTY);
		modelPropertyName.getController().setDisabled(true);
		modelPropertyValue.getController().setValue(TEXT_EMPTY);
		modelPropertyValue.getController().setDisabled(true);
	}

	/**
	 * On model property save
	 */
	public void onModelPropertySave() {
		ModelViewerView modelViewerView = ((ModelViewerView)TDMEViewer.getInstance().getView());
		if (modelViewerView.modelPropertySave(
			modelPropertiesList.getController().getValue().toString(),
			modelPropertyName.getController().getValue().toString(),
			modelPropertyValue.getController().getValue().toString()) == false) {
			//
			showErrorPopUp("Warning", "Saving model property failed");
		}
	}

	/**
	 * On model property add
	 */
	public void onModelPropertyAdd() {
		if (((ModelViewerView)TDMEViewer.getInstance().getView()).modelPropertyAdd() == false) {
			showErrorPopUp("Warning", "Adding new model property failed");
		}
	}

	/**
	 * On model property remove
	 */
	public void onModelPropertyRemove() {
		if (((ModelViewerView)TDMEViewer.getInstance().getView()).modelPropertyRemove(modelPropertiesList.getController().getValue().toString()) == false) {
			showErrorPopUp("Warning", "Removing model property failed");
		}
	}

	/**
	 * On model property preset apply 
	 */
	public void onModelPropertyPresetApply() {
		((ModelViewerView)TDMEViewer.getInstance().getView()).modelPropertiesPreset(modelPropertiesPresets.getController().getValue().toString());
	}

	/**
	 * Event callback for model properties selection
	 */
	public void onModelPropertiesSelectionChanged() {
		modelPropertyName.getController().setDisabled(true);
		modelPropertyName.getController().setValue(TEXT_EMPTY);
		modelPropertyValue.getController().setDisabled(true);
		modelPropertyValue.getController().setValue(TEXT_EMPTY);
		modelPropertySave.getController().setDisabled(true);
		PropertyModelClass modelProperty = ((ModelViewerView)TDMEViewer.getInstance().getView()).getSelectedModel().getProperty(modelPropertiesList.getController().getValue().toString());
		if (modelProperty != null) {
			modelPropertyName.getController().setValue(value.set(modelProperty.getName()));
			modelPropertyValue.getController().setValue(value.set(modelProperty.getValue()));
			modelPropertyName.getController().setDisabled(false);
			modelPropertyValue.getController().setDisabled(false);
			modelPropertySave.getController().setDisabled(false);
		}
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
		((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().show(
			"Load from: ", 
			new String[]{"tmm", "dae", "tm"},
			((ModelViewerView)TDMEViewer.getInstance().getView()).getFileName(),
			new Action() {
				public void performAction() {
					((ModelViewerView)TDMEViewer.getInstance().getView()).loadFile(
						((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().getPathName(), 
						((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().getFileName()
					);
					((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().close();
				}
				
			}
		);
	}

	/**
	 * On model save
	 */
	public void onModelSave() {
		String fileName = ((ModelViewerView)TDMEViewer.getInstance().getView()).getFileName();
		if (fileName.toLowerCase().endsWith(".tmm") == false) {
			fileName+= ".tmm";
		}
		((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().show(
			"Save from: ", 
			new String[]{"tmm"},
			fileName,
			new Action() {
				public void performAction() {
					try {
						((ModelViewerView)TDMEViewer.getInstance().getView()).saveFile(
							((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().getPathName(), 
							((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().getFileName()
						);
						((ModelViewerView)TDMEViewer.getInstance().getView()).getFileDialogPopUpController().close();
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
		((ModelViewerView)TDMEViewer.getInstance().getView()).reloadFile();
	}

	/**
	 * On model data apply
	 */
	public void onModelDataApply() {
		((ModelViewerView)TDMEViewer.getInstance().getView()).setModelData(
			modelName.getController().getValue().toString(), 
			modelDescription.getController().getValue().toString()
		);

		// rename in library
		/*
		LevelEditorModel model = ((ModelViewerView)TDMEViewer.getInstance().getView()).getSelectedModel();
		if (model == null) return;
		Element modelNameElement = screen.findElementByName("modelchoser_name_" + model.getId());
		TextRenderer modelNameElementRenderer = modelNameElement.getRenderer(TextRenderer.class);
		modelNameElementRenderer.setText(model.getName());
		*/
	}

	/**
	 * On pivot apply
	 */
	public void onPivotApply() {
		try {
			float x = Float.parseFloat(pivotX.getController().getValue().toString());
			float y = Float.parseFloat(pivotY.getController().getValue().toString());
			float z = Float.parseFloat(pivotZ.getController().getValue().toString());
			((ModelViewerView)TDMEViewer.getInstance().getView()).pivotApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * Unset bounding volume
	 */
	public void unsetBoundingVolume() {
		((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
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
		LevelEditorModel model = ((ModelViewerView)TDMEViewer.getInstance().getView()).getSelectedModel();
		if (model == null) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
			return;
		}
		BoundingVolume bv = model.getBoundingVolume();
		if (bv == null) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
		} else
		if (bv instanceof Sphere) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(1);
		} else
		if (bv instanceof Capsule) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(2);
		} else
		if (bv instanceof BoundingBox) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(3);
		} else
		if (bv instanceof OrientedBoundingBox) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(4);
		} else
		if (bv instanceof ConvexMesh) {
			((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(5);
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
		GUIParentNode boundingVolumeTypeDropDownInnerNode = (GUIParentNode)(modelPropertiesList.getScreenNode().getNodeById(boundingVolumeTypeDropDown.getId() + "_inner"));

		// clear sub nodes
		boundingVolumeTypeDropDownInnerNode.clearSubNodes();

		// construct XML for sub nodes
		int idx = 0;
		String boundingVolumeTypeDropDownSubNodesXML = "";
		for (String bvType: boundingVolumeTypes) {
			boundingVolumeTypeDropDownSubNodesXML+= "<dropdown-option text=\"" + bvType + "\" value=\"" +  + (idx++) + "\" />\n";
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
		((ModelViewerView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(boundingVolumeTypeId);
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
		((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeNone();
	}

	/**
	 * On bounding volume sphere apply
	 */
	public void onBoundingVolumeSphereApply() {
		try {
			((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeSphere(
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
			((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeCapsule(
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
			((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeAabb(
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
			((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeObb(
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
		((ModelViewerView)TDMEViewer.getInstance().getView()).applyBoundingVolumeConvexMesh(
			boundingvolumeConvexMeshFile.getController().getValue().toString()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.PopUpsController#saveFile(java.lang.String, java.lang.String)
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		((ModelViewerView)TDMEViewer.getInstance().getView()).saveFile(pathName, fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.PopUpsController#loadFile(java.lang.String, java.lang.String)
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		((ModelViewerView)TDMEViewer.getInstance().getView()).loadFile(pathName, fileName);
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		((ModelViewerView)TDMEViewer.getInstance().getView()).getInfoDialogPopUpController().show(caption, message);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		if (node == modelPropertiesList) {
			onModelPropertiesSelectionChanged();
		} else {
			// System.out.println("ModelViewerScreenController::onValueChanged(): id = '" + node.getId() + "'" + ", name = '" + node.getName() + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
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
					if (node.getId().equals("button_model_apply")) {
						onModelDataApply();
					} else
					if (node.getId().equals("button_model_properties_presetapply")) {
						onModelPropertyPresetApply();
					} else
					if (node.getId().equals("button_model_properties_add")) {
						onModelPropertyAdd();
					} else
					if (node.getId().equals("button_model_properties_remove")) {
						onModelPropertyRemove();
					} else
					if (node.getId().equals("button_model_properties_save")) {
						onModelPropertySave();
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
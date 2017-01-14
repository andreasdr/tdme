package net.drewke.tdme.tools.viewer.controller;

import java.io.File;
import java.util.Collection;

import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.BoundingVolume;
import net.drewke.tdme.engine.primitives.Capsule;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.OrientedBoundingBox;
import net.drewke.tdme.engine.primitives.Sphere;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.viewer.TDMEViewer;
import net.drewke.tdme.tools.viewer.Tools;
import net.drewke.tdme.tools.viewer.model.LevelEditorModel;
import net.drewke.tdme.tools.viewer.model.PropertyModelClass;
import net.drewke.tdme.tools.viewer.views.ModelLibraryView;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TabSelectedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;

/**
 * Main Screen Controller
 * @author Andreas Drewke
 * @version $Id: ModelLibraryController.java 82 2013-12-26 13:56:48Z drewke.net $
 */
public final class ModelLibraryController extends PopUpsController {

	public enum BoundingVolumeType {NONE, SPHERE, CAPSULE, BOUNDINGBOX, ORIENTEDBOUNDINGBOX, CONVEXMESH};

	private Nifty nifty;
	private Screen screen;
	private Element screenCaption;
	private Element display;
	private CheckBox displayBoundingVolume;
	private CheckBox displayShadowing;
	private CheckBox displayGround;
	private TextField modelName;
	private TextField modelDescription;
	private Button modelDataApply;
	private Button modelReload;
	private Button modelSave;
	private TextField objectPropertyName;
	private TextField objectPropertyValue;
	private Button objectPropertySave;
	private Button objectPropertyAdd;
	private Button objectPropertyRemove;
	private Button objectPropertyPresetApply;
	private ListBox<PropertyModelClass> objectPropertiesListBox;
	private DropDown<String> objectPropertiesPresets;
	private TextField pivotX;
	private TextField pivotY;
	private TextField pivotZ;
	private Button btnPivotApply;
	private DropDown<String> boundingVolumeTypeDropDown;
	private TextField statsOpaqueFaces;
	private TextField statsTransparentFaces;
	private TextField statsMaterialCount;
	private Element boundingVolumePanel;
	private Element boundingVolumeNoneElement;
	private Element boundingVolumeOrientedBoundingBoxElement;
	private Element boundingVolumeSphereElement;
	private Element boundingVolumeCapsuleElement;
	private Element boundingVolumeBoundingBoxElement;
	private Element boundingVolumeConvexMeshElement;
	private Button boundingVolumeNoneApply;
	private TextField boundingvolumeSphereCenter;
	private TextField boundingvolumeSphereRadius;
	private TextField boundingvolumeCapsuleA;
	private TextField boundingvolumeCapsuleB;
	private TextField boundingvolumeCapsuleRadius;
	private TextField boundingvolumeBoundingBoxMin;
	private TextField boundingvolumeBoundingBoxMax;
	private TextField boundingvolumeObbCenter;
	private TextField boundingvolumeObbHalfextension;
	private TextField boundingvolumeObbAxis0;
	private TextField boundingvolumeObbAxis1;
	private TextField boundingvolumeObbAxis2;
	private TextField boundingvolumeConvexMeshFile;

	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen, new File("."), "tmm,dae,tm");
		this.nifty = nifty;
		this.screen = screen;
		screenCaption = screen.findElementByName("screen_caption");
		display = screen.findElementByName("panel_properties_display");
		displayBoundingVolume = screen.findNiftyControl("display_boundingvolume", CheckBox.class);
		displayShadowing = screen.findNiftyControl("display_shadowing", CheckBox.class);
		displayGround = screen.findNiftyControl("display_ground", CheckBox.class);
		modelName = screen.findNiftyControl("model_name", TextField.class);
		modelDescription = screen.findNiftyControl("model_description", TextField.class);
		modelDataApply = screen.findNiftyControl("button_modeldata_apply", Button.class);
		modelReload = screen.findNiftyControl("button_model_reload", Button.class);
		modelSave = screen.findNiftyControl("button_model_save", Button.class);
		objectPropertyName = screen.findNiftyControl("object_property_name", TextField.class);
		objectPropertyValue = screen.findNiftyControl("object_property_value", TextField.class);
		objectPropertySave = screen.findNiftyControl("button_object_properties_save", Button.class);
		objectPropertyAdd = screen.findNiftyControl("button_object_properties_add", Button.class);
		objectPropertyRemove = screen.findNiftyControl("button_object_properties_remove", Button.class);
		objectPropertyPresetApply = screen.findNiftyControl("button_object_properties_presetapply", Button.class);
		objectPropertiesListBox = (ListBox<PropertyModelClass>) screen.findNiftyControl("objectproperties_listbox", ListBox.class);
		objectPropertiesPresets = (DropDown<String>) screen.findNiftyControl("objectproperties_presets", DropDown.class);
		pivotX = screen.findNiftyControl("pivot_x", TextField.class);
		pivotY = screen.findNiftyControl("pivot_y", TextField.class);
		pivotZ = screen.findNiftyControl("pivot_z", TextField.class);
		objectPropertiesListBox = (ListBox<PropertyModelClass>) screen.findNiftyControl("objectproperties_listbox", ListBox.class);
		btnPivotApply = screen.findNiftyControl("button_pivot_apply", Button.class);
		boundingVolumeTypeDropDown = (DropDown<String>)screen.findNiftyControl("boundingvolume_type", DropDown.class);
		boundingVolumeNoneElement = screen.findElementByName("boundingvolume_none");
		boundingVolumeSphereElement = screen.findElementByName("boundingvolume_sphere");
		boundingVolumeCapsuleElement = screen.findElementByName("boundingvolume_capsule");
		boundingVolumeOrientedBoundingBoxElement = screen.findElementByName("boundingvolume_obb");
		boundingVolumeBoundingBoxElement = screen.findElementByName("boundingvolume_aabb");
		boundingVolumeConvexMeshElement = screen.findElementByName("boundingvolume_convexmesh");
		boundingVolumeNoneApply = screen.findNiftyControl("button_boundingvolume_apply", Button.class);
		boundingVolumePanel = screen.findElementByName("panel_boundingvolume_tab");
		boundingvolumeSphereCenter = screen.findNiftyControl("boundingvolume_sphere_center", TextField.class);
		boundingvolumeSphereRadius = screen.findNiftyControl("boundingvolume_sphere_radius", TextField.class);
		boundingvolumeCapsuleA = screen.findNiftyControl("boundingvolume_capsule_a", TextField.class);
		boundingvolumeCapsuleB = screen.findNiftyControl("boundingvolume_capsule_b", TextField.class);
		boundingvolumeCapsuleRadius = screen.findNiftyControl("boundingvolume_capsule_radius", TextField.class);
		boundingvolumeBoundingBoxMin = screen.findNiftyControl("boundingvolume_aabb_min", TextField.class);
		boundingvolumeBoundingBoxMax = screen.findNiftyControl("boundingvolume_aabb_max", TextField.class);
		boundingvolumeObbCenter = screen.findNiftyControl("boundingvolume_obb_center", TextField.class);
		boundingvolumeObbCenter = screen.findNiftyControl("boundingvolume_obb_center", TextField.class);
		boundingvolumeObbHalfextension = screen.findNiftyControl("boundingvolume_obb_halfextension", TextField.class);
		boundingvolumeObbAxis0 = screen.findNiftyControl("boundingvolume_obb_axis0", TextField.class);
		boundingvolumeObbAxis1 = screen.findNiftyControl("boundingvolume_obb_axis1", TextField.class);
		boundingvolumeObbAxis2 = screen.findNiftyControl("boundingvolume_obb_axis2", TextField.class);
		boundingvolumeConvexMeshFile = screen.findNiftyControl("boundingvolume_convexmesh_file", TextField.class);
		statsOpaqueFaces = screen.findNiftyControl("stats_opaque_faces", TextField.class);
		statsTransparentFaces = screen.findNiftyControl("stats_transparent_faces", TextField.class);
		statsMaterialCount = screen.findNiftyControl("stats_material_count", TextField.class);
		statsOpaqueFaces.disable();
		statsTransparentFaces.disable();
		statsMaterialCount.disable();
	}

	public void onEndScreen() {
	}

	public void onStartScreen() {
		setupDisplay();
	}

	public void setupDisplay() {
		displayShadowing.setChecked(((ModelLibraryView)TDMEViewer.getInstance().getView()).isDisplayShadowing());
		displayGround.setChecked(((ModelLibraryView)TDMEViewer.getInstance().getView()).isDisplayGroundPlate());
		displayBoundingVolume.setChecked(((ModelLibraryView)TDMEViewer.getInstance().getView()).isDisplayBoundingVolume());
	}

	/**
	 * On display apply button event
	 */
	public void onDisplayApply() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).setDisplayShadowing(displayShadowing.isChecked());
		((ModelLibraryView)TDMEViewer.getInstance().getView()).setDisplayGroundPlate(displayGround.isChecked());
		((ModelLibraryView)TDMEViewer.getInstance().getView()).setDisplayBoundingVolume(displayBoundingVolume.isChecked());
	}

	public void setScreenCaption(String text) {
		screenCaption.getRenderer(TextRenderer.class).setText(text);
		screen.layoutLayers();
	}

	/**
	 * Set pivot tab
	 * @param pivot
	 */
	public void setPivot(Vector3 pivot) {
		pivotX.enable();
		pivotX.setText(Tools.formatFloat(pivot.getX()));
		pivotY.enable();
		pivotY.setText(Tools.formatFloat(pivot.getY()));
		pivotZ.enable();
		pivotZ.setText(Tools.formatFloat(pivot.getZ()));
		btnPivotApply.enable();
	}

	/**
	 * Unset pivot tab
	 */
	public void unsetPivot() {
		pivotX.setText("");
		pivotX.disable();
		pivotY.setText("");
		pivotY.disable();
		pivotZ.setText("");
		pivotZ.disable();
		btnPivotApply.disable();
	}

	/**
	 * Set up general model data
	 * @param name
	 * @param description
	 */
	public void setModelData(String name, String description) {
		modelName.enable();
		modelName.setText(name);
		modelDescription.enable();
		modelDescription.setText(description);
		modelDataApply.enable();
		modelReload.enable();
		modelSave.enable();
	}

	/**
	 * Unset model data
	 */
	public void unsetModelData() {
		modelName.setText("");
		modelName.disable();
		modelDescription.setText("");
		modelDescription.disable();
		modelDataApply.disable();
		modelReload.disable();
		modelSave.disable();
	}

	/**
	 * @return display shadowing checked
	 */
	public boolean getDisplayShadowing() {
		return displayShadowing.isChecked(); 
	}

	/**
	 * @return display ground checked
	 */
	public boolean getDisplayGround() {
		return displayGround.isChecked();
	}

	/**
	 * @return display bounding volume checked
	 */
	public boolean getDisplayBoundingVolume() {
		return displayBoundingVolume.isChecked();
	}

	/**
	 * Set up object property preset ids
	 * @param object property preset ids
	 */
	public void setObjectPresetIds(Collection<String> objectPresetIds) {
		objectPropertiesPresets.addItem("");
		for (String objectPresetId: objectPresetIds) {
			objectPropertiesPresets.addItem(objectPresetId);
		}
	}

	/**
	 * @return object property preset selection
	 */
	public String getObjectPropertyPresetSelection() {
		return objectPropertiesPresets.getSelection();
	}

	/**
	 * Set up object properties
	 * @param has properties
	 * @param preset id
	 * @param object properties
	 */
	public void setObjectProperties(String presetId, Iterable<PropertyModelClass> objectProperties) {
		objectPropertiesPresets.enable();
		objectPropertyPresetApply.enable();
		objectPropertiesListBox.enable();
		objectPropertyAdd.enable();
		objectPropertyRemove.enable();
		objectPropertySave.disable();
		objectPropertyName.disable();
		objectPropertyValue.disable();
		objectPropertiesListBox.clear();
		objectPropertiesPresets.selectItem(presetId);
		if (objectProperties != null) {
			for (PropertyModelClass objectProperty: objectProperties) {
				objectPropertiesListBox.addItem(objectProperty);
			}
		}
	}

	/**
	 * Unset object properties
	 */
	public void unsetObjectProperties() {
		objectPropertiesPresets.selectItemByIndex(0);
		objectPropertiesPresets.disable();
		objectPropertyPresetApply.disable();
		objectPropertiesListBox.disable();
		objectPropertyAdd.disable();
		objectPropertyRemove.disable();
		objectPropertySave.disable();
		objectPropertyName.setText(new String());
		objectPropertyName.disable();
		objectPropertyValue.setText(new String());
		objectPropertyValue.disable();
		objectPropertiesListBox.clear();
	}

	/**
	 * On object property save
	 */
	public void onObjectPropertySave() {
		ModelLibraryView modelLibraryView = ((ModelLibraryView)TDMEViewer.getInstance().getView());
		for (PropertyModelClass objectProperty: objectPropertiesListBox.getSelection()) {
			if (modelLibraryView.objectPropertySave(
				objectProperty,
				objectPropertyName.getText(),
				objectPropertyValue.getText()) == false) {
				//
				showErrorPopUp("Warning", "Saving object property failed");
				return;
			}
		}
		objectPropertiesListBox.refresh();
	}

	/**
	 * On object property add
	 */
	public void onObjectPropertyAdd() {
		PropertyModelClass objectProperty = ((ModelLibraryView)TDMEViewer.getInstance().getView()).objectPropertyAdd();
		if (objectProperty == null) {
			showErrorPopUp("Warning", "Adding new object property failed");
			return;
		}
		objectPropertiesListBox.addItem(objectProperty);
		objectPropertiesListBox.selectItem(objectProperty);
		objectPropertiesListBox.refresh();
	}

	/**
	 * On object property remove
	 */
	public void onObjectPropertyRemove() {
		for (PropertyModelClass objectProperty: objectPropertiesListBox.getSelection()) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).objectPropertyRemove(objectProperty);			
			objectPropertiesListBox.removeItem(objectProperty);
			objectPropertiesListBox.refresh();
		}
	}

	/**
	 * On object property preset apply 
	 */
	public void onObjectPropertyPresetApply() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).objectPropertiesPreset(objectPropertiesPresets.getSelection());
	}

	/**
	 * Event callback for object properties selection
	 * @param id
	 * @param event
	 */
	@NiftyEventSubscriber(id = "objectproperties_listbox")
	public void onObjectPropertiesSelectionChanged(final String id, final ListBoxSelectionChangedEvent<PropertyModelClass> event) {
		objectPropertyName.disable();
		objectPropertyName.setText(new String());
		objectPropertyValue.disable();
		objectPropertyValue.setText(new String());
		objectPropertySave.disable();
		for (PropertyModelClass objectProperty: event.getSelection()) {
			objectPropertyName.setText(objectProperty.getName());
			objectPropertyValue.setText(objectProperty.getValue());
			objectPropertyName.enable();
			objectPropertyValue.enable();
			objectPropertySave.enable();
		}
	}

	/**
	 * Set up model statistics
	 * @param stats opaque faces
	 * @param stats transparent faces
	 * @param stats material count
	 */
	public void setStatistics(int statsOpaqueFaces, int statsTransparentFaces, int statsMaterialCount) {
		this.statsOpaqueFaces.setText(String.valueOf(statsOpaqueFaces));
		this.statsTransparentFaces.setText(String.valueOf(statsTransparentFaces));
		this.statsMaterialCount.setText(String.valueOf(statsMaterialCount));
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
		showFileDialogPopUp(FileDialogPopUpMode.LOAD);
	}

	/**
	 * On model save
	 */
	public void onModelSave() {
		showFileDialogPopUp(FileDialogPopUpMode.SAVE);
	}

	/**
	 * On model reload
	 */
	public void onModelReload() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).reloadFile();
	}

	/**
	 * On model data apply
	 */
	public void onModelDataApply() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).setModelData(modelName.getText(), modelDescription.getText());

		// rename in library
		LevelEditorModel model = ((ModelLibraryView)TDMEViewer.getInstance().getView()).getSelectedModel();
		if (model == null) return;
		Element modelNameElement = screen.findElementByName("modelchoser_name_" + model.getId());
		TextRenderer modelNameElementRenderer = modelNameElement.getRenderer(TextRenderer.class);
		modelNameElementRenderer.setText(model.getName());
	}

	/**
	 * On pivot apply
	 */
	public void onPivotApply() {
		try {
			float x = Float.parseFloat(pivotX.getText());
			float y = Float.parseFloat(pivotY.getText());
			float z = Float.parseFloat(pivotZ.getText());
			((ModelLibraryView)TDMEViewer.getInstance().getView()).pivotApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * Unset bounding volume
	 */
	public void unsetBoundingVolume() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
		boundingVolumeTypeDropDown.disable();
		boundingVolumeNoneApply.disable();
	}

	/**
	 * Set up bounding volume
	 */
	public void setBoundingVolume() {
		boundingVolumeTypeDropDown.enable();
		boundingVolumeNoneApply.enable();
	}

	@NiftyEventSubscriber(id="tabs_properties")
	public void onTabPropertiesSelected(final String id, final TabSelectedEvent event) {
		LevelEditorModel model = ((ModelLibraryView)TDMEViewer.getInstance().getView()).getSelectedModel();
		if (model == null) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
			return;
		}
		BoundingVolume bv = model.getBoundingVolume();
		if (bv == null) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(0);
		} else
		if (bv instanceof Sphere) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(1);
		} else
		if (bv instanceof Capsule) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(2);
		} else
		if (bv instanceof BoundingBox) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(3);
		} else
		if (bv instanceof OrientedBoundingBox) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(4);
		} else
		if (bv instanceof ConvexMesh) {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(5);
		} else {
			System.out.println("ModelLibraryController::onTabSelected(): invalid bounding volume: " + bv);
		}
	}

	/**
	 * Set up bounding volume types
	 * @param bounding volume types
	 */
	public void setupBoundingVolumeTypes(String[] boundingVolumeTypes, String selectedBoundingVolumeType) {
		for (String bvType: boundingVolumeTypes) {
			boundingVolumeTypeDropDown.addItem(bvType);
		}
		boundingVolumeTypeDropDown.selectItem(selectedBoundingVolumeType);
	}

	/**
	 * Display given bounding volume GUI elements
	 * @param bvType
	 */
	public void selectBoundingVolume(BoundingVolumeType bvType) {
		switch (bvType) {
			case NONE:
				boundingVolumeNoneElement.setVisible(true);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeSphereElement.setVisible(false);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("0%"));
				boundingVolumeCapsuleElement.setVisible(false);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeBoundingBoxElement.setVisible(false);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(false);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeConvexMeshElement.setVisible(false);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeTypeDropDown.selectItemByIndex(0);
				break;
			case SPHERE:
				boundingVolumeNoneElement.setVisible(false);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeSphereElement.setVisible(true);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeCapsuleElement.setVisible(false);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeBoundingBoxElement.setVisible(false);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(false);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeConvexMeshElement.setVisible(false);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeTypeDropDown.selectItemByIndex(1);
				break;
			case CAPSULE:
				boundingVolumeNoneElement.setVisible(false);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeSphereElement.setVisible(false);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeCapsuleElement.setVisible(true);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeBoundingBoxElement.setVisible(false);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(false);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeConvexMeshElement.setVisible(false);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeTypeDropDown.selectItemByIndex(2);
				break;
			case BOUNDINGBOX: 
				boundingVolumeNoneElement.setVisible(false);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeSphereElement.setVisible(false);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeCapsuleElement.setVisible(false);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeBoundingBoxElement.setVisible(true);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(false);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeConvexMeshElement.setVisible(false);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeTypeDropDown.selectItemByIndex(3);
				break;
			case ORIENTEDBOUNDINGBOX: 
				boundingVolumeNoneElement.setVisible(false);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeSphereElement.setVisible(false);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeCapsuleElement.setVisible(false);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeBoundingBoxElement.setVisible(false);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(true);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeConvexMeshElement.setVisible(false);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeTypeDropDown.selectItemByIndex(4);
				break;
			case CONVEXMESH:
				boundingVolumeNoneElement.setVisible(false);
				boundingVolumeNoneElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeSphereElement.setVisible(false);
				boundingVolumeSphereElement.setConstraintHeight(new SizeValue("0%"));
				boundingVolumeCapsuleElement.setVisible(false);
				boundingVolumeCapsuleElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeBoundingBoxElement.setVisible(false);
				boundingVolumeBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeOrientedBoundingBoxElement.setVisible(false);
				boundingVolumeOrientedBoundingBoxElement.setConstraintHeight(new SizeValue("0px"));
				boundingVolumeConvexMeshElement.setVisible(true);
				boundingVolumeConvexMeshElement.setConstraintHeight(new SizeValue("100%"));
				boundingVolumeTypeDropDown.selectItemByIndex(5);
				break;
		}
		boundingVolumeNoneElement.resetLayout();
		boundingVolumeSphereElement.resetLayout();
		boundingVolumeCapsuleElement.resetLayout();
		boundingVolumeBoundingBoxElement.resetLayout();
		boundingVolumeOrientedBoundingBoxElement.resetLayout();
		boundingVolumeConvexMeshElement.resetLayout();
		boundingVolumePanel.resetLayout();
		boundingVolumePanel.layoutElements();
	}

	/**
	 * Setup sphere bounding volume
	 * @param center
	 * @param radius
	 */
	public void setupSphere(Vector3 center, float radius) {
		selectBoundingVolume(BoundingVolumeType.SPHERE);
		boundingvolumeSphereCenter.setText(
			Tools.formatFloat(center.getX()) + ", " +
			Tools.formatFloat(center.getY()) + ", " + 
			Tools.formatFloat(center.getZ())
		);
		boundingvolumeSphereRadius.setText(
			Tools.formatFloat(radius)
		);
	}

	/**
	 * Setup capsule bounding volume
	 * @param center
	 * @param radius
	 */
	public void setupCapsule(Vector3 a, Vector3 b, float radius) {
		selectBoundingVolume(BoundingVolumeType.CAPSULE);
		boundingvolumeCapsuleA.setText(
			Tools.formatFloat(a.getX()) + ", " +
			Tools.formatFloat(a.getY()) + ", " + 
			Tools.formatFloat(a.getZ())
		);
		boundingvolumeCapsuleB.setText(
			Tools.formatFloat(b.getX()) + ", " +
			Tools.formatFloat(b.getY()) + ", " + 
			Tools.formatFloat(b.getZ())
		);
		boundingvolumeCapsuleRadius.setText(
			Tools.formatFloat(radius)
		);
	}

	/**
	 * Setup AABB bounding volume
	 * @param min
	 * @param max
	 */
	public void setupBoundingBox(Vector3 min, Vector3 max) {
		selectBoundingVolume(BoundingVolumeType.BOUNDINGBOX);
		boundingvolumeBoundingBoxMin.setText(
			Tools.formatFloat(min.getX()) + ", " +
			Tools.formatFloat(min.getY()) + ", " + 
			Tools.formatFloat(min.getZ())
		);
		boundingvolumeBoundingBoxMax.setText(
			Tools.formatFloat(max.getX()) + ", " +
			Tools.formatFloat(max.getY()) + ", " + 
			Tools.formatFloat(max.getZ())
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
		boundingvolumeObbCenter.setText(
			Tools.formatFloat(center.getX()) + ", " +
			Tools.formatFloat(center.getY()) + ", " + 
			Tools.formatFloat(center.getZ())
		);
		boundingvolumeObbHalfextension.setText(
			Tools.formatFloat(halfExtension.getX()) + ", " +
			Tools.formatFloat(halfExtension.getY()) + ", " + 
			Tools.formatFloat(halfExtension.getZ())
		);
		boundingvolumeObbAxis0.setText(
			Tools.formatFloat(axis0.getX()) + ", " +
			Tools.formatFloat(axis0.getY()) + ", " + 
			Tools.formatFloat(axis0.getZ())
		);
		boundingvolumeObbAxis1.setText(
			Tools.formatFloat(axis1.getX()) + ", " +
			Tools.formatFloat(axis1.getY()) + ", " + 
			Tools.formatFloat(axis1.getZ())
		);
		boundingvolumeObbAxis2.setText(
			Tools.formatFloat(axis2.getX()) + ", " +
			Tools.formatFloat(axis2.getY()) + ", " + 
			Tools.formatFloat(axis2.getZ())
		);
	}

	/**
	 * Setup convex mesh bounding volume
	 * @param file
	 */
	public void setupConvexMesh(String file) {
		selectBoundingVolume(BoundingVolumeType.CONVEXMESH);
		boundingvolumeConvexMeshFile.setText(
			file
		);
	}

	/**
	 * On pivot apply
	 */
	public void onBoundingVolumeTypeApply() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).selectBoundingVolumeType(boundingVolumeTypeDropDown.getSelectedIndex());
		switch(boundingVolumeTypeDropDown.getSelectedIndex()) {
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
		((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeNone();
	}

	/**
	 * On bounding volume sphere apply
	 */
	public void onBoundingVolumeSphereApply() {
		try {
			((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeSphere(
				Tools.convertToVector3(boundingvolumeSphereCenter.getText()),
				Tools.convertToFloat(boundingvolumeSphereRadius.getText())
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
			((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeCapsule(
				Tools.convertToVector3(boundingvolumeCapsuleA.getText()),
				Tools.convertToVector3(boundingvolumeCapsuleB.getText()),
				Tools.convertToFloat(boundingvolumeCapsuleRadius.getText())
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
			((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeAabb(
				Tools.convertToVector3(boundingvolumeBoundingBoxMin.getText()),
				Tools.convertToVector3(boundingvolumeBoundingBoxMax.getText())
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
			((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeObb(
				Tools.convertToVector3(boundingvolumeObbCenter.getText()),
				Tools.convertToVector3(boundingvolumeObbAxis0.getText()),
				Tools.convertToVector3(boundingvolumeObbAxis1.getText()),
				Tools.convertToVector3(boundingvolumeObbAxis2.getText()),
				Tools.convertToVector3(boundingvolumeObbHalfextension.getText())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On bounding volume convex mesh apply
	 */
	public void onBoundingVolumeConvexMeshApply() {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).applyBoundingVolumeConvexMesh(
			boundingvolumeConvexMeshFile.getText()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.PopUpsController#saveFile(java.lang.String, java.lang.String)
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).saveFile(pathName, fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.viewer.controller.PopUpsController#loadFile(java.lang.String, java.lang.String)
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		((ModelLibraryView)TDMEViewer.getInstance().getView()).loadFile(pathName, fileName);
	}

}

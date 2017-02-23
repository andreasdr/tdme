package net.drewke.tdme.tools.leveleditor.controller;

import java.io.File;
import java.util.Collection;

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.Tools;
import net.drewke.tdme.tools.leveleditor.controller.PopUpsController.FileDialogPopUpMode;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel;
import net.drewke.tdme.tools.leveleditor.model.PropertyModelClass;
import net.drewke.tdme.tools.leveleditor.views.ModelLibraryView;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.ScrollPanel;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.layout.align.VerticalAlign;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.SizeValue;

/**
 * Main Screen ScreenController
 * @author Andreas Drewke
 * @version $Id: ModelLibraryController.java 82 2013-12-26 13:56:48Z drewke.net $
 */
public final class ModelLibraryController extends PopUpsController {

	private Nifty nifty;
	private Screen screen;
	private ScrollPanel modelChoserScrollPanel;
	private Element modelChoser;
	private Element screenCaption;
	private CheckBox displayBoundingVolume;
	private CheckBox displayShadowing;
	private CheckBox displayGround;
	private TextField modelName;
	private TextField modelDescription;
	private Button modelDataApply;
	private TextField objectPropertyName;
	private TextField objectPropertyValue;
	private Button objectPropertySave;
	private Button objectPropertyAdd;
	private Button objectPropertyRemove;
	private Button objectPropertyPresetApply;
	private ListBox<PropertyModelClass> objectPropertiesListBox;
	private DropDown<String> objectPropertiesPresets;
	private TextField triggerWidth;
	private TextField triggerHeight;
	private TextField triggerDepth;
	private Button btnTriggerApply;
	private TextField pivotX;
	private TextField pivotY;
	private TextField pivotZ;
	private Button btnPivotApply;
	private Element modelThumbnailSelected = null;

	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen, new File("."), "tmm,dae");
		this.nifty = nifty;
		this.screen = screen;
		screenCaption = screen.findElementByName("screen_caption");
		modelChoserScrollPanel = screen.findNiftyControl("modelchoser_scrollbar", ScrollPanel.class);
		modelChoser = screen.findElementByName("modelchoser");
		displayBoundingVolume = screen.findNiftyControl("display_boundingvolume", CheckBox.class);
		displayShadowing = screen.findNiftyControl("display_shadowing", CheckBox.class);
		displayGround = screen.findNiftyControl("display_ground", CheckBox.class);
		modelName = screen.findNiftyControl("model_name", TextField.class);
		modelDescription = screen.findNiftyControl("model_description", TextField.class);
		modelDataApply = screen.findNiftyControl("button_modeldata_apply", Button.class);
		objectPropertyName = screen.findNiftyControl("object_property_name", TextField.class);
		objectPropertyValue = screen.findNiftyControl("object_property_value", TextField.class);
		objectPropertySave = screen.findNiftyControl("button_object_properties_save", Button.class);
		objectPropertyAdd = screen.findNiftyControl("button_object_properties_add", Button.class);
		objectPropertyRemove = screen.findNiftyControl("button_object_properties_remove", Button.class);
		objectPropertyPresetApply = screen.findNiftyControl("button_object_properties_presetapply", Button.class);
		objectPropertiesListBox = (ListBox<PropertyModelClass>) screen.findNiftyControl("objectproperties_listbox", ListBox.class);
		objectPropertiesPresets = (DropDown<String>) screen.findNiftyControl("objectproperties_presets", DropDown.class);
		triggerWidth = screen.findNiftyControl("trigger_width", TextField.class);
		triggerHeight = screen.findNiftyControl("trigger_height", TextField.class);
		triggerDepth = screen.findNiftyControl("trigger_depth", TextField.class);
		btnTriggerApply = screen.findNiftyControl("button_trigger_apply", Button.class);
		pivotX = screen.findNiftyControl("pivot_x", TextField.class);
		pivotY = screen.findNiftyControl("pivot_y", TextField.class);
		pivotZ = screen.findNiftyControl("pivot_z", TextField.class);
		btnPivotApply = screen.findNiftyControl("button_pivot_apply", Button.class);
		modelChoserScrollPanel.setStepSizeY(128f + 10f);
		modelThumbnailSelected = null;
	}

	public void onEndScreen() {
	}

	public void onStartScreen() {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayShadowing(displayShadowing.isChecked());
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayGroundPlate(displayGround.isChecked());
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayBoundingVolume(displayBoundingVolume.isChecked());
	}

	public void setScreenCaption(String text) {
		screenCaption.getRenderer(TextRenderer.class).setText(text);
		screen.layoutLayers();
	}

	/**
	 * Set trigger tab
	 * @param dimension
	 */
	public void setTrigger(Vector3 dimension) {
		triggerWidth.setText(Tools.formatFloat(dimension.getX()));
		triggerWidth.enable();
		triggerHeight.setText(Tools.formatFloat(dimension.getY()));
		triggerHeight.enable();		
		triggerDepth.setText(Tools.formatFloat(dimension.getZ()));
		triggerDepth.enable();
		btnTriggerApply.enable();		
	}

	/**
	 * Unset trigger tab
	 */
	public void unsetTrigger() {
		triggerWidth.setText("");
		triggerWidth.disable();
		triggerHeight.setText("");
		triggerHeight.disable();		
		triggerDepth.setText("");
		triggerDepth.disable();
		btnTriggerApply.disable();
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
	}

	@NiftyEventSubscriber(id="display_shadowing")
	public void onDisplayShadowingCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayShadowing(event.isChecked());
	}

	@NiftyEventSubscriber(id="display_ground")
	public void onDisplayGroundCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayGroundPlate(event.isChecked());
	}

	@NiftyEventSubscriber(id="display_boundingvolume")
	public void onDisplayBoundingColumeCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setDisplayBoundingVolume(event.isChecked());
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
		ModelLibraryView modelLibraryView = ((ModelLibraryView)TDMELevelEditor.getInstance().getView());
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
		PropertyModelClass objectProperty = ((ModelLibraryView)TDMELevelEditor.getInstance().getView()).objectPropertyAdd();
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
			((ModelLibraryView)TDMELevelEditor.getInstance().getView()).objectPropertyRemove(objectProperty);			
			objectPropertiesListBox.removeItem(objectProperty);
			objectPropertiesListBox.refresh();
		}
	}

	/**
	 * On object property preset apply 
	 */
	public void onObjectPropertyPresetApply() {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).objectPropertiesPreset(objectPropertiesPresets.getSelection());
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
	 * On quit
	 */
	public void onQuit() {
		TDMELevelEditor.getInstance().quit();
	}

	/**
	 * On add model
	 */
	public void onAddModel() {
		showFileDialogPopUp(FileDialogPopUpMode.LOAD);
	}

	/**
	 * On add trigger
	 */
	public void onAddTrigger() {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).createTrigger();
	}

	/**
	 * On remove model
	 */
	public void onRemoveModel() {
		LevelEditorModel model = ((ModelLibraryView)TDMELevelEditor.getInstance().getView()).getSelectedModel();
		if (model == null) return;

		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).removeModel();
		Element modelThumbnailElement = screen.findElementByName("modelchoser_img_" + model.getId());
		Element modelNameElement = screen.findElementByName("modelchoser_name_" + model.getId());
		modelThumbnailElement.markForRemoval();
		modelNameElement.markForRemoval();

		// set new height
		modelChoser.setConstraintHeight(new SizeValue((TDMELevelEditor.getInstance().getModelLibrary().getModelCount() * (128 + 10 + 20)) + "px"));

		//
		modelThumbnailSelected = null;

		// layout
		screen.layoutLayers();
	}

	/**
	 * Add model
	 * @param model
	 */
	public void addModel(LevelEditorModel model) {
		// add name
		new TextBuilder("modelchoser_name_" + model.getId()).build(nifty, screen, modelChoser);
		Element modelNameElement = screen.findElementByName("modelchoser_name_" + model.getId());
		TextRenderer modelNameElementRenderer = modelNameElement.getRenderer(TextRenderer.class);
		modelNameElementRenderer.setText(model.getName());
		modelNameElementRenderer.setColor(new Color("#000000"));
		modelNameElementRenderer.setTextHAlign(HorizontalAlign.center);
		modelNameElementRenderer.setTextVAlign(VerticalAlign.center);
		modelNameElementRenderer.setFont(nifty.createFont("aurulent-sans-16.fnt"));
		modelNameElement.setMarginBottom(new SizeValue("0px"));
		modelNameElement.setMarginTop(new SizeValue("0px"));
		modelNameElement.setMarginLeft(new SizeValue("8px"));
		modelNameElement.setMarginRight(new SizeValue("8px"));
		modelNameElement.setConstraintWidth(new SizeValue("128px"));
		modelNameElement.setConstraintHeight(new SizeValue("20px"));

		// add thumbnail
		new ImageBuilder("modelchoser_img_" + model.getId()).build(nifty, screen, modelChoser);
		Element modelThumbnailElement = screen.findElementByName("modelchoser_img_" + model.getId());
		modelThumbnailElement.setMarginBottom(new SizeValue("5px"));
		modelThumbnailElement.setMarginTop(new SizeValue("5px"));
		modelThumbnailElement.setMarginLeft(new SizeValue("8px"));
		modelThumbnailElement.setMarginRight(new SizeValue("8px"));
		modelThumbnailElement.setConstraintWidth(new SizeValue("128px"));
		modelThumbnailElement.setConstraintHeight(new SizeValue("128px"));

		//
		selectModel(model.getId());

		// set new height
		modelChoser.setConstraintHeight(new SizeValue((TDMELevelEditor.getInstance().getModelLibrary().getModelCount() * (128 + 10 + 20)) + "px"));

		// layout
		screen.layoutLayers();
	}

	/**
	 * Update model
	 * @param model
	 */
	public void updateModel(LevelEditorModel model) {
		Element modelThumbnailElement = screen.findElementByName("modelchoser_img_" + model.getId());
		modelThumbnailElement.getRenderer(ImageRenderer.class).setImage(nifty.createImage(model.getThumbnail(), false));
		selectModel(model.getId());
	}

	/**
	 * Clickevent model list scrollbar
	 * @param x
	 * @param y
	 */
	public void onListModelsClick(int mouseX, int mouseY) {
		for (Element thumbnailElement: modelChoser.getChildren()) {
			int x = thumbnailElement.getX();
			int y = thumbnailElement.getY();
			int width = thumbnailElement.getWidth();
			int height = thumbnailElement.getHeight();
			if (mouseX >= x && mouseX <= x + width &&
				mouseY >= y && mouseY <= y + height) {
				// this is the clicked thumbnail
				if (thumbnailElement.getId().startsWith("modelchoser_img_")) {
					int id = Integer.parseInt(thumbnailElement.getId().substring("modelchoser_img_".length()));
					selectModel(id);
				}
				break;
			};
		}
	}

	/**
	 * Select model
	 * @param id
	 */
	private void selectModel(int id) {
		if (modelThumbnailSelected != null) {
			int _id = Integer.parseInt(modelThumbnailSelected.getId().substring("modelchoser_img_".length()));
			LevelEditorModel _model = TDMELevelEditor.getInstance().getModelLibrary().getModel(_id);
			if (_model != null) {
				modelThumbnailSelected.getRenderer(ImageRenderer.class).setImage(nifty.createImage("tmp/" + _model.getThumbnail(), false));
			}			
		}
		LevelEditorModel model = TDMELevelEditor.getInstance().getModelLibrary().getModel(id);
		Element modelThumbnailElement = screen.findElementByName("modelchoser_img_" + model.getId());
		modelThumbnailElement.getRenderer(ImageRenderer.class).setImage(nifty.createImage("tmp/selected_" + model.getThumbnail(), false));
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).loadModelFromLibrary(model.getId());
		modelThumbnailSelected = modelThumbnailElement;
	}

	/**
	 * On back
	 */
	public void onBack() {
		TDMELevelEditor.getInstance().switchToLevelEditor();
	}

	/**
	 * On model data apply
	 */
	public void onModelDataApply() {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).setModelData(modelName.getText(), modelDescription.getText());

		// rename in library
		LevelEditorModel model = ((ModelLibraryView)TDMELevelEditor.getInstance().getView()).getSelectedModel();
		if (model == null) return;
		Element modelNameElement = screen.findElementByName("modelchoser_name_" + model.getId());
		TextRenderer modelNameElementRenderer = modelNameElement.getRenderer(TextRenderer.class);
		modelNameElementRenderer.setText(model.getName());
	}

	/**
	 * On trigger apply
	 */
	public void onTriggerApply() {
		try {
			float width = Float.parseFloat(triggerWidth.getText());
			float height = Float.parseFloat(triggerHeight.getText());
			float depth = Float.parseFloat(triggerDepth.getText());
			((ModelLibraryView)TDMELevelEditor.getInstance().getView()).triggerApply(width, height, depth);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	/**
	 * On pivot apply
	 */
	public void onPivotApply() {
		try {
			float x = Float.parseFloat(pivotX.getText());
			float y = Float.parseFloat(pivotY.getText());
			float z = Float.parseFloat(pivotZ.getText());
			((ModelLibraryView)TDMELevelEditor.getInstance().getView()).pivotApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	@Override
	public void saveFile(String pathName, String fileName) throws Exception {
		throw new Exception("not implemented");
	}

	@Override
	public void loadFile(String pathName, String fileName) throws Exception {
		((ModelLibraryView)TDMELevelEditor.getInstance().getView()).loadFile(pathName, fileName);		
	}

}

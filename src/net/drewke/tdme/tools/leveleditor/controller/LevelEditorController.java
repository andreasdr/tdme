package net.drewke.tdme.tools.leveleditor.controller;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.Tools;
import net.drewke.tdme.tools.leveleditor.controller.PopUpsController.FileDialogPopUpMode;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorLight;
import net.drewke.tdme.tools.leveleditor.model.LevelEditorModel;
import net.drewke.tdme.tools.leveleditor.model.LevelPropertyPresets;
import net.drewke.tdme.tools.leveleditor.model.PropertyModelClass;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBox.SelectionMode;
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
public final class LevelEditorController extends PopUpsController {

	private Nifty nifty;
	private Screen screen;
	private ScrollPanel modelChoserScrollPanel;
	private Element modelChoser;
	private Element screenCaption;
	private Button btnObjectTranslationApply;
	private Button btnObjectScaleApply;
	private Button btnObjectRotationsApply;
	private Button btnObjectColor;
	private Button btnObjectCenter;
	private Button btnObjectRemove;
	private TextField gridYPosition;
	private CheckBox gridEnabled;
	private TextField mapWidth;
	private TextField mapDepth;
	private TextField mapHeight;
	private TextField mapPropertyName;
	private TextField mapPropertyValue;
	private Button mapPropertySave;
	private ListBox<PropertyModelClass> mapPropertiesListBox;
	private TextField objectName;
	private TextField objectDescription;
	private TextField objectModel;
	private TextField objectCenter;
	private Button btnObjectDataApply;
	private TextField objectTranslationX;
	private TextField objectTranslationY;
	private TextField objectTranslationZ;
	private TextField objectScaleX;
	private TextField objectScaleY;
	private TextField objectScaleZ;
	private TextField objectRotationX;
	private TextField objectRotationY;
	private TextField objectRotationZ;
	private TextField objectPropertyName;
	private TextField objectPropertyValue;
	private Button btnObjectPropertySave;
	private Button btnObjectPropertyAdd;
	private Button btnObjectPropertyRemove;
	private Button btnObjectPropertyPresetApply;
	private ListBox<PropertyModelClass> objectPropertiesListBox;
	private DropDown<String> objectPropertiesPresets;
	private Element modelThumbnailSelected = null;
	private ListBox<String> objectsListBox;
	private DropDown<String>[] lightsPresets;
	private TextField[] lightsAmbient;
	private TextField[] lightsDiffuse;
	private TextField[] lightsSpecular;
	private TextField[] lightsPosition;
	private TextField[] lightsLinAttenuation;
	private TextField[] lightsConstAttenuation;
	private TextField[] lightsQuadAttenuation;
	private TextField[] lightsSpotTo;
	private TextField[] lightsSpotDirection;
	private TextField[] lightsSpotExponent;
	private TextField[] lightsSpotCutoff;
	private Button[] ligthsSpotDirectionCompute;
	private CheckBox[] lightsEnabled;

	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#bind(de.lessvoid.nifty.Nifty, de.lessvoid.nifty.screen.Screen)
	 */
	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen, new File(TDMELevelEditor.getInstance().getLevel().getPathName()), "tl,dae");
		this.nifty = nifty;
		this.screen = screen;
		screenCaption = screen.findElementByName("screen_caption");
		modelChoserScrollPanel = screen.findNiftyControl("modelchoser_scrollbar", ScrollPanel.class);
		modelChoser = screen.findElementByName("modelchoser");
		modelChoserScrollPanel.setStepSizeY(128f + 10f);
		gridEnabled = screen.findNiftyControl("grid_enabled", CheckBox.class);
		gridYPosition = screen.findNiftyControl("grid_y_position", TextField.class);
		mapWidth = screen.findNiftyControl("map_width", TextField.class);
		mapDepth = screen.findNiftyControl("map_depth", TextField.class);
		mapHeight = screen.findNiftyControl("map_height", TextField.class);
		mapPropertyName = screen.findNiftyControl("map_property_name", TextField.class);
		mapPropertyValue = screen.findNiftyControl("map_property_value", TextField.class);
		mapPropertySave = screen.findNiftyControl("button_map_properties_save", Button.class);
		mapPropertiesListBox = (ListBox<PropertyModelClass>) screen.findNiftyControl("mapproperties_listbox", ListBox.class);
		objectName = screen.findNiftyControl("object_name", TextField.class);
		objectDescription = screen.findNiftyControl("object_description", TextField.class);
		objectModel = screen.findNiftyControl("object_model", TextField.class);
		objectCenter = screen.findNiftyControl("object_center", TextField.class);
		btnObjectDataApply = screen.findNiftyControl("button_objectdata_apply", Button.class);
		btnObjectTranslationApply = screen.findNiftyControl("button_translation_apply", Button.class);
		btnObjectScaleApply = screen.findNiftyControl("button_scale_apply", Button.class);
		btnObjectRotationsApply = screen.findNiftyControl("button_rotations_apply", Button.class);
		btnObjectColor = screen.findNiftyControl("button_object_color", Button.class);
		btnObjectCenter = screen.findNiftyControl("button_object_center", Button.class);
		btnObjectRemove = screen.findNiftyControl("button_object_remove", Button.class);
		objectTranslationX = screen.findNiftyControl("object_translation_x", TextField.class);
		objectTranslationY = screen.findNiftyControl("object_translation_y", TextField.class);
		objectTranslationZ = screen.findNiftyControl("object_translation_z", TextField.class);
		objectScaleX = screen.findNiftyControl("object_scale_x", TextField.class);
		objectScaleY = screen.findNiftyControl("object_scale_y", TextField.class);
		objectScaleZ = screen.findNiftyControl("object_scale_z", TextField.class);
		objectRotationX = screen.findNiftyControl("object_rotation_x", TextField.class);
		objectRotationY = screen.findNiftyControl("object_rotation_y", TextField.class);
		objectRotationZ = screen.findNiftyControl("object_rotation_z", TextField.class);
		objectPropertyName = screen.findNiftyControl("object_property_name", TextField.class);
		objectPropertyValue = screen.findNiftyControl("object_property_value", TextField.class);
		btnObjectPropertySave = screen.findNiftyControl("button_object_properties_save", Button.class);
		btnObjectPropertyAdd = screen.findNiftyControl("button_object_properties_add", Button.class);
		btnObjectPropertyRemove = screen.findNiftyControl("button_object_properties_remove", Button.class);
		btnObjectPropertyPresetApply = screen.findNiftyControl("button_object_properties_presetapply", Button.class);
		objectPropertiesListBox = (ListBox<PropertyModelClass>) screen.findNiftyControl("objectproperties_listbox", ListBox.class);
		objectPropertiesPresets = (DropDown<String>) screen.findNiftyControl("objectproperties_presets", DropDown.class);
		objectsListBox = (ListBox<String>) screen.findNiftyControl("objects_listbox", ListBox.class);
		objectsListBox.changeSelectionMode(SelectionMode.Multiple, false);
		mapWidth.disable();
		mapDepth.disable();
		mapHeight.disable();
		objectModel.disable();
		objectCenter.disable();

		//
		lightsPresets = new DropDown[4];
		lightsAmbient = new TextField[4];
		lightsDiffuse = new TextField[4];
		lightsSpecular = new TextField[4];
		lightsPosition = new TextField[4];
		lightsConstAttenuation = new TextField[4];
		lightsLinAttenuation = new TextField[4];
		lightsQuadAttenuation = new TextField[4];
		lightsSpotTo = new TextField[4];
		lightsSpotDirection = new TextField[4];
		lightsSpotExponent = new TextField[4];
		lightsSpotCutoff = new TextField[4];
		ligthsSpotDirectionCompute = new Button[4];
		lightsEnabled = new CheckBox[4];
		for (int i = 0; i < 4; i++) {
			lightsPresets[i] = (DropDown<String>) screen.findNiftyControl("presets_light" + i, DropDown.class);
			lightsAmbient[i] = screen.findNiftyControl("light" + i + "_ambient", TextField.class);
			lightsDiffuse[i] = screen.findNiftyControl("light" + i + "_diffuse", TextField.class);	
			lightsSpecular[i] = screen.findNiftyControl("light" + i + "_specular", TextField.class);
			lightsPosition[i] = screen.findNiftyControl("light" + i + "_position", TextField.class);
			lightsLinAttenuation[i] = screen.findNiftyControl("light" + i + "_linear_attenuation", TextField.class);
			lightsConstAttenuation[i] = screen.findNiftyControl("light" + i + "_constant_attenuation", TextField.class);
			lightsQuadAttenuation[i] = screen.findNiftyControl("light" + i + "_quadratic_attenuation", TextField.class);
			lightsSpotTo[i] = screen.findNiftyControl("light" + i + "_spot_to", TextField.class);
			lightsSpotDirection[i] = screen.findNiftyControl("light" + i + "_spot_direction", TextField.class);
			lightsSpotExponent[i] = screen.findNiftyControl("light" + i + "_spot_exponent", TextField.class);
			lightsSpotCutoff[i] = screen.findNiftyControl("light" + i + "_spot_cutoff", TextField.class);
			ligthsSpotDirectionCompute[i] = screen.findNiftyControl("button_light" + i + "_spotdirection_compute", Button.class);
			lightsEnabled[i] = screen.findNiftyControl("light" + i + "_enabled", CheckBox.class); 
		}

		modelThumbnailSelected = null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#onEndScreen()
	 */
	public void onEndScreen() {
	}

	/*
	 * (non-Javadoc)
	 * @see de.lessvoid.nifty.screen.ScreenController#onStartScreen()
	 */
	public void onStartScreen() {
	}

	/**
	 * Set up screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getRenderer(TextRenderer.class).setText(text);
		screen.layoutLayers();
	}

	/**
	 * Set grid
	 * @param enabled
	 * @param grid y position
	 */
	public void setGrid(boolean enabled, float gridY) {
		gridEnabled.setChecked(enabled);
		gridYPosition.setText(String.valueOf(gridY));
	}

	/**
	 * Set up level size
	 * @param width
	 * @param height
	 */
	public void setLevelSize(float width, float depth, float height) {
		mapWidth.setText(Tools.formatFloat(width));
		mapDepth.setText(Tools.formatFloat(depth));
		mapHeight.setText(Tools.formatFloat(height));
	}

	/**
	 * Set up map properties
	 * @param map properties
	 */
	public void setMapProperties(Iterable<PropertyModelClass> mapProperties) {
		mapPropertyName.disable();
		mapPropertyValue.disable();
		mapPropertySave.disable();
		mapPropertiesListBox.clear();
		for (PropertyModelClass mapProperty: mapProperties) {
			mapPropertiesListBox.addItem(mapProperty);
		}
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
	 * Unset object properties
	 */
	public void unsetObjectProperties() {
		objectPropertiesPresets.selectItemByIndex(0);
		objectPropertiesPresets.disable();
		btnObjectPropertyPresetApply.disable();
		objectPropertiesListBox.disable();
		btnObjectPropertyAdd.disable();
		btnObjectPropertyRemove.disable();
		btnObjectPropertySave.disable();
		objectPropertyName.setText(new String());
		objectPropertyName.disable();
		objectPropertyValue.setText(new String());
		objectPropertyValue.disable();
		objectPropertiesListBox.clear();
	}

	/**
	 * @return object property preset selection
	 */
	public String getObjectPropertyPresetSelection() {
		return objectPropertiesPresets.getSelection();
	}

	/**
	 * Set up general object data
	 * @param name
	 * @param description
	 * @param model name
	 * @param center
	 */
	public void setObjectData(String name, String description, String modelName, Vector3 center) {
		objectName.enable();
		objectName.setText(name);
		objectDescription.enable();
		objectDescription.setText(description);
		objectModel.setText(modelName);
		this.objectCenter.setText(
			Tools.formatFloat(center.getX())
			+ ", " +
			Tools.formatFloat(center.getY())
			+ ", " +
			Tools.formatFloat(center.getZ()));
		btnObjectDataApply.enable();
	}

	/**
	 * Unset model data
	 */
	public void unsetObjectData() {
		objectName.setText("");
		objectName.disable();
		objectDescription.setText("");
		objectDescription.disable();
		objectModel.setText("");
		objectModel.disable();
		objectCenter.setText("");
		objectCenter.disable();
		btnObjectDataApply.disable();
	}

	/**
	 * On object data apply
	 */
	public void onObjectDataApply() {
		if (((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectDataApply(objectName.getText(), objectDescription.getText()) == false) {
			showErrorPopUp("Warning", "Changing object data failed");
		}
	}

	/**
	 * Set up object list box
	 * @param object id enumerator
	 */
	public void setObjectListbox(Iterator<String> objectIdsIterator) {
		objectsListBox.clear();
		while (objectIdsIterator.hasNext()) {
			objectsListBox.addItem(objectIdsIterator.next());
		}
		objectsListBox.sortAllItems();
	}

	/**
	 * Add a object to object list box
	 * @param object id
	 */
	public void addObjectToObjectListbox(String objectId) {
		objectsListBox.addItem(objectId);
		objectsListBox.sortAllItems();
		objectsListBox.selectItem(objectId);
	}

	/**
	 * Remove a object from object list box
	 * @param object id
	 */
	public void removeObjectFromObjectListbox(String objectId) {
		objectsListBox.removeItem(objectId);
		for (String selectedObjectId: objectsListBox.getSelection()) {
			objectsListBox.deselectItem(selectedObjectId);
		}
	}

	/**
	 * Update a object from in object list box
	 * @param object id enumerator
	 */
	public void updateObjectInObjectListbox(String oldObjectId, String newObjectId) {
		objectsListBox.removeItem(oldObjectId);
		objectsListBox.addItem(newObjectId);
		objectsListBox.sortAllItems();
		objectsListBox.selectItem(newObjectId);
	}

	/**
	 * Unselect objects in object list box
	 */
	public void unselectObjectInObjectListBox(String objectId) {
		objectsListBox.deselectItem(objectId);
	}

	/**
	 * Unselect objects in object list box
	 */
	public void unselectObjectsInObjectListBox() {
		for (String selectedObjectId: objectsListBox.getSelection()) {
			objectsListBox.deselectItem(selectedObjectId);
		}
	}

	/**
	 * Select a object in object list box
	 * @param object id
	 */
	public void selectObjectInObjectListbox(String objectId) {
		objectsListBox.selectItem(objectId);
	}

	/**
	 * On objects select button click event
	 */
	public void onObjectsSelect() {
		LevelEditorView view = (LevelEditorView)TDMELevelEditor.getInstance().getView();
		if (objectsListBox.getSelection().isEmpty() == false) view.selectObjectsById(objectsListBox.getSelection());
	}

	/**
	 * On objects unselect button click event
	 */
	public void onObjectsUnselect() {
		LevelEditorView view = (LevelEditorView)TDMELevelEditor.getInstance().getView();
		view.unselectObjects();
	}

	/**
	 * Set up object properties
	 * @param has properties
	 * @param preset id
	 * @param object properties
	 */
	public void setObjectProperties(String presetId, Iterable<PropertyModelClass> objectProperties) {
		objectPropertiesPresets.enable();
		btnObjectPropertyPresetApply.enable();
		objectPropertiesListBox.enable();
		btnObjectPropertyAdd.enable();
		btnObjectPropertyRemove.enable();
		btnObjectPropertySave.disable();
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
	 * Set up object
	 * @param translation
	 * @param scale
	 * @param rotation x
	 * @param rotation y
	 * @param rotation z
	 */
	public void setObject(Vector3 translation, Vector3 scale, float rotationX, float rotationY, float rotationZ) {
		btnObjectTranslationApply.enable();
		btnObjectScaleApply.enable();
		btnObjectRotationsApply.enable();
		btnObjectColor.enable();
		btnObjectCenter.enable();
		btnObjectRemove.enable();
		objectTranslationX.enable();
		objectTranslationY.enable();
		objectTranslationZ.enable();
		objectScaleX.enable();
		objectScaleY.enable();
		objectScaleZ.enable();
		objectRotationX.enable();
		objectRotationY.enable();
		objectRotationZ.enable();
		objectTranslationX.setText(Tools.formatFloat(translation.getX()));
		objectTranslationY.setText(Tools.formatFloat(translation.getY()));
		objectTranslationZ.setText(Tools.formatFloat(translation.getZ()));
		objectScaleX.setText(Tools.formatFloat(scale.getX()));
		objectScaleY.setText(Tools.formatFloat(scale.getY()));
		objectScaleZ.setText(Tools.formatFloat(scale.getZ()));
		objectRotationX.setText(Tools.formatFloat(rotationX));
		objectRotationY.setText(Tools.formatFloat(rotationY));
		objectRotationZ.setText(Tools.formatFloat(rotationZ));
	}

	/**
	 * Unset current object
	 */
	public void unsetObject() {
		btnObjectTranslationApply.disable();
		btnObjectScaleApply.disable();
		btnObjectRotationsApply.disable();
		btnObjectColor.disable();
		btnObjectCenter.disable();
		btnObjectRemove.disable();
		objectTranslationX.disable();
		objectTranslationY.disable();
		objectTranslationZ.disable();
		objectScaleX.disable();
		objectScaleY.disable();
		objectScaleZ.disable();
		objectRotationX.disable();
		objectRotationY.disable();
		objectRotationZ.disable();
		objectTranslationX.setText(new String());
		objectTranslationY.setText(new String());
		objectTranslationZ.setText(new String());
		objectScaleX.setText(new String());
		objectScaleY.setText(new String());
		objectScaleZ.setText(new String());
		objectRotationX.setText(new String());
		objectRotationY.setText(new String());
		objectRotationZ.setText(new String());
	}

	/**
	 * Set up light presets
	 * @param light presets
	 */
	public void setLightPresetsIds(Collection<String> lightPresetIds) {
		for (int i = 0; i < lightsPresets.length; i++) {
			lightsPresets[i].addItem("");
			for (String lightPresetId: lightPresetIds) {
				lightsPresets[i].addItem(lightPresetId);
			}
		}
	}

	/**
	 * Unselect light presets
	 */
	public void unselectLightPresets() {
		for (int i = 0; i < lightsPresets.length; i++) {
			lightsPresets[i].selectItem("");
		}
	}

	/**
	 * Set up light indexed by i
	 * @param i
	 * @param ambient
	 * @param diffuse
	 * @param specular
	 * @param position
	 * @param const attenuation
	 * @param linear attenuation
	 * @param quadratic attenuation
	 * @param spot to
	 * @param spot direction
	 * @param spot exponent
	 * @param spot cutoff
	 * @param enabled
	 */
	public void setLight(int i,
		Color4 ambient, Color4 diffuse, Color4 specular, Vector4 position,
		float constAttenuation, float linearAttenuation, float quadraticAttenuation,
		Vector3 spotTo,Vector3 spotDirection,
		float spotExponent, float spotCutoff, boolean enabled) {
		lightsAmbient[i].setText(
			Tools.formatFloat(ambient.getRed()) + ", " +
			Tools.formatFloat(ambient.getGreen()) + ", " +
			Tools.formatFloat(ambient.getBlue()) + ", " +
			Tools.formatFloat(ambient.getAlpha())
		);
		lightsDiffuse[i].setText(
			Tools.formatFloat(diffuse.getRed()) + ", " +
			Tools.formatFloat(diffuse.getGreen()) + ", " +
			Tools.formatFloat(diffuse.getBlue()) + ", " +
			Tools.formatFloat(diffuse.getAlpha())
		);
		lightsSpecular[i].setText(
			Tools.formatFloat(specular.getRed()) + ", " +
			Tools.formatFloat(specular.getGreen()) + ", " +
			Tools.formatFloat(specular.getBlue()) + ", " +
			Tools.formatFloat(specular.getAlpha())
		);
		lightsPosition[i].setText(
			Tools.formatFloat(position.getX()) + ", " +
			Tools.formatFloat(position.getY()) + ", " +
			Tools.formatFloat(position.getZ()) + ", " +
			Tools.formatFloat(position.getW())
		);
		lightsConstAttenuation[i].setText(Tools.formatFloat(constAttenuation));
		lightsLinAttenuation[i].setText(Tools.formatFloat(linearAttenuation));
		lightsQuadAttenuation[i].setText(Tools.formatFloat(quadraticAttenuation));
		lightsSpotTo[i].setText(
			Tools.formatFloat(spotTo.getX()) + ", " +
			Tools.formatFloat(spotTo.getY()) + ", " +
			Tools.formatFloat(spotTo.getZ())
		);
		lightsSpotDirection[i].setText(
			Tools.formatFloat(spotDirection.getX()) + ", " +
			Tools.formatFloat(spotDirection.getY()) + ", " +
			Tools.formatFloat(spotDirection.getZ())
		);
		lightsSpotExponent[i].setText(Tools.formatFloat(spotExponent));
		lightsSpotCutoff[i].setText(Tools.formatFloat(spotCutoff));
		lightsEnabled[i].setChecked(enabled);
		lightsAmbient[i].setEnabled(enabled);
		lightsDiffuse[i].setEnabled(enabled);
		lightsSpecular[i].setEnabled(enabled);
		lightsPosition[i].setEnabled(enabled);
		lightsConstAttenuation[i].setEnabled(enabled);
		lightsLinAttenuation[i].setEnabled(enabled);
		lightsQuadAttenuation[i].setEnabled(enabled);
		lightsSpotTo[i].setEnabled(enabled);
		lightsSpotDirection[i].setEnabled(enabled);
		lightsSpotExponent[i].setEnabled(enabled);
		lightsSpotCutoff[i].setEnabled(enabled);
		ligthsSpotDirectionCompute[i].setEnabled(enabled);
	}

	/**
	 * Event callback for map properties selection
	 * @param id
	 * @param event
	 */
	@NiftyEventSubscriber(id = "mapproperties_listbox")
	public void onMapPropertiesSelectionChanged(final String id, final ListBoxSelectionChangedEvent<PropertyModelClass> event) {
		mapPropertyName.disable();
		mapPropertyName.setText(new String());
		mapPropertyValue.disable();
		mapPropertyValue.setText(new String());
		mapPropertySave.disable();
		for (PropertyModelClass mapProperty: event.getSelection()) {
			mapPropertyName.setText(mapProperty.getName());
			mapPropertyValue.setText(mapProperty.getValue());
			mapPropertyName.enable();
			mapPropertyValue.enable();
			mapPropertySave.enable();
		}
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
		btnObjectPropertySave.disable();
		for (PropertyModelClass objectProperty: event.getSelection()) {
			objectPropertyName.setText(objectProperty.getName());
			objectPropertyValue.setText(objectProperty.getValue());
			objectPropertyName.enable();
			objectPropertyValue.enable();
			btnObjectPropertySave.enable();
		}
	}

	/**
	 * On quit action
	 */
	public void onQuit() {
		TDMELevelEditor.getInstance().quit();
	}

	/**
	 * On library action
	 */
	public void onLibrary() {
		TDMELevelEditor.getInstance().switchToModelLibrary();
	}

	/**
	 * Add a model to level editor view
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
	 * Removes all models
	 */
	public void removeModels() {
		for (Element modelThumbnailElement: modelChoser.getChildren()) {
			modelThumbnailElement.markForRemoval();
		}

		// set new height
		modelChoser.setConstraintHeight(new SizeValue("0px"));

		// layout
		screen.layoutLayers();
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
	 * internal select off model
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
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).loadModelFromLibrary(model.getId());
		modelThumbnailSelected = modelThumbnailElement;
	}

	/**
	 * place model button clicked
	 */
	public void onPlaceModel() {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).placeObject();
	}

	/**
	 * On object translation apply action
	 */
	public void onObjectTranslationApply() {
		try {
			float x = Float.parseFloat(objectTranslationX.getText());
			float y = Float.parseFloat(objectTranslationY.getText());
			float z = Float.parseFloat(objectTranslationZ.getText());
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectTranslationApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	/**
	 * On object scale apply action
	 */
	public void onObjectScaleApply() {
		try {
			float x = Float.parseFloat(objectScaleX.getText());
			float y = Float.parseFloat(objectScaleY.getText());
			float z = Float.parseFloat(objectScaleZ.getText());
			if (x < -10f || x > 10f) throw new IllegalArgumentException("x scale must be within -10 .. +10");
			if (y < -10f || y > 10f) throw new IllegalArgumentException("y scale must be within -10 .. +10");
			if (z < -10f || z > 10f) throw new IllegalArgumentException("z scale must be within -10 .. +10");
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectScaleApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	/**
	 * On object rotations apply action
	 */
	public void onObjectRotationsApply() {
		try {
			float x = Float.parseFloat(objectRotationX.getText());
			float y = Float.parseFloat(objectRotationY.getText());
			float z = Float.parseFloat(objectRotationZ.getText());
			if (x < -360f || x > 360f) throw new IllegalArgumentException("x axis rotation must be within -360 .. +360");
			if (y < -360f || y > 360f) throw new IllegalArgumentException("y axis rotation must be within -360 .. +360");
			if (z < -360f || z > 360f) throw new IllegalArgumentException("z axis rotation must be within -360 .. +360");
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectRotationsApply(x, y, z);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	/**
	 * On object remove action
	 */
	public void onObjectRemove() {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).removeObject();		
	}

	/**
	 * On object color action
	 */
	public void onObjectColor() {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).colorObject();		
	}

	/**
	 * On object center action
	 */
	public void onObjectCenter() {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).centerObject();		
	}

	/**
	 * On map load action
	 */
	public void onMapLoad() {
		showFileDialogPopUp(FileDialogPopUpMode.LOAD);		
	}

	/**
	 * On map save action
	 */
	public void onMapSave() {
		showFileDialogPopUp(FileDialogPopUpMode.SAVE);		
	}

	/**
	 * On map property save
	 */
	public void onMapPropertySave() {
		for (PropertyModelClass mapProperty: mapPropertiesListBox.getSelection()) {
			LevelEditorView levelEditorView = ((LevelEditorView)TDMELevelEditor.getInstance().getView());
			if (levelEditorView.mapPropertySave(
				mapProperty,
				mapPropertyName.getText(),
				mapPropertyValue.getText()) == false) {
				//
				showErrorPopUp("Warning", "Saving map property failed");
				return;
			}
		}
		mapPropertiesListBox.refresh();
	}

	/**
	 * On map property remove
	 */
	public void onMapPropertyRemove() {
		for (PropertyModelClass mapProperty: mapPropertiesListBox.getSelection()) {
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).mapPropertyRemove(mapProperty);			
			mapPropertiesListBox.removeItem(mapProperty);
			mapPropertiesListBox.refresh();
		}
	}

	/**
	 * On map property add
	 */
	public void onMapPropertyAdd() {
		PropertyModelClass mapProperty = ((LevelEditorView)TDMELevelEditor.getInstance().getView()).mapPropertyAdd();
		if (mapProperty == null) {
			showErrorPopUp("Warning", "Adding new map property failed");
			return;
		}
		mapPropertiesListBox.addItem(mapProperty);
		mapPropertiesListBox.selectItem(mapProperty);
		mapPropertiesListBox.refresh();
	}

	/**
	 * On object property save
	 */
	public void onObjectPropertySave() {
		LevelEditorView levelEditorView = ((LevelEditorView)TDMELevelEditor.getInstance().getView());
		for (PropertyModelClass objectProperty: objectPropertiesListBox.getSelection()) {
			if (levelEditorView.objectPropertySave(
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
		PropertyModelClass objectProperty = ((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectPropertyAdd();
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
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectPropertyRemove(objectProperty);			
			objectPropertiesListBox.removeItem(objectProperty);
			objectPropertiesListBox.refresh();
		}
	}

	/**
	 * On grid apply button
	 */
	public void onGridApply() {
		try {
			float gridY = Float.parseFloat(gridYPosition.getText());
			if (gridY < -5f || gridY > 5f) throw new IllegalArgumentException("grid y position must be within -5 .. +5");
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).setGridY(gridY);
			((LevelEditorView)TDMELevelEditor.getInstance().getView()).setGridEnabled(gridEnabled.isChecked());
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		} catch (IllegalArgumentException iae) {
			showErrorPopUp("Warning", iae.getMessage());
		}
	}

	/**
	 * On object property preset apply 
	 */
	public void onObjectPropertyPresetApply() {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).objectPropertiesPreset(objectPropertiesPresets.getSelection());
	}

	/**
	 * On light 0 apply
	 */
	public void onLight0Apply() {
		onLightApply(0);
	}

	/**
	 * On light 1 apply
	 */
	public void onLight1Apply() {
		onLightApply(1);
	}

	/**
	 * On light 2 apply
	 */
	public void onLight2Apply() {
		onLightApply(2);
	}

	/**
	 * On light 3 apply
	 */
	public void onLight3Apply() {
		onLightApply(3);
	}

	/**
	 * On light 3 apply
	 * @param light idx
	 */
	public void onLightApply(int lightIdx) {
		try {
			((LevelEditorView)TDMELevelEditor.getInstance(). getView()).applyLight(
				lightIdx,
				Tools.convertToColor4(lightsAmbient[lightIdx].getText()),
				Tools.convertToColor4(lightsDiffuse[lightIdx].getText()),
				Tools.convertToColor4(lightsSpecular[lightIdx].getText()),
				Tools.convertToVector4(lightsPosition[lightIdx].getText()),
				Tools.convertToFloat(lightsConstAttenuation[lightIdx].getText()),
				Tools.convertToFloat(lightsLinAttenuation[lightIdx].getText()),
				Tools.convertToFloat(lightsQuadAttenuation[lightIdx].getText()),
				Tools.convertToVector3(lightsSpotTo[lightIdx].getText()),
				Tools.convertToVector3(lightsSpotDirection[lightIdx].getText()),
				Tools.convertToFloat(lightsSpotExponent[lightIdx].getText()),
				Tools.convertToFloat(lightsSpotCutoff[lightIdx].getText()),
				lightsEnabled[lightIdx].isChecked()
			);
			lightsAmbient[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsDiffuse[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsSpecular[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsPosition[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsConstAttenuation[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsLinAttenuation[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsQuadAttenuation[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsSpotTo[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsSpotDirection[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsSpotExponent[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			lightsSpotCutoff[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
			ligthsSpotDirectionCompute[lightIdx].setEnabled(lightsEnabled[lightIdx].isChecked());
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/**
	 * On light 0 preset apply
	 */
	public void onLight0PresetApply() {
		onLightPresetApply(0);
	}

	/**
	 * On light 1 preset apply
	 */
	public void onLight1PresetApply() {
		onLightPresetApply(1);
	}

	/**
	 * On light 2 preset apply
	 */
	public void onLight2PresetApply() {
		onLightPresetApply(2);
	}

	/**
	 * On light 3 preset apply
	 */
	public void onLight3PresetApply() {
		onLightPresetApply(3);
	}

	/**
	 * On light preset apply for light
	 * @param i
	 */
	public void onLightPresetApply(int lightIdx) {
		// get preset
		LevelEditorLight lightPreset = LevelPropertyPresets.getInstance().getLightPresets().get(lightsPresets[lightIdx].getSelection());
		if (lightPreset == null) return;

		// apply preset
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).applyLight(
			lightIdx,
			lightPreset.getAmbient(),
			lightPreset.getDiffuse(),
			lightPreset.getSpecular(),
			lightPreset.getPosition(),
			lightPreset.getConstantAttenuation(),
			lightPreset.getLinearAttenuation(),
			lightPreset.getQuadraticAttenuation(),
			lightPreset.getSpotTo(),
			lightPreset.getSpotDirection(),
			lightPreset.getSpotExponent(),
			lightPreset.getSpotCutOff(),
			lightPreset.isEnabled()
		);
	}

	/**
	 * On Light 0 spot direction compute 
	 */
	public void onLight0SpotDirectionCompute() {
		onLightSpotDirectionCompute(0);
	}

	/**
	 * On Light 1 spot direction compute 
	 */
	public void onLight1SpotDirectionCompute() {
		onLightSpotDirectionCompute(1);
	}

	/**
	 * On Light 2 spot direction compute 
	 */
	public void onLight2SpotDirectionCompute() {
		onLightSpotDirectionCompute(2);
	}

	/**
	 * On Light 3 spot direction compute 
	 */
	public void onLight3SpotDirectionCompute() {
		onLightSpotDirectionCompute(3);
	}

	/**
	 * On Light spot direction compute for given light idx 
	 */
	public void onLightSpotDirectionCompute(int lightIdx) {		
		try {
			((LevelEditorView)TDMELevelEditor.getInstance(). getView()).computeSpotDirection(
				lightIdx,
				Tools.convertToVector4(lightsPosition[lightIdx].getText()),
				Tools.convertToVector3(lightsSpotTo[lightIdx].getText())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/*
	 * 
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).saveMap(
			pathName,
			fileName
		);
	}

	/*
	 * 
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		((LevelEditorView)TDMELevelEditor.getInstance().getView()).loadMap(
			pathName,
			fileName
		);
	}

}
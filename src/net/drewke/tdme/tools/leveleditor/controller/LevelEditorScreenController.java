 package net.drewke.tdme.tools.leveleditor.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.events.Action;
import net.drewke.tdme.gui.events.GUIActionListener;
import net.drewke.tdme.gui.events.GUIChangeListener;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.math.Vector4;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.leveleditor.views.LevelEditorView;
import net.drewke.tdme.tools.shared.controller.ScreenController;
import net.drewke.tdme.tools.shared.model.LevelEditorLight;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.tools.shared.model.LevelPropertyPresets;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.utils.MutableString;

/**
 * Level Editor Screen Controller
 * @author Andreas Drewke
 * @version $Id: ModelLibraryController.java 82 2013-12-26 13:56:48Z drewke.net $
 */
public final class LevelEditorScreenController extends ScreenController implements GUIActionListener, GUIChangeListener {

	private final static MutableString CHECKBOX_CHECKED = new MutableString("1");
	private final static MutableString CHECKBOX_UNCHECKED = new MutableString("");
	private final static MutableString TEXT_EMPTY = new MutableString("");

	private LevelEditorView view;
	private GUIScreenNode screenNode;
	private GUITextNode screenCaption;
	private GUIElementNode btnObjectTranslationApply;
	private GUIElementNode btnObjectScaleApply;
	private GUIElementNode btnObjectRotationApply;
	private GUIElementNode btnObjectColor;
	private GUIElementNode btnObjectCenter;
	private GUIElementNode btnObjectRemove;
	private GUIElementNode gridYPosition;
	private GUIElementNode gridEnabled;
	private GUIElementNode mapWidth;
	private GUIElementNode mapDepth;
	private GUIElementNode mapHeight;
	private GUIElementNode mapPropertyName;
	private GUIElementNode mapPropertyValue;
	private GUIElementNode mapPropertySave;
	private GUIElementNode mapPropertyRemove;
	private GUIElementNode mapPropertiesListBox;
	private GUIElementNode objectName;
	private GUIElementNode objectDescription;
	private GUIElementNode objectModel;
	private GUIElementNode objectCenter;
	private GUIElementNode btnObjectDataApply;
	private GUIElementNode objectTranslationX;
	private GUIElementNode objectTranslationY;
	private GUIElementNode objectTranslationZ;
	private GUIElementNode objectScaleX;
	private GUIElementNode objectScaleY;
	private GUIElementNode objectScaleZ;
	private GUIElementNode objectRotationX;
	private GUIElementNode objectRotationY;
	private GUIElementNode objectRotationZ;
	private GUIElementNode objectPropertyName;
	private GUIElementNode objectPropertyValue;
	private GUIElementNode btnObjectPropertySave;
	private GUIElementNode btnObjectPropertyAdd;
	private GUIElementNode btnObjectPropertyRemove;
	private GUIElementNode btnObjectPropertyPresetApply;
	private GUIElementNode objectPropertiesListBox;
	private GUIElementNode objectPropertiesPresets;
	private GUIElementNode objectsListBox;
	private GUIElementNode[] lightsPresets;
	private GUIElementNode[] lightsAmbient;
	private GUIElementNode[] lightsDiffuse;
	private GUIElementNode[] lightsSpecular;
	private GUIElementNode[] lightsPosition;
	private GUIElementNode[] lightsLinAttenuation;
	private GUIElementNode[] lightsConstAttenuation;
	private GUIElementNode[] lightsQuadAttenuation;
	private GUIElementNode[] lightsSpotTo;
	private GUIElementNode[] lightsSpotDirection;
	private GUIElementNode[] lightsSpotExponent;
	private GUIElementNode[] lightsSpotCutoff;
	private GUIElementNode[] ligthsSpotDirectionCompute;
	private GUIElementNode[] lightsEnabled;

	private MutableString value;
	private MutableString selectedObjects;
	private ArrayList<String> selectedObjectList;

	private String mapPath;

	/**
	 * Public constructor
	 * @param view
	 */
	public LevelEditorScreenController(LevelEditorView view) {
		this.view = view;
		this.mapPath = ".";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#getScreenNode()
	 */
	public GUIScreenNode getScreenNode() {
		return screenNode;
	}

	/**
	 * @return map path
	 */
	public String getMapPath() {
		return mapPath;
	}

	/**
	 * Set map path
	 * @param map path
	 */
	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#init()
	 */
	public void init() {
		try {
			screenNode = GUIParser.parse("resources/tools/leveleditor/gui", "screen_leveleditor.xml");
			screenNode.addActionListener(this);
			screenNode.addChangeListener(this);

			// 
			screenCaption = (GUITextNode)screenNode.getNodeById("screen_caption");
			gridEnabled = (GUIElementNode)screenNode.getNodeById("grid_enabled");
			gridYPosition = (GUIElementNode)screenNode.getNodeById("grid_y_position");
			mapWidth = (GUIElementNode)screenNode.getNodeById("map_width");
			mapDepth = (GUIElementNode)screenNode.getNodeById("map_depth");
			mapHeight = (GUIElementNode)screenNode.getNodeById("map_height");
			mapPropertyName = (GUIElementNode)screenNode.getNodeById("map_property_name");
			mapPropertyValue = (GUIElementNode)screenNode.getNodeById("map_property_value");
			mapPropertySave = (GUIElementNode)screenNode.getNodeById("button_map_properties_save");
			mapPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_map_properties_remove");
			mapPropertiesListBox = (GUIElementNode)screenNode.getNodeById("map_properties_listbox");
			objectName = (GUIElementNode)screenNode.getNodeById("object_name");
			objectDescription = (GUIElementNode)screenNode.getNodeById("object_description");
			objectModel = (GUIElementNode)screenNode.getNodeById("object_model");
			objectCenter = (GUIElementNode)screenNode.getNodeById("object_center");
			btnObjectDataApply = (GUIElementNode)screenNode.getNodeById("button_objectdata_apply");
			btnObjectTranslationApply = (GUIElementNode)screenNode.getNodeById("button_translation_apply");
			btnObjectScaleApply = (GUIElementNode)screenNode.getNodeById("button_scale_apply");
			btnObjectRotationApply = (GUIElementNode)screenNode.getNodeById("button_rotation_apply");
			btnObjectColor = (GUIElementNode)screenNode.getNodeById("button_object_color");
			btnObjectCenter = (GUIElementNode)screenNode.getNodeById("button_object_center");
			btnObjectRemove = (GUIElementNode)screenNode.getNodeById("button_object_remove");
			objectTranslationX = (GUIElementNode)screenNode.getNodeById("object_translation_x");
			objectTranslationY = (GUIElementNode)screenNode.getNodeById("object_translation_y");
			objectTranslationZ = (GUIElementNode)screenNode.getNodeById("object_translation_z");
			objectScaleX = (GUIElementNode)screenNode.getNodeById("object_scale_x");
			objectScaleY = (GUIElementNode)screenNode.getNodeById("object_scale_y");
			objectScaleZ = (GUIElementNode)screenNode.getNodeById("object_scale_z");
			objectRotationX = (GUIElementNode)screenNode.getNodeById("object_rotation_x");
			objectRotationY = (GUIElementNode)screenNode.getNodeById("object_rotation_y");
			objectRotationZ = (GUIElementNode)screenNode.getNodeById("object_rotation_z");
			objectPropertyName = (GUIElementNode)screenNode.getNodeById("object_property_name");
			objectPropertyValue = (GUIElementNode)screenNode.getNodeById("object_property_value");
			btnObjectPropertySave = (GUIElementNode)screenNode.getNodeById("button_object_properties_save");
			btnObjectPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_object_properties_remove");
			btnObjectPropertyAdd = (GUIElementNode)screenNode.getNodeById("button_object_properties_add");
			btnObjectPropertyRemove = (GUIElementNode)screenNode.getNodeById("button_object_properties_remove");
			btnObjectPropertyPresetApply = (GUIElementNode)screenNode.getNodeById("button_object_properties_presetapply");
			objectPropertiesListBox = (GUIElementNode)screenNode.getNodeById("object_properties_listbox");
			objectPropertiesPresets = (GUIElementNode)screenNode.getNodeById("object_properties_presets");
			objectsListBox = (GUIElementNode)screenNode.getNodeById("objects_listbox");
			mapWidth.getController().setDisabled(true);
			mapDepth.getController().setDisabled(true);
			mapHeight.getController().setDisabled(true);
			objectModel.getController().setDisabled(true);
			objectCenter.getController().setDisabled(true);
	
			//
			lightsPresets = new GUIElementNode[4];
			lightsAmbient = new GUIElementNode[4];
			lightsDiffuse = new GUIElementNode[4];
			lightsSpecular = new GUIElementNode[4];
			lightsPosition = new GUIElementNode[4];
			lightsConstAttenuation = new GUIElementNode[4];
			lightsLinAttenuation = new GUIElementNode[4];
			lightsQuadAttenuation = new GUIElementNode[4];
			lightsSpotTo = new GUIElementNode[4];
			lightsSpotDirection = new GUIElementNode[4];
			lightsSpotExponent = new GUIElementNode[4];
			lightsSpotCutoff = new GUIElementNode[4];
			ligthsSpotDirectionCompute = new GUIElementNode[4];
			lightsEnabled = new GUIElementNode[4];
			for (int i = 0; i < 4; i++) {
				lightsPresets[i] = (GUIElementNode)screenNode.getNodeById("presets_light" + i);
				lightsAmbient[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_ambient");
				lightsDiffuse[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_diffuse");	
				lightsSpecular[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_specular");
				lightsPosition[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_position");
				lightsLinAttenuation[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_linear_attenuation");
				lightsConstAttenuation[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_constant_attenuation");
				lightsQuadAttenuation[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_quadratic_attenuation");
				lightsSpotTo[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_spot_to");
				lightsSpotDirection[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_spot_direction");
				lightsSpotExponent[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_spot_exponent");
				lightsSpotCutoff[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_spot_cutoff");
				ligthsSpotDirectionCompute[i] = (GUIElementNode)screenNode.getNodeById("button_light" + i + "_spotdirection_compute");
				lightsEnabled[i] = (GUIElementNode)screenNode.getNodeById("light" + i + "_enabled"); 
			}
	
			value = new MutableString();
			selectedObjects = new MutableString();
			selectedObjectList = new ArrayList<String>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.controller.ScreenController#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set up screen caption
	 * @param text
	 */
	public void setScreenCaption(String text) {
		screenCaption.getText().set(text);
		screenNode.layout(screenCaption);
	}

	/**
	 * Set grid
	 * @param enabled
	 * @param grid y position
	 */
	public void setGrid(boolean enabled, float gridY) {
		gridEnabled.getController().setValue(enabled == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		gridYPosition.getController().setValue(value.set(String.valueOf(gridY)));
	}

	/**
	 * Set up level size
	 * @param width
	 * @param height
	 */
	public void setLevelSize(float width, float depth, float height) {
		mapWidth.getController().setValue(value.set(Tools.formatFloat(width)));
		mapDepth.getController().setValue(value.set(Tools.formatFloat(depth)));
		mapHeight.getController().setValue(value.set(Tools.formatFloat(height)));
	}

	/**
	 * Unset object properties
	 */
	public void unsetObjectProperties() {
		objectPropertiesPresets.getController().setValue(value.set("none"));
		objectPropertiesPresets.getController().setDisabled(true);
		btnObjectPropertyPresetApply.getController().setDisabled(true);
		objectPropertiesListBox.getController().setDisabled(true);
		btnObjectPropertyAdd.getController().setDisabled(true);
		btnObjectPropertyRemove.getController().setDisabled(true);
		btnObjectPropertySave.getController().setDisabled(true);
		objectPropertyName.getController().setValue(TEXT_EMPTY);
		objectPropertyName.getController().setDisabled(true);
		objectPropertyValue.getController().setValue(TEXT_EMPTY);
		objectPropertyValue.getController().setDisabled(true);

		// object properties list box inner
		GUIParentNode objectPropertiesListBoxInnerNode = (GUIParentNode)(objectPropertiesListBox.getScreenNode().getNodeById(objectPropertiesListBox.getId() + "_inner"));
		// 	clear sub nodes
		objectPropertiesListBoxInnerNode.clearSubNodes();
	}

	/**
	 * @return object property preset selection
	 */
	public String getObjectPropertyPresetSelection() {
		// TODO: return objectPropertiesPresets.getSelection();
		return "";
	}

	/**
	 * Set up general object data
	 * @param name
	 * @param description
	 * @param model name
	 * @param center
	 */
	public void setObjectData(String name, String description, String modelName, Vector3 center) {
		objectName.getController().setDisabled(false);
		objectName.getController().setValue(value.set(name));
		objectDescription.getController().setDisabled(false);
		objectDescription.getController().setValue(value.set(description));
		objectModel.getController().setValue(value.set(modelName));
		objectCenter.getController().setValue(
			value.reset().
			append(Tools.formatFloat(center.getX())).
			append(", ").
			append(Tools.formatFloat(center.getY())).
			append(", ").
			append(Tools.formatFloat(center.getZ()))
		);
		btnObjectDataApply.getController().setDisabled(false);
	}

	/**
	 * Unset model data
	 */
	public void unsetObjectData() {
		objectName.getController().setValue(TEXT_EMPTY);
		objectName.getController().setDisabled(true);
		objectDescription.getController().setValue(TEXT_EMPTY);
		objectDescription.getController().setDisabled(true);
		objectModel.getController().setValue(TEXT_EMPTY);
		objectModel.getController().setDisabled(true);
		objectCenter.getController().setValue(TEXT_EMPTY);
		objectCenter.getController().setDisabled(true);
		btnObjectDataApply.getController().setDisabled(true);
	}

	/**
	 * On object data apply
	 */
	public void onObjectDataApply() {
		if (view.objectDataApply(
				objectName.getController().getValue().toString(), 
				objectDescription.getController().getValue().toString()
			) == false) {
			showErrorPopUp("Warning", "Changing object data failed");
		}
	}

	/**
	 * Set up object list box
	 * @param object id enumerator
	 */
	public void setObjectListbox(Iterator<String> objectIdsIterator) {
		// store selected object ids
		selectedObjects.set(objectsListBox.getController().getValue());

		// model properties list box inner
		GUIParentNode objectsListBoxInnerNode = (GUIParentNode)(objectsListBox.getScreenNode().getNodeById(objectsListBox.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 1;
		String objectsListBoxSubNodesXML = "";
		objectsListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + objectsListBox.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		while (objectIdsIterator.hasNext()) {
			String objectId = objectIdsIterator.next();
			objectsListBoxSubNodesXML+= 
				"<selectbox-multiple-option text=\"" + 
				GUIParser.escapeQuotes(objectId) + 
				"\" value=\"" + 
				GUIParser.escapeQuotes(objectId) + 
				"\" " +
				// (selectedValue != null && modelProperty.getName().equals(selectedValue)?"selected=\"true\" ":"") +
				"/>\n";
		}
		objectsListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			objectsListBoxInnerNode.replaceSubNodes(
				objectsListBoxSubNodesXML,
				false
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// store selected object ids
		objectsListBox.getController().setValue(selectedObjects);
	}

	/**
	 * Unselect objects in object list box
	 */
	public void unselectObjectInObjectListBox(String objectId) {
		// store selected object ids
		selectedObjects.set(objectsListBox.getController().getValue());
		// find and remove object id from value list
		value.set('|').append(objectId).append('|');
		int pos = selectedObjects.indexOf(value);
		if (pos != -1) selectedObjects.delete(pos, value.length());
		// set new value
		objectsListBox.getController().setValue(selectedObjects);
	}

	/**
	 * Unselect objects in object list box
	 */
	public void unselectObjectsInObjectListBox() {
		objectsListBox.getController().setValue(value.reset());
	}

	/**
	 * Select a object in object list box
	 * @param object id
	 */
	public void selectObjectInObjectListbox(String objectId) {
		// store selected object ids
		selectedObjects.set(objectsListBox.getController().getValue());
		// make sure object is not yet selected and add object id if it does not yet exist
		value.set('|').append(objectId).append('|');
		int pos = selectedObjects.indexOf(value);
		if (pos == -1) selectedObjects.append(value);
		objectsListBox.getController().setValue(selectedObjects);
	}

	/**
	 * On objects select button click event
	 */
	public void onObjectsSelect() {
		selectedObjectList.clear();
		StringTokenizer t = new StringTokenizer(objectsListBox.getController().getValue().toString(), "|");
		while (t.hasMoreTokens()) {
			selectedObjectList.add(t.nextToken());
		}
		if (selectedObjectList.isEmpty() == false) view.selectObjects(selectedObjectList);
	}

	/**
	 * On objects unselect button click event
	 */
	public void onObjectsUnselect() {
		view.unselectObjects();
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
		btnObjectTranslationApply.getController().setDisabled(false);
		btnObjectScaleApply.getController().setDisabled(false);
		btnObjectRotationApply.getController().setDisabled(false);
		btnObjectColor.getController().setDisabled(false);
		btnObjectCenter.getController().setDisabled(false);
		btnObjectRemove.getController().setDisabled(false);
		objectTranslationX.getController().setDisabled(false);
		objectTranslationY.getController().setDisabled(false);
		objectTranslationZ.getController().setDisabled(false);
		objectScaleX.getController().setDisabled(false);
		objectScaleY.getController().setDisabled(false);
		objectScaleZ.getController().setDisabled(false);
		objectRotationX.getController().setDisabled(false);
		objectRotationY.getController().setDisabled(false);
		objectRotationZ.getController().setDisabled(false);
		objectTranslationX.getController().setValue(value.set(Tools.formatFloat(translation.getX())));
		objectTranslationY.getController().setValue(value.set(Tools.formatFloat(translation.getY())));
		objectTranslationZ.getController().setValue(value.set(Tools.formatFloat(translation.getZ())));
		objectScaleX.getController().setValue(value.set(Tools.formatFloat(scale.getX())));
		objectScaleY.getController().setValue(value.set(Tools.formatFloat(scale.getY())));
		objectScaleZ.getController().setValue(value.set(Tools.formatFloat(scale.getZ())));
		objectRotationX.getController().setValue(value.set(Tools.formatFloat(rotationX)));
		objectRotationY.getController().setValue(value.set(Tools.formatFloat(rotationY)));
		objectRotationZ.getController().setValue(value.set(Tools.formatFloat(rotationZ)));
	}

	/**
	 * Unset current object
	 */
	public void unsetObject() {
		btnObjectTranslationApply.getController().setDisabled(true);
		btnObjectScaleApply.getController().setDisabled(true);
		btnObjectRotationApply.getController().setDisabled(true);
		btnObjectColor.getController().setDisabled(true);
		btnObjectCenter.getController().setDisabled(true);
		btnObjectRemove.getController().setDisabled(true);
		objectTranslationX.getController().setDisabled(true);
		objectTranslationY.getController().setDisabled(true);
		objectTranslationZ.getController().setDisabled(true);
		objectScaleX.getController().setDisabled(true);
		objectScaleY.getController().setDisabled(true);
		objectScaleZ.getController().setDisabled(true);
		objectRotationX.getController().setDisabled(true);
		objectRotationY.getController().setDisabled(true);
		objectRotationZ.getController().setDisabled(true);
		objectTranslationX.getController().setValue(TEXT_EMPTY);
		objectTranslationY.getController().setValue(TEXT_EMPTY);
		objectTranslationZ.getController().setValue(TEXT_EMPTY);
		objectScaleX.getController().setValue(TEXT_EMPTY);
		objectScaleY.getController().setValue(TEXT_EMPTY);
		objectScaleZ.getController().setValue(TEXT_EMPTY);
		objectRotationX.getController().setValue(TEXT_EMPTY);
		objectRotationY.getController().setValue(TEXT_EMPTY);
		objectRotationZ.getController().setValue(TEXT_EMPTY);
	}

	/**
	 * Event callback for map properties selection
	 * @param id
	 * @param event
	 */
	public void onMapPropertiesSelectionChanged() {
		mapPropertyName.getController().setDisabled(true);
		mapPropertyName.getController().setValue(TEXT_EMPTY);
		mapPropertyValue.getController().setDisabled(true);
		mapPropertyValue.getController().setValue(TEXT_EMPTY);
		mapPropertySave.getController().setDisabled(true);
		mapPropertyRemove.getController().setDisabled(true);
		PropertyModelClass mapProperty = view.getLevel().getProperty(mapPropertiesListBox.getController().getValue().toString());
		if (mapProperty != null) {
			mapPropertyName.getController().setValue(value.set(mapProperty.getName()));
			mapPropertyValue.getController().setValue(value.set(mapProperty.getValue()));
			mapPropertyName.getController().setDisabled(false);
			mapPropertyValue.getController().setDisabled(false);
			mapPropertySave.getController().setDisabled(false);
			mapPropertyRemove.getController().setDisabled(false);
		}
	}

	/**
	 * Set up map properties
	 * @param map properties
	 */
	public void setMapProperties(Iterable<PropertyModelClass> mapProperties, String selectedName) {
		//
		mapPropertyName.getController().setDisabled(true);
		mapPropertyValue.getController().setDisabled(true);
		mapPropertySave.getController().setDisabled(true);

		// map properties list box inner
		GUIParentNode mapPropertiesListBoxInnerNode = (GUIParentNode)(mapPropertiesListBox.getScreenNode().getNodeById(mapPropertiesListBox.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 1;
		String mapPropertiesListBoxSubNodesXML = "";
		mapPropertiesListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + mapPropertiesListBox.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (PropertyModelClass mapProperty: mapProperties) {
			mapPropertiesListBoxSubNodesXML+=
				"<selectbox-option text=\"" +
				GUIParser.escapeQuotes(mapProperty.getName()) +
				": " +
				GUIParser.escapeQuotes(mapProperty.getValue()) +
				"\" value=\"" +
				GUIParser.escapeQuotes(mapProperty.getName()) +
				"\" " +
				(selectedName != null && mapProperty.getName().equals(selectedName)?"selected=\"true\" ":"") +
				"/>\n";
		}
		mapPropertiesListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			mapPropertiesListBoxInnerNode.replaceSubNodes(
				mapPropertiesListBoxSubNodesXML,
				false
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		onMapPropertiesSelectionChanged();
	}

	/**
	 * On map property save
	 */
	public void onMapPropertySave() {
		if (view.mapPropertySave(
			mapPropertiesListBox.getController().getValue().toString(),
			mapPropertyName.getController().getValue().toString(),
			mapPropertyValue.getController().getValue().toString()) == false) {
			//
			showErrorPopUp("Warning", "Saving map property failed");
		}
	}

	/**
	 * On model property add
	 */
	public void onMapPropertyAdd() {
		if (view.mapPropertyAdd() == false) {
			showErrorPopUp("Warning", "Adding new map property failed");
		}
	}

	/**
	 * On model property remove
	 */
	public void onMapPropertyRemove() {
		if (view.mapPropertyRemove(mapPropertiesListBox.getController().getValue().toString()) == false) {
			showErrorPopUp("Warning", "Removing map property failed");
		}
	}

	/**
	 * Set up object property preset ids
	 * @param object property preset ids
	 */
	public void setObjectPresetIds(Collection<String> objectPresetIds) {
		// model properties presets inner
		GUIParentNode objectPropertiesPresetsInnerNode = (GUIParentNode)(objectPropertiesPresets.getScreenNode().getNodeById(objectPropertiesPresets.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 0;
		String objectPropertiesPresetsInnerNodeSubNodesXML = "";
		objectPropertiesPresetsInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + objectPropertiesPresets.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100\">\n";
		for (String modelPresetId: objectPresetIds) {
			objectPropertiesPresetsInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(modelPresetId) + "\" value=\"" + GUIParser.escapeQuotes(modelPresetId) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
			idx++;
		}
		objectPropertiesPresetsInnerNodeSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			objectPropertiesPresetsInnerNode.replaceSubNodes(
				objectPropertiesPresetsInnerNodeSubNodesXML,
				true
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Event callback for object properties selection
	 * @param id
	 * @param event
	 */
	public void onObjectPropertiesSelectionChanged() {
		//
		objectPropertyName.getController().setDisabled(true);
		objectPropertyName.getController().setValue(TEXT_EMPTY);
		objectPropertyValue.getController().setDisabled(true);
		objectPropertyValue.getController().setValue(TEXT_EMPTY);
		btnObjectPropertySave.getController().setDisabled(true);
		btnObjectPropertyRemove.getController().setDisabled(true);

		// get selected level editor object
		LevelEditorObject levelEditorObject = view.getSelectedObject();
		if (levelEditorObject == null) return;

		// get model property
		PropertyModelClass modelProperty = levelEditorObject.getProperty(objectPropertiesListBox.getController().getValue().toString());
		if (modelProperty != null) {
			objectPropertyName.getController().setValue(value.set(modelProperty.getName()));
			objectPropertyValue.getController().setValue(value.set(modelProperty.getValue()));
			objectPropertyName.getController().setDisabled(false);
			objectPropertyValue.getController().setDisabled(false);
			btnObjectPropertySave.getController().setDisabled(false);
			btnObjectPropertyRemove.getController().setDisabled(false);
		}
	}

	/**
	 * Set up object properties
	 * @param preset id
	 * @param object properties
	 * @param selected name
	 */
	public void setObjectProperties(String presetId, Iterable<PropertyModelClass> objectProperties, String selectedName) {
		objectPropertiesPresets.getController().setDisabled(false);
		btnObjectPropertyPresetApply.getController().setDisabled(false);
		objectPropertiesListBox.getController().setDisabled(false);
		btnObjectPropertyAdd.getController().setDisabled(false);
		btnObjectPropertyRemove.getController().setDisabled(false);
		btnObjectPropertySave.getController().setDisabled(true);
		objectPropertyName.getController().setDisabled(true);
		objectPropertyValue.getController().setDisabled(true);

		// set up preset
		objectPropertiesPresets.getController().setValue(presetId != null?value.set(presetId):value.set("none"));

		// object properties list box inner
		GUIParentNode objectPropertiesListBoxInnerNode = (GUIParentNode)(objectPropertiesListBox.getScreenNode().getNodeById(objectPropertiesListBox.getId() + "_inner"));

		// construct XML for sub nodes
		int idx = 1;
		String objectPropertiesListBoxSubNodesXML = "";
		objectPropertiesListBoxSubNodesXML+= "<scrollarea-vertical id=\"" + objectPropertiesListBox.getId() + "_inner_scrollarea\" width=\"100%\" height=\"100%\">\n";
		for (PropertyModelClass objectProperty: objectProperties) {
			objectPropertiesListBoxSubNodesXML+= 
				"<selectbox-option text=\"" + 
				GUIParser.escapeQuotes(objectProperty.getName()) + 
				": " + 
				GUIParser.escapeQuotes(objectProperty.getValue()) + 
				"\" value=\"" + 
				GUIParser.escapeQuotes(objectProperty.getName()) + 
				"\" " +
				(selectedName != null && objectProperty.getName().equals(selectedName)?"selected=\"true\" ":"") +
				"/>\n";
		}
		objectPropertiesListBoxSubNodesXML+= "</scrollarea-vertical>\n";

		// inject sub nodes
		try {
			objectPropertiesListBoxInnerNode.replaceSubNodes(
				objectPropertiesListBoxSubNodesXML,
				false
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		onObjectPropertiesSelectionChanged();
	}

	/**
	 * On object property save
	 */
	public void onObjectPropertySave() {
		if (view.objectPropertySave(
			objectPropertiesListBox.getController().getValue().toString(),
			objectPropertyName.getController().getValue().toString(),
			objectPropertyValue.getController().getValue().toString()) == false) {
			//
			showErrorPopUp("Warning", "Saving object property failed");
		}
	}

	/**
	 * On object property add
	 */
	public void onObjectPropertyAdd() {
		if (view.objectPropertyAdd() == false) {
			showErrorPopUp("Warning", "Adding new object property failed");
		}
	}

	/**
	 * On object property remove
	 */
	public void onObjectPropertyRemove() {
		if (view.objectPropertyRemove(objectPropertiesListBox.getController().getValue().toString()) == false) {
			showErrorPopUp("Warning", "Removing object property failed");
		}
	}

	/**
	 * On quit action
	 */
	public void onQuit() {
		TDMELevelEditor.getInstance().quit();
	}

	/**
	 * On object translation apply action
	 */
	public void onObjectTranslationApply() {
		try {
			float x = Float.parseFloat(objectTranslationX.getController().getValue().toString());
			float y = Float.parseFloat(objectTranslationY.getController().getValue().toString());
			float z = Float.parseFloat(objectTranslationZ.getController().getValue().toString());
			view.objectTranslationApply(x, y, z);
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
			float x = Float.parseFloat(objectScaleX.getController().getValue().toString());
			float y = Float.parseFloat(objectScaleY.getController().getValue().toString());
			float z = Float.parseFloat(objectScaleZ.getController().getValue().toString());
			if (x < -10f || x > 10f) throw new IllegalArgumentException("x scale must be within -10 .. +10");
			if (y < -10f || y > 10f) throw new IllegalArgumentException("y scale must be within -10 .. +10");
			if (z < -10f || z > 10f) throw new IllegalArgumentException("z scale must be within -10 .. +10");
			view.objectScaleApply(x, y, z);
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
			float x = Float.parseFloat(objectRotationX.getController().getValue().toString());
			float y = Float.parseFloat(objectRotationY.getController().getValue().toString());
			float z = Float.parseFloat(objectRotationZ.getController().getValue().toString());
			if (x < -360f || x > 360f) throw new IllegalArgumentException("x axis rotation must be within -360 .. +360");
			if (y < -360f || y > 360f) throw new IllegalArgumentException("y axis rotation must be within -360 .. +360");
			if (z < -360f || z > 360f) throw new IllegalArgumentException("z axis rotation must be within -360 .. +360");
			view.objectRotationsApply(x, y, z);
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
		view.removeObject();
	}

	/**
	 * On object color action
	 */
	public void onObjectColor() {
		view.colorObject();
	}

	/**
	 * On object center action
	 */
	public void onObjectCenter() {
		view.centerObject();
	}

	/**
	 * On map load action
	 */
	public void onMapLoad() {
		view.getPopUps().getFileDialogScreenController().show(
			mapPath,
			"Load from: ", 
			new String[]{"tl", "dae"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.loadMap(
						view.getPopUps().getFileDialogScreenController().getPathName(),
						view.getPopUps().getFileDialogScreenController().getFileName()
					);
					mapPath = view.getPopUps().getFileDialogScreenController().getPathName();
					view.getPopUps().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On map save action
	 */
	public void onMapSave() {
		view.getPopUps().getFileDialogScreenController().show(
			mapPath,
			"Save to: ", 
			new String[]{"tl"},
			view.getFileName(),
			new Action() {
				public void performAction() {
					view.saveMap(
						view.getPopUps().getFileDialogScreenController().getPathName(),
						view.getPopUps().getFileDialogScreenController().getFileName()
					);
					mapPath = view.getPopUps().getFileDialogScreenController().getPathName();
					view.getPopUps().getFileDialogScreenController().close();
				}
				
			}
		);
	}

	/**
	 * On grid apply button
	 */
	public void onGridApply() {
		try {
			float gridY = Float.parseFloat(gridYPosition.getController().getValue().toString());
			if (gridY < -5f || gridY > 5f) throw new IllegalArgumentException("grid y position must be within -5 .. +5");
			view.setGridY(gridY);
			view.setGridEnabled(gridEnabled.getController().getValue().equals(CHECKBOX_CHECKED));
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
		view.objectPropertiesPreset(objectPropertiesPresets.getController().getValue().toString());
	}

	/**
	 * Set up light presets
	 * @param light presets
	 */
	public void setLightPresetsIds(Collection<String> lightPresetIds) {
		for (int i = 0; i < 4; i++) {
			// model properties presets inner
			GUIParentNode lightPresetsInnerNode = (GUIParentNode)(lightsPresets[i].getScreenNode().getNodeById(lightsPresets[i].getId() + "_inner"));

			// construct XML for sub nodes
			int idx = 0;
			String lightPresetsInnerNodeSubNodesXML = "";
			lightPresetsInnerNodeSubNodesXML+= "<scrollarea-vertical id=\"" + lightsPresets[i].getId() + "_inner_scrollarea\" width=\"100%\" height=\"50\">\n";
			for (String lightPresetId: lightPresetIds) {
				lightPresetsInnerNodeSubNodesXML+= "<dropdown-option text=\"" + GUIParser.escapeQuotes(lightPresetId) + "\" value=\"" + GUIParser.escapeQuotes(lightPresetId) + "\" " + (idx == 0?"selected=\"true\" ":"")+ " />\n";
				idx++;
			}
			lightPresetsInnerNodeSubNodesXML+= "</scrollarea-vertical>\n";

			// inject sub nodes
			try {
				lightPresetsInnerNode.replaceSubNodes(
					lightPresetsInnerNodeSubNodesXML,
					true
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Unselect light presets
	 */
	public void unselectLightPresets() {
		// TODO: unselect light presets
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
		lightsAmbient[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(ambient.getRed())).
			append(", ").
			append(Tools.formatFloat(ambient.getGreen())).
			append(", ").
			append(Tools.formatFloat(ambient.getBlue())).
			append(", ").
			append(Tools.formatFloat(ambient.getAlpha()))
		);
		lightsDiffuse[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(diffuse.getRed())).
			append(", ").
			append(Tools.formatFloat(diffuse.getGreen())).
			append(", ").
			append(Tools.formatFloat(diffuse.getBlue())).
			append(", ").
			append(Tools.formatFloat(diffuse.getAlpha()))
		);
		lightsSpecular[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(specular.getRed())).
			append(", ").
			append(Tools.formatFloat(specular.getGreen())).
			append(", ").
			append(Tools.formatFloat(specular.getBlue())).
			append(", ").
			append(Tools.formatFloat(specular.getAlpha()))
		);
		lightsPosition[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(position.getX())).
			append(", ").
			append(Tools.formatFloat(position.getY())).
			append(", ").
			append(Tools.formatFloat(position.getZ())).
			append(", ").
			append(Tools.formatFloat(position.getW()))
		);
		lightsConstAttenuation[i].getController().setValue(value.set(Tools.formatFloat(constAttenuation)));
		lightsLinAttenuation[i].getController().setValue(value.set(Tools.formatFloat(linearAttenuation)));
		lightsQuadAttenuation[i].getController().setValue(value.set(Tools.formatFloat(quadraticAttenuation)));
		lightsSpotTo[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(spotTo.getX())).
			append(", ").
			append(Tools.formatFloat(spotTo.getY())).
			append(", ").
			append(Tools.formatFloat(spotTo.getZ()))
		);
		lightsSpotDirection[i].getController().setValue(
			value.reset().
			append(Tools.formatFloat(spotDirection.getX())).
			append(", ").
			append(Tools.formatFloat(spotDirection.getY())).
			append(", ").
			append(Tools.formatFloat(spotDirection.getZ()))
		);
		lightsSpotExponent[i].getController().setValue(value.set(Tools.formatFloat(spotExponent)));
		lightsSpotCutoff[i].getController().setValue(value.set(Tools.formatFloat(spotCutoff)));
		lightsEnabled[i].getController().setValue(enabled == true?CHECKBOX_CHECKED:CHECKBOX_UNCHECKED);
		lightsAmbient[i].getController().setDisabled(enabled == false);
		lightsDiffuse[i].getController().setDisabled(enabled == false);
		lightsSpecular[i].getController().setDisabled(enabled == false);
		lightsPosition[i].getController().setDisabled(enabled == false);
		lightsConstAttenuation[i].getController().setDisabled(enabled == false);
		lightsLinAttenuation[i].getController().setDisabled(enabled == false);
		lightsQuadAttenuation[i].getController().setDisabled(enabled == false);
		lightsSpotTo[i].getController().setDisabled(enabled == false);
		lightsSpotDirection[i].getController().setDisabled(enabled == false);
		lightsSpotExponent[i].getController().setDisabled(enabled == false);
		lightsSpotCutoff[i].getController().setDisabled(enabled == false);
		ligthsSpotDirectionCompute[i].getController().setDisabled(enabled == false);
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
			boolean enabled = lightsEnabled[lightIdx].getController().getValue().equals(CHECKBOX_CHECKED);
			view.applyLight(
				lightIdx,
				Tools.convertToColor4(lightsAmbient[lightIdx].getController().getValue().toString()),
				Tools.convertToColor4(lightsDiffuse[lightIdx].getController().getValue().toString()),
				Tools.convertToColor4(lightsSpecular[lightIdx].getController().getValue().toString()),
				Tools.convertToVector4(lightsPosition[lightIdx].getController().getValue().toString()),
				Tools.convertToFloat(lightsConstAttenuation[lightIdx].getController().getValue().toString()),
				Tools.convertToFloat(lightsLinAttenuation[lightIdx].getController().getValue().toString()),
				Tools.convertToFloat(lightsQuadAttenuation[lightIdx].getController().getValue().toString()),
				Tools.convertToVector3(lightsSpotTo[lightIdx].getController().getValue().toString()),
				Tools.convertToVector3(lightsSpotDirection[lightIdx].getController().getValue().toString()),
				Tools.convertToFloat(lightsSpotExponent[lightIdx].getController().getValue().toString()),
				Tools.convertToFloat(lightsSpotCutoff[lightIdx].getController().getValue().toString()),
				enabled
			);
			lightsAmbient[lightIdx].getController().setDisabled(enabled == false);
			lightsDiffuse[lightIdx].getController().setDisabled(enabled == false);
			lightsSpecular[lightIdx].getController().setDisabled(enabled == false);
			lightsPosition[lightIdx].getController().setDisabled(enabled == false);
			lightsConstAttenuation[lightIdx].getController().setDisabled(enabled == false);
			lightsLinAttenuation[lightIdx].getController().setDisabled(enabled == false);
			lightsQuadAttenuation[lightIdx].getController().setDisabled(enabled == false);
			lightsSpotTo[lightIdx].getController().setDisabled(enabled == false);
			lightsSpotDirection[lightIdx].getController().setDisabled(enabled == false);
			lightsSpotExponent[lightIdx].getController().setDisabled(enabled == false);
			lightsSpotCutoff[lightIdx].getController().setDisabled(enabled == false);
			ligthsSpotDirectionCompute[lightIdx].getController().setDisabled(enabled == false);
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
		LevelEditorLight lightPreset = LevelPropertyPresets.getInstance().getLightPresets().get(lightsPresets[lightIdx].getController().getValue().toString());
		if (lightPreset == null) return;

		// apply preset
		view.applyLight(
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
			view.computeSpotDirection(
				lightIdx,
				Tools.convertToVector4(lightsPosition[lightIdx].getController().getValue().toString()),
				Tools.convertToVector3(lightsSpotTo[lightIdx].getController().getValue().toString())
			);
		} catch (NumberFormatException nfe) {
			showErrorPopUp("Warning", "Invalid number entered");
		}
	}

	/*
	 * 
	 */
	public void saveFile(String pathName, String fileName) throws Exception {
		view.saveMap(
			pathName,
			fileName
		);
	}

	/*
	 * 
	 */
	public void loadFile(String pathName, String fileName) throws Exception {
		view.loadMap(
			pathName,
			fileName
		);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIChangeListener#onValueChanged(net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onValueChanged(GUIElementNode node) {
		if (node.getId().equals("objects_listbox") == true) {
			// no op
		} else 
		if (node.getId().equals("map_properties_listbox") == true) {
			onMapPropertiesSelectionChanged();
		} else 
		if (node.getId().equals("object_properties_listbox") == true) {
			onObjectPropertiesSelectionChanged();
		} else {
			System.out.println("LevelEditorScreenController::onValueChanged: " + node.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIActionListener#onActionPerformed(net.drewke.tdme.gui.events.GUIActionListener.Type, net.drewke.tdme.gui.nodes.GUIElementNode)
	 */
	public void onActionPerformed(Type type, GUIElementNode node) {
		if (type == Type.PERFORMED) {
			if (node.getId().equals("button_objects_select") == true) {
				onObjectsSelect();
			} else
			if (node.getId().equals("button_objects_unselect") == true) {
				onObjectsUnselect();
			} else
			if (node.getId().equals("button_grid_apply") == true) {
				onGridApply();
			} else
			if (node.getId().equals("button_map_load") == true) {
				onMapLoad();
			} else
			if (node.getId().equals("button_map_save") == true) {
				onMapSave();
			} else
			if (node.getId().equals("button_map_properties_add") == true) {
				onMapPropertyAdd();
			} else
			if (node.getId().equals("button_map_properties_remove") == true) {
				onMapPropertyRemove();
			} else
			if (node.getId().equals("button_map_properties_save") == true) {
				onMapPropertySave();
			} else
			if (node.getId().equals("button_objectdata_apply") == true) {
				onObjectDataApply();
			} else
			if (node.getId().equals("button_translation_apply") == true) {
				onObjectTranslationApply();
			} else
			if (node.getId().equals("button_scale_apply") == true) {
				onObjectScaleApply();
			} else
			if (node.getId().equals("button_rotation_apply") == true) {
				onObjectRotationsApply();;
			} else
			if (node.getId().equals("button_object_color") == true) {
				onObjectColor();
			} else
			if (node.getId().equals("button_object_center") == true) {
				onObjectCenter();
			} else
			if (node.getId().equals("button_object_remove") == true) {
				onObjectRemove();
			} else
			if (node.getId().equals("button_object_properties_presetapply") == true) {
				onObjectPropertyPresetApply();
			} else
			if (node.getId().equals("button_object_properties_add") == true) {
				onObjectPropertyAdd();
			} else
			if (node.getId().equals("button_object_properties_remove") == true) {
				onObjectPropertyRemove();
			} else
			if (node.getId().equals("button_object_properties_save") == true) {
				onObjectPropertySave();
			} else
			if (node.getId().equals("button_light0_presetapply") == true) {
				onLight0PresetApply();
			} else
			if (node.getId().equals("button_light0_spotdirection_compute") == true) {
				onLight0SpotDirectionCompute();
			} else
			if (node.getId().equals("button_light0_apply") == true) {
				onLight0Apply();
			} else
			if (node.getId().equals("button_light1_presetapply") == true) {
				onLight1PresetApply();
			} else
			if (node.getId().equals("button_light1_spotdirection_compute") == true) {
				onLight1SpotDirectionCompute();
			} else
			if (node.getId().equals("button_light1_apply") == true) {
				onLight1Apply();
			} else
			if (node.getId().equals("button_light2_presetapply") == true) {
				onLight2PresetApply();
			} else
			if (node.getId().equals("button_light2_spotdirection_compute") == true) {
				onLight2SpotDirectionCompute();
			} else
			if (node.getId().equals("button_light2_apply") == true) {
				onLight2Apply();
			} else
			if (node.getId().equals("button_light3_presetapply") == true) {
				onLight3PresetApply();
			} else
			if (node.getId().equals("button_light3_spotdirection_compute") == true) {
				onLight3SpotDirectionCompute();
			} else
			if (node.getId().equals("button_light3_apply") == true) {
				onLight3Apply();
			} else {
				System.out.println("LevelEditorScreenController::onActionPerformed: " + node.getId());
			}
		}
	}

	/**
	 * Shows the error pop up
	 */
	public void showErrorPopUp(String caption, String message) {
		System.out.println(caption + ":" + message);
		view.getPopUps().getInfoDialogScreenController().show(caption, message);
	}

}
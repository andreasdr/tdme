package net.drewke.tdme.tools.shared.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.shared.tools.Tools;
import net.drewke.tdme.utils.Console;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Level Property Presets
 * @author Andreas Drewke
 * @version $Id$
 */
public final class LevelPropertyPresets {

	private ArrayList<PropertyModelClass> mapPropertiesPreset;
	private HashMap<String, ArrayList<PropertyModelClass>> objectPropertiesPresets;
	private HashMap<String, LevelEditorLight> lightPresets;
	private static LevelPropertyPresets instance = null;

	/**
	 * @return level editor presets instance
	 */
	public static LevelPropertyPresets getInstance() {
		if (instance == null) {
			try {
				instance = new LevelPropertyPresets("resources/tools/leveleditor/gd", "presets.xml");
			} catch (Exception exception) {
				Console.println("Couldn't load level property presets: " + exception.getMessage());
			}
		}
		return instance;
	}

	/**
	 * Set default level properties  
	 * @param level
	 */
	public void setDefaultLevelProperties(LevelEditorLevel level) {
		// init level default map properties
		for (PropertyModelClass mapProperty: getMapPropertiesPreset()) {
			level.addProperty(mapProperty.getName(), mapProperty.getValue());
		}
	}

	/**
	 * Constructor
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public LevelPropertyPresets(String pathName, String fileName) throws Exception {
		mapPropertiesPreset = new ArrayList<PropertyModelClass>();
		objectPropertiesPresets = new HashMap<String, ArrayList<PropertyModelClass>>();
		lightPresets = new HashMap<String, LevelEditorLight>();
		// load preset xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();
		for (Element xmlMap: getChildrenByTagName(xmlRoot, "map"))
		for (Element xmlProperty: getChildrenByTagName(xmlMap, "property")) {
			mapPropertiesPreset.add(
				new PropertyModelClass(
					xmlProperty.getAttribute("name"),
					xmlProperty.getAttribute("value")
				)
			);
		}
		for (Element xmlObject: getChildrenByTagName(xmlRoot, "object"))
		for (Element xmlType: getChildrenByTagName(xmlObject, "type")) {
			String typeId = xmlType.getAttribute("id");
			ArrayList<PropertyModelClass> objectPropertiesPreset = new ArrayList<PropertyModelClass>();
			objectPropertiesPresets.put(typeId, objectPropertiesPreset);
			objectPropertiesPreset.add(
				new PropertyModelClass(
					"preset",
					typeId
				)
			);
			for (Element xmlProperty: getChildrenByTagName(xmlType, "property")) {
				objectPropertiesPreset.add(
					new PropertyModelClass(
						xmlProperty.getAttribute("name"),
						xmlProperty.getAttribute("value")
					)
				);
			}
		}
		int lightId = 0;
		for (Element xmlLights: getChildrenByTagName(xmlRoot, "lights"))
		for (Element xmlType: getChildrenByTagName(xmlLights, "type")) {
			String typeId = xmlType.getAttribute("id");
			LevelEditorLight light = new LevelEditorLight(lightId++);
			// set up light in level
			light.getAmbient().set(
				Tools.convertToColor4(getChildrenByTagName(xmlType, "ambient").get(0).getTextContent())
			);
			light.getDiffuse().set(
				Tools.convertToColor4(getChildrenByTagName(xmlType, "diffuse").get(0).getTextContent())
			);
			light.getSpecular().set(
				Tools.convertToColor4(getChildrenByTagName(xmlType, "specular").get(0).getTextContent())
			);
			light.getPosition().set(
				Tools.convertToVector4(getChildrenByTagName(xmlType, "position").get(0).getTextContent())
			);
			light.setConstantAttenuation(
				Tools.convertToFloat(getChildrenByTagName(xmlType, "constant_attenuation").get(0).getTextContent())
			);
			light.setLinearAttenuation(
				Tools.convertToFloat(getChildrenByTagName(xmlType, "linear_attenuation").get(0).getTextContent())
			);
			light.setQuadraticAttenuation(
				Tools.convertToFloat(getChildrenByTagName(xmlType, "quadratic_attenuation").get(0).getTextContent())
			);
			light.getSpotTo().set(
				Tools.convertToVector3(getChildrenByTagName(xmlType, "spot_to").get(0).getTextContent())
			);
			light.getSpotDirection().set(
				Tools.convertToVector3(getChildrenByTagName(xmlType, "spot_direction").get(0).getTextContent())
			);
			light.setSpotExponent(
				Tools.convertToFloat(getChildrenByTagName(xmlType, "spot_exponent").get(0).getTextContent())
			);
			light.setSpotCutOff(
				Tools.convertToFloat(getChildrenByTagName(xmlType, "spot_cutoff").get(0).getTextContent())
			);
			light.setEnabled(true);

			//
			lightPresets.put(typeId, light);
		}
		Console.println(lightPresets);
	}

	/**
	 * @return map properties preset
	 */
	public ArrayList<PropertyModelClass> getMapPropertiesPreset() {
		return mapPropertiesPreset;
	}

	/**
	 * @return object property presets
	 */
	public HashMap<String, ArrayList<PropertyModelClass>> getObjectPropertiesPresets() {
		return objectPropertiesPresets;
	}

	/**
	 * @return light presets
	 */
	public HashMap<String, LevelEditorLight> getLightPresets() {
		return lightPresets;
	}

	/**
	 * Returns immediate children by tagnames of parent
	 * @param parent
	 * @param name
	 * @return children with given name
	 */
	private static List<Element> getChildrenByTagName(Element parent, String name) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}
		return nodeList;
	}

}
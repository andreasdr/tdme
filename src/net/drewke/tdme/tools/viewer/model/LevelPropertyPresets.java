package net.drewke.tdme.tools.viewer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.drewke.tdme.os.FileSystem;

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

	private HashMap<String, ArrayList<PropertyModelClass>> objectPropertiesPresets;
	private static LevelPropertyPresets instance = null;

	/**
	 * @return level editor presets instance
	 */
	public static LevelPropertyPresets getInstance() {
		if (instance == null) {
			try {
				instance = new LevelPropertyPresets("resources/tools/viewer/gd", "presets.xml");
			} catch (Exception exception) {
				System.out.println("Couldn't load level property presets: " + exception.getMessage());
			}
		}
		return instance;
	}

	/**
	 * Constructor
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public LevelPropertyPresets(String pathName, String fileName) throws Exception {
		objectPropertiesPresets = new HashMap<String, ArrayList<PropertyModelClass>>();
		// load preset xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();
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
	}

	/**
	 * @return object property presets
	 */
	public HashMap<String, ArrayList<PropertyModelClass>> getObjectPropertiesPresets() {
		return objectPropertiesPresets;
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

package net.drewke.tdme.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.drewke.tdme.gui.elements.GUICheckbox;
import net.drewke.tdme.gui.elements.GUIElement;
import net.drewke.tdme.gui.elements.GUIRadioButton;
import net.drewke.tdme.gui.elements.GUISelectBox;
import net.drewke.tdme.gui.elements.GUISelectBoxOption;
import net.drewke.tdme.gui.nodes.GUIColor;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIImageNode;
import net.drewke.tdme.gui.nodes.GUILayoutNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIPanelNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUISpaceNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * GUI parser 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIParser {

	public static HashMap<String, GUIElement> elements;

	/**
	 * Parses a GUI XML file
	 * @param pathName
	 * @param fileName
	 * @return Model instance
	 * @throws IOException
	 */
	public static GUIScreenNode parse(String pathName, String fileName) throws Exception {
		// load GUI xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();

		// create GUI main / screen node
		GUIScreenNode guiScreenNode = null;
		if (xmlRoot.getNodeName().equals("screen") == false) {
			throw new GUIParserException("XML root node must be <screen>");
		}
		guiScreenNode = new GUIScreenNode(
			null, 
			xmlRoot.getAttribute("id"), 
			GUINode.createAlignments(
				xmlRoot.getAttribute("horizontal-align"), 
				xmlRoot.getAttribute("vertical-align")							
			),
			GUINode.createRequestedConstraints(
				xmlRoot.getAttribute("left"), 
				xmlRoot.getAttribute("top"), 
				xmlRoot.getAttribute("width"), 
				xmlRoot.getAttribute("height")
			),
			GUINode.getRequestedColor(xmlRoot.getAttribute("background-color"), GUIColor.TRANSPARENT),
			GUIParentNode.createBorder(
				xmlRoot.getAttribute("border"), 
				xmlRoot.getAttribute("border-left"), 
				xmlRoot.getAttribute("border-top"), 
				xmlRoot.getAttribute("border-right"), 
				xmlRoot.getAttribute("border-bottom"), 
				xmlRoot.getAttribute("border-color"),
				xmlRoot.getAttribute("border-color-left"), 
				xmlRoot.getAttribute("border-color-top"), 
				xmlRoot.getAttribute("border-color-right"), 
				xmlRoot.getAttribute("border-color-bottom")
			),
			GUIParentNode.createPadding(
				xmlRoot.getAttribute("padding"), 
				xmlRoot.getAttribute("padding-left"), 
				xmlRoot.getAttribute("padding-top"), 
				xmlRoot.getAttribute("padding-right"), 
				xmlRoot.getAttribute("padding-bottom") 
			),
			GUINode.createConditions(xmlRoot.getAttribute("show-on")),
			GUINode.createConditions(xmlRoot.getAttribute("hide-on"))
		);

		// parse GUI nodes
		parseGUINode(guiScreenNode, guiScreenNode, xmlRoot, null);

		//
		return guiScreenNode;
	}

	/**
	 * Parse node
	 * @param gui parent node
	 * @param xml parent node
	 */
	protected static void parseGUINode(GUIScreenNode guiScreenNode, GUIParentNode guiParentNode, Element xmlParentNode, GUIElement guiElement) throws Exception {
		//
		GUINodeController guiElementController = null;
		boolean guiElementControllerInstalled = false;
		// parse sub nodes
		for (Element node: getChildrenTags(xmlParentNode)) {
			if (node.getNodeName().equals("layout")) {
				// TODO: validate root node
				GUILayoutNode guiLayoutNode = new GUILayoutNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUIParentNode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					node.getAttribute("alignment")
				);
				guiParentNode.getSubNodes().add(guiLayoutNode);
				if (guiScreenNode.addNode(guiLayoutNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiLayoutNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiLayoutNode.setController(guiElementController = guiElement.createController(guiLayoutNode));
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiScreenNode, guiLayoutNode, node, null);	
			} else
			if (node.getNodeName().equals("space")) {
				// TODO: validate root node
				GUISpaceNode guiSpaceNode = new GUISpaceNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUISpaceNode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on"))
				);
				guiParentNode.getSubNodes().add(guiSpaceNode);
				if (guiScreenNode.addNode(guiSpaceNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiSpaceNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiSpaceNode.setController(guiElementController = guiElement.createController(guiSpaceNode));
					guiElementControllerInstalled = true;
				}
			} else
			if (node.getNodeName().equals("panel")) {
				// TODO: validate root node
				GUIPanelNode guiPanelNode = new GUIPanelNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUIParentNode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					node.getAttribute("alignment")
				);
				guiParentNode.getSubNodes().add(guiPanelNode);
				if (guiScreenNode.addNode(guiPanelNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiPanelNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiPanelNode.setController(guiElementController = guiElement.createController(guiPanelNode));
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiScreenNode, guiPanelNode, node, null);	
			} else
			if (node.getNodeName().equals("element")) {
				// TODO: validate root node
				GUIElementNode guiElementNode = new GUIElementNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUINode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					node.getAttribute("name")
				);
				guiParentNode.getSubNodes().add(guiElementNode);
				if (guiScreenNode.addNode(guiElementNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiElementNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementNode.setController(guiElementController = guiElement.createController(guiElementNode));
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiScreenNode, guiElementNode, node, null);	
			} else
			if (node.getNodeName().equals("image")) {
				// TODO: validate root node
				GUIImageNode guiImageNode = new GUIImageNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUINode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					node.getAttribute("src"),
					GUINode.getRequestedColor(node.getAttribute("effect-color-mul"), GUIColor.EFFECT_COLOR_MUL),
					GUINode.getRequestedColor(node.getAttribute("effect-color-add"), GUIColor.EFFECT_COLOR_ADD)
				);
				guiParentNode.getSubNodes().add(guiImageNode);
				if (guiScreenNode.addNode(guiImageNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiImageNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiImageNode.setController(guiElementController = guiElement.createController(guiImageNode));
					guiElementControllerInstalled = true;
				}
			} else
			if (node.getNodeName().equals("text")) {
				// TODO: validate root node
				GUITextNode guiTextNode = new GUITextNode(
					guiScreenNode,
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createAlignments(
						node.getAttribute("horizontal-align"), 
						node.getAttribute("vertical-align")							
					),
					GUINode.createRequestedConstraints(
						node.getAttribute("left"), 
						node.getAttribute("top"), 
						node.getAttribute("width"), 
						node.getAttribute("height")
					),
					GUINode.getRequestedColor(node.getAttribute("background-color"), GUIColor.TRANSPARENT),
					GUIParentNode.createBorder(
						node.getAttribute("border"), 
						node.getAttribute("border-left"), 
						node.getAttribute("border-top"), 
						node.getAttribute("border-right"), 
						node.getAttribute("border-bottom"), 
						node.getAttribute("border-color"),
						node.getAttribute("border-color-left"), 
						node.getAttribute("border-color-top"), 
						node.getAttribute("border-color-right"), 
						node.getAttribute("border-color-bottom")
					),
					GUIParentNode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					node.getAttribute("font"),
					node.getAttribute("color"),
					node.getAttribute("text")
				);
				guiParentNode.getSubNodes().add(guiTextNode);
				if (guiScreenNode.addNode(guiTextNode) == false) {
					throw new GUIParserException("Screen '" + guiScreenNode.getId() + "' already has a node attached with given node id '" + guiTextNode.getId() + "'");
				}
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiTextNode.setController(guiElementController = guiElement.createController(guiTextNode));
					guiElementControllerInstalled = true;
				}
			} else {
				// Try to load from GUI elements
				GUIElement newGuiElement = elements.get(node.getNodeName());
				if (newGuiElement == null) {
					throw new GUIParserException("Unknown element '" + node.getNodeName() + "'");
				}

				// create final template, replace attributes
				String newGuiElementTemplate = newGuiElement.getTemplate();
				HashMap<String, String> newGuiElementAttributes = newGuiElement.getAttributes();
				for (int i = 0; i < node.getAttributes().getLength(); i++) {
					Node attribute = node.getAttributes().item(i);
					newGuiElementAttributes.put(attribute.getNodeName(), attribute.getNodeValue());
				}
				for (String newGuiElementAttributeKey: newGuiElementAttributes.getKeysIterator()) {
					String guiElementAttributeValue = newGuiElementAttributes.get(newGuiElementAttributeKey);
					newGuiElementTemplate = newGuiElementTemplate.replace("{$" + newGuiElementAttributeKey + "}", guiElementAttributeValue);
				}
				newGuiElementTemplate = newGuiElementTemplate.replace("{$innerXml}", getInnerXml(node));

				// create xml document and parse
				DocumentBuilder newGuiElementBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document newGuiElementDocument = newGuiElementBuilder.parse(
					new ByteArrayInputStream(
						new String(
							"<gui-element>\n" +
							newGuiElementTemplate +
							"</gui-element>\n"
						).getBytes()
					)
				);
				parseGUINode(guiScreenNode, guiParentNode, newGuiElementDocument.getDocumentElement(), newGuiElement);
			}
		}

		// if we have a GUI element controller just init it, after element has been loaded
		if (guiElementController != null) {
			guiElementController.init();
		}
	}

	/**
	 * Returns immediate children tags
	 * @param parent
	 * @return children
	 */
	protected static List<Element> getChildrenTags(Element parent) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				nodeList.add((Element) child);
			}
		}
		return nodeList;
	}

	/**
	 * Get inner XML
	 * 	see: http://stackoverflow.com/questions/3300839/get-a-nodes-inner-xml-as-string-in-java-dom
	 * @param node
	 * @return string
	 */
	public static String getInnerXml(Node node) {
	    DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
	    LSSerializer lsSerializer = lsImpl.createLSSerializer();
	    NodeList childNodes = node.getChildNodes();
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < childNodes.getLength(); i++) {
	       sb.append(lsSerializer.writeToString(childNodes.item(i)));
	    }
	    String result = sb.toString();
	    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-16\"?>", "");
	    return result;
	}

	// 
	static {
		elements = new HashMap<String, GUIElement>();

		// add check box
		try {
			GUIElement guiCheckbox = new GUICheckbox();
			elements.put(guiCheckbox.getName(), guiCheckbox);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add radio button
		try {
			GUIElement guiRadioButton = new GUIRadioButton();
			elements.put(guiRadioButton.getName(), guiRadioButton);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box
		try {
			GUIElement guiSelectBox = new GUISelectBox();
			elements.put(guiSelectBox.getName(), guiSelectBox);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box option
		try {
			GUIElement guiSelectBoxOption = new GUISelectBoxOption();
			elements.put(guiSelectBoxOption.getName(), guiSelectBoxOption);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

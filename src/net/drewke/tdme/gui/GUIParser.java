package net.drewke.tdme.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.drewke.tdme.os.FileSystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * GUI parser 
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIParser {

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
			GUINode.createConditions(xmlRoot.getAttribute("hide-on")),
			xmlRoot.getAttribute("background-color"),
			xmlRoot.getAttribute("background-image")
		);

		// parse GUI nodes
		parseGUINode(guiScreenNode, xmlRoot);

		//
		return guiScreenNode;
	}

	/**
	 * Parse node
	 * @param gui parent node
	 * @param xml parent node
	 */
	protected static void parseGUINode(GUIParentNode guiParentNode, Element xmlParentNode) throws Exception {
		// parse sub nodes
		for (Element node: getChildrenTags(xmlParentNode)) {
			if (node.getNodeName().equals("layout")) {
				// TODO: validate root node
				GUILayoutNode guiLayoutNode = new GUILayoutNode(
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
					node.getAttribute("background-color"),
					node.getAttribute("background-image"),
					node.getAttribute("alignment")
				);
				guiParentNode.getSubNodes().add(guiLayoutNode);
				parseGUINode(guiLayoutNode, node);	
			} else
			if (node.getNodeName().equals("space")) {
				// TODO: validate root node
				GUISpaceNode guiSpaceNode = new GUISpaceNode(
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
			} else
			if (node.getNodeName().equals("panel")) {
				// TODO: validate root node
				GUIPanelNode guiPanelNode = new GUIPanelNode(
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
					node.getAttribute("background-color"),
					node.getAttribute("background-image"),
					node.getAttribute("alignment")
				);
				guiParentNode.getSubNodes().add(guiPanelNode);
				parseGUINode(guiPanelNode, node);	
			} else
			if (node.getNodeName().equals("element")) {
				// TODO: validate root node
				GUIElementNode guiElementNode = new GUIElementNode(
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
					node.getAttribute("background-color"),
					node.getAttribute("background-image")
				);
				guiParentNode.getSubNodes().add(guiElementNode);
				parseGUINode(guiElementNode, node);	
			} else
			if (node.getNodeName().equals("image")) {
				// TODO: validate root node
				GUIImageNode guiImageNode = new GUIImageNode(
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
			} else
			if (node.getNodeName().equals("text")) {
				// TODO: validate root node
				GUITextNode guiTextNode = new GUITextNode(
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
			} else {
				// TODO: check if end tag or unsupported
			}
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

}

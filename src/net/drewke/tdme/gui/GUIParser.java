package net.drewke.tdme.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.drewke.tdme.gui.elements.GUIButton;
import net.drewke.tdme.gui.elements.GUICheckbox;
import net.drewke.tdme.gui.elements.GUIDropDown;
import net.drewke.tdme.gui.elements.GUIDropDownOption;
import net.drewke.tdme.gui.elements.GUIElement;
import net.drewke.tdme.gui.elements.GUIInput;
import net.drewke.tdme.gui.elements.GUIRadioButton;
import net.drewke.tdme.gui.elements.GUIScrollArea;
import net.drewke.tdme.gui.elements.GUIScrollAreaHorizontal;
import net.drewke.tdme.gui.elements.GUIScrollAreaVertical;
import net.drewke.tdme.gui.elements.GUISelectBox;
import net.drewke.tdme.gui.elements.GUISelectBoxMultiple;
import net.drewke.tdme.gui.elements.GUISelectBoxMultipleOption;
import net.drewke.tdme.gui.elements.GUISelectBoxOption;
import net.drewke.tdme.gui.elements.GUITab;
import net.drewke.tdme.gui.elements.GUITabContent;
import net.drewke.tdme.gui.elements.GUITabs;
import net.drewke.tdme.gui.elements.GUITabsContent;
import net.drewke.tdme.gui.elements.GUITabsHeader;
import net.drewke.tdme.gui.nodes.GUIColor;
import net.drewke.tdme.gui.nodes.GUIElementNode;
import net.drewke.tdme.gui.nodes.GUIHorizontalScrollbarInternalNode;
import net.drewke.tdme.gui.nodes.GUIImageNode;
import net.drewke.tdme.gui.nodes.GUIInputInternalNode;
import net.drewke.tdme.gui.nodes.GUILayoutNode;
import net.drewke.tdme.gui.nodes.GUINode;
import net.drewke.tdme.gui.nodes.GUINode.AlignmentHorizontal;
import net.drewke.tdme.gui.nodes.GUINode.AlignmentVertical;
import net.drewke.tdme.gui.nodes.GUINodeController;
import net.drewke.tdme.gui.nodes.GUIPanelNode;
import net.drewke.tdme.gui.nodes.GUIParentNode;
import net.drewke.tdme.gui.nodes.GUIScreenNode;
import net.drewke.tdme.gui.nodes.GUISpaceNode;
import net.drewke.tdme.gui.nodes.GUITextNode;
import net.drewke.tdme.gui.nodes.GUIVerticalScrollbarInternalNode;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;
import net.drewke.tdme.utils.MutableString;

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

	private static HashMap<String, GUIElement> elements = new HashMap<String, GUIElement>();

	/**
	 * Parses a GUI XML file
	 * @param path name
	 * @param file name
	 * @return GUI screen node
	 * @throws IOException
	 */
	public static GUIScreenNode parse(String pathName, String fileName) throws Exception {
		return parse(FileSystem.getInstance().getContent(pathName, fileName));
	}

	/**
	 * Parses a GUI XML content
	 * @param xml
	 * @return GUI screen node
	 * @throws IOException
	 */
	public static GUIScreenNode parse(String xml) throws Exception {
		// load GUI xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(
			new ByteArrayInputStream(
				new String(
					xml
				).getBytes()
			)
		);
		Element xmlRoot = document.getDocumentElement();

		// create GUI main / screen node
		GUIScreenNode guiScreenNode = null;
		if (xmlRoot.getNodeName().equals("screen") == false) {
			throw new GUIParserException("XML root node must be <screen>");
		}
		guiScreenNode = new GUIScreenNode( 
			xmlRoot.getAttribute("id"),
			GUINode.createFlow(xmlRoot.getAttribute("flow")),
			GUIParentNode.createOverflow(xmlRoot.getAttribute("overflow-x")),
			GUIParentNode.createOverflow(xmlRoot.getAttribute("overflow-y")),
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
			GUINode.createBorder(
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
			GUINode.createPadding(
				xmlRoot.getAttribute("padding"), 
				xmlRoot.getAttribute("padding-left"), 
				xmlRoot.getAttribute("padding-top"), 
				xmlRoot.getAttribute("padding-right"), 
				xmlRoot.getAttribute("padding-bottom") 
			),
			GUINode.createConditions(xmlRoot.getAttribute("show-on")),
			GUINode.createConditions(xmlRoot.getAttribute("hide-on")),
			xmlRoot.getAttribute("scrollable").trim().equalsIgnoreCase("true"),
			xmlRoot.getAttribute("popup").trim().equalsIgnoreCase("true")
		);

		// parse GUI nodes
		parseGUINode(guiScreenNode, xmlRoot, null);

		//
		return guiScreenNode;
	}

	/**
	 * Parses a GUI XML file into parent node
	 * @param parent node
	 * @param path name
	 * @param file name
	 * @throws IOException
	 */
	public static void parse(GUIParentNode parentNode, String pathName, String fileName) throws Exception {
		// load GUI xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(
			new ByteArrayInputStream(
				new String(
					"<children>" +
					FileSystem.getInstance().getContent(pathName, fileName) +
					"</children>"
				).getBytes()
			)
		);
		Element xmlNode = document.getDocumentElement();

		// parse GUI nodes
		parseGUINode(parentNode, xmlNode, null);
	}

	/**
	 * Parses a GUI XML content into parent node
	 * @param parent node
	 * @param xml
	 * @throws IOException
	 */
	public static void parse(GUIParentNode parentNode, String xml) throws Exception {
		// load GUI xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(
			new ByteArrayInputStream(
				new String(
					"<children>" +
					xml +
					"</children>"
				).getBytes()
			)
		);
		Element xmlNode = document.getDocumentElement();

		// parse GUI nodes
		parseGUINode(parentNode, xmlNode, null);
	}

	/**
	 * Parse GUI node
	 * @param gui parent node
	 * @param xml parent node
	 * @param gui element
	 * @throws Exception
	 */
	private static void parseGUINode(GUIParentNode guiParentNode, Element xmlParentNode, GUIElement guiElement) throws Exception {
		//
		GUINodeController guiElementController = null;
		boolean guiElementControllerInstalled = false;
		// parse sub nodes
		for (Element node: getChildrenTags(xmlParentNode)) {
			if (node.getNodeName().equals("panel")) {
				// TODO: validate root node
				GUIPanelNode guiPanelNode = new GUIPanelNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createFlow(node.getAttribute("flow")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-x")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-y")),
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
					GUINode.getRequestedColor(node.getAttribute("background-color"), new GUIColor("#F0F0F0")),
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					GUILayoutNode.createAlignment(node.getAttribute("alignment"))
				);
				guiParentNode.addSubNode(guiPanelNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiPanelNode);
					if (guiElementController != null) {
						guiPanelNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiPanelNode, node, null);
			} else
			if (node.getNodeName().equals("layout")) {
				// TODO: validate root node
				GUILayoutNode guiLayoutNode = new GUILayoutNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createFlow(node.getAttribute("flow")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-x")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-y")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					GUILayoutNode.createAlignment(node.getAttribute("alignment"))
				);
				guiParentNode.addSubNode(guiLayoutNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiLayoutNode);
					if (guiElementController != null) {
						guiLayoutNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiLayoutNode, node, null);
			} else
			if (node.getNodeName().equals("space")) {
				// TODO: validate root node
				GUISpaceNode guiSpaceNode = new GUISpaceNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on"))
				);
				guiParentNode.addSubNode(guiSpaceNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiSpaceNode);
					if (guiElementController != null) {
						guiSpaceNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;

				}
			} else
			if (node.getNodeName().equals("element")) {
				// TODO: validate root node
				GUIElementNode guiElementNode = new GUIElementNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createFlow(node.getAttribute("flow")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-x")),
					GUIParentNode.createOverflow(node.getAttribute("overflow-y")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					unescapeQuotes(node.getAttribute("name")),
					unescapeQuotes(node.getAttribute("value")),
					node.getAttribute("selected").trim().equalsIgnoreCase("true"),
					node.getAttribute("disabled").trim().equalsIgnoreCase("true"),
					node.getAttribute("focusable").trim().equalsIgnoreCase("true"),
					node.getAttribute("ignore-events").trim().equalsIgnoreCase("true")
				);
				guiParentNode.addSubNode(guiElementNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiElementNode);
					if (guiElementController != null) {
						guiElementNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
				// parse child nodes
				parseGUINode(guiElementNode, node, null);
			} else
			if (node.getNodeName().equals("image")) {
				// TODO: validate root node
				GUIImageNode guiImageNode = new GUIImageNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"),
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					unescapeQuotes(node.getAttribute("src")),
					GUINode.getRequestedColor(node.getAttribute("effect-color-mul"), GUIColor.EFFECT_COLOR_MUL),
					GUINode.getRequestedColor(node.getAttribute("effect-color-add"), GUIColor.EFFECT_COLOR_ADD)
				);
				guiParentNode.addSubNode(guiImageNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiImageNode);
					if (guiElementController != null) {
						guiImageNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
			} else
			if (node.getNodeName().equals("text")) {
				// TODO: validate root node
				GUITextNode guiTextNode = new GUITextNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
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
					new MutableString(unescapeQuotes(node.getAttribute("text")))
				);
				guiParentNode.addSubNode(guiTextNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiTextNode);
					if (guiElementController != null) {
						guiTextNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
			} else 
			if (node.getNodeName().equals("input-internal")) {
				// TODO: validate root node
				GUIInputInternalNode guiInputInternalNode = new GUIInputInternalNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
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
					node.getAttribute("color-disabled"),
					new MutableString(unescapeQuotes(node.getAttribute("text"))),
					GUIInputInternalNode.createMaxLength(node.getAttribute("maxlength"))
				);
				guiParentNode.addSubNode(guiInputInternalNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiInputInternalNode);
					if (guiElementController != null) {
						guiInputInternalNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
			} else 
			if (node.getNodeName().equals("vertical-scrollbar-internal")) {
				// TODO: validate root node
				GUIVerticalScrollbarInternalNode guiVerticalScrollbarInternalNode = new GUIVerticalScrollbarInternalNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					GUINode.getRequestedColor(node.getAttribute("color-none"), GUIColor.BLACK),
					GUINode.getRequestedColor(node.getAttribute("color-mouseover"), GUIColor.BLACK),
					GUINode.getRequestedColor(node.getAttribute("color-dragging"), GUIColor.BLACK)
				);
				guiParentNode.addSubNode(guiVerticalScrollbarInternalNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiVerticalScrollbarInternalNode);
					if (guiElementController != null) {
						guiVerticalScrollbarInternalNode.setController(guiElementController);
					}
					guiElementControllerInstalled = true;
				}
			} else
			if (node.getNodeName().equals("horizontal-scrollbar-internal")) {
				// TODO: validate root node
				GUIHorizontalScrollbarInternalNode guiHorizontalScrollbarInternalNode = new GUIHorizontalScrollbarInternalNode(
					guiParentNode.getScreenNode(),
					guiParentNode, 
					node.getAttribute("id"), 
					GUINode.createFlow(node.getAttribute("flow")),
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
					GUINode.createBorder(
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
					GUINode.createPadding(
						node.getAttribute("padding"), 
						node.getAttribute("padding-left"), 
						node.getAttribute("padding-top"), 
						node.getAttribute("padding-right"), 
						node.getAttribute("padding-bottom") 
					),
					GUINode.createConditions(node.getAttribute("show-on")),
					GUINode.createConditions(node.getAttribute("hide-on")),
					GUINode.getRequestedColor(node.getAttribute("color-none"), GUIColor.BLACK),
					GUINode.getRequestedColor(node.getAttribute("color-mouseover"), GUIColor.BLACK),
					GUINode.getRequestedColor(node.getAttribute("color-dragging"), GUIColor.BLACK)
				);
				guiParentNode.addSubNode(guiHorizontalScrollbarInternalNode);
				// install gui element controller if not yet done
				if (guiElement != null && guiElementControllerInstalled == false) {
					guiElementController = guiElement.createController(guiHorizontalScrollbarInternalNode);
					if (guiElementController != null) {
						guiHorizontalScrollbarInternalNode.setController(guiElementController);
					}
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
				for (int i = 0; i < node.getAttributes().getLength(); i++) {
					Node attribute = node.getAttributes().item(i);
					newGuiElementTemplate = newGuiElementTemplate.replace("{$" + attribute.getNodeName() + "}", escapeQuotes(attribute.getNodeValue()));
				}
				HashMap<String, String> newGuiElementAttributes = newGuiElement.getAttributes(guiParentNode.getScreenNode());
				for (String newGuiElementAttributeKey: newGuiElementAttributes.getKeysIterator()) {
					String guiElementAttributeValue = escapeQuotes(newGuiElementAttributes.get(newGuiElementAttributeKey));
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
				parseGUINode(guiParentNode, newGuiElementDocument.getDocumentElement(), newGuiElement);
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
	private static List<Element> getChildrenTags(Element parent) {
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
	private static String getInnerXml(Node node) {
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

	/**
	 * Unescape quotes
	 * @param string
	 * @return string with unescaped quotes
	 */
	public static String unescapeQuotes(String string) {
		return string.replace("&quot;", "\"");
	}

	/**
	 * Escape quotes
	 * @param string
	 * @return string with escaped quotes
	 */
	public static String escapeQuotes(String string) {
		return string.replace("\"", "&quot;");
	}

	/**
	 * Add GUI element
	 * @param guiElement
	 * @throws GUIParserException
	 */
	public static void addElement(GUIElement guiElement) throws GUIParserException {
		if (elements.get(guiElement.getName()) != null) {
			throw new GUIParserException("Element with given name '" + guiElement.getName() + "' already exists");
		}
		elements.put(guiElement.getName(), guiElement);
	}

	// 
	static {
		// add check box
		try {
			GUIElement guiElement = new GUICheckbox();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add radio button
		try {
			GUIElement guiElement = new GUIRadioButton();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box
		try {
			GUIElement guiElement = new GUISelectBox();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box option
		try {
			GUIElement guiElement = new GUISelectBoxOption();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box multiple
		try {
			GUIElement guiElement = new GUISelectBoxMultiple();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add select box multiple option
		try {
			GUIElement guiElement = new GUISelectBoxMultipleOption();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add drop down
		try {
			GUIElement guiElement = new GUIDropDown();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add drop down option
		try {
			GUIElement guiElement = new GUIDropDownOption();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add tabs
		try {
			GUIElement guiElement = new GUITabs();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add tabs-header
		try {
			GUIElement guiElement = new GUITabsHeader();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add tab
		try {
			GUIElement guiElement = new GUITab();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add tab-contents
		try {
			GUIElement guiElement = new GUITabsContent();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add tab-content
		try {
			GUIElement guiElement = new GUITabContent();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add button
		try {
			GUIElement guiElement = new GUIButton();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add input
		try {
			GUIElement guiElement = new GUIInput();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add vertical scrollbar
		try {
			GUIElement guiElement = new GUIScrollAreaVertical();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add horizontal scrollbar
		try {
			GUIElement guiElement = new GUIScrollAreaHorizontal();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add scrollbars
		try {
			GUIElement guiElement = new GUIScrollArea();
			addElement(guiElement);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

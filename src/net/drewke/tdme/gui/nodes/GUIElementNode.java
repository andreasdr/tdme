package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;

/**
 * GUI element node
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIElementNode extends GUIParentNode {

	protected static final String CONDITION_ALWAYS = "always";
	protected static final String CONDITION_ONMOUSEOVER = "mouseover";
	protected static final String CONDITION_CLICK = "click";
	public static final String CONDITION_FOCUS = "focus";

	protected String name;
	protected String value;
	protected boolean selected;
	protected boolean disabled;

	protected GUINodeConditions activeConditions = new GUINodeConditions();

	protected boolean focusable;
	protected boolean ignoreEvents;

	/**
	 * Constructor
	 * @param screen node
	 * @param parent node
	 * @param id
	 * @param flow
	 * @param overflow x
	 * @param overflow y
	 * @param alignments
	 * @param requested constraints
	 * @param background color
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on
	 * @param name
	 * @param value
	 * @param selected
	 * @param disabled
	 * @param focusable
	 * @param ignore events
	 * @throws GUIParserException
	 */
	public GUIElementNode(
		GUIScreenNode screenNode,
		GUIParentNode parentNode, 
		String id, 
		Flow flow,
		Overflow overflowX,
		Overflow overflowY,
		Alignments alignments, 
		RequestedConstraints requestedConstraints,
		GUIColor backgroundColor,
		Border border, 
		Padding padding,
		GUINodeConditions showOn, 
		GUINodeConditions hideOn, 
		String name,
		String value,
		boolean selected,
		boolean disabled,
		boolean focusable,
		boolean ignoreEvents
		) throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, overflowX, overflowY, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);

		//
		this.name = name;
		this.value = value;
		this.selected = selected;
		this.disabled = disabled;
		this.focusable = focusable;
		this.ignoreEvents = ignoreEvents;

		// controller
		this.controller = ignoreEvents == true?new GUIElementIgnoreEventsController(this):new GUIElementController(this);
		this.controller.initialize();
	}

	/**
	 * @return node type
	 */
	protected String getNodeType() {
		return "element";
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#isContentNode()
	 */
	protected boolean isContentNode() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentWidth()
	 */
	public int getContentWidth() {
		// determine content width
		int width = 0;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentWidth = guiSubNode.getAutoWidth();
			if (contentWidth > width) {
				width = contentWidth;
			}
		}

		// add border, padding
		width+= border.left + border.right;
		width+= padding.left + padding.right;

		//
		return width;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#getContentHeight()
	 */
	public int getContentHeight() {
		// determine content height
		int height = 0;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			int contentHeight = guiSubNode.getAutoHeight();
			if (contentHeight > height) {
				height = contentHeight;
			}
		}

		// add border, padding
		height+= border.top + border.bottom;
		height+= padding.top + padding.bottom;

		//
		return height;
	}

	/**
	 * @return focusable
	 */
	public boolean isFocusable() {
		return focusable;
	}

	/**
	 * Set computed top
	 * @param top
	 */
	protected void setTop(int top) {
		super.setTop(top);
		top+= computedConstraints.alignmentTop;
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).setTop(top);
		}
	}

	/**
	 * Set computed left
	 * @param left
	 */
	protected void setLeft(int left) {
		super.setLeft(left);
		left+= computedConstraints.alignmentLeft;
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).setLeft(left);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUIParentNode#layoutSubNodes()
	 */
	protected void layoutSubNodes() {
		//
		super.layoutSubNodes();

		// stars
		int height = computedConstraints.height - border.top - border.bottom - padding.top - padding.bottom;
		int width = computedConstraints.width - border.left - border.right - padding.left - padding.right;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			boolean doLayoutSubNodes = false;
			if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
				guiSubNode.computedConstraints.height = height;
				doLayoutSubNodes = true;
			} else
			if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
				guiSubNode.computedConstraints.width = width;
				doLayoutSubNodes = true;
			}

			// layout sub node, sub nodes, second pass
			if (guiSubNode instanceof GUIParentNode &&
				doLayoutSubNodes == true) {
				((GUIParentNode)guiSubNode).layoutSubNodes();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIParentNode#layout()
	 */
	protected void layout() {
		// workaround for a bug if element node and its sub nodes has a fixed pixel height and 
		// sub nodes nodes do not fit into element node
		// this is only done if DOWNSIZE_CHILDREN overflow is selected
		// not sure if to keep this work around or fix it another way
		//	height
		if (requestedConstraints.heightType == RequestedConstraintsType.PIXEL) {
			int subNodesHeight = requestedConstraints.height - border.top - border.bottom - padding.top - padding.bottom;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode subNode = subNodes.get(i);
				if (overflowY == Overflow.DOWNSIZE_CHILDREN &&
					subNode.requestedConstraints.heightType == RequestedConstraintsType.PIXEL &&
					subNode.requestedConstraints.height > subNodesHeight) {
					//
					subNode.requestedConstraints.height = subNodesHeight;
				}
			}
		}
		//	width
		if (requestedConstraints.widthType == RequestedConstraintsType.PIXEL) {
			int subNodesWidth = requestedConstraints.width - border.left - border.right - padding.left - padding.right;
			for (int i = 0; i < subNodes.size(); i++) {
				GUINode subNode = subNodes.get(i);
				if (overflowY == Overflow.DOWNSIZE_CHILDREN &&
					subNode.requestedConstraints.widthType == RequestedConstraintsType.PIXEL &&
					subNode.requestedConstraints.width > subNodesWidth) {
					//
					subNode.requestedConstraints.width = subNodesWidth;
				}
			}
		}

		// super layout
		super.layout();

		// do parent + children top, left adjustments
		setTop(computedConstraints.top);
		setLeft(computedConstraints.left);

		// compute children alignments
		computeHorizontalChildrenAlignment();
		computeVerticalChildrenAlignment();
	}

	/** 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @return is disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * @return active conditions
	 */
	public GUINodeConditions getActiveConditions() {
		return activeConditions;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUIParentNode#handleMouseEvent(net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUIMouseEvent event) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// remove mouse over, click to active conditions
		activeConditions.remove(CONDITION_ONMOUSEOVER);
		activeConditions.remove(CONDITION_CLICK);

		// skip if processed by floating node
		if (screenNode.mouseEventProcessedByFloatingNode == true) return;

		//
		if (isEventBelongingToNode(event)) {
			//
			switch (event.getType()) {
				case MOUSE_MOVED:
					activeConditions.add(CONDITION_ONMOUSEOVER);
					if (ignoreEvents == false) event.setProcessed(true);
					break;
				case MOUSE_PRESSED:
					activeConditions.add(CONDITION_CLICK);
					if (ignoreEvents == false) event.setProcessed(true);
					break;
				default:
					break;
			}
		}

		//
		super.handleMouseEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleKeyboardEvent(net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
		// ignore events?
		if (ignoreEvents == true) return;

		// check if conditions were met
		if (conditionsMet == false) return;

		// delegate event to sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode subNode = subNodes.get(i);
			subNode.handleKeyboardEvent(event);
		}

		//
		super.handleKeyboardEvent(event);
	}

}
	

package net.drewke.tdme.gui.nodes;

import net.drewke.tdme.gui.GUIParserException;
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
	protected static final String CONDITION_FOCUS = "focus";

	protected String name;
	protected String value;
	protected boolean selected;
	protected boolean disabled;

	protected GUINodeConditions activeConditions = new GUINodeConditions();

	protected boolean focusable;

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
		boolean focusable
		) throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, overflowX, overflowY, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);

		//
		this.name = name;
		this.value = value;
		this.selected = selected;
		this.disabled = disabled;
		this.focusable = focusable;

		// controller
		this.controller = new GUIElementController(this);
		this.controller.init();
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
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			boolean doLayoutSubNodes = false;
			if (guiSubNode.requestedConstraints.heightType == RequestedConstraintsType.STAR) {
				guiSubNode.computedConstraints.height = computedConstraints.height;
				doLayoutSubNodes = true;
			} else
			if (guiSubNode.requestedConstraints.widthType == RequestedConstraintsType.STAR) {
				guiSubNode.computedConstraints.width = computedConstraints.width;
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

		// add mouseoever, click to active conditions
		activeConditions.remove(CONDITION_ONMOUSEOVER);
		activeConditions.remove(CONDITION_CLICK);
		if (isEventBelongingToNode(event)) {
			//
			switch (event.getType()) {
				case MOUSE_MOVED:
					activeConditions.add(CONDITION_ONMOUSEOVER);
					break;
				case MOUSE_PRESSED:
					activeConditions.add(CONDITION_CLICK);
					break;
				default:
					break;
			}
		}

		//
		super.handleMouseEvent(event);
	}

}
	
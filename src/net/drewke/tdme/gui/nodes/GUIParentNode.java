package net.drewke.tdme.gui.nodes;

import java.util.ArrayList;

import net.drewke.tdme.gui.GUIParserException;
import net.drewke.tdme.gui.events.GUIKeyboardEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent;
import net.drewke.tdme.gui.events.GUIMouseEvent.Type;
import net.drewke.tdme.gui.nodes.GUINode.Flow;
import net.drewke.tdme.gui.nodes.GUINode.RequestedConstraints.RequestedConstraintsType;
import net.drewke.tdme.gui.renderer.GUIRenderer;

/**
 * A parent node supporting child notes
 * @author Andreas Drewke
 * @version $Id$
 */
public abstract class GUIParentNode extends GUINode {

	enum Overflow {HIDDEN, DOWNSIZE_CHILDREN, SCROLL};

	protected ArrayList<GUINode> subNodes;

	protected Overflow overflowX;
	protected Overflow overflowY;

	protected float childrenRenderOffsetX;
	protected float childrenRenderOffsetY;

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
	 * @oaram background color
	 * @param border
	 * @param padding
	 * @param show on
	 * @param hide on 
	 */
	protected GUIParentNode(
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
		GUINodeConditions hideOn
		) throws GUIParserException {
		//
		super(screenNode, parentNode, id, flow, alignments, requestedConstraints, backgroundColor, border, padding, showOn, hideOn);
		this.subNodes = new ArrayList<GUINode>();
		this.overflowX = overflowX;
		this.overflowY = overflowY;
		this.childrenRenderOffsetX = 0f;
		this.childrenRenderOffsetY = 0f;
	}

	/**
	 * Clear sub nodes
	 */
	public void clearSubNodes() {
		childrenRenderOffsetX = 0f;
		childrenRenderOffsetY = 0f;
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode node = subNodes.get(i);
			screenNode.removeNode(node);
			if (node instanceof GUIParentNode) {
				((GUIParentNode)node).clearSubNodes();
			}
		}
		subNodes.clear();
	}

	/**
	 * Add sub node
	 * @param node
	 */
	public void addSubNode(GUINode node) throws GUIParserException {
		if (screenNode.addNode(node) == false) {
			throw new GUIParserException("Screen '" + screenNode.id + "' already has a node attached with given node id '" + node.id + "'");
		}
		subNodes.add(node);
	}

	/**
	 * @return overflow x
	 */
	public Overflow getOverflowX() {
		return overflowX;
	}

	/**
	 * @return overflow y
	 */
	public Overflow getOverflowY() {
		return overflowY;
	}

	/**
	 * Create over flow
	 * @param over flow
	 * @return over flow
	 * @throws gui parser exception
	 */
	public static Overflow createOverflow(String overflow) throws GUIParserException {
		if (overflow.trim().equalsIgnoreCase("hidden") == true) {
			return Overflow.HIDDEN;
		} else 
		if (overflow.trim().equalsIgnoreCase("downsize-children") == true) {
			return Overflow.DOWNSIZE_CHILDREN;
		} else
		if (overflow.trim().equalsIgnoreCase("scroll") == true) {
			return Overflow.SCROLL;
		} else 
		if (overflow == null || overflow.trim().length() == 0) {
			return Overflow.HIDDEN;
		} else {
			throw new GUIParserException("Unknown overflow '" + overflow + "'");
		}
	}

	/**
	 * @return children render offset x
	 */
	public float getChildrenRenderOffsetX() {
		return childrenRenderOffsetX;
	}

	/**
	 * Set children render offset x
	 * @param children render offset x
	 */
	public void setChildrenRenderOffsetX(float childrenRenderOffSetX) {
		this.childrenRenderOffsetX = childrenRenderOffSetX;
	}

	/**
	 * @return children render offset y
	 */
	public float getChildrenRenderOffsetY() {
		return childrenRenderOffsetY;
	}

	/**
	 * Set children render offset y
	 * @param children render offset y
	 */
	public void setChildrenRenderOffsetY(float childrenRenderOffSetY) {
		this.childrenRenderOffsetY = childrenRenderOffSetY;
	}

	/**
	 * Create requested constraints
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return requested constraints
	 */
	public static RequestedConstraints createRequestedConstraints(String left, String top, String width, String height) {
		RequestedConstraints constraints = new RequestedConstraints();
		constraints.leftType = getRequestedConstraintsType(left.trim(), RequestedConstraintsType.PIXEL);
		constraints.left = getRequestedConstraintsValue(left.trim(), 0);
		constraints.topType = getRequestedConstraintsType(top.trim(), RequestedConstraintsType.PIXEL);
		constraints.top = getRequestedConstraintsValue(top.trim(), 0);
		constraints.widthType = getRequestedConstraintsType(width.trim(), RequestedConstraintsType.AUTO);
		constraints.width = getRequestedConstraintsValue(width.trim(), -1);
		constraints.heightType = getRequestedConstraintsType(height.trim(), RequestedConstraintsType.AUTO);
		constraints.height = getRequestedConstraintsValue(height.trim(), -1);
		return constraints;
	}

	/**
	 * Layout
	 */
	public void layout() {
		super.layout();
		layoutSubNodes();
	}

	/**
	 * Layout sub nodes
	 */
	public void layoutSubNodes() {
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).layout();
		}
	}

	/**
	 * Compute horizontal children alignment
	 */
	protected void computeHorizontalChildrenAlignment() {
		// align all vertical aligned children horizontally
		switch (alignments.horizontal) {
			case LEFT: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = border.left + padding.left; 
					}
					break;
				}
			case CENTER: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width) / 2; 
					}
					break;
				}
			case RIGHT: {
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentLeft = (computedConstraints.width - guiSubNode.computedConstraints.width - border.right - padding.right); 
					}
					break;
				}
			}
		}
	}

	/**
	 * Compute vertical children alignment
	 */
	protected void computeVerticalChildrenAlignment() {
		// align all horizontal aligned children vertically 
		switch (alignments.vertical) {
			case TOP: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = border.top + padding.top; 
					}
					break;
				}
			case CENTER: 
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height) / 2; 
					}
					break;
				}
			case BOTTOM: {
				{
					for (int i = 0; i < subNodes.size(); i++) {
						GUINode guiSubNode = subNodes.get(i);
						guiSubNode.computedConstraints.alignmentTop = (computedConstraints.height - guiSubNode.computedConstraints.height - border.bottom - padding.bottom); 
					}
					break;
				}
			}
		}
	}

	/**
	 * Get child controller nodes internal
	 * @param child controller nodes
	 */
	private void getChildControllerNodesInternal(ArrayList<GUINode> childControllerNodes) {
		// dispose sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode node = subNodes.get(i);
			if (node.controller != null) {
				childControllerNodes.add(node);
			}
			if (node instanceof GUIParentNode) {
				((GUIParentNode)node).getChildControllerNodesInternal(childControllerNodes);
			}
		}		
	}

	/**
	 * Get child controller nodes
	 * @param child controller nodes
	 */
	public void getChildControllerNodes(ArrayList<GUINode> childControllerNodes) {
		childControllerNodes.clear();
		getChildControllerNodesInternal(childControllerNodes);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#dispose()
	 */
	public void dispose() {
		// dispose sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			subNodes.get(i).dispose();
		}
		// parent dispose
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#setConditionsMet()
	 */
	public void setConditionsMet() {
		// check conditions
		conditionsMet = checkConditions();

		// check if conditions were met
		if (conditionsMet == false) return;

		//
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			guiSubNode.setConditionsMet();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#render(net.drewke.tdme.gui.GUIRenderer)
	 */
	public void render(GUIRenderer guiRenderer, ArrayList<GUINode> floatingNodes) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// store render area current
		float renderAreaLeftCurrent = guiRenderer.getRenderAreaLeft();
		float renderAreaTopCurrent = guiRenderer.getRenderAreaTop();
		float renderAreaRightCurrent = guiRenderer.getRenderAreaRight();
		float renderAreaBottomCurrent = guiRenderer.getRenderAreaBottom();

		// screen dimension
		float screenWidth = guiRenderer.getGUI().getWidth();
		float screenHeight = guiRenderer.getGUI().getHeight();

		// element location and dimensions
		float left = computedConstraints.left + computedConstraints.alignmentLeft;
		float top = computedConstraints.top + computedConstraints.alignmentTop;
		float width = computedConstraints.width;
		float height = computedConstraints.height;

		// store current render offset
		float renderOffsetXCurrent = guiRenderer.getRenderOffsetX();
		float renderOffsetYCurrent = guiRenderer.getRenderOffsetY(); 

		// determine render offsets
		float renderOffsetXPixel = 0f;
		float renderOffsetYPixel = 0f;
		for (GUIParentNode parentNode = this; parentNode != null; parentNode = parentNode.parentNode) {
			renderOffsetXPixel+= parentNode.childrenRenderOffsetX;
			renderOffsetYPixel+= parentNode.childrenRenderOffsetY;
		}

		// new render offsets
		float renderOffsetX = renderOffsetXPixel / (screenWidth / 2f);
		float renderOffsetY = renderOffsetYPixel / (screenHeight / 2f);

		// render area
		float renderAreaLeft = ((left) / (screenWidth / 2f)) - 1f;
		float renderAreaTop = ((screenHeight - top) / (screenHeight / 2f)) + renderOffsetYCurrent - 1f;
		float renderAreaRight = ((left + width) / (screenWidth / 2f)) - 1f;
		float renderAreaBottom = ((screenHeight - top - height) / (screenHeight / 2f)) + renderOffsetYCurrent - 1f;

		// render area
		guiRenderer.setSubRenderAreaLeft(renderAreaLeft);
		guiRenderer.setSubRenderAreaTop(renderAreaTop);
		guiRenderer.setSubRenderAreaRight(renderAreaRight);
		guiRenderer.setSubRenderAreaBottom(renderAreaBottom);

		// render offsets
		guiRenderer.setRenderOffsetX(renderOffsetX);
		guiRenderer.setRenderOffsetY(renderOffsetY);

		// call parent renderer
		super.render(guiRenderer, floatingNodes);

		//
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode guiSubNode = subNodes.get(i);
			// render floating nodes later
			if (guiSubNode.flow == Flow.FLOATING) {
				floatingNodes.add(guiSubNode);
				continue;
			}

			// restore render area
			guiRenderer.setRenderAreaLeft(renderAreaLeftCurrent);
			guiRenderer.setRenderAreaTop(renderAreaTopCurrent);
			guiRenderer.setRenderAreaRight(renderAreaRightCurrent);
			guiRenderer.setRenderAreaBottom(renderAreaBottomCurrent);

			// set up sub render area
			guiRenderer.setSubRenderAreaLeft(renderAreaLeft);
			guiRenderer.setSubRenderAreaTop(renderAreaTop);
			guiRenderer.setSubRenderAreaRight(renderAreaRight);
			guiRenderer.setSubRenderAreaBottom(renderAreaBottom);

			// render offsets
			guiRenderer.setRenderOffsetX(renderOffsetX);
			guiRenderer.setRenderOffsetY(renderOffsetY);

			// render sub nodes
			guiSubNode.render(guiRenderer, floatingNodes);
		}

		// restore render offsets
		guiRenderer.setRenderOffsetX(renderOffsetXCurrent);
		guiRenderer.setRenderOffsetY(renderOffsetYCurrent);

		// restore render area
		guiRenderer.setRenderAreaLeft(renderAreaLeftCurrent);
		guiRenderer.setRenderAreaTop(renderAreaTopCurrent);
		guiRenderer.setRenderAreaRight(renderAreaRightCurrent);
		guiRenderer.setRenderAreaBottom(renderAreaBottomCurrent);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleMouseEvent(net.drewke.tdme.gui.events.GUIMouseEvent)
	 */
	public void handleMouseEvent(GUIMouseEvent event) {
		// check if conditions were met
		if (conditionsMet == false) return;

		// skip if processed by floating node
		if (screenNode.mouseEventProcessedByFloatingNode == true) return;

		// mouse wheel event?
		if (isEventBelongingToNode(event) == true &&
			event.getType() == Type.MOUSE_WHEEL_MOVED) {

			// check for overflow x
			if (event.getWheelX() != 0f && overflowX == Overflow.SCROLL) {
				childrenRenderOffsetX-= event.getWheelX();

				//
				float elementWidth = this.computedConstraints.width;
				float contentWidth = this.getContentWidth();
				float scrollableWidth = contentWidth - elementWidth;

				// clip to min, max
				if (childrenRenderOffsetX < 0) childrenRenderOffsetX = 0;
				if (childrenRenderOffsetX > scrollableWidth) childrenRenderOffsetX = scrollableWidth;

				// set event processed
				event.setProcessed(true);

				// exit
				return;
			}

			// check for overflow y
			if (event.getWheelY() != 0f && overflowY == Overflow.SCROLL) {
				childrenRenderOffsetY-= event.getWheelY();

				//
				float elementHeight = this.computedConstraints.height;
				float contentHeight = this.getContentHeight();
				float scrollableHeight = contentHeight - elementHeight;

				// clip to min, max
				if (childrenRenderOffsetY < 0) childrenRenderOffsetY = 0;
				if (childrenRenderOffsetY > scrollableHeight) childrenRenderOffsetY = scrollableHeight;

				// set event processed
				event.setProcessed(true);

				// exit
				return;
			}
		}

		// delegate event to sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode subNode = subNodes.get(i);
			// handle events of floating nodes later
			if (subNode.flow == Flow.FLOATING) {
				continue;
			}

			//
			subNode.handleMouseEvent(event);
		}

		//
		super.handleMouseEvent(event);

		// did mouse event have been consumed on a floating node
		// then mark those as consumed
		if (flow == Flow.FLOATING && event.isProcessed() == true) {
			screenNode.mouseEventProcessedByFloatingNode = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#handleKeyboardEvent(net.drewke.tdme.gui.events.GUIKeyboardEvent)
	 */
	public void handleKeyboardEvent(GUIKeyboardEvent event) {
		// check if conditions were met
		if (conditionsMet == false) return;

		//
		super.handleKeyboardEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.nodes.GUINode#tick()
	 */
	public void tick() {
		// check if conditions were met
		if (conditionsMet == false) return;

		// delegate event to sub nodes
		for (int i = 0; i < subNodes.size(); i++) {
			GUINode subNode = subNodes.get(i);
			subNode.tick();
		}

		//
		super.tick();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#toString()
	 */
	public String toString() {
		return toString(0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUINode#toString(int)
	 */
	protected String toString(int indent) {
		String tmp =
				indent(indent) +
				"GUIParentNode ["
				+ "type=" + getNodeType() 
				+ ", id=" + id 
				+ ", alignments=" + alignments
				+ ", requestedConstraints=" + requestedConstraints 
				+ ", computedConstraints=" + computedConstraints
				+ ", border=" + border 
				+ ", padding=" + padding
				+ ", controller=" + (this.controller != null?"yes":"no")
				+ "]" + "\n";
		for (int i = 0; i < subNodes.size(); i++) {
			tmp+= subNodes.get(i).toString(indent + 1) + (i == subNodes.size() - 1?"":"\n");
		}
		return tmp;
	}

}

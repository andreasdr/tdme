package net.drewke.tdme.gui.effects;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.gui.nodes.GUIColor;
import net.drewke.tdme.gui.renderer.GUIRenderer;

/**
 * GUI color effect
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUIColorEffect extends GUIEffect {

	// color definitions
	private GUIColor colorAddStart = new GUIColor();
	private GUIColor colorAddEnd = new GUIColor();
	private GUIColor colorMulStart = new GUIColor();
	private GUIColor colorMulEnd = new GUIColor();

	// current color
	private GUIColor colorAdd = new GUIColor();
	private GUIColor colorMul = new GUIColor();

	/**
	 * @return color add start
	 */
	public GUIColor getColorAddStart() {
		return colorAddStart;
	}

	/**
	 * @return color add end
	 */
	public GUIColor getColorAddEnd() {
		return colorAddEnd;
	}

	/**
	 * @return color mul start
	 */
	public GUIColor getColorMulStart() {
		return colorMulStart;
	}

	/**
	 * @return color mul end
	 */
	public GUIColor getColorMulEnd() {
		return colorMulEnd;
	}

	/**
	 * Start
	 */
	public void start() {
		super.start();
		colorMul.set(colorMulStart);
		colorAdd.set(colorAddStart);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.effects.GUIEffect#update(net.drewke.tdme.gui.renderer.GUIRenderer)
	 */
	public void update(GUIRenderer guiRenderer) {
		super.update(guiRenderer);
		if (active == true) {
			colorMul.add(
				(colorMulEnd.getRed() - colorMulStart.getRed()) / timeTotal * timePassed,
				(colorMulEnd.getGreen() - colorMulStart.getGreen()) / timeTotal * timePassed,
				(colorMulEnd.getBlue() - colorMulStart.getBlue()) / timeTotal * timePassed,
				(colorMulEnd.getAlpha() - colorMulStart.getAlpha()) / timeTotal * timePassed
			);
			guiRenderer.setGUIEffectColorMul(colorMul);
			colorAdd.add(
				(colorAddEnd.getRed() - colorAddStart.getRed()) / timeTotal * timePassed,
				(colorAddEnd.getGreen() - colorAddStart.getGreen()) / timeTotal * timePassed,
				(colorAddEnd.getBlue() - colorAddStart.getBlue()) / timeTotal * timePassed,
				(colorAddEnd.getAlpha() - colorAddStart.getAlpha()) / timeTotal * timePassed
			);
			guiRenderer.setGUIEffectColorAdd(colorAdd);
		}
	}

}

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
	 * Public constructor
	 */
	public GUIColorEffect() {
		super();
	}

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
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.effects.GUIEffect#apply()
	 */
	public void apply(GUIRenderer guiRenderer) {
		// exit if not active
		if (active == false) return;

		// color mul
		colorMul.set(
			colorMulStart.getRed() + ((colorMulEnd.getRed() - colorMulStart.getRed()) / timeTotal * (timeTotal - timeLeft)),
			colorMulStart.getGreen() + ((colorMulEnd.getGreen() - colorMulStart.getGreen()) / timeTotal * (timeTotal - timeLeft)),
			colorMulStart.getBlue() + ((colorMulEnd.getBlue() - colorMulStart.getBlue()) / timeTotal * (timeTotal - timeLeft)),
			colorMulStart.getAlpha() + ((colorMulEnd.getAlpha() - colorMulStart.getAlpha()) / timeTotal * (timeTotal - timeLeft))
		);
		guiRenderer.setGUIEffectColorMul(colorMul);

		// color add
		colorAdd.set(
			colorAddStart.getRed() + ((colorAddEnd.getRed() - colorAddStart.getRed()) / timeTotal * (timeTotal - timeLeft)),
			colorAddStart.getGreen() + ((colorAddEnd.getGreen() - colorAddStart.getGreen()) / timeTotal * (timeTotal - timeLeft)),
			colorAddStart.getBlue() + ((colorAddEnd.getBlue() - colorAddStart.getBlue()) / timeTotal * (timeTotal - timeLeft)),
			colorAddStart.getAlpha() + ((colorAddEnd.getAlpha() - colorAddStart.getAlpha()) / timeTotal * (timeTotal - timeLeft))
		);
		guiRenderer.setGUIEffectColorAdd(colorAdd);
	}

}

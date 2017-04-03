package net.drewke.tdme.gui.effects;

import net.drewke.tdme.gui.renderer.GUIRenderer;

public class GUIPositionEffect extends GUIEffect {

	private float positionXStart = 0f;
	private float positionXEnd = 0f;
	private float positionYStart = 0f;
	private float positionYEnd = 0f;

	private float positionX = 0f;
	private float positionY = 0f;

	/**
	 * Public constructor
	 */
	public GUIPositionEffect() {
		super();
	}

	/**
	 * @return position X start
	 */
	public float getPositionXStart() {
		return positionXStart;
	}

	/**
	 * Set position X start
	 * @param position X start
	 */
	public void setPositionXStart(float positionXStart) {
		this.positionXStart = positionXStart;
	}

	/**
	 * @return position X end
	 */
	public float getPositionXEnd() {
		return positionXEnd;
	}

	/**
	 * Set position X end
	 * @param position X end
	 */
	public void setPositionXEnd(float positionXEnd) {
		this.positionXEnd = positionXEnd;
	}

	/**
	 * @return position Y start
	 */
	public float getPositionYStart() {
		return positionYStart;
	}

	/**
	 * Set position Y start
	 * @param position Y start
	 */
	public void setPositionYStart(float positionYStart) {
		this.positionYStart = positionYStart;
	}

	/**
	 * @return get position Y end
	 */
	public float getPositionYEnd() {
		return positionYEnd;
	}

	/**
	 * Set position Y end
	 * @param position Y end
	 */
	public void setPositionYEnd(float positionYEnd) {
		this.positionYEnd = positionYEnd;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.effects.GUIEffect#apply(net.drewke.tdme.gui.renderer.GUIRenderer)
	 */
	public void apply(GUIRenderer guiRenderer) {
		// exit if not active
		if (active == false) return;

		// screen dimension
		float screenWidth = guiRenderer.getGUI().getWidth();
		float screenHeight = guiRenderer.getGUI().getHeight();

		//
		positionX = positionXStart + ((positionXEnd - positionXStart) / timeTotal * (timeTotal - timeLeft));
		positionY = positionYStart + ((positionYEnd - positionYStart) / timeTotal * (timeTotal - timeLeft));
		guiRenderer.setGUIEffectOffsetX(-positionX / screenWidth * 2f);
		guiRenderer.setGUIEffectOffsetY(-positionY / screenHeight * 2f);
	}

}

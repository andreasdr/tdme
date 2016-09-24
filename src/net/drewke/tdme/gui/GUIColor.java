package net.drewke.tdme.gui;

/**
 * GUI color
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIColor {

	public final static GUIColor WHITE = new GUIColor(new float[] {1f, 1f, 1f, 1f});
	public final static GUIColor BLACK = new GUIColor(new float[] {0f, 0f, 0f, 1f});
	public final static GUIColor RED = new GUIColor(new float[] {1f, 0f, 0f, 1f});
	public final static GUIColor GREEN = new GUIColor(new float[] {0f, 1f, 0f, 1f});
	public final static GUIColor BLUE = new GUIColor(new float[] {0f, 0f, 1f, 1f});
	
	private float[] data;

	/**
	 * Public constructor
	 */
	public GUIColor() {
		data = new float[] {0f,0f,0f,1f};
	}

	/**
	 * Public constructor
	 * @param data
	 */
	public GUIColor(float[] data) {
		this.data = data;
	}

	/**
	 * Public constructor
	 * @param color text
	 */
	public GUIColor(String colorString) throws GUIParserException {
		if (colorString.startsWith("#") == false ||
			(colorString.length() != 7 && colorString.length() != 9)) {
			throw new GUIParserException("Invalid color '" + colorString + "'");
		}
		data = new float[] {0f,0f,0f,1f};
        data[0] = Integer.valueOf(colorString.substring(1, 3), 16) / 255f;
        data[1] = Integer.valueOf(colorString.substring(3, 5), 16) / 255f;
        data[2] = Integer.valueOf(colorString.substring(5, 7), 16) / 255f;
        if (colorString.length() > 7) {
        	data[3] = Integer.valueOf(colorString.substring(7, 9), 16) / 255f;
        }
	}

	/**
	 * @return data
	 */
	public float[] getData() {
		return data;
	}

}

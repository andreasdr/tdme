package net.drewke.tdme.gui;

/**
 * GUI color
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIColor {

	private float[] data;

	/**
	 * Public constructor
	 */
	public GUIColor() {
		data = new float[] {0f,0f,0f,1f};
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
        data[0] = Integer.valueOf(colorString.substring(1, 3), 16);
        data[1] = Integer.valueOf(colorString.substring(3, 5), 16);
        data[2] = Integer.valueOf(colorString.substring(5, 7), 16);
        if (colorString.length() > 7) {
        	data[2] = Integer.valueOf(colorString.substring(7, 9), 16);
        }
	}

	/**
	 * @return data
	 */
	public float[] getData() {
		return data;
	}

}

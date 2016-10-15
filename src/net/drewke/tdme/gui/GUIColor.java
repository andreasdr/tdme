package net.drewke.tdme.gui;

import java.util.Arrays;

/**
 * GUI color
 * @author Andreas Drewke
 * @version $Id$
 */
public final class GUIColor {

	// predefined colors
	public final static GUIColor WHITE = new GUIColor(new float[] {1f, 1f, 1f, 1f});
	public final static GUIColor BLACK = new GUIColor(new float[] {0f, 0f, 0f, 1f});
	public final static GUIColor RED = new GUIColor(new float[] {1f, 0f, 0f, 1f});
	public final static GUIColor GREEN = new GUIColor(new float[] {0f, 1f, 0f, 1f});
	public final static GUIColor BLUE = new GUIColor(new float[] {0f, 0f, 1f, 1f});
	public final static GUIColor TRANSPARENT = new GUIColor(new float[] {0f, 0f, 0f, 0f});
	public final static GUIColor EFFECT_COLOR_MUL = new GUIColor(new float[] {1f, 1f, 1f, 1f});
	public final static GUIColor EFFECT_COLOR_ADD = new GUIColor(new float[] {0f, 0f, 0f, 0f});

	// predefined color instances
	private final static GUIColor[] COLOR_INSTANCES = {
		WHITE,
		BLACK,
		RED,
		GREEN,
		BLUE
	};

	// predefined color names
	private final static String[] COLOR_NAMES = {
		"WHITE",
		"BLACK",
		"RED",
		"GREEN",
		"BLUE"
	};
	
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
		// check if color string is given
		if (colorString == null) {
			throw new GUIParserException("No color given");
		}

		// try to take from predefined colors
		for (int i = 0; i < COLOR_NAMES.length; i++) {
			if (COLOR_NAMES[i].equalsIgnoreCase(colorString) == true) {
				this.data = new float[] {
					COLOR_INSTANCES[i].data[0], 
					COLOR_INSTANCES[i].data[1], 
					COLOR_INSTANCES[i].data[2], 
					COLOR_INSTANCES[i].data[3]
				};
				return;
			}
		}

		// otherwise check if color string has valid hex notation
		if (colorString.startsWith("#") == false ||
			(colorString.length() != 7 && colorString.length() != 9)) {
			throw new GUIParserException("Invalid color '" + colorString + "'");
		}

		// jup, parse RGB first, then alpha
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "GUIColor [data=" + Arrays.toString(data) + "]";
	}

}

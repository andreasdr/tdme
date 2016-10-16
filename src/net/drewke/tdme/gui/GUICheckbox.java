package net.drewke.tdme.gui;

import net.drewke.tdme.utils.HashMap;

/**
 * GUI Checkbox
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUICheckbox extends GUIElement  {

	private static final String NAME = "checkbox";

	private static final String rootTemplate =
		"<layout padding='{$padding}' alignment='horizontal' width='{$width}' height='{$height}'>" +
		"	<element>" +
		"		<image effect-color-mul='#8080A0' hide-on='mouseover' src='resources/gui/images/elements/checkbox/checkbox_unchecked.png' />" +
		"		<image effect-color-mul='#8080FF' show-on='mouseover' src='resources/gui/images/elements/checkbox/checkbox_unchecked.png' />" +
		"	</element>" +
		"	<space width='{$separator-width}' />" +
		"	<text width='*' horizontal-align='{$text-horizontal-align}' font='resources/gui/fonts/Foo_25.fnt' text='{$text}' color='{$text-color}' />" +
		"</layout>";

	private HashMap<String, String> attributes;
	
	/**
	 * Constructor
	 */
	protected GUICheckbox() {
		attributes = new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getName()
	 */
	protected String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getTemplate(net.drewke.tdme.utils.HashMap)
	 */
	protected String getTemplate() {
		return rootTemplate;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.GUIElement#getAttributes()
	 */
	protected HashMap<String, String> getAttributes() {
		attributes.clear();
		attributes.put("padding", "5");
		attributes.put("width", "100%");
		attributes.put("height", "auto");
		attributes.put("separator-width", "10");
		attributes.put("text-horizontal-align", "left");
		attributes.put("text", "");
		attributes.put("text-color", "#000000");
		return attributes;
	}

}

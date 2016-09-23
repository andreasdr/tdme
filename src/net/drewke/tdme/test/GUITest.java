package net.drewke.tdme.test;

import net.drewke.tdme.gui.GUIParser;
import net.drewke.tdme.gui.GUIScreenNode;

/**
 * GUI test
 * @author Andreas Drewke
 * @version $Id$
 */
public class GUITest {

	/**
	 * Main
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		GUIScreenNode screen = GUIParser.parse("resources/gui/definitions", "button-example.xml");
		screen.setScreenSize(640, 480);
		screen.layout();
		System.out.println(screen);
		screen.setScreenSize(640, 480);
		screen.layout();
		System.out.println(screen);
	}

}

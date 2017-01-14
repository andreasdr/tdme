package net.drewke.tdme.tests;

import java.util.ArrayList;

import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMWriter;
import net.drewke.tdme.engine.model.Model;

/**
 * Model DAE level test
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelDAELevelTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Reading: resources/tests/levels/foxrun/FloorTile_withConnector.dae");
		ArrayList<Model> models = DAEReader.readAsLevel("resources/tests/levels/foxrun", "FloorTile_withConnector.dae");
		for (Model model: models) {
			System.out.println("Writing: resources/tests/models/tmtests/" + model.getName() + ".tm");
			TMWriter.write(model, "resources/tests/models/tmtests", model.getName() + ".tm");
		}
	}

}

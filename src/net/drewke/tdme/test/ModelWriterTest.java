package net.drewke.tdme.test;

import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMWriter;
import net.drewke.tdme.engine.model.Model;

/**
 * TDME model writer test
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelWriterTest {

	public static void main(String[] args) throws Exception {
		System.out.println("ModelWriterTest::reading resources/models/barrel/barrel.dae");
		Model barrel = DAEReader.read("resources/models/barrel", "barrel.dae");
		System.out.println("ModelWriterTest::writing resources/models/tmtests/barrel.tm");
		TMWriter.write(barrel, "resources/models/tmtests", "barrel.tm");
	}

}

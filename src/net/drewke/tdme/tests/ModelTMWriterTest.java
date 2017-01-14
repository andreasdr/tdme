package net.drewke.tdme.tests;

import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
import net.drewke.tdme.engine.fileio.models.TMWriter;
import net.drewke.tdme.engine.model.Model;

/**
 * Model TM writer test
 * @author Andreas Drewke
 * @version $Id$
 */
public class ModelTMWriterTest {

	public static void main(String[] args) throws Exception {
		System.out.println("ModelTMWriterTest::reading resources/tests/models/barrel/barrel.dae");
		Model barrel = DAEReader.read("resources/tests/models/barrel", "barrel.dae");
		System.out.println("ModelTMWriterTest::writing resources/tests/models/tmtests/barrel.tm");
		TMWriter.write(barrel, "resources/tests/models/tmtests", "barrel.tm");
		System.out.println("ModelTMWriterTest::reading resources/tests/models/dummy/testDummy_textured.DAE");
		Model dummy = DAEReader.read("resources/tests/models/dummy", "testDummy_textured.DAE");
		System.out.println("ModelTMWriterTest::writing resources/tests/models/tmtests/testDummy_textured.tm");
		TMWriter.write(dummy, "resources/tests/models/tmtests", "testDummy_textured.tm");
		System.out.println("ModelTMWriterTest::reading resources/tests/models/tmtests/testDummy_textured.tm");
		Model dummyTM = TMReader.read("resources/tests/models/tmtests", "testDummy_textured.tm");
		System.out.println(dummy);
		System.out.println(dummyTM);
	}

}

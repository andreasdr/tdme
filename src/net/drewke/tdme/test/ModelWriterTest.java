package net.drewke.tdme.test;

import net.drewke.tdme.engine.fileio.models.DAEReader;
import net.drewke.tdme.engine.fileio.models.TMReader;
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
		System.out.println("ModelWriterTest::reading resources/models/dummy/testDummy_textured.DAE");
		Model dummy = DAEReader.read("resources/models/dummy", "testDummy_textured.DAE");
		System.out.println("ModelWriterTest::writing resources/models/tmtests/testDummy_textured.tm");
		TMWriter.write(dummy, "resources/models/tmtests", "testDummy_textured.tm");
		System.out.println("ModelWriterTest::reading resources/models/tmtests/testDummy_textured.tm");
		Model dummyTM = TMReader.read("resources/models/tmtests", "testDummy_textured.tm");
		System.out.println(dummy);
		System.out.println(dummyTM);
	}

}

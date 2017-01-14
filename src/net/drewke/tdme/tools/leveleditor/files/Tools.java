package net.drewke.tdme.tools.leveleditor.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Tools class
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Tools {

	/**
	 * Reads a file into a string
	 * @param input stream
	 * @return file data
	 * @throws IOException
	 */
	public static String readStringFromFile(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

}

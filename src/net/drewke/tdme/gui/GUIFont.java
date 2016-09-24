package net.drewke.tdme.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import net.drewke.tdme.engine.Engine;
import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.fileio.textures.TextureLoader;
import net.drewke.tdme.os.FileSystem;

/**
 * GUI Font
 * 
 * A font implementation that will parse the output of the AngelCode font tool available at:
 * 
 * @see http://www.angelcode.com/products/bmfont/
 *
 * This implementation copes with both the font display and kerning information allowing nicer
 * looking paragraphs of text. Note that this utility only supports the text format definition
 * file.
 * 
 * @author kevin, Andreas Drewke
 * @version $Id$
 */
public class GUIFont {

	private final static int CHARACTERS_MAX = 10000;

	/**
	 * The definition of a single character as defined in the AngelCode file format
	 * 
	 * @author kevin, Andreas Drewke
	 * @version $Id$
	 */
	private class CharacterDefinition {
		/** The id of the character */
		public int id;
		/** The x location on the sprite sheet */
		public int x;
		/** The y location on the sprite sheet */
		public int y;
		/** The width of the character image */
		public int width;
		/** The height of the character image */
		public int height;
		/** The amount the x position should be offset when drawing the image */
		public int xoffset;
		/** The amount the y position should be offset when drawing the image */
		public int yoffset;
		/** The amount to move the current position after drawing the character */
		public int xadvance;
		
		/**
		 * Initialise the image by cutting the right section from the
		 * map produced by the AngelCode tool.
		 */
		public void init() {
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "[CharDef id="+id+" x="+x+" y="+y+"]";
		}

		/**
		 * Draw character
		 * @param gui renderer
		 * @param x
		 * @param y
		 */
		public void draw(GUIRenderer guiRenderer, int x, int y) {
			// screen dimension
			float screenWidth = guiRenderer.gui.width;
			float screenHeight = guiRenderer.gui.height;

			// element location and dimensions
			float left = x + xoffset;
			float top = y + yoffset;
			float width = this.width;
			float height = this.height;

			// texture dimension
			float textureWidth = texture.getTextureWidth();
			float textureHeight = texture.getTextureHeight();

			// element location and dimensions
			float textureCharLeft = this.x;
			float textureCharTop = this.y;
			float textureCharWidth = this.width;
			float textureCharHeight = this.height;

			// background color
			float[] fontColor = GUIColor.WHITE.getData();

			// render panel background
			guiRenderer.addQuad(
				((left) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top) / (screenHeight / 2f)) - 1f,  
				fontColor[0], fontColor[1], fontColor[2], fontColor[3],
				(textureCharLeft) / textureWidth, 
				(textureCharTop) / textureHeight,
				((left + width) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top) / (screenHeight / 2f)) - 1f,  
				fontColor[0], fontColor[1], fontColor[2], fontColor[3],
				(textureCharLeft + textureCharWidth) / textureWidth, 
				(textureCharTop) / textureHeight,
				((left + width) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
				fontColor[0], fontColor[1], fontColor[2], fontColor[3],
				(textureCharLeft + textureCharWidth) / textureWidth, 
				(textureCharTop + textureCharHeight) / textureHeight,
				((left) / (screenWidth / 2f)) - 1f, 
				((screenHeight - top - height) / (screenHeight / 2f)) - 1f,  
				fontColor[0], fontColor[1], fontColor[2], fontColor[3],
				(textureCharLeft) / textureWidth,
				(textureCharTop + textureCharHeight) / textureHeight
			);
		}
	}

	/** The image containing the bitmap font */
	private Texture texture;

	/** Texture id */
	private int textureId;

	/** The characters building up the font */
	private CharacterDefinition[] chars = new CharacterDefinition[CHARACTERS_MAX];

	/** The kerning information */
	/*
	private int[][] kerning = new int[CHARACTERS_MAX][CHARACTERS_MAX];
	*/

	/** The height of a line */
	private int lineHeight;

	/**
	 * Parse the font definition file
	 * @param font file
	 * @throws GUIParserException
	 */
	public static GUIFont parse(String pathName, String fileName) throws Exception {
		GUIFont font = new GUIFont();

		// now parse the font file
		BufferedReader in = new BufferedReader(new InputStreamReader(FileSystem.getInstance().getInputStream(pathName, fileName)));
		String info = in.readLine();
		String common = in.readLine();
		String page = in.readLine();

		// load texture
		font.texture = TextureLoader.loadTexture(
			pathName, 
			page.substring(
				page.indexOf("file=") + "file=\"".length(),
				page.lastIndexOf("\"")
			)
		);
		font.textureId = Engine.getInstance().getTextureManager().addTexture(font.texture);

		// parse
		boolean done = false;
		while (!done) {
			String line = in.readLine();
			if (line == null) {
				done = true;
			} else {
				if (line.startsWith("chars c")) {
					// ignore
				}
				else if (line.startsWith("char")) {
					CharacterDefinition def = font.parseCharacter(line);
					font.chars[def.id] = def;
				}
				if (line.startsWith("kernings c")) {
					// ignore
				}
				else if (line.startsWith("kerning")) {
					StringTokenizer tokens = new StringTokenizer(line," =");
					tokens.nextToken(); // kerning
					tokens.nextToken(); // first
					int first = Integer.parseInt(tokens.nextToken()); // first value
					tokens.nextToken(); // second
					int second = Integer.parseInt(tokens.nextToken()); // second value
					tokens.nextToken(); // offset
					int offset = Integer.parseInt(tokens.nextToken()); // offset value
					
					/*
					font.kerning[first][second] = offset;
					*/
				}
			}
		}
		return font;
	}
	
	/**
	 * Parse a single character line from the definition
	 * 
	 * @param line The line to be parsed
	 * @return The character definition from the line
	 */
	private CharacterDefinition parseCharacter(String line) {
		CharacterDefinition characterDefinition= new CharacterDefinition();
		StringTokenizer tokens = new StringTokenizer(line," =");
		
		tokens.nextToken(); // char
		tokens.nextToken(); // id
		characterDefinition.id = Integer.parseInt(tokens.nextToken()); // id value
		tokens.nextToken(); // x
		characterDefinition.x = Integer.parseInt(tokens.nextToken()); // x value
		tokens.nextToken(); // y
		characterDefinition.y = Integer.parseInt(tokens.nextToken()); // y value
		tokens.nextToken(); // width
		characterDefinition.width = Integer.parseInt(tokens.nextToken()); // width value
		tokens.nextToken(); // height
		characterDefinition.height = Integer.parseInt(tokens.nextToken()); // height value
		tokens.nextToken(); // x offset
		characterDefinition.xoffset = Integer.parseInt(tokens.nextToken()); // xoffset value
		tokens.nextToken(); // y offset
		characterDefinition.yoffset = Integer.parseInt(tokens.nextToken()); // yoffset value
		tokens.nextToken(); // xadvance
		characterDefinition.xadvance = Integer.parseInt(tokens.nextToken()); // xadvance
		
		characterDefinition.init();

		if (characterDefinition.id != ' ') {
			lineHeight = Math.max(characterDefinition.height+characterDefinition.yoffset, lineHeight);
		}
		
		return characterDefinition;
	}

	/**
	 * Draw string
	 * @param gui renderer
	 * @param x
	 * @param y
	 * @param text
	 * @param color
	 */
	public void drawString(GUIRenderer guiRenderer, int x, int y, String text, GUIColor color) {
		guiRenderer.bindTexture(textureId);
		guiRenderer.setFontColor(color);
		for (int i=0;i < text.length(); i++) {
			int id = text.charAt(i);
			if (id >= chars.length) {
				continue;
			}
			if (chars[id] == null) {
				continue;
			}
			chars[id].draw(guiRenderer, x, y);
			x += chars[id].xadvance;
			/*
			if (i < text.length()-1) {
				if ((text.charAt(i+1) < CHARACTERS_MAX) && (id < CHARACTERS_MAX)) {
					x += kerning[id][text.charAt(i+1)];
				}
			}
			*/
		}
		guiRenderer.render();
	}

	/**
	 * Get the offset from the draw location the font will place glyphs
	 * 
	 * @param text The text that is to be tested
	 * @return The yoffset from the y draw location at which text will start
	 */
	public int getYOffset(String text) {
		int minYOffset = 10000;
		for (int i=0;i < text.length(); i++) {
			int id = text.charAt(i);
			if (chars[id] == null) {
				continue;
			}
			minYOffset = Math.min(chars[id].yoffset,minYOffset);
		}
		
		return minYOffset;
	}

	/**
	 * Text height
	 * @param text
	 * @return text height
	 */
	public int getTextHeight(String text) {
		int maxHeight = 0;
		for (int i=0;i < text.length();i++) {
			int id = text.charAt(i);
			if (chars[id] == null) {
				continue;
			}
			// ignore space, it doesn't contribute to height
			if (id == ' ') {
				continue;
			}
			maxHeight = Math.max(chars[id].height+chars[id].yoffset, maxHeight);
		}
		return maxHeight;
	}

	/**
	 * Text width
	 * @param text
	 * @return text width
	 */
	public int getTextWidth(String text) {
		int width = 0;
		for (int i=0;i < text.length();i++) {
			int id = text.charAt(i);
			if (chars[id] == null) {
				continue;
			}
			width += chars[id].xadvance;
			/*
			if (i < text.length()-1) {
				width += kerning[id][text.charAt(i+1)];
			}
			*/
		}
		
		return width;
	}

	/**
	 * @return line height
	 */
	public int getLineHeight() {
		return lineHeight;
	}
	
}

package net.drewke.tdme.engine.fileio.textures;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Minimal PNG Encoder
 * 	based on: PNG encoder (C) 2006-2009 by Christian Froeschlin, www.chrfr.de
 * @author Christian Froeschlin, Andreas Drewke
 * @version $Id$
 */
public final class PNG {

	/**
	 * ZLIB helper class
	 */
	protected static class ZLIB {

		static final int BLOCK_SIZE = 32000;

		/**
		 * Write data to ZLib
		 * @param raw
		 * @return byte
		 * @throws IOException
		 */
		public static byte[] toZLIB(byte[] raw) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length
					+ 6 + (raw.length / BLOCK_SIZE) * 5);
			DataOutputStream zlib = new DataOutputStream(baos);

			byte tmp = (byte) 8;
			zlib.writeByte(tmp); // CM = 8, CMINFO = 0
			zlib.writeByte((31 - ((tmp << 8) % 31)) % 31); // FCHECK
															// (FDICT/FLEVEL=0)

			int pos = 0;
			while (raw.length - pos > BLOCK_SIZE) {
				writeUncompressedDeflateBlock(zlib, false, raw, pos,
						(char) BLOCK_SIZE);
				pos += BLOCK_SIZE;
			}

			writeUncompressedDeflateBlock(zlib, true, raw, pos,
					(char) (raw.length - pos));

			// zlib check sum of uncompressed data
			zlib.writeInt(calcADLER32(raw));

			return baos.toByteArray();
		}

		/**
		 * Writes a uncompressed deflate block
		 * @param zlib
		 * @param last
		 * @param raw
		 * @param off
		 * @param len
		 * @throws IOException
		 */
		private static void writeUncompressedDeflateBlock(
				DataOutputStream zlib, boolean last, byte[] raw, int off,
				char len) throws IOException {
			zlib.writeByte((byte) (last ? 1 : 0)); // Final flag, Compression
													// type 0
			zlib.writeByte((byte) (len & 0xFF)); // Length LSB
			zlib.writeByte((byte) ((len & 0xFF00) >> 8)); // Length MSB
			zlib.writeByte((byte) (~len & 0xFF)); // Length 1st complement LSB
			zlib.writeByte((byte) ((~len & 0xFF00) >> 8)); // Length 1st
															// complement MSB
			zlib.write(raw, off, len); // Data
		}

		/**
		 * Calculate adler 32
		 * @param raw
		 * @return value
		 */
		private static int calcADLER32(byte[] raw) {
			int s1 = 1;
			int s2 = 0;
			for (int i = 0; i < raw.length; i++) {
				int abs = raw[i] >= 0 ? raw[i] : (raw[i] + 256);
				s1 = (s1 + abs) % 65521;
				s2 = (s2 + s1) % 65521;
			}
			return (s2 << 16) + s1;
		}
	}

	/**
	 * Saves a RGBA pixel data to stream
	 * @param width
	 * @param height
	 * @param pixels
	 * @return
	 * @throws IOException
	 */
	public static void save(int width, int height, ByteBuffer pixels, OutputStream out) throws IOException {
		int[] crcTable = createCRCTable();
		byte[] signature = new byte[] {
			(byte) 137, (byte) 80, (byte) 78,
			(byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10
		};
		byte[] header = createHeaderChunk(width, height, crcTable);
		byte[] data = createDataChunk(width, height, pixels, crcTable);
		byte[] trailer = createTrailerChunk(crcTable);
		out.write(signature);
		out.write(header);
		out.write(data);
		out.write(trailer);
	}

	/**
	 * Create PNG header
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	private static byte[] createHeaderChunk(int width, int height, int[] crcTable) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(13);
		DataOutputStream chunk = new DataOutputStream(baos);
		chunk.writeInt(width);
		chunk.writeInt(height);
		chunk.writeByte(8); // Bitdepth
		chunk.writeByte(6); // Colortype ARGB
		chunk.writeByte(0); // Compression
		chunk.writeByte(0); // Filter
		chunk.writeByte(0); // Interlace
		return toChunk("IHDR", baos.toByteArray(), crcTable);
	}

	/**
	 * Create a PNG data chunkg
	 * @param width
	 * @param height
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 * @throws IOException
	 */
	private static byte[] createDataChunk(int width, int height, ByteBuffer pixels, int[] crcTable) throws IOException {
		int dest;
		byte[] raw = new byte[4 * (width * height) + height];
		for (int y = height - 1; y >= 0; y--) {
			dest = (y * width) * 4 + y;
			raw[dest++] = 0; // No filter 
			for (int x = 0; x < width; x++) {
				raw[dest++] = pixels.get();
				raw[dest++] = pixels.get();
				raw[dest++] = pixels.get();
				raw[dest++] = (byte)0xFF;
				pixels.get();
			}
		}
		return toChunk("IDAT", toZLIB(raw), crcTable);
	}

	/**
	 * Creates a PNG trailer chunk
	 * @return
	 * @throws IOException
	 */
	private static byte[] createTrailerChunk(int[] crcTable) throws IOException {
		return toChunk("IEND", new byte[] {}, crcTable);
	}

	/**
	 * Writes an PNG chunk
	 * @param id
	 * @param raw
	 * @return
	 * @throws IOException
	 */
	private static byte[] toChunk(String id, byte[] raw, int[] crcTable) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length + 12);
		DataOutputStream chunk = new DataOutputStream(baos);

		chunk.writeInt(raw.length);

		byte[] bid = new byte[4];
		for (int i = 0; i < 4; i++) {
			bid[i] = (byte) id.charAt(i);
		}

		chunk.write(bid);

		chunk.write(raw);

		int crc = 0xFFFFFFFF;
		crc = updateCRC(crc, bid, crcTable);
		crc = updateCRC(crc, raw, crcTable);
		chunk.writeInt(~crc);

		return baos.toByteArray();
	}

	/**
	 * Create CRC table
	 * @return crc table
	 */
	private static int[] createCRCTable() {
		int[] crcTable = new int[256];

		for (int i = 0; i < 256; i++) {
			int c = i;
			for (int k = 0; k < 8; k++) {
				c = ((c & 1) > 0) ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
			}
			crcTable[i] = c;
		}

		return crcTable;
	}

	/**
	 * Update CRC
	 * @param crc
	 * @param raw
	 * @param crc table
	 * @return crc
	 */
	private static int updateCRC(int crc, byte[] raw, int[] crcTable) {
		for (int i = 0; i < raw.length; i++) {
			crc = crcTable[(crc ^ raw[i]) & 0xFF] ^ (crc >>> 8);
		}

		return crc;
	}

	/*
	 * This method is called to encode the image data as a zlib block as
	 * required by the PNG specification. This file comes with a minimal ZLIB
	 * encoder which uses uncompressed deflate blocks (fast, short, easy, but no
	 * compression). If you want compression, call another encoder (such as
	 * JZLib?) here.
	 */
	private static byte[] toZLIB(byte[] raw) throws IOException {
		return ZLIB.toZLIB(raw);
	}

}

package net.drewke.tdme.utils;

import java.util.Arrays;

/**
 * Key
 * @author Andreas Drewke
 * @version $Id$
 */
public final class Key {

	private final static int LENGTH_MAX = 512;
	private int length;
	private int hash;
	private char[] data;

	/**
	 * Constructor
	 */
	public Key() {
		length = 0;
		hash = 0;
		data = new char[LENGTH_MAX];
	}

	/**
	 * Reset
	 */
	public void reset() {
		length = 0;
		hash = 0;
	}

	/**
	 * Append string
	 * @param string
	 */
	public void append(String string) {
		if (length + string.length() > LENGTH_MAX) {
			System.out.println("Key.append: key too long");
		}
		string.getChars(0, string.length(), data, length);
		length+= string.length();
		hash = 0;
	}

	/**
	 * Append float value
	 * @param value
	 */
	public void append(float value) {
		if (length + 4 > LENGTH_MAX) {
			System.out.println("Key.append: key too long");
		}
		int intValue = Float.floatToIntBits(value);
		data[length++] = (char)((intValue) & 0xFF);
		data[length++] = (char)((intValue >> 8) & 0xFF);
		data[length++] = (char)((intValue >> 16) & 0xFF);
		data[length++] = (char)((intValue >> 24) & 0xFF);
		hash = 0;
	}

	/**
	 * Append int value
	 * @param value
	 */
	public void append(int value) {
		if (length + 4 > LENGTH_MAX) {
			System.out.println("Key.append: key too long");
		}
		data[length++] = (char)((value) & 0xFF);
		data[length++] = (char)((value >> 8) & 0xFF);
		data[length++] = (char)((value >> 16) & 0xFF);
		data[length++] = (char)((value >> 24) & 0xFF);
		hash = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int h = hash;
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) {
				h = 31 * h + data[i];
			}
			hash = h;
		}
		return h;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		// check for same object
		if (this == object) return true;

		// type
		if (object instanceof Key == false) return false;
		Key key2 = (Key)object;

		// check length
		if (length != key2.length) return false;

		// check characters
		for (int i = 0; i < length; i++)
		if (data[i] != key2.data[i]) {
			return false;
		}

		// we are done
		return true;
	}

	/**
	 * Clones this key into key2
	 * @param key 2
	 */
	public void cloneInto(Key key2) {
		key2.length = length;
		key2.hash = hash;
		System.arraycopy(data, 0, key2.data, 0, length);
	}

	/**
	 * @return key data
	 */
	public char[] getData() {
		return data;
	}

	/**
	 * @return key length
	 */
	public int getLength() {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = "\"";
		for (int i = 0; i < length; i++) {
			tmp+= data[i];
		}
		tmp+="\"";
		//tmp+="(length " + length + ", hash value " + hashCode() + ")";
		return tmp;
	}

}

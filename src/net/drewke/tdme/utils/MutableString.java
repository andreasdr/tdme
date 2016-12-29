package net.drewke.tdme.utils;

import java.util.Arrays;

/**
 * Mutable string
 * @author Andreas Drewke
 * @version $Id$
 */
public final class MutableString {

	private char[] data;
	private int length;
	private int hash;

	/**
	 * Public default constructor
	 */
	public MutableString() {
		this.data = new char[64];
		this.length = 0;
		this.hash = 0;
	}

	/**
	 * Public constructor
	 */
	public MutableString(String s) {
		this.data = new char[64];
		this.length = 0;
		this.hash = 0;
		append(s);
	}

	/**
	 * Grow string backing array
	 */
	private void grow() {
		char[] dataNew = new char[data.length + 64];
		System.arraycopy(data, 0, dataNew, 0, length);
		data = dataNew;
	}

	/**
	 * @return length
	 */
	public int length() {
		return length;
	}

	/**
	 * Get char at index
	 * @param idx
	 * @return char
	 */
	public char charAt(int idx) {
		return data[idx];
	}

	/**
	 * Reset
	 */
	public void reset() {
		Arrays.fill(data, '\0');
		length = 0;
		hash = 0;
	}

	/**
	 * Set character
	 * @param c
	 */
	public void set(char c) {
		reset();
		append(c);
	}

	/**
	 * Append character
	 * @param c
	 */
	public void append(char c) {
		if (length + 1 >= data.length) {
			grow();
		}
		data[length++] = c;
		hash = 0;
	}

	/**
	 * Insert character c at idx
	 * @param c
	 */
	public void insert(int idx, char c) {
		if (length + 1 >= data.length) {
			grow();
		}
		System.arraycopy(data, idx, data, idx + 1, length - idx);
		data[idx] = c;
		length++;
		hash = 0;
	}

	/**
	 * Set string
	 * @param s
	 */
	public void set(String s) {
		reset();
		append(s);
	}

	/**
	 * Append string
	 * @param s
	 */
	public void append(String s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
	}

	/**
	 * Insert string at idx
	 * @param c
	 */
	public void insert(int idx, String s) {
		for (int i = 0; i < s.length(); i++) {
			insert(idx + i, s.charAt(i));
		}
	}
 
	/**
	 * Set mutable string 
	 * @param s
	 */
	public void set(MutableString s) {
		reset();
		append(s);
	}

	/**
	 * Append mutable string
	 * @param s
	 */
	public void append(MutableString s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
	}

	/**
	 * Insert mutable string at idx
	 * @param c
	 */
	public void insert(int idx, MutableString s) {
		for (int i = 0; i < s.length(); i++) {
			insert(idx + i, s.charAt(i));
		}
	}

	/**
	 * Set integer
	 * @param i
	 */
	public void set(int i) {
		reset();
		append(i);
	}

	/**
	 * Append integer
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 */
	public void append(int i) {
		insert(length, i);
	}

	/**
	 * Insert integer at idx
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 */
	public void insert(int idx, int i) {
		boolean negative = false;
		if (i < 0) {
			negative = true;
			i = -i;
		}
		while (true == true) {
			int remainder = i % 10;
			i = i / 10;
			insert(idx, (char)('0' + remainder));
			if (i == 0) {
				break;
			}
		}
		if (negative == true) {
			insert(idx, '-');
		}
	}

	/**
	 * Set float
	 * @param f
	 * @param decimals
	 */
	public void set(float f, int decimals) {
		reset();
		append(f, decimals);
	}

	/**
	 * Append float with given decimals
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param f
	 * @param decimals
	 */
	public void append(float f, int decimals) {
		insert(length, f, decimals);
	}

	/**
	 * Insert float at idx
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 */
	public void insert(int idx, float f, int decimals) {
		int integer = (int)f;
		int integerDecimals = (int)((f - integer) * Math.pow(10f, decimals));
		if (integerDecimals < 0f) integerDecimals = -integerDecimals;
		if (decimals > 0) {
			insert(idx, integerDecimals);
			insert(idx, '.');
		}
		insert(idx, integer);
	}

	/**
	 * Delete characters at idx with given length
	 * @param idx
	 * @param length
	 */
	public void delete(int idx, int count) {
		if (count + idx > length) return; 
		System.arraycopy(data, idx + count, data, idx, length - count - idx);
		length-= count;
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

	/**
	 * Equals 
	 * @param string 2
	 * @return string 2 equals this string
	 */
	public boolean equals(MutableString s2) {
		if (length != s2.length) return false;
		for (int i = 0; i < length; i++) {
			if (data[i] != s2.data[i]) return false;
		}
		return true;
	}

	/**
	 * Equals 
	 * @param string 2
	 * @return string 2 equals this string
	 */
	public boolean equals(String s2) {
		if (length != s2.length()) return false;
		for (int i = 0; i < length; i++) {
			if (data[i] != s2.charAt(i)) return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new String(data, 0, length);
	}

}

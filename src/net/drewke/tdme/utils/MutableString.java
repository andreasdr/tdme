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
	public MutableString reset() {
		Arrays.fill(data, '\0');
		length = 0;
		hash = 0;
		return this;
	}

	/**
	 * Set character
	 * @param c
	 * @return this mutable string
	 */
	public MutableString set(char c) {
		reset();
		append(c);
		return this;
	}

	/**
	 * Append character
	 * @param c
	 * @return this mutable string
	 */
	public MutableString append(char c) {
		if (length + 1 >= data.length) {
			grow();
		}
		data[length++] = c;
		hash = 0;
		return this;
	}

	/**
	 * Insert character c at idx
	 * @param c
	 * @return this mutable string
	 */
	public MutableString insert(int idx, char c) {
		if (length + 1 >= data.length) {
			grow();
		}
		System.arraycopy(data, idx, data, idx + 1, length - idx);
		data[idx] = c;
		length++;
		hash = 0;
		return this;
	}

	/**
	 * Set string
	 * @param s
	 * @return this mutable string
	 */
	public MutableString set(String s) {
		reset();
		append(s);
		return this;
	}

	/**
	 * Append string
	 * @param s
	 * @return this mutable string
	 */
	public MutableString append(String s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
		return this;
	}

	/**
	 * Insert string at idx
	 * @param c
	 * @return this mutable string
	 */
	public MutableString insert(int idx, String s) {
		for (int i = 0; i < s.length(); i++) {
			insert(idx + i, s.charAt(i));
		}
		return this;
	}
 
	/**
	 * Set mutable string 
	 * @param s
	 * @return this mutable string
	 */
	public MutableString set(MutableString s) {
		reset();
		append(s);
		return this;
	}

	/**
	 * Append mutable string
	 * @param s
	 * @return this mutable string
	 */
	public MutableString append(MutableString s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
		return this;
	}

	/**
	 * Insert mutable string at idx
	 * @param c
	 * @return this mutable string
	 */
	public MutableString insert(int idx, MutableString s) {
		for (int i = 0; i < s.length(); i++) {
			insert(idx + i, s.charAt(i));
		}
		return this;
	}

	/**
	 * Set integer
	 * @param i
	 * @return this mutable string
	 */
	public MutableString set(int i) {
		reset();
		append(i);
		return this;
	}

	/**
	 * Append integer
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 * @return this mutable string
	 */
	public MutableString append(int i) {
		insert(length, i);
		return this;
	}

	/**
	 * Insert integer at idx
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 * @return this mutable string
	 */
	public MutableString insert(int idx, int i) {
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
		return this;
	}

	/**
	 * Set float
	 * @param f
	 * @param decimals
	 * @return this mutable string
	 */
	public MutableString set(float f, int decimals) {
		reset();
		append(f, decimals);
		return this;
	}

	/**
	 * Append float with given decimals
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param f
	 * @param decimals
	 * @return this mutable string
	 */
	public MutableString append(float f, int decimals) {
		insert(length, f, decimals);
		return this;
	}

	/**
	 * Insert float at idx
	 * @see http://stackoverflow.com/questions/7123490/how-compiler-is-converting-integer-to-string-and-vice-versa
	 * @param i
	 * @return this mutable string
	 */
	public MutableString insert(int idx, float f, int decimals) {
		int integer = (int)f;
		int integerDecimals = (int)((f - integer) * Math.pow(10f, decimals));
		if (integerDecimals < 0f) integerDecimals = -integerDecimals;
		for (int i = 0; i < decimals; i++) {
			int integerDecimal = (int)((f - integer) * Math.pow(10f, i + 1)) - (10 * (int)((f - integer) * Math.pow(10f, i)));
			insert(idx+i, integerDecimal);
		}
		insert(idx, '.');
		insert(idx, integer);
		return this;
	}

	/**
	 * Delete characters at idx with given length
	 * @param idx
	 * @param length
	 * @return this mutable string
	 */
	public MutableString delete(int idx, int count) {
		if (count + idx > length) return this; 
		System.arraycopy(data, idx + count, data, idx, length - count - idx);
		length-= count;
		hash = 0;
		return this;
	}

	/**
	 * Returns the character index where string s have been found or -1 if not found 
	 * @param string
	 * @param index
	 * @return index where string has been found or -1
	 */
	public int indexOf(MutableString s, int idx) {
		for (int i = idx; i < length; i++) {
			boolean found = true;
			for (int j = 0; j < s.length; j++) {
				if (i + j >= length) {
					found = false;
					break;
				};
				if (data[i + j] != s.data[j]) {
					found = false;
					break;
				}
			}
			if (found == true) {
				return i;
			}
		}

		// not found
		return -1;
	}

	/**
	 * Returns the character index where string s have been found or -1 if not found 
	 * @param string
	 * @return index where string has been found or -1
	 */
	public int indexOf(MutableString s) {
		return indexOf(s, 0);
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
	public boolean equals(String s2) {
		if (length != s2.length()) return false;
		for (int i = 0; i < length; i++) {
			if (data[i] != s2.charAt(i)) return false;
		}
		return true;
	}

	/**
	 * Equals
	 * @param string 2
	 * @return string 2 equals this string
	 */
	public boolean equals(MutableString s2) {
		if (this == s2) return true;
		if (length != s2.length) return false;
		for (int i = 0; i < length; i++) {
			if (data[i] != s2.data[i]) return false;
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

package net.drewke.tdme.utils;

import java.util.Iterator;

/**
 * Array list implementation
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ArrayList<V> implements Iterator<V>, Iterable<V> {

	private int capacity;
	private int size;
	private V[] elements;

	private int iteratorIdx;

	/**
	 * Public constructor
	 */
	public ArrayList() {
		capacity = 32;
		size = 0;
		elements = (V[])new Object[capacity];
		iteratorIdx = 0;
	}

	/**
	 * Public constructor
	 */
	public ArrayList(V[] values) {
		capacity = 32;
		size = 0;
		elements = (V[])new Object[capacity];
		iteratorIdx = 0;
		for (int i = 0; i < values.length; i++) {
			add(values[i]);
		}
	}

	/**
	 * Grow
	 */
	private void grow() {
		V[] elementsOld = elements;
		capacity*= 2;
		elements = (V[])new Object[capacity];
		for (int i = 0; i < size; i++) {
			elements[i] = elementsOld[i];			
		}
	}

	/**
	 * @return is empty
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * @return size or number of stored elements
	 */
	public int size() {
		return size;
	}

	/**
	 * @return capacity
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * Clear
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Add a value
	 * @param value
	 */
	public void add(V value) {
		if (size == capacity) {
			grow();
		}
		elements[size++] = value;
	}

	/**
	 * Add all elements from given array list
	 * @param values
	 */
	public void addAll(ArrayList<V> values) {
		for (int i = 0; i < values.size; i++) {
			add(values.elements[i]);
		}
	}

	/**
	 * Set value at given index
	 * @param idx
	 * @param value
	 */
	public V set(int idx, V value) {
		V oldValue = elements[idx];
		elements[idx] = value;
		return oldValue;
	}

	/**
	 * Remove by index
	 * @param idx
	 * @return removed value
	 */
	public V remove(int idx) {
		V value = elements[idx];
		System.arraycopy(elements, idx + 1, elements, idx, size - idx - 1);
		size--;
		return value;
	}

	/**
	 * Remove by value
	 * @param value
	 * @return if element has been removed
	 */
	public boolean remove(V value) {
		if (value == null) {
			for (int i = 0; i < size; i++) {
				if (elements[i] == null) {
					remove(i);
					return true;
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				if (elements[i] != null && elements[i].equals(value)) {
					remove(i);
					return true;
				}
			}			
		}
		return false;
	}

	/**
	 * Gets an element
	 * @param idx
	 * @return value
	 */
	public V get(int idx) {
		return elements[idx];
	}

	/**
	 * Check if value is contained in array
	 * @param value
	 * @return array list contains value
	 */
	public boolean contains(V value) {
		if (value == null) {
			for (int i = 0; i < size; i++) {
				if (elements[i] == null) return true;
			}
		} else {
			for (int i = 0; i < size; i++) {
				if (elements[i] != null && elements.equals(value) == true) return true;
			}
		}
		return false;
	}

	/**
	 * Fill given array with data from this array list
	 * @param array
	 * @return array
	 */
	public V[] toArray(V[] array) {
		for (int i = 0; i < size; i++) {
			array[i] = elements[i];
		}
		return array;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<V> iterator() {
		iteratorIdx = 0;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return iteratorIdx < size;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public V next() {
		return elements[iteratorIdx++];
	}

	/**
	 * Clones this array list
	 */
	public ArrayList<V> clone() {
		ArrayList clone = new ArrayList<V>();
		clone.capacity = capacity;
		clone.size = size;
		clone.elements = (V[])new Object[clone.capacity];
		clone.iteratorIdx = 0;
		for (int i = 0; i < size; i++) {
			clone.elements[i] = elements[i];
		}
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "[";
		for (int i = 0; i < size; i++) {
			result+= elements[i];
			if (i < size - 1) result+=", ";
		}
		result+= "]";
		return result;
	}

}

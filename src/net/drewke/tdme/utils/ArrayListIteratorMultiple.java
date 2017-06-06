package net.drewke.tdme.utils;

import java.util.Iterator;

/**
 * Array list iterator for multiple array lists
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ArrayListIteratorMultiple<E> implements Iterator<E>, Iterable<E> {
	int vectorIdx;
	int elementIdx;
	int length;
	ArrayList<ArrayList<E>> arrayLists;

	/**
	 * Public constructor
	 * @param vector
	 */
	public ArrayListIteratorMultiple() {
		this.arrayLists = new ArrayList<ArrayList<E>>();
		reset();
	}

	/**
	 * Public constructor
	 * @param vector
	 */
	public ArrayListIteratorMultiple(ArrayList<ArrayList<E>> vectors) {
		this.arrayLists = vectors;
		reset();
	}

	/**
	 * Clears list of vectors to iterate
	 */
	public void clear() {
		arrayLists.clear();
	}

	/**
	 * Adds a vector to iterate
	 * @param vector
	 */
	public void addVector(ArrayList<E> vector) {
		if (vector == null) return;
		if (vector.size() == 0) return;
		for (int i = 0; i < arrayLists.size(); i++) {
			if (arrayLists.get(i) == vector) return;
		}
		arrayLists.add(vector);
	}

	/**
	 * resets vector iterator for iterating
	 * @return this vector iterator
	 */
	public ArrayListIteratorMultiple<E> reset() {
		this.vectorIdx = 0;
		this.elementIdx = 0;
		this.length = 0;
		for (int i = 0; i < arrayLists.size(); i++) {
			this.length+= arrayLists.get(i).size();
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		boolean hasNext =
			(vectorIdx < arrayLists.size() - 1) ||
			(vectorIdx == arrayLists.size() - 1 && elementIdx < arrayLists.get(vectorIdx).size());
		return hasNext;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public E next() {
		E element = arrayLists.get(vectorIdx).get(elementIdx++);
		if (elementIdx == arrayLists.get(vectorIdx).size()) {
			elementIdx = 0;
			vectorIdx++;
		}
		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<E> iterator() {
		reset();
		return this;
	}

	/**
	 * Clones this iterator
	 */
	public ArrayListIteratorMultiple<E> clone() {
		return new ArrayListIteratorMultiple<E>((ArrayList<ArrayList<E>>)arrayLists.clone());
	}

}

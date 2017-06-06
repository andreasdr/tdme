package net.drewke.tdme.utils;

import java.util.Iterator;

/**
 * Array list iterator for multiple array lists
 * @author Andreas Drewke
 * @version $Id$
 */
public final class ArrayListIteratorMultiple<E> implements Iterator<E>, Iterable<E> {

	private int vectorIdx;
	private int elementIdx;
	private int length;
	private ArrayList<ArrayList<E>> arrayLists;

	/**
	 * Public constructor
	 */
	public ArrayListIteratorMultiple() {
		this.arrayLists = new ArrayList<ArrayList<E>>();
		reset();
	}

	/**
	 * Public constructor
	 * @param array lists
	 */
	public ArrayListIteratorMultiple(ArrayList<ArrayList<E>> arrayLists) {
		this.arrayLists = arrayLists;
		reset();
	}

	/**
	 * Clears list of array lists to iterate
	 */
	public void clear() {
		arrayLists.clear();
	}

	/**
	 * Adds array lists to iterate
	 * @param array lists
	 */
	public void addArrayList(ArrayList<E> _arrayLists) {
		if (_arrayLists == null) return;
		if (_arrayLists.size() == 0) return;
		for (int i = 0; i < arrayLists.size(); i++) {
			if (arrayLists.get(i) == _arrayLists) return;
		}
		arrayLists.add(_arrayLists);
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

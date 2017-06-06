package net.drewke.tdme.utils;

import java.util.Iterator;

/**
 * Reusable not threadsafe array list iterator 
 * @author Andreas Drewke
 * @version $Id$
 * @param <E>
 */
public final class ArrayListIterator<E> implements Iterator<E>, Iterable<E> {

	int idx;
	int length;
	ArrayList<E> arrayList;

	/**
	 * Public constructor
	 * @param vector
	 */
	public ArrayListIterator(ArrayList<E> arrayList) {
		this.arrayList = arrayList;
		reset();
	}

	/**
	 * resets vector iterator for iterating
	 * @return this vector iterator
	 */
	public ArrayListIterator<E> reset() {
		this.idx = 0;
		this.length = arrayList.size();
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return idx < length;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public E next() {
		return arrayList.get(idx++);
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
	public ArrayListIterator<E> clone() {
		return new ArrayListIterator<E>(arrayList);
	}

}
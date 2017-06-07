package net.drewke.tdme.utils;

import java.util.Iterator;
import java.util.Vector;

/**
 * Reusable not threadsafe vector iterator 
 * @author Andreas Drewke
 * @version $Id$
 * @param <E>
 */
public final class VectorIterator<E> implements Iterator<E>, Iterable<E> {

	int idx;
	int length;
	Vector<E> vector;

	/**
	 * Public constructor
	 * @param vector
	 */
	public VectorIterator(Vector<E> vector) {
		this.vector = vector;
		reset();
	}

	/**
	 * resets vector iterator for iterating
	 * @return this vector iterator
	 */
	public VectorIterator<E> reset() {
		this.idx = 0;
		this.length = vector.size();
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
		return vector.elementAt(idx++);
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
	public VectorIterator<E> clone() {
		return new VectorIterator<E>(vector);
	}

}

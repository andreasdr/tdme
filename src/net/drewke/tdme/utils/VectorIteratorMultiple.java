package net.drewke.tdme.utils;

import java.util.Iterator;
import java.util.Vector;

/**
 * Vector iterator for multiple vectors
 * @author Andreas Drewke
 * @version $Id$
 */
public final class VectorIteratorMultiple<E> implements Iterator<E>, Iterable<E> {
	int vectorIdx;
	int elementIdx;
	int length;
	Vector<Vector<E>> vectors;

	/**
	 * Public constructor
	 * @param vector
	 */
	public VectorIteratorMultiple() {
		this.vectors = new Vector<Vector<E>>();
		reset();
	}

	/**
	 * Public constructor
	 * @param vector
	 */
	public VectorIteratorMultiple(Vector<Vector<E>> vectors) {
		this.vectors = vectors;
		reset();
	}

	/**
	 * Clears list of vectors to iterate
	 */
	public void clear() {
		vectors.clear();
	}

	/**
	 * Adds a vector to iterate
	 * @param vector
	 */
	public void addVector(Vector<E> vector) {
		if (vector == null) return;
		if (vectors.contains(vector) == false) {
			vectors.add(vector);
		}
	}

	/**
	 * resets vector iterator for iterating
	 * @return this vector iterator
	 */
	public VectorIteratorMultiple<E> reset() {
		this.vectorIdx = 0;
		this.elementIdx = 0;
		this.length = 0;
		for (int i = 0; i < vectors.size(); i++) {
			this.length+= vectors.get(i).size();
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		boolean hasNext =
			(vectorIdx < vectors.size() - 1) ||
			(vectorIdx == vectors.size() - 1 && elementIdx < vectors.get(vectorIdx).size());
		return hasNext;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public E next() {
		// System.out.println(vectorIdx + "(" + vectors.size() + ")" + "," + elementIdx + "(" + vectors.get(vectorIdx).size() + ")");
		E element = vectors.get(vectorIdx).elementAt(elementIdx++);
		if (elementIdx == vectors.get(vectorIdx).size()) {
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
	public VectorIteratorMultiple<E> clone() {
		return new VectorIteratorMultiple<E>((Vector<Vector<E>>)vectors.clone());
	}

}

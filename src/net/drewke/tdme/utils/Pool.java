package net.drewke.tdme.utils;

import java.util.ArrayList;

/**
 * Pool 
 * @author Andreas Drewke
 * @version $Id$
 * @param <E>
 */
abstract public class Pool<E> {

	private ArrayList<E> freeElements;
	private ArrayList<E> usedElements;

	/**
	 * Public constructor
	 */
	public Pool() {
		freeElements = new ArrayList<E>();
		usedElements = new ArrayList<E>();
	}

	/**
	 * Allocate a new element from pool
	 * @return element
	 */
	final public E allocate() {
		if (freeElements.isEmpty() == false) {
			E element = freeElements.remove(freeElements.size() - 1);
			usedElements.add(element);
			return element;
		}
		E element = instantiate();
		usedElements.add(element);
		return element;
	}

	/**
	 * Instantiate element
	 */
	abstract public E instantiate(); 

	/**
	 * Release element in pool for being reused
	 * @param element
	 */
	final public void release(E element) {
		for (int i = 0; i < usedElements.size(); i++) {
			if (usedElements.get(i) == element) {
				usedElements.remove(i);
				freeElements.add(element);
				return;
			}
		}
		Console.println("Pool::release()::did not find:" + element);
	}

	/**
	 * @return element capacity
	 */
	final public int capacity() {
		return usedElements.size() + freeElements.size();
	}

	/**
	 * @return elements in use
	 */
	final public int size() {
		return usedElements.size();
	}

	/**
	 * Reset this pool
	 */
	final public void reset() {
		for (int i = 0; i < usedElements.size(); i++) {
			freeElements.add(usedElements.get(i));
		}
		usedElements.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	final public String toString() {
		return "Pool [size=" + size() + ", capacity=" + capacity() + "]";
	}

}

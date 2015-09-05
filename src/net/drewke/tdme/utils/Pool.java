package net.drewke.tdme.utils;

import java.util.ArrayList;

/**
 * Pool 
 * @author Andreas Drewke
 * @version $Id$
 * @param <E>
 */
abstract public class Pool<E> {

	/**
	 * Pool element
	 * @author Andreas Drewke
	 * @version $Id$
	 * @param <E>
	 */
	private static class PoolElement<E> {
		private boolean inUse;
		private E element;
	}

	private ArrayList<PoolElement<E>> elements;
	private int elementCount;

	/**
	 * Public constructor
	 */
	public Pool() {
		elements = new ArrayList<PoolElement<E>>();
		elementCount = 0;
	}

	/**
	 * Allocate a new element from pool
	 * @return element
	 */
	final public E allocate() {
		// try to find free element in pool
		for (int i = 0; i < elements.size(); i++) {
			PoolElement<E> element = elements.get(i);
			// do we have one?
			if (element.inUse == false) {
				// yes
				element.inUse = true;
				elementCount++;
				return element.element;
			}
		}
		// otherwise add new element
		elements.add(new PoolElement<E>());
		PoolElement<E> element = elements.get(elements.size() - 1);
		element.inUse = true;
		element.element = instantiate();
		elementCount++;
		return element.element;
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
		for (int i = 0; i < elements.size(); i++) {
			PoolElement<E> _element = elements.get(i);
			if (_element.inUse == true && _element.element == element) {
				_element.inUse = false;
				elementCount--;
				return;
			}
		}
		System.out.println("Pool::release()::did not find:" + element);
	}

	/**
	 * @return element capacity
	 */
	final public int capacity() {
		return elements.size();
	}

	/**
	 * @return elements in use
	 */
	final public int size() {
		return elementCount;
	}

	/**
	 * Reset this pool
	 */
	final public void reset() {
		elementCount = 0;
		for (int i = 0; i < elements.size(); i++) {
			PoolElement<E> _element = elements.get(i);
			_element.inUse = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	final public String toString() {
		return "Pool [elements=" + elements + "]";
	}

}

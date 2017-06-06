package net.drewke.tdme.utils;

import java.util.Comparator;
import java.util.List;

/**
 * Quick sort
 * 	based on http://www.programcreek.com/2012/11/quicksort-array-in-java/
 * @author Andreas Drewke
 * @version $Id$
 */
public final class QuickSort {

	/**
	 * Quick sort
	 * @param array
	 */
	public static void sort(ArrayList array) {
		quickSort(array, 0, array.size() - 1);
	}

	/**
	 * Quick sort
	 * @param array list
	 * @param low
	 * @param high
	 */
	private static void quickSort(ArrayList<Comparable> array, int low, int high) {
		if (array == null || array.size() == 0)
			return;

		if (low >= high)
			return;

		// pick the pivot
		int middle = low + (high - low) / 2;
		Comparable pivot = array.get(middle);

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while ((array.get(i)).compareTo(pivot) < 0) {
				i++;
			}

			while ((array.get(j)).compareTo(pivot) > 0) {
				j--;
			}

			if (i <= j) {
				Comparable temp = array.get(i);
				array.set(i, array.get(j));
				array.set(j, temp);
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if (low < j)
			quickSort(array, low, j);

		if (high > i)
			quickSort(array, i, high);
	}

	/**
	 * Quick sort
	 * @param array
	 * @param comparator
	 */
	public static void sort(ArrayList array, Comparator comparator) {
		quickSort(array, comparator, 0, array.size() - 1);
	}

	/**
	 * Quick sort
	 * @param array list
	 * @param low
	 * @param high
	 * @param comparator
	 */
	private static void quickSort(ArrayList array, Comparator comparator, int low, int high) {
		if (array == null || array.size() == 0)
			return;

		if (low >= high)
			return;

		// pick the pivot
		int middle = low + (high - low) / 2;
		Object pivot = array.get(middle);

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (comparator.compare(array.get(i), pivot) < 0) {
				i++;
			}

			while (comparator.compare(array.get(j), pivot) > 0) {
				j--;
			}

			if (i <= j) {
				Object temp = array.get(i);
				array.set(i, array.get(j));
				array.set(j, temp);
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if (low < j)
			quickSort(array, comparator, low, j);

		if (high > i)
			quickSort(array, comparator, i, high);
	}

}

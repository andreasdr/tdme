package net.drewke.tdme.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Hashmap which keeps track of keys and values in a array list
 * @author Andreas Drewke
 *
 * @param <K>
 * @param <V>
 */
public final class HashMap<K,V> {

	/**
	 * Values Iterator 
	 * @author Andreas Drewke
	 * @param <K>
	 * @param <V>
	 */
	public final static class ValuesIterator<K,V> implements Iterator<V>, Iterable<V> {

		private HashMap<K,V> hashMap;
		private int bucketIdx;
		private int keyValuePairIdx;
		private int elementIdx;

		/**
		 * Public constructor
		 * @param hashmap
		 */
		public ValuesIterator(HashMap<K,V> hashmap) {
			this.hashMap = hashmap;
			reset();
		}

		/**
		 * Reset
		 * @return this iterator
		 */
		public Iterator<V> reset() {
			this.bucketIdx = 0;
			this.keyValuePairIdx = 0;
			this.elementIdx = 0;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		public Iterator<V> iterator() {
			return reset();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return elementIdx < hashMap.elements;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public V next() {
			while (bucketIdx < hashMap.buckets.size()) {
				ArrayList<Pair<K,V>> bucket = hashMap.buckets.get(bucketIdx);
				if (keyValuePairIdx == bucket.size()) {
					keyValuePairIdx = 0;
					bucketIdx++;
				} else {
					Pair<K,V> keyValuePair = bucket.get(keyValuePairIdx++);
					elementIdx++;
					return keyValuePair.value;
				}
			}
			return null;
		}
	}

	/**
	 * Keys Iterator 
	 * @author Andreas Drewke
	 * @param <K>
	 * @param <V>
	 */
	public final static class KeysIterator<K,V> implements Iterator<K>, Iterable<K> {

		private HashMap<K,V> hashMap;
		private int bucketIdx;
		private int keyValuePairIdx;
		private int elementIdx;

		/**
		 * Public constructor
		 * @param hashmap
		 */
		public KeysIterator(HashMap<K,V> hashmap) {
			this.hashMap = hashmap;
			reset();
		}

		/**
		 * Reset
		 * @return this iterator
		 */
		public Iterator<K> reset() {
			this.bucketIdx = 0;
			this.keyValuePairIdx = 0;
			this.elementIdx = 0;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		public Iterator<K> iterator() {
			return reset();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return elementIdx < hashMap.elements;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public K next() {
			while (bucketIdx < hashMap.buckets.size()) {
				ArrayList<Pair<K,V>> bucket = hashMap.buckets.get(bucketIdx);
				if (keyValuePairIdx == bucket.size()) {
					keyValuePairIdx = 0;
					bucketIdx++;
				} else {
					Pair<K,V> keyValuePair = bucket.get(keyValuePairIdx++);
					elementIdx++;
					return keyValuePair.key;
				}
			}
			return null;
		}
	}

	/**
	 * @author Andreas Drewke
	 * @param <K>
	 * @param <V>
	 */
	private final static class Pair<K,V> {
		private K key;
		private V value;
	}

	private int capacity = 256;
	private int elements = 0;
	private ArrayList<ArrayList<Pair<K,V>>> buckets = new ArrayList<ArrayList<Pair<K,V>>>();
	private HashMap.ValuesIterator<K,V> valuesIterator = new ValuesIterator<K,V>(this);
	private HashMap.KeysIterator<K,V> keysIterator = new KeysIterator<K,V>(this);
	private Pool<Pair<K,V>> pairPool = new Pool<Pair<K,V>>() {
		public Pair<K, V> instantiate() {
			return new Pair<K, V>();
		}
		
	};

	/**
	 * Public constructor
	 */
	public HashMap() {
		for (int i = 0; i < capacity; i++) {
			buckets.add(new ArrayList<Pair<K,V>>());
		}
	}

	/**
	 * Clears this hash table
	 */
	public void clear() {
		pairPool.reset();
		elements = 0;
		for (int i = 0; i < capacity; i++) {
			buckets.get(i).clear();
		}
	}

	/**
	 * Grow hash map
	 */
	private void grow() {
		// store old buckets
		ArrayList<ArrayList<Pair<K,V>>> oldBuckets = buckets;

		// create new buckets
		buckets = new ArrayList<ArrayList<Pair<K,V>>>();
		capacity*= 2;
		elements = 0;
		for (int i = 0; i < capacity; i++) {
			buckets.add(new ArrayList<Pair<K,V>>());
		}

		// recreate bucket table
		for (int i = 0; i < oldBuckets.size(); i++) {
			ArrayList<Pair<K,V>> bucket = oldBuckets.get(i);
			for (int j = 0; j < bucket.size(); j++) {
				Pair<K,V> keyValuePair = bucket.get(j);
				put(keyValuePair.key, keyValuePair.value);
			}
		}
	}

	/**
	 * Get the associated value of given object / key
	 */
	public V get(K key) {
		ArrayList<Pair<K,V>> bucket = buckets.get(Math.abs(key.hashCode()) % capacity);
		for (int i = 0; i < bucket.size(); i++) {
			Pair<K,V> keyValuePair = bucket.get(i);
			if (keyValuePair.key.equals(key)) return keyValuePair.value;  
		}
		return null;
	}

	/**
	 * Removes associated value of given object / key
	 */
	public V remove(K key) {
		ArrayList<Pair<K,V>> bucket = buckets.get(Math.abs(key.hashCode()) % capacity);
		for (int i = 0; i < bucket.size(); i++) {
			Pair<K,V> keyValuePair = bucket.get(i);
			if (keyValuePair.key.equals(key)) {
				pairPool.release(keyValuePair);
				bucket.remove(i);
				elements--;
				return keyValuePair.value;  
			}
		}
		return null;
	}

	/**
	 * Put given value with associated key into this hash map
	 */
	public V put(K key, V value) {
		if (elements + 1 >= buckets.size()) grow();
		V oldValue = remove(key);
		ArrayList<Pair<K,V>> bucket = buckets.get(Math.abs(key.hashCode()) % capacity);
		Pair<K,V> keyValuePair = pairPool.allocate();
		keyValuePair.key = key;
		keyValuePair.value = value;
		bucket.add(keyValuePair);
		elements++;
		return oldValue;
	}

	/**
	 * @return number of elements
	 */
	public int size() {
		return elements;
	}

	/**
	 * @return Values Iterator
	 */
	public ValuesIterator<K,V> getValuesIterator() {
		valuesIterator.reset();
		return valuesIterator;
	}

	/**
	 * @return Keys Iterator
	 */
	public KeysIterator<K,V> getKeysIterator() {
		keysIterator.reset();
		return keysIterator;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String string = new String();
		for (int i = 0; i < buckets.size(); i++) {
			ArrayList<Pair<K,V>> bucket = buckets.get(i);
			for (int j = 0; j < bucket.size(); j++) {
				Pair<K,V> keyValuePair = bucket.get(j);
				if (string.length() > 0) string+=", ";
				string+= keyValuePair.key + "=" + keyValuePair.value;
			}
		}
		string ="HashMap[" + string + "]";
		return string;
	}

}
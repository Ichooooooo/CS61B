package hashmap;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author icovo
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    protected int initialSize = 16;
    protected double loadFactor = 0.75;
    private int keySize = 0;
    private int tableSize = 0;

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(initialSize);
        tableSize = initialSize;
    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        buckets = createTable(initialSize);
        tableSize = initialSize;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        buckets = createTable(initialSize);
        tableSize = initialSize;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] tables = new Collection[tableSize];
        for (int i = 0; i < tableSize; i ++) {
            tables[i] = createBucket();
        }
        return tables;
    }

    // Your code won't compile until you do so!
    @Override
    public void clear() {
        buckets = createTable(initialSize);
        tableSize = initialSize;
        keySize = 0;
    }

    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), tableSize);
    }

    private int getIndex(K key, int tableSize) {
        return Math.floorMod(key.hashCode(), tableSize);
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        int tableIndex = getIndex(key);
        Collection<Node> bucket = buckets[tableIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return keySize;
    }

    private V removeKey(K key) {
        int tableIndex = getIndex(key);
        Collection<Node> bucket = buckets[tableIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                bucket.remove(node);
                keySize--;
                return node.value;
            }
        }
        return null;
    }

    private V removeKey(K key, V value) {
        int tableIndex = getIndex(key);
        Collection<Node> bucket = buckets[tableIndex];

        for (Node node : bucket) {
            if (node.key.equals(key) && node.value.equals(value)) {
                bucket.remove(node);
                keySize--;
                return node.value;
            }
        }
        return null;
    }

    private void expandExpansion() {
        int newTableSize = 2 * tableSize;
        Collection<Node>[] newBuckets = createTable(newTableSize);

        for (int i = 0; i < tableSize; i++) {
            Collection<Node> bucket = buckets[i];

            for (Node node : bucket) {
                int newTableIndex = getIndex(node.key, newTableSize);
                newBuckets[newTableIndex].add(createNode(node.key, node.value));
            }
        }

        buckets = newBuckets;
        tableSize = newTableSize;
    }

    @Override
    public void put(K key, V value) {
        int tableIndex = getIndex(key);
        Collection<Node> bucket = buckets[tableIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }

        bucket.add(createNode(key, value));
        keySize++;

        if ((double) keySize / tableSize > loadFactor) {
            expandExpansion();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (int i = 0; i < tableSize; i++) {
            Collection<Node> bucket = buckets[i];

            for (Node node : bucket) {
                keySet.add(node.key);
            }
        }
        return keySet;
    }

    private class HashMapIterator implements Iterator<K> {
        private int size;
        private int tableIndex;
        private Iterator<Node> iterator;

        public HashMapIterator() {
            size = 0;
            tableIndex = 0;
            iterator = buckets[0].iterator();
        }

        public boolean hasNext() {
            return size < keySize;
        }

        public K next() {
            if (iterator.hasNext()) {
                Node node = iterator.next();
                size++;
                return node.key;
            } else {
                while (!iterator.hasNext()) {
                    tableIndex++;
                    iterator = buckets[tableIndex].iterator();
                }

                Node node = iterator.next();
                size++;
                return node.key;
            }
        }
    }

    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

    @Override
    public V remove(K key) {
        return removeKey(key);
    }

    @Override
    public V remove(K key, V value) {
        return removeKey(key, value);
    }
}

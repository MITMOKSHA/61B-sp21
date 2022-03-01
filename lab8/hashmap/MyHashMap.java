package hashmap;

import java.sql.Connection;
import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
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

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private HashSet<K> keys;
    private double loadFactor;
    private int items;
    private void resize() {  // resize hash capacity.
        Collection<Node>[] newBuckets = new Collection[2*buckets.length];
        for (int i = 0; i < buckets.length; ++i) {
            newBuckets[i] = buckets[i];
        }
        buckets = newBuckets;
    }

    /** Constructors */
    public MyHashMap() {
        buckets = new Collection[16];
        loadFactor = 0.75;
        items = 0;
        keys = new HashSet<>();
    }

    public MyHashMap(int initialSize) {
        buckets = new Collection[initialSize];
        loadFactor = 0.75;
        items = 0;
        keys = new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = new Collection[initialSize];
        loadFactor = maxLoad;
        items = 0;
        keys = new HashSet<>();
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
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public Iterator<K> iterator() {
        return null;
    }

    @Override
    public void clear() {
        for (Collection<Node> bucket : buckets) {
            bucket = null;
        }
        buckets = null;
        items = 0;
    }
    @Override
    public boolean containsKey(K key) {
        if (buckets == null || key == null) {
            return false;
        }
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            return false;
        }
        for (Node node : buckets[index]) {  // traverse linklist.
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public V get(K key) {
        if (key == null || buckets == null) {
            return null;
        }
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;  // map contains no mapping for the key.
    }
    @Override
    public int size() {  // dynamic increase bucket size.
        return items;
    }
    @Override
    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        if (Double.valueOf(items)/Double.valueOf(buckets.length) >= loadFactor) {
            resize();
        }
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
            buckets[index].add(createNode(key, value));
            keys.add(key);
            items++;
        } else {
            if (containsKey(key)) {  // overwrite.
                for (Node node : buckets[index]) {
                    if (node.key.equals(key)) {
                        node.value = value;
                        break;
                    }
                }
            } else {  // add
                buckets[index].add(createNode(key, value));  // add new node.
                keys.add(key);  // add key into keys set.
                items++;
            }
        }
    }
    @Override
    public Set<K> keySet() {
        return keys;
    }
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }
}

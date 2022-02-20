package bstmap;

import edu.princeton.cs.algs4.BST;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V>{
    private BSTNode root;
    private class BSTNode {
        private K key;            // sorted by key
        private V val;            // associated data
        private BSTNode left, right; // left and right subtrees
        private int size;         // member of nodes in subtree
        public BSTNode(K key, V val, int size) {
            this.key = key;
            this.val = val;
            this.size = size;
        }
    }
    @Override
    public void clear() {

    }

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    private V get(BSTNode x, K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        if (x == null) {
            return null;
        }
        int cmp = key.compareTo(x.key);  // bounded type parameter.
        if (cmp < 0) {
            return get(x.left, key);     // recursive to search binary tree.
        } else if (cmp > 0) {
            return get(x.right, key);
        } else {
            return x.val;
        }
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private int size(BSTNode x) {
        if (x == null) {
            return 0;
        } else {
            return x.size;
        }
    }

    @Override
    public int size() {
        return size(root);
    }

    private BSTNode put(BSTNode x, K key, V val) {
        if (x == null) {
            return new BSTNode(key, val, 1);
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            x.left = put(x.left, key, val);
        } else if (cmp > 0) {
            x.right = put(x.right, key, val);
        } else {
            x.val = val;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    @Override
    public void put(K key, V val) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        if (val == null) {
            return;
        }
        root = put(root, key, val);
    }

    /**
     *  prints out BSTMap in order of increasing Key
     */
    public void printInOrder() {

    }

    @Override
    public V remove(K key) {
        return (V) new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        return (V) new UnsupportedOperationException();
    }

    @Override
    public Iterator iterator() {
        return (Iterator) new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return (Set<K>) new UnsupportedOperationException();
    }
}

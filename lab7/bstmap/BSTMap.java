package bstmap;

import edu.princeton.cs.algs4.BST;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
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

    private void clear(BSTNode x) {
        if (x.left == null && x.right == null) {  // leaf node
            x.size--;
            return;
        }
        if (x.left != null) {
            clear(x.left);
        }
        if (x.right != null) {
            clear(x.right);
        }
    }

    @Override
    public void clear() {
        root.size = 0;
        root = null;
    }

    private boolean containsKey(BSTNode x, K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls containsKey with a null key");
        }
        if (x == null) {
            return false;
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            return containsKey(x.left, key);
        } else if (cmp > 0) {
            return containsKey(x.right, key);
        } else {
            return true;
        }
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
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
        root = put(root, key, val);
    }

    private void printInOrder(BSTNode x) {
        if (x.left == null && x.right == null) {
            System.out.println(x.val);
        }
        if (x.left != null) {
            printInOrder(x.left);
        }
        if (x.right != null) {
            printInOrder(x.right);
        }
    }

    /**
     *  prints out BSTMap in order of increasing Key
     */
    public void printInOrder() {
        printInOrder(root);
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
    public Iterator<K> iterator() {
        return (Iterator<K>) new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return (Set<K>) new UnsupportedOperationException();
    }
}

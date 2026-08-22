package bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.*;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V>, Iterable<K> {
    private class BSTNode<K, V> {
        // if no declaration, default package-private
        BSTNode<K, V> father;
        BSTNode<K, V> leftSon;
        BSTNode<K, V> rightSon;
        K key;
        V value;

        public BSTNode() {}

        public BSTNode(K key, V value, BSTNode father) {
            this.key = key;
            this.value = value;
            this.father = father;
        }
    }

    // only one BSTNode : the root one
    private BSTNode<K, V> origin;

    // get the key located BSTNode
    private BSTNode<K, V> getKeyNode(BSTNode<K, V> Node, K findKey) {
        if (Node.leftSon != null && Node.key.compareTo(findKey) > 0) {
            return getKeyNode(Node.leftSon, findKey);
        }
        if (Node.rightSon != null && Node.key.compareTo(findKey) < 0) {
            return getKeyNode(Node.rightSon, findKey);
        }
        if (findKey.equals(Node.key)) {
            return Node;
        }
        return null;
    }

    // get the size of tree
    private int getSize(BSTNode<K, V> node) {
        int size = 1;
        if (node.leftSon != null) {
            size += getSize(node.leftSon);
        }
        if (node.rightSon != null) {
            size += getSize(node.rightSon);
        }
        return size;
    }

    @Override
    public void clear() {
        origin = null;
    }

    @Override
    public boolean containsKey(K key) {
        if (origin == null) return false;
        if (getKeyNode(origin, key) == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public V get(K key) {
        if (origin == null) {
            return null;
        }

        BSTNode<K, V> keyNode = getKeyNode(origin, key);
        if (keyNode == null) {
            return null;
        } else {
            return keyNode.value;
        }
    }

    @Override
    public int size() {
        if (origin == null) {
            return 0;
        }
        return getSize(origin);
    }

    // put the new BSTNode should firstly find its father BSTNode
    private BSTNode<K, V> getFatherNode(BSTNode<K, V> node, K key) {
        if (key.compareTo(node.key) < 0) {
            if (node.leftSon == null) {
                return node;
            } else {
                return getFatherNode(node.leftSon, key);
            }
        } else if (key.compareTo(node.key) > 0) {
            if (node.rightSon == null) {
                return node;
            } else {
                return getFatherNode(node.rightSon, key);
            }
        } else {
            return node;
        }
    }

    @Override
    public void put(K key, V value) {
        if (origin == null) {
            origin = new BSTNode<>(key, value, null);
            return;
        }
        BSTNode<K, V> fatherNode = getFatherNode(origin, key);
        if (key.compareTo(fatherNode.key) < 0) {
            fatherNode.leftSon = new BSTNode<>(key, value, fatherNode);
        } else if (key.compareTo(fatherNode.key) > 0) {
            fatherNode.rightSon = new BSTNode<>(key, value, fatherNode);
        } else {
            fatherNode.value = value;
        }
    }

    // get the Set of key on tree
    private void getKeySet(Set<K> keySet, BSTNode<K, V> node) {
        keySet.add(node.key);
        if (node.leftSon != null) {
            getKeySet(keySet, node.leftSon);
        }
        if (node.rightSon != null) {
            getKeySet(keySet, node.rightSon);
        }
    }

    @Override
    public Set<K> keySet() {
        if (origin == null) {
            return null;
        }
        Set<K> keySet = new TreeSet<>();
        getKeySet(keySet, origin);
        return keySet;
    }

    // remove the leaf node of tree
    private void removeLeafNode(BSTNode<K, V> node) {
        if (node.father == null) {
            origin = null;
        } else {
            if (node.father.key.compareTo(node.key) > 0) {
                node.father.leftSon = null;
            } else {
                node.father.rightSon = null;
            }
        }
    }

    // remove the node of only one son
    private void removeOneSonNode(BSTNode<K, V> node) {
        if (node.father == null) {
            BSTNode<K, V> sonNode;
            if (node.rightSon != null) {
                sonNode = node.rightSon;
            } else {
                sonNode = node.leftSon;
            }
            sonNode.father = null;
            origin = sonNode;
        } else {
            if (node.key.compareTo(node.father.key) > 0) {
                BSTNode<K, V> sonNode;
                if (node.rightSon != null) {
                    sonNode = node.rightSon;
                } else {
                    sonNode = node.leftSon;
                }
                node.father.rightSon = sonNode;
                sonNode.father = node.father;
            } else {
                BSTNode<K, V> sonNode;
                if (node.rightSon != null) {
                    sonNode = node.rightSon;
                } else {
                    sonNode = node.leftSon;
                }
                node.father.leftSon = sonNode;
                sonNode.father = node.father;
            }
        }
    }

    private BSTNode<K, V> leftMaxNode(BSTNode<K, V> node) {
        if (node.rightSon != null) {
            return  leftMaxNode(node.rightSon);
        } else {
            return node;
        }
    }

    private BSTNode<K, V> rightMinNode(BSTNode<K, V> node) {
        if (node.leftSon != null) {
            return rightMinNode(node.leftSon);
        } else {
            return node;
        }
    }

    // remove the node of two sons
    private void removeTwoSonNode(BSTNode<K, V> node) {
        BSTNode<K, V> leftMax = leftMaxNode(node.leftSon);
        BSTNode<K, V> fatherNode = node.father;
        BSTNode<K, V> leftSon = node.leftSon;
        BSTNode<K, V> rightSon = node.rightSon;
        BSTNode<K, V> newNode = new BSTNode<>(leftMax.key, leftMax.value, fatherNode);
        if (fatherNode != null) {
            if (newNode.key.compareTo(fatherNode.key) > 0) {
                fatherNode.rightSon = newNode;
            } else {
                fatherNode.leftSon = newNode;
            }
        } else {
            origin = newNode;
        }
        leftSon.father = newNode;
        rightSon.father = newNode;
        newNode.leftSon = leftSon;
        newNode.rightSon = rightSon;
        removeNode(leftMax);
    }

    // remove node after judge the type of it
    private void removeNode(BSTNode<K, V> node) {
        if (node.leftSon == null && node.rightSon == null) {
            removeLeafNode(node);
        } else if (node.leftSon != null && node.rightSon != null) {
            removeTwoSonNode(node);
        } else {
            removeOneSonNode(node);
        }
    }

    @Override
    public V remove(K key) {
        BSTNode<K, V> keyNode = getKeyNode(origin, key);
        V returnV = keyNode.value;
        if (keyNode == null) {
            return null;
        }
        removeNode(keyNode);
        return returnV;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    private class BSTMapIterator implements Iterator<K> {
        private Stack<BSTNode<K, V>> keyStack = new Stack<>();

        private void pushLeft(BSTNode<K, V> node) {
            keyStack.push(node);
            while (node.leftSon != null) {
                node = node.leftSon;
                keyStack.push(node);
            }
        }

        public BSTMapIterator(BSTNode<K, V> originNode) {
            pushLeft(originNode);
        }

        public boolean hasNext() {
            return !keyStack.empty();
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            BSTNode<K, V> stackNode = keyStack.pop();
            if (stackNode.rightSon != null) {
                pushLeft(stackNode.rightSon);
            }

            return stackNode.key;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator(origin);
    }
}

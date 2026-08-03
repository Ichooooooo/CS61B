package deque;

import java.util.Iterator;
import java.util.LinkedList;
//import java.util.Objects;

public class LinkedListDeque <T> implements Deque<T>, Iterable<T> {
    private static class IntNode <T> {
        public IntNode prev;
        public T item;
        public IntNode next;
        public IntNode (T i, IntNode p, IntNode n) {
            item = i;
            prev = p;
            next = n;
        }
    }

    private IntNode sentinel;
    private int size;

    public LinkedListDeque () {
        sentinel = new IntNode (0, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    @Override
    public void addFirst (T item) {
        IntNode t = sentinel.next;
        sentinel.next = new IntNode (item, sentinel, sentinel.next);
        t.prev = sentinel.next;
        size++;
    }

    @Override
    public void addLast (T item) {
        IntNode t = sentinel.prev;
        sentinel.prev = new IntNode (item, t, sentinel);
        t.next = sentinel.prev;
        size++;
    }

    @Override
    public int size () {
        return size;
    }

    @Override
    public void printDeque () {
        StringBuilder returnSB = new StringBuilder();

        if (size == 0) {
            return;
        }

        IntNode p = sentinel;
        p = p.next;
        while (p != sentinel) {
            System.out.print (p.item + " ");
            p = p.next;
        }

        String returnSBString = returnSB.toString();
        System.out.println(returnSBString);
    }

    @Override
    public T removeFirst () {

        if (size == 0) { return null;}

        IntNode t = sentinel.next;
        sentinel.next = t.next;
        t.next.prev = sentinel;
        size--;

        return (T) t.item;
    }

    @Override
    public T removeLast () {
        IntNode t = sentinel.prev;

        if (size == 0) { return null;}

        sentinel.prev = t.prev;
        t.prev.next = sentinel;
        size--;

        return (T) t.item;
    }

    private  T getNextByR(IntNode p, int index) {
        if (index == 0) return (T) p.item;

        return getNextByR(p.next, index - 1);
    }

    public T getRecursive(int index) {
        if (size < index + 1) { return null;}

        IntNode t = sentinel;
        return getNextByR(t.next, index);
    }

    @Override
    public T get (int index) {
        int nowIndex = 0;
        IntNode p = sentinel;

        if (size < index + 1) { return null;}

        while (nowIndex <= index) {
            p = p.next;
            nowIndex++;
        }

        return (T) p.item;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int num;
        private IntNode p = sentinel;

        LinkedListDequeIterator() {
            num = 0;
        }

        public boolean hasNext() {
            return num < size;
        }

        public T next() {
            p = p.next;
            T returnNum = (T) p.item;
            num++;

            return returnNum;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Deque)) return false;

        Deque<T> q = (Deque<T>) o;

        if (q.size() != size) return false;

        for (int i = 0; i < size; i++) {
            if (!this.get(i).equals(q.get(i))) return false;
        }

        return true;
    }
}
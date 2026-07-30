package deque;

public class LinkedListDeque <T> implements Deque<T> {
    public static class IntNode <T> {
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

    public LinkedListDeque (T item) {
        sentinel = new IntNode (0, null, null);
        IntNode t = new IntNode (item, sentinel, sentinel);
        sentinel.next = t;
        sentinel.prev = t;
        size++;
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
        IntNode p = sentinel;

        p = p.next;
        while (p != sentinel) {
            System.out.print (p.item + " ");
            p = p.next;
        }
        System.out.println("");
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

//    public Iterator<T> iterator() {
//
//    }
//
//    public boolean equals (Object o) {
//
//    }
}
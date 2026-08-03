package deque;

import java.util.Iterator;
import java.util.Objects;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size = 0;
    private T[] p;
    private int SIZE = 8;
    private int back, front;
    private static double RFACTOR = 1.5;
    private static int SMALLSIZE = 30; // small size not need R >= 0.25

    public ArrayDeque() {
        p = (T[]) new Object[8];
        back = SIZE - 1;
        front = 0;
    }

    private void expandSize () {
        if (back >= front) {
            int TSIZE = (int) ((int) SIZE * RFACTOR);
            T[] t = (T[]) new Object[TSIZE];
            System.arraycopy(p, 0, t, 0, front);
            int length = SIZE - 1 - back;
            int tback = TSIZE - 1 - length;
            System.arraycopy(p, back + 1, t, tback + 1, length);

            p = t;
            SIZE = TSIZE;
            front = front;
            back = tback;
        } else {
            int TSIZE = (int) ((int) SIZE * RFACTOR);
            T[] t = (T []) new Object[TSIZE];
            System.arraycopy(p, back + 1, t, 0, size);

            p = t;
            SIZE = TSIZE;
            front = size;
            back = TSIZE - 1;
        }
    }

    private boolean checkSize () {
        if (4 * size < SIZE && SIZE > SMALLSIZE) { return true;}
        else { return false;}
    }

    private void reduceSize () {
        if (back > front) {
            int TSIZE = SIZE / 2;
            T[] t = (T []) new Object[TSIZE];
            System.arraycopy(p, 0, t, 0, front);
            int length = SIZE - 1 - back;
            int tback = TSIZE - 1 - length;
            System.arraycopy(p, back + 1, t, tback + 1, length);

            p = t;
            SIZE = TSIZE;
            front = front;
            back = tback;
        } else {
            int TSIZE = SIZE / 2;
            T[] t = (T []) new Object[TSIZE];
            System.arraycopy(p, back + 1, t, 0, size);

            p = t;
            SIZE = TSIZE;
            front = size;
            back = TSIZE - 1;
        }
    }

    private int index (int t) {
        return (t + SIZE) % SIZE;
    }

    @Override
    public void addFirst (T item) {
        if (size == SIZE - 1) {
            expandSize();
        }
        p[back] = item;
        back = index (back - 1);
        size++;
    }

    @Override
    public void addLast (T item) {
        if (size == SIZE - 1) {
            expandSize();
        }
        p[front] = item;
        front = index (front + 1);
        size++;
    }

    @Override
    public int size () {
        return size;
    }

    @Override
    public void printDeque () {
        System.out.println(toString());
    }

    @Override
    public T removeFirst () {
        if (size == 0) { return null;}

        T ans = p[index(back + 1)];
        p[index(back + 1)] = null;
        back = index(back + 1);
        size--;

        if (checkSize()) {
            reduceSize();
        }
        return ans;
    }

    @Override
    public T removeLast () {
        if (size == 0) { return null;}

        T ans = p[index(front - 1)];
        p[index(front - 1)] = null;
        front = index(front - 1);
        size--;

        if (checkSize()) {
            reduceSize();
        }
        return ans;
    }

    @Override
    public T get (int index) {
        return p[index(back + 1 + index)];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int num;
        private int index;

        public ArrayDequeIterator() {
            num = 0;
            index = index(back + 1);
        }

        public boolean hasNext() {
            return num < size;
        }

        public T next() {
            T returnNum = p[index];
            index = index(index + 1);
            num++;
            return returnNum;
        }
    }

    @Override
    public String toString() {
        StringBuilder returnSB = new StringBuilder();
        for (int in = back + 1, count = 0; count < size; count++, in++) {
            if (count != 0) {
                int t = index(in);
                returnSB.append(" ");
                returnSB.append(p[t]);
            } else {
                int t = index(in);
                returnSB.append(p[t]);
            }
        }

        return returnSB.toString();
    }

    @Override
    public boolean equals (Object o) {
        if (o == this) return true;
        if (!(o instanceof Deque)) return false;

        Deque<T> q;
        if (o instanceof LinkedListDeque) {
            q = (LinkedListDeque<T>) o;
        } else {
            q = (ArrayDeque<T>) o;
        }

        if (q.size() != size) return false;

        for (int i = 0; i < size; i++) {
            if (!this.get(i).equals(q.get(i))) return false;
        }

        return true;
    }
}

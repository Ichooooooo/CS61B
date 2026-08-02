package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/** Performs some basic linked list tests. */
public class ArrayDequeTest {
    @Test
    public void testBasicF () {
        ArrayDeque<Integer> p = new ArrayDeque<>();
        p.addFirst(1);
        p.addLast(2);

        assertTrue("The array shouldn't be empty", !p.isEmpty());
        assertTrue("The real size should be two", p.size() == 2);

        p.printDeque();

        /** test the get*/
        for (int i = 0; i < p.size(); i ++) {
            System.out.println("The index of " + i + " is : " + p.get(i));
        }
    }

    @Test
    public void testBasicRemoveF () {
        ArrayDeque<String> test = new ArrayDeque<>();
        ArrayDeque<String> expected = new ArrayDeque<>();
        test.addFirst("i");
        test.addLast("love");
        test.addLast("you");

        test.printDeque();

        while (!test.isEmpty()) {
            test.removeFirst();
            test.addFirst("i");
            assertTrue("The size now is three", test.size() == 3);
            assertEquals("i", test.get(0));

            test.removeLast();
            assertTrue("The size now is two", test.size() == 2);
            assertEquals("i", test.get(0));
            test.removeFirst();
            assertEquals("love", test.get(0));
            test.removeFirst();
            assertTrue("The array is empty", test.isEmpty());
        }
    }

    @Test
    public void testComplexRemoveF () {
        ArrayDeque<Integer> p = new ArrayDeque<>();
        p.addLast(2);
        p.addLast(1);
        p.addFirst(3);
        p.addFirst(4);

        ArrayDeque<Integer> test1 = p;
        int f = 4;
        while (test1.isEmpty()) {
            assertEquals(f, (int)test1.removeFirst());
            f--;
        }

        ArrayDeque<Integer> test2 = p;
        int ff = 1;
        while (test2.isEmpty()) {
            assertEquals(ff, (int)(test2.removeLast()));
            ff++;
        }
    }

    @Test
    public void testSizeExpand () {
        ArrayDeque <Integer> p = new ArrayDeque<>();
        for (int i = 0; i < 9; i ++) {
            p.addFirst(i);
        }

        assertEquals(9, p.size());

        for (int i = 0; i < 9; i ++) {
            assertEquals(8 - i, (int)p.get(i));
        }

        p.printDeque();
    }

    @Test
    public void testSizeReduce () {
        ArrayDeque <Integer> p = new ArrayDeque<>();
        for (int i = 0; i < 40; i ++) {
            p.addLast(i);
        }

        assertEquals(40, p.size());

        for (int i = 0; i < 40; i ++) {
            assertEquals(i, (int)p.get(i));
        }

        for (int i = 0; i < 30; i ++) {
            p.removeLast();
        }

        for (int i = 0; i < 10; i ++) {
            assertEquals(i, (int)p.get(i));
        }

        p.printDeque();
    }

    @Test
    /** test the iterator */
    public void testIterator() {
        ArrayDeque<Integer> p = new ArrayDeque<>();
        p.addLast(1);
        p.addLast(2);
        p.addLast(3);
        p.addLast(4);

        int t = 1;
        for (int x : p) {
            assertEquals(t, x);
            t++;
        }
    }

    @Test
    /**test the equals */
    public void testEquals() {
        ArrayDeque<Integer> p = new ArrayDeque<>();
        ArrayDeque<Integer> q = new ArrayDeque<>();
        ArrayDeque<Integer> g = new ArrayDeque<>();

        p.addLast(1);
        p.addLast(2);
        p.addLast(3);
        q.addLast(1);
        q.addLast(2);
        q.addLast(3);
        g.addLast(3);
        g.addLast(3);
        g.addLast(3);

        assertTrue(p.equals(q));
        assertTrue(q.equals(p));
        assertTrue(!q.equals(g));
        assertTrue(!p.equals(g));
    }

    @Test
    /** random test */
    public void randomTest() {
        int N = 5000;
        java.util.Deque<Integer> expected = new java.util.ArrayDeque<>();
        ArrayDeque<Integer> test = new ArrayDeque<>();

        for (int testNum = 0; testNum < N; testNum++) {
            int operatorNumber = StdRandom.uniform(0, 7);
            if (operatorNumber == 0) {
                int num = StdRandom.uniform(0, 5);
                expected.addFirst(num);
                test.addFirst(num);
            } else if (operatorNumber == 1) {
                int num = StdRandom.uniform(0, 5);
                expected.addLast(num);
                test.addLast(num);
            } else if (operatorNumber == 2) {
                assertEquals(expected.size(), test.size());
            } else if (operatorNumber == 3) {
                if (test.size() == 0) continue;

                assertEquals(expected.removeFirst(), test.removeFirst());
            } else if (operatorNumber == 4) {
                if (test.size() == 0) continue;

                assertEquals(expected.removeLast(), test.removeLast());
            } else if (operatorNumber == 5) {
                if (test.size() == 0) continue;

                int num = StdRandom.uniform(0, test.size());
                System.out.println("The index of : " + num + " is " + test.get(num));
            } else {
                test.printDeque();
            }
        }
    }
}


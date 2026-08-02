package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Deque;
import java.util.LinkedList;
import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** test the basic function */
    public void testBasic () {
        LinkedListDeque<Integer> test = new LinkedListDeque<>();

        test.addFirst (1);
        test.printDeque();
        test.addLast (2);
        test.printDeque();

        assertTrue ("The size should be two", test.size() == 2);

        test.removeLast();
        test.printDeque();
        test.removeFirst();
        test.printDeque();

        assertTrue ("The size is empty now", test.isEmpty());
    }

    @Test
    public void printByGet () {
        LinkedListDeque <String> test = new LinkedListDeque<>("i");
        LinkedListDeque <String> expected = new LinkedListDeque<>();

        expected.addFirst("i");
        expected.addFirst("love");
        expected.addFirst("you");
        expected.addFirst("always");

        test.addLast("love");
        test.addLast("you");
        test.addLast("always");

        String sexpected = "i love you always";
        String stest = "";

        for (int i = 0; i < test.size(); i ++) {
            if (i != 0) {
                stest += " " + test.get(i);
            } else {
                stest = test.get (i);
            }
        }

        assertEquals(stest, sexpected);
        assertTrue("The size of test", test.size() == 4);
    }

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */

    public void addIsEmptySizeTest() {

        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {

        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

//        for (double i = 999999; i > 500000; i--) {
//            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
//        }
    }

    @Test
    /** test the iterator */
    public void testIterator() {
        LinkedListDeque<Integer> p = new LinkedListDeque<>();
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
        LinkedListDeque<Integer> p = new LinkedListDeque<>();
        LinkedListDeque<Integer> q = new LinkedListDeque<>();
        LinkedListDeque<Integer> g = new LinkedListDeque<>();

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
        java.util.Deque<Integer> expected = new java.util.LinkedList<>();
        LinkedListDeque<Integer> test = new LinkedListDeque<>();

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

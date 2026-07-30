package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    /** basic test */
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> expected = new AListNoResizing<>();
        BuggyAList<Integer> test = new BuggyAList<>();
        expected.addLast(4);
        expected.addLast(5);
        expected.addLast(6);
        test.addLast(4);
        test.addLast(5);
        test.addLast(6);

        for (int i = 0; i < 3; i++) {
            assertEquals(expected.removeLast(), test.removeLast());
        }
    }

    @Test
    /** random test */
    public void randomizedTest() {
        AListNoResizing<Integer> expected = new AListNoResizing<>();
        BuggyAList<Integer>  test = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                expected.addLast(randVal);
                test.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = expected.size();
                int _size = test.size();

                assertEquals("the size should be equal", size, _size);
            } else if (operationNumber == 2) {
                if (expected.size() == 0) { continue;}

                assertEquals(expected.getLast(), test.getLast());
            } else if (operationNumber == 3) {
                if (expected.size() == 0) { continue;}

                assertEquals(expected.removeLast(), test.removeLast());
            }
        }
    }
}

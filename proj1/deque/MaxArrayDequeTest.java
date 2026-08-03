package deque;

import org.junit.Test;
import org.junit.Before;

import java.util.Comparator;
import java.util.Objects;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    private class testMaxByNum implements Comparator<Integer>{
        public int compare(Integer a, Integer b) {
            return a - b;
        }
    }
    @Test
    /** test the max compator */
    public void testMaxComparator() {
        Comparator<Integer> comparator = new testMaxByNum();
        MaxArrayDeque<Integer> p = new MaxArrayDeque<>(comparator);

        assertNull(p.max());
        p.addFirst(1);
        p.addFirst(2);
        p.addFirst(3);

        assertEquals(p.max(), Integer.valueOf(3));
    }
}

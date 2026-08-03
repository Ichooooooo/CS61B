package flik;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

public class testFlik {
    @Test
    public void testEquals() {
        int N = 5000, M = 5000;
        for (int i = 0; i < N; i++) {
            int x = i;
            int y = StdRandom.uniform(0, M);

            System.out.println("test on " + i);
            assertTrue(Flik.isSameNumber(x, i));
            assertEquals(Objects.equals(x, y), Flik.isSameNumber(x, y));
//            assertEquals(Objects.equals(x, i), Flik.isSameNumber(x, i));
        }
    }
}

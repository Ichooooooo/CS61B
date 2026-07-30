package timingtest;

import edu.princeton.cs.introcs.Stopwatch;
//import org.checkerframework.checker.units.qual.A;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int origin = 1000;
        for (int i = 1; i <= 8; i++) {
            Ns.addLast(origin);
            opCounts.addLast(origin);

            Stopwatch sw = new Stopwatch();
            AList<Integer> t = new AList<>();
            for (int j = 0; j < origin; j++) {
                t.addLast(i);
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);

            origin *= 2;
        }

        printTimingTable(Ns, times, opCounts);
    }
}

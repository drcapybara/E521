package Tests;
/*
 * Contains experimental runtime tests of curve used for analytic research.
 * Values recorded will likely vary between runs due to performance anomalies within the JVM.
 */
import Curve.E521;
import java.math.BigInteger;

/**
 * @author Dustin Ray
 * @version Spring 2021
 */
public class RuntimeTests {

    /** Generator point for curve.  */
    public static final E521 G = new E521(new BigInteger("4"), false);
    /** Holds average runtimes of 10000 runs for each value of s. */
    public long[][] runtimes;

    /** Experimental runtime tests for elliptic curve point multiplication algorithm variants. */
    public RuntimeTests() {

        System.out.println("RUNTIME TESTS: GET AVERAGE RUNNING TIME OF 10,000 RUNS FOR:\n" +
                "1. Double and add\n" +
                "2. Montgomery Ladder\n\n");
        init();
        testDoubleAndAdd();
        init();
        testMontgomery();
        System.out.println("TESTS COMPLETE\n########");

    }

    /** Reset running times array after test of each algorithm. */
    public void init() {
        runtimes = new long[5][2];
        runtimes[0][0] = 10;
        runtimes[1][0] = 100;
        runtimes[2][0] = 1000;
        runtimes[3][0] = 10000;
        runtimes[4][0] = 100000;
    }

    /** Runs 10000 curve multiplications for scalar values of 10, 100, 1000, 10000, and 100000. */
    public void testDoubleAndAdd() {

        long avgRunTime = 0;
        for(int i = 0; i < 5; i++) {
            long idx = runtimes[i][0];
            for (int j = 0; j < idx; j++) {
                final long startTime = System.nanoTime();
                Curve.E521 test1 = G.doubleAndAdd(new BigInteger("100000"));
                final long endTime = System.nanoTime();
                avgRunTime += (endTime - startTime);
            }
            runtimes[i][1] = avgRunTime /= 10000;
        }
        System.out.println(
                            "Average double and add runtime for G * 10: " + runtimes[0][1] + " ns\n" +
                            "Average double and add runtime for G * 100: " + runtimes[1][1] + " ns\n" +
                            "Average double and add runtime for G * 1000: " + runtimes[2][1] + " ns\n" +
                            "Average double and add runtime for G * 10000: " + runtimes[3][1] + " ns\n" +
                            "Average double and add runtime for G * 100000: " + runtimes[4][1] + " ns\n"
        );
    }

    /**
     * Runs 10000 curve multiplications for scalar values of 10, 100, 1000, 10000, and 100000.
     */
    public void testMontgomery() {

        long avgRunTime = 0;
        for(int i = 0; i < 5; i++) {
            long idx = runtimes[i][0];
            for (int j = 0; j < idx; j++) {
                final long startTime = System.nanoTime();
                Curve.E521 test1 = G.multiplyMontgomery(new BigInteger("100000"), G);
                final long endTime = System.nanoTime();
                avgRunTime += (endTime - startTime);
            }
            runtimes[i][1] = avgRunTime /= 10000;
        }
        System.out.println(
                            "Average Montgomery Ladder runtime for G * 10: " + runtimes[0][1] + " ns\n" +
                            "Average Montgomery Ladder runtime for G * 100: " + runtimes[1][1] + " ns\n" +
                            "Average Montgomery Ladder runtime for G * 1000: " + runtimes[2][1] + " ns\n" +
                            "Average Montgomery Ladder runtime for G * 10000: " + runtimes[3][1] + " ns\n" +
                            "Average Montgomery Ladder runtime for G * 100000: " + runtimes[4][1] + " ns\n"
        );
    }
}

package Tests;

import Curve.E521;

import java.math.BigInteger;
import java.security.SecureRandom;

/** Test cases for elliptic curve Montgomery Ladder point multiplication algorithm. */
public class InitialTests {

    /** Generator point for curve.  */
    public static final E521 G = new E521(new BigInteger("4"), false);

    /** random scalar t % n, recalling that n := 4r */
    public static final BigInteger t = BigInteger.valueOf(new SecureRandom().nextInt()).mod(G.getR().multiply(BigInteger.valueOf(4)));

    /** random scalar k % n, recalling that n := 4r */
    public static final BigInteger k = BigInteger.valueOf(new SecureRandom().nextInt()).mod(G.getR().multiply(BigInteger.valueOf(4)));

    /** Constructor.*/
    public InitialTests() {

        //initial tests to ensure curve operations produce correct values.
        System.out.println(G.toString("Point G: "));
        zeroTest();
        oneTest();
        oppositeTest();
        twoG();
        fourG();
        rG();

        kG();
        kPlusOne();
        kPlusT();
        KTGGR();
    }

    /**Test multiplication for neutral point. 0 * G = O */
    public void zeroTest() {
        E521 zero = G.multiplyMontgomery(BigInteger.ZERO, G);
        System.out.println(zero.toString("0 * G: "));
    }

    /** Tests G * 1 = G */
    public void oneTest() {
        E521 zero = G.multiplyMontgomery(BigInteger.ONE, G);
        System.out.println(zero.toString("1 * G: "));
    }

    /** Tests G + (-G) = O  */
    public void oppositeTest() {
        E521 opposite = G.getOpposite();
        opposite = opposite.add(G);
        System.out.println(opposite.toString("G + (-G) "));
    }

    /** Calculates 2 * G */
    public void twoG() {
        E521 twoG = G.multiplyMontgomery(BigInteger.TWO, G);
        System.out.println(twoG.toString("2 * G: "));
    }

    /** Tests that 2G = 2 * (2 * G) */
    public void fourG() {
        E521 twoG = G.multiplyMontgomery(BigInteger.TWO, G);
        twoG = twoG.multiplyMontgomery(BigInteger.TWO, twoG);
        System.out.println(twoG.toString("2 * 2G: "));
        E521 fourG = G.multiplyMontgomery(new BigInteger("4"), G);
        System.out.println(twoG.toString("4 * G: "));
    }

    /** Tests modularity of curve, in that r * G = O */
    public void rG() {
        E521 rG = G.multiplyMontgomery(G.getR(), G);
        System.out.println(rG.toString("r * G: "));
    }

    /** tests multiplication of random k * G */
    public void kG() {

        System.out.println("Random Secret Key k: " + k);
        E521 kG = G.multiplyMontgomery(k, G);
        System.out.println(kG.toString("kG = :"));
        E521 kModRG = G.multiplyMontgomery(k.mod(G.getR()), G);
        System.out.println(kG.toString("(k % r) * G = :"));
    }

    /** Tests k + 1 * G = k * G + G */
    public void kPlusOne() {

        BigInteger k2 = k.add(BigInteger.ONE);
        System.out.println("Random Secret Key k + 1: " + k2);
        E521 kPlusOneG = G.multiplyMontgomery(k2, G);
        System.out.println(kPlusOneG.toString("(k + 1) * G = :"));

        E521 kGPlusOne = G.multiplyMontgomery(k, G);
        kGPlusOne = kGPlusOne.add(G);
        System.out.println(kGPlusOne.toString("(k * G) + G: "));
    }

    /** Tests (k + t) * G = (k * G) + (t * G) */
    public void kPlusT() {

        System.out.println("Random Secret Key t: " + t);
        BigInteger kPlusT = t.add(k);
        E521 gTimesKPlusT = G.multiplyMontgomery(kPlusT, G);
        System.out.println(gTimesKPlusT.toString("(k + t) * G = :"));

        E521 kTimesG = G.multiplyMontgomery(k, G);
        E521 tTimesG = G.multiplyMontgomery(t, G);
        E521 kTimesGPlusTTimesG = kTimesG.add(tTimesG);
        System.out.println(kTimesGPlusTTimesG.toString("(k * G) + (t * G) = : "));
    }

    /** tests k * (t * P) = t * (k * G) = (k * t mod r) * G */
    public void KTGGR(){

        E521 P = G.multiplyMontgomery(k, G);

        E521 KTP = P.multiplyMontgomery(t, P).multiplyMontgomery(k, P);
        E521 TKG = G.multiplyMontgomery(k, G).multiplyMontgomery(t, G);
        BigInteger KTmodR = k.multiply(t).mod(G.getR());

        E521 KtmodRG = G.multiplyMontgomery(KTmodR, G);

        System.out.println(KTP.toString("k * (t * P) = :"));
        System.out.println(TKG.toString("t * (k * G) = :"));
        System.out.println(KtmodRG.toString("(k * t mod r) * G = :"));


    }

}






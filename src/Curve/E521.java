package Curve;
import java.math.BigInteger;

/**
 * Curve.E521 Elliptic Curve (Edward's Curve) of equation: (x^2) + (y^2) = 1 + d(x^2)(y^2)
 * where d = -376014
 * Contains methods to add and multiply points on curve using scalar values.
 *
 * @version Spring 2021
 * @author Dustin Ray
 */
public class E521 {

    /** Mersenne prime defining a finite field F(p) */
    public static final BigInteger P = (new BigInteger("2").pow(521)).subtract(BigInteger.ONE);
    /** d = -376014 */
    public static final BigInteger D = new BigInteger("-376014");
    /** X-coordinate of point. */
    private BigInteger myX;
    /** Y-coordinate of point. */
    private BigInteger myY;
    /** number of points on Curve.E521 Curve -> n := 4 * (R) .*/
    public static final BigInteger R = (new BigInteger("2").pow(519))
            .subtract(new BigInteger(
                    "337554763258501705789107630418782636071904961214051226618635150085779108655765"));

    /**
     * Constructor for point with only X provided. Solves for Y.
     * @param theX int value which is desired X coordinate of point. Y is generated
     * from X using sqrt method.
     */
    public E521(final BigInteger theX, boolean theLSB) {
        myX = theX;
        //solve for y
        BigInteger num = (BigInteger.ONE.subtract(theX.pow(2))).mod(P);
        BigInteger denom = BigInteger.ONE.add(new BigInteger("376014").multiply(theX.pow(2))).mod(P);
        denom = denom.modInverse(P);
        BigInteger radicand = num.multiply(denom);
        myY = sqrt(radicand, P, theLSB);
    }

    /**
     * Constructor for point with arbitrary x and y.
     * @param theX x coordinate of point
     * @param theY y coordinate of point
     */
    public E521(final BigInteger theX, final BigInteger theY) {
        myX = theX;
        myY = theY;
    }

    /** Constructor for neutral element, defined to be point (0, 1) */
    public E521() {
        myX = BigInteger.ZERO;
        myY = BigInteger.ONE;
    }

    /**
     * Compares two points for equality by X and Y coordinates.
     * Utilizes .equals method of BigInteger class.
     *
     * @param theOther Curve.E521 point to be compared against this object.
     * @return boolean, true for equal, else otherwise.
     */
    public boolean equals(final E521 theOther) {
        return this.getX().equals(theOther.getX())
                && this.getY().equals(theOther.getY());
    }

    /**
     * Gets the opposite value of a point, defined as the following:
     * if P = (X, Y), opposite of P = (-X, Y).
     *
     * @return Curve.E521 point which is opposite of a given point as defined above.
     */
    public E521 getOpposite() {
        return new E521(myX.negate(), this.getY());
    }

    /**
     * Adds two Curve.E521 points and returns another (X, Y) point which is on the Curve.E521 curve defined
     * using parameters set by constructor. Add operation is defined as:
     * (x1, y1) + (x2, y2) = ((x1y2 + y1x2) / (1 + (d)x1x2y1y2)), ((y1y2 - x1x2) / (1 - (d)x1x2y1y2))
     * where "/" is defined to be multiplication by modular inverse.
     *
     * @param theOther e521 point to be added to current point.
     * @return Curve.E521 point which is the result of the addition operation defined here. Will
     * fail with exception if point is not on curve defined in class fields.
     */
    public E521 add(final E521 theOther) {

        BigInteger x1 = this.getX();
        BigInteger x2 = theOther.getX();

        BigInteger y1 = this.getY();
        BigInteger y2 = theOther.getY();

        BigInteger xNum = ((x1.multiply(y2)).add(y1.multiply(x2))).mod(P);
        BigInteger xDenom = (BigInteger.ONE.add(D.multiply(x1).multiply(x2).multiply(y1).multiply(y2))).mod(P);

        xDenom = xDenom.modInverse(P);
        BigInteger newX = xNum.multiply(xDenom).mod(P);

        BigInteger yNum = (y1.multiply(y2).subtract(x1.multiply(x2))).mod(P);
        BigInteger yDenom = (BigInteger.ONE.subtract(D.multiply(x1).multiply(x2).multiply(y1).multiply(y2))).mod(P);

        yDenom = yDenom.modInverse(P);
        BigInteger newY = yNum.multiply(yDenom).mod(P);

        return new E521(newX, newY);
    }

    /**
     * Naive multiplication. Computes sP as P[1] + P[2] ... P[i] 
     * @param theS is number of times to add P to itself. 
     * @param P is the point to multiply. 
     */
    public E521 multiply(final BigInteger theS, final E521 P) {
        E521 result = new E521(BigInteger.ZERO, BigInteger.ONE);
        for(int i = 0; i < theS.intValue(); i ++) {
            result = result.add(P);
        }
        return result;
    }

    /**
     * Double and add multiplication for Curve.E521 point, index decreasing.
     * @param theS Scalar value to multiply by
     * @return Curve.E521 point which is product of multiplication
     * with S.
     */
    public E521 doubleAndAdd(final BigInteger theS) {

        E521 result = new E521(BigInteger.ZERO, BigInteger.ONE);
        int idx = theS.bitLength();
        while (idx >= 0) {
            result = result.add(result);
            if (theS.testBit(idx--)) {
                result = result.add(this);
            }
        }
        return result;
    }

    /**
     * EC Multiplication algorithm using the Montgomery Ladder approach to mitigate
     * timing side channel attacks. Mostly constructed around
     * https://eprint.iacr.org/2014/140.pdf pg 4
     * 2R here is defined as a call to the addition method to act as a doubling algorithm. Can probably
     * be replaced with individual doubling algorithm.
     *
     * @param theS scalar value to multiply by. S is a private key and should be kept secret.
     * @return Curve.E521 point which is result of multiplication.
     */
    public E521 multiplyMontgomery(final BigInteger theS, final E521 P) {

        E521 r0 = new E521(BigInteger.ZERO, BigInteger.ONE);
        E521 r1 = P;
        int idx = theS.bitLength();
        while (idx >= 0) {
            if (theS.testBit(idx--)) {
                r0 = r0.add(r1);
                r1 = r1.add(r1);
            } else {
                r1 = r0.add(r1);
                r0 = r0.add(r0);
            }
        }
        return r0; // r0 = P * s
    }


    /**
     * Compute a square root of v mod p with a specified
     * least significant bit, if such a root exists.
     *
     * @param v   the radicand.
     * @param p   the modulus (must satisfy p mod 4 = 3).
     * @param lsb desired least significant bit (true: 1, false: 0).
     * @return a square root r of v mod p with r mod 2 = 1 iff lsb = true
     * if such a root exists, otherwise null.
     */
    public static BigInteger sqrt(BigInteger v, BigInteger p, boolean lsb) {
        assert (p.testBit(0) && p.testBit(1)); // p = 3 (mod 4)
        if (v.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger r = v.modPow(p.shiftRight(2).add(BigInteger.ONE), p);
        if (r.testBit(0) != lsb) {
            r = p.subtract(r); // correct the lsb }
            return (r.multiply(r).subtract(v).mod(p).signum() == 0) ? r : null;
        }
        return r;
    }


    /**
     * Gets string representation of object as pair of X and Y coordinates.
     * @return String which is labelled pair of X and Y coordinates.
     */
    public String toString(final String name) {
        return name + "\n" + "X coordinate: " + getX().toString() + "\n"
                + "Y coordinate: " + getY().toString() + "\n";
    }

    /**
     * Gets current X value.
     * @return BigInteger which is current X coordinate of point.
     */
    public BigInteger getX() {
        return myX;
    }

    /** Set X coordinate. */
    public void setX(final BigInteger theX) {myX = theX;}
    /** Set Y coordinate. */
    public void setY(final BigInteger theY) {myY = theY;}

    /**
     * Gets current Y value.
     * @return BigInteger which is current Y coordinate of point.
     */
    public BigInteger getY() { return myY; }

    /** Returns r value for curve.
     * @return r value for curve. */
    public BigInteger getR() { return R; }




}

pub mod e521 {
    use std::{str::FromStr, ops::{Sub, Mul}};
    use num_bigint::{BigInt, Sign};
    use num::Integer;
    use num::One;
    use num::Zero;
    /*
    The elliptic curve that will be implemented is known as the 𝐸521 curve (a so-
    called Edwards curve), defined by the following parameters:
    • 𝑝 ≔ 2^521−1, a Mersenne prime defining the finite field 𝔽𝑝 .
    • curve equation: 𝑥^2 + 𝑦^2 = 1 + 𝑑𝑥^2𝑦^2 with 𝑑 = −376014. 
    */
    #[derive(Debug)]
    pub struct E521 {
    pub x: BigInt,
    pub y: BigInt,
    pub p: BigInt,
    pub d: BigInt,
    pub r: BigInt,
    pub n: BigInt,
    }
    /// Initializes r value for curve. 
    fn set_r() -> BigInt {
    let r = BigInt::from(2);
    let r = r.pow(519);
    let s = BigInt::from_str("337554763258501705789107630418782636071904961214051226618635150085779108655765").unwrap();
    let r = r.sub(s);
    return r;
    }

    /// Initializes curve order, 𝑝 ≔ 2^521−1, a Mersenne prime defining the finite field 𝔽𝑝.
    fn set_p() -> BigInt {
    let p = BigInt::from(2);
    let p = p.pow(521);
    let p = p - 1;
    return p;
    }

    /// Initializes number of points on the curve. 
    fn set_n() -> BigInt {
    let n = set_r();
    let n = n * &BigInt::from(4);
    return n;
    }

    /// Sets the curve d parameter. 
    fn set_d() -> BigInt { return Some(BigInt::from(-376014)).unwrap();}





    /// Generates the neutral point (0, 1)
    pub fn get_e521_id_point() -> E521 {
    let point = E521{
        x: BigInt::from(0),
        y: BigInt::from(1),
        p: set_p(),
        d: set_d(),
        r: set_r(), 
        n: set_n()
    };
    point
    }

    /// Gets point for arbitrary (x, y) TODO verify point is on curve
    pub fn get_e521_point(x: BigInt, y: BigInt) -> E521 {
    let point = E521{
        x,
        y,
        p: set_p(),
        d: set_d(),
        r: set_r(), 
        n: set_n()
    };
    point
    }

    /// Gets point for arbitrary (x, y) TODO verify point is on curve
    pub fn get_e521_gen_point(msb: bool) -> E521 {
    let x = BigInt::from(4);
    let new_x = x.clone();
    let point = E521{
        x,
        y: solve_for_y(&new_x, set_p(), msb),
        p: set_p(),
        d: set_d(),
        r: set_r(), 
        n: set_n()
    };
    point
    }

    /// If a point is defined as (x, y) then its negation is (-x, y)
    pub fn negate_point(p: &E521) -> E521 {
    let x = p.x.clone();
    let y = p.y.clone();
    let x = x * -1;
    let point = get_e521_point(x, y);
    point
    }


    // point operations


    ///Adds two E521 points and returns another E521 curve point. If a point is defined as
    /// E521 = (x, y), then E521 addition is defined as:

    ///(x1, y1) + (x2, y2) = ((x1y2 + y1x2) / (1 + (d)x1x2y1y2)), ((y1y2 - x1x2) / (1 - (d)x1x2y1y2))

    ///where "/" is defined to be multiplication by modular inverse.
    pub fn add_points(p1  : &E521, p2: &E521) -> E521 {

    let p = p1.p.clone();
    let d = p1.d.clone();

    let x1 = p1.x.clone();
    let y1 = p1.y.clone();

    // (x1y2 + y1x2)
    let x1y2 = (x1 * &p2.y).mod_floor(&p);
    let y1x2 = (y1 * &p2.x).mod_floor(&p);
    let x1y2y1x2_sum = (x1y2 + y1x2).mod_floor(&p);

    let x1 = p1.x.clone();

    // 1 / (1 + (d)x1x2y1y2))
    let one_plus_dx1x2y1y2 = (BigInt::from(1) + (d * x1 * &p2.x) *(&p1.y)* &p2.y).mod_floor(&p);
    let one_plus_dx1x2y1y2inv = mod_inv(&one_plus_dx1x2y1y2, &p);

    let x1 = p1.x.clone();
    let y1 = p1.y.clone();

    // (y1y2 - x1x2)
    let y1y2x1x2_difference = ((y1 * &p2.y) - (x1 * &p2.x)).mod_floor(&p);

    let x1 = p1.x.clone();
    let d = p1.d.clone();

    // 1 / (1 - (d)x1x2y1y2))
    let one_minus_dx1x2y1y2 = (BigInt::from(1) - (d * x1 * &p2.x * &p1.y * &p2.y)).mod_floor(&p);
    let one_minus_dx1x2y1y2inv = mod_inv(&one_minus_dx1x2y1y2, &p);

    let new_x = (x1y2y1x2_sum * one_plus_dx1x2y1y2inv).mod_floor(&p);
    let new_y = (y1y2x1x2_difference * one_minus_dx1x2y1y2inv).mod_floor(&p);;
    get_e521_point(new_x, new_y)
    }

    /// constant time multiplication NOTE not memory safe afaik.
    /// multiplication is defined to be P + P + P .... s times
    pub fn sec_mul(s: &BigInt, point: &E521) -> E521 {
    let mut r1 = get_e521_gen_point(false);
    let mut r0 = get_e521_id_point();
    let len = s.bits();
    for i in (0..len).rev()  {
        if s.bit(i) {
            r0 = add_points(&r0, &r1);
            r1 = add_points(&r1, &r1);
        } else { 
            r1 = add_points(&r0, &r1);
            r0 = add_points(&r0, &r1);
        }
        }
    r0
    }



    /// Solves for y in curve equation 𝑥^2 + 𝑦^2 = 1 + 𝑑𝑥^2𝑦^2
    fn solve_for_y(x: &BigInt, p: BigInt, msb: bool) -> BigInt {
    let num = BigInt::from(1) - x.pow(2);
    let num = mod_formula(&num, &p);
    let denom = BigInt::from(376_014) * x.pow(2) + BigInt::from(1);
    let denom = mod_formula(&denom, &p);
    let denom = mod_inv(&denom, &p);
    let radicand = num * denom;
    let y = sqrt(&radicand, p, msb);
    y
    }

    /// Compute a square root of v mod p with a specified
    /// least significant bit, if such a root exists.
    fn sqrt(v: &BigInt, p: BigInt, lsb: bool) -> BigInt {
    if v.sign() ==  Sign::NoSign{ return BigInt::from(0); }
    let r = v.modpow(&((p.clone() >> 2) + 1), &p);
    if !r.bit(0).eq(&lsb) {
        let new_r = &p - r; // correct the lsb
        let borrowed_r = new_r.clone();
        let return_r = new_r.clone();
        let bi = mod_formula(&new_r.mul(borrowed_r).sub(v), &p);
        if bi.sign() == Sign::NoSign {
            return return_r;
        } else { return BigInt::from(0); }
    } r
    }

    /// Performs BigInt modular arithematic.
    fn mod_formula(a: &BigInt, b: &BigInt) -> BigInt { ((a % b) + b) % b }

    /// Performs modular inverse via euclidian algorithm. 
    fn mod_inv(n: &BigInt, p: &BigInt) -> BigInt {
    if p.is_one() { return BigInt::one() }
    let (mut a, mut m, mut x, mut inv) = (n.clone(), p.clone(), BigInt::zero(), BigInt::one());
    while a < BigInt::zero() { a += p }
    while a > BigInt::one() {
        let (div, rem) = a.div_rem(&m);
        inv -= div * &x;
        a = rem;
        std::mem::swap(&mut a, &mut m);
        std::mem::swap(&mut x, &mut inv);
    }
    if inv < BigInt::zero() { inv += p }
    inv
    }


}
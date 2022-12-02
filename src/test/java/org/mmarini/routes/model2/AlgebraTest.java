package org.mmarini.routes.model2;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static java.lang.Math.PI;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgebraTest {

    @Test
    void angle() {
        assertEquals(0D, Algebra.angle(new Point2D.Double(1, 0)));
        assertEquals(PI / 2, Algebra.angle(new Point2D.Double(0, 1)));
        assertEquals(-PI / 2, Algebra.angle(new Point2D.Double(0, -1)));
        assertEquals(PI, Algebra.angle(new Point2D.Double(-1, 0)));
        assertEquals(PI / 4, Algebra.angle(new Point2D.Double(1, 1)));
        assertEquals(-PI / 4, Algebra.angle(new Point2D.Double(1, -1)));
        assertEquals(PI * 3 / 4, Algebra.angle(new Point2D.Double(-1, 1)));
        assertEquals(-PI * 3 / 4, Algebra.angle(new Point2D.Double(-1, -1)));
        assertEquals(0, Algebra.angle(new Point2D.Double()));
    }

    @Test
    void dot() {
        assertEquals(0, Algebra.dot(
                new Point2D.Double(0, 1),
                new Point2D.Double(1, 0)
        ));
        assertEquals(1, Algebra.dot(
                new Point2D.Double(1, 0),
                new Point2D.Double(1, 0)
        ));
        assertEquals(-1, Algebra.dot(
                new Point2D.Double(-1, 0),
                new Point2D.Double(1, 0)
        ));
        assertEquals(23, Algebra.dot(
                new Point2D.Double(2, 3),
                new Point2D.Double(4, 5)
        ));
    }

    @Test
    void fromPolar() {
        assertEquals(new Point2D.Double(1, 0), Algebra.fromPolar(new Algebra.Polar(1, 0)));
        assertEquals(new Point2D.Double(-1, 0), Algebra.fromPolar(new Algebra.Polar(1, -PI)));
    }

    @Test
    void length() {
        assertEquals(4, Algebra.length(new Point2D.Double(0, -4)));
        assertEquals(5, Algebra.length(new Point2D.Double(3, -4)));
    }

    @Test
    void neg() {
        assertEquals(new Point2D.Double(-1, -2), Algebra.neg(new Point2D.Double(1, 2)));
    }

    @Test
    void prod() {
        assertEquals(new Point2D.Double(3, -6), Algebra.prod(new Point2D.Double(1, -2), 3));
    }

    @Test
    void sqrLength() {
        assertEquals(25, Algebra.sqrLength(new Point2D.Double(3, 4)));
    }

    @Test
    void sub() {
        assertEquals(new Point2D.Double(1, 2), Algebra.sub(
                new Point2D.Double(2, 3),
                new Point2D.Double(1, 1)));
    }

    @Test
    void sum() {
        assertEquals(new Point2D.Double(0, 2), Algebra.sum(
                new Point2D.Double(2, 3),
                new Point2D.Double(1, 1),
                new Point2D.Double(-3, -2)));
    }

    @Test
    void testFromPolar() {
        assertEquals(new Point2D.Double(1, 0), Algebra.fromPolar(1, 0));
        assertEquals(new Point2D.Double(-1, 0), Algebra.fromPolar(1, -PI));
    }

    @Test
    void toPoint() {
        assertEquals(new Point2D.Double(1, 0), new Algebra.Polar(1, 0).toPoint());
        assertEquals(new Point2D.Double(-1, 0), new Algebra.Polar(1, -PI).toPoint());
    }

    @Test
    void toPolar() {
        assertEquals(new Algebra.Polar(1, 0), Algebra.toPolar(new Point2D.Double(1, 0)));
        assertEquals(new Algebra.Polar(2, PI), Algebra.toPolar(new Point2D.Double(-2, 0)));
        assertEquals(new Algebra.Polar(3, PI / 2), Algebra.toPolar(new Point2D.Double(0, 3)));
        assertEquals(new Algebra.Polar(4, -PI / 2), Algebra.toPolar(new Point2D.Double(0, -4)));
    }

    @Test
    void vectProd() {
        assertEquals(1, Algebra.vectProd(
                new Point2D.Double(1, 0),
                new Point2D.Double(0, 1)
        ));
        assertEquals(-1, Algebra.vectProd(
                new Point2D.Double(0, 1),
                new Point2D.Double(1, 0)
        ));
        assertEquals(3, Algebra.vectProd(
                new Point2D.Double(2, 1),
                new Point2D.Double(1, 2)
        ));
    }
}
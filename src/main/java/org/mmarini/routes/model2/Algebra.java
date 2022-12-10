package org.mmarini.routes.model2;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;

/**
 * Algebric functions for 2D vector space
 */
public interface Algebra {
    double HALF_PI = PI / 2;

    /**
     * Return the polar direction of a vector (RADS)
     *
     * @param a the vector
     */
    static double angle(Point2D a) {
        return atan2(a.getY(), a.getX());
    }

    /**
     * Returns the scalar product of 2 vectors (a * b)
     *
     * @param a the first vector
     * @param b the second vector
     */
    static double dot(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    /**
     * Returns the cartesian coordinates from polar coordinates
     *
     * @param p the polar coordinates
     */
    static Point2D fromPolar(Polar p) {
        return p.toPoint();
    }


    /**
     * Returns the cartesian coordinates from polar coordinates
     *
     * @param r     the vector length
     * @param theta the polar direction (RADS)
     */
    static Point2D fromPolar(double r, double theta) {
        if (theta == 0) {
            return new Point2D.Double(r, 0);
        } else if (theta == PI || theta == -PI) {
            return new Point2D.Double(-r, 0);
        } else if (theta == HALF_PI) {
            return new Point2D.Double(0, r);
        } else if (theta == -HALF_PI) {
            return new Point2D.Double(0, -r);
        } else {
            return new Point2D.Double(r * cos(theta), r * sin(theta));
        }
    }

    /**
     * Returns the length of vector
     *
     * @param a the vector
     */
    static double length(Point2D a) {
        return sqrt(sqrLength(a));
    }

    /**
     * Returns the negate vector (-a)
     *
     * @param a the vector
     */
    static Point2D neg(Point2D a) {
        return new Point2D.Double(-a.getX(), -a.getY());
    }

    /**
     * Returns the product of vector by scalar (k * a)
     *
     * @param a the vector
     * @param k the scalar
     */
    static Point2D prod(Point2D a, double k) {
        return new Point2D.Double(a.getX() * k, a.getY() * k);
    }

    /**
     * Returns the step rounded value (the nearest approximated value by step)
     *
     * @param value the value
     * @param step  the step
     */
    static double snapTo(double value, double step) {
        return Math.round(value / step) * step;
    }

    /**
     * Returns the squared length of a vector
     *
     * @param a the vector
     */
    static double sqrLength(Point2D a) {
        return dot(a, a);
    }

    /**
     * Returns the difference of two vectors (a - b)
     *
     * @param a the vector
     * @param b the vector
     */
    static Point2D sub(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }

    /**
     * Returns the sum of vectors (sum pts_i)
     *
     * @param pts the vectors
     */
    static Point2D sum(Point2D... pts) {
        requireNonNull(pts);
        double x = Arrays.stream(pts).mapToDouble(Point2D::getX).sum();
        double y = Arrays.stream(pts).mapToDouble(Point2D::getY).sum();
        return new Point2D.Double(x, y);
    }

    /**
     * Retruns the polar coordinate from cartesian coorrdinate
     *
     * @param a the cartesian coordinate
     */
    static Polar toPolar(Point2D a) {
        return new Polar(length(a), angle(a));
    }

    /**
     * Returns the vectorial product of 2 vector (a x b)
     *
     * @param a the first vector
     * @param b the second vector
     */
    static double vectProd(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    /**
     * The polar coordinate vector
     */
    class Polar {
        private final double length;
        private final double theta;

        /**
         * Creates the polar coordinate vector
         *
         * @param length the length of vector
         * @param theta  the angular direction
         */
        public Polar(double length, double theta) {
            this.length = length;
            this.theta = theta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Polar polar = (Polar) o;
            return Double.compare(polar.length, length) == 0 && Double.compare(polar.theta, theta) == 0;
        }

        /**
         * Returns the length of vector
         */
        public double getLength() {
            return length;
        }

        /**
         * Returns the angular direction (RADS)
         */
        public double getTheta() {
            return theta;
        }

        @Override
        public int hashCode() {
            return Objects.hash(length, theta);
        }

        /**
         * Returns the cartesian coordinate of vector
         */
        public Point2D toPoint() {
            return fromPolar(length, theta);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Polar.class.getSimpleName() + "[", "]")
                    .add("radius=" + length)
                    .add("theta=" + theta)
                    .toString();
        }
    }
}

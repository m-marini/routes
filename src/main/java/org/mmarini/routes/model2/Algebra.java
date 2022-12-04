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

    static double angle(Point2D a) {
        return atan2(a.getY(), a.getX());
    }

    static double dot(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    static Point2D fromPolar(Polar p) {
        return p.toPoint();
    }

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

    static double length(Point2D a) {
        return sqrt(sqrLength(a));
    }

    static Point2D neg(Point2D a) {
        return new Point2D.Double(-a.getX(), -a.getY());
    }

    static Point2D prod(Point2D a, double k) {
        return new Point2D.Double(a.getX() * k, a.getY() * k);
    }

    static double snapTo(double value, double step) {
        return Math.round(value / step) * step;
    }

    static double sqrLength(Point2D a) {
        return dot(a, a);
    }

    static Point2D sub(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }

    static Point2D sum(Point2D... pts) {
        requireNonNull(pts);
        double x = Arrays.stream(pts).mapToDouble(Point2D::getX).sum();
        double y = Arrays.stream(pts).mapToDouble(Point2D::getY).sum();
        return new Point2D.Double(x, y);
    }

    static Polar toPolar(Point2D a) {
        return new Polar(length(a), angle(a));
    }

    static double vectProd(Point2D a, Point2D b) {
        requireNonNull(a);
        requireNonNull(b);
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    class Polar {
        private final double radius;
        private final double theta;

        public Polar(double radius, double theta) {
            this.radius = radius;
            this.theta = theta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Polar polar = (Polar) o;
            return Double.compare(polar.radius, radius) == 0 && Double.compare(polar.theta, theta) == 0;
        }

        public double getRadius() {
            return radius;
        }

        public double getTheta() {
            return theta;
        }

        @Override
        public int hashCode() {
            return Objects.hash(radius, theta);
        }

        public Point2D toPoint() {
            return fromPolar(radius, theta);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Polar.class.getSimpleName() + "[", "]")
                    .add("radius=" + radius)
                    .add("theta=" + theta)
                    .toString();
        }
    }
}

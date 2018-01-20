package org.tibtof;

import java.awt.geom.Point2D;

/**
 * Alias for {@code Point2D.Double}
 *
 * @see Double
 */
public class
Point extends Point2D.Double {

    public Point(double x, double y) {
        super(x, y);
    }

    public Point project(double distance, double angle) {
        return new Point(getX() + distance * Math.sin(angle), getY() + distance * Math.cos(angle));
    }

    public double angle(Point other) {
        return Math.atan2(other.x - this.x, other.y - this.y);
    }

}

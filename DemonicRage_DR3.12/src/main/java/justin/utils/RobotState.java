/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.lang.Object
 */
package justin.utils;

import java.awt.geom.Point2D;

public class RobotState {
    public Point2D.Double location;
    public double heading;
    public double velocity;
    public long time;
    public boolean smoothing;

    public RobotState(Point2D.Double botLocation, double botHeadingRadians, double botVelocity) {
        this.location = botLocation;
        this.heading = botHeadingRadians;
        this.velocity = botVelocity;
        this.smoothing = false;
    }

    public RobotState(Point2D.Double botLocation, double botHeadingRadians, double botVelocity, long currentTime) {
        this(botLocation, botHeadingRadians, botVelocity);
        this.time = currentTime;
    }

    public RobotState(Point2D.Double botLocation, double botHeadingRadians, double botVelocity, long currentTime, boolean smooth) {
        this(botLocation, botHeadingRadians, botVelocity, currentTime);
        this.smoothing = smooth;
    }

    public Object clone() {
        return new RobotState((Point2D.Double)this.location.clone(), this.heading, this.velocity, this.time, this.smoothing);
    }
}


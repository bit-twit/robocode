/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.lang.Object
 */
package justin.movement;

import java.awt.geom.Point2D;
import justin.utils.MovSimStat;

public class Destination {
    public Point2D.Double location;
    public double risk;
    public double goAngle;
    public long time;
    public MovSimStat[] saveSimResult;

    public Destination(Point2D.Double loc, double r, double ang) {
        this.location = loc;
        this.risk = r;
        this.goAngle = ang;
    }

    public Destination(Point2D.Double loc, long t) {
        this.location = loc;
        this.time = t;
    }
}


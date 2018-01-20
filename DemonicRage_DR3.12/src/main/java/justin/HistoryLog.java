/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.lang.Object
 */
package justin;

import java.awt.geom.Point2D;

public class HistoryLog {
    public long scanTime = 0L;
    public int round = 0;
    public Point2D.Double location;
    public double headingRadians;
    public double absBearingRadians;
    public double distance;
    public double velocity = 0.0;
    public HistoryLog next;
    public HistoryLog previous;
}


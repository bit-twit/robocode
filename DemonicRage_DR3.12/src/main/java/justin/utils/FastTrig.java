/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.lang.Math
 *  java.lang.Object
 */
package justin.utils;

import java.awt.geom.Point2D;

public class FastTrig {
    public static final double PI = 3.141592653589793;
    public static final double TWO_PI = 6.283185307179586;
    public static final double HALF_PI = 1.5707963267948966;
    public static final double QUARTER_PI = 0.7853981633974483;
    public static final double THREE_OVER_TWO_PI = 4.71238898038469;
    private static final int TRIG_DIVISIONS = 8192;
    private static final int TRIG_HIGH_DIVISIONS = 131072;
    private static final double K = 1303.7972938088067;
    private static final double ACOS_K = 65535.0;
    private static final double TAN_K = 41721.51340188181;
    private static final double[] sineTable = new double[8192];
    private static final double[] tanTable = new double[131072];
    private static final double[] acosTable = new double[131072];

    public static final void init() {
        int i = 0;
        while (i < 8192) {
            FastTrig.sineTable[i] = Math.sin((double)((double)i / 1303.7972938088067));
            ++i;
        }
        i = 0;
        while (i < 131072) {
            FastTrig.tanTable[i] = Math.tan((double)((double)i / 41721.51340188181));
            FastTrig.acosTable[i] = Math.acos((double)((double)i / 65535.0 - 1.0));
            ++i;
        }
    }

    public static final double sin(double value) {
        return sineTable[(int)((value * 1303.7972938088067 + 0.5) % 8192.0 + 8192.0) & 8191];
    }

    public static final double cos(double value) {
        return sineTable[(int)((value * 1303.7972938088067 + 0.5) % 8192.0 + 10240.0) & 8191];
    }

    public static final double tan(double value) {
        return tanTable[(int)((value * 41721.51340188181 + 0.5) % 131072.0 + 131072.0) & 131071];
    }

    public static final double asin(double value) {
        return 1.5707963267948966 - FastTrig.acos(value);
    }

    public static final double acos(double value) {
        return acosTable[(int)(value * 65535.0 + 65535.5)];
    }

    public static final double atan(double value) {
        return value >= 0.0 ? FastTrig.acos(1.0 / FastTrig.sqrt(value * value + 1.0)) : - FastTrig.acos(1.0 / FastTrig.sqrt(value * value + 1.0));
    }

    public static final double atan2(double x, double y) {
        return x >= 0.0 ? FastTrig.acos(y / FastTrig.sqrt(x * x + y * y)) : - FastTrig.acos(y / FastTrig.sqrt(x * x + y * y));
    }

    public static final double sqrt(double x) {
        return Math.sqrt((double)x);
    }

    public static Point2D.Double project(Point2D.Double source, double angle, double distance) {
        return new Point2D.Double(source.getX() + FastTrig.sin(angle) * distance, source.getY() + FastTrig.cos(angle) * distance);
    }

    public static double absoluteBearing(Point2D.Double sourceLocation, Point2D.Double target) {
        return FastTrig.atan2(target.x - sourceLocation.x, target.y - sourceLocation.y);
    }
}


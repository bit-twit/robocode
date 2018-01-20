/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.awt.Graphics2D
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.Hashtable
 *  java.util.Vector
 *  robocode.Rules
 *  robocode.ScannedRobotEvent
 *  robocode.util.Utils
 */
package justin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Vector;
import justin.Enemy;
import justin.HistoryLog;
import justin.Module;
import justin.utils.DRUtils;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class BulletInfoEnemy {
    public static final double ROLLING_DEPTH = 20.0;
    public static final double SMOOTH_BIN_SPEED = 24.0;
    public static final double BOT_WIDTH = 18.0;
    public static double flattenerValue = 0.0;
    public static final double UPDATE_MISSED_BULLET = 20.0;
    public static final double PAINT_GRAPH_SIZE = 20.0;
    public String fromName;
    public boolean surf = true;
    public Point2D.Double fireLocation;
    public Point2D.Double targetLocation;
    public double velocity;
    public double power;
    public double distanceTraveled;
    public double myLateralDir;
    public double[] buffer;
    public double surfWeight;
    public double hotHeading;
    public double linearHeading;
    public double circularHeading;
    public double guessFactorHeading;
    public double antiSurfHeading;

    public static void detection(Enemy scan, ScannedRobotEvent e, Module bot) {
        if (scan.deltaEnergy <= 3.01 && scan.deltaEnergy >= 0.1 && Math.abs((double)(Math.abs((double)scan.previousVelocity) - Math.abs((double)scan.velocity))) <= scan.deltaScanTime * 2.0 && scan.deltaScanTime <= 8.0 && bot.getTime() > 3L && (scan.cbC < 3.0 || scan.scanTime - (double)scan.timeLastBulletHit < (double)Math.min((long)45L, (long)bot.getTime()))) {
            BulletInfoEnemy enemyBullet = new BulletInfoEnemy();
            enemyBullet.surf = scan.distance < scan.cbD * 1.3 || scan.scanTime - (double)scan.timeLastBulletHit < (double)Math.min((long)45L, (long)bot.getTime()) || bot.getOthers() < 2 || bot.enemyBullets.size() == 0 && scan.distance <= bot.myData.cbD * 1.35;
            enemyBullet.fromName = scan.name;
            enemyBullet.power = scan.deltaEnergy;
            enemyBullet.distanceTraveled = enemyBullet.velocity = Rules.getBulletSpeed((double)enemyBullet.power);
            if (Module.melee) {
                scan.bulletShotsMelee += 1.0;
            } else {
                scan.bulletShots1v1 += 1.0;
            }
            HistoryLog info = scan.last;
            HistoryLog myInfo = bot.myData.last;
            double timeInHistory = Math.ceil((double)(scan.deltaScanTime / 2.0));
            double i = 0.0;
            while (i < timeInHistory) {
                if (info.previous == null) break;
                info = info.previous;
                myInfo = myInfo.previous;
                i += 1.0;
            }
            enemyBullet.fireLocation = info.location;
            enemyBullet.targetLocation = myInfo.location;
            enemyBullet.distanceTraveled = enemyBullet.velocity * (timeInHistory + 0.0);
            enemyBullet.hotHeading = DRUtils.absoluteBearing(enemyBullet.fireLocation, enemyBullet.targetLocation);
            double rotation = myInfo.previous == null ? 1.0 : Utils.normalRelativeAngle((double)(enemyBullet.hotHeading - DRUtils.absoluteBearing(enemyBullet.fireLocation, myInfo.previous.location)));
            enemyBullet.myLateralDir = rotation >= 0.0 ? 1 : -1;
            enemyBullet.buffer = null;
            enemyBullet.buffer = Module.melee ? scan.surfStatsMelee : scan.surfStats1vrs1;
            enemyBullet.circularHeading = BulletInfoEnemy.getEnemiesCircularTargeting(scan, bot, enemyBullet.power, bot.myData.deltaHeadingRadians);
            enemyBullet.guessFactorHeading = BulletInfoEnemy.getAngleToBin(BulletInfoEnemy.getBinIndexWithHighestValue(enemyBullet), enemyBullet);
            enemyBullet.antiSurfHeading = BulletInfoEnemy.getAngleToBin(BulletInfoEnemy.getBinIndexWithLowestValue(enemyBullet), enemyBullet);
            enemyBullet.surfWeight = (1.0 + (1.0 - Math.min((double)scan.distance, (double)1000.0) / 1000.0) * ((scan.cbC + 2.0) / (double)(bot.getOthers() + 1)) * (double)(11 - bot.getOthers())) / 2.0;
            bot.enemyBullets.add((Object)enemyBullet);
        }
    }

    public static double[] getSegment(HistoryLog myInfo, HistoryLog info, double[][][][][] stats, Module bot) {
        int FWD_W_Index = (int)(0.0 * DRUtils.getWallDist(myInfo.location, myInfo.headingRadians, Module.bw, Module.bh));
        int distanceIndex = (int)(3.0 * Math.min((double)info.distance, (double)900.0) / 900.0);
        int velocityIndex = (int)(3.0 * Math.abs((double)myInfo.velocity) / 8.0);
        int previousVelocityIndex = (int)(3.0 * Math.abs((double)myInfo.previous.velocity) / 8.0);
        int BWD_W_Index = (int)(0.0 * DRUtils.getWallDist(myInfo.location, Utils.normalAbsoluteAngle((double)(myInfo.headingRadians + 3.141592653589793)), Module.bw, Module.bh));
        int runTime_Index = (int)(3.0 * Math.min((double)bot.myData.tSDC, (double)30.0) / 30.0);
        double[] buffer = stats[distanceIndex][velocityIndex][previousVelocityIndex][runTime_Index];
        return buffer;
    }

    public static void updateEnemyBullets(Module bot) {
        int i = 0;
        while (i < bot.enemyBullets.size()) {
            BulletInfoEnemy bullet = (BulletInfoEnemy)bot.enemyBullets.get(i);
            bullet.distanceTraveled += bullet.velocity;
            if (bullet.distanceTraveled > bot.myData.location.distance((Point2D)bullet.fireLocation) + 20.0) {
                BulletInfoEnemy.logHit(bullet, bot.myData.location, Double.MIN_VALUE, bot);
                bot.enemyBullets.remove(i);
                --i;
            }
            ++i;
        }
    }

    public static void logHit(BulletInfoEnemy ew, Point2D.Double targetLocation, double value, Module bot) {
        if (value == 0.0 && !ew.surf) {
            return;
        }
        Enemy enemyWhoShot = (Enemy)Module.enemies.get((Object)ew.fromName);
        double index = BulletInfoEnemy.getBinIndex(ew, targetLocation);
        if (value != Double.NaN) {
            double angle = BulletInfoEnemy.getAngleToBin(index, ew);
            double tolerence = Math.atan((double)(18.0 / ew.distanceTraveled));
            if (Math.abs((double)(angle - ew.guessFactorHeading)) < tolerence) {
                enemyWhoShot.TMguessFactor += 1.0;
            }
            if (Math.abs((double)(angle - ew.hotHeading)) < tolerence) {
                enemyWhoShot.TMheadOn += 1.0;
            }
            if (Math.abs((double)(angle - ew.circularHeading)) < tolerence) {
                enemyWhoShot.TMcircular += 1.0;
            }
            if (Math.abs((double)(angle - ew.linearHeading)) < tolerence) {
                enemyWhoShot.TMlinear += 1.0;
            }
            if (Math.abs((double)(angle - ew.antiSurfHeading)) < tolerence) {
                enemyWhoShot.TMantiSurf += 1.0;
            }
        } else {
            value = flattenerValue;
        }
        int x = 0;
        while (x < 41) {
            double newEntry = 0.0;
            if (Math.abs((double)((double)x - index)) < 4.0) {
                newEntry = 1.0;
            }
            ew.buffer[x] = DRUtils.rollingAverage(ew.buffer[x], newEntry * 100.0 * value, 20.0);
            ++x;
        }
        x = 1;
        while (x < 40) {
            ew.buffer[x] = DRUtils.rollingAverage(ew.buffer[x], ew.buffer[x + 1], 24.0);
            ew.buffer[40 - x] = DRUtils.rollingAverage(ew.buffer[40 - x], ew.buffer[40 - x - 1], 24.0);
            ++x;
        }
    }

    public static Point2D.Double getBinIndexRange(BulletInfoEnemy ew, Point2D.Double targetLocation) {
        double offsetAngleMax = DRUtils.absoluteBearing(ew.fireLocation, targetLocation) - ew.hotHeading + Math.atan((double)(22.0 / ew.fireLocation.distance((Point2D)targetLocation)));
        double offsetAngleMin = DRUtils.absoluteBearing(ew.fireLocation, targetLocation) - ew.hotHeading - Math.atan((double)(22.0 / ew.fireLocation.distance((Point2D)targetLocation)));
        double factorMax = Utils.normalRelativeAngle((double)offsetAngleMax) / (BulletInfoEnemy.maxEscapeAngle(ew.velocity) * ew.myLateralDir);
        double factorMin = Utils.normalRelativeAngle((double)offsetAngleMin) / (BulletInfoEnemy.maxEscapeAngle(ew.velocity) * ew.myLateralDir);
        return new Point2D.Double((double)((int)(factorMax * 20.0) + 20), (double)((int)(factorMin * 20.0) + 20));
    }

    public static int getBinIndex(BulletInfoEnemy ew, Point2D.Double targetLocation) {
        double offsetAngle = DRUtils.absoluteBearing(ew.fireLocation, targetLocation) - ew.hotHeading;
        double factor = Utils.normalRelativeAngle((double)offsetAngle) / (BulletInfoEnemy.maxEscapeAngle(ew.velocity) * ew.myLateralDir);
        return (int)DRUtils.limit(0.0, factor * 20.0 + 20.0, 40.0);
    }

    public static int getBinIndexWithLowestValue(BulletInfoEnemy w) {
        double smallestValue = Double.POSITIVE_INFINITY;
        double binValue = Double.POSITIVE_INFINITY;
        int binIndex = 20;
        int x = 0;
        while (x < 41) {
            binValue = w.buffer[x];
            if (binValue < smallestValue) {
                binIndex = x;
                smallestValue = binValue;
            }
            ++x;
        }
        return binIndex;
    }

    public static int getBinIndexWithHighestValue(BulletInfoEnemy w) {
        double highestValue = Double.NEGATIVE_INFINITY;
        double binValue = Double.NEGATIVE_INFINITY;
        int binIndex = 20;
        int x = 0;
        while (x < 41) {
            binValue = w.buffer[x];
            if (binValue > highestValue) {
                binIndex = x;
                highestValue = binValue;
            }
            ++x;
        }
        return binIndex;
    }

    public static double getAngleToBin(double bin, BulletInfoEnemy b) {
        double factor = (bin - 20.0) / 20.0;
        double bearingOffset = b.myLateralDir * (factor * BulletInfoEnemy.maxEscapeAngle(b.velocity));
        return b.hotHeading + bearingOffset;
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin((double)(8.0 / velocity));
    }

    public static double[] getDefaultWave() {
        double[] wave = new double[41];
        int x = 0;
        while (x < 41) {
            double[] arrd = wave;
            int n = x;
            arrd[n] = arrd[n] + 3.0 / (Math.pow((double)(20 - x), (double)2.0) + 1.0);
            ++x;
        }
        return wave;
    }

    public static double[] getZeroWave() {
        double[] wave = new double[41];
        int x = 0;
        while (x < 41) {
            wave[x] = 0.0;
            ++x;
        }
        return wave;
    }

    public static BulletInfoEnemy getClosestSurfableWave(Module bot) {
        double closestDistance = Double.POSITIVE_INFINITY;
        BulletInfoEnemy surfWave = null;
        int x = 0;
        while (x < bot.enemyBullets.size()) {
            double distance;
            BulletInfoEnemy ew = (BulletInfoEnemy)bot.enemyBullets.get(x);
            if (ew.surf && (distance = bot.myData.location.distance((Point2D)ew.fireLocation) - ew.distanceTraveled) > ew.velocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
            ++x;
        }
        return surfWave;
    }

    public static BulletInfoEnemy getClosestSurfableWave2(BulletInfoEnemy wave, Module bot) {
        double closestDistance = Double.POSITIVE_INFINITY;
        BulletInfoEnemy surfWave = null;
        int x = 0;
        while (x < bot.enemyBullets.size()) {
            double distance;
            BulletInfoEnemy ew = (BulletInfoEnemy)bot.enemyBullets.get(x);
            if (ew.surf && (distance = bot.myData.location.distance((Point2D)ew.fireLocation) - ew.distanceTraveled) > ew.velocity && distance < closestDistance && ew != wave) {
                surfWave = ew;
                closestDistance = distance;
            }
            ++x;
        }
        return surfWave;
    }

    public static void paintWaves(Graphics2D g, Module bot) {
        double maxValue = Double.NEGATIVE_INFINITY;
        double minValue = Double.POSITIVE_INFINITY;
        int x = 0;
        while (x < bot.enemyBullets.size()) {
            BulletInfoEnemy b = (BulletInfoEnemy)bot.enemyBullets.get(x);
            BulletInfoEnemy.paintSimpleWave(b, g, bot);
            int j = 0;
            while (j < 41) {
                if (b.surf) {
                    if (b.buffer[j] < minValue) {
                        minValue = b.buffer[j];
                    }
                    if (b.buffer[j] > maxValue) {
                        maxValue = b.buffer[j];
                    }
                }
                ++j;
            }
            ++x;
        }
        if (maxValue <= 0.0) {
            maxValue = 1.0;
        }
        BulletInfoEnemy w = BulletInfoEnemy.getClosestSurfableWave(bot);
        BulletInfoEnemy w2 = BulletInfoEnemy.getClosestSurfableWave2(w, bot);
        BulletInfoEnemy.paintBins(w, g, minValue, maxValue);
        BulletInfoEnemy.paintBins(w2, g, minValue, maxValue);
        BulletInfoEnemy.paintGraph(w, g, 30, 55);
    }

    public static boolean paintGraph(BulletInfoEnemy w, Graphics2D g, int X, int Y) {
        String m;
        int moveOver = 0;
        g.setColor(new Color(250, 250, 250, 200));
        String string = m = Module.melee ? "   melee" : "   1vrs1";
        if (w != null) {
            g.drawString(String.valueOf((Object)w.fromName) + m, X, Y - 20);
        } else {
            g.drawString("    No Waves.", X, Y - 20);
        }
        int i = 0;
        while (i < 41) {
            Color c;
            Color color = c = i > 20 ? new Color(51, 204, 0, 200) : new Color(204, 0, 0, 200);
            if (i == 20) {
                c = new Color(250, 250, 0, 200);
            }
            g.setColor(c);
            if (w != null) {
                g.drawLine(X + i * 3, Y, X + i * 3, (int)((double)(Y + 2) + w.buffer[i] * 20.0));
            } else {
                g.drawLine(X + i * 3, Y, X + i * 3, Y + 2);
            }
            moveOver += 5;
            ++i;
        }
        return true;
    }

    public static boolean paintBins(BulletInfoEnemy w, Graphics2D g, double minValue, double maxValue) {
        if (w == null) {
            return false;
        }
        int i = 0;
        while (i < 41) {
            double ang = BulletInfoEnemy.getAngleToBin(i, w);
            Point2D.Double pt = DRUtils.project(w.fireLocation, ang, w.distanceTraveled - 10.0);
            g.setColor(DRUtils.calculateNewColor(Color.red, Color.blue, (w.buffer[i] - minValue) / (maxValue - minValue)));
            g.fillOval((int)pt.x - 3, (int)pt.y - 3, 6, 6);
            ++i;
        }
        return true;
    }

    public static void paintSimpleWave(BulletInfoEnemy w, Graphics2D g, Module bot) {
        if (w == null) {
            return;
        }
        g.setColor(new Color(250, 250, 250, 45));
        g.drawOval((int)(w.fireLocation.x - w.distanceTraveled), (int)(w.fireLocation.y - w.distanceTraveled), (int)(2.0 * w.distanceTraveled), (int)(2.0 * w.distanceTraveled));
    }

    public static double getEnemiesLinearTargeting(Enemy e, Module bot, double bulletPower) {
        return BulletInfoEnemy.getEnemiesCircularTargeting(e, bot, bulletPower, 0.0);
    }

    public static double getEnemiesCircularTargeting(Enemy e, Module bot, double bulletPower, double targetDeltaHeading) {
        Point2D.Double enemyNextLocation = DRUtils.nextLocation(e.location, e.velocity, e.headingRadians);
        double myHeading = bot.myData.headingRadians;
        double myVelocity = bot.myData.velocity;
        double deltaTime = 0.0;
        Point2D.Double predictedLocation = bot.myData.location;
        while ((deltaTime += 1.0) * Rules.getBulletSpeed((double)bulletPower) < enemyNextLocation.distance((Point2D)predictedLocation)) {
            predictedLocation = DRUtils.project(predictedLocation, myHeading, myVelocity);
            myHeading -= targetDeltaHeading;
            if (!Module.bf.contains((Point2D)predictedLocation)) break;
        }
        double theta = DRUtils.absoluteBearing(enemyNextLocation, predictedLocation);
        return theta;
    }

    public static Point2D.Double predictPosition(BulletInfoEnemy surfWave, double direction, Module bot) {
        Point2D.Double predictedPosition = bot.myData.location;
        double predictedVelocity = bot.getVelocity();
        double predictedHeading = bot.getHeadingRadians();
        int counter = 0;
        boolean intercepted = false;
        do {
            double moveAngle = BulletInfoEnemy.wallSmoothing(predictedPosition, DRUtils.absoluteBearing(surfWave.fireLocation, predictedPosition) + direction * 1.5707963267948966, direction) - predictedHeading;
            double moveDir = 1.0;
            if (Math.cos((double)moveAngle) < 0.0) {
                moveAngle += 3.141592653589793;
                moveDir = -1.0;
            }
            moveAngle = Utils.normalRelativeAngle((double)moveAngle);
            double maxTurning = 0.004363323129985824 * (40.0 - 3.0 * Math.abs((double)predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle((double)(predictedHeading + DRUtils.limit(- maxTurning, moveAngle, maxTurning)));
            predictedVelocity += predictedVelocity * moveDir < 0.0 ? 2.0 * moveDir : moveDir;
            if ((predictedPosition = DRUtils.project(predictedPosition, predictedHeading, predictedVelocity = DRUtils.limit(-8.0, predictedVelocity, 8.0))).distance((Point2D)surfWave.fireLocation) >= surfWave.distanceTraveled + (double)(++counter) * surfWave.velocity + surfWave.velocity) continue;
            intercepted = true;
        } while (!intercepted && counter < 500);
        return predictedPosition;
    }

    public static double wallSmoothing(Point2D.Double botLocation, double angle, double orientation) {
        while (!Module.bf.contains((Point2D)DRUtils.project(botLocation, angle, 80.0))) {
            angle += orientation * 0.05;
        }
        return angle;
    }
}


/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.io.Serializable
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 *  robocode.ScannedRobotEvent
 *  robocode.util.Utils
 */
package justin;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import justin.HistoryLog;
import justin.Module;
import justin.utils.KdTree;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Enemy
implements Serializable {
    public boolean alive = false;
    public String name = null;
    public double scanTime;
    public double round;
    public double deltaScanTime = 1.0;
    public Point2D.Double location;
    public double energy;
    public double correctedHeadingRadians;
    public double deltaHeadingRadians = 0.0;
    public double headingRadians;
    public double deltaEnergy;
    public double velocity;
    public double previousVelocity;
    public double accel;
    public double direction;
    public double distance;
    public double deltaDistance;
    public double previousBearingRadians;
    public double bearingRadians;
    public double absBearingRadians;
    public double previousAbsBearingRadians;
    public double deltaAbsBearingRadians;
    public String cbName;
    public double cbC = 0.0;
    public double cbD;
    public double tSDC;
    public double damageGiven = 0.0;
    public double damageRecieved = 0.0;
    public double timeAliveTogether = 0.0;
    public double hisThreatLevel;
    public double myThreatLevel;
    public double[] surfStats1vrs1;
    public double[][][][][] surfStats1vrs1Segmented;
    public double[] surfStatsMelee;
    public double[][][][][] surfStatsMeleeSegmented;
    public double TMheadOn = 0.0;
    public double TMlinear = 0.0;
    public double TMcircular = 0.0;
    public double TMguessFactor = 0.0;
    public double TMantiSurf = 0.0;
    public double bulletHits1v1 = 0.0;
    public double bulletShots1v1 = 0.0;
    public double bulletHitsMelee = 0.0;
    public double bulletShotsMelee = 0.0;
    public long timeLastBulletHit = 0L;
    public KdTree<HistoryLog> gunTree1vrs1;
    public KdTree<HistoryLog> gunTreeMelee;
    public HistoryLog last;

    public static Enemy update(Enemy newData, ScannedRobotEvent e, Module bot) {
        double dir;
        newData.alive = true;
        newData.name = e.getName();
        newData.deltaScanTime = (double)((int)e.getTime()) - newData.scanTime;
        newData.scanTime = (int)e.getTime();
        newData.deltaEnergy = newData.energy - e.getEnergy();
        newData.energy = e.getEnergy() + 0.001;
        newData.previousVelocity = newData.velocity;
        newData.velocity = e.getVelocity();
        newData.accel = 0.0;
        if (Math.abs((double)newData.velocity) > Math.abs((double)newData.previousVelocity)) {
            newData.accel = 1.0;
        } else if (Math.abs((double)newData.previousVelocity) < Math.abs((double)newData.velocity)) {
            newData.accel = -1.0;
        }
        double d = dir = newData.velocity != 0.0 ? Math.signum((double)newData.velocity) : newData.direction;
        newData.tSDC = newData.direction == dir && newData.deltaScanTime < 20.0 && newData.round == (double)bot.getRoundNum() ? (newData.tSDC += newData.deltaScanTime) : 0.0;
        newData.round = bot.getRoundNum();
        newData.direction = dir;
        newData.correctedHeadingRadians = newData.direction < 0.0 ? Utils.normalAbsoluteAngle((double)(e.getHeadingRadians() + 3.141592653589793)) : e.getHeadingRadians();
        newData.deltaHeadingRadians = Utils.normalRelativeAngle((double)(newData.headingRadians - e.getHeadingRadians()));
        newData.headingRadians = e.getHeadingRadians();
        newData.deltaDistance = newData.distance - e.getDistance();
        newData.distance = e.getDistance();
        newData.previousBearingRadians = newData.bearingRadians;
        newData.bearingRadians = e.getBearingRadians();
        newData.previousAbsBearingRadians = newData.absBearingRadians;
        newData.absBearingRadians = Utils.normalAbsoluteAngle((double)(bot.getHeadingRadians() + newData.bearingRadians));
        newData.deltaAbsBearingRadians = Utils.normalRelativeAngle((double)(newData.previousAbsBearingRadians - newData.absBearingRadians));
        double x = bot.getX() + newData.distance * Math.sin((double)newData.absBearingRadians);
        double y = bot.getY() + newData.distance * Math.cos((double)newData.absBearingRadians);
        newData.location = new Point2D.Double(x, y);
        Enemy cb = Enemy.getClosestBotTo(newData, bot);
        newData.cbName = cb.name;
        newData.cbC = Enemy.getCloserBotCount(newData, bot);
        newData.cbD = cb.location.distance((Point2D)newData.location);
        newData.timeAliveTogether += newData.deltaScanTime;
        newData.hisThreatLevel = (newData.damageGiven + 20.0) / newData.timeAliveTogether;
        newData.myThreatLevel = (newData.damageRecieved + 20.0) / newData.timeAliveTogether;
        HistoryLog dataLog = new HistoryLog();
        dataLog.scanTime = e.getTime();
        dataLog.round = bot.getRoundNum();
        dataLog.location = newData.location;
        dataLog.headingRadians = newData.correctedHeadingRadians;
        dataLog.absBearingRadians = newData.absBearingRadians;
        dataLog.distance = newData.distance;
        dataLog.velocity = newData.velocity;
        if (newData.last != null && e.getTime() - newData.last.scanTime > 1L & newData.last.round == bot.getRoundNum()) {
            Enemy.interpolateLogData(dataLog, newData.last, newData);
        }
        Enemy.updateHistoryLog(dataLog, newData);
        return newData;
    }

    public static void updateHistoryLog(HistoryLog dataLog, Enemy scanned) {
        if (scanned.last == null) {
            scanned.last = dataLog;
        } else {
            scanned.last.next = dataLog;
            dataLog.previous = scanned.last;
            scanned.last = dataLog;
        }
    }

    public static void interpolateLogData(HistoryLog n, HistoryLog o, Enemy newData) {
        double missingScans = n.scanTime - o.scanTime;
        double mu = 0.0;
        long t = 1L;
        while ((double)t < missingScans) {
            HistoryLog nd = new HistoryLog();
            nd.scanTime = o.scanTime + 1L + t;
            nd.round = o.round;
            double x = Enemy.LinearInterpolate(o.location.x, n.location.x, mu += 1.0 / missingScans);
            double y = Enemy.LinearInterpolate(o.location.y, n.location.y, mu);
            nd.location = new Point2D.Double(x, y);
            Enemy.updateHistoryLog(nd, newData);
            ++t;
        }
    }

    public static double LinearInterpolate(double y1, double y2, double mu) {
        return y1 * (1.0 - mu) + y2 * mu;
    }

    public static Enemy getClosestBotTo(Enemy scan, Module bot) {
        Iterator iterator = Module.enemies.values().iterator();
        double smallestDist = Double.POSITIVE_INFINITY;
        Enemy selected = new Enemy();
        selected.location = bot.myData.location;
        while (iterator.hasNext()) {
            double dist;
            Enemy e = (Enemy)iterator.next();
            if (!e.alive || (dist = e.location.distance((Point2D)scan.location)) >= smallestDist || e.name == scan.name) continue;
            selected = e;
            smallestDist = dist;
        }
        return selected;
    }

    public static int getCloserBotCount(Enemy scan, Module bot) {
        int cbC = 0;
        Iterator iterator = Module.enemies.values().iterator();
        Enemy selected = new Enemy();
        selected.location = bot.myData.location;
        while (iterator.hasNext()) {
            double myDist;
            double botDist;
            Enemy e = (Enemy)iterator.next();
            if (!e.alive || (myDist = bot.myData.location.distance((Point2D)scan.location)) <= (botDist = e.location.distance((Point2D)scan.location)) || e.name == scan.name) continue;
            ++cbC;
        }
        return cbC;
    }
}


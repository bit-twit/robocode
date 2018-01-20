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
 *  java.lang.ClassCastException
 *  java.lang.Comparable
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.ArrayList
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 *  java.util.List
 *  java.util.Vector
 *  robocode.Bullet
 *  robocode.Event
 *  robocode.Rules
 *  robocode.ScannedRobotEvent
 *  robocode.util.Utils
 */
package justin.targeting;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import justin.BulletInfoEnemy;
import justin.Enemy;
import justin.HistoryLog;
import justin.Module;
import justin.Targeting;
import justin.utils.DRUtils;
import justin.utils.KdTree;
import robocode.Bullet;
import robocode.Event;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class DCGun
extends Targeting {
    public static final int NumOfDirections = 0;
    public static final int DIMENSIONS = 9;
    public static final int MAX_TREE_SIZE = 30000;
    public int TOP_SCANS_SIZE = Math.max((int)40, (int)(50 - Module.skippedTurns));
    public int TOP_ANGLES_SIZE = Math.max((int)40, (int)(50 - Module.skippedTurns));
    public static Point2D.Double myNextLocation;
    public static double bulletPower;
    public static boolean aiming;
    public double timeWasted = 0.0;
    private final double[] wieghtsMelee = new double[]{6.0, 4.0, 3.0, 1.0, 0.0, 0.0, 2.0, 1.0, 1.0, 0.1, 0.1, 0.1};
    private final double[] wieghts1vrs1 = new double[]{4.0, 4.0, 2.0, 0.0, 2.0, 1.0, 2.0, 1.0, 1.0, 0.0, 0.0, 0.0};

    static {
        bulletPower = 0.0;
        aiming = false;
    }

    public DCGun(Module bot) {
        super(bot);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void listen(Event E) {
        if (E instanceof ScannedRobotEvent) {
            ScannedRobotEvent e = (ScannedRobotEvent)E;
            Enemy scanned = (Enemy)Module.enemies.get((Object)e.getName());
            if (scanned == null) {
                return;
            }
            if (Module.melee) {
                scanned.gunTreeMelee.addPoint(this.getTreeLocation(scanned), scanned.last);
            } else {
                scanned.gunTree1vrs1.addPoint(this.getTreeLocation(scanned), scanned.last);
            }
        }
    }

    @Override
    public void target() {
        myNextLocation = DRUtils.nextLocation(this.bot);
        bulletPower = this.bulletPower();
        if (this.bot.getGunHeat() > this.bot.getGunCoolingRate() * 4.0 || this.bot.getEnergy() < bulletPower || bulletPower == 0.0) {
            aiming = false;
        } else if (aiming && this.bot.getGunTurnRemaining() == 0.0 && this.bot.getGunHeat() == 0.0) {
            Bullet bullet = this.bot.setFireBullet(bulletPower);
            this.bot.registerMyBullet(bullet);
            aiming = false;
        } else if (!aiming) {
            double fireAngle = this.findBestFireAngle();
            if (fireAngle != 10000.0) {
                this.bot.setTurnGunRightRadians(Utils.normalRelativeAngle((double)(fireAngle - this.bot.getGunHeadingRadians())));
                if (this.bot.getGunHeat() < this.bot.getGunCoolingRate()) {
                    aiming = true;
                }
            } else {
                this.bot.setTurnGunRightRadians(Utils.normalRelativeAngle((double)(this.bot.enemy.absBearingRadians - this.bot.getGunHeadingRadians())));
                if (this.bot.getGunHeat() < this.bot.getGunCoolingRate()) {
                    aiming = true;
                }
            }
        }
    }

    public double bulletPower() {
        Enemy e = Enemy.getClosestBotTo(this.bot.myData, this.bot);
        bulletPower = 3.0;
        bulletPower = (this.bot.getEnergy() > 80.0 || this.bot.getOthers() > 7) && this.bot.getRoundNum() > 1 ? Math.min((double)bulletPower, (double)(1200.0 / e.distance)) : Math.min((double)bulletPower, (double)(900.0 / e.distance));
        if ((DCGun.bulletPower = Math.min((double)bulletPower, (double)((e.energy + 0.1) / 4.0))) * 6.0 >= this.bot.getEnergy()) {
            bulletPower = this.bot.getEnergy() / 5.0;
        }
        bulletPower = DRUtils.limit(0.1, bulletPower, 3.0);
        if (this.bot.getEnergy() - bulletPower <= 0.3 && this.bot.enemyBullets.size() > 0) {
            bulletPower = 0.0;
        }
        if (this.bot.getEnergy() - bulletPower < 0.1) {
            bulletPower = 0.0;
        }
        return bulletPower;
    }

    public double findBestFireAngle() {
        ArrayList allAngles = new ArrayList();
        for (Enemy e : Module.enemies.values()) {
            if (!e.alive || e.location == null || e.last == null) continue;
            List<KdTree.Entry<HistoryLog>> list = Module.melee ? e.gunTreeMelee.nearestNeighbor(this.getTreeLocation(e), Math.min((int)((int)Math.ceil((double)Math.sqrt((double)e.gunTreeMelee.size()))), (int)this.TOP_SCANS_SIZE), false) : e.gunTree1vrs1.nearestNeighbor(this.getTreeLocation(e), Math.min((int)((int)Math.ceil((double)Math.sqrt((double)e.gunTree1vrs1.size()))), (int)this.TOP_SCANS_SIZE), false);
            allAngles.addAll(this.getAngles(list, e, this.calculateWeight(e)));
        }
        if (allAngles.size() < 1) {
            return 10000.0;
        }
        double maxDiff = 0.0;
        int IDdiff = 0;
        int j = 0;
        while (j < allAngles.size()) {
            Angle aj = (Angle)allAngles.get(j);
            int c = 0;
            while (c < allAngles.size()) {
                Angle ac = (Angle)allAngles.get(c);
                if (Math.abs((double)(aj.ang - ac.ang)) < ac.tolerence) {
                    aj.diff += ac.wieght;
                }
                if (Math.abs((double)(ac.ang - aj.ang)) < aj.tolerence) {
                    ac.diff += aj.wieght;
                }
                ++c;
            }
            if (aj.diff > maxDiff) {
                maxDiff = aj.diff;
                IDdiff = j;
            }
            ++j;
        }
        Angle IDX = (Angle)allAngles.get(IDdiff);
        double bestAngle = IDX.ang;
        if (bestAngle >= 1000.0) {
            bestAngle = 10000.0;
        }
        return bestAngle;
    }

    public ArrayList<Angle> getAngles(List<KdTree.Entry<HistoryLog>> list, Enemy e, double weight) {
        Angle ang = null;
        if (list == null) {
            return null;
        }
        ArrayList topAngles = new ArrayList();
        double bulletSpeed = Rules.getBulletSpeed((double)bulletPower);
        long time = this.bot.getTime();
        int i = 0;
        while (i < list.size() && topAngles.size() < this.TOP_ANGLES_SIZE) {
            ang = this.getGunAngle((HistoryLog)((KdTree.Entry)list.get((int)i)).value, e, bulletSpeed, time, weight);
            if (ang != null) {
                topAngles.add((Object)ang);
            }
            ++i;
        }
        return topAngles;
    }

    public Angle getGunAngle(HistoryLog similar, Enemy e, double bulletSpeed, long time, double weight) {
        HistoryLog similarInfo = similar;
        HistoryLog currInfo = e.last;
        HistoryLog endInfo = similarInfo;
        long timeDelta = time - currInfo.scanTime;
        double predDist = 0.0;
        Point2D.Double myRelativePosition = DRUtils.project(similarInfo.location, Utils.normalRelativeAngle((double)(currInfo.absBearingRadians + 3.141592653589793 - currInfo.headingRadians + similarInfo.headingRadians)), currInfo.distance);
        while (endInfo.next != null && endInfo.round == similarInfo.round && endInfo.scanTime >= similarInfo.scanTime) {
            endInfo = endInfo.next;
            double bulletTime = myRelativePosition.distance((Point2D)endInfo.location) / bulletSpeed + 1.0;
            if (Math.abs((double)((double)(endInfo.scanTime - similarInfo.scanTime - timeDelta) - bulletTime)) <= 1.0) break;
        }
        if (endInfo.next == null | endInfo.round != similarInfo.round) {
            return null;
        }
        double predAng = Utils.normalRelativeAngle((double)(DRUtils.absoluteBearing(similarInfo.location, endInfo.location) - similarInfo.headingRadians));
        predDist = similarInfo.location.distance((Point2D)endInfo.location);
        Point2D.Double predLocation = DRUtils.project(currInfo.location, Utils.normalRelativeAngle((double)(predAng + currInfo.headingRadians)), predDist);
        if (!Module.bf.contains((Point2D)predLocation)) {
            return null;
        }
        predAng = DRUtils.absoluteBearing(this.bot.myData.location, predLocation);
        predDist = this.bot.myData.location.distance((Point2D)predLocation);
        return new Angle(predAng, Math.atan((double)(18.0 / predDist)), 0.0, weight);
    }

    public double calculateWeight(Enemy e) {
        double shootingMe = this.bot.myData.cbD < 330.0 && e.name == this.bot.myData.cbName && e.cbName == this.bot.myData.name ? 4 : 1;
        return (0.15 + DCGun.sqr(1.0 - Math.min((double)e.distance, (double)1200.0) / 1200.0)) * shootingMe;
    }

    public double[] getTreeLocation(Enemy e) {
        double[] p = new double[9];
        p[0] = Math.abs((double)e.velocity) / 8.0;
        p[1] = DRUtils.getWallDist(e.location, e.correctedHeadingRadians, Module.bw, Module.bh);
        p[2] = DRUtils.getWallDist(e.location, Utils.normalAbsoluteAngle((double)(e.correctedHeadingRadians + 3.141592653589793)), Module.bw, Module.bh);
        p[3] = Math.min((double)e.distance, (double)1200.0) / 1200.0;
        p[4] = 0.5;
        if (Math.abs((double)e.velocity) > Math.abs((double)e.previousVelocity)) {
            p[4] = 1.0;
        } else if (Math.abs((double)e.previousVelocity) < Math.abs((double)e.velocity)) {
            p[4] = 0.0;
        }
        p[5] = Math.abs((double)Utils.normalRelativeAngle((double)(e.headingRadians - this.bot.myData.headingRadians - e.bearingRadians))) / 3.141592653589793;
        p[6] = Math.min((double)e.tSDC, (double)120.0) / 120.0;
        double distance10 = 0.0;
        HistoryLog history = e.last;
        while (history.previous != null && distance10 < 10.0) {
            history = history.previous;
            distance10 += 1.0;
        }
        p[7] = Math.min((double)history.distance, (double)1200.0) / 1200.0;
        while (history.previous != null && distance10 < 10.0) {
            history = history.previous;
            distance10 += 1.0;
        }
        p[8] = Math.min((double)history.distance, (double)1200.0) / 1200.0;
        double[] wieghts = Module.melee ? this.wieghtsMelee : this.wieghts1vrs1;
        int i = 0;
        while (i < 9) {
            p[i] = p[i] * wieghts[i];
            ++i;
        }
        return p;
    }

    public static double distanceTo(double x1, double y1, double x2, double y2) {
        return Math.sqrt((double)(DCGun.sqr(x2 - x1) + DCGun.sqr(y2 - y1)));
    }

    private static final double sqr(double x) {
        return x * x;
    }

    protected double[] generateRiskFor(Enemy e) {
        double[] pointRisks = new double[]{};
        double sliceSize = Double.POSITIVE_INFINITY;
        int i = 0;
        while (i < 0) {
            double angle = e.correctedHeadingRadians + sliceSize / 2.0 + (double)i * sliceSize;
            Point2D.Double position = DRUtils.project(e.location, angle, 16.0);
            pointRisks[i] = this.getRiskFor(e, position, angle);
            ++i;
        }
        return pointRisks;
    }

    protected double getRiskFor(Enemy thisBot, Point2D.Double pt, double goAngle) {
        double test = 0.0;
        Iterator iterator = Module.enemies.values().iterator();
        while (iterator.hasNext()) {
            Enemy e = (Enemy)iterator.next();
            if (e.name == thisBot.name) {
                e = this.bot.myData;
            }
            if (!e.alive) continue;
            double angle = 1.0 + Math.abs((double)Math.cos((double)(DRUtils.absoluteBearing(thisBot.location, e.location) - goAngle)));
            double health = DRUtils.limit(0.5, e.energy / thisBot.energy, 2.0);
            double distanceSq = pt.distanceSq((Point2D)e.location);
            test += 100000.0 * (angle * health / distanceSq);
        }
        return test;
    }

    public int getClosestBotCountFrom(Point2D.Double testLoc, Enemy fromBot) {
        int cbC = 0;
        Iterator iterator = Module.enemies.values().iterator();
        Iterator iterator2 = Module.enemies.values().iterator();
        while (iterator.hasNext()) {
            Enemy e = (Enemy)iterator.next();
            if (e.name == fromBot.name) {
                e = this.bot.myData;
            }
            if (!e.alive) continue;
            while (iterator2.hasNext()) {
                Enemy e2 = (Enemy)iterator2.next();
                if (testLoc.distance((Point2D)e.location) >= e.location.distance((Point2D)e2.location)) continue;
                ++cbC;
            }
        }
        return cbC;
    }

    @Override
    public void onPaint(Graphics2D g) {
        if (this.bot.getGunHeat() == 0.0) {
            this.timeWasted += 1.0;
        }
    }

    public void drawRisks(Enemy e, double[] risks) {
        if (Module.paintTargeting) {
            double lowestRisk = Double.POSITIVE_INFINITY;
            double highestRisk = Double.NEGATIVE_INFINITY;
            int i = 0;
            while (i < risks.length) {
                if (risks[i] < lowestRisk) {
                    lowestRisk = risks[i];
                }
                if (risks[i] > highestRisk) {
                    highestRisk = risks[i];
                }
                ++i;
            }
            double avg = DCGun.average(risks);
            double stDev = DCGun.standardDeviation(risks);
            Graphics2D g = this.bot.getGraphics();
            int i2 = 0;
            while (i2 < risks.length) {
                Color color = DCGun.riskColor(risks[i2] - lowestRisk, avg - lowestRisk, stDev, false, 2.0);
                g.setColor(color);
                double sliceSize = 6.283185307179586 / (double)risks.length;
                double angle = e.correctedHeadingRadians + sliceSize / 2.0 + (double)i2 * sliceSize;
                Point2D.Double displayC = DRUtils.project(e.location, angle, 36.0);
                g.fillOval((int)displayC.x - 6, (int)displayC.y - 6, 12, 12);
                ++i2;
            }
        }
    }

    public static Color riskColor(double risk, double avg, double stDev, boolean safestYellow, double maxStDev) {
        if (risk < 1.0E-7 && safestYellow) {
            return Color.yellow;
        }
        return new Color((int)DRUtils.limit(0.0, 255.0 * (risk - (avg - maxStDev * stDev)) / (2.0 * maxStDev * stDev), 255.0), 0, (int)DRUtils.limit(0.0, 255.0 * (avg + maxStDev * stDev - risk) / (2.0 * maxStDev * stDev), 255.0));
    }

    public static double standardDeviation(double[] values) {
        double avg = DCGun.average(values);
        double sumSquares = 0.0;
        int x = 0;
        while (x < values.length) {
            sumSquares += DCGun.sqr(avg - values[x]);
            ++x;
        }
        return Math.sqrt((double)(sumSquares / (double)values.length));
    }

    public static double average(double[] values) {
        double sum = 0.0;
        int x = 0;
        while (x < values.length) {
            sum += values[x];
            ++x;
        }
        return sum / (double)values.length;
    }

    private class Angle {
        double ang;
        double tolerence;
        double diff;
        double wieght;

        public Angle(double a, double t, double d, double w) {
            this.diff = 1.0;
            this.ang = a;
            this.tolerence = t;
            this.diff = d;
            this.wieght = w;
        }
    }

    public class TopScan
    implements Comparable<TopScan> {
        HistoryLog info;
        double distanceScore;
        Enemy enemy;

        public TopScan(HistoryLog gd, double dS, Enemy e) {
            this.distanceScore = Double.POSITIVE_INFINITY;
            this.info = gd;
            this.distanceScore = dS;
            this.enemy = e;
        }

        public int compareTo(TopScan otherScan) throws ClassCastException {
            if (!(otherScan instanceof TopScan)) {
                throw new ClassCastException("A Topscan object expected.");
            }
            int BEFORE = -1;
            boolean EQUAL = false;
            boolean AFTER = true;
            if (this.distanceScore < otherScan.distanceScore) {
                return -1;
            }
            if (this.distanceScore > otherScan.distanceScore) {
                return 1;
            }
            return 0;
        }
    }

}


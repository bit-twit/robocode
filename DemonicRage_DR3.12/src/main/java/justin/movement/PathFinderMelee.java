/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.awt.Graphics2D
 *  java.awt.Polygon
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.ArrayList
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 *  java.util.Vector
 *  robocode.AdvancedRobot
 *  robocode.Rules
 */
package justin.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import justin.BulletInfoEnemy;
import justin.Enemy;
import justin.Module;
import justin.Movement;
import justin.movement.Destination;
import justin.utils.DRUtils;
import justin.utils.FastTrig;
import justin.utils.MovSim;
import justin.utils.MovSimStat;
import robocode.AdvancedRobot;
import robocode.Rules;

public class PathFinderMelee
extends Movement {
    public static final int SIM_LENGTH = 30;
    public static final double WAVE_SURFING_WEIGHT = 1.3;
    public static final double MIN_RISK_WEIGHT = 10000.0;
    public static final double REPEL_WEIGHT = 0.0;
    public static final double HBW = 20.0;
    public static Point2D.Double bfCenter;
    public static Polygon diamond;
    public Point2D.Double myLocation;
    public Destination currentDestination;
    public static MovSim sim;
    public MovSimStat[] testSim;
    public MovSimStat[] saveSimResult;
    public double minValue = 0.1;
    public double maxValue = 1.0;
    public Polygon poly;
    Point2D.Double closestCorner;
    public boolean switchOn = true;
    public double switchDir = 1.0;
    public double addRepelPoint = 1.0;
    public int timer = 1;

    public PathFinderMelee(Module bot) {
        super(bot);
    }

    @Override
    public void initialize() {
        bfCenter = new Point2D.Double(Module.bw / 2.0, Module.bh / 2.0);
        diamond = new Polygon();
        diamond.addPoint((int)Module.bw / 2, (int)Module.bh);
        diamond.addPoint((int)Module.bw, (int)Module.bh / 2);
        diamond.addPoint((int)Module.bw / 2, 0);
        diamond.addPoint(0, (int)Module.bh / 2);
        this.closestCorner = new Point2D.Double(0.0, 0.0);
        this.currentDestination = new Destination(this.myLocation, Double.POSITIVE_INFINITY, 0.0);
        this.timer = 1;
        sim = new MovSim();
    }

    @Override
    public void move() {
        Destination nextDestination;
        this.myLocation = new Point2D.Double(this.bot.getX(), this.bot.getY());
        if (!(Module.melee || this.bot.enemyBullets.size() >= 1 || this.bot.enemy.name == null || this.bot.myData.energy > 2.0 && Rules.getBulletDamage((double)this.bot.enemy.energy) >= this.bot.myData.energy * 2.0)) {
            DRUtils.driveTo(this.bot.enemy.location, this.bot);
            return;
        }
        if (Math.random() < 0.06) {
            this.switchDir *= -1.0;
            this.switchOn = !this.switchOn;
        }
        if (Module.enemies.size() == 0) {
            return;
        }
        ArrayList testLocations = new ArrayList();
        testLocations.addAll(this.generateLocations());
        this.setMinMaxValues();
        this.currentDestination = nextDestination = this.getLeastRisk(testLocations);
        double goAngle = DRUtils.absoluteBearing(this.bot.myData.location, nextDestination.location);
        DRUtils.driveTo(nextDestination.location, this.bot);
    }

    public void setClosestCorner() {
        if (this.bot.myData.location.distance(0.0, 0.0) < this.bot.myData.location.distance((Point2D)this.closestCorner)) {
            this.closestCorner = new Point2D.Double(0.0, 0.0);
        }
        if (this.bot.myData.location.distance(0.0, Module.bh) < this.bot.myData.location.distance((Point2D)this.closestCorner)) {
            this.closestCorner = new Point2D.Double(0.0, Module.bh);
        }
        if (this.bot.myData.location.distance(Module.bw, 0.0) < this.bot.myData.location.distance((Point2D)this.closestCorner)) {
            this.closestCorner = new Point2D.Double(Module.bw, 0.0);
        }
        if (this.bot.myData.location.distance(Module.bw, Module.bh) < this.bot.myData.location.distance((Point2D)this.closestCorner)) {
            this.closestCorner = new Point2D.Double(Module.bw, Module.bh);
        }
    }

    protected ArrayList<Destination> generateLocations() {
        ArrayList destinations = new ArrayList();
        int NumOfDirections = Math.max((int)23, (int)(31 - Module.skippedTurns));
        double sliceSize = 6.283185307179586 / (double)NumOfDirections;
        int i = 0;
        while (i < NumOfDirections) {
            double testDist = Math.min((double)(this.bot.myData.cbD * 0.9), (double)(90.0 + 125.0 * Math.random()));
            double angle = (double)i * sliceSize;
            Point2D.Double testLoc = FastTrig.project(this.bot.myData.location, angle, testDist);
            Destination test = this.testDestination(testLoc, angle);
            if (Module.bf.contains((Point2D)testLoc)) {
                destinations.add((Object)test);
            }
            ++i;
        }
        return destinations;
    }

    protected Destination getLeastRisk(ArrayList<Destination> loc) {
        Destination leastRisk = this.currentDestination;
        double lowestRisk = Double.POSITIVE_INFINITY;
        for (Destination d : loc) {
            if (d.risk >= lowestRisk) continue;
            lowestRisk = d.risk;
            leastRisk = d;
        }
        return leastRisk;
    }

    protected Destination testDestination(Point2D.Double destination, double goAngle) {
        Destination test = this.simulate(30, destination, goAngle);
        boolean onlyOnce = false;
        for (Enemy e : Module.enemies.values()) {
            if (!e.alive) continue;
            double distanceSq = destination.distanceSq((Point2D)e.location);
            double angle = 1.0 + Math.abs((double)FastTrig.cos(e.absBearingRadians - goAngle));
            double health = DRUtils.limit(0.6, e.energy / this.bot.myData.energy, 1.8);
            double threatLevel = DRUtils.limit(0.7, e.myThreatLevel / e.hisThreatLevel - 0.2, 1.8);
            double diamondSpace = 1.0;
            if (Module.melee && this.switchOn) {
                diamondSpace = diamond.contains((Point2D)destination) ? (this.bot.getTime() < 35L ? 1.7 : 1.07) : 1.0;
            }
            double protectCorner = 1.0;
            if (this.bot.myData.location.distance((Point2D)this.closestCorner) * 1.4 > e.location.distance((Point2D)this.closestCorner) && !onlyOnce) {
                onlyOnce = true;
                protectCorner = DRUtils.limit(0.85, this.bot.myData.location.distance((Point2D)this.closestCorner) / destination.distance((Point2D)this.closestCorner), 1.3);
            }
            test.risk += 10000.0 * angle * health / (distanceSq * protectCorner * (double)(this.getClosestBotCountFrom(destination, e) + 1) * threatLevel / diamondSpace);
        }
        return test;
    }

    public int getClosestBotCountFrom(Point2D.Double testLoc, Enemy scan) {
        int cbC = 0;
        for (Enemy e : Module.enemies.values()) {
            if (!e.alive || testLoc.distance((Point2D)scan.location) <= e.location.distance((Point2D)scan.location) || e.name == scan.name) continue;
            ++cbC;
        }
        return cbC;
    }

    public Destination simulate(int testTurns, Point2D.Double estimateLocation, double testHeading) {
        Destination test = new Destination(estimateLocation, 0.0, testHeading);
        ArrayList waveHitBuffer = new ArrayList();
        int waveHitCounter = 0;
        double damage = 0.0;
        Point2D.Double simLocation = bfCenter;
        DRUtils.driveTo(estimateLocation, this.bot);
        this.testSim = sim.futurePos(testTurns, this.bot, 8.0, 10.0);
        int i = 0;
        while (i < this.testSim.length) {
            if (waveHitCounter > 2) break;
            simLocation = new Point2D.Double(this.testSim[i].x, this.testSim[i].y);
            if (this.testSim[i].x < 20.0 || this.testSim[i].y < 20.0 || this.testSim[i].x > Module.bw - 20.0 || this.testSim[i].y > Module.bh - 20.0) {
                damage = Double.MAX_VALUE;
            }
            int xx = 0;
            while (xx < this.bot.enemyBullets.size()) {
                if (waveHitCounter > 2) break;
                BulletInfoEnemy bullet = (BulletInfoEnemy)this.bot.enemyBullets.get(xx);
                double distanceTraveled = bullet.distanceTraveled + (double)i * bullet.velocity;
                if (bullet.surf && !waveHitBuffer.contains((Object)xx) && Math.abs((double)(distanceTraveled - simLocation.distance((Point2D)bullet.fireLocation))) < 18.0) {
                    waveHitBuffer.add((Object)xx);
                    double bulletWeight = Module.melee ? 1.0 : 1.0 / Math.pow((double)(++waveHitCounter), (double)1.2);
                    Point2D.Double left = FastTrig.project(simLocation, FastTrig.absoluteBearing(bullet.fireLocation, simLocation) - 1.5707963267948966, 18.0);
                    Point2D.Double right = FastTrig.project(simLocation, FastTrig.absoluteBearing(bullet.fireLocation, simLocation) + 1.5707963267948966, 18.0);
                    int indexL = BulletInfoEnemy.getBinIndex(bullet, left);
                    int indexM = BulletInfoEnemy.getBinIndex(bullet, simLocation);
                    int indexR = BulletInfoEnemy.getBinIndex(bullet, right);
                    damage += (bullet.buffer[indexL] - this.minValue) / (this.maxValue - this.minValue) * 1.3 * bulletWeight;
                    damage += (bullet.buffer[indexM] - this.minValue) / (this.maxValue - this.minValue) * 1.3 * bulletWeight;
                    damage += (bullet.buffer[indexR] - this.minValue) / (this.maxValue - this.minValue) * 1.3 * bulletWeight;
                    damage /= 3.0;
                }
                ++xx;
            }
            ++i;
        }
        test.risk = damage /= (double)Math.max((int)1, (int)waveHitCounter);
        return test;
    }

    public void setMinMaxValues() {
        this.maxValue = Double.NEGATIVE_INFINITY;
        this.minValue = Double.POSITIVE_INFINITY;
        int x = 0;
        while (x < this.bot.enemyBullets.size()) {
            BulletInfoEnemy b = (BulletInfoEnemy)this.bot.enemyBullets.get(x);
            int j = 0;
            while (j < 41) {
                if (b.surf) {
                    if (b.buffer[j] < this.minValue) {
                        this.minValue = b.buffer[j];
                    }
                    if (b.buffer[j] > this.maxValue) {
                        this.maxValue = b.buffer[j];
                    }
                }
                ++j;
            }
            ++x;
        }
        if (this.maxValue <= 0.0) {
            this.maxValue = 1.0;
        }
    }

    public void reduceVelocityForTurnRateDegrees(double degreesPerTurn) {
        this.bot.setMaxVelocity((10.0 - degreesPerTurn) / 0.75);
    }

    @Override
    public void onPaint(Graphics2D g) {
    }

    public void drawRisks(ArrayList<Destination> destinations) {
        double lowestRisk = Double.POSITIVE_INFINITY;
        double highestRisk = Double.NEGATIVE_INFINITY;
        Iterator destIterator = destinations.iterator();
        double[] risks = new double[destinations.size()];
        int x = 0;
        while (destIterator.hasNext()) {
            Destination d = (Destination)destIterator.next();
            risks[x++] = d.risk;
            if (d.risk < lowestRisk) {
                lowestRisk = d.risk;
            }
            if (d.risk <= highestRisk) continue;
            highestRisk = d.risk;
        }
        double avg = PathFinderMelee.average(risks);
        double stDev = PathFinderMelee.standardDeviation(risks);
        destIterator = destinations.iterator();
        Graphics2D g = this.bot.getGraphics();
        while (destIterator.hasNext()) {
            Destination d = (Destination)destIterator.next();
            g.fillOval((int)d.location.x - 1, (int)d.location.y - 1, 2, 2);
            Color color = PathFinderMelee.riskColor(d.risk - lowestRisk, avg - lowestRisk, stDev, false, 2.0);
            g.setColor(color);
            g.fillOval((int)d.location.x - 4, (int)d.location.y - 4, 8, 8);
        }
    }

    public static Color riskColor(double risk, double avg, double stDev, boolean safestYellow, double maxStDev) {
        if (Math.abs((double)stDev) - Math.abs((double)avg) < 1.0E-7) {
            return Color.blue;
        }
        if (risk < 1.0E-7 && safestYellow) {
            return Color.yellow;
        }
        return new Color((int)DRUtils.limit(0.0, 255.0 * (risk - (avg - maxStDev * stDev)) / (2.0 * maxStDev * stDev), 255.0), 0, (int)DRUtils.limit(0.0, 255.0 * (avg + maxStDev * stDev - risk) / (2.0 * maxStDev * stDev), 255.0));
    }

    public static double standardDeviation(double[] values) {
        double avg = PathFinderMelee.average(values);
        double sumSquares = 0.0;
        int x = 0;
        while (x < values.length) {
            sumSquares += DRUtils.square(avg - values[x]);
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
}


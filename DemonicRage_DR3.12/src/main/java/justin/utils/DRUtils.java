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
 *  java.util.List
 *  robocode.AdvancedRobot
 *  robocode.util.Utils
 */
package justin.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import justin.utils.DiaWave;
import justin.utils.FastTrig;
import justin.utils.MovSim;
import justin.utils.MovSimStat;
import justin.utils.RobotState;
import robocode.AdvancedRobot;
import robocode.util.Utils;

public class DRUtils {
    public static MovSim moveSimulator;
    public static final boolean IGNORE_WALLS = true;
    public static final boolean OBSERVE_WALL_HITS = false;

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return DRUtils.project(sourceLocation, Math.sin((double)angle), Math.cos((double)angle), length);
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double sinAngle, double cosAngle, double length) {
        return new Point2D.Double(sourceLocation.x + sinAngle * length, sourceLocation.y + cosAngle * length);
    }

    public static double absoluteBearing(Point2D.Double sourceLocation, Point2D.Double target) {
        return Math.atan2((double)(target.x - sourceLocation.x), (double)(target.y - sourceLocation.y));
    }

    public static int nonZeroSign(double d) {
        if (d < 0.0) {
            return -1;
        }
        return 1;
    }

    public static double square(double d) {
        return d * d;
    }

    public static double cube(double d) {
        return d * d * d;
    }

    public static double power(double d, int exp) {
        double r = 1.0;
        int x = 0;
        while (x < exp) {
            r *= d;
            ++x;
        }
        return r;
    }

    public static Color calculateNewColor(Color initial, Color end, double percentage) {
        int newR = DRUtils.calculateValue(initial.getRed(), end.getRed(), percentage);
        int newG = DRUtils.calculateValue(initial.getGreen(), end.getGreen(), percentage);
        int newB = DRUtils.calculateValue(initial.getBlue(), end.getBlue(), percentage);
        return new Color(newR, newG, newB);
    }

    public static int calculateValue(double initial, double end, double percent) {
        double abs = Math.abs((double)(initial - end));
        int multiply = initial - end > 0.0 ? -1 : 1;
        return (int)Math.round((double)(initial + (double)multiply * (1.0 - percent) * abs));
    }

    public static double[] normalise(double[] values) {
        double lowestRisk = Double.POSITIVE_INFINITY;
        double highestRisk = Double.NEGATIVE_INFINITY;
        int i = 0;
        while (i < values.length) {
            if (values[i] < lowestRisk) {
                lowestRisk = values[i];
            }
            if (values[i] > highestRisk) {
                highestRisk = values[i];
            }
            ++i;
        }
        double[] normalisedValues = new double[values.length];
        int i2 = 0;
        while (i2 < values.length) {
            normalisedValues[i2] = (values[i2] - lowestRisk) / (highestRisk - lowestRisk);
            ++i2;
        }
        return normalisedValues;
    }

    public static double limit(double min, double value, double max) {
        return Math.max((double)min, (double)Math.min((double)value, (double)max));
    }

    public static double botWidthAimAngle(double distance) {
        return Math.abs((double)(18.0 / distance));
    }

    public static int bulletTicksFromPower(double distance, double power) {
        return (int)Math.ceil((double)(distance / (20.0 - 3.0 * power)));
    }

    public static int bulletTicksFromSpeed(double distance, double speed) {
        return (int)Math.ceil((double)(distance / speed));
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngleRadians) {
        double angle = Utils.normalRelativeAngle((double)(goAngleRadians - robot.getHeadingRadians()));
        if (Math.abs((double)angle) > 1.5707963267948966) {
            if (angle < 0.0) {
                robot.setTurnRightRadians(3.141592653589793 + angle);
            } else {
                robot.setTurnLeftRadians(3.141592653589793 - angle);
            }
            robot.setBack(1000.0);
        } else {
            if (angle < 0.0) {
                robot.setTurnLeftRadians(-1.0 * angle);
            } else {
                robot.setTurnRightRadians(angle);
            }
            robot.setAhead(1000.0);
        }
    }

    public static void driveTo(Point2D.Double location, AdvancedRobot bot) {
        Point2D.Double myLoc = new Point2D.Double(bot.getX(), bot.getY());
        double distance = myLoc.distance((Point2D)location);
        double angle = DRUtils.absoluteBearing(myLoc, location) - bot.getHeadingRadians();
        double direction = 1.0;
        if (Math.cos((double)angle) < 0.0) {
            angle += 3.141592653589793;
            direction = -1.0;
        }
        bot.setAhead(distance * direction);
        angle = Utils.normalRelativeAngle((double)angle);
        bot.setTurnRightRadians(angle);
    }

    public static double rollingAvg(double value, double newEntry, double n, double weighting) {
        return (value * n + newEntry * weighting) / (n + weighting);
    }

    public static double rollingAverage(double previousValue, double newValue, double depth) {
        return (previousValue * depth + newValue) / (depth + 1.0);
    }

    public static double round(double d, int i) {
        long powerTen = 1L;
        int x = 0;
        while (x < i) {
            powerTen *= 10L;
            ++x;
        }
        return (double)Math.round((double)(d * (double)powerTen)) / (double)powerTen;
    }

    public static double getWallDist(Point2D.Double loc, double heading, double battleFieldWidth, double battleFieldHieght) {
        heading = Utils.normalAbsoluteAngle((double)heading);
        double maxWDist = 500.0;
        double distV = 0.0;
        double distH = 0.0;
        distV = heading == 1.5707963267948966 || heading == 4.71238898038469 ? Double.POSITIVE_INFINITY : (heading < 1.5707963267948966 || heading > 4.71238898038469 ? (battleFieldHieght - loc.y) / FastTrig.cos(heading) : loc.y / FastTrig.cos(heading - 3.141592653589793));
        distH = heading == 3.141592653589793 || heading == 0.0 ? Double.POSITIVE_INFINITY : (heading < 3.141592653589793 ? (battleFieldWidth - loc.x) / Math.cos((double)(heading - 1.5707963267948966)) : loc.x / Math.cos((double)(heading - 3.141592653589793 - 1.5707963267948966)));
        return 1.0 - Math.min((double)Math.min((double)distV, (double)distH), (double)maxWDist) / maxWDist;
    }

    public static void drawTracks(Point2D.Double location, double heading, Graphics2D g) {
        g.setColor(Color.gray.darker());
        Point2D.Double L = DRUtils.project(location, heading + 1.5707963267948966, 8.0);
        Point2D.Double LL = DRUtils.project(location, heading + 1.5707963267948966, 18.0);
        Point2D.Double R = DRUtils.project(location, heading - 1.5707963267948966, 8.0);
        Point2D.Double RR = DRUtils.project(location, heading - 1.5707963267948966, 18.0);
        g.drawLine((int)L.x, (int)L.y, (int)LL.x, (int)LL.y);
        g.drawLine((int)R.x, (int)R.y, (int)RR.x, (int)RR.y);
    }

    public static Point2D.Double nextLocation(AdvancedRobot robot) {
        if (moveSimulator == null) {
            moveSimulator = new MovSim();
        }
        MovSimStat[] next = moveSimulator.futurePos(1, robot);
        return new Point2D.Double(next[0].x, next[0].y);
    }

    public static Point2D.Double nextLocation(Point2D.Double botLocation, double velocity, double heading) {
        return new Point2D.Double(botLocation.x + Math.sin((double)heading) * velocity, botLocation.y + Math.cos((double)heading) * velocity);
    }

    public static RobotState nextLocation(Point2D.Double botLocation, double velocity, double maxVelocity, double headingRadians, double goAngleRadians, long currentTime, boolean isSmoothing, boolean ignoreWallHits, double battleFieldWidth, double battleFieldHeight) {
        double futureDistance;
        MovSim movSim = DRUtils.getMovSim();
        double futureTurn = Utils.normalRelativeAngle((double)(goAngleRadians - headingRadians));
        if (Math.abs((double)futureTurn) > 1.5707963267948966) {
            futureTurn = futureTurn < 0.0 ? (futureTurn += 3.141592653589793) : -1.0 * (3.141592653589793 - futureTurn);
            futureDistance = -1000.0;
        } else {
            futureDistance = 1000.0;
        }
        int extraWallSize = 0;
        if (ignoreWallHits) {
            extraWallSize = 50000;
        }
        MovSimStat[] futureMoves = movSim.futurePos(1, (double)extraWallSize + botLocation.x, (double)extraWallSize + botLocation.y, velocity, maxVelocity, headingRadians, futureDistance, futureTurn, 10.0, (double)(extraWallSize * 2) + battleFieldWidth, (double)(extraWallSize * 2) + battleFieldHeight);
        return new RobotState(new Point2D.Double(DRUtils.round(futureMoves[0].x - (double)extraWallSize, 4), DRUtils.round(futureMoves[0].y - (double)extraWallSize, 4)), futureMoves[0].h, futureMoves[0].v, currentTime + 1L, isSmoothing);
    }

    public static RobotState nextPerpendicularLocation(Point2D.Double targetLocation, double absBearingRadians, double enemyVelocity, double enemyHeadingRadians, boolean clockwise, long currentTime, boolean ignoreWallHits) {
        boolean purelyPerpendicularOffset = false;
        return DRUtils.nextPerpendicularLocation(targetLocation, absBearingRadians, enemyVelocity, enemyHeadingRadians, (double)purelyPerpendicularOffset, clockwise, currentTime, ignoreWallHits);
    }

    public static RobotState nextPerpendicularLocation(Point2D.Double targetLocation, double absBearingRadians, double enemyVelocity, double enemyHeadingRadians, double attackAngle, boolean clockwise, long currentTime, boolean ignoreWallHits) {
        return DRUtils.nextPerpendicularWallSmoothedLocation(targetLocation, absBearingRadians, enemyVelocity, 8.0, enemyHeadingRadians, attackAngle, clockwise, currentTime, null, 0.0, 0.0, 0.0, ignoreWallHits);
    }

    public static RobotState nextPerpendicularWallSmoothedLocation(Point2D.Double targetLocation, double absBearingRadians, double enemyVelocity, double maxVelocity, double enemyHeadingRadians, double attackAngle, boolean clockwise, long currentTime, Rectangle2D.Double battleField, double bfWidth, double bfHeight, double wallStick, boolean ignoreWallHits) {
        int orientation = clockwise ? 1 : -1;
        double goAngleRadians = Utils.normalRelativeAngle((double)(absBearingRadians + (double)orientation * (1.5707963267948966 + attackAngle)));
        boolean isSmoothing = false;
        if (wallStick != 0.0 && battleField != null) {
            double smoothedAngle = DRUtils.wallSmoothing(battleField, bfWidth, bfHeight, targetLocation, goAngleRadians, orientation, wallStick);
            if (DRUtils.round(smoothedAngle, 4) != DRUtils.round(goAngleRadians, 4)) {
                isSmoothing = true;
            }
            goAngleRadians = smoothedAngle;
        }
        return DRUtils.nextLocation(targetLocation, enemyVelocity, maxVelocity, enemyHeadingRadians, goAngleRadians, currentTime, isSmoothing, bfWidth, bfHeight, ignoreWallHits);
    }

    public static RobotState nextLocation(Point2D.Double targetLocation, double enemyVelocity, double maxVelocity, double enemyHeadingRadians, double goAngleRadians, long currentTime, boolean isSmoothing, double battleFieldWidth, double battleFieldHeight, boolean ignoreWallHits) {
        double futureDistance;
        MovSim movSim = DRUtils.getMovSim();
        double futureTurn = Utils.normalRelativeAngle((double)(goAngleRadians - enemyHeadingRadians));
        if (Math.abs((double)futureTurn) > 1.5707963267948966) {
            futureTurn = futureTurn < 0.0 ? (futureTurn += 3.141592653589793) : -1.0 * (3.141592653589793 - futureTurn);
            futureDistance = -1000.0;
        } else {
            futureDistance = 1000.0;
        }
        int extraWallSize = 0;
        if (ignoreWallHits) {
            extraWallSize = 50000;
        }
        MovSimStat[] futureMoves = movSim.futurePos(1, (double)extraWallSize + targetLocation.x, (double)extraWallSize + targetLocation.y, enemyVelocity, maxVelocity, enemyHeadingRadians, futureDistance, futureTurn, 10.0, (double)(extraWallSize * 2) + battleFieldWidth, (double)(extraWallSize * 2) + battleFieldHeight);
        return new RobotState(new Point2D.Double(futureMoves[0].x - (double)extraWallSize, futureMoves[0].y - (double)extraWallSize), futureMoves[0].h, futureMoves[0].v, currentTime + 1L, isSmoothing);
    }

    public static MovSim getMovSim() {
        if (moveSimulator == null) {
            moveSimulator = new MovSim();
        }
        return moveSimulator;
    }

    public static double orbitalWallDistance(Point2D.Double sourceLocation, Point2D.Double targetLocation, double bulletPower, int direction, Rectangle2D.Double fieldRect) {
        return DRUtils.orbitalWallDistance(sourceLocation, targetLocation, bulletPower, direction, fieldRect, 1.0);
    }

    public static double orbitalWallDistance(Point2D.Double sourceLocation, Point2D.Double targetLocation, double bulletPower, int direction, Rectangle2D.Double fieldRect, double fudge) {
        double absBearing = DRUtils.absoluteBearing(sourceLocation, targetLocation);
        double distance = sourceLocation.distance((Point2D)targetLocation) * fudge;
        double maxAngleRadians = FastTrig.asin(8.0 / (20.0 - 3.0 * bulletPower));
        double wallDistance = 2.0;
        int x = 0;
        while (x < 200) {
            if (!fieldRect.contains(sourceLocation.x + Math.sin((double)(absBearing + (double)direction * ((double)x / 100.0) * maxAngleRadians)) * distance, sourceLocation.y + Math.cos((double)(absBearing + (double)direction * ((double)x / 100.0) * maxAngleRadians)) * distance)) {
                wallDistance = (double)x / 100.0;
                break;
            }
            ++x;
        }
        return wallDistance;
    }

    public static double directToWallDistance(Point2D.Double targetLocation, double distance, double heading, double bulletPower, Rectangle2D.Double fieldRect) {
        int bulletTicks = DRUtils.bulletTicksFromPower(distance, bulletPower);
        double wallDistance = 2.0;
        double sinHeading = Math.sin((double)heading);
        double cosHeading = Math.cos((double)heading);
        int x = 0;
        while (x < 2 * bulletTicks) {
            if (!fieldRect.contains(targetLocation.x + sinHeading * 8.0 * (double)x, targetLocation.y + cosHeading * 8.0 * (double)x)) {
                wallDistance = (double)x / (double)bulletTicks;
                break;
            }
            ++x;
        }
        return wallDistance;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static double wallSmoothing(Rectangle2D.Double field, double bfWidth, double bfHeight, Point2D.Double startLocation, double startAngleRadians, int orientation, double wallStick) {
        angle = startAngleRadians;
        wallDistanceX = Math.min((double)(startLocation.x - 18.0), (double)(bfWidth - startLocation.x - 18.0));
        wallDistanceY = Math.min((double)(startLocation.y - 18.0), (double)(bfHeight - startLocation.y - 18.0));
        if (wallDistanceX > wallStick && wallDistanceY > wallStick) {
            return startAngleRadians;
        }
        testX = startLocation.x + Math.sin((double)angle) * wallStick;
        testY = startLocation.y + Math.cos((double)angle) * wallStick;
        testDistanceX = Math.min((double)(testX - 18.0), (double)(bfWidth - testX - 18.0));
        testDistanceY = Math.min((double)(testY - 18.0), (double)(bfHeight - testY - 18.0));
        adjacent = 0.0;
        g = 0;
        ** GOTO lbl27
        {
            angle += 6.283185307179586;
            do {
                if (angle < 0.0) continue block0;
                if (testDistanceY < 0.0 && testDistanceY < testDistanceX) {
                    angle = (double)((int)((angle + 1.5707963267948966) / 3.141592653589793)) * 3.141592653589793;
                    adjacent = Math.abs((double)wallDistanceY);
                } else if (testDistanceX < 0.0 && testDistanceX <= testDistanceY) {
                    angle = (double)((int)(angle / 3.141592653589793)) * 3.141592653589793 + 1.5707963267948966;
                    adjacent = Math.abs((double)wallDistanceX);
                }
                testX = startLocation.x + Math.sin((double)(angle += (double)orientation * (Math.abs((double)Math.acos((double)(adjacent / wallStick))) + 5.0E-4))) * wallStick;
                testY = startLocation.y + Math.cos((double)angle) * wallStick;
                testDistanceX = Math.min((double)(testX - 18.0), (double)(bfWidth - testX - 18.0));
                testDistanceY = Math.min((double)(testY - 18.0), (double)(bfHeight - testY - 18.0));
lbl27: // 2 sources:
                if (testDistanceX < 0.0) continue;
                if (testDistanceY >= 0.0) return angle;
            } while (g++ < 25);
        }
        return angle;
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList, Point2D.Double targetLocation, long currentTime, boolean onlySurfable, double matchDistanceThreshold) {
        return DRUtils.findClosestWave(waveList, targetLocation, currentTime, onlySurfable, false, matchDistanceThreshold, null);
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList, Point2D.Double targetLocation, long currentTime, boolean onlySurfable, boolean onlyFiring, double matchDistanceThreshold, String botName) {
        double closestDistance = Double.POSITIVE_INFINITY;
        DiaWave closestWave = null;
        for (DiaWave w : waveList) {
            double distanceFromTargetToWave;
            if (w.altWave || botName != null && !botName.equals((Object)w.botName) && !botName.equals((Object)"") || Math.abs((double)(distanceFromTargetToWave = w.sourceLocation.distance((Point2D)targetLocation) - w.distanceTraveled(currentTime))) >= matchDistanceThreshold || Math.abs((double)distanceFromTargetToWave) >= closestDistance || onlySurfable && distanceFromTargetToWave <= 0.0 || onlyFiring && !w.firingWave) continue;
            closestDistance = Math.abs((double)distanceFromTargetToWave);
            closestWave = w;
        }
        return closestWave;
    }

    public static double standardDeviation(double[] values) {
        double avg = DRUtils.average(values);
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

    public static double accel(double velocity, double previousVelocity) {
        double accel = velocity - previousVelocity;
        accel = previousVelocity == 0.0 ? Math.abs((double)accel) : (accel *= Math.signum((double)previousVelocity));
        return accel;
    }

    public static Point2D.Double translateToField(Point2D.Double p, double width, double height) {
        return new Point2D.Double(DRUtils.limit(18.0, p.x, width - 18.0), DRUtils.limit(18.0, p.y, height - 18.0));
    }

    public static double distanceSq(double[] p1, double[] p2, double[] weights) {
        double sum = 0.0;
        int x = 0;
        while (x < p1.length) {
            double z = (p1[x] - p2[x]) * weights[x];
            sum += z * z;
            ++x;
        }
        return sum;
    }

    public static double manhattanDistance(double[] p1, double[] p2, double[] weights) {
        double sum = 0.0;
        int x = 0;
        while (x < p1.length) {
            sum += Math.abs((double)(p1[x] - p2[x])) * weights[x];
            ++x;
        }
        return sum;
    }

    public static double findLongestDistance(double[][] points, double[] testPoint, double[] weights, boolean manhattan) {
        double longestDistance = 0.0;
        int x = 0;
        while (x < points.length) {
            double distance = manhattan ? DRUtils.manhattanDistance(points[x], testPoint, weights) : DRUtils.distanceSq(points[x], testPoint, weights);
            if (distance > longestDistance) {
                longestDistance = distance;
            }
            ++x;
        }
        return longestDistance;
    }

    public static double findAndReplaceLongestDistance(double[][] points, double[] nearestDistances, double[] newPoint, double newPointDistance) {
        double longestDistance = 0.0;
        double newLongestDistance = 0.0;
        int longestIndex = 0;
        int x = 0;
        while (x < points.length) {
            double distance = nearestDistances[x];
            if (distance > longestDistance) {
                newLongestDistance = longestDistance;
                longestDistance = distance;
                longestIndex = x;
            } else if (distance > newLongestDistance) {
                newLongestDistance = distance;
            }
            ++x;
        }
        points[longestIndex] = newPoint;
        nearestDistances[longestIndex] = newPointDistance;
        return Math.max((double)newLongestDistance, (double)newPointDistance);
    }

    public static double[][] nearestNeighbors(double[][] dataSet, double[] searchPoint, double[] weights, int numNeighbors, boolean manhattan) {
        if (dataSet.length <= numNeighbors) {
            return dataSet;
        }
        double[][] closestPoints = new double[numNeighbors][searchPoint.length];
        double[] nearestDistances = new double[numNeighbors];
        int y = 0;
        while (y < numNeighbors) {
            closestPoints[y] = dataSet[y];
            nearestDistances[y] = manhattan ? DRUtils.manhattanDistance(closestPoints[y], searchPoint, weights) : DRUtils.distanceSq(closestPoints[y], searchPoint, weights);
            ++y;
        }
        double closestDistanceThreshold = DRUtils.findLongestDistance(closestPoints, searchPoint, weights, manhattan);
        int y2 = numNeighbors;
        while (y2 < dataSet.length) {
            double[] point = dataSet[y2];
            double thisDistance = manhattan ? DRUtils.manhattanDistance(searchPoint, point, weights) : DRUtils.distanceSq(searchPoint, point, weights);
            if (thisDistance < closestDistanceThreshold) {
                closestDistanceThreshold = DRUtils.findAndReplaceLongestDistance(closestPoints, nearestDistances, point, thisDistance);
            }
            ++y2;
        }
        return closestPoints;
    }
}


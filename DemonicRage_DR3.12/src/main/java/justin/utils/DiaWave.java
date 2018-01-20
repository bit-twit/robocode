/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.io.PrintStream
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.lang.System
 *  robocode.util.Utils
 */
package justin.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import justin.utils.DRUtils;
import justin.utils.RobotState;
import robocode.util.Utils;

public class DiaWave {
    public static final Point2D.Double ORIGIN = new Point2D.Double(0.0, 0.0);
    public static final double MAX_GUESSFACTOR = 1.0;
    public static final int CLOCKWISE = 1;
    public static final int COUNTERCLOCKWISE = -1;
    public static final boolean FIRING_WAVE = true;
    public static final boolean SURFABLE_WAVE = true;
    public static final boolean ANY_WAVE = false;
    public static final boolean POSITIVE_GUESSFACTOR = true;
    public static final boolean NEGATIVE_GUESSFACTOR = false;
    public static final double NO_CACHED_ESCAPE_ANGLE = -1.0;
    public long fireTime;
    public Point2D.Double sourceLocation;
    public Point2D.Double targetLocation;
    public double absBearing;
    public double bulletPower;
    public double bulletSpeed;
    public int orbitDirection;
    public boolean processedBulletHit;
    public boolean processedPassed;
    public boolean processedWaveBreak;
    public boolean firingWave;
    public String botName;
    public double targetHeading;
    public double targetRelativeHeading;
    public double targetVelocity;
    public double targetAccel;
    public int targetVelocitySign;
    public double targetDistance;
    public double targetDchangeTime;
    public double targetVchangeTime;
    public double targetWallDistance;
    public double targetRevWallDistance;
    public double targetDl8t;
    public double targetDl20t;
    public double targetDl40t;
    public double targetAgHeading;
    public double targetAgForce;
    public double targetCornerDistance;
    public double targetCornerBearing;
    public double gunHeat;
    public int enemiesAlive;
    public Point2D.Double waveBreakLocation;
    public long waveBreakTime;
    public Point2D.Double prevLocation;
    public long prevLocationTime;
    public boolean altWave;
    protected Rectangle2D.Double _fieldRect;
    protected double _fieldWidth;
    protected double _fieldHeight;
    protected double _cachedPositiveEscapeAngle = -1.0;
    protected double _cachedNegativeEscapeAngle = -1.0;
    public boolean usedNegativeSmoothingMea = false;
    public boolean usedPositiveSmoothingMea = false;

    public DiaWave(Point2D.Double source, Point2D.Double target, Point2D.Double orbitLocation, long time, double power, String name, double heading, double velocity, double accel, int vSign, double distance, double dChange, double vChange, double wallDist1, double wallDist2, double dl8t, double dl20t, double dl40t, double agh, double agf, double cd, double cb, int alive, double gh, Rectangle2D.Double field, double width, double height) {
        this.sourceLocation = source;
        this.targetLocation = target;
        this.fireTime = time;
        this.setBulletPower(power);
        this.absBearing = DRUtils.absoluteBearing(source, target);
        this.botName = name;
        this.targetHeading = heading;
        this.targetVelocity = velocity;
        this.targetAccel = accel;
        this.targetVelocitySign = vSign;
        this.targetDistance = distance;
        this.targetDchangeTime = dChange;
        this.targetVchangeTime = vChange;
        this.targetDl8t = dl8t;
        this.targetDl20t = dl20t;
        this.targetDl40t = dl40t;
        this.targetAgForce = agf;
        this.targetCornerDistance = cd;
        this.enemiesAlive = alive;
        this.gunHeat = gh;
        double orbitRelativeHeading = Utils.normalRelativeAngle((double)(this.effectiveHeading() - DRUtils.absoluteBearing(orbitLocation, this.targetLocation)));
        this.orbitDirection = orbitRelativeHeading < 0.0 ? -1 : 1;
        this.targetRelativeHeading = Math.abs((double)orbitRelativeHeading);
        this.targetAgHeading = Utils.normalRelativeAngle((double)(agh - this.effectiveHeading())) * (double)this.orbitDirection;
        this.targetCornerBearing = Utils.normalRelativeAngle((double)(cb - this.effectiveHeading())) * (double)this.orbitDirection;
        this.setWallDistance(this.sourceLocation, wallDist1, wallDist2);
        this._fieldRect = field;
        this._fieldWidth = width;
        this._fieldHeight = height;
        this.processedBulletHit = false;
        this.processedPassed = false;
        this.processedWaveBreak = false;
        this.waveBreakLocation = null;
        this.prevLocation = null;
        this.firingWave = false;
        this.altWave = false;
    }

    public void setBulletPower(double power) {
        this.bulletPower = power;
        this.bulletSpeed = 20.0 - 3.0 * power;
    }

    public void setWallDistance(Point2D.Double orbitLocation, double wallDist1, double wallDist2) {
        if (this.orbitDirection == -1 && this.enemiesAlive == 1) {
            this.targetWallDistance = wallDist2;
            this.targetRevWallDistance = wallDist1;
        } else {
            this.targetWallDistance = wallDist1;
            this.targetRevWallDistance = wallDist2;
        }
    }

    public boolean wavePassedInterpolate(Point2D.Double lastScanLocation, long lastScanTime, long currentTime) {
        return this.wavePassedInterpolate(lastScanLocation, lastScanTime, currentTime, 0);
    }

    public boolean wavePassedInterpolate(Point2D.Double lastScanLocation, long lastScanTime, long currentTime, int offset) {
        if (this.processedPassed) {
            return true;
        }
        if (this.sourceLocation.distanceSq((Point2D)lastScanLocation) < DRUtils.square((double)offset + this.bulletSpeed * ((double)(currentTime - this.fireTime) + 1.5)) && lastScanTime == currentTime) {
            if (currentTime - this.prevLocationTime == 1L || this.prevLocation == null) {
                this.waveBreakLocation = lastScanLocation;
                this.waveBreakTime = currentTime;
            } else {
                double deltaDistance = lastScanLocation.distance((Point2D)this.prevLocation) / (double)(currentTime - this.prevLocationTime);
                double deltaBearing = DRUtils.absoluteBearing(this.prevLocation, lastScanLocation);
                double dbSin = Math.sin((double)deltaBearing);
                double dbCos = Math.cos((double)deltaBearing);
                long x = this.prevLocationTime + 1L;
                while (x <= currentTime) {
                    long interpoTime = x - this.prevLocationTime;
                    Point2D.Double interpoLocation = DRUtils.project(this.prevLocation, dbSin, dbCos, (double)interpoTime * deltaDistance);
                    if (this.sourceLocation.distanceSq((Point2D)interpoLocation) < DRUtils.square((double)offset + this.bulletSpeed * ((double)(x - this.fireTime) + 1.5))) {
                        this.waveBreakLocation = interpoLocation;
                        this.waveBreakTime = x;
                        break;
                    }
                    ++x;
                }
                if (this.waveBreakLocation == null) {
                    System.out.println("WARNING: Anomaly in wave break interpolation.");
                    this.waveBreakLocation = lastScanLocation;
                    this.waveBreakTime = lastScanTime;
                }
            }
            this.processedPassed = true;
            return true;
        }
        this.prevLocation = lastScanLocation;
        this.prevLocationTime = lastScanTime;
        return false;
    }

    public boolean wavePassed(Point2D.Double enemyLocation, long currentTime, double interceptOffset) {
        double threshold = this.bulletSpeed * (double)(currentTime - this.fireTime) + interceptOffset;
        if (threshold > 0.0 && enemyLocation.distanceSq((Point2D)this.sourceLocation) < DRUtils.square(threshold)) {
            return true;
        }
        return false;
    }

    public Point2D.Double waveBreakLocation() {
        if (this.waveBreakLocation == null) {
            return this.prevLocation;
        }
        return this.waveBreakLocation;
    }

    public long waveBreakTime() {
        if (this.waveBreakLocation == null) {
            return this.prevLocationTime;
        }
        return this.waveBreakTime;
    }

    public long waveBreakBulletTicks() {
        return this.waveBreakBulletTicks(this.waveBreakTime());
    }

    public long waveBreakBulletTicks(long waveBreakTime) {
        return waveBreakTime - this.fireTime;
    }

    public double effectiveHeading() {
        return Utils.normalAbsoluteAngle((double)(this.targetHeading + (this.targetVelocitySign == 1 ? 0.0 : 3.141592653589793)));
    }

    public Point2D.Double displacementVector() {
        return this.displacementVector(this.waveBreakLocation(), this.waveBreakTime());
    }

    public Point2D.Double displacementVector(Point2D.Double botLocation, long time) {
        double vectorBearing = Utils.normalRelativeAngle((double)(DRUtils.absoluteBearing(this.targetLocation, botLocation) - this.effectiveHeading()));
        double vectorDistance = this.targetLocation.distance((Point2D)botLocation) / (double)this.waveBreakBulletTicks(time);
        return DRUtils.project(ORIGIN, vectorBearing * (double)this.orbitDirection, vectorDistance);
    }

    public double firingAngleFromDisplacementVector(Point2D.Double dispVector) {
        return Utils.normalAbsoluteAngle((double)DRUtils.absoluteBearing(this.sourceLocation, this.projectLocation(dispVector)));
    }

    public double firingAngleFromTargetLocation(Point2D.Double firingTarget) {
        return Utils.normalAbsoluteAngle((double)DRUtils.absoluteBearing(this.sourceLocation, firingTarget));
    }

    public Point2D.Double projectLocationBlind(Point2D.Double myNextLocation, Point2D.Double dispVector, long currentTime) {
        return this.projectLocation(myNextLocation, dispVector, currentTime - this.fireTime + 1L);
    }

    public Point2D.Double projectLocation(Point2D.Double dispVector) {
        return this.projectLocation(this.sourceLocation, dispVector, 0L);
    }

    public Point2D.Double projectLocation(Point2D.Double firingLocation, Point2D.Double dispVector, long extraTicks) {
        Point2D.Double projectedLocation;
        double dispAngle = this.effectiveHeading() + DRUtils.absoluteBearing(ORIGIN, dispVector) * (double)this.orbitDirection;
        double dispDistance = ORIGIN.distance((Point2D)dispVector);
        long bulletTicks = DRUtils.bulletTicksFromSpeed(firingLocation.distance((Point2D)this.targetLocation), this.bulletSpeed) - 1;
        long prevBulletTicks = 0L;
        int sanityCounter = 0;
        double daSin = Math.sin((double)dispAngle);
        double daCos = Math.cos((double)dispAngle);
        do {
            projectedLocation = DRUtils.project(this.targetLocation, daSin, daCos, (double)(bulletTicks + extraTicks) * dispDistance);
            long prevPrevBulletTicks = prevBulletTicks;
            prevBulletTicks = bulletTicks;
            bulletTicks = DRUtils.bulletTicksFromSpeed(firingLocation.distance((Point2D)projectedLocation), this.bulletSpeed) - 1;
            if (bulletTicks != prevPrevBulletTicks) continue;
            projectedLocation = DRUtils.project(this.targetLocation, daSin, daCos, ((double)(bulletTicks + prevBulletTicks) / 2.0 + (double)extraTicks) * dispDistance);
            break;
        } while (bulletTicks != prevBulletTicks && sanityCounter++ < 20);
        return projectedLocation;
    }

    public double distanceTraveled(long currentTime) {
        return (double)(currentTime - this.fireTime) * this.bulletSpeed;
    }

    public double guessFactor(Point2D.Double targetLocation) {
        double newBearingToTarget = DRUtils.absoluteBearing(this.sourceLocation, targetLocation);
        double guessAngle = (double)this.orbitDirection * Utils.normalRelativeAngle((double)(newBearingToTarget - this.absBearing));
        double maxEscapeAngle = Math.asin((double)(8.0 / this.bulletSpeed));
        return guessAngle / maxEscapeAngle;
    }

    public double guessFactorPrecise(Point2D.Double targetLocation) {
        double newBearingToTarget = DRUtils.absoluteBearing(this.sourceLocation, targetLocation);
        double guessAngle = (double)this.orbitDirection * Utils.normalRelativeAngle((double)(newBearingToTarget - this.absBearing));
        double maxEscapeAngle = this.preciseEscapeAngle(guessAngle >= 0.0);
        return guessAngle / maxEscapeAngle;
    }

    public double lateralVelocity() {
        return Math.sin((double)this.targetRelativeHeading) * Math.abs((double)this.targetVelocity);
    }

    public double escapeAngleRange() {
        return this.preciseEscapeAngle(true) + this.preciseEscapeAngle(false);
    }

    public double preciseEscapeAngle(boolean positiveGuessFactor) {
        if (positiveGuessFactor) {
            if (this._cachedPositiveEscapeAngle == -1.0) {
                this._cachedPositiveEscapeAngle = this.uncachedPreciseEscapeAngle(positiveGuessFactor) * 1.0;
            }
            return this._cachedPositiveEscapeAngle;
        }
        if (this._cachedNegativeEscapeAngle == -1.0) {
            this._cachedNegativeEscapeAngle = this.uncachedPreciseEscapeAngle(positiveGuessFactor) * 1.0;
        }
        return this._cachedNegativeEscapeAngle;
    }

    public double firingAngle(double guessFactor) {
        return this.absBearing + guessFactor * (double)this.orbitDirection * Math.asin((double)(8.0 / this.bulletSpeed));
    }

    public double uncachedPreciseEscapeAngle(boolean positiveGuessFactor) {
        boolean hitWall = false;
        boolean wavePassed = false;
        RobotState predictedState = new RobotState((Point2D.Double)this.targetLocation.clone(), this.targetHeading, this.targetVelocity);
        long predictedTime = this.fireTime;
        boolean clockwisePrediction = this.orbitDirection == 1 && positiveGuessFactor || this.orbitDirection == -1 && !positiveGuessFactor;
        double noSmoothingEscapeAngle = 0.0;
        double bulletVelocity = this.bulletSpeed;
        do {
            predictedState = DRUtils.nextPerpendicularLocation(predictedState.location, this.absBearing, predictedState.velocity, predictedState.heading, clockwisePrediction, predictedTime, true);
            predictedTime = predictedState.time;
            if (!this._fieldRect.contains((Point2D)predictedState.location)) {
                hitWall = true;
                continue;
            }
            if (!this.wavePassed(predictedState.location, predictedTime, bulletVelocity)) continue;
            wavePassed = true;
        } while (!hitWall && !wavePassed);
        noSmoothingEscapeAngle = Math.abs((double)Utils.normalRelativeAngle((double)(DRUtils.absoluteBearing(this.sourceLocation, predictedState.location) - this.absBearing)));
        double withSmoothingEscapeAngle = 0.0;
        if (hitWall) {
            double wallSmoothingStick = 80.0;
            double purelyPerpendicularAttackAngle = 0.0;
            double fullVelocity = 8.0;
            double orbitAbsBearing = this.absBearing;
            double bestSmoothingEscapeAngle = 0.0;
            int x = 0;
            while (x < 3) {
                wavePassed = false;
                predictedState = new RobotState((Point2D.Double)this.targetLocation.clone(), this.targetHeading, this.targetVelocity);
                predictedTime = this.fireTime;
                do {
                    predictedState = DRUtils.nextPerpendicularWallSmoothedLocation(predictedState.location, orbitAbsBearing, predictedState.velocity, fullVelocity, predictedState.heading, purelyPerpendicularAttackAngle, clockwisePrediction, predictedTime, this._fieldRect, this._fieldWidth, this._fieldHeight, wallSmoothingStick, false);
                    predictedTime = predictedState.time;
                    if (!this.wavePassed(predictedState.location, predictedTime, bulletVelocity)) continue;
                    wavePassed = true;
                } while (!wavePassed);
                orbitAbsBearing = DRUtils.absoluteBearing(this.targetLocation, predictedState.location) - (double)(clockwisePrediction ? 1 : -1) * 1.5707963267948966;
                bestSmoothingEscapeAngle = Math.max((double)bestSmoothingEscapeAngle, (double)Math.abs((double)Utils.normalRelativeAngle((double)(DRUtils.absoluteBearing(this.sourceLocation, predictedState.location) - this.absBearing))));
                ++x;
            }
            withSmoothingEscapeAngle = bestSmoothingEscapeAngle;
        }
        return Math.max((double)noSmoothingEscapeAngle, (double)withSmoothingEscapeAngle);
    }

    public void clearCachedPreciseEscapeAngles() {
        this._cachedPositiveEscapeAngle = -1.0;
        this._cachedNegativeEscapeAngle = -1.0;
    }
}


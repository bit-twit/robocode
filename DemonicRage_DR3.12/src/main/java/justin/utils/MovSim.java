/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.lang.Math
 *  java.lang.Object
 *  robocode.AdvancedRobot
 *  robocode.util.Utils
 */
package justin.utils;

import justin.utils.MovSimStat;
import robocode.AdvancedRobot;
import robocode.util.Utils;

public class MovSim {
    private double systemMaxTurnRate = Math.toRadians((double)10.0);
    private double systemMaxVelocity = 8.0;
    private double maxBraking = 2.0;
    private double maxAcceleration = 1.0;
    public double defaultMaxTurnRate = 10.0;
    public double defaultMaxVelocity = 8.0;

    public MovSimStat[] futurePos(int steps, AdvancedRobot b) {
        return this.futurePos(steps, b, this.defaultMaxVelocity, this.defaultMaxTurnRate);
    }

    public MovSimStat[] futurePos(int steps, AdvancedRobot b, double maxVel, double maxTurnRate) {
        return this.futurePos(steps, b.getX(), b.getY(), b.getVelocity(), maxVel, b.getHeadingRadians(), b.getDistanceRemaining(), b.getTurnRemainingRadians(), maxTurnRate, b.getBattleFieldWidth(), b.getBattleFieldHeight());
    }

    public MovSimStat[] futurePos(int steps, double x, double y, double velocity, double maxVelocity, double heading, double distanceRemaining, double angleToTurn, double maxTurnRate, double battleFieldW, double battleFieldH) {
        MovSimStat[] pos = new MovSimStat[steps];
        double acceleration = 0.0;
        boolean slowingDown = false;
        maxTurnRate = Math.toRadians((double)maxTurnRate);
        double moveDirection = distanceRemaining == 0.0 ? 0.0 : (distanceRemaining < 0.0 ? -1.0 : 1.0);
        int i = 0;
        while (i < steps) {
            double lastHeading = heading;
            double turnRate = Math.min((double)maxTurnRate, (double)((0.4 + 0.6 * (1.0 - Math.abs((double)velocity) / this.systemMaxVelocity)) * this.systemMaxTurnRate));
            if (angleToTurn > 0.0) {
                if (angleToTurn < turnRate) {
                    heading += angleToTurn;
                    angleToTurn = 0.0;
                } else {
                    heading += turnRate;
                    angleToTurn -= turnRate;
                }
            } else if (angleToTurn < 0.0) {
                if (angleToTurn > - turnRate) {
                    heading += angleToTurn;
                    angleToTurn = 0.0;
                } else {
                    heading -= turnRate;
                    angleToTurn += turnRate;
                }
            }
            heading = Utils.normalAbsoluteAngle((double)heading);
            if (distanceRemaining != 0.0 || velocity != 0.0) {
                if (!slowingDown && moveDirection == 0.0) {
                    slowingDown = true;
                    moveDirection = velocity > 0.0 ? 1.0 : (velocity < 0.0 ? -1.0 : 0.0);
                }
                double desiredDistanceRemaining = distanceRemaining;
                if (slowingDown) {
                    if (moveDirection == 1.0 && distanceRemaining < 0.0) {
                        desiredDistanceRemaining = 0.0;
                    } else if (moveDirection == -1.0 && distanceRemaining > 1.0) {
                        desiredDistanceRemaining = 0.0;
                    }
                }
                double slowDownVelocity = (int)(this.maxBraking / 2.0 * (Math.sqrt((double)(4.0 * Math.abs((double)desiredDistanceRemaining) + 1.0)) - 1.0));
                if (moveDirection == -1.0) {
                    slowDownVelocity = - slowDownVelocity;
                }
                if (!slowingDown) {
                    if (moveDirection == 1.0) {
                        acceleration = velocity < 0.0 ? this.maxBraking : this.maxAcceleration;
                        if (velocity + acceleration > slowDownVelocity) {
                            slowingDown = true;
                        }
                    } else if (moveDirection == -1.0) {
                        acceleration = velocity > 0.0 ? - this.maxBraking : - this.maxAcceleration;
                        if (velocity + acceleration < slowDownVelocity) {
                            slowingDown = true;
                        }
                    }
                }
                if (slowingDown) {
                    double perfectAccel;
                    if (distanceRemaining != 0.0 && Math.abs((double)velocity) <= this.maxBraking && Math.abs((double)distanceRemaining) <= this.maxBraking) {
                        slowDownVelocity = distanceRemaining;
                    }
                    if ((perfectAccel = slowDownVelocity - velocity) > this.maxBraking) {
                        perfectAccel = this.maxBraking;
                    } else if (perfectAccel < - this.maxBraking) {
                        perfectAccel = - this.maxBraking;
                    }
                    acceleration = perfectAccel;
                }
                if (velocity > maxVelocity || velocity < - maxVelocity) {
                    acceleration = 0.0;
                }
                if ((velocity += acceleration) > maxVelocity) {
                    velocity -= Math.min((double)this.maxBraking, (double)(velocity - maxVelocity));
                }
                if (velocity < - maxVelocity) {
                    velocity += Math.min((double)this.maxBraking, (double)(- velocity - maxVelocity));
                }
                double dx = velocity * Math.sin((double)heading);
                double dy = velocity * Math.cos((double)heading);
                x += dx;
                y += dy;
                if (slowingDown && velocity == 0.0) {
                    distanceRemaining = 0.0;
                    moveDirection = 0.0;
                    slowingDown = false;
                    acceleration = 0.0;
                }
                distanceRemaining -= velocity;
                if (x < 17.9998 || y < 17.9998 || x > battleFieldW - 17.9998 || y > battleFieldH - 17.9998) {
                    distanceRemaining = 0.0;
                    angleToTurn = 0.0;
                    velocity = 0.0;
                    moveDirection = 0.0;
                    x = Math.max((double)18.0, (double)Math.min((double)(battleFieldW - 18.0), (double)x));
                    y = Math.max((double)18.0, (double)Math.min((double)(battleFieldH - 18.0), (double)y));
                }
            }
            pos[i] = new MovSimStat(x, y, velocity, heading, Utils.normalRelativeAngle((double)(heading - lastHeading)));
            ++i;
        }
        return pos;
    }
}


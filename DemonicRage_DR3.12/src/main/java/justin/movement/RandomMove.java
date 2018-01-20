/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.RoundRectangle2D
 *  java.awt.geom.RoundRectangle2D$Double
 *  java.lang.Math
 *  robocode.util.Utils
 */
package justin.movement;

import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import justin.Enemy;
import justin.Module;
import justin.Movement;
import robocode.util.Utils;

public class RandomMove
extends Movement {
    static final double MAX_VELOCITY = 8.0;
    static final double WALL_MARGIN = 25.0;
    Point2D.Double robotLocation;
    Point2D.Double enemyLocation;
    double enemyDistance;
    double enemyAbsoluteBearing;
    double movementLateralAngle = 0.2;

    public RandomMove(Module bot) {
        super(bot);
    }

    @Override
    public void initialize() {
        this.enemyLocation = null;
    }

    public void cleanUpRound() {
    }

    @Override
    public void move() {
        if (this.bot.enemy.location == null) {
            return;
        }
        this.robotLocation = this.bot.myData.location;
        this.enemyAbsoluteBearing = this.bot.enemy.absBearingRadians;
        this.enemyDistance = this.bot.enemy.distance;
        this.enemyLocation = this.bot.enemy.location;
        this.reverse();
        Point2D.Double robotDestination = null;
        double tries = 0.0;
        do {
            robotDestination = RandomMove.project(this.enemyLocation, RandomMove.absoluteBearing((Point2D)this.enemyLocation, (Point2D)this.robotLocation) + this.movementLateralAngle, this.enemyDistance * (1.1 - tries / 100.0));
        } while ((tries += 1.0) < 100.0 && !this.fieldRectangle(25.0).contains((Point2D)robotDestination));
        this.goTo((Point2D)robotDestination);
    }

    void reverse() {
        double flattenerFactor = 0.05;
        if (Math.random() < flattenerFactor) {
            this.movementLateralAngle *= -1.0;
        }
    }

    RoundRectangle2D fieldRectangle(double margin) {
        return new RoundRectangle2D.Double(margin, margin, this.bot.getBattleFieldWidth() - margin * 2.0, this.bot.getBattleFieldHeight() - margin * 2.0, 75.0, 75.0);
    }

    void goTo(Point2D destination) {
        double angle = Utils.normalRelativeAngle((double)(RandomMove.absoluteBearing((Point2D)this.robotLocation, destination) - this.bot.getHeadingRadians()));
        double turnAngle = Math.atan((double)Math.tan((double)angle));
        this.bot.setTurnRightRadians(turnAngle);
        this.bot.setAhead(this.robotLocation.distance(destination) * (double)(angle == turnAngle ? 1 : -1));
        this.bot.setMaxVelocity(Math.abs((double)this.bot.getTurnRemaining()) > 33.0 ? 0.0 : 8.0);
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin((double)angle) * length, sourceLocation.y + Math.cos((double)angle) * length);
    }

    static Point2D vector(double angle, double length, Point2D sourceLocation) {
        return RandomMove.vector(angle, length, sourceLocation, (Point2D)new Point2D.Double());
    }

    static Point2D vector(double angle, double length, Point2D sourceLocation, Point2D targetLocation) {
        targetLocation.setLocation(sourceLocation.getX() + Math.sin((double)angle) * length, sourceLocation.getY() + Math.cos((double)angle) * length);
        return targetLocation;
    }

    static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2((double)(target.getX() - source.getX()), (double)(target.getY() - source.getY()));
    }

    public static double limit(double min, double value, double max) {
        return Math.max((double)min, (double)Math.min((double)value, (double)max));
    }
}


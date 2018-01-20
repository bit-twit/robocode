package org.bittwit;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Hashtable;

public class YourAverageBot extends AdvancedRobot {
    private double BULLET_POWER = 1;
    private double BULLET_SPEED = 20 - 3 * BULLET_POWER;
    private static Hashtable<String, Enemy> enemies = new Hashtable<>();
    private static Enemy target;
    private static Point2D.Double nextDestination;
    private static Point2D.Double lastPosition;
    private static Point2D.Double curPos;
    private static double curEnergy;

    private void calculatePower(Point2D.Double aim) {
        double dist = aim.distance(curPos);
        if(dist>800)
            BULLET_POWER=0.5;
        else if(dist>600)
            BULLET_POWER=1;
        else if (dist>450)
            BULLET_POWER=2;
        else if (dist>250)
            BULLET_POWER=2.5;
        else
            BULLET_POWER=3;
        BULLET_SPEED = 20 - 3 * BULLET_POWER;
    }

    private static double calculateRisk(Point2D.Double p, double addLast) {
        double eval = addLast * 0.08 / p.distanceSq(lastPosition);

        Enumeration<Enemy> _enum = enemies.elements();
        while (_enum.hasMoreElements()) {
            Enemy en = _enum.nextElement();
            if (en.live) {
                eval += Math.min(en.energy / curEnergy, 2) *
                        (1 + Math.abs(Math.cos(calcAngle(curPos, p) - calcAngle(en.pos, p)))) / p.distanceSq(en.pos);
            }
        }
        return eval;
    }

    private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return new Point2D.Double(p.x + dist * Math.sin(ang), p.y + dist * Math.cos(ang));
    }

    private static double calcAngle(Point2D.Double p2, Point2D.Double p1) {
        return Math.atan2(p2.x - p1.x, p2.y - p1.y);
    }

    public void run() {
        setColors(Color.magenta, Color.magenta, Color.cyan);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        nextDestination = lastPosition = curPos = new Point2D.Double(getX(), getY());
        target = new Enemy();

        while (true) {
            curPos = new Point2D.Double(getX(), getY());
            curEnergy = getEnergy();

            if (target.live && getTime() > 9)
                doMovementAndGun();
            execute();
        }
    }

    private void doMovementAndGun() {

        double distanceToTarget = curPos.distance(target.pos);

        /*if (getGunTurnRemaining() == 0 && curEnergy > 1)
            setFire(Math.min(Math.min(curEnergy / 6d, 1300d / distanceToTarget), target.energy / 3d));

        setTurnGunRightRadians(Utils.normalRelativeAngle(calcAngle(target.pos, curPos) - getGunHeadingRadians()));*/

        double distanceToNextDestination = curPos.distance(nextDestination);

        if (distanceToNextDestination < 15) {
            double addLast = 1 - Math.rint(Math.pow(Math.random(), getOthers()));
            Rectangle2D.Double battleField = new Rectangle2D.Double(30, 30, getBattleFieldWidth() - 60, getBattleFieldHeight() - 60);
            Point2D.Double testPoint;
            int i = 200;

            while (i-- > 0) {
                testPoint = calcPoint(curPos, Math.min(distanceToTarget * 0.8, 100 + 200 * Math.random()), 2 * Math.PI * Math.random());
                if (battleField.contains(testPoint) && calculateRisk(testPoint, addLast) < calculateRisk(nextDestination, addLast))
                    nextDestination = testPoint;
            }

            lastPosition = curPos;

        } else {
            double angle = calcAngle(nextDestination, curPos) - getHeadingRadians();
            double direction = 1;

            if (Math.cos(angle) < 0) {
                angle += Math.PI;
                direction = -1;
            }
            setAhead(distanceToNextDestination * direction);
            setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
            setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        Enemy en = enemies.get(e.getName());

        if (en == null) {
            en = new Enemy();
            enemies.put(e.getName(), en);
            en.head = e.getHeadingRadians();
            en.headTime = getTime();
        }

        en.energy = e.getEnergy();
        en.live = true;
        en.pos = calcPoint(curPos, e.getDistance(), getHeadingRadians() + e.getBearingRadians());
        en.prevHead = en.head;
        en.prevHeadTime = en.headTime;
        en.head = e.getHeadingRadians();
        en.headTime = getTime();

        if (!target.live || e.getDistance() < curPos.distance(target.pos))
            target = en;

        if (target == en) {
            double enemyHeadingChange = (en.head - en.prevHead) / (en.headTime - en.prevHeadTime);
            double enemyHeading = en.head;
            double deltaTime = 0;
            Point2D.Double predict = en.pos;
            while((++deltaTime) * BULLET_SPEED <  curPos.distance(predict)) {
                predict.x += Math.sin(enemyHeading) * e.getVelocity();
                predict.y += Math.cos(enemyHeading) * e.getVelocity();
                enemyHeading += enemyHeadingChange;
                predict.x = Math.max(Math.min(predict.x,getBattleFieldWidth()-18),18);
                predict.y = Math.max(Math.min(predict.y,getBattleFieldHeight()-18),18);
            }

            calculatePower(predict);
            double dawei = Utils.normalAbsoluteAngle(Math.atan2(predict.x - getX(), predict.y - getY()));
            setTurnGunRightRadians(Utils.normalRelativeAngle(dawei - getGunHeadingRadians()));
            setFire(BULLET_POWER);
        }

        if (getOthers() == 1)
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }

    public void onRobotDeath(RobotDeathEvent e) {
        (enemies.get(e.getName())).live = false;
    }

    public class Enemy {
        Point2D.Double pos;
        double energy;
        boolean live;
        long prevHeadTime;
        double prevHead;
        long headTime;
        double head;
    }
}
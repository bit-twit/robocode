package org.tibtof;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Parzival extends AdvancedRobot {

    private final EnemyRepository enemyRepository = new EnemyRepository();
    private Rectangle2D.Double battleField;
    private Enemy target;
    private Point nextDestination;
    private Point lastPosition;
    private Point position;
    private double distanceToTarget;
    private double energy;

    public void run() {
        setColors(Color.white, Color.green, Color.pink);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        battleField = new Rectangle2D.Double(30, 30, getBattleFieldWidth() - 60, getBattleFieldHeight() - 60);

        nextDestination = lastPosition = position = new Point(getX(), getY());

        while (true) {
            position = new Point(getX(), getY());
            energy = getEnergy();

            if (getTime() > 7) {
                doMovementAndGun();
            }

            execute();

        }

    }

    private void doMovementAndGun() {
        distanceToTarget = Double.MAX_VALUE;
        if (target != null) {
            distanceToTarget = position.distance(target.getPosition());

            if (getGunTurnRemaining() == 0 && energy > 1) {
                setFire(Math.min(Math.min(energy / 6d, 1500d / distanceToTarget), target.getEnergy() / 3d));
//                setFire(1d);
            }

            setTurnGunRightRadians(
                    Utils.normalRelativeAngle(position.angle(target.getPosition()) - getGunHeadingRadians()));
        }

        double distanceToNextDestination = position.distance(nextDestination);

        if (distanceToNextDestination < 15) {

            double addLast = 1 - Math.rint(Math.pow(Math.random(), getOthers()));

            Point testPoint;
            int i = 0;

            do {
                testPoint = position.project(Math.min(distanceToTarget * 0.8, 100 + 200 * Math.random()), 2 * Math.PI * Math.random());
                if (battleField.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            } while (i++ < 200);

            lastPosition = position;
        } else {
            double angle = position.angle(nextDestination) - getHeadingRadians();
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

    private double evaluate(Point p, double addLast) {
        return enemyRepository.findAll()
                .stream()
                .map(e -> Math.min(e.getEnergy() / energy, 2) *
                        (1 + Math.abs(Math.cos(p.angle(position) - p.angle(e.getPosition())))) / p.distanceSq(e.getPosition()))
                .reduce(Double::sum)
                .orElse(addLast * 0.08 / p.distanceSq(lastPosition));
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        Enemy enemy = enemyRepository.findByName(event.getName());
        enemy.setEnergy(event.getEnergy());
        enemy.setPosition(position.project(event.getDistance(), getHeadingRadians() + event.getBearingRadians()));

        moveTargetToClosestEnemy(event, enemy);

        if (oneEnemyLeft()) {
            lockRadar();
        }
    }

    private void moveTargetToClosestEnemy(ScannedRobotEvent e, Enemy enemy) {
        if (target == null || e.getDistance() < position.distance(target.getPosition())) {
            target = enemy;
        }
    }

    private boolean oneEnemyLeft() {
        return getOthers() == 1;
    }

    private void lockRadar() {
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemyRepository.remove(e.getName());
        if (target != null && target.getName().equals(e.getName())) {
            target = null;
        }
    }

}

package org.bittwit;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

public class MaiLightAsa extends AdvancedRobot {
    static Random random = new Random(110000);
    static Hashtable enemies = new Hashtable();
    static TargetEnemy target;
    static Point2D.Double nextDestination;
    static Point2D.Double lastPosition;
    static Point2D.Double myPos;
    static double myEnergy;

    @Override
    public void run() {
        setColors(Color.yellow, Color.yellow, Color.yellow);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        nextDestination = lastPosition = myPos = new Point2D.Double(getX(), getY());

        target = new TargetEnemy();
       do {
            myPos = new Point2D.Double(getX(), getY());
            myEnergy = getEnergy();

            if (target.live && getTime() > 9) {
                moveAndShoot();
            }

            execute();
        } while (true);
    }

    private void moveAndShoot() {
        Rectangle2D.Double battleField = new Rectangle2D.Double(30, 30, getBattleFieldWidth() - 60, getBattleFieldHeight() - 60);
        
        double distanceToTarget = myPos.distance(target.pos);
        
        shoot(battleField, distanceToTarget);
        move(battleField, distanceToTarget);
    }

    private void move(Rectangle2D.Double battleField, double distanceToTarget) {
        
        double distanceToNextDestination = myPos.distance(nextDestination);
        if (distanceToNextDestination < 15) {

            double addLast = 1 - Math.rint(Math.pow(Math.random(), getOthers()));
            out.println(addLast);
            
            Point2D.Double testPoint;
            int i = 0;

            do {
                //	calculate the testPoint somewhere around the current position. 100 + 200*Math.random() proved to be good if there are
                //	around 10 bots in a 1000x1000 field. but this needs to be limited this to distanceToTarget*0.8. this way the bot wont
                //	run into the target (should mostly be the closest bot)
                testPoint = calcPoint(myPos, Math.min(distanceToTarget * 0.8, 100 + 200 * Math.random()), 2 * Math.PI * Math.random());
                if (battleField.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            } while (i++ < 200);

            lastPosition = myPos;

        } else {

            // just the normal goTo stuff
            double angle = calcAngle(nextDestination, myPos) - getHeadingRadians();
            double direction = 1;

            if (Math.cos(angle) < 0) {
                angle += Math.PI;
                direction = -1;
            }

            setAhead(distanceToNextDestination * direction);
            setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
            // hitting walls isn't a good idea, but HawkOnFire still does it pretty often
            setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);

        }
    }

    private void shoot(Rectangle2D.Double battleField, double distanceToTarget) {
        

        if (getGunTurnRemaining() == 0 && myEnergy > 1) {
            //setFire(Math.min(Math.min(myEnergy / 6d, 1300d / distanceToTarget), target.energy / 3d));
            setFire(myEnergy / distanceToTarget);
        }

        setTurnGunRightRadians(Utils.normalRelativeAngle(calcAngle(target.pos, myPos) - getGunHeadingRadians()));
    }

    private static double calcAngle(Point2D.Double p2, Point2D.Double p1) {
        return Math.atan2(p2.x - p1.x, p2.y - p1.y);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        
        out.println("scanned " + e.getName() + " bearing "+e.getBearingRadians());
        TargetEnemy en = (TargetEnemy) enemies.get(e.getName());

        if (en == null) {
            en = new TargetEnemy();
            enemies.put(e.getName(), en);
        }

        en.energy = e.getEnergy();
        en.live = true;
        en.pos = calcPoint(myPos, e.getDistance(), getHeadingRadians() + e.getBearingRadians());
        
        // normal target selection: the one closer to you is the most dangerous so attack him
        if (!target.live || e.getDistance() < myPos.distance(target.pos)) {
            target = en;
        }

        // locks the radar if there is only one opponent left
        if (getOthers() == 1) setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }

    private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return new Point2D.Double(p.x + dist * Math.sin(ang), p.y + dist * Math.cos(ang));
    }

    // events

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        out.println("Ouch, I got hit.");
//        turnLeft(90 - e.getBearing());
        // Move randomly ahead/back 10 and in the same time turn left perpendicular to the bullet
        setAhead(30 + random.nextInt(100000) % 10);
//        degrees  = Utils.normalRelativeAngleDegrees()
        turnLeft(90 - e.getBearing());
    }

    @Override
    public void onBulletHit(BulletHitEvent ev) {
        out.println("Uuuu, I hit.");
        Bullet b = ev.getBullet();
        MaiHard.TargetEnemy e = (MaiHard.TargetEnemy) enemies.get(ev.getName());
        if (e != null) {
            double power = b.getPower();
            double damage = 4 * power;
            if (power > 1) damage += 2 * (power - 1);
            e.energy -= damage;
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        out.println("Ouch, I hit a wall bearing " + event.getBearing() + " degrees.");
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        out.println("Ouch, I hit a robot bearing " + event.getBearing() + " degrees.");
    }

    public void onRobotDeath(RobotDeathEvent e) {
        ((TargetEnemy) enemies.get(e.getName())).live = false;
    }

    public static double evaluate(Point2D.Double p, double addLast) {
        // this is basically here that the bot uses more space on the battlefield. In melee it is dangerous to stay somewhere too long.
        double eval = addLast * 0.08 / p.distanceSq(lastPosition);

        Enumeration _enum = enemies.elements();
        while (_enum.hasMoreElements()) {
            TargetEnemy en = (TargetEnemy) _enum.nextElement();
            // this is the heart of HawkOnFire. So I try to explain what I wanted to do:
            // -	Math.min(en.energy/myEnergy,2) is multiplied because en.energy/myEnergy is an indicator how dangerous an enemy is
            // -	Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p))) is bigger if the moving direction isn't good in relation
            //		to a certain bot. it would be more natural to use Math.abs(Math.cos(calcAngle(p, myPos) - calcAngle(en.pos, myPos)))
            //		but this wasn't going to give me good results
            // -	1 / p.distanceSq(en.pos) is just the normal anti gravity thing
            if (en.live) {
                eval += Math.min(en.energy / myEnergy, 2) *
                        (1 + Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p)))) / p.distanceSq(en.pos);
            }
        }
        return eval;
    }

    public class TargetEnemy {
        public Point2D.Double pos;
        public double energy;
        public boolean live;
    }
}

package org.bittwit;


import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Ionut extends AdvancedRobot {

    int moveDirection = 1;//which way to move
    //boolean movingForward;

    /**
     * run:  Tracker's main run function
     */
    public void run() {
        setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
        setAllColors(Color.white);
        setScanColor(Color.white);
        setBulletColor(Color.yellow);
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
        turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
    }

    /**
     * onScannedRobot:  Here's the good stuff
     */

    Map<String, Double> tankEnergy = new HashMap<>();

    public void onScannedRobot(ScannedRobotEvent e) {
        out.println("found "+e.getName() + " "+ e.getEnergy());
        double absBearing = e.getBearingRadians() + getHeadingRadians();//enemies absolute bearing
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity
        double gunTurnAmt;//amount to turn our gun
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar

        if (Math.random() > .9) {
            setMaxVelocity((12 * Math.random()) + 12);//randomly change speed
        }

        if (!tankEnergy.containsKey(e.getName())) {
            tankEnergy.put(e.getName(), e.getEnergy());
        } else {
            Double targetEnergy = tankEnergy.get(e.getName());
            if (e.getEnergy() < targetEnergy) {
                out.println("a tras !!");
                reverseDirection();
                setTurnRightRadians(45);
                if (moveDirection > 0) {
                    setAhead(4000);
                } else {
                    setBack(4000);
                }
                tankEnergy.put(e.getName(), e.getEnergy());
            }

        }

        if (e.getDistance() > 150) {//if distance is greater than 150
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));//drive towards the enemies predicted future location
            setAhead((e.getDistance() - 140) * moveDirection);//move forward
            setFire(1);//fire
        } else {//if we are close enough...
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 15);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt);//turn our gun
            setTurnLeft(-90 - e.getBearing()); //turn perpendicular to the enemy
            setAhead((e.getDistance() - 140) * moveDirection);//move forward
            setFire(3);//fire
        }
    }

    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;//reverse direction upon hitting a wall
    }

    public void onHitRobot(HitRobotEvent e) {
        if (e.isMyFault()) {
            this.reverseDirection();
        }

    }

    public void reverseDirection() {
//        if (this.movingForward) {
//            this.setBack(140.0D);
//            this.movingForward = false;
//        } else {
//            this.setAhead(140.0D);
//            this.movingForward = true;
//        }
        moveDirection = -moveDirection;//reverse direction upon hitting a wall

    }
}

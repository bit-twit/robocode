/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.lang.Math
 *  robocode.Bullet
 *  robocode.Rules
 *  robocode.util.Utils
 */
package justin.targeting;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import justin.Enemy;
import justin.Module;
import justin.Targeting;
import justin.utils.DRUtils;
import robocode.Bullet;
import robocode.Rules;
import robocode.util.Utils;

public class CircularTargeting
extends Targeting {
    double bulletPower = 3.0;
    public static Point2D.Double myNextLocation;

    public CircularTargeting(Module bot) {
        super(bot);
    }

    @Override
    public void target() {
        if (this.bot.enemy.location == null) {
            return;
        }
        myNextLocation = DRUtils.nextLocation(this.bot);
        this.setBulletPower();
        double enemyHeading = this.bot.enemy.headingRadians;
        double enemyVelocity = this.bot.enemy.velocity;
        double deltaTime = 0.0;
        Point2D.Double predictedLocation = this.bot.enemy.location;
        while ((deltaTime += 1.0) * Rules.getBulletSpeed((double)this.bulletPower) < myNextLocation.distance((Point2D)predictedLocation)) {
            if (this.bot.enemy.accel > 0.0 && Math.abs((double)enemyVelocity) < 8.0) {
                enemyVelocity = DRUtils.limit(-8.0, enemyVelocity + this.bot.enemy.direction, 8.0);
            }
            predictedLocation = DRUtils.project(predictedLocation, enemyHeading, enemyVelocity);
            enemyHeading -= this.bot.enemy.deltaHeadingRadians;
            if (!Module.bf.contains((Point2D)predictedLocation)) break;
        }
        double theta = DRUtils.absoluteBearing(myNextLocation, predictedLocation);
        this.bot.setTurnGunRightRadians(Utils.normalRelativeAngle((double)(theta - this.bot.getGunHeadingRadians())));
        Bullet bullet = this.bot.setFireBullet(this.bulletPower);
        this.bot.registerMyBullet(bullet);
    }

    public void setBulletPower() {
        Enemy e = Enemy.getClosestBotTo(this.bot.myData, this.bot);
        this.bulletPower = 3.0;
        this.bulletPower = (this.bot.getEnergy() > 80.0 || this.bot.getOthers() > 7) && this.bot.getRoundNum() > 1 ? Math.min((double)this.bulletPower, (double)(1200.0 / e.distance)) : Math.min((double)this.bulletPower, (double)(900.0 / e.distance));
        this.bulletPower = Math.min((double)this.bulletPower, (double)((e.energy + 0.1) / 4.0));
        if (this.bulletPower * 6.0 >= this.bot.getEnergy()) {
            this.bulletPower = this.bot.getEnergy() / 5.0;
        }
        this.bulletPower = DRUtils.limit(0.1, this.bulletPower, 3.0);
        if (this.bot.getEnergy() - this.bulletPower < 0.2) {
            this.bulletPower = 0.0;
        }
    }
}


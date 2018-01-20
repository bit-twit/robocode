/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.lang.Math
 *  java.util.Hashtable
 */
package justin;

import java.awt.Color;
import java.util.Hashtable;
import justin.BulletInfoEnemy;
import justin.Enemy;
import justin.Module;
import justin.Movement;
import justin.Radar;
import justin.SelectEnemy;
import justin.Special;
import justin.Targeting;
import justin.movement.GoToCenterField;
import justin.movement.HoF;
import justin.movement.PathFinderMelee;
import justin.movement.Quiet;
import justin.movement.RandomMove;
import justin.movement.Walls;
import justin.radar.DynamicLocking;
import justin.radar.SpinningRadar;
import justin.radar.WideLock;
import justin.selectEnemy.EnemySelection;
import justin.special.ColorChanging;
import justin.targeting.CircularTargeting;
import justin.targeting.DCGun;
import justin.targeting.HeadOnTargeting;
import justin.targeting.QuietGun;

public class DemonicRage
extends Module {
    SelectEnemy enemySelection;
    Radar spinningRadar;
    Radar wideLock;
    Radar dynamicLocking;
    Targeting quietGun;
    Targeting headOnTargeting;
    Targeting circularTargeting;
    Targeting dcGun;
    Movement quiet;
    Movement goToCenterField;
    Movement walls;
    Movement randomMove;
    Movement pathFinderMelee;
    Movement hoF;
    ColorChanging colorChangingBot;

    public DemonicRage() {
        this.enemySelection = new EnemySelection(this);
        this.spinningRadar = new SpinningRadar(this);
        this.wideLock = new WideLock(this);
        this.dynamicLocking = new DynamicLocking(this);
        this.quietGun = new QuietGun(this);
        this.headOnTargeting = new HeadOnTargeting(this);
        this.circularTargeting = new CircularTargeting(this);
        this.dcGun = new DCGun(this);
        this.quiet = new Quiet(this);
        this.goToCenterField = new GoToCenterField(this);
        this.walls = new Walls(this);
        this.randomMove = new RandomMove(this);
        this.pathFinderMelee = new PathFinderMelee(this);
        this.hoF = new HoF(this);
        this.colorChangingBot = new ColorChanging(this);
    }

    @Override
    protected void initialize() {
        this.colorChangingBot.initialBody = Color.black;
        this.colorChangingBot.initialGun = Color.black;
        this.colorChangingBot.initialRadar = Color.black;
        this.colorChangingBot.endBody = new Color(150, 0, 0);
        this.colorChangingBot.endGun = new Color(150, 0, 0);
        this.colorChangingBot.endRadar = new Color(150, 0, 0);
        this.pathFinderMelee.initialize();
        this.randomMove.initialize();
    }

    @Override
    protected void selectBehavior() {
        this.activate(this.colorChangingBot);
        double hitRate = this.enemy.bulletHits1v1 / Math.max((double)this.enemy.bulletShots1v1, (double)1.0) * 100.0;
        if (melee) {
            BulletInfoEnemy.flattenerValue = 0.0;
        } else if (this.getTime() % 100L == 1L) {
            BulletInfoEnemy.flattenerValue = this.myData.energy >= 0.3 && (hitRate < 11.0 || this.getRoundNum() < 3) ? 0.0 : 0.2;
        }
        if (enemies.size() > 0 && this.getOthers() > 0) {
            this.selectEnemy = this.enemySelection;
            this.radar = this.dynamicLocking;
            this.targeting = this.dcGun;
            this.movement = this.pathFinderMelee;
        } else {
            this.selectEnemy = this.enemySelection;
            this.radar = this.spinningRadar;
            this.targeting = this.quietGun;
            this.movement = this.randomMove;
        }
    }
}


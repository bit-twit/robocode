/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  robocode.util.Utils
 */
package justin.targeting;

import justin.Enemy;
import justin.Module;
import justin.Targeting;
import robocode.util.Utils;

public class HeadOnTargeting
extends Targeting {
    public HeadOnTargeting(Module bot) {
        super(bot);
    }

    @Override
    public void target() {
        this.bot.setTurnGunRightRadians(Utils.normalRelativeAngle((double)(this.bot.enemy.absBearingRadians - this.bot.getGunHeadingRadians())));
    }
}


/*
 * Decompiled with CFR 0_124.
 */
package justin.targeting;

import justin.Module;
import justin.Targeting;

public class QuietGun
extends Targeting {
    public QuietGun(Module bot) {
        super(bot);
    }

    @Override
    public void target() {
        this.bot.setTurnGunRightRadians(1.0E-4);
    }
}


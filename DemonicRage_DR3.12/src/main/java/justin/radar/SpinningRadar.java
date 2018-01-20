/*
 * Decompiled with CFR 0_124.
 */
package justin.radar;

import justin.Module;
import justin.Radar;

public class SpinningRadar
extends Radar {
    public SpinningRadar(Module bot) {
        super(bot);
    }

    @Override
    public void scan() {
        this.bot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }
}


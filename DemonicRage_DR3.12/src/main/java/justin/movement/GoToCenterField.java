/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 */
package justin.movement;

import java.awt.geom.Point2D;
import justin.Enemy;
import justin.Module;
import justin.Movement;
import justin.utils.DRUtils;

public class GoToCenterField
extends Movement {
    public GoToCenterField(Module bot) {
        super(bot);
    }

    @Override
    public void move() {
        Point2D.Double bfCenter = new Point2D.Double(Module.bw / 2.0, Module.bh / 2.0);
        DRUtils.setBackAsFront(this.bot, DRUtils.absoluteBearing(this.bot.myData.location, bfCenter));
    }
}


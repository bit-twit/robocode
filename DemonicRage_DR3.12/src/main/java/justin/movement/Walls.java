/*
 * Decompiled with CFR 0_124.
 */
package justin.movement;

import justin.Module;
import justin.Movement;

public class Walls
extends Movement {
    public Walls(Module bot) {
        super(bot);
    }

    @Override
    public void move() {
        if (this.bot.getHeading() % 90.0 != 0.0) {
            this.bot.setTurnLeft(this.bot.getHeading() % 90.0);
        }
        this.bot.setAhead(Double.POSITIVE_INFINITY);
        if (this.bot.getVelocity() == 0.0) {
            this.bot.setTurnRight(90.0);
        }
    }
}


/*
 * Decompiled with CFR 0_124.
 */
package justin.movement;

import justin.Module;
import justin.Movement;

public class Quiet
extends Movement {
    public Quiet(Module bot) {
        super(bot);
    }

    @Override
    public void move() {
        this.bot.setAhead(1.0E-4);
    }
}


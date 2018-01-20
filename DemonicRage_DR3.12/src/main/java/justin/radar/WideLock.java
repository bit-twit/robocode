/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.lang.Math
 *  robocode.Event
 *  robocode.ScannedRobotEvent
 *  robocode.util.Utils
 */
package justin.radar;

import justin.Enemy;
import justin.Module;
import justin.Radar;
import robocode.Event;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class WideLock
extends Radar {
    private int timeSinceLastScan = 10;
    private double enemyAbsoluteBearing;

    public WideLock(Module bot) {
        super(bot);
    }

    @Override
    public void scan() {
        ++this.timeSinceLastScan;
        double radarOffset = Double.NEGATIVE_INFINITY;
        if (this.timeSinceLastScan < 3) {
            radarOffset = Utils.normalRelativeAngle((double)(this.bot.getRadarHeadingRadians() - this.bot.enemy.absBearingRadians));
            radarOffset += Math.signum((double)radarOffset) * 0.2;
        }
        this.bot.setTurnRadarLeftRadians(radarOffset);
    }

    @Override
    public void listen(Event e) {
        if (e instanceof ScannedRobotEvent) {
            this.timeSinceLastScan = 0;
        }
    }
}


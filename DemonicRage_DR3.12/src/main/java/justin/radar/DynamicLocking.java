/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.lang.Boolean
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 *  robocode.DeathEvent
 *  robocode.Event
 *  robocode.RobotDeathEvent
 *  robocode.ScannedRobotEvent
 *  robocode.WinEvent
 *  robocode.util.Utils
 */
package justin.radar;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import justin.Enemy;
import justin.Module;
import justin.Radar;
import robocode.DeathEvent;
import robocode.Event;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class DynamicLocking
extends Radar {
    static final double PI = 3.141592653589793;
    static double radarDirection = 1.0;
    public Enemy lookingFor = new Enemy();
    private Hashtable<String, Boolean> knownEnemiesList = new Hashtable();
    public boolean knownEnemiesListFull = false;

    public DynamicLocking(Module bot) {
        super(bot);
    }

    @Override
    public void scan() {
        if (this.bot.getRadarTurnRemaining() == 0.0) {
            radarDirection = Utils.normalRelativeAngle((double)(this.absbearing(this.bot.getX(), this.bot.getY(), this.bot.getBattleFieldWidth() / 2.0, this.bot.getBattleFieldHeight() / 2.0) - this.bot.getRadarHeadingRadians())) > 0.0 ? 1 : -1;
            double radarTurn = Double.POSITIVE_INFINITY * radarDirection;
            this.bot.setTurnRadarRightRadians(radarTurn);
        }
    }

    @Override
    public void listen(Event e) {
        if (e instanceof WinEvent) {
            this.cleanUpRound();
        }
        if (e instanceof DeathEvent) {
            this.cleanUpRound();
        }
        if (e instanceof RobotDeathEvent && ((RobotDeathEvent)e).getName() == this.lookingFor.name) {
            this.lookingFor = new Enemy();
        }
        if (e instanceof ScannedRobotEvent) {
            if (!this.knownEnemiesListFull) {
                ((Enemy)Module.enemies.get((Object)((ScannedRobotEvent)e).getName())).alive = true;
                this.knownEnemiesList.put((Object)((ScannedRobotEvent)e).getName(), (Object)true);
                boolean bl = this.knownEnemiesListFull = this.knownEnemiesList.size() >= this.bot.getOthers();
                if (!this.knownEnemiesListFull) {
                    return;
                }
            }
            if (this.lookingFor.name == null) {
                this.lookingFor = (Enemy)Module.enemies.get((Object)((ScannedRobotEvent)e).getName());
            }
            if (((ScannedRobotEvent)e).getName() == this.lookingFor.name) {
                Iterator iterator = Module.enemies.values().iterator();
                double bestScore = Double.POSITIVE_INFINITY;
                while (iterator.hasNext()) {
                    double score;
                    double time;
                    double sweepSize;
                    Enemy tank = (Enemy)iterator.next();
                    if (!tank.alive || (score = (time = tank.scanTime) - (sweepSize = Math.abs((double)Utils.normalRelativeAngle((double)(tank.absBearingRadians - this.bot.getRadarHeadingRadians()))) / 3.141592653589793)) >= bestScore) continue;
                    bestScore = score;
                    this.lookingFor = tank;
                }
                double angle = this.lookingFor.absBearingRadians - this.lookingFor.deltaAbsBearingRadians * 2.0;
                radarDirection = (int)Math.signum((double)Utils.normalRelativeAngle((double)(angle - this.bot.getRadarHeadingRadians())));
                double turnsTillScanBot = Utils.normalRelativeAngle((double)Math.abs((double)(this.bot.getRadarHeadingRadians() - angle))) / 0.7;
                double radarTurn = Double.POSITIVE_INFINITY * radarDirection;
                if (this.lookingFor.deltaScanTime < 1.1 && turnsTillScanBot < 1.0) {
                    double offset = 0.0;
                    offset += Math.abs((double)(this.lookingFor.deltaAbsBearingRadians * 3.0));
                    offset += 20.0 * this.lookingFor.deltaScanTime / this.lookingFor.distance;
                    radarTurn = Utils.normalRelativeAngle((double)(angle - this.bot.getRadarHeadingRadians() + (offset *= radarDirection)));
                }
                this.bot.setTurnRadarRightRadians(radarTurn);
            }
        }
    }

    public void cleanUpRound() {
        this.knownEnemiesList = null;
        this.knownEnemiesListFull = false;
        this.lookingFor = new Enemy();
        for (Enemy him : Module.enemies.values()) {
            if (him.alive) continue;
            him.alive = true;
        }
    }

    public double absbearing(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = this.getRange(x1, y1, x2, y2);
        if (xo > 0.0 && yo > 0.0) {
            return Math.asin((double)(xo / h));
        }
        if (xo > 0.0 && yo < 0.0) {
            return 3.141592653589793 - Math.asin((double)(xo / h));
        }
        if (xo < 0.0 && yo < 0.0) {
            return 3.141592653589793 + Math.asin((double)((- xo) / h));
        }
        if (xo < 0.0 && yo > 0.0) {
            return 6.283185307179586 - Math.asin((double)((- xo) / h));
        }
        return 0.0;
    }

    public double getRange(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = Math.sqrt((double)(xo * xo + yo * yo));
        return h;
    }
}


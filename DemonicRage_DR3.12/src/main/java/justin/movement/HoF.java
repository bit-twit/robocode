/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.Collection
 *  java.util.Hashtable
 *  robocode.Event
 *  robocode.RobotDeathEvent
 *  robocode.ScannedRobotEvent
 *  robocode.util.Utils
 */
package justin.movement;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Hashtable;
import justin.Module;
import justin.Movement;
import justin.movement.microEnemy;
import robocode.Event;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class HoF
extends Movement {
    static final int m = 30;
    static Hashtable targets = new Hashtable();
    static microEnemy target = new microEnemy();
    static Point2D.Double myPos;
    static Point2D.Double lastPos;
    static double myEnergy;
    Point2D.Double next;

    public HoF(Module bot) {
        super(bot);
    }

    @Override
    public void move() {
        myPos = new Point2D.Double(this.bot.getX(), this.bot.getY());
        myEnergy = this.bot.getEnergy();
        this.doStuff();
    }

    public void doStuff() {
        if (target == null || HoF.target.pos == null) {
            return;
        }
        int i = 0;
        Point2D.Double p = HoF.target.pos;
        double dist = myPos.distance((Point2D)p);
        double angle = Math.min((double)Math.min((double)(myEnergy / 6.0), (double)(1300.0 / dist)), (double)(HoF.target.energy / 3.0));
        if (this.next == null) {
            this.next = HoF.lastPos = myPos;
        }
        if ((angle = myPos.distance((Point2D)this.next)) < 15.0) {
            angle = 1.0 - Math.rint((double)Math.pow((double)Math.random(), (double)this.bot.getOthers()));
            do {
                p = HoF.calcPoint(myPos, Math.min((double)(dist * 0.8), (double)(100.0 + 200.0 * Math.random())), 6.283185307179586 * Math.random());
                if (!new Rectangle2D.Double(30.0, 30.0, this.bot.getBattleFieldWidth() - 60.0, this.bot.getBattleFieldHeight() - 60.0).contains((Point2D)p) || HoF.evaluate(p, angle) >= HoF.evaluate(this.next, angle)) continue;
                this.next = p;
            } while (i++ < 100);
            lastPos = myPos;
        } else {
            double d = angle;
            double d2 = Utils.normalRelativeAngle((double)(HoF.calcAngle(this.next, myPos) - this.bot.getHeadingRadians()));
            angle = d2;
            this.bot.setAhead(d * (double)(d2 != (angle = Math.atan((double)Math.tan((double)angle))) ? -1 : 1));
            this.bot.setTurnRightRadians(angle);
            this.bot.setMaxVelocity(Math.abs((double)angle) <= 1.0 ? 8.0 : 0.0);
        }
    }

    public static double evaluate(Point2D.Double p, double addLast) {
        double eval = addLast * 0.08 / p.distanceSq((Point2D)lastPos);
        for (microEnemy en : targets.values()) {
            if (!en.live) continue;
            eval += Math.min((double)(en.energy / myEnergy), (double)2.0) * (1.0 + Math.abs((double)Math.cos((double)(HoF.calcAngle(myPos, p) - HoF.calcAngle(en.pos, p))))) / p.distanceSq((Point2D)en.pos);
        }
        return eval;
    }

    @Override
    public void listen(Event e) {
        if (e instanceof ScannedRobotEvent) {
            String eName = ((ScannedRobotEvent)e).getName();
            microEnemy en = (microEnemy)targets.get((Object)eName);
            if (en == null) {
                en = new microEnemy();
                targets.put((Object)eName, (Object)en);
            }
            en.name = eName;
            en.energy = ((ScannedRobotEvent)e).getEnergy();
            en.live = true;
            en.pos = HoF.calcPoint(myPos, ((ScannedRobotEvent)e).getDistance(), this.bot.getHeadingRadians() + ((ScannedRobotEvent)e).getBearingRadians());
            if (!HoF.target.live || ((ScannedRobotEvent)e).getDistance() < myPos.distance((Point2D)HoF.target.pos)) {
                target = en;
            }
        }
        if (e instanceof RobotDeathEvent) {
            ((microEnemy)HoF.targets.get((Object)((RobotDeathEvent)e).getName())).live = false;
        }
    }

    private static final Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return new Point2D.Double(p.x + dist * Math.sin((double)ang), p.y + dist * Math.cos((double)ang));
    }

    private static final double calcAngle(Point2D.Double p2, Point2D.Double p1) {
        return Math.atan2((double)(p2.x - p1.x), (double)(p2.y - p1.y));
    }
}


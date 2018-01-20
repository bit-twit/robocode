/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.awt.Graphics2D
 *  java.awt.Shape
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 */
package justin.selectEnemy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import justin.Enemy;
import justin.Module;
import justin.SelectEnemy;

public class EnemySelection
extends SelectEnemy {
    double getOther;

    public EnemySelection(Module bot) {
        super(bot);
    }

    @Override
    public void select() {
        this.getOther = this.bot.getOthers();
        if (this.getOther == 0.0) {
            return;
        }
        Iterator iterator = Module.enemies.values().iterator();
        double bestScore = Double.POSITIVE_INFINITY;
        Enemy selected = this.bot.enemy;
        while (iterator.hasNext()) {
            Enemy e = (Enemy)iterator.next();
            if ((double)this.bot.getTime() - e.scanTime > 25.0) {
                e.alive = false;
            }
            if (!e.alive) continue;
            if (this.bot.enemy == null || this.bot.enemy.name == null) {
                this.bot.enemy = e;
            }
            double score = e.distance * (e.energy / 220.0 + 1.0);
            if (e.name == this.bot.enemy.name) {
                score = this.getOther > 2.0 ? (score *= 0.8) : (score *= 0.85);
            }
            if (this.getOther < 4.0) {
                score *= e.energy / 400.0 + 1.0;
            }
            if (e.energy < 1.0) {
                score *= 0.5;
            }
            if (score >= bestScore) continue;
            selected = e;
            bestScore = score;
        }
        this.bot.enemy = selected;
    }

    public static double limit(double min, double value, double max) {
        return Math.max((double)min, (double)Math.min((double)value, (double)max));
    }

    @Override
    public void onPaint(Graphics2D g) {
        g.setColor(new Color(50, 230, 100, 75));
        g.draw((Shape)Module.bf);
        for (Enemy e : Module.enemies.values()) {
            if (!e.alive) continue;
            if (this.bot.getOthers() > 1 && e.cbC < 1.0) {
                g.setColor(new Color(50, 230, 100, 35));
                g.fillOval((int)(e.location.x - e.cbD), (int)(e.location.y - e.cbD), (int)(e.cbD * 2.0), (int)(e.cbD * 2.0));
            }
            if (e.name == this.bot.enemy.name) {
                g.setColor(Color.red.darker());
            } else {
                g.setColor(Color.gray.darker());
            }
            g.drawRect((int)e.location.x - 20, (int)e.location.y - 20, 40, 40);
        }
    }
}


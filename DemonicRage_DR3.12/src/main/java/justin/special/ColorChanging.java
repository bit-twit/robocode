/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.lang.Math
 *  robocode.Robot
 */
package justin.special;

import java.awt.Color;
import justin.Module;
import justin.Special;
import justin.utils.DRUtils;
import robocode.Robot;

public class ColorChanging
extends Special {
    public Color initialBody;
    public Color initialGun;
    public Color initialRadar;
    public Color endBody;
    public Color endGun;
    public Color endRadar;
    public static double energyPercent = 1.2;

    public ColorChanging(Module bot) {
        super(bot);
    }

    @Override
    public void doIt() {
        this.updateColors((Robot)this.bot);
    }

    public void updateColors(Robot bot) {
        double value;
        double pulse = 0.0;
        double newestPercent = bot.getEnergy() / 105.0;
        if (bot.getTime() > 4L) {
            if (bot.getOthers() < 1) {
                newestPercent = newestPercent < 0.8 ? 1 : -1;
                pulse = 0.0;
                if (newestPercent > energyPercent) {
                    energyPercent = Math.min((double)1.01, (double)(energyPercent + 0.03));
                }
            } else if (newestPercent > energyPercent) {
                energyPercent = Math.min((double)0.99, (double)(energyPercent + 0.03));
            }
            if (newestPercent < energyPercent) {
                energyPercent = Math.max((double)0.03, (double)(energyPercent - 0.07));
            }
        }
        if ((value = DRUtils.limit(0.0, energyPercent + pulse, 1.01)) > 1.0) {
            bot.setColors(this.initialBody, this.initialGun, this.initialRadar);
        } else if (value < 0.0) {
            bot.setColors(this.endBody, this.endGun, this.endRadar);
        } else {
            bot.setBodyColor(ColorChanging.calculateNewColor(this.initialBody, this.endBody, value));
            bot.setGunColor(ColorChanging.calculateNewColor(this.initialGun, this.endGun, value));
            bot.setRadarColor(ColorChanging.calculateNewColor(this.initialRadar, this.endRadar, value));
        }
    }

    private static Color calculateNewColor(Color initial, Color end, double percent) {
        int newR = ColorChanging.calculateValue(initial.getRed(), end.getRed(), percent);
        int newG = ColorChanging.calculateValue(initial.getGreen(), end.getGreen(), percent);
        int newB = ColorChanging.calculateValue(initial.getBlue(), end.getBlue(), percent);
        return new Color(newR, newG, newB);
    }

    private static int calculateValue(double initial, double end, double percent) {
        double abs = Math.abs((double)(initial - end));
        int multiply = initial - end > 0.0 ? -1 : 1;
        return (int)Math.round((double)(initial + (double)multiply * (1.0 - percent) * abs));
    }
}


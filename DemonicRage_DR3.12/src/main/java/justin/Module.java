/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.awt.Color
 *  java.awt.Graphics2D
 *  java.awt.event.InputEvent
 *  java.awt.event.KeyEvent
 *  java.awt.geom.Point2D
 *  java.awt.geom.Point2D$Double
 *  java.awt.geom.Rectangle2D
 *  java.awt.geom.Rectangle2D$Double
 *  java.io.PrintStream
 *  java.lang.Class
 *  java.lang.Integer
 *  java.lang.Math
 *  java.lang.Object
 *  java.lang.String
 *  java.lang.System
 *  java.util.Collection
 *  java.util.Hashtable
 *  java.util.Iterator
 *  java.util.Vector
 *  justin.Gun
 *  robocode.AdvancedRobot
 *  robocode.Bullet
 *  robocode.BulletHitBulletEvent
 *  robocode.BulletHitEvent
 *  robocode.BulletMissedEvent
 *  robocode.DeathEvent
 *  robocode.Event
 *  robocode.HitByBulletEvent
 *  robocode.HitRobotEvent
 *  robocode.HitWallEvent
 *  robocode.RobotDeathEvent
 *  robocode.Rules
 *  robocode.ScannedRobotEvent
 *  robocode.SkippedTurnEvent
 *  robocode.WinEvent
 *  robocode.util.Utils
 */
package justin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import justin.BulletInfo;
import justin.BulletInfoEnemy;
import justin.Enemy;
import justin.Gun;
import justin.HistoryLog;
import justin.Movement;
import justin.Radar;
import justin.SelectEnemy;
import justin.Special;
import justin.Targeting;
import justin.utils.FastTrig;
import justin.utils.KdTree;
import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.Event;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public abstract class Module
extends AdvancedRobot {
    public Radar radar;
    public Targeting targeting;
    public Movement movement;
    public Gun gun;
    public SelectEnemy selectEnemy;
    public Vector<Special> specials = new Vector();
    public static final boolean enablePaint = true;
    public static final boolean enableWavesOnly = true;
    public static boolean paintWaves;
    public static boolean paintTargeting;
    public static boolean paintMovement;
    public static boolean paintRadar;
    public static boolean paintAll;
    public static boolean paintNone;
    public static int skippedTurns;
    private static int wallHits;
    private static int wallDamage;
    private static int[] finishes;
    public Enemy enemy = new Enemy();
    public Enemy myData = new Enemy();
    public static Hashtable<String, Enemy> enemies;
    public Vector<BulletInfo> bullets = new Vector();
    public Vector<BulletInfoEnemy> enemyBullets = new Vector();
    public static final boolean useNonSegmented = true;
    public static final int BINS = 41;
    public static boolean melee;
    public static double bw;
    public static double bh;
    public static Rectangle2D.Double bf;

    static {
        FastTrig.init();
        paintWaves = false;
        paintTargeting = false;
        paintMovement = false;
        paintRadar = false;
        paintAll = false;
        paintNone = true;
        skippedTurns = 0;
        wallHits = 0;
        wallDamage = 0;
        enemies = new Hashtable();
        melee = true;
    }

    public void run() {
        this.setAdjustRadarForRobotTurn(true);
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);
        if (finishes == null) {
            finishes = new int[this.getOthers() + 1];
        }
        bw = this.getBattleFieldWidth();
        bh = this.getBattleFieldHeight();
        bf = new Rectangle2D.Double(17.6, 17.6, bw - 35.2, bh - 35.2);
        this.initialize();
        do {
            melee = this.getOthers() > 1;
            this.updateMyData();
            BulletInfoEnemy.updateEnemyBullets(this);
            this.selectBehavior();
            this.executeBehavior();
        } while (true);
    }

    protected abstract void selectBehavior();

    protected abstract void initialize();

    private void executeBehavior() {
        this.selectEnemy.select();
        this.radar.scan();
        this.targeting.target();
        this.movement.move();
        Iterator i = this.specials.iterator();
        while (i.hasNext()) {
            ((Special)i.next()).doIt();
        }
        this.execute();
    }

    private void listenEvent(Event e) {
        this.selectEnemy.listen(e);
        this.radar.listen(e);
        this.targeting.listen(e);
        this.movement.listen(e);
        Iterator i = this.specials.iterator();
        while (i.hasNext()) {
            ((Special)i.next()).listen(e);
        }
    }

    private void listenInputEvent(InputEvent e) {
        if (this.selectEnemy != null) {
            this.selectEnemy.listenInput(e);
        }
        if (this.radar != null) {
            this.radar.listenInput(e);
        }
        if (this.targeting != null) {
            this.targeting.listenInput(e);
        }
        if (this.movement != null) {
            this.movement.listenInput(e);
        }
        for (Special special : this.specials) {
            if (special == null) continue;
            special.listenInput(e);
        }
    }

    public void activate(Special special) {
        if (!this.specials.contains((Object)special)) {
            this.specials.add((Object)special);
        }
    }

    public void deactivate(Special special) {
        this.specials.remove((Object)special);
    }

    public void registerMyBullet(Bullet bullet) {
        BulletInfo bulletInfo = new BulletInfo();
        bulletInfo.bullet = bullet;
        bulletInfo.targeting = this.targeting.getClass().getSimpleName();
        bulletInfo.timeFire = (int)this.getTime();
        this.bullets.add((Object)bulletInfo);
    }

    public void updateMyData() {
        double dir;
        this.myData.alive = true;
        this.myData.name = this.getName();
        this.myData.scanTime = this.getTime();
        this.myData.deltaScanTime = 1.0;
        this.myData.previousBearingRadians = 0.0;
        this.myData.bearingRadians = 0.0;
        this.myData.deltaHeadingRadians = Utils.normalRelativeAngle((double)(this.myData.headingRadians - this.getHeadingRadians()));
        this.myData.headingRadians = this.getHeadingRadians();
        this.myData.velocity = this.getVelocity();
        double d = dir = this.myData.velocity != 0.0 ? Math.signum((double)this.myData.velocity) : this.myData.direction;
        this.myData.tSDC = this.myData.direction == dir && this.myData.deltaScanTime < 20.0 && this.myData.round == (double)this.getRoundNum() ? (this.myData.tSDC += 1.0) : 0.0;
        this.myData.direction = dir;
        this.myData.round = this.getRoundNum();
        this.myData.correctedHeadingRadians = this.myData.direction < 0.0 ? Utils.normalAbsoluteAngle((double)(this.getHeadingRadians() + 3.141592653589793)) : this.getHeadingRadians();
        this.myData.distance = 0.0;
        this.myData.location = new Point2D.Double(this.getX(), this.getY());
        this.myData.energy = this.getEnergy();
        this.myData.scanTime = this.getTime();
        this.myData.round = this.getRoundNum();
        Enemy myClosestBot = Enemy.getClosestBotTo(this.myData, this);
        this.myData.cbName = myClosestBot.name;
        this.myData.cbD = this.myData.location.distance((Point2D)myClosestBot.location);
        this.myData.cbC = 0.0;
        this.myData.timeAliveTogether += 1.0;
        HistoryLog historyLog = new HistoryLog();
        historyLog.scanTime = this.getTime();
        historyLog.round = this.getRoundNum();
        historyLog.location = this.myData.location;
        historyLog.headingRadians = this.myData.correctedHeadingRadians;
        historyLog.velocity = this.getVelocity();
        if (this.myData.last != null) {
            Enemy.updateHistoryLog(historyLog, this.myData);
        } else {
            this.myData.last = historyLog;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        Enemy oldData = (Enemy)enemies.get((Object)e.getName());
        if (oldData == null) {
            oldData = new Enemy();
            oldData.gunTree1vrs1 = new KdTree.SqrEuclid<HistoryLog>(9, 30000);
            oldData.gunTreeMelee = new KdTree.SqrEuclid<HistoryLog>(9, 30000);
            oldData.surfStats1vrs1 = BulletInfoEnemy.getDefaultWave();
            oldData.surfStatsMelee = BulletInfoEnemy.getDefaultWave();
            oldData.surfStats1vrs1Segmented = new double[4][4][4][4][41];
            oldData.surfStatsMeleeSegmented = new double[4][4][4][4][41];
        }
        Enemy scanned = Enemy.update(oldData, e, this);
        enemies.put((Object)e.getName(), (Object)scanned);
        BulletInfoEnemy.detection(scanned, e, this);
        this.listenEvent((Event)e);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        this.listenEvent((Event)e);
        Enemy him = (Enemy)enemies.get((Object)e.getBullet().getName());
        him.damageGiven += Rules.getBulletDamage((double)e.getBullet().getPower());
        him.timeLastBulletHit = this.getTime();
        if (melee) {
            him.bulletHitsMelee += 1.0;
        } else {
            him.bulletHits1v1 += 1.0;
        }
        if (!this.enemyBullets.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            BulletInfoEnemy hitWave = null;
            int x = 0;
            while (x < this.enemyBullets.size()) {
                BulletInfoEnemy ew = (BulletInfoEnemy)this.enemyBullets.get(x);
                if (ew.fromName == e.getBullet().getName()) {
                    ew.surf = true;
                }
                if (Math.abs((double)(ew.distanceTraveled - this.myData.location.distance((Point2D)ew.fireLocation))) < 55.0 && Math.round((double)(Rules.getBulletSpeed((double)e.getBullet().getPower()) * 10.0)) == Math.round((double)(ew.velocity * 10.0)) && ew.fromName == e.getBullet().getName()) {
                    hitWave = ew;
                }
                ++x;
            }
            if (hitWave != null) {
                BulletInfoEnemy.logHit(hitWave, hitBulletLocation, 1.0, this);
                this.paintHitWave(hitWave);
                this.enemyBullets.remove(this.enemyBullets.lastIndexOf((Object)hitWave));
            }
        }
    }

    public void onHitRobot(HitRobotEvent e) {
        this.listenEvent((Event)e);
    }

    public void onHitWall(HitWallEvent e) {
        this.listenEvent((Event)e);
    }

    public void onBulletHit(BulletHitEvent e) {
        Enemy him = (Enemy)enemies.get((Object)e.getName());
        him.damageRecieved += Rules.getBulletDamage((double)e.getBullet().getPower());
        this.listenEvent((Event)e);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (!this.enemyBullets.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(e.getHitBullet().getX(), e.getHitBullet().getY());
            BulletInfoEnemy hitWave = null;
            BulletInfoEnemy ew = null;
            int x = 0;
            while (x < this.enemyBullets.size()) {
                ew = (BulletInfoEnemy)this.enemyBullets.get(x);
                if (ew.distanceTraveled - hitBulletLocation.distance((Point2D)ew.fireLocation) < 50.0 && Math.abs((double)(e.getHitBullet().getVelocity() - ew.velocity)) < 0.006 && ew.fromName == e.getHitBullet().getName()) {
                    hitWave = ew;
                    break;
                }
                ++x;
            }
            if (hitWave != null) {
                BulletInfoEnemy.logHit(hitWave, hitBulletLocation, 0.5, this);
                this.enemyBullets.remove(this.enemyBullets.lastIndexOf((Object)hitWave));
            }
        }
        this.listenEvent((Event)e);
    }

    public void onBulletMissed(BulletMissedEvent e) {
        this.listenEvent((Event)e);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        Enemy him = (Enemy)enemies.get((Object)e.getName());
        him.alive = false;
        if (him.name == this.enemy.name) {
            this.enemy = new Enemy();
        }
        this.selectEnemy.select();
        this.listenEvent((Event)e);
    }

    public void onWin(WinEvent e) {
        this.finishRound();
        this.listenEvent((Event)e);
    }

    public void onDeath(DeathEvent e) {
        this.finishRound();
        this.listenEvent((Event)e);
    }

    public void finishRound() {
        this.out.println();
        System.out.print("Finishes :");
        int[] arrn = finishes;
        int n = this.getOthers();
        arrn[n] = arrn[n] + 1;
        int i = 0;
        while (i < finishes.length) {
            this.out.print(String.valueOf((int)finishes[i]) + " ");
            ++i;
        }
        this.out.println();
        System.out.println("Wall Damage Total :" + wallDamage);
        System.out.println("wall Hits Total :" + wallHits);
        System.out.println("Skipped Turn Total :" + skippedTurns);
        System.out.println(" -===  GunStats  ===-");
        System.out.println("  ");
        if (this.getRoundNum() == 35) {
            System.out.println(" I don't need a big gun, when I'm standing behind you.");
        }
        for (Enemy him : enemies.values()) {
            if (!him.alive) {
                him.alive = true;
            }
            him.timeLastBulletHit = 0L;
        }
        this.out.println();
    }

    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("Skipped Turn time:" + this.getTime() + " , skippedTurn total :" + ++skippedTurns);
        this.listenEvent((Event)e);
    }

    public void onKeyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        if (key == 'w') {
            paintWaves = this.toggleButton(paintWaves);
        }
        if (key == 't') {
            paintTargeting = this.toggleButton(paintTargeting);
        }
        if (key == 'r') {
            paintRadar = this.toggleButton(paintRadar);
        }
        if (key == 'm') {
            paintMovement = this.toggleButton(paintMovement);
        }
        if (key == 'a') {
            this.allButtons(true);
        }
        if (key == 'n') {
            this.allButtons(false);
        }
        this.listenInputEvent((InputEvent)e);
    }

    public boolean toggleButton(boolean button) {
        if (button) {
            return false;
        }
        return true;
    }

    public void allButtons(boolean button) {
        if (button) {
            paintAll = true;
            paintNone = false;
        } else {
            paintAll = false;
            paintNone = true;
        }
        paintWaves = button;
        paintTargeting = button;
        paintMovement = button;
        paintRadar = button;
    }

    public void onPaint(Graphics2D g) {
        if (this.getTime() < 10L) {
            return;
        }
        g.setColor(Color.white);
        g.drawString("  PAINT :    All / None", 20, 5);
        if (paintWaves) {
            BulletInfoEnemy.paintWaves(g, this);
            g.setColor(Color.red);
        } else {
            g.setColor(Color.gray);
        }
        g.drawString("  Waves", 200, 5);
        if (paintTargeting) {
            this.targeting.onPaint(g);
            g.setColor(Color.red);
        } else {
            g.setColor(Color.gray);
        }
        g.drawString("  Targeting", 300, 5);
        if (paintMovement) {
            this.movement.onPaint(g);
            g.setColor(Color.red);
        } else {
            g.setColor(Color.gray);
        }
        g.drawString("  Movement", 400, 5);
        if (paintRadar) {
            this.radar.onPaint(g);
            this.selectEnemy.onPaint(g);
            g.setColor(Color.red);
        } else {
            g.setColor(Color.gray);
        }
        g.drawString("  Radar", 500, 5);
    }

    public void paintHitWave(BulletInfoEnemy w) {
        Graphics2D g = this.getGraphics();
        g.setColor(Color.red);
        g.drawOval((int)(w.fireLocation.x - w.distanceTraveled - 1.0), (int)(w.fireLocation.y - w.distanceTraveled - 1.0), (int)(2.0 * (w.distanceTraveled + 2.0)), (int)(2.0 * (2.0 + w.distanceTraveled)));
    }
}


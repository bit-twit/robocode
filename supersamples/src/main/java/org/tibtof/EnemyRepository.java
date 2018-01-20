package org.tibtof;

import de.metro.robocode.util.Point;

import java.util.Collection;
import java.util.HashMap;

public class EnemyRepository {

    private HashMap<String, Enemy> enemies = new HashMap<>();

    public Enemy findByName(String name) {
        return enemies.computeIfAbsent(name, Enemy::new);
    }

    public Collection<Enemy> findAll() {
        return enemies.values();
    }

    public void remove(String name) {
        enemies.remove(name);
    }

    public Enemy closestTo(Point myLocation) {
        //TODO
        return enemies.get(enemies.keySet().iterator().next());
    }

    public int count() {
        return enemies.size();
    }

}

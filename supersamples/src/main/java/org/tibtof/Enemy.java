package org.tibtof;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Enemy {

    private final String name;
    private Point position;
    private double energy;
    private List<Point> previousPositions = new ArrayList<>();

    public void setPosition(Point position) {
        this.previousPositions.add(this.position);
        this.position = position;
    }

    public Point getFuturePosition() {
        return position;
    }
}

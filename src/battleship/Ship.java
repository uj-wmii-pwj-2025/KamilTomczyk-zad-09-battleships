package battleship;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final List<Coordinate> segments = new ArrayList<>();
    private int hits = 0;

    public void addSegment(int x, int y) {
        segments.add(new Coordinate(x, y));
    }

    public boolean isAt(int x, int y) {
        return segments.stream().anyMatch(c -> c.x == x && c.y == y);
    }

    public boolean registerHit() {
        hits++;
        return isSunk();
    }

    public boolean isSunk() {
        return hits >= segments.size();
    }
}
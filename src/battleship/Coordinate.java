package battleship;

public class Coordinate {
    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {

        char col = (char) ('A' + x);
        return col + String.valueOf(y + 1);
    }

    public static Coordinate parse(String s) {

        s = s.trim();
        int x = s.charAt(0) - 'A';
        int y = Integer.parseInt(s.substring(1)) - 1;
        return new Coordinate(x, y);
    }
}
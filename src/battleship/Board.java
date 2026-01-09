package battleship;

import map.BattleshipGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Board {
    public static final int SIZE = 10;
    private final CellState[][] grid = new CellState[SIZE][SIZE];
    private final List<Ship> ships = new ArrayList<>();
    private final boolean[][] enemyMisses = new boolean[SIZE][SIZE];


    public Board() {
        for (int y = 0; y < SIZE; y++) Arrays.fill(grid[y], CellState.UNKNOWN);
    }

    public Board(String mapPath) throws IOException {
        ensureMapExists(mapPath);
        loadMap(mapPath);
    }

    private void ensureMapExists(String mapPath) throws IOException {
        Path path = Paths.get(mapPath);
        if (!Files.exists(path)) {
            System.out.println("Generating new map to: " + mapPath);
            String raw = BattleshipGenerator.defaultInstance().generateMap();
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < SIZE; i++) {
                lines.add(raw.substring(i * SIZE, (i + 1) * SIZE));
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        }
    }

    private void loadMap(String path) throws IOException {
        for (int y = 0; y < SIZE; y++) Arrays.fill(grid[y], CellState.EMPTY);
        List<String> lines = Files.readAllLines(Paths.get(path));
        boolean[][] visited = new boolean[SIZE][SIZE];


        for (int y = 0; y < Math.min(lines.size(), SIZE); y++) {
            String line = lines.get(y);
            for (int x = 0; x < Math.min(line.length(), SIZE); x++) {
                if (line.charAt(x) == '#') grid[y][x] = CellState.SHIP;
            }
        }

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (grid[y][x] == CellState.SHIP && !visited[y][x]) {
                    Ship ship = new Ship();
                    floodFillShip(x, y, visited, ship);
                    ships.add(ship);
                }
            }
        }
    }

    private void floodFillShip(int x, int y, boolean[][] visited, Ship ship) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
        if (visited[y][x] || grid[y][x] != CellState.SHIP) return;
        visited[y][x] = true;
        ship.addSegment(x, y);
        floodFillShip(x + 1, y, visited, ship); // Right
        floodFillShip(x - 1, y, visited, ship); // Left
        floodFillShip(x, y + 1, visited, ship); // Down
        floodFillShip(x, y - 1, visited, ship); // Up
    }

    public String checkShot(Coordinate c) {
        if (c.x < 0 || c.x >= SIZE || c.y < 0 || c.y >= SIZE) return "pudło";

        CellState current = grid[c.y][c.x];

        if (current == CellState.EMPTY || current == CellState.MISS) {
            enemyMisses[c.y][c.x] = true;
            return "pudło";
        }
        if (current == CellState.SHIP) {
            grid[c.y][c.x] = CellState.HIT;
            for (Ship s : ships) {
                if (s.isAt(c.x, c.y)) {
                    if (s.registerHit()) {
                        return allShipsSunk() ? "ostatni zatopiony" : "trafiony zatopiony";
                    }
                    return "trafiony";
                }
            }
        }

        if (current == CellState.HIT) {
            for (Ship s : ships) {
                if (s.isAt(c.x, c.y)) return s.isSunk() ? "trafiony zatopiony" : "trafiony";
            }
        }
        return "pudło";
    }

    private boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public void setCell(int x, int y, CellState state) {
        grid[y][x] = state;
    }

    public CellState getCell(int x, int y) {
        return grid[y][x];
    }

    public boolean isFull() {
        for (CellState[] row : grid)
            for (CellState cell : row)
                if (cell == CellState.UNKNOWN) return false;
        return true;
    }


    public void surroundSunkShip(int startX, int startY) {
        Queue<Coordinate> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        List<Coordinate> shipParts = new ArrayList<>();

        queue.add(new Coordinate(startX, startY));
        visited.add(startX + "," + startY);

        while (!queue.isEmpty()) {
            Coordinate c = queue.poll();
            shipParts.add(c);
            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                int nx = c.x + d[0], ny = c.y + d[1];
                if (isValid(nx, ny) && grid[ny][nx] == CellState.HIT && !visited.contains(nx + "," + ny)) {
                    visited.add(nx + "," + ny);
                    queue.add(new Coordinate(nx, ny));
                }
            }
        }

        for (Coordinate c : shipParts) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = c.x + dx, ny = c.y + dy;
                    if (isValid(nx, ny) && grid[ny][nx] == CellState.UNKNOWN) {
                        grid[ny][nx] = CellState.MISS;
                    }
                }
            }
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public String renderOwnMap(boolean postGame) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                char c = '.';
                CellState st = grid[y][x];
                if (st == CellState.SHIP) c = '#';
                else if (st == CellState.HIT) c = '@';
                else if (st == CellState.EMPTY && enemyMisses[y][x]) c = '~';
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String renderEnemyMap(boolean revealAll) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                char c = '?';
                CellState st = grid[y][x];
                if (st == CellState.HIT) c = '#';
                else if (st == CellState.MISS) c = '.';
                else if (st == CellState.UNKNOWN && revealAll) c = '.';
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
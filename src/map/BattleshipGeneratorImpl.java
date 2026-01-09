package map;

import java.util.Random;

public class BattleshipGeneratorImpl implements BattleshipGenerator {
    private static final int BOARD_SIZE = 10;
    private static final int TOTAL_CELLS = BOARD_SIZE * BOARD_SIZE;
    private final Random random = new Random();

    @Override
    public String generateMap() {
        char[] board = new char[TOTAL_CELLS];

        // Initialize board with water
        for (int i = 0; i < TOTAL_CELLS; i++) {
            board[i] = '.';
        }

        // Place ships: 1x4, 2x3, 3x2, 4x1
        placeShip(board, 4, 1);
        placeShip(board, 3, 2);
        placeShip(board, 2, 3);
        placeShip(board, 1, 4);

        return new String(board);
    }

    private void placeShip(char[] board, int shipSize, int shipCount) {
        for (int i = 0; i < shipCount; i++) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(BOARD_SIZE);
                int col = random.nextInt(BOARD_SIZE);
                boolean horizontal = random.nextBoolean();

                if (canPlaceShip(board, row, col, shipSize, horizontal)) {
                    placeShipOnBoard(board, row, col, shipSize, horizontal);
                    placed = true;
                }
            }
        }
    }

    private boolean canPlaceShip(char[] board, int startRow, int startCol, int size, boolean horizontal) {
        if (horizontal) {
            if (startCol + size > BOARD_SIZE) return false;
        } else {
            if (startRow + size > BOARD_SIZE) return false;
        }

        for (int i = 0; i < size; i++) {
            int row = horizontal ? startRow : startRow + i;
            int col = horizontal ? startCol + i : startCol;
            int index = row * BOARD_SIZE + col;

            if (board[index] != '.') return false;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int adjRow = row + dr;
                    int adjCol = col + dc;

                    if (adjRow >= 0 && adjRow < BOARD_SIZE && adjCol >= 0 && adjCol < BOARD_SIZE) {
                        int adjIndex = adjRow * BOARD_SIZE + adjCol;
                        if (board[adjIndex] != '.') return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeShipOnBoard(char[] board, int startRow, int startCol, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int row = horizontal ? startRow : startRow + i;
            int col = horizontal ? startCol + i : startCol;
            int index = row * BOARD_SIZE + col;
            board[index] = '#';
        }
    }

    public static BattleshipGenerator defaultInstance() {
        return new BattleshipGeneratorImpl();
    }
}
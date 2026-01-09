package battleship;

import java.io.IOException;
import java.util.Random;

public class GameSession {
    private final NetworkManager network;
    private final Board myBoard;
    private final Board enemyBoardTracking;
    private final boolean isServer;
    private final Random random = new Random();
    private String lastSentMessage = "";
    private static final int MAX_RETRIES = 3;

    public GameSession(NetworkManager network, Board myBoard, boolean isServer) {
        this.network = network;
        this.myBoard = myBoard;
        this.isServer = isServer;
        this.enemyBoardTracking = new Board();
    }

    public void run() throws IOException {
        if (!isServer) {
            Coordinate shot = generateShot();
            String cmd = "start;" + shot.toString();
            sendMessage(cmd);
        }

        boolean gameOver = false;
        while (!gameOver) {
            String incomingLine = receiveWithRetry();
            if (incomingLine == null) break;

            System.out.println("Otrzymano: " + incomingLine);
            String[] parts = incomingLine.split(";");
            String command = parts[0];

            if (!command.equals("start")) {
                Coordinate lastShot = Coordinate.parse(lastSentMessage.split(";")[1]);
                processShotResult(command, lastShot);
            }

            if (command.equals("ostatni zatopiony")) {
                handleWin();
                break;
            }

            if (parts.length >= 2) {
                Coordinate enemyShot = Coordinate.parse(parts[1]);
                String responseCmd = myBoard.checkShot(enemyShot);

                if (responseCmd.equals("ostatni zatopiony")) {
                    sendMessage(responseCmd);
                    handleLoss();
                    break;
                } else {
                    Coordinate nextShot = generateShot();
                    sendMessage(responseCmd + ";" + nextShot.toString());
                }
            }
        }
    }

    private void sendMessage(String msg) {
        System.out.println("Wysyłano: " + msg);
        network.send(msg);
        lastSentMessage = msg;
    }

    private String receiveWithRetry() {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                String line = network.receive();
                if (line == null) throw new IOException("Connection closed");
                if (!line.startsWith("start") && !line.startsWith("pudło") &&
                        !line.startsWith("trafiony") && !line.startsWith("ostatni")) {
                    throw new IOException("Invalid protocol");
                }
                return line;
            } catch (IOException e) {
                attempts++;
                if (attempts < MAX_RETRIES) {
                    System.out.println("Błąd/Timeout. Ponawiam (" + attempts + "/" + MAX_RETRIES + ")...");
                    if (!lastSentMessage.isEmpty()) network.send(lastSentMessage);
                }
            }
        }
        System.out.println("Błąd komunikacji");
        System.exit(0);
        return null;
    }

    private Coordinate generateShot() {
        while (true) {
            int x = random.nextInt(Board.SIZE);
            int y = random.nextInt(Board.SIZE);
            if (enemyBoardTracking.getCell(x, y) == CellState.UNKNOWN) return new Coordinate(x, y);
            if (enemyBoardTracking.isFull()) return new Coordinate(x, y);
        }
    }

    private void processShotResult(String command, Coordinate c) {
        CellState state = CellState.MISS;
        if (command.contains("trafiony")) state = CellState.HIT;

        enemyBoardTracking.setCell(c.x, c.y, state);
        if (command.contains("zatopiony")) enemyBoardTracking.surroundSunkShip(c.x, c.y);
    }

    private void handleWin() {
        System.out.println("Wygrana");
        System.out.println(enemyBoardTracking.renderEnemyMap(true));
        System.out.println("\n" + myBoard.renderOwnMap(true));
    }

    private void handleLoss() {
        System.out.println("Przegrana");
        System.out.println(enemyBoardTracking.renderEnemyMap(false));
        System.out.println("\n" + myBoard.renderOwnMap(true));
    }
}
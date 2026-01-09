package battleship;

public class BattleshipGame {

    public static void main(String[] args) {
        try {
            Config config = new Config(args);

            Board myBoard = new Board(config.mapPath);

            System.out.println("Moja mapa:");
            System.out.println(myBoard.renderOwnMap(false));

            try (NetworkManager network = new NetworkManager()) {
                network.connect(config.isServer, config.host, config.port);

                GameSession session = new GameSession(network, myBoard, config.isServer);
                session.run();
            }
        } catch (Exception e) {
            System.err.println("Błąd krytyczny: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static class Config {
        boolean isServer;
        int port;
        String host;
        String mapPath;

        Config(String[] args) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-mode" -> isServer = args[++i].equalsIgnoreCase("server");
                    case "-port" -> port = Integer.parseInt(args[++i]);
                    case "-host" -> host = args[++i];
                    case "-map"  -> mapPath = args[++i];
                }
            }
        }
    }
}
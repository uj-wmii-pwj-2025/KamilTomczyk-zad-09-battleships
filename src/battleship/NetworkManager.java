package battleship;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class NetworkManager implements AutoCloseable {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public void connect(boolean isServer, String host, int port) throws IOException {
        if (isServer) {
            System.out.println("Oczekiwanie na połączenie na porcie " + port + "...");
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            System.out.println("Klient połączony.");
        } else {
            System.out.println("Łączenie do " + host + ":" + port + "...");
            socket = new Socket(host, port);
            System.out.println("Połączono.");
        }
        socket.setSoTimeout(1000);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void send(String msg) {
        writer.print(msg);
        if (!msg.endsWith("\n")) writer.print("\n");
        writer.flush();
    }

    public String receive() throws IOException {
        return reader.readLine();
    }

    @Override
    public void close() throws Exception {
        if (socket != null) socket.close();
        if (serverSocket != null) serverSocket.close();
    }
}
package architectureTest.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class ThreadPerClientServer extends Server {

    private ServerSocket serverSocket;

    public ThreadPerClientServer(int port) {
        super(port);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Failed to create socket on port " + PORT);
            return;
        }
        while (true) {
            Socket socket;
            Instant gotRequestTime;
            try {
                socket = serverSocket.accept();
                gotRequestTime = Instant.now();
            } catch (IOException e) {
                System.out.println("Failed to accept connection");
                return;
            }
            ClientRequestHandler handler = new ClientRequestHandler(socket, stat, gotRequestTime);
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
        }
    }

    @Override
    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Failed to close server socket");
        }
    }
}

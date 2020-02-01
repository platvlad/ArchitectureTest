package architectureTest.server;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class Server implements Runnable {
    protected ServerStat stat = new ServerStat();
    protected int PORT = 8080;
    protected ServerSocket serverSocket;

    public Server(int port) {
        PORT = port;
    }

    @Override
    public abstract void run();

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to close server socket");
            }
        }
    }
}

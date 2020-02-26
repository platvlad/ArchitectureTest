package architectureTest.server.threadPerClient;

import architectureTest.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadPerClientServer extends Server {

    public ThreadPerClientServer(int port, int numClients) {
        super(port, numClients);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Failed to create socket on port " + PORT);
            return;
        }
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                stat.finish = true;
                stat.valid = false;
                break;
            }
            if (socket != null) {
                ClientHandler handler = new ClientHandler(socket, stat, startLatch);
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
    }
}

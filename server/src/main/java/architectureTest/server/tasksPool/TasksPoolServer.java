package architectureTest.server.tasksPool;

import architectureTest.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TasksPoolServer extends Server {

    public TasksPoolServer(int port, int numClients) {
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
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
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

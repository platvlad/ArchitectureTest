package architectureTest.server;

import architectureTest.server.nonBlocking.NonBlockingServer;
import architectureTest.server.tasksPool.TasksPoolServer;
import architectureTest.server.threadPerClient.ThreadPerClientServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public abstract class Server implements Runnable {
    protected ServerStat stat = new ServerStat();
    protected int PORT;
    protected volatile ServerSocket serverSocket;
    protected CountDownLatch startLatch;

    public Server(int port, int numClients) {
        PORT = port;
        startLatch = new CountDownLatch(numClients);
    }

    @Override
    public abstract void run();

    public void stop(Thread thread) {
        while (serverSocket == null);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to close server socket");
            }
        }
    }

    public static Map.Entry<Server, Thread> startServer(ServerParameters params) {
        int port = params.getPort();
        int numClients = params.getNumClients();
        Server server;
        switch (params.getArchitecture()) {
            case THREAD_PER_CLIENT:
                server = new ThreadPerClientServer(port, numClients);
                break;
            case TASKS_POOL:
                server = new TasksPoolServer(port, numClients);
                break;
            default:
                server = new NonBlockingServer(port, numClients);
                break;
        }
        Thread serverThread = new Thread(server);
        serverThread.start();
        return new AbstractMap.SimpleEntry<>(server, serverThread);
    }
}

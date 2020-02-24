package architectureTest.server.tasksPool;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.server.ServerStat;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private Socket socket;

    private ServerStat stat;

    private CountDownLatch startLatch;

    public ClientHandler(Socket socket, ServerStat stat, CountDownLatch startLatch) {
        this.socket = socket;
        this.stat = stat;
        this.startLatch = startLatch;
    }

    @Override
    public void run() {
        ExecutorService sendPool = Executors.newSingleThreadExecutor();
        ExecutorService sortPool = Executors.newFixedThreadPool(4);
        while (true) {
            Request request;
            try {
                request = Network.parseRequest(socket);
                if (request == null) {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Failed to handle request input");
                break;
            }
            Instant gotRequestTime = Instant.now();
            sortPool.submit(new RequestHandler(request, stat, startLatch, sendPool, socket, gotRequestTime));
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
        sortPool.shutdown();
        sendPool.shutdown();
    }
}

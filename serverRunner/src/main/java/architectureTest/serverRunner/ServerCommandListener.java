package architectureTest.serverRunner;

import architectureTest.protobuf.ServerCodeOuterClass.ServerCode;
import architectureTest.server.Server;
import architectureTest.server.ServerParameters;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ServerCommandListener implements Runnable {

    private ServerSocket serverSocket;

    private Server server;
    private Thread serverThread;

    @Override
    public void run() {
        int port = 8081;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Failed to create socket on port " + port);
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
                InputStream input;
                ServerCode serverCode;
                try {
                    input  = socket.getInputStream();
                    serverCode = ServerCode.parseDelimitedFrom(input);
                } catch (IOException e) {
                    System.out.println("Failed to get or parse input");
                    continue;
                }
                String[] params = null;
                switch (serverCode.getCode()) {
                    case 1:
                        params = new String[]{"-a", "thread_per_client", "-p", "8080"};
                        break;
                    case 2:
                        params = new String[]{"-a", "tasks_pool", "-p", "8080"};
                        break;
                    case 3:
                        params = new String[]{"-a", "non_blocking", "-p", "8080"};
                        break;
                    default:
                        break;
                }
                if (params == null) {
                    if (server != null) {
                        server.stop(serverThread);
                        try {
                            serverThread.join();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                } else {
                    ServerParameters serverParameters = new ServerParameters();
                    try {
                        serverParameters.parseArgs(params);
                    } catch (ParseException e) {
                        System.out.println("Failed to parse parameters");
                    }
                    Map.Entry<Server, Thread> serverThreadEntry = Server.startServer(serverParameters);
                    server = serverThreadEntry.getKey();
                    serverThread = serverThreadEntry.getValue();
                }
                try {
                    serverCode.writeDelimitedTo(socket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Failed to send response");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Failed to close socket");
                }
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
    }

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

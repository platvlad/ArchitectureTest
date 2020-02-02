package architectureTest.server;

import architectureTest.server.nonBlocking.NonBlockingServer;
import architectureTest.server.tasksPool.TasksPoolServer;
import architectureTest.server.threadPerClient.ThreadPerClientServer;
import org.apache.commons.cli.ParseException;

import java.util.Scanner;

public class Main {
    public static void waitQuit() {
        Scanner scanner = new Scanner(System.in).useDelimiter("[^\r\n]");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("quit")) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        ServerParameters params = new ServerParameters();
        try {
            params.parseArgs(args);
        } catch (ParseException e) {
            return;
        }
        int port = params.getPort();
        Server server;
        switch (params.getArchitecture()) {
            case THREAD_PER_CLIENT:
                server = new ThreadPerClientServer(port);
                break;
            case TASKS_POOL:
                server = new TasksPoolServer(port);
                break;
            default:
                server = new NonBlockingServer(port);
                break;
        }
        Thread serverThread = new Thread(server);
        serverThread.start();
        waitQuit();
        server.stop();
    }
}

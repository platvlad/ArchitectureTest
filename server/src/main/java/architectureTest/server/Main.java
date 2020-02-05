package architectureTest.server;

import architectureTest.server.nonBlocking.NonBlockingServer;
import architectureTest.server.tasksPool.TasksPoolServer;
import architectureTest.server.threadPerClient.ThreadPerClientServer;
import org.apache.commons.cli.ParseException;

import java.util.Map;
import java.util.Scanner;

import static architectureTest.server.Server.startServer;

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
        Map.Entry<Server, Thread> serverThreadEntry = startServer(params);
        Server server = serverThreadEntry.getKey();
        Thread thread = serverThreadEntry.getValue();
        waitQuit();
        server.stop(thread);
    }
}

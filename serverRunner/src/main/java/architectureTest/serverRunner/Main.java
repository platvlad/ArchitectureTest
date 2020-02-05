package architectureTest.serverRunner;

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
        ServerCommandListener listener = new ServerCommandListener();
        Thread listenerThread = new Thread(listener);
        listenerThread.start();
        waitQuit();
        listener.stop();
    }
}

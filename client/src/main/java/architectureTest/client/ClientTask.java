package architectureTest.client;

import ArchitectureTest.utils.Network;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClientTask implements Runnable {

    private ClientParameters params;

    public ClientTask(ClientParameters params) {
        this.params = params;
    }

    private List<Long> generateList(int size) {
        Random random = new Random();
        return random.longs().limit(size).boxed().collect(Collectors.toList());
    }

    private void genSortArray(Socket socket, int listSize) throws IOException {
        List<Long> elems = generateList(listSize);
        try {
            Network.sendArray(socket, elems);
        } catch (IOException e) {
            System.out.println("Failed to send array");
            throw e;
        }
        try {
            Network.parseRequest(socket);
        } catch (IOException e) {
            System.out.println("Failed to get sort results");
            throw e;
        }
    }

    @Override
    public void run() {
        int numRequests = params.getNumRequests();
        String host = params.getServerIP();
        int port = params.getPort();
        try (Socket socket = new Socket(host, port)) {
            int listSize = params.getNumElems();
            long waitTime = params.getDelta();
            for (int i = 0; i < numRequests; i++) {
                try {
                    genSortArray(socket, listSize);
                } catch (IOException e) {
                    return;
                }
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
            try {
                Network.sendFinishSign(socket);
                Network.parseRequest(socket);
            } catch (IOException e) {
                System.out.println("Failed to send finish sign");
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to " + host);
        }

    }
}

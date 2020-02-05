package architectureTest.client;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClientTask implements Runnable {

    private ClientParameters params;

    private volatile double avgRequestTime;

    private List<Duration> durations = new ArrayList<>();

    public ClientTask(ClientParameters params) {
        this.params = params;
    }

    private List<Long> generateList(int size) {
        Random random = new Random();
        return random.longs().limit(size).boxed().map(x -> x % 100).collect(Collectors.toList());
    }

    private void checkAnswer(List<Long> elems, Request answer) {
        if (answer == null) {
            System.out.println(false);
            return;
        }
        elems.sort(Long::compareTo);
        List<Long> answerList = answer.getElemsList();

        boolean correct = elems.size() == answerList.size();
        if (!correct) {
            System.out.println(false);
            return;
        }
        Iterator<Long> elemsIter = elems.iterator();
        Iterator<Long> answerIter = answerList.iterator();
        while (elemsIter.hasNext()) {
            long elem = elemsIter.next();
            long answerElem = answerIter.next();
            if (elem != answerElem) {
                correct = false;
            }
        }
        System.out.println(correct);
    }

    private void genSortArray(Socket socket, int listSize) throws IOException {
        List<Long> elems = generateList(listSize);
        Instant start = Instant.now();
        try {
            Network.sendArray(socket, elems);
        } catch (IOException e) {
            System.out.println("Failed to send array");
            throw e;
        }
        try {
            Request result = Network.parseRequest(socket);
        } catch (IOException e) {
            System.out.println("Failed to get sort results");
            throw e;
        }
        Instant end = Instant.now();
        durations.add(Duration.between(start, end));
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
            avgRequestTime = durations.stream().mapToDouble(Duration::toMillis).average().orElse(0);
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

    public double getAvgRequestTime() {
        return avgRequestTime;
    }
}

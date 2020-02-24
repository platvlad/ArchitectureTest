package architectureTest.server.tasksPool;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.server.ServerStat;
import architectureTest.server.Sorter;
import com.google.protobuf.Message;

import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class RequestHandler implements Runnable {

    private Request request;
    private ServerStat stat;
    private CountDownLatch startLatch;
    private ExecutorService sendPool;
    private Socket socket;
    private Instant gotRequestTime;

    public RequestHandler(
            Request request,
            ServerStat stat,
            CountDownLatch startLatch,
            ExecutorService sendPool,
            Socket socket,
            Instant gotRequestTime) {
        this.request = request;
        this.stat = stat;
        this.startLatch = startLatch;
        this.sendPool = sendPool;
        this.socket = socket;
        this.gotRequestTime = gotRequestTime;
    }

    private void processArraySortingRequest(Request request) {
        List<Long> elemsList = request.getElemsList();
        Instant startSorting = Instant.now();
        Long[] elemsArray = elemsList.toArray(new Long[0]);
        Sorter.sort(elemsArray);
        Instant endSorting = Instant.now();
        if (!stat.finish) {
            stat.sortTimes.add(Duration.between(startSorting, endSorting));
        }
        Request toSend = Network.packArray(Arrays.asList(elemsArray));
        sendPool.submit(new ResponseSender(socket, toSend, stat, gotRequestTime));
    }

    @Override
    public void run() {
        int code = request.getCode();
        Message response;
        switch (code) {
            case 0:
                response = stat.setFinishFlag();
                sendPool.submit(new ResponseSender(socket, response));
                break;
            case 1:
                response = stat.buildResponse();
                sendPool.submit(new ResponseSender(socket, response));
                break;
            case 2:
                processArraySortingRequest(request);
                break;
            case 3:
                startLatch.countDown();
                while (startLatch.getCount() > 0) {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while waiting for clients");
                        return;
                    }
                }
                response = Network.makeSignMessage(3);
                sendPool.submit(new ResponseSender(socket, response));
                break;
        }
    }
}

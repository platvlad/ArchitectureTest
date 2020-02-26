package architectureTest.server.nonBlocking;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.server.ServerStat;
import architectureTest.server.Sorter;
import com.google.protobuf.Message;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class RequestHandler implements Runnable {
    private Request request;
    private ServerStat stat;
    private ClientBuffers buffers;

    public RequestHandler(Request request, ServerStat stat, ClientBuffers buffers) {
        this.request = request;
        this.stat = stat;
        this.buffers = buffers;
    }

    private Request processArraySortingRequest(Request request) {
        List<Long> elemsList = request.getElemsList();
        Instant startSorting = Instant.now();
        Long[] elemsArray = elemsList.toArray(new Long[0]);
        Sorter.sort(elemsArray);
        Instant endSorting = Instant.now();
        if (!stat.finish) {
            stat.sortTimes.add(Duration.between(startSorting, endSorting));
        }
        return Network.packArray(Arrays.asList(elemsArray));
    }

    @Override
    public void run() {
        int code = request.getCode();
        Message response;
        boolean needWriteDuration = false;
        switch (code) {
            case 0:
                response = stat.setFinishFlag();
                break;
            case 1:
                response = stat.buildResponse();
                break;
            case 2:
                response = processArraySortingRequest(request);
                needWriteDuration = true;
                break;
            default:
                CountDownLatch startLatch = buffers.getStartLatch();
                startLatch.countDown();
                while (startLatch.getCount() > 0) {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while waiting for clients");
                        stat.setNotValid();
                        return;
                    }
                }
                response = Network.makeSignMessage(3);
                break;
        }
        try {
            buffers.setMessageToSend(response, needWriteDuration);
        } catch (IOException e) {
            stat.setNotValid();
            System.out.println("Failed to send response");
        }
    }
}

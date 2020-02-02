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
        switch (code) {
            case 0:
                response = stat.setFinishFlag();
                break;
            case 1:
                response = stat.buildResponse();
                break;
            default:
                response = processArraySortingRequest(request);
                break;
        }
        try {
            buffers.setMessageToSend(response);
        } catch (IOException e) {
            System.out.println("Failed to send response");
        }
    }
}

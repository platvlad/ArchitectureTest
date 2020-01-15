package architectureTest.server;

import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;
import com.google.protobuf.Message;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ClientRequestHandler implements Runnable {

    private Socket socket;

    private ServerStat stat;

    private Instant startProcess;

    public ClientRequestHandler(Socket socket, ServerStat stat, Instant gotRequest) {
        this.socket = socket;
        this.stat = stat;
        startProcess = gotRequest;
    }

    private Request parseRequest() throws IOException {
        Request request;
        InputStream input = socket.getInputStream();
        ObjectInputStream objectInput = new ObjectInputStream(input);
        int messageSize = objectInput.readInt();
        byte[] message = new byte[messageSize];
        int offset = 0;
        while (offset < messageSize) {
            int readBytes = input.read(message, offset, messageSize - offset);
            offset += readBytes;
        }
        request = Request.parseFrom(message);
        return request;
    }

    private void sendMessage(Message msg, OutputStream output) throws IOException {
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.writeInt(msg.getSerializedSize());
        msg.writeTo(output);
        output.flush();
    }

    private void sendArray(List<Long> elems) throws IOException {
        OutputStream output = socket.getOutputStream();
        Request.Builder responseBuilder = Request.newBuilder();
        responseBuilder.setCode(2);
        responseBuilder.addAllElems(elems);
        Request response = responseBuilder.build();
        sendMessage(response, output);
    }

    private void processArraySortingRequest(Request request) throws IOException {
        List<Long> arrayElems = request.getElemsList();
        Instant startSorting = Instant.now();
        Sorter.sort(arrayElems);
        Instant endSorting = Instant.now();
        if (!stat.finish) {
            stat.sortTimes.add(Duration.between(startSorting, endSorting));
        }
        sendArray(arrayElems);
        Instant endProcess = Instant.now();
        if (!stat.finish) {
            stat.processTimes.add(Duration.between(startProcess, endProcess));
        }
    }

    private void processStatRequest() throws IOException {
        StatResponse.Builder responseBuilder = StatResponse.newBuilder();
        responseBuilder.setSortAvg(stat.getAvgSortTime());
        responseBuilder.setSortAvg(stat.getAvgProcessTime());
        StatResponse response = responseBuilder.build();
        OutputStream output = socket.getOutputStream();
        sendMessage(response, output);
    }

    @Override
    public void run() {
        Request request;
        try {
            request = parseRequest();
        } catch (IOException e) {
            System.out.println("Failed to handle request input");
            return;
        }

        int code = request.getCode();
        try {
            switch(code) {
                case 0:
                    stat.finish = true;
                    break;
                case 1:
                    processStatRequest();
                    break;
                case 2:
                    processArraySortingRequest(request);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Failed to write to socket");
        }

    }
}

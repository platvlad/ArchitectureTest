package architectureTest.server.threadPerClient;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;
import architectureTest.server.ServerStat;
import architectureTest.server.Sorter;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;

    private ServerStat stat;

    public ClientHandler(Socket socket, ServerStat stat) {
        this.socket = socket;
        this.stat = stat;
    }

    private void processArraySortingRequest(Request request, Instant startProcess) throws IOException {
        List<Long> elemsList = request.getElemsList();
        Instant startSorting = Instant.now();
        Long[] elemsArray = elemsList.toArray(new Long[0]);
        Sorter.sort(elemsArray);
        Instant endSorting = Instant.now();
        if (!stat.finish) {
            stat.sortTimes.add(Duration.between(startSorting, endSorting));
        }

        Network.sendArray(socket, Arrays.asList(elemsArray));
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
        Network.sendMessage(response, output);
    }

    @Override
    public void run() {
        while (true) {
            Request request;
            try {
                request = Network.parseRequest(socket);
                if (request == null) {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Failed to handle request input");
                break;
            }
            Instant startProcess = Instant.now();

            int code = request.getCode();
            try {
                switch (code) {
                    case 0:
                        Request toSend = stat.setFinishFlag();
                        Network.sendMessage(toSend, socket.getOutputStream());
                        break;
                    case 1:
                        StatResponse response = stat.buildResponse();
                        Network.sendMessage(response, socket.getOutputStream());
                        break;
                    case 2:
                        processArraySortingRequest(request, startProcess);
                        break;
                }
            } catch (IOException e) {
                System.out.println("Failed to write to socket");
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
    }
}

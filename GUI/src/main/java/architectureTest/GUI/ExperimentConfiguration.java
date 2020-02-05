package architectureTest.GUI;

import ArchitectureTest.utils.Network;
import architectureTest.client.ClientParameters;
import architectureTest.client.ClientTask;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.ServerCodeOuterClass.ServerCode;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentConfiguration {

    public static class Parameter {
        private int fixedValue;
        private int minValue;
        private int maxValue;
        private int step;

        @Override
        public String toString() {
            return "fixedValue = " + fixedValue + "; minValue = " + minValue + "; maxValue = " + maxValue + "; step = " + step;
        }
    }

    private Map<String, Parameter> parameters = new HashMap<>();

    public ExperimentConfiguration copy() {
        ExperimentConfiguration result = new ExperimentConfiguration();
        for (Map.Entry<String, Parameter> entry: parameters.entrySet()) {
            result.addParameter(entry.getKey());
            Parameter newParam = result.parameters.get(entry.getKey());
            Parameter copiedParam = entry.getValue();
            newParam.fixedValue = copiedParam.fixedValue;
            newParam.minValue = copiedParam.minValue;
            newParam.maxValue = copiedParam.maxValue;
            newParam.step = copiedParam.step;
        }
        return result;
    }

    public void setMinParameter(String paramName, int value) {
        parameters.get(paramName).minValue = value;
        //System.out.println(parameters.get(paramName));
    }

    public void setMaxParameter(String paramName, int value) {
        parameters.get(paramName).maxValue = value;
        //System.out.println(parameters.get(paramName));
    }

    public void setStepParameter(String paramName, String value) {

        parameters.get(paramName).step = Integer.parseInt(value);
        //System.out.println(parameters.get(paramName));
    }

    public void setFixedParameter(String paramName, String value) {
        parameters.get(paramName).fixedValue = Integer.parseInt(value);
        //System.out.println(parameters.get(paramName));
    }

    public void addParameter(String name) {
        parameters.put(name, new Parameter());
    }

    public void runExperiment(String archType, String floatingParamName) {
        System.out.println("Start running experiment");
        System.out.println(parameters);
        Parameter floatingParam = parameters.get(floatingParamName);
        floatingParam.fixedValue = floatingParam.minValue;
        while (floatingParam.fixedValue <= floatingParam.maxValue) {
            StatResponse serverStat;
            try {
                serverStat = runTest(archType);
            } catch (IOException e) {
                System.out.println("Failed to run test");
                return;
            }
            System.out.println(serverStat.getSortAvg() + " " + serverStat.getProcessAvg());
            floatingParam.fixedValue += floatingParam.step;
        }
    }

    private void runServer(int code, String hostName) throws IOException {
        ServerCode.Builder serverCodeBuilder = ServerCode.newBuilder();
        serverCodeBuilder.setCode(code);
        ServerCode serverCode = serverCodeBuilder.build();
        Socket socket = new Socket(hostName, 8081);
        serverCode.writeDelimitedTo(socket.getOutputStream());
        ServerCode.parseDelimitedFrom(socket.getInputStream());
    }

    private ClientTask createClientTask(String host, int numElems, int delta, int requestsNumber) throws ParseException {
        String[] clientOptions = {"-n", Integer.toString(numElems),
                "-d", Integer.toString(delta),
                "-r", Integer.toString(requestsNumber),
                "-s", host,
                "-p", Integer.toString(8080)};
        ClientParameters clientParams = new ClientParameters();
        clientParams.parseArgs(clientOptions);
        return new ClientTask(clientParams);
    }

    private StatResponse getServerStat(String hostName) throws IOException {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setCode(1);
        requestBuilder.addAllElems(new ArrayList<>());
        Request request = requestBuilder.build();
        Socket socket = new Socket(hostName, 8080);
        Network.sendMessage(request, socket.getOutputStream());
        return StatResponse.newBuilder().build();
        //return Network.parseStatResponse(socket);
    }

    private StatResponse runTest(String archType) throws IOException {
        int serverCode = 1;
        if (archType.equals("Tasks pool")) {
            serverCode = 2;
        } else if (archType.equals("Non-blocking")) {
            serverCode = 3;
        }
        String ip;
        List<String> ipFileLines = Files.readAllLines(Paths.get("ip.txt"));
        ip = ipFileLines.get(0);
        runServer(serverCode, ip);

        int numElems = parameters.get("Number of elements").fixedValue;
        int numClients = parameters.get("Number of clients").fixedValue;
        int delta = parameters.get("delta").fixedValue;
        int requestsNumber = parameters.get("requests number").fixedValue;


        List<Thread> clients = new ArrayList<>();
        ClientTask task;
        try {
            task = createClientTask(ip, numElems, delta, requestsNumber);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IOException("Failed to create client options");
        }
        for (int i = 0; i < numClients; i++) {
            Thread thread = new Thread(task);
            thread.start();
            clients.add(thread);
        }
        System.out.println(archType);
        for (Thread thread: clients) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IOException("Failed to join clients");
            }
        }
        System.out.println();

        //StatResponse statResponse = getServerStat(ip);
        // stop server
        runServer(0, ip);

        StatResponse.Builder response = StatResponse.newBuilder();
        response.setProcessAvg(5);
        response.setSortAvg(3);
        return response.build();
        //return statResponse;
    }

}

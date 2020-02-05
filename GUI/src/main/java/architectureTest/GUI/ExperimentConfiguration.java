package architectureTest.GUI;

import ArchitectureTest.utils.Network;
import architectureTest.client.ClientParameters;
import architectureTest.client.ClientTask;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.ServerCodeOuterClass.ServerCode;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;
import org.apache.commons.cli.ParseException;
import org.jfree.chart.ChartUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExperimentConfiguration {

    public static class Parameter {
        private int fixedValue;
        private int minValue;
        private int maxValue;
        private int step;

        @Override
        public String toString() {
            return "fixedValue = " + fixedValue +
                    "; minValue = " + minValue +
                    "; maxValue = " + maxValue +
                    "; step = " + step;
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
    }

    public void setMaxParameter(String paramName, int value) {
        parameters.get(paramName).maxValue = value;
    }

    public void setStepParameter(String paramName, String value) {

        parameters.get(paramName).step = Integer.parseInt(value);
    }

    public void setFixedParameter(String paramName, String value) {
        parameters.get(paramName).fixedValue = Integer.parseInt(value);
    }

    public void addParameter(String name) {
        parameters.put(name, new Parameter());
    }

    private void addParameterValue(List<Stat> stat, List<String> descStrings, String paramName, int minParam, int maxParam) {
        String str = paramName + ": ";
        if (minParam != maxParam) {
            str += "from " + minParam + " to " + maxParam;
            descStrings.add(str);
            descStrings.add("step: " + stat.get(0).step);
        } else {
            str += minParam;
            descStrings.add(str);
        }
    }

    private void saveDescriptionFile(List<Stat> stat, String fileName) throws IOException {
        Stat firstTestStat = stat.get(0);
        Stat lastTestStat = stat.get(stat.size() - 1);
        List<String> descriptionLines = new ArrayList<>();
        descriptionLines.add("Architecture: " + stat.get(0).architecture);
        addParameterValue(stat, descriptionLines, "Number of requests", firstTestStat.numRequests, lastTestStat.numRequests);
        addParameterValue(stat, descriptionLines, "Number of elements", firstTestStat.numElements, lastTestStat.numElements);
        addParameterValue(stat, descriptionLines, "Number of clients", firstTestStat.numClients, lastTestStat.numClients);
        addParameterValue(stat, descriptionLines, "Delta", firstTestStat.delta, lastTestStat.delta);
        Files.write(Paths.get(fileName), descriptionLines);
    }

    private void saveStat(List<Stat> stat, String floatingParameter, List<Integer> xValues)  {
        try {
            saveDescriptionFile(stat, "output/description.txt");
        } catch (IOException e) {
            System.out.println("Failed to save description file");
        }
        List<Double> sortTimes = stat.stream().map(Stat::getAvgSortTimes).collect(Collectors.toList());
        List<Double> processTimes = stat.stream().map(Stat::getAvgProcessTime).collect(Collectors.toList());
        List<Double> clientTimes = stat.stream().map(Stat::getAvgClientTime).collect(Collectors.toList());

        List<String> sortTimesStrings = sortTimes.stream().map(Object::toString).collect(Collectors.toList());
        List<String> processTimesStrings = processTimes.stream().map(Object::toString).collect(Collectors.toList());
        List<String> clientTimesStrings = clientTimes.stream().map(Object::toString).collect(Collectors.toList());

        try {
        Files.write(Paths.get("output/sort_times.txt"), sortTimesStrings);
        Files.write(Paths.get("output/process_times.txt"), processTimesStrings);
        Files.write(Paths.get("output/client_times.txt"), clientTimesStrings);
        } catch (IOException e) {
            System.out.println("Failed to save stat to file");
        }

        Chart chart = new Chart(stat.get(0).architecture, xValues, sortTimes, processTimes, clientTimes, floatingParameter);
        chart.setVisible(true);

        try (FileOutputStream fileOutput = new FileOutputStream("output/chart.png")) {
            ChartUtils.writeChartAsPNG(fileOutput, chart.getJfreeChart(), 600, 600);
        } catch (FileNotFoundException e) {
            System.out.println("File output/chart.png cannot be created");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runExperiment(String archType, String floatingParamName) {
        Parameter floatingParam = parameters.get(floatingParamName);
        floatingParam.fixedValue = floatingParam.minValue;
        int step = floatingParam.step;
        List<Stat> stats = new ArrayList<>();
        List<Integer> xValues = new ArrayList<>();
        while (floatingParam.fixedValue <= floatingParam.maxValue) {
            try {
                stats.add(runTest(archType));
            } catch (IOException e) {
                return;
            }
            xValues.add(floatingParam.fixedValue);
            floatingParam.fixedValue += floatingParam.step;
        }
        for (Stat stat: stats) {
            stat.step = step;
        }
        saveStat(stats, floatingParamName, xValues);
    }

    private void runServer(int code, String hostName) throws IOException {
        ServerCode.Builder serverCodeBuilder = ServerCode.newBuilder();
        serverCodeBuilder.setCode(code);
        ServerCode serverCode = serverCodeBuilder.build();
        Socket socket = new Socket(hostName, 8081);
        serverCode.writeDelimitedTo(socket.getOutputStream());
        ServerCode.parseDelimitedFrom(socket.getInputStream());
        socket.close();
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
        StatResponse response = Network.parseStatResponse(socket);
        socket.close();
        return response;
    }

    private Stat runTest(String archType) throws IOException {
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
        List<ClientTask> tasks = new ArrayList<>();

        for (int i = 0; i < numClients; i++) {
            ClientTask task;
            try {
                task = createClientTask(ip, numElems, delta, requestsNumber);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IOException("Failed to create client options");
            }
            Thread thread = new Thread(task);
            thread.start();
            tasks.add(task);
            clients.add(thread);
        }
        for (Thread thread: clients) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IOException("Failed to join clients");
            }
        }

        double clientAvgTime = tasks
                .stream()
                .mapToDouble(ClientTask::getAvgRequestTime)
                .average()
                .orElse(0);

        StatResponse statResponse = getServerStat(ip);
        Stat stat = new Stat();
        stat.avgSortTime = statResponse.getSortAvg();
        stat.avgProcessTime = statResponse.getProcessAvg();
        stat.avgClientTime = clientAvgTime;
        stat.architecture = archType;
        stat.numRequests = requestsNumber;
        stat.numElements = numElems;
        stat.numClients = numClients;
        stat.delta = delta;
        // stop server
        runServer(0, ip);

        return stat;
    }

}

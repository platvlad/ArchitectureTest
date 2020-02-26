package architectureTest.GUI;

import ArchitectureTest.utils.Network;
import architectureTest.client.ClientParameters;
import architectureTest.client.ClientTask;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.ServerConfigOuterClass.ServerConfig;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;
import org.apache.commons.cli.ParseException;
import org.jfree.chart.ChartUtils;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExperimentConfiguration {

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
        String outputDirectory = "output/";
        try {
            saveDescriptionFile(stat, outputDirectory + "description.txt");
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
        Files.write(Paths.get(outputDirectory + "sort_times.txt"), sortTimesStrings);
        Files.write(Paths.get(outputDirectory + "process_times.txt"), processTimesStrings);
        Files.write(Paths.get(outputDirectory + "client_times.txt"), clientTimesStrings);
        } catch (IOException e) {
            System.out.println("Failed to save stat to file");
        }

        Chart chart = new Chart(stat.get(0).architecture, xValues, sortTimes, processTimes, clientTimes, floatingParameter);
        chart.setVisible(true);

        Dimension chartSize = chart.getContentPane().getSize();
        int width = (int) chartSize.getWidth();
        int height = (int) chartSize.getHeight();

        try (FileOutputStream fileOutput = new FileOutputStream(outputDirectory + "chart.png")) {
            ChartUtils.writeChartAsPNG(fileOutput, chart.getJfreeChart(), width, height);
        } catch (FileNotFoundException e) {
            System.out.println("File " + outputDirectory + "chart.png cannot be created");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runExperiment(String archType, String floatingParamName, int minValue, int maxValue, int step, Map<String, Integer> paramValues) {
        List<Stat> stats = new ArrayList<>();
        List<Integer> xValues = new ArrayList<>();
        int counter = 0;
        int floatingParamValue = minValue;
        while (floatingParamValue <= maxValue) {
            paramValues.put(floatingParamName, floatingParamValue);
            try {
                stats.add(runTest(archType, paramValues));
            } catch (IOException e) {
                return;
            }
            xValues.add(floatingParamValue);
            floatingParamValue += step;
            counter++;
            System.out.println("Finished test " + counter);
        }
            for (Stat stat: stats) {
            stat.step = step;
        }
        saveStat(stats, floatingParamName, xValues);
    }

    private void runServer(int code, int numClients, String hostName) throws IOException {
        ServerConfig.Builder serverConfigBuilder = ServerConfig.newBuilder();
        serverConfigBuilder.setCode(code);

        serverConfigBuilder.setNumClients(numClients);
        serverConfigBuilder.setNumClients(numClients);
        ServerConfig serverConfig = serverConfigBuilder.build();
        Socket socket = new Socket(hostName, 8081);
        serverConfig.writeDelimitedTo(socket.getOutputStream());
        ServerConfig.parseDelimitedFrom(socket.getInputStream());
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

    private Stat runTest(String archType, Map<String, Integer> parameters) throws IOException {
        int serverCode = 1;
        if (archType.equals("Tasks pool")) {
            serverCode = 2;
        } else if (archType.equals("Non-blocking")) {
            serverCode = 3;
        }
        int numClients = parameters.get("Number of clients");
        String ip;
        List<String> ipFileLines = Files.readAllLines(Paths.get("ip.txt"));
        ip = ipFileLines.get(0);
        runServer(serverCode, numClients, ip);

        int numElems = parameters.get("Number of elements");
        int delta = parameters.get("delta");
        int requestsNumber = parameters.get("requests number");


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

        for (ClientTask task : tasks) {
            if (!task.isStatValid()) {
                System.out.println("Stat is not valid");
                break;
            }
        }

        double clientAvgTime = tasks
                .stream()
                .mapToDouble(ClientTask::getAvgRequestTime)
                .average()
                .orElse(0);

        StatResponse statResponse = getServerStat(ip);
        if (!statResponse.getValid()) {
            System.out.println("Stat is not valid");
        }
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
        runServer(0, numClients, ip);

        return stat;
    }

}

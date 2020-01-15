package architectureTest.client;

import org.apache.commons.cli.*;

public class ClientParameters {
    public int getNumElems() {
        return numElems;
    }

    public long getDelta() {
        return delta;
    }

    public int getNumRequests() {
        return numRequests;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getPort() {
        return port;
    }

    private int numElems = 10;
    private long delta = 100;
    private int numRequests = 20;
    private String serverIP = "localhost";
    private int port = 8080;

    private int getParam(CommandLine cmd, String optionName, int defaultValue) {
        String paramString = cmd.getOptionValue(optionName, String.valueOf(defaultValue));
        return Integer.parseInt(paramString);
    }

    private long getLongParam(CommandLine cmd, String optionName, long defaultValue) {
        String paramString = cmd.getOptionValue(optionName, String.valueOf(defaultValue));
        return Long.parseLong(paramString);
    }

    public void parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("n", "num_elems", true,"Number of elements in sorted arrays");
        options.addOption("d", "delta", true, "Time interval between arrays in milliseconds");
        options.addOption("r", "requests", true, "Number of sent requests");
        options.addOption("s", "server", true, "Server ip");
        options.addOption("p", "port", true, "server port");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            numElems = getParam(cmd, "n", numElems);
            delta = getLongParam(cmd, "d", delta);
            numRequests = getParam(cmd, "r", numRequests);
            serverIP = cmd.getOptionValue("s", serverIP);
            port = getParam(cmd, "p", port);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            throw e;
        }
    }

}

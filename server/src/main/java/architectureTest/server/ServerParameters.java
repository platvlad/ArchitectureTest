package architectureTest.server;

import org.apache.commons.cli.*;

public class ServerParameters {
    public int getPort() {
        return port;
    }

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    private int port = 8080;
    private ArchitectureType architecture = ArchitectureType.THREAD_PER_CLIENT;

    public enum ArchitectureType {
        THREAD_PER_CLIENT,
        TASKS_POOL,
        NON_BLOCKING
    }

    public void parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("p", "port", true, "Server port");
        options.addOption("a",
                "architecture",
                true,
                "Architecture type (one of thread_per_client, tasks_pool, non_blocking");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            throw e;
        }
        String portString = cmd.getOptionValue("p", String.valueOf(port));
        port = Integer.parseInt(portString);
        String archString = cmd.getOptionValue("a", architecture.name());
        switch(archString) {
            case "tasks_pool":
                architecture = ArchitectureType.TASKS_POOL;
                break;
            case "non_blocking":
                architecture = ArchitectureType.NON_BLOCKING;
                break;
            default:
                architecture = ArchitectureType.THREAD_PER_CLIENT;
                break;
        }
    }
}

package architectureTest.client;

import org.apache.commons.cli.ParseException;

public class Main {
    public static void main(String[] args) {
        ClientParameters parameters = new ClientParameters();
        try {
            parameters.parseArgs(args);
        } catch (ParseException e) {
            return;
        }
        ClientTask task = new ClientTask(parameters);
        task.run();
    }
}

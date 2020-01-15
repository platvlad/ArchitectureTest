package architectureTest.server;

public abstract class Server implements Runnable {
    protected ServerStat stat = new ServerStat();
    protected int PORT = 8080;

    public Server(int port) {
        PORT = port;
    }

    @Override
    public abstract void run();

    public abstract void stop();
}

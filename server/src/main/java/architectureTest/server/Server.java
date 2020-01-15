package architectureTest.server;

public abstract class Server {
    protected ServerStat stat = new ServerStat();

    public abstract void run();
}

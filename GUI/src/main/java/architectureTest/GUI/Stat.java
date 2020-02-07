package architectureTest.GUI;

public class Stat {
    public double avgSortTime;
    public double avgProcessTime;
    public double avgClientTime;

    public String architecture;
    public int numRequests;
    public int numElements;
    public int numClients;
    public int delta;
    public int step;

    public double getAvgSortTimes() {
        return avgSortTime;
    }
    public double getAvgProcessTime() {
        return avgProcessTime;
    }

    public double getAvgClientTime() {
        return avgClientTime;
    }

}

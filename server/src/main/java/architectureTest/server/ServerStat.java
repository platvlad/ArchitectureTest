package architectureTest.server;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerStat {
    public volatile boolean finish;
    public final List<Duration> sortTimes = Collections.synchronizedList(new ArrayList<>());
    public final List<Duration> processTimes = Collections.synchronizedList(new ArrayList<>());

    public double getAvgSortTime() {
        synchronized (sortTimes) {
            return getAvg(sortTimes);
        }
    }

    public double getAvgProcessTime() {
        synchronized (processTimes) {
            return getAvg(processTimes);
        }
    }

    private double getAvg(List<Duration> lst) {
        return lst.stream().map(Duration::toMillis).mapToLong(Long::longValue).average().orElse(0);
    }

}

package architectureTest.server;

import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.protobuf.StatResponseOuterClass.StatResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerStat {
    public volatile boolean finish;
    public volatile boolean valid = true;
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

    public StatResponse buildResponse() {
        StatResponse.Builder responseBuilder = StatResponse.newBuilder();
        responseBuilder.setSortAvg(getAvgSortTime());
        responseBuilder.setProcessAvg(getAvgProcessTime());
        responseBuilder.setValid(valid);
        return responseBuilder.build();
    }

    public Request setFinishFlag() {
        finish = true;
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setCode(0);
        requestBuilder.addAllElems(new ArrayList<>());
        return requestBuilder.build();
    }

    public void setNotValid() {
        finish = true;
        valid = false;
    }

}

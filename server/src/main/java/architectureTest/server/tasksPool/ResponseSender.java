package architectureTest.server.tasksPool;

import ArchitectureTest.utils.Network;
import architectureTest.server.ServerStat;
import com.google.protobuf.Message;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

public class ResponseSender implements Runnable {

    private Message response;
    private Socket socket;
    private ServerStat stat;
    private Instant gotRequestTime;

    public ResponseSender(Socket socket, Message response, ServerStat stat, Instant gotRequestTime) {
        this.socket = socket;
        this.response = response;
        this.stat = stat;
        this.gotRequestTime = gotRequestTime;
    }

    public ResponseSender(Socket socket, Message response) {
        this.socket = socket;
        this.response = response;
    }

    @Override
    public void run() {
        try {
            Network.sendMessage(response, socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Failed to send response");
            stat.setNotValid();
        }
        if (gotRequestTime != null) {
            Instant sentResponseTime = Instant.now();
            if (!stat.finish) {
                stat.processTimes.add(Duration.between(gotRequestTime, sentResponseTime));
            }
        }
    }
}

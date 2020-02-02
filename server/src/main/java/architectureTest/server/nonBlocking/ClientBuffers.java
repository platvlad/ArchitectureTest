package architectureTest.server.nonBlocking;

import ArchitectureTest.utils.Network;
import architectureTest.protobuf.RequestOuterClass.Request;
import architectureTest.server.ServerStat;
import com.google.protobuf.Message;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;

public class ClientBuffers {
    private NonBlockingServer server;
    private SocketChannel socketChannel;

    public ByteBuffer inputSizeBuffer = ByteBuffer.allocate(4);
    public ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
    Instant startRequest;

    private ByteBuffer outputBuffer;

    public ClientBuffers(NonBlockingServer server, SocketChannel socketChannel) {
        this.server = server;
        this.socketChannel = socketChannel;
    }

    public boolean readToBuffer(SelectionKey readKey, Instant gotRequestTime) throws IOException {
        if (inputSizeBuffer.position() == 0) {
            startRequest = gotRequestTime;
        }
        SocketChannel channel = (SocketChannel) readKey.channel();
        if (inputSizeBuffer.hasRemaining()) {
            long bytesRead = channel.read(inputSizeBuffer);
            return bytesRead >= 0;
        }
        inputSizeBuffer.flip();
        byte[] inputSizeArray = new byte[4];
        inputSizeBuffer.get(inputSizeArray);
        int inputSize = Network.byteArrayToInt(inputSizeArray);
        if (inputBuffer.capacity() < inputSize) {
            inputBuffer = ByteBuffer.allocate(inputSize);
        }
        long bytesRead = channel.read(inputBuffer);
        if (bytesRead < 0) {
            return false;
        }
        int inputBufferPosition = inputBuffer.position();
        if (inputBufferPosition >= inputSize) {
            byte[] inputBufferArray = new byte[inputSize];
            inputBuffer.flip();
            inputSizeBuffer.flip();
            inputBuffer.get(inputBufferArray);
            Request request = Request.parseFrom(inputBufferArray);
            ExecutorService sortPool = server.getSortPool();
            ServerStat stat = server.getStat();
            sortPool.submit(new RequestHandler(request, stat, this));
            inputBuffer.clear();
            inputSizeBuffer.clear();
        }
        return true;
    }

    public void setMessageToSend(Message message) throws IOException {
        Selector selector = server.getWriteSelector();
        int requestSize = message.getSerializedSize();
        byte[] requestSizeBytes = Network.intToByteArray(requestSize);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(requestSize + 4);
        arrayOutputStream.write(requestSizeBytes);
        message.writeTo(arrayOutputStream);
        outputBuffer = ByteBuffer.wrap(arrayOutputStream.toByteArray());
        if (socketChannel.keyFor(selector) == null) {
            socketChannel.register(selector, SelectionKey.OP_WRITE, this);
        }
    }

    public void writeOutput(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(outputBuffer);
        if (!outputBuffer.hasRemaining()) {
            Instant sentResponseTime = Instant.now();
            ServerStat stat = server.getStat();
            if (!stat.finish) {
                stat.processTimes.add(Duration.between(startRequest, sentResponseTime));
            }
            key.cancel();
        }
    }
}

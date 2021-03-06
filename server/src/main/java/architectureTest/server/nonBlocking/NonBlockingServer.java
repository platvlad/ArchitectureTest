package architectureTest.server.nonBlocking;

import architectureTest.server.Server;
import architectureTest.server.ServerStat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServer extends Server {

    private ReadSelectorListener readSelectorListener;
    private WriteSelectorListener writeSelectorListener;
    private ExecutorService sortPool;
    private int numClients;

    public NonBlockingServer(int port, int numClients) {
        super(port, numClients);
        this.numClients = numClients;
        try {
            readSelectorListener = new ReadSelectorListener(stat);
            writeSelectorListener = new WriteSelectorListener(stat);
        } catch (IOException e) {
            System.out.println("Failed to open selectors");
            stat.setNotValid();
        }
    }

    public ExecutorService getSortPool() {
        return sortPool;
    }

    public ServerStat getStat() {
        return stat;
    }

    public Selector getWriteSelector() {
        return writeSelectorListener.getSelector();
    }

    public WriteSelectorListener getWriteSelectorListener() {
        return writeSelectorListener;
    }

    public CountDownLatch getStartLatch() {
        return startLatch;
    }

    @Override
    public void run() {
        if (readSelectorListener == null || writeSelectorListener == null) {
            return;
        }
        sortPool = Executors.newFixedThreadPool(Math.max(4, numClients));
        Thread readingThread = new Thread(readSelectorListener);
        readingThread.start();
        Thread writingThread = new Thread(writeSelectorListener);
        writingThread.start();
        ServerSocketChannel serverChannel;
        InetSocketAddress serverAddress = new InetSocketAddress(PORT);
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(serverAddress);
        } catch (IOException e) {
            System.out.println("Failed to open server socket channel");
            return;
        }
        serverSocket = serverChannel.socket();
        while (true) {
            SocketChannel socketChannel;
            try {
                socketChannel = serverChannel.accept();
            } catch (IOException e) {
                stat.setNotValid();
                break;
            }
            if (socketChannel != null) {
                try {
                    socketChannel.configureBlocking(false);
                    Selector readSelector = readSelectorListener.getSelector();
                    ClientBuffers channelBuffers = new ClientBuffers(this, socketChannel);
                    readSelectorListener.newChannels.put(socketChannel, channelBuffers);
                    readSelector.wakeup();
                } catch (IOException e) {
                    System.out.println("Failed to configure channel blocking");
                    stat.setNotValid();
                }
            }
        }
        readingThread.interrupt();
        writingThread.interrupt();
        try {
            serverChannel.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
        sortPool.shutdown();
    }
}

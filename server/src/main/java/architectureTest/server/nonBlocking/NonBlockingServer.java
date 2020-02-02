package architectureTest.server.nonBlocking;

import architectureTest.server.Server;
import architectureTest.server.ServerStat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServer extends Server {

    private ReadSelectorListener readSelectorListener;
    private WriteSelectorListener writeSelectorListener;
    private ExecutorService sortPool;

    public NonBlockingServer(int port) {
        super(port);
        try {
            readSelectorListener = new ReadSelectorListener();
            writeSelectorListener = new WriteSelectorListener();
        } catch (IOException e) {
            System.out.println("Failed to open selectors");
        }
    }

    public ExecutorService getSortPool() {
        return sortPool;
    }

    public ServerStat getStat() {
        return stat;
    }

    Selector getWriteSelector() {
        return writeSelectorListener.getSelector();
    }

    @Override
    public void run() {
        if (readSelectorListener == null || writeSelectorListener == null) {
            return;
        }
        Thread readingThread = new Thread(readSelectorListener);
        readingThread.start();
        Thread writingThread = new Thread(writeSelectorListener);
        writingThread.start();
        sortPool = Executors.newFixedThreadPool(4);
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
                break;
            }
            if (socketChannel != null) {
                try {
                    socketChannel.configureBlocking(false);
                    Selector readSelector = readSelectorListener.getSelector();
                    socketChannel.register(readSelector, SelectionKey.OP_READ, new ClientBuffers(this, socketChannel));
                } catch (IOException e) {
                    System.out.println("Failed to configure channel blocking");
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
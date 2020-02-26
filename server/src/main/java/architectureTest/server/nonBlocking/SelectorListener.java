package architectureTest.server.nonBlocking;

import architectureTest.server.ServerStat;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SelectorListener {
    protected Selector selector;

    protected ServerStat stat;

    public ConcurrentMap<SocketChannel, ClientBuffers> newChannels = new ConcurrentHashMap<>();

    public SelectorListener(ServerStat stat) throws IOException {
        selector = Selector.open();
        this.stat = stat;
    }

    public Selector getSelector() {
        return selector;
    }

    protected void registerChannels(int ops) {
        Iterator<Map.Entry<SocketChannel, ClientBuffers>> channelBufferIter = newChannels.entrySet().iterator();
        while (channelBufferIter.hasNext()) {
            Map.Entry<SocketChannel, ClientBuffers> channelBuffer = channelBufferIter.next();
            SocketChannel channel = channelBuffer.getKey();
            ClientBuffers buffers = channelBuffer.getValue();
            try {
                channel.register(selector, ops, buffers);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
                stat.setNotValid();
                continue;
            }
            channelBufferIter.remove();
        }
    }

}

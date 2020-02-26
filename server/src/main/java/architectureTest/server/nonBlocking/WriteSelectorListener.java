package architectureTest.server.nonBlocking;

import architectureTest.server.ServerStat;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;

public class WriteSelectorListener extends SelectorListener implements Runnable {

    public WriteSelectorListener(ServerStat stat) throws IOException {
        super(stat);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                selector.select();
                registerChannels(SelectionKey.OP_WRITE);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isWritable()) {
                        ClientBuffers clientBuffers = (ClientBuffers) key.attachment();
                        clientBuffers.writeOutput(key);
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                stat.setNotValid();
                System.out.println("Failed to write response");
            }
        }
    }
}

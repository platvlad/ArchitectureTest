package architectureTest.server.nonBlocking;

import architectureTest.server.ServerStat;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;

public class ReadSelectorListener extends SelectorListener implements Runnable {

    public ReadSelectorListener(ServerStat stat) throws IOException {
        super(stat);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                System.out.println("Failed to select channels");
                stat.setNotValid();
                continue;
            }

            registerChannels(SelectionKey.OP_READ);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isReadable()) {
                    Instant gotRequestTime = Instant.now();
                    ClientBuffers buffers = (ClientBuffers) key.attachment();
                    try {
                        boolean keepInSelector = buffers.readToBuffer(key, gotRequestTime);
                        if (!keepInSelector) {
                            key.cancel();
                        }
                    } catch (IOException e) {
                        stat.setNotValid();
                        System.out.println("Failed to read to buffer");
                    }
                }
                keyIterator.remove();
            }
        }
    }
}

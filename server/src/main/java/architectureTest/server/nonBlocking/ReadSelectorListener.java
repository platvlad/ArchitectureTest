package architectureTest.server.nonBlocking;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;

public class ReadSelectorListener implements Runnable {

    private Selector selector;

    public ReadSelectorListener() throws IOException {
        selector = Selector.open();
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                selector.selectNow();
            } catch (IOException e) {
                System.out.println("Failed to select channels");
            }
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
                        System.out.println("Failed to read to buffer");
                    }
                }
                keyIterator.remove();
            }
        }
    }
}

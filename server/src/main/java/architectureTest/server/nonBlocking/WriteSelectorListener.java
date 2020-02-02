package architectureTest.server.nonBlocking;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class WriteSelectorListener implements Runnable {

    private Selector selector;

    public WriteSelectorListener() throws IOException {
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
                System.out.println("Failed to write response");
            }
        }
    }
}

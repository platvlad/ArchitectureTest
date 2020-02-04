package architectureTest.GUI;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

public class Application {
    private ExecutorService uiThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService workThreadPool = Executors.newFixedThreadPool(4);

    public void runApplication() {
        uiThreadPool.submit(() -> {
            JFrame frame = new JFrame("Architecture test");
            frame.setBounds(100, 100, 600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            String[] architectures = {"Thread per client", "Tasks pool", "Non-blocking"};
            JComboBox<String> box = new JComboBox<>(architectures);
            frame.add(box, BorderLayout.NORTH);

            JPanel centerPanel = new ParametersPanel();
            frame.add(centerPanel, BorderLayout.WEST);

            StartButtonPanel buttonPanel = new StartButtonPanel();
            frame.add(buttonPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

        });
    }

    public void stopApplication() {
        uiThreadPool.shutdown();
        workThreadPool.shutdown();
    }

}
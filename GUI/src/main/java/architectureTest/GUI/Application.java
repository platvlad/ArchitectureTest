package architectureTest.GUI;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class Application {
    private ExecutorService uiThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService workThreadPool = Executors.newFixedThreadPool(4);
    Configuration config = new Configuration();

    private JComboBox<String> createArchitectureComboBox() {
        String[] architectures = {"Thread per client", "Tasks pool", "Non-blocking"};
        JComboBox<String> box = new JComboBox<>(architectures);
        config.setArchitectureType(Objects.requireNonNull(box.getSelectedItem()).toString());
        box.addItemListener((ItemEvent e) -> {
            config.setArchitectureType((String) e.getItem());
        });
        return box;
    }

    public void runApplication() {
        uiThreadPool.submit(() -> {
            JFrame frame = new JFrame("Architecture test");
            frame.setBounds(100, 100, 600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JComboBox<String> architectureBox = createArchitectureComboBox();
            frame.add(architectureBox, BorderLayout.NORTH);

            JPanel centerPanel = new ParametersPanel(config);
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
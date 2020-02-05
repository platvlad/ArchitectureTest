package architectureTest.GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class Application {
    private ExecutorService uiThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService workThreadPool = Executors.newFixedThreadPool(4);
    final Configuration config = new Configuration();

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

            JPanel buttonPanel = addButtonPanel();
            frame.add(buttonPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

        });
    }

    private JPanel addButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton button = new JButton("Test");
        button.addActionListener((ActionEvent evt) -> {
            String archType;
            String floatingParam;
            ExperimentConfiguration expConfig;
            synchronized (config) {
                archType = config.getArchitectureType();
                floatingParam = config.getFloatingParam();
                expConfig = config.getCurrentExperiment();
            }
            workThreadPool.submit(() -> expConfig.runExperiment(archType, floatingParam));
        });
        buttonPanel.add(button);
        JLabel label = new JLabel();
        buttonPanel.add(label);
        return buttonPanel;
    }

    public void stopApplication() {
        uiThreadPool.shutdown();
        workThreadPool.shutdown();
    }

}
package architectureTest.GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class Application {
    private ExecutorService uiThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService workThreadPool = Executors.newFixedThreadPool(4);
    private JComboBox<String> architectureBox;

    private ParametersPanel centerPanel;

    private JComboBox<String> createArchitectureComboBox() {
        String[] architectures = {"Thread per client", "Tasks pool", "Non-blocking"};

        return new JComboBox<>(architectures);
    }

    public void runApplication() {
        uiThreadPool.submit(() -> {
            JFrame frame = new JFrame("Architecture test");
            frame.setBounds(100, 100, 600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            architectureBox = createArchitectureComboBox();
            frame.add(architectureBox, BorderLayout.NORTH);

            centerPanel = new ParametersPanel();
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
            String archType = (String) architectureBox.getSelectedItem();
            ParametersCard card = centerPanel.getCurrentCard();
            String floatingParam = card.getFloatingParameter();
            int minValue = card.getMinSlider().getValue();
            int maxValue = card.getMaxSlider().getValue();
            int step = Integer.parseInt(card.getStepField().getText());
            Map<String, Integer> paramValues = card.getParamValues();

            ExperimentConfiguration expConfig = new ExperimentConfiguration();

            workThreadPool.submit(() -> expConfig.runExperiment(archType, floatingParam, minValue, maxValue, step, paramValues));
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
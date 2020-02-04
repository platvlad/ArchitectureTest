package architectureTest.GUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.List;

public class ParametersCard extends JPanel {

    private int minValue;
    private int maxValue;

    public ParametersCard(String[] parameterNames, String floatingParameter, int minValue, int maxValue) {
        super(new SpringLayout());
        this.minValue = minValue;
        this.maxValue = maxValue;
        createSlider("min " + floatingParameter);
        createSlider("max " + floatingParameter);
        createTextField(floatingParameter + " step");
        int numElems = 3;
        for (String paramName: parameterNames) {
            if (!paramName.equals(floatingParameter)) {
                createTextField(paramName);
                numElems++;
            }
        }
        SpringUtilities.makeCompactGrid(this,
                numElems, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
    }

    private void createTextField(String name) {
        JLabel label = new JLabel(name, JLabel.TRAILING);
        add(label);
        JTextField textField = new JTextField(10);
        label.setLabelFor(textField);
        add(textField);
    }

    private void createSlider(String name) {
        JLabel label = new JLabel(name, JLabel.TRAILING);
        add(label);
        JSlider slider = new JSlider(minValue, maxValue);
        label.setLabelFor(slider);
        JPanel sliderTextPanel = new JPanel();
        sliderTextPanel.setLayout(new BoxLayout(sliderTextPanel,  BoxLayout.Y_AXIS));
        JLabel valueText = new JLabel();
        valueText.setText(Integer.toString(slider.getValue()));
        slider.addChangeListener((ChangeEvent e) -> {
            valueText.setText(Integer.toString(slider.getValue()));
        });
        sliderTextPanel.add(slider);
        sliderTextPanel.add(valueText);
        add(sliderTextPanel);
    }
}

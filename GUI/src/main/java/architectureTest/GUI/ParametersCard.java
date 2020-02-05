package architectureTest.GUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;

public class ParametersCard extends JPanel {

    private int minValue;
    private int maxValue;
    private Configuration config;
    private String floatingParameter;

    private enum SliderType {
        MIN,
        MAX
    }

    public ParametersCard(String[] parameterNames, String floatingParameter, int minValue, int maxValue, Configuration cfg) {
        super(new SpringLayout());
        this.floatingParameter = floatingParameter;
        config = cfg;
        config.addParamExperiment(floatingParameter);
        this.minValue = minValue;
        this.maxValue = maxValue;
        config.addParameter(floatingParameter, floatingParameter);
        createSlider(floatingParameter, SliderType.MIN);
        createSlider(floatingParameter, SliderType.MAX);
        createTextField(floatingParameter, true);
        int numElems = 3;
        for (String paramName: parameterNames) {
            if (!paramName.equals(floatingParameter)) {
                createTextField(paramName, false);
                numElems++;
            }
        }
        SpringUtilities.makeCompactGrid(this,
                numElems, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
    }

    private void createTextField(String name, boolean isStepParam) {
        String labelText = isStepParam ? name + " step" : name;
        JLabel label = new JLabel(labelText, JLabel.TRAILING);
        add(label);
        JTextField textField = new JTextField(10);
        label.setLabelFor(textField);
        if (!isStepParam) {
            config.addParameter(floatingParameter, name);
        }
        textField.addActionListener((ActionEvent evt) -> {
            String newText = textField.getText();
            if (isStepParam) {
                config.setStepParameter(floatingParameter, name, newText);
            } else {
                config.setFixedParameter(floatingParameter, name, newText);
            }
        });
        add(textField);
    }

    private void createSlider(String paramName, SliderType type) {
        String sliderName = (type == SliderType.MIN) ? "min " + paramName : "max " + paramName;
        JLabel label = new JLabel(sliderName, JLabel.TRAILING);
        add(label);
        JSlider slider = new JSlider(minValue, maxValue);
        label.setLabelFor(slider);
        JPanel sliderTextPanel = new JPanel();
        sliderTextPanel.setLayout(new BoxLayout(sliderTextPanel,  BoxLayout.Y_AXIS));
        JLabel valueText = new JLabel();
        valueText.setText(Integer.toString(slider.getValue()));
        int sliderInitValue = (minValue + maxValue) / 2;
        slider.setValue(sliderInitValue);
        if (type.equals(SliderType.MIN)) {
            config.setMinParameter(floatingParameter, paramName, sliderInitValue);
        } else {
            config.setMaxParameter(floatingParameter, paramName, sliderInitValue);
        }
        slider.addChangeListener((ChangeEvent e) -> {
            int sliderValue = slider.getValue();
            valueText.setText(Integer.toString(sliderValue));
            if (type.equals(SliderType.MIN)) {
                config.setMinParameter(floatingParameter, paramName, sliderValue);
            } else {
                config.setMaxParameter(floatingParameter, paramName, sliderValue);
            }
        });
        sliderTextPanel.add(slider);
        sliderTextPanel.add(valueText);
        add(sliderTextPanel);
    }
}

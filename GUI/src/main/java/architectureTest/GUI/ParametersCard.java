package architectureTest.GUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class ParametersCard extends JPanel {

    private int minValue;
    private int maxValue;

    public Map<String, Integer> getParamValues() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, JTextField> entry: paramFields.entrySet()) {
            result.put(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
        }
        return result;
    }

    public JTextField getStepField() {
        return stepField;
    }

    public JSlider getMinSlider() {
        return minSlider;
    }

    public JSlider getMaxSlider() {
        return maxSlider;
    }

    public String getFloatingParameter() {
        return floatingParameter;
    }

    private String floatingParameter;

    private Map<String, JTextField> paramFields = new HashMap<>();

    private JTextField stepField;

    private JSlider minSlider;

    private JSlider maxSlider;

    private enum SliderType {
        MIN,
        MAX
    }

    public ParametersCard(String[] parameterNames, String floatingParameter, int minValue, int maxValue) {
        super(new SpringLayout());
        this.floatingParameter = floatingParameter;
        this.minValue = minValue;
        this.maxValue = maxValue;
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
            paramFields.put(name, textField);
        } else {
            stepField = textField;
        }

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
            minSlider = slider;
        } else {
            maxSlider = slider;
        }
        slider.addChangeListener((ChangeEvent evt) -> {
            valueText.setText(Integer.toString(slider.getValue()));
        });
        sliderTextPanel.add(slider);
        sliderTextPanel.add(valueText);
        add(sliderTextPanel);
    }
}

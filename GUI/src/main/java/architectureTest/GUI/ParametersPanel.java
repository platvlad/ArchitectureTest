package architectureTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ParametersPanel extends JPanel {

    private Configuration config;

    public ParametersPanel(Configuration cfg) {
        super();
        config = cfg;
        JPanel cards = new JPanel(new CardLayout());
        JPanel comboBoxPane = new JPanel(); //use FlowLayout
        String[] comboBoxItems = { "Number of elements", "Number of clients", "delta" };
        JComboBox<String> cb = new JComboBox<>(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener((ItemEvent e) -> {
            CardLayout layout = (CardLayout) cards.getLayout();
            layout.show(cards, (String) e.getItem());
        });
        comboBoxPane.add(cb);

        String[] parameterNames = new String[comboBoxItems.length + 1];
        System.arraycopy(comboBoxItems, 0, parameterNames, 0, comboBoxItems.length);
        parameterNames[comboBoxItems.length] = "requests number";
        JPanel numElemsCard = new ParametersCard(parameterNames, parameterNames[0], 1, 100000, config);
        cards.add(numElemsCard, parameterNames[0]);
        JPanel numClientsCard = new ParametersCard(parameterNames, parameterNames[1], 1, 50, config);
        cards.add(numClientsCard, parameterNames[1]);
        JPanel deltaCard = new ParametersCard(parameterNames, parameterNames[2], 0, 5000, config);
        cards.add(deltaCard, parameterNames[2]);

        add(comboBoxPane, BorderLayout.PAGE_START);
        add(cards, BorderLayout.CENTER);
    }

}

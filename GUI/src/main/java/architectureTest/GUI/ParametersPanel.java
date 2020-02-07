package architectureTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ParametersPanel extends JPanel {

    private Map<String, ParametersCard> panels = new HashMap<>();

    public ParametersCard getCurrentCard() {
        return panels.get(currentCard);
    }

    private String currentCard;

    public ParametersPanel() {
        super();
        JPanel cards = new JPanel(new CardLayout());
        JPanel comboBoxPane = new JPanel();
        String[] comboBoxItems = { "Number of elements", "Number of clients", "delta" };
        JComboBox<String> cb = new JComboBox<>(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener((ItemEvent e) -> {
            CardLayout layout = (CardLayout) cards.getLayout();
            String itemName = (String) e.getItem();
            layout.show(cards, itemName);
            currentCard = itemName;
        });
        currentCard = Objects.requireNonNull(cb.getSelectedItem()).toString();
        comboBoxPane.add(cb);

        String[] parameterNames = new String[comboBoxItems.length + 1];
        System.arraycopy(comboBoxItems, 0, parameterNames, 0, comboBoxItems.length);
        parameterNames[comboBoxItems.length] = "requests number";
        ParametersCard numElemsCard = new ParametersCard(parameterNames, parameterNames[0], 1, 100000);
        cards.add(numElemsCard, parameterNames[0]);
        panels.put(parameterNames[0], numElemsCard);

        ParametersCard numClientsCard = new ParametersCard(parameterNames, parameterNames[1], 1, 50);
        cards.add(numClientsCard, parameterNames[1]);
        panels.put(parameterNames[1], numClientsCard);

        ParametersCard deltaCard = new ParametersCard(parameterNames, parameterNames[2], 0, 5000);
        cards.add(deltaCard, parameterNames[2]);
        panels.put(parameterNames[2], deltaCard);

        add(comboBoxPane, BorderLayout.PAGE_START);
        add(cards, BorderLayout.CENTER);
    }

}

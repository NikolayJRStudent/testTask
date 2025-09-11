package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    private JPanel cards;
    private CardLayout cardLayout;
    private JTextField numberInput;
    private JPanel numberButtonsPanel;
    private JButton sortBtn, resetBtn;
    private List<Integer> numbersList = new ArrayList<>();
    private boolean descending = true;
    private int totalNumbers;

    public Main() {
        setTitle("Sorting App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(buildIntroPanel(), "INTRO");
        cards.add(buildSortPanel(), "SORT");

        add(cards);
    }

    private JPanel buildIntroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        JLabel prompt = new JLabel("Number of values:");
        numberInput = new JTextField(10);
        JButton enterBtn = new JButton("Enter");

        enterBtn.addActionListener(e -> handleEnter());
        numberInput.addActionListener(e -> handleEnter());

        gbc.gridx = 0; gbc.gridy = 0; panel.add(prompt, gbc);
        gbc.gridx = 1; panel.add(numberInput, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; panel.add(enterBtn, gbc);

        return panel;
    }

    private JPanel buildSortPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        numberButtonsPanel = new JPanel();
        numberButtonsPanel.setLayout(new GridLayout(1,1,10,10));

        JScrollPane scrollPane = new JScrollPane(numberButtonsPanel);

        JPanel controls = new JPanel();
        sortBtn = new JButton("Sort");
        resetBtn = new JButton("Reset");

        sortBtn.addActionListener(e -> runSort());
        resetBtn.addActionListener(e -> cardLayout.show(cards, "INTRO"));

        controls.add(sortBtn);
        controls.add(resetBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void handleEnter() {
        try {
            totalNumbers = Integer.parseInt(numberInput.getText());
            if (totalNumbers <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive number!");
                return;
            }
            generateNumbers();
            refreshNumbersPanel();
            cardLayout.show(cards, "SORT");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number!");
        }
    }

    private void generateNumbers() {
        numbersList.clear();
        Random rnd = new Random();
        boolean hasSmall = false;

        for (int i = 0; i < totalNumbers; i++) {
            int val = rnd.nextInt(1000) + 1;
            numbersList.add(val);
            if (val <= 30) hasSmall = true;
        }
        if (!hasSmall) {
            numbersList.set(rnd.nextInt(totalNumbers), rnd.nextInt(30)+1);
        }
    }

    private void refreshNumbersPanel() {
        numberButtonsPanel.removeAll();
        int cols = (int)Math.ceil(numbersList.size()/10.0);
        numberButtonsPanel.setLayout(new GridLayout(0, cols, 10, 10));

        for (int n : numbersList) {
            JButton btn = new JButton(String.valueOf(n));
            btn.addActionListener(e -> handleNumberClick(n));
            numberButtonsPanel.add(btn);
        }
        numberButtonsPanel.revalidate();
        numberButtonsPanel.repaint();
    }

    private void handleNumberClick(int value) {
        if (value <= 30) {
            generateNumbers();
            refreshNumbersPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a value smaller or equal to 30.");
        }
    }

    private void runSort() {
        new Thread(() -> {
            quickSort(0, numbersList.size()-1);
            descending = !descending;
        }).start();
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int pivotIndex = partition(low, high);
            SwingUtilities.invokeLater(this::refreshNumbersPanel);
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
            quickSort(low, pivotIndex-1);
            quickSort(pivotIndex+1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = numbersList.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (descending ? numbersList.get(j) > pivot : numbersList.get(j) < pivot) {
                i++;
                int temp = numbersList.get(i);
                numbersList.set(i, numbersList.get(j));
                numbersList.set(j, temp);
            }
        }
        int temp = numbersList.get(i+1);
        numbersList.set(i+1, numbersList.get(high));
        numbersList.set(high, temp);
        return i+1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

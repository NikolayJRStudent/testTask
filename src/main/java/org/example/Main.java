package org.example;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.CardLayout;

/**
 * Main class represents a Swing-based sorting application.
 * It generates random numbers, displays them as buttons, and allows
 * the user to sort them with a visual highlight.
 */
public class Main extends JFrame {

    private static final String INTRO_PANEL = "INTRO";
    private static final String SORT_PANEL = "SORT";

    private static final int MAX_COLUMN_SIZE = 10;
    private static final int MAX_RANDOM_VALUE = 1000;
    private static final int SMALL_NUMBER_THRESHOLD = 30;

    private final List<JButton> numberButtons = new ArrayList<>();
    private static final int SORT_DELAY_MS = 250;
    private static final Dimension BUTTON_SIZE = new Dimension(60, 30);
    private static final Insets BUTTON_INSETS = new Insets(5, 5, 5, 5);

    private static final int GRID_PROMPT_X = 0;
    private static final int GRID_PROMPT_Y = 0;
    private static final int GRID_INPUT_X = 1;
    private static final int GRID_INPUT_Y = 0;
    private static final int GRID_BUTTON_X = 0;
    private static final int GRID_BUTTON_Y = 1;
    private static final int GRID_BUTTON_WIDTH = 2;

    private final JPanel cards;
    private final CardLayout cardLayout;
    private JTextField numberInput;
    private JPanel numberButtonsPanel;

    private final List<Integer> numbersList = new ArrayList<>();
    private final Random random = new Random();
    private boolean descending = true;
    private int totalNumbers;

    private volatile boolean sortingActive = false;
    private Thread sortingThread;

    /**
     * Constructs the main frame of the sorting application.
     * Sets up intro and sort panels and initializes UI.
     */
    public Main() {
        setTitle("Sorting App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(buildIntroPanel(), INTRO_PANEL);
        cards.add(buildSortPanel(), SORT_PANEL);

        add(cards);
    }

    /**
     * Builds the introductory panel with a label, input field and Enter button.
     *
     * @return JPanel representing the intro panel
     */
    private JPanel buildIntroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = BUTTON_INSETS;

        JLabel promptLabel = new JLabel("Number of values:");
        numberInput = new JTextField(10);
        JButton enterButton = new JButton("Enter");

        enterButton.addActionListener(e -> handleEnter());
        numberInput.addActionListener(e -> handleEnter());

        constraints.gridx = GRID_PROMPT_X;
        constraints.gridy = GRID_PROMPT_Y;
        panel.add(promptLabel, constraints);

        constraints.gridx = GRID_INPUT_X;
        constraints.gridy = GRID_INPUT_Y;
        panel.add(numberInput, constraints);

        constraints.gridx = GRID_BUTTON_X;
        constraints.gridy = GRID_BUTTON_Y;
        constraints.gridwidth = GRID_BUTTON_WIDTH;
        panel.add(enterButton, constraints);

        return panel;
    }

    /**
     * Builds the sorting panel containing number buttons and control buttons (Sort, Reset).
     *
     * @return JPanel representing the sort panel
     */
    private JPanel buildSortPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        numberButtonsPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(numberButtonsPanel);

        JPanel controlsPanel = new JPanel();
        JButton sortButton = new JButton("Sort");
        JButton resetButton = new JButton("Reset");

        sortButton.addActionListener(e -> runSort());
        resetButton.addActionListener(e -> {
            sortingActive = false;
            if (sortingThread != null && sortingThread.isAlive()) {
                sortingThread.interrupt();
            }
            cardLayout.show(cards, INTRO_PANEL);
        });

        controlsPanel.add(sortButton);
        controlsPanel.add(resetButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Handles the Enter button click or input action.
     * Parses input value and generates random numbers for sorting.
     */
    private void handleEnter() {
        try {
            totalNumbers = Integer.parseInt(numberInput.getText());
            if (totalNumbers <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive number!");
                return;
            }
            generateNumbers(totalNumbers);
            refreshNumbersPanel();
            cardLayout.show(cards, SORT_PANEL);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number!");
        }
    }

    /**
     * Generates a list of random numbers.
     * Ensures at least one number is less than or equal to SMALL_NUMBER_THRESHOLD.
     *
     * @param size the number of random numbers to generate
     */
    private void generateNumbers(int size) {
        numbersList.clear();
        boolean hasSmallNumber = false;

        for (int i = 0; i < size; i++) {
            int value = random.nextInt(MAX_RANDOM_VALUE) + 1;
            numbersList.add(value);
            if (value <= SMALL_NUMBER_THRESHOLD) hasSmallNumber = true;
        }

        if (!hasSmallNumber && size > 0) {
            numbersList.set(random.nextInt(size), random.nextInt(SMALL_NUMBER_THRESHOLD) + 1);
        }
    }

    /**
     * Refreshes the number buttons panel according to the current numbersList.
     * Arranges numbers in columns of MAX_COLUMN_SIZE.
     */
    private void refreshNumbersPanel() {
        numberButtonsPanel.removeAll();
        numberButtons.clear();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = BUTTON_INSETS;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;
        int col = 0;

        for (int index = 0; index < numbersList.size(); index++) {
            int value = numbersList.get(index);
            JButton numberButton = new JButton(String.valueOf(value));
            numberButton.setPreferredSize(BUTTON_SIZE);
            numberButton.setMinimumSize(BUTTON_SIZE);
            numberButton.setMaximumSize(BUTTON_SIZE);

            int finalIndex = index;
            numberButton.addActionListener(e -> handleNumberClick(numbersList.get(finalIndex)));

            constraints.gridx = col;
            constraints.gridy = row;
            numberButtonsPanel.add(numberButton, constraints);
            numberButtons.add(numberButton);

            row++;
            if (row >= MAX_COLUMN_SIZE) {
                row = 0;
                col++;
            }
        }

        numberButtonsPanel.revalidate();
        numberButtonsPanel.repaint();
    }

    /**
     * Handles a click on a number button.
     * If value <= SMALL_NUMBER_THRESHOLD, generates new numbers of that size.
     *
     * @param value the number on the clicked button
     */
    private void handleNumberClick(int value) {
        if (value <= SMALL_NUMBER_THRESHOLD) {
            generateNumbers(value);
            refreshNumbersPanel();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a value smaller or equal to " + SMALL_NUMBER_THRESHOLD + ".");
        }
    }

    /**
     * Starts sorting numbers in a separate thread using quicksort.
     * Prevents multiple simultaneous sorts.
     */
    private void runSort() {
        if (sortingActive) return;
        sortingActive = true;

        sortingThread = new Thread(() -> {
            try {
                quickSort(0, numbersList.size() - 1);
                descending = !descending;
            } finally {
                sortingActive = false;
            }
        });
        sortingThread.start();
    }

    /**
     * Recursively sorts numbers using quicksort algorithm.
     *
     * @param low  the starting index
     * @param high the ending index
     */
    private void quickSort(int low, int high) {
        if (!sortingActive || Thread.currentThread().isInterrupted()) return;
        if (low >= high) return;

        int pivotIndex = partition(low, high);
        if (!sortingActive || Thread.currentThread().isInterrupted()) return;

        try {
            Thread.sleep(SORT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        quickSort(low, pivotIndex - 1);
        quickSort(pivotIndex + 1, high);
    }

    /**
     * Partitions the numbers list for quicksort and highlights swapped buttons.
     *
     * @param low  the starting index
     * @param high the ending index
     * @return the partition index
     */
    private int partition(int low, int high) {
        int pivotValue = numbersList.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (!sortingActive || Thread.currentThread().isInterrupted()) return i + 1;

            boolean condition = descending ? numbersList.get(j) > pivotValue : numbersList.get(j) < pivotValue;
            if (condition) {
                i++;
                swapAndHighlight(i, j);

                if (!sortingActive || Thread.currentThread().isInterrupted()) return i;
            }
        }
        swapAndHighlight(i + 1, high);
        return i + 1;
    }

    /**
     * Swaps two numbers in the list and visually highlights only the pair being swapped.
     *
     * @param i index of first number
     * @param j index of second number
     */
    private void swapAndHighlight(int i, int j) {
        int valueI = numbersList.get(i);
        numbersList.set(i, numbersList.get(j));
        numbersList.set(j, valueI);

        SwingUtilities.invokeLater(() -> {
            if (i < numberButtons.size() && j < numberButtons.size()) {
                JButton buttonI = numberButtons.get(i);
                JButton buttonJ = numberButtons.get(j);

                buttonI.setText(String.valueOf(numbersList.get(i)));
                buttonJ.setText(String.valueOf(numbersList.get(j)));


                buttonI.setBackground(Color.YELLOW);
                buttonJ.setBackground(Color.YELLOW);
            }
        });

        try {
            Thread.sleep(SORT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        SwingUtilities.invokeLater(() -> {
            if (i < numberButtons.size() && j < numberButtons.size()) {
                JButton buttonI = numberButtons.get(i);
                JButton buttonJ = numberButtons.get(j);

                buttonI.setBackground(null);
                buttonJ.setBackground(null);
            }
        });
    }

    /**
     * Interpolates between two colors for animation effect.
     *
     * @param start  color
     * @param end  color
     * @param ratio  between 0.0 and 1.0
     * @return interpolated color
     */
    private Color interpolate(Color start, Color end, float ratio) {
        int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
        int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
        return new Color(red, green, blue);
    }

    /**
     * Main method. Launches the sorting application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

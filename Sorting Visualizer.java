package Project;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SortVisualizer extends JPanel 
{

    private int[] array;
    private int[] originalArray;
    private int size;
    public boolean sorting = false;
    public boolean paused = false;
    public int sleepTime = 100;
    private final Object lock = new Object();
    private int swapIndex1 = -1;
    private int swapIndex2 = -1;
    private static JTextArea stepsTextArea = new JTextArea(5, 20);

    public SortVisualizer(int[] userArray) {
        this.size = userArray.length;
        this.array = Arrays.copyOf(userArray, userArray.length);
        this.originalArray = Arrays.copyOf(userArray, userArray.length);
        this.setBackground(new Color(245, 245, 255)); // array elements background 
    }

    public void bubbleSort() {
        new Thread(() -> {
            sorting = true;
            for (int i = 0; i < size - 1; i++) {
                for (int j = 0; j < size - i - 1; j++) {
                    swapIndex1 = j;
                    swapIndex2 = j + 1;
                    if (array[j] > array[j + 1]) {
                        int temp = array[j];
                        array[j] = array[j + 1];
                        array[j + 1] = temp;
                        appendStep("Swapped " + array[j + 1] + " and " + array[j]);
                    }
                    repaint();
                    sleepIfPaused();
                }
            }
            endSort();
        }).start();
    }

    public void insertionSort() {
        new Thread(() -> {
            sorting = true;
            for (int i = 1; i < size; i++) {
                int key = array[i];
                int j = i - 1;
                while (j >= 0 && array[j] > key) {
                    swapIndex1 = j;
                    swapIndex2 = j + 1;
                    array[j + 1] = array[j];
                    appendStep("Moved " + array[j] + " to position " + (j + 1));
                    j--;
                    repaint();
                    sleepIfPaused();
                }
                array[j + 1] = key;
                appendStep("Inserted " + key + " at position " + (j + 1));
                repaint();
                sleepIfPaused();
            }
            endSort();
        }).start();
    }

    public void selectionSort() {
        new Thread(() -> {
            sorting = true;
            for (int i = 0; i < size - 1; i++) {
                int minIdx = i;
                for (int j = i + 1; j < size; j++) {
                    swapIndex1 = minIdx;
                    swapIndex2 = j;
                    if (array[j] < array[minIdx]) {
                        minIdx = j;
                        appendStep("Selected minimum " + array[minIdx] + " at position " + minIdx);
                    }
                    repaint();
                    sleepIfPaused();
                }
                if (minIdx != i) {
                    int temp = array[minIdx];
                    array[minIdx] = array[i];
                    array[i] = temp;
                    appendStep("Swapped " + array[minIdx] + " with " + array[i]);
                    repaint();
                    sleepIfPaused();
                }
            }
            endSort();
        }).start();
    }

    public void quickSort() {
        new Thread(() -> {
            sorting = true;
            quickSortHelper(0, size - 1);
            endSort();
        }).start();
    }

    private void quickSortHelper(int low, int high) {
        if (low < high) {
            int pivot = partition(low, high);
            quickSortHelper(low, pivot - 1);
            quickSortHelper(pivot + 1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = array[high];
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            swapIndex1 = j;
            swapIndex2 = high;
            if (array[j] < pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
                appendStep("Swapped " + array[j] + " and " + array[i]);
                repaint();
                sleepIfPaused();
            }
        }

        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        appendStep("Swapped " + array[i + 1] + " and " + array[high]);
        repaint();
        sleepIfPaused();
        return i + 1;
    }

    private void sleepIfPaused() {
        try {
            synchronized (lock) {
                while (paused) {
                    lock.wait();
                }
            }
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void endSort() {
        swapIndex1 = -1;
        swapIndex2 = -1;
        sorting = false;
        repaint();
    }

    private static void appendStep(String step) {
        SwingUtilities.invokeLater(() -> stepsTextArea.append(step + "\n"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int barWidth = getWidth() / size;
        g.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < size; i++) {
            int barHeight = array[i];
            int x = i * barWidth;
            int y = getHeight() - barHeight;
            g.setColor(i == swapIndex1 || i == swapIndex2 ? Color.RED : (sorting ? new Color(100, 149, 237) : new Color(60, 179, 113)));
            g.fillRoundRect(x + 2, y, barWidth - 4, barHeight, 10, 10);

            String text = String.valueOf(array[i]);
            int textWidth = g.getFontMetrics().stringWidth(text);
            int textX = x + (barWidth - textWidth) / 2;
            int textY = y - 5;

            if (textY > 0) {
                g.setColor(Color.BLACK);
                g.drawString(text, textX, textY);
            }
        }
    }

    public void togglePauseResume() {
        synchronized (lock) {
            paused = !paused;
            if (!paused) {
                lock.notify();
            }
        }
    }

    public static void main(String[] args) 
    {
        JFrame frame = new JFrame("Sort Visualizer");
        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(230, 240, 255));

        JLabel welcomeLabel = new JLabel("Welcome to the Sorting Visualizer!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Verdana", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(70, 70, 70));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Verdana", Font.PLAIN, 24));
        startButton.setBackground(new Color(100, 149, 237));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        welcomePanel.add(startButton, BorderLayout.SOUTH);

        frame.add(welcomePanel);
        frame.setVisible(true);
          
        // Add Functionality Using a lambda
        
        startButton.addActionListener(e -> { 

            welcomePanel.setVisible(false);

            int size = 0;
            while (size < 1 || size > 200) {
                try {
                    String input = JOptionPane.showInputDialog("Enter number of elements (1-200):"); // abc
                    if (input == null) {
                        System.exit(0);
                    }
                    size = Integer.parseInt(input);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number.");
                }
            }

            int[] userArray = new int[size];
            for (int i = 0; i < size; i++) 
            {
                while(true) 
                {
                    try {
                        String val = JOptionPane.showInputDialog("Enter element " + (i + 1) + ":");
                        if (val == null) {
                            System.exit(0);
                        }
                        userArray[i] = Integer.parseInt(val);
                        break;
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid number.");
                    }
                }
            }

            SortVisualizer visualizer = new SortVisualizer(userArray);

            frame.setLayout(new BorderLayout()); // array elements border 
            frame.add(visualizer, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel(new GridLayout(2, 1));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(240, 248, 255));

            JButton bubbleBtn = new JButton("Bubble Sort");
            JButton insertBtn = new JButton("Insertion Sort");
            JButton selectBtn = new JButton("Selection Sort");
            JButton quickSortBtn = new JButton("Quick Sort");
            JButton pauseBtn = new JButton("Pause");
            JButton restartBtn = new JButton("Restart");

            // This is a Java array — specifically, an array of JButton objects.
            JButton[] buttons = {bubbleBtn, insertBtn, selectBtn, quickSortBtn, pauseBtn, restartBtn};
            
            // Button Front, Size, Type, Color
            for (JButton b : buttons) 
            {
                b.setFont(new Font("Arial", Font.BOLD, 16));
                b.setFocusPainted(false);
                b.setBackground(new Color(65, 105, 225)); // buttons background
                b.setForeground(Color.WHITE); // buttons text color

                buttonPanel.add(b);
            }

            // evt is a parameter (short for event) — it represents the ActionEvent triggered when the button is clicked.
            bubbleBtn.addActionListener(evt -> { // -> means: "goes to" or "executes this code when the event happens."
                if (!visualizer.sorting) {
                    stepsTextArea.setText(""); //step clear
                    visualizer.array = Arrays.copyOf(visualizer.originalArray, visualizer.originalArray.length);
                    visualizer.bubbleSort();
                }
            });

            insertBtn.addActionListener(evt -> {
                if (!visualizer.sorting) {
                    stepsTextArea.setText("");
                    visualizer.array = Arrays.copyOf(visualizer.originalArray, visualizer.originalArray.length);
                    visualizer.insertionSort();
                }
            });

            selectBtn.addActionListener(evt -> { 
                if (!visualizer.sorting) {
                    stepsTextArea.setText("");
                    visualizer.array = Arrays.copyOf(visualizer.originalArray, visualizer.originalArray.length);
                    visualizer.selectionSort();
                }
            });

            quickSortBtn.addActionListener(evt -> {
                if (!visualizer.sorting) {
                    stepsTextArea.setText("");
                    visualizer.array = Arrays.copyOf(visualizer.originalArray, visualizer.originalArray.length);
                    visualizer.quickSort();
                }
            });

            pauseBtn.addActionListener(evt -> {
                visualizer.togglePauseResume();
                pauseBtn.setText(visualizer.paused ? "Resume" : "Pause");
            });

            restartBtn.addActionListener(evt -> {
                frame.dispose();
                main(null);
            });

            JScrollPane scrollPane = new JScrollPane(stepsTextArea);
            stepsTextArea.setEditable(false);
            stepsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

            JPanel sliderPanel = new JPanel();
            JLabel speedLabel = new JLabel("Speed (ms): 10");
            JSlider speedSlider = new JSlider(10, 1500, 10);
            
            speedSlider.addChangeListener(e1 -> {
                visualizer.sleepTime = speedSlider.getValue();
                speedLabel.setText("Speed(ms): " + visualizer.sleepTime);
            });
            
            sliderPanel.add(speedLabel);
            sliderPanel.add(speedSlider);

            controlPanel.add(buttonPanel);
            controlPanel.add(sliderPanel);

            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.SOUTH);
            frame.revalidate();
        });
    }
}




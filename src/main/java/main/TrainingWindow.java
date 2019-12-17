package main;

import model.GroupController;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class TrainingWindow {
    private JPanel rootPanel;
    private JProgressBar progressBar;
    private JTextField maxScoreField;
    private JTextField iterationField;
    private JButton startButton;
    private JTextField countField;
    
    private int id;
    
    private void initComponents() {
        startButton.addActionListener(ev -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    int toDo = Integer.parseInt(countField.getText());
                    GroupController controller = Utils.getController(id);
                    if (toDo > 0 && controller != null) {
                        startButton.setEnabled(false);
                        int startIteration = controller.getIteration();
                        Consumer<Integer> onNew = it -> {
                            progressBar.setValue(Utils.clamp((int) Math.round(100 * (it - startIteration) / (double) toDo), 0, 100));
                            iterationField.setText(String.valueOf(it));
                            maxScoreField.setText(String.valueOf(controller.getMaxAvgScore()));
                            progressBar.repaint();
                        };
                        Consumer<Void> onFinish = v -> {
                            Utils.updateDatabase(id, null, controller);
                            Utils.saveNetworks(id, controller);
                            startButton.setEnabled(true);
                        };
                        onNew.accept(startIteration);
                        controller.train(toDo, 120, onNew, onFinish);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Invalid number format" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                    startButton.setEnabled(true);
                }
            });
            executorService.shutdown();
        });
    }
    
    TrainingWindow(int id) {
        this.id = id;
        initComponents();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

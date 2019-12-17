package main;

import graphics.FieldDisplay;
import learning.Network;
import model.GroupController;

import javax.swing.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class DisplayWindow {
    private JPanel rootPanel;
    private JButton startButton;
    private JButton stopButton;
    private JTextField delayField;
    private FieldDisplay display;
    private JTextField scoreField;
    
    private int id;
    private int networkID;
    GroupController controller;
    private Future<Double> future;
    
    private void initComponents() {
        startButton.addActionListener(ev -> {
            int delay;
            try {
                delay = Integer.parseInt(delayField.getText());
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (controller == null) {
                return;
            }
            controller.setDisplay(display);
    
            Network network = Utils.getNetwork(networkID);
            
            Consumer<Double> onTick = s -> scoreField.setText(String.valueOf((int)Math.round(s)));
            
            future = controller.startDisplayRun(network, delay, onTick);
            
            /*ExecutorService ex = Executors.newSingleThreadExecutor();
            future = ex.submit(() -> {
            
            });
            ex.shutdown();*/
        });
        stopButton.addActionListener(ev -> {
            if (future != null) {
                future.cancel(true);
            }
        });
    }
    
    DisplayWindow(int id, int networkID) {
        this.id = id;
        this.networkID = networkID;
        controller = Utils.getController(id);
        initComponents();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

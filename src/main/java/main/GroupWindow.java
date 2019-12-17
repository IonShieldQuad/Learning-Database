package main;

import model.GroupController;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.MainWindow.TITLE;

public class GroupWindow {
    private JPanel rootPanel;
    private JButton resetButton;
    private JButton trainButton;
    private JButton viewButton;
    private JTextField idField;
    private JTextField nameField;
    private JTextField iterationField;
    private JTextField maxScoreField;
    private JTextField widthField;
    private JTextField heightField;
    private JTextField layersField;
    private JTextField activationField;
    private JTextField sizeField;
    private JTextField runsField;
    private JTextField maxDensityField;
    private JTextField distributionField;
    private JButton refreshButton;
    private JButton saveButton;
    
    private int id;
    
    private void initComponents() {
        refreshButton.addActionListener(ev -> updateFields());
        saveButton.addActionListener(ev -> {
            save();
            updateFields();
        });
        resetButton.addActionListener(ev -> {
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.submit(() -> {
                resetButton.setEnabled(false);
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset the group?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    reset();
                }
                updateFields();
                resetButton.setEnabled(true);
            });
            ex.shutdown();
        });
        trainButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            TrainingWindow gui = new TrainingWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        viewButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            TableWindow gui = new TableWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    private void reset() {
        GroupController controller = Utils.getController(id);
        if (controller != null) {
            controller.reset();
            Utils.updateDatabase(id, null, controller);
    
            String sql = "DELETE FROM networks WHERE group_id = " + id + ";";
            
            try (Connection conn = DriverManager.getConnection(Utils.url);
                 Statement stmt = conn.createStatement()) {
    
                Statement s = conn.createStatement();
                s.execute(Utils.enableFK);
                s.close();
                
                stmt.execute(sql);
        
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            Utils.saveNetworks(id, controller);
        }
    }
    
    private void save() {
        try {
            String name = nameField.getText();
            int runsLimit = Integer.parseInt(runsField.getText());
            double maxDensity = Double.parseDouble(maxDensityField.getText());
            double distribution = Double.parseDouble(distributionField.getText());
            
            GroupController controller = Utils.getController(id);
            if (controller != null) {
                controller.setRunsLimit(runsLimit);
                controller.setMaxDensity(maxDensity);
                controller.setDistribution(distribution);
                
                Utils.updateDatabase(id, name, controller);
            }
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    private void updateFields() {
        String sql = "SELECT * FROM groups WHERE id = " + id + ";";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
    
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
        
            res.next();
            idField.setText(String.valueOf(id));
            nameField.setText(res.getString("name"));
            iterationField.setText(String.valueOf(res.getInt("iteration")));
            maxScoreField.setText(String.valueOf(res.getInt("max_score")));
            widthField.setText(String.valueOf(res.getInt("width")));
            heightField.setText(String.valueOf(res.getInt("height")));
            layersField.setText(String.valueOf(res.getInt("hidden_layers")));
            activationField.setText(String.valueOf(res.getString("activation")));
            sizeField.setText(String.valueOf(res.getInt("count")));
            runsField.setText(String.valueOf(res.getInt("runs_limit")));
            maxDensityField.setText(String.valueOf(res.getDouble("max_density")));
            distributionField.setText(String.valueOf(res.getDouble("distribution")));
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    GroupWindow(int id) {
        this.id = id;
        initComponents();
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

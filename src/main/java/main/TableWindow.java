package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;

import static main.MainWindow.TITLE;

public class TableWindow {
    private JPanel rootPanel;
    private JButton visualDisplayButton;
    private JTable networkTable;
    private JButton refreshButton;
    private JTable nodesTable;
    private JTable linksTable;
    private JButton getNodesButton;
    private JButton getLinksButton;
    
    private int id;
    
    private void initComponents() {
        DefaultTableModel model0 = new DefaultTableModel(0, 6) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        networkTable.setAutoCreateColumnsFromModel(false);
        networkTable.setModel(model0);
    
        TableColumn[] columns0 = new TableColumn[6];
    
        for (int i = 0; i < columns0.length; i++) {
            columns0[i] = new TableColumn(i, 50, null, null);
            columns0[i].setIdentifier(i);
        }
    
        columns0[0].setWidth(20);
        columns0[0].setHeaderValue("ID");
    
        columns0[1].setWidth(20);
        columns0[1].setHeaderValue("Generation");
    
        columns0[2].setWidth(20);
        columns0[2].setHeaderValue("Inputs");
    
        columns0[3].setWidth(20);
        columns0[3].setHeaderValue("Outputs");
    
        columns0[4].setWidth(20);
        columns0[4].setHeaderValue("Layers");
    
        columns0[5].setWidth(20);
        columns0[5].setHeaderValue("Activation");
    
        for (int i = 0; i < columns0.length; i++) {
            networkTable.addColumn(columns0[i]);
        }
    
    
        DefaultTableModel model1 = new DefaultTableModel(0, 5) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        nodesTable.setAutoCreateColumnsFromModel(false);
        nodesTable.setModel(model1);
    
        TableColumn[] columns1 = new TableColumn[5];
    
        for (int i = 0; i < columns1.length; i++) {
            columns1[i] = new TableColumn(i, 50, null, null);
            columns1[i].setIdentifier(i);
        }
    
        columns1[0].setWidth(20);
        columns1[0].setHeaderValue("ID");
    
        columns1[1].setWidth(20);
        columns1[1].setHeaderValue("Layer");
    
        columns1[2].setWidth(20);
        columns1[2].setHeaderValue("Index");
    
        columns1[3].setWidth(20);
        columns1[3].setHeaderValue("Bias");
    
        columns1[4].setWidth(20);
        columns1[4].setHeaderValue("Type");
    
    
        for (int i = 0; i < columns1.length; i++) {
            nodesTable.addColumn(columns1[i]);
        }
    
    
        DefaultTableModel model2 = new DefaultTableModel(0, 5) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        linksTable.setAutoCreateColumnsFromModel(false);
        linksTable.setModel(model2);
    
        TableColumn[] columns2 = new TableColumn[5];
    
        for (int i = 0; i < columns2.length; i++) {
            columns2[i] = new TableColumn(i, 50, null, null);
            columns2[i].setIdentifier(i);
        }
    
        columns2[0].setWidth(20);
        columns2[0].setHeaderValue("ID");
    
        columns2[1].setWidth(20);
        columns2[1].setHeaderValue("Input Node ID");
    
        columns2[2].setWidth(20);
        columns2[2].setHeaderValue("Input Index");
    
        columns2[3].setWidth(20);
        columns2[3].setHeaderValue("Output Index");
    
        columns2[4].setWidth(20);
        columns2[4].setHeaderValue("Weight");
    
    
        for (int i = 0; i < columns2.length; i++) {
            linksTable.addColumn(columns2[i]);
        }
        
        refreshButton.addActionListener(ev -> updateNetworksTable());
        getNodesButton.addActionListener(ev -> updateNodesTable());
        getLinksButton.addActionListener(ev -> updateLinksTable());
        
        visualDisplayButton.addActionListener(ev -> {
            int index = networkTable.getSelectedRow();
            if (index == -1) {
                return;
            }
    
            int id = Integer.parseInt(String.valueOf(networkTable.getModel().getValueAt(index, 0)));
    
            JFrame frame = new JFrame(TITLE);
            DisplayWindow gui = new DisplayWindow(this.id, id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        
    }
    
    private void updateNetworksTable() {
        
        DefaultTableModel model = (DefaultTableModel)networkTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT * FROM networks WHERE group_id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {res.getInt("id"),
                        res.getInt("generation"),
                        res.getInt("in_size"),
                        res.getInt("out_size"),
                        res.getInt("layers"),
                        res.getString("activation")
                });
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateNodesTable() {
        int index = networkTable.getSelectedRow();
        if (index == -1) {
            return;
        }
    
        int id = Integer.parseInt(String.valueOf(networkTable.getModel().getValueAt(index, 0)));
        
        DefaultTableModel model = (DefaultTableModel)nodesTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT * FROM nodes WHERE network_id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {res.getInt("id"),
                        res.getInt("layer"),
                        res.getInt("node_index"),
                        res.getDouble("bias"),
                        res.getString("type")
                });
            }
            
            
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateLinksTable() {
        int index = nodesTable.getSelectedRow();
        if (index == -1) {
            return;
        }
        
        int id = Integer.parseInt(String.valueOf(nodesTable.getModel().getValueAt(index, 0)));
        
        DefaultTableModel model = (DefaultTableModel)linksTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT * FROM links WHERE in_node_id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {res.getInt("id"),
                        res.getInt("in_node_id"),
                        res.getInt("in_node_index"),
                        res.getInt("out_node_index"),
                        res.getDouble("weight")
                });
            }
            
            
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public TableWindow(int id) {
        this.id = id;
        initComponents();
        updateNetworksTable();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

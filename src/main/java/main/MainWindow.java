package main;

import learning.Network;
import model.GroupController;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import static learning.Network.Node.*;

public class MainWindow {
    
    
    public static final String TITLE = "Learning database";
    private JPanel rootPanel;
    private JTable table1;
    private JTextField nameField;
    private JComboBox activationComboBox;
    private JButton loadGroupButton;
    private JButton createGroupButton;
    private JTextField widthField;
    private JTextField heightField;
    private JTextField layersField;
    private JTextField sizeField;
    private JTextField runsField;
    private JTextField maxDensityField;
    private JTextField distributionField;
    private JButton deleteGroupButton;
    private JButton refreshButton;
    
    private void initComponents() {
        refreshButton.addActionListener(ev -> updateTable());
        createGroupButton.addActionListener(ev -> addNewGroup());
        loadGroupButton.addActionListener(ev -> {
            int index = table1.getSelectedRow();
            if (index == -1) {
                return;
            }
    
            int id = Integer.parseInt(String.valueOf(table1.getModel().getValueAt(index, 0)));
            
            JFrame frame = new JFrame(TITLE);
            GroupWindow gui = new GroupWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        deleteGroupButton.addActionListener(ev -> {
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.submit(() -> {
                deleteGroupButton.setEnabled(false);
                loadGroupButton.setEnabled(false);
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset the group?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    deleteSelectedGroup();
                }
                deleteGroupButton.setEnabled(true);
                loadGroupButton.setEnabled(true);
            });
            ex.shutdown();
        });
    
        DefaultTableModel model = new DefaultTableModel(0, 12) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setAutoCreateColumnsFromModel(false);
        table1.setModel(model);
    
        TableColumn[] columns = new TableColumn[12];
    
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(i, 50, null, null);
            columns[i].setIdentifier(i);
        }
        
        columns[0].setWidth(20);
        columns[0].setHeaderValue("ID");
    
        columns[1].setWidth(50);
        columns[1].setHeaderValue("Name");
    
        columns[2].setWidth(20);
        columns[2].setHeaderValue("Iteration");
    
        columns[3].setWidth(20);
        columns[3].setHeaderValue("Max score");
    
        columns[4].setWidth(20);
        columns[4].setHeaderValue("Width");
    
        columns[5].setWidth(20);
        columns[5].setHeaderValue("Height");
    
        columns[6].setWidth(20);
        columns[6].setHeaderValue("Hidden layers");
    
        columns[7].setWidth(50);
        columns[7].setHeaderValue("Activation function");
    
        columns[8].setWidth(20);
        columns[8].setHeaderValue("Size");
    
        columns[9].setWidth(20);
        columns[9].setHeaderValue("Runs");
    
        columns[10].setWidth(20);
        columns[10].setHeaderValue("Max density");
    
        columns[11].setWidth(20);
        columns[11].setHeaderValue("Distribution");
    
        for (int i = 0; i < columns.length; i++) {
            table1.addColumn(columns[i]);
        }
    }
    
    private void updateTable() {
    
        DefaultTableModel model = (DefaultTableModel)table1.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT * FROM groups;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
    
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {res.getInt("id"),
                        res.getString("name"),
                        res.getInt("iteration"),
                        res.getInt("max_score"),
                        res.getInt("width"),
                        res.getInt("height"),
                        res.getInt("hidden_layers"),
                        res.getString("activation"),
                        res.getInt("count"),
                        res.getInt("runs_limit"),
                        res.getDouble("max_density"),
                        res.getDouble("distribution")
                });
            }
            
            
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addNewGroup() {
        try {
            String name = nameField.getText();
            int iteration = 0;
            int maxScore = 0;
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());
            int layers = Integer.parseInt(layersField.getText());
            int size = Integer.parseInt(sizeField.getText());
            int runs = Integer.parseInt(runsField.getText());
            double maxDensity = Double.parseDouble(maxDensityField.getText());
            double distribution = Double.parseDouble(distributionField.getText());
        
            String activationString;
            switch (activationComboBox.getSelectedIndex()) {
                case 0:
                    activationString = "ReLU";
                    break;
                case 1:
                    activationString = "LReLU";
                    break;
                case 2:
                    activationString = "Softplus";
                    break;
                case 3:
                    activationString = "Sigmoid";
                    break;
                default:
                    activationString = "Unknown";
            }
            ActivationType activation;
            switch (activationComboBox.getSelectedIndex()) {
                case 0:
                    activation = ActivationType.RELU;
                    break;
                case 1:
                    activation = ActivationType.LRELU;
                    break;
                case 2:
                    activation = ActivationType.SOFTPLUS;
                    break;
                case 3:
                    activation = ActivationType.SIGMOID;
                    break;
                default:
                    activation = ActivationType.LRELU;
            }
        
            GroupController groupController = new GroupController(width, height, layers, activation, maxDensity, distribution, size, runs);
        
            String sql = "INSERT INTO groups(name, iteration, max_score, width, height, hidden_layers, activation, count, runs_limit, max_density, distribution, object) "
            + "VALUES (?, " + iteration + ", " + maxScore + ", " + width + ", " + height + ", " + layers + ", " + "?" + ", " + size + ", " + runs + ", " + maxDensity + ", " + distribution + ", " + "?" + ")";
            
            try (Connection conn = DriverManager.getConnection(Utils.url);
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    
                Statement s = conn.createStatement();
                s.execute(Utils.enableFK);
                s.close();
                
                pstmt.setString(1, name);
                pstmt.setString(2, activationString);
                pstmt.setObject(3, SerializationUtils.serialize(groupController), Types.BLOB);
                pstmt.execute();
                ResultSet res = pstmt.getGeneratedKeys();
                res.next();
                int id = res.getInt(1);
                Utils.saveNetworks(id, groupController);
                
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateTable();
    }
    
    public void deleteSelectedGroup() {
        int index = table1.getSelectedRow();
        if (index == -1) {
            return;
        }
    
        int id = Integer.parseInt(String.valueOf(table1.getModel().getValueAt(index, 0)));
    
        String sql = "DELETE FROM groups WHERE id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement()) {
    
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
        
            conn.setAutoCommit(false);
            stmt.execute(sql);
            conn.commit();
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        updateTable();
    }
    
    public MainWindow() {
        initComponents();
        updateTable();
    }
    
    
    public static void createNewDatabase() {
        
        String createGroupsTable = "CREATE TABLE IF NOT EXISTS groups(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    name varchar NOT NULL,\n"
                + "    iteration int NOT NULL,\n"
                + "    max_score int NOT NULL,\n"
                + "    width int NOT NULL,\n"
                + "    height int NOT NULL,\n"
                + "    hidden_layers int NOT NULL,\n"
                + "    activation varchar(128) NOT NULL,\n"
                + "    count int NOT NULL,\n"
                + "    runs_limit int NOT NULL,\n"
                + "    max_density real NOT NULL,\n"
                + "    distribution real NOT NULL,\n"
                + "    object blob\n"
                + ");";
        
        String createNetworksTable = "CREATE TABLE IF NOT EXISTS networks(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    group_id int NOT NULL,\n"
                + "    generation int NOT NULL,\n"
                + "    in_size int NOT NULL,\n"
                + "    out_size int NOT NULL,\n"
                + "    layers int NOT NULL,\n"
                + "    activation varchar(128) NOT NULL,\n"
                + "    object blob,\n"
                + "    CONSTRAINT fk_group"
                + "    FOREIGN KEY(group_id) "
                + "    REFERENCES groups(id)\n"
                + "    ON DELETE CASCADE"
                + ");";
        
        String createNodesTable = "CREATE TABLE IF NOT EXISTS nodes(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    network_id int NOT NULL,\n"
                + "    layer int NOT NULL,\n"
                + "    node_index int NOT NULL,\n"
                + "    bias real NOT NULL,\n"
                + "    type varchar(128) NOT NULL,\n"
                + "    object blob,\n"
                + "    CONSTRAINT fk_network"
                + "    FOREIGN KEY(network_id) "
                + "    REFERENCES networks(id)\n"
                + "    ON DELETE CASCADE"
                + ");";
    
        String createLinksTable = "CREATE TABLE IF NOT EXISTS links(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    in_node_id int NOT NULL,\n"
                + "    in_node_index int NOT NULL,\n"
                + "    out_node_index int NOT NULL,\n"
                + "    weight real NOT NULL,\n"
                + "    object blob,\n"
                + "    CONSTRAINT fk_node"
                + "    FOREIGN KEY(in_node_id) "
                + "    REFERENCES nodes(id)\n"
                + "    ON DELETE CASCADE"
                + ");";
        
        String exists0 = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='networks';";
        String exists1 = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='nodes';";
        String exists2 = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='links';";
        
        String index0 = "CREATE INDEX networks_group_id_index ON networks(group_id);";
        String index1 = "CREATE INDEX nodes_network_id_index ON nodes(network_id);";
        String index2 = "CREATE INDEX links_nodes_id_index ON links(in_node_id);";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("Database connection established.");
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            conn.setAutoCommit(false);
            
            ResultSet res0 = stmt.executeQuery(exists0);
            ResultSet res1 = stmt.executeQuery(exists1);
            ResultSet res2 = stmt.executeQuery(exists2);
            
            boolean e0 = res0.getInt(1) == 1;
            boolean e1 = res1.getInt(1) == 1;
            boolean e2 = res2.getInt(1) == 1;
            
            res0.close();
            res1.close();
            res2.close();
            
            stmt.execute(createGroupsTable);
            stmt.execute(createNetworksTable);
            stmt.execute(createNodesTable);
            stmt.execute(createLinksTable);
            
            if (!e0) {
                stmt.execute(index0);
            }
            if (!e1) {
                stmt.execute(index1);
            }
            if (!e2) {
                stmt.execute(index2);
            }
            conn.commit();
    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        /*try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }*/
        createNewDatabase();
        JFrame frame = new JFrame(TITLE);
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

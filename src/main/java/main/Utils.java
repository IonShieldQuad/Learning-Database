package main;

import learning.Network;
import model.GroupController;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;

public abstract class Utils {
    
    public static final int RADIUS = 2;
    
    public static final String dbName = "Learning-Database";
    public static final String url = "jdbc:sqlite:" + dbName + ".db";
    public static final String enableFK = "PRAGMA foreign_keys = ON;";
    
    public static void updateDatabase(int id, String name, GroupController controller) {
        String sql = "UPDATE groups SET " +
                (name == null ? "" : "name='" + name + "', ") +
                "iteration=" + controller.getIteration() + ", " +
                "max_score=" + controller.getMaxAvgScore() + ", " +
                "width=" + controller.getWidth() + ", " +
                "height=" + controller.getHeight() + ", " +
                "hidden_layers=" + controller.getHiddenLayers() + ", " +
                "activation='" + controller.getActivationFunctionType() + "', " +
                "count=" + controller.getCollectionSize() + ", " +
                "runs_limit=" + controller.getRunsLimit() + ", " +
                "max_density=" + controller.getMaxDensity() + ", " +
                "distribution=" + controller.getDistribution() + ", " +
                "object=" + "?" + " " +
                "WHERE id = " + id + ";";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            Statement s = conn.createStatement();
            s.execute(enableFK);
            s.close();
            
            pstmt.setObject(1, SerializationUtils.serialize(controller), Types.BLOB);
            pstmt.execute();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static GroupController getController(int id) {
        String sql = "SELECT object FROM groups WHERE id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
    
            Statement s = conn.createStatement();
            s.execute(enableFK);
            s.close();
            
            res.next();
            InputStream bis = res.getBinaryStream("object");
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (GroupController)ois.readObject();
            
        } catch (SQLException | IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static Network getNetwork(int id) {
        String sql = "SELECT object FROM networks WHERE id = " + id + ";";
        
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(enableFK);
            s.close();
            
            res.next();
            InputStream bis = res.getBinaryStream("object");
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (Network) ois.readObject();
            
        } catch (SQLException | IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static void saveNetworks(int id, GroupController controller) {
        
        String sql0 = "INSERT INTO networks (group_id, generation, in_size, out_size, layers, activation, object) VALUES (?, ?, ?, ?, ?, ?, ?);";
    
        ResultSet res = null;
        PreparedStatement pstmt1 = null;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql0, Statement.RETURN_GENERATED_KEYS)) {
    
            Statement s = conn.createStatement();
            s.execute(enableFK);
            s.close();
            
            conn.setAutoCommit(false);
            
            for (int i = 0; i < controller.getCollectionSize(); i++) {
                Network network = controller.getCollection().getNetwork(i);
                pstmt.setInt(1, id);
                pstmt.setInt(2, network.getGeneration());
                pstmt.setInt(3, network.getInSize());
                pstmt.setInt(4, network.getOutSize());
                pstmt.setInt(5, network.getLayersCount());
                pstmt.setString(6, controller.getActivationFunctionType().toString());
                pstmt.setObject(7, SerializationUtils.serialize(network), Types.BLOB);
                pstmt.execute();
                
                res = pstmt.getGeneratedKeys();
                res.next();
                int networkId = res.getInt(1);
                res.close();
                
                for (int j = 0; j < network.getLayersCount(); j++) {
                    for (int k = 0; k < network.getNodes().get(j).size(); k++) {
                        Network.Node node = network.getNodes().get(j).get(k);
                        String sql1 = "INSERT INTO nodes (network_id, layer, node_index, bias, type, object) VALUES (" + networkId + ", " + j + ", " + k + ", " + node.getBias() + ", '" + node.getType() + "', " + "?" + ");";
                        pstmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
                        pstmt1.setObject(1, SerializationUtils.serialize(node), Types.BLOB);
                        pstmt1.execute();
                        ResultSet res1 = pstmt1.getGeneratedKeys();
                        int node_id = res1.getInt(1);
                        pstmt1.close();
                        res1.close();
    
                        for (int l = 0; l < node.getOutLinks().size(); l++) {
                            Network.Link link = node.getOutLinks().get(l);
                            String sql2 = "INSERT INTO links (in_node_id, in_node_index, out_node_index, weight, object) VALUES(" + node_id + ", " + k + ", " + l + ", " + link.getWeight() + ", " + "?" + ");";
                            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                            pstmt2.setObject(1, SerializationUtils.serialize(link), Types.BLOB);
                            pstmt2.execute();
                            pstmt2.close();
                        }
                    }
                }
                
            }
            conn.commit();
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        /*String sql1 = "SELECT id FROM networks WHERE object = ?;";
        
        
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            for (int i = 0; i < controller.getCollectionSize(); i++) {
                Network network = controller.getCollection().getNetwork(i);
                pstmt.setObject(7, SerializationUtils.serialize(network), Types.BLOB);
                res = pstmt.executeQuery();
                res.next();
                int networkId = res.getInt("id");
    
                for (int j = 0; j < network.getLayersCount(); j++) {
                    for (int k = 0; k < network.getNodes().get(j).size(); k++) {
                        Network.Node node = network.getNodes().get(j).get(k);
                        String sql2 = "INSERT INTO nodes (network_id, bias, type, object) VALUES (" + networkId + ", " + node.getBias() + ", " + node.getType() + ", " + "?" + ")";
                        pstmt1 = conn.prepareStatement(sql2);
                        pstmt1.setObject(1, SerializationUtils.serialize(node), Types.BLOB);
                        pstmt1.execute();
                        pstmt1.close();
                    }
                }
                
                res.close();
            }
            
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (pstmt1 != null) {
                    pstmt1.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/
    }
    
    public static double clamp(double val, double min, double max) {
        return Math.min(max, Math.max(min, val));
    }
    
    public static int clamp(int val, int min, int max) {
        return Math.min(max, Math.max(min, val));
    }
    
    public static double wrap(double val, double size) {
        while (val < 0 || val > size - 1){
            if (val > 0){
                val -= size;
            }
            else {
                val += size;
            }
        }
        return val;
    }
    
    public static int wrap(int val, int size) {
        while (val < 0 || val > size - 1){
            if (val > 0){
                val -= size;
            }
            else {
                val += size;
            }
        }
        return val;
    }
    
    public static int randomInt(int min, int max) {
        return (int)Math.floor(Math.random() * (max - min + 1)) + min;
    }
    
    public static double randomDouble(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }
}

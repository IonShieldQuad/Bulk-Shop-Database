package main;

import main.Utils;
import model.Item;
import model.Supply;
import model.Warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;

public class SupplyViewWindow {
    private JPanel rootPanel;
    private JButton saveButton;
    private JTextField idField;
    private JTextField itemIDField;
    private JTextField warehouseIDField;
    private JTextField countField;
    private JTextField itemPriceField;
    private JTextField datetimeField;
    
    private int id;
    
    private void initComponents() {
        saveButton.addActionListener(ev -> updateDatabase());
    }
    
    private void updateFields() {
        Supply supply = Utils.getSupply(id);
        if (supply == null) {
            return;
        }
    
        String sql = "SELECT item_id, warehouse_id, datetime(datetime, 'unixepoch') AS dt FROM supplies WHERE id=?";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
    
            ResultSet res = pstmt.executeQuery();
            
            itemIDField.setText(String.valueOf(res.getInt("item_id")));
            warehouseIDField.setText(String.valueOf(res.getInt("warehouse_id")));
            datetimeField.setText(String.valueOf(res.getString("dt")));
            
            res.close();
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        idField.setText(String.valueOf(id));
        itemPriceField.setText(String.valueOf(supply.getItemPrice()));
        countField.setText(String.valueOf(supply.getCount()));
    }
    
    private void updateDatabase() {
        int itemID = Integer.parseInt(itemIDField.getText());
        int warehouseID = Integer.parseInt(warehouseIDField.getText());
        int count = Integer.parseInt(countField.getText());
        int itemPrice = Integer.parseInt(itemPriceField.getText());
        long datetime = Utils.parseDatetime(datetimeField.getText());
        
        String sql = "UPDATE supplies SET item_id=?, warehouse_id=?, count=?, item_price=?, datetime=? WHERE id=?";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, itemID);
            pstmt.setInt(2, warehouseID);
            pstmt.setInt(3, count);
            pstmt.setInt(4, itemPrice);
            pstmt.setLong(5, datetime);
            pstmt.setInt(6, id);
            
            pstmt.execute();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Number Format Error", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Date Format Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public SupplyViewWindow(int id) {
        this.id = id;
        initComponents();
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

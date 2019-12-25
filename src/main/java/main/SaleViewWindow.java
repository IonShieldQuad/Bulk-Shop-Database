package main;

import model.Sale;
import model.Supply;

import javax.swing.*;
import java.sql.*;
import java.time.format.DateTimeParseException;

public class SaleViewWindow {
    private JPanel rootPanel;
    private JButton saveButton;
    private JTextField idField;
    private JTextField itemIDField;
    private JTextField warehouseIDField;
    private JTextField countField;
    private JTextField itemPriceField;
    private JTextField datetimeField;
    private JTextField clientIDField;
    
    private int id;
    
    private void initComponents() {
        saveButton.addActionListener(ev -> updateDatabase());
    }
    
    private void updateFields() {
        Sale sale = Utils.getSale(id);
        if (sale == null) {
            return;
        }
        
        String sql = "SELECT item_id, warehouse_id, client_id, datetime(datetime, 'unixepoch') AS dt FROM sales WHERE id=?";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
            
            ResultSet res = pstmt.executeQuery();
            
            itemIDField.setText(String.valueOf(res.getInt("item_id")));
            warehouseIDField.setText(String.valueOf(res.getInt("warehouse_id")));
            clientIDField.setText(String.valueOf(res.getInt("client_id")));
            datetimeField.setText(String.valueOf(res.getString("dt")));
            
            res.close();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        idField.setText(String.valueOf(id));
        itemPriceField.setText(String.valueOf(sale.getItemPrice()));
        countField.setText(String.valueOf(sale.getCount()));
    }
    
    private void updateDatabase() {
        int itemID = Integer.parseInt(itemIDField.getText());
        int warehouseID = Integer.parseInt(warehouseIDField.getText());
        int clientID = Integer.parseInt(clientIDField.getText());
        int count = Integer.parseInt(countField.getText());
        int itemPrice = Integer.parseInt(itemPriceField.getText());
        long datetime = Utils.parseDatetime(datetimeField.getText());
        
        String sql = "UPDATE sales SET item_id=?, warehouse_id=?, client_id=?, count=?, item_price=?, datetime=? WHERE id=?";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, itemID);
            pstmt.setInt(2, warehouseID);
            pstmt.setInt(3, clientID);
            pstmt.setInt(4, count);
            pstmt.setInt(5, itemPrice);
            pstmt.setLong(6, datetime);
            pstmt.setInt(7, id);
            
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
    
    public SaleViewWindow(int id) {
        this.id = id;
        initComponents();
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

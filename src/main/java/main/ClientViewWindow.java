package main;

import model.Client;
import model.Item;
import model.Warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;

public class ClientViewWindow {
    private JPanel rootPanel;
    private JButton saveButton;
    private JTextField idField;
    private JTextField nameField;
    private JTable itemsTable;
    private JTextField datetimeField;
    private JButton filterButton;
    private JTextField itemCountField;
    private JTextField totalSpendingField;
    
    private int id;
    
    private void initComponents() {
        initTable();
        filterButton.addActionListener(ev -> {
            try {
                updateTable(Utils.parseDatetime(datetimeField.getText()));
            } catch (DateTimeParseException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: Invalid datetime format", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        });
        
        saveButton.addActionListener(ev -> updateDatabase());
    }
    
    private void updateFields() {
        Client client = Utils.getClient(id);
        if (client == null) {
            return;
        }
        
        String sql = "SELECT item_id, sum(count) AS sum, sum(count * item_price) AS total FROM sales WHERE client_id = ?;";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
        
            ResultSet res = pstmt.executeQuery();
            res.next();
    
            totalSpendingField.setText(String.valueOf(res.getInt("total")));
            itemCountField.setText(String.valueOf(res.getInt("sum")));
            
            res.close();
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        idField.setText(String.valueOf(client.getId()));
        nameField.setText(String.valueOf(client.getName()));
    }
    
    private void updateDatabase() {
        String name = nameField.getText();
        String sql = "UPDATE clients SET name=? WHERE id=?";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            
            pstmt.execute();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initTable() {
        int columnCount = 3;
        
        DefaultTableModel model = new DefaultTableModel(0, columnCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable.setAutoCreateColumnsFromModel(false);
        itemsTable.setModel(model);
        
        TableColumn[] columns = new TableColumn[columnCount];
        
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(i, 50, null, null);
            columns[i].setIdentifier(i);
        }
        
        columns[0].setWidth(20);
        columns[0].setHeaderValue("ID");
        
        columns[1].setWidth(20);
        columns[1].setHeaderValue("Name");
        
        columns[2].setWidth(20);
        columns[2].setHeaderValue("Count");
        
        
        for (TableColumn column : columns) {
            itemsTable.addColumn(column);
        }
    }
    
    private void updateTable(long datetime) {
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT item_id, sum(count) AS sum FROM sales WHERE client_id = ? AND datetime <= ? GROUP BY item_id";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
    
            pstmt.setInt(1, id);
            pstmt.setLong(2, datetime);
            
            ResultSet res = pstmt.executeQuery();
            
            while(res.next()) {
                int it_id = res.getInt("item_id");
                Item item = Utils.getItem(it_id);
                if (item == null) {
                    continue;
                }
                int count = res.getInt("sum");
                if (count > 0) {
                    model.addRow(new Object[] {
                            item.getId(),
                            item.getName(),
                            count
                    });
                }
            }
        
            res.close();
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public ClientViewWindow(int id) {
        this.id = id;
        initComponents();
        updateTable(Utils.parseDatetime(""));
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

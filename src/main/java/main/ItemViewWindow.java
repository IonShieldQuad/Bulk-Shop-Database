package main;

import model.Item;
import model.Warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;

public class ItemViewWindow {
    private JPanel rootPanel;
    private JButton saveButton;
    private JTextField idField;
    private JTextField nameField;
    private JTable itemsTable;
    private JTextField datetimeField;
    private JButton filterButton;
    
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
        Item item = Utils.getItem(id);
        if (item == null) {
            return;
        }
        
        idField.setText(String.valueOf(item.getId()));
        nameField.setText(String.valueOf(item.getName()));
    }
    
    private void updateDatabase() {
        String name = nameField.getText();
        String sql = "UPDATE items SET name=? WHERE id=?";
        
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
        columns[2].setHeaderValue("Address");
        
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
        
        Item item = Utils.getItem(id);
        if (item == null) {
            return;
        }
    
        String sql = "SELECT id FROM warehouses;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet res = pstmt.executeQuery()) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                int wh_id = res.getInt("id");
                Warehouse warehouse = Utils.getWarehouse(wh_id, datetime);
                if (warehouse == null) {
                    continue;
                }
                int count = 0;
                count += warehouse.getCountOf(item);
                if (count > 0) {
                    model.addRow(new Object[] {
                            warehouse.getId(),
                            warehouse.getName(),
                            count
                    });
                }
            }
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public ItemViewWindow(int id) {
        this.id = id;
        initComponents();
        updateTable(Utils.parseDatetime(""));
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

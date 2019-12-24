package main;

import model.Client;
import model.Item;
import model.Sale;
import model.Warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;

public class WarehouseViewWindow {
    private JPanel rootPanel;
    private JTable itemsTable;
    private JTextField datetimeField;
    private JButton filterButton;
    private JTextField idField;
    private JTextField nameField;
    private JTextField addressField;
    private JButton saveButton;
    
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
        Warehouse warehouse = Utils.getWarehouse(id);
        if (warehouse == null) {
            return;
        }
        
        idField.setText(String.valueOf(warehouse.getId()));
        nameField.setText(String.valueOf(warehouse.getName()));
        addressField.setText(String.valueOf(warehouse.getAddress()));
    }
    
    private void updateDatabase() {
        String name = nameField.getText();
        String address = addressField.getText();
        String sql = "UPDATE warehouses SET name=?, address=? WHERE id=?";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
        
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setInt(3, id);
            
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
        
        Warehouse warehouse = Utils.getWarehouse(id, datetime);
        if (warehouse == null) {
            return;
        }
        
        for (Item item : warehouse.getItems().keySet()) {
            if (warehouse.getCountOf(item) > 0) {
                model.addRow(new Object[]{
                        item.getId(),
                        item.getName(),
                        warehouse.getCountOf(item)
                });
            }
        }
    }
    
    public WarehouseViewWindow(int id) {
        this.id = id;
        initComponents();
        updateTable(Utils.parseDatetime(""));
        updateFields();
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

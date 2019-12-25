package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.MainWindow.TITLE;

public class SuppliesWindow {
    private JTable suppliesTable;
    private JButton createButton;
    private JTextField itemIDField;
    private JButton viewButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JPanel rootPanel;
    private JTextField warehouseIDField;
    private JTextField countField;
    private JTextField itemPriceField;
    private JTextField dateTimeField;
    
    public SuppliesWindow() {
        initComponents();
        updateTable();
    }
    
    private void initComponents() {
        initTable();
        
        refreshButton.addActionListener(ev -> updateTable());
    
        viewButton.addActionListener(ev -> {
            int index = suppliesTable.getSelectedRow();
            if (index == -1) {
                return;
            }
        
            int id = Integer.parseInt(String.valueOf(suppliesTable.getModel().getValueAt(index, 0)));
        
            JFrame frame = new JFrame(TITLE);
            SupplyViewWindow gui = new SupplyViewWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        
        deleteButton.addActionListener(ev -> {
            int index = suppliesTable.getSelectedRow();
            if (index == -1) {
                return;
            }
            
            int id = Integer.parseInt(String.valueOf(suppliesTable.getModel().getValueAt(index, 0)));
            
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.submit(() -> {
                createButton.setEnabled(false);
                deleteButton.setEnabled(false);
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected supply entry?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    
                    String sql = "DELETE FROM supplies WHERE id=?";
                    
                    try (Connection conn = DriverManager.getConnection(Utils.url);
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
                        Statement s = conn.createStatement();
                        s.execute(Utils.enableFK);
                        s.close();
                        
                        pstmt.setInt(1, id);
                        pstmt.execute();
                        
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                updateTable();
                createButton.setEnabled(true);
                deleteButton.setEnabled(true);
            });
            ex.shutdown();
        });
        
        createButton.addActionListener(ev -> {
            try {
                int itemID = Integer.parseInt(itemIDField.getText());
                int warehouseID = Integer.parseInt(warehouseIDField.getText());
                int count = Integer.parseInt(countField.getText());
                int itemPrice = Integer.parseInt(itemPriceField.getText());
                
                String sql = "INSERT INTO supplies(item_id, warehouse_id, count, item_price, datetime) VALUES (?, ?, ?, ?, ?)";
                
                try (Connection conn = DriverManager.getConnection(Utils.url);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
                    Statement s = conn.createStatement();
                    s.execute(Utils.enableFK);
                    s.close();
                    
                    pstmt.setInt(1, itemID);
                    pstmt.setInt(2, warehouseID);
                    pstmt.setInt(3, count);
                    pstmt.setInt(4, itemPrice);
                    pstmt.setLong(5, Utils.parseDatetime(dateTimeField.getText()));
                    pstmt.execute();
                    
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: Invalid datetime format", "Error", JOptionPane.ERROR_MESSAGE);
            }
    
            updateTable();
        });
    }
    
    private void initTable() {
        int columnCount = 6;
        
        DefaultTableModel model = new DefaultTableModel(0, columnCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        suppliesTable.setAutoCreateColumnsFromModel(false);
        suppliesTable.setModel(model);
        
        TableColumn[] columns = new TableColumn[columnCount];
        
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(i, 50, null, null);
            columns[i].setIdentifier(i);
        }
        
        columns[0].setWidth(20);
        columns[0].setHeaderValue("ID");
        
        columns[1].setWidth(20);
        columns[1].setHeaderValue("Item ID");
    
        columns[2].setWidth(20);
        columns[2].setHeaderValue("Warehouse ID");
    
        columns[3].setWidth(20);
        columns[3].setHeaderValue("Count");
    
        columns[4].setWidth(20);
        columns[4].setHeaderValue("Item Price");
    
        columns[5].setWidth(100);
        columns[5].setHeaderValue("Datetime");
        
        
        for (TableColumn column : columns) {
            suppliesTable.addColumn(column);
        }
    }
    
    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) suppliesTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT id, item_id, warehouse_id, count, item_price, datetime(datetime, 'unixepoch') dt FROM supplies;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {
                        res.getInt("id"),
                        res.getInt("item_id"),
                        res.getInt("warehouse_id"),
                        res.getInt("count"),
                        res.getInt("item_price"),
                        res.getString("dt")
                });
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public JPanel getRootPanel() {
        return rootPanel;
    }
}

package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.MainWindow.TITLE;

public class SalesWindow {
    private JTable salesTable;
    private JButton createButton;
    private JTextField itemIDField;
    private JButton viewButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JTextField warehouseIDField;
    private JTextField countField;
    private JTextField itemPriceField;
    private JTextField dateTimeField;
    private JPanel rootPanel;
    private JTextField clientIDField;
    
    public SalesWindow() {
        initComponents();
        updateTable();
    }
    
    private void initComponents() {
        initTable();
        
        refreshButton.addActionListener(ev -> updateTable());
    
        viewButton.addActionListener(ev -> {
            int index = salesTable.getSelectedRow();
            if (index == -1) {
                return;
            }
        
            int id = Integer.parseInt(String.valueOf(salesTable.getModel().getValueAt(index, 0)));
        
            JFrame frame = new JFrame(TITLE);
            SaleViewWindow gui = new SaleViewWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        
        deleteButton.addActionListener(ev -> {
            int index = salesTable.getSelectedRow();
            if (index == -1) {
                return;
            }
            
            int id = Integer.parseInt(String.valueOf(salesTable.getModel().getValueAt(index, 0)));
            
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.submit(() -> {
                createButton.setEnabled(false);
                deleteButton.setEnabled(false);
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected sales entry?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    
                    String sql = "DELETE FROM sales WHERE id=?";
                    
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
                int clientID = Integer.parseInt(clientIDField.getText());
                int count = Integer.parseInt(countField.getText());
                int itemPrice = Integer.parseInt(itemPriceField.getText());
                
                String sql = "INSERT INTO sales(item_id, warehouse_id, client_id, count, item_price, datetime) VALUES (?, ?, ?, ?, ?, ?)";
                
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
                    pstmt.setLong(6, Utils.parseDatetime(dateTimeField.getText()));
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
        int columnCount = 7;
        
        DefaultTableModel model = new DefaultTableModel(0, columnCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable.setAutoCreateColumnsFromModel(false);
        salesTable.setModel(model);
        
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
        columns[3].setHeaderValue("Client ID");
        
        columns[4].setWidth(20);
        columns[4].setHeaderValue("Count");
        
        columns[5].setWidth(20);
        columns[5].setHeaderValue("Item Price");
        
        columns[6].setWidth(100);
        columns[6].setHeaderValue("Datetime");
        
        
        for (TableColumn column : columns) {
            salesTable.addColumn(column);
        }
    }
    
    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT id, item_id, warehouse_id, client_id, count, item_price, datetime(datetime, 'unixepoch') dt FROM sales;";
        
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
                        res.getInt("client_id"),
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

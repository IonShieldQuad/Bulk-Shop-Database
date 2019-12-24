package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.MainWindow.TITLE;

public class ItemsWindow {
    private JTable itemsTable;
    private JButton createButton;
    private JTextField nameField;
    private JButton viewButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JPanel rootPanel;
    
    public ItemsWindow() {
        initComponents();
        updateTable();
    }
    
    private void initComponents() {
        initTable();
        
        refreshButton.addActionListener(ev -> updateTable());
    
        viewButton.addActionListener(ev -> {
            int index = itemsTable.getSelectedRow();
            if (index == -1) {
                return;
            }
        
            int id = Integer.parseInt(String.valueOf(itemsTable.getModel().getValueAt(index, 0)));
        
            JFrame frame = new JFrame(TITLE);
            ItemViewWindow gui = new ItemViewWindow(id);
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        
        deleteButton.addActionListener(ev -> {
            int index = itemsTable.getSelectedRow();
            if (index == -1) {
                return;
            }
            
            int id = Integer.parseInt(String.valueOf(itemsTable.getModel().getValueAt(index, 0)));
            
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.submit(() -> {
                createButton.setEnabled(false);
                deleteButton.setEnabled(false);
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected item and all associated entries?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    
                    String sql = "DELETE FROM items WHERE id=?";
                    
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
                String name = nameField.getText();
                
                String sql = "INSERT INTO items(name) VALUES (?)";
                
                try (Connection conn = DriverManager.getConnection(Utils.url);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
                    Statement s = conn.createStatement();
                    s.execute(Utils.enableFK);
                    s.close();
                    
                    pstmt.setString(1, name);
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
            }
            
            updateTable();
        });
    }
    
    private void initTable() {
        int columnCount = 2;
        
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
        
        columns[1].setWidth(50);
        columns[1].setHeaderValue("Name");
        
        
        for (TableColumn column : columns) {
            itemsTable.addColumn(column);
        }
    }
    
    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        String sql = "SELECT id, name FROM items;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            while(res.next()) {
                model.addRow(new Object[] {
                        res.getInt("id"),
                        res.getString("name"),
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

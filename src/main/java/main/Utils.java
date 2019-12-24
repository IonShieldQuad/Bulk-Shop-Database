package main;

import model.*;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public abstract class Utils {
    
    public static final String dbName = "Bulk-Shop-Database";
    public static final String url = "jdbc:sqlite:" + dbName + ".db";
    public static final String enableFK = "PRAGMA foreign_keys = ON;";
    
    public static void createNewDatabase() {
        
        String createItemsTable = "CREATE TABLE IF NOT EXISTS items(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    name text NOT NULL\n"
                + ");";
        
        String createWarehousesTable = "CREATE TABLE IF NOT EXISTS warehouses(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    name text NOT NULL,\n"
                + "    address text NOT NULL\n"
                + ");";
        
        String createClientsTable = "CREATE TABLE IF NOT EXISTS clients(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    name text NOT NULL\n"
                + ");";
        
        String createSuppliesTable = "CREATE TABLE IF NOT EXISTS supplies(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    item_id int NOT NULL,\n"
                + "    warehouse_id int NOT NULL,\n"
                + "    count int NOT NULL,\n"
                + "    item_price int NOT NULL,\n"
                + "    datetime int,\n"
                + "    CONSTRAINT fk_warehouse"
                + "    FOREIGN KEY(warehouse_id) "
                + "    REFERENCES warehouses(id)\n"
                + "    ON DELETE CASCADE\n"
                + "    ON UPDATE CASCADE,\n"
                + "    CONSTRAINT fk_item"
                + "    FOREIGN KEY(item_id) "
                + "    REFERENCES items(id)\n"
                + "    ON DELETE CASCADE"
                + "    ON UPDATE CASCADE\n"
                + ");";
    
        String createSalesTable = "CREATE TABLE IF NOT EXISTS sales(\n"
                + "    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "    item_id int NOT NULL,\n"
                + "    warehouse_id int NOT NULL,\n"
                + "    client_id int NOT NULL,\n"
                + "    count int NOT NULL,\n"
                + "    item_price int NOT NULL,\n"
                + "    datetime int,\n"
                + "    CONSTRAINT fk_warehouse"
                + "    FOREIGN KEY(warehouse_id) "
                + "    REFERENCES warehouses(id)\n"
                + "    ON DELETE CASCADE\n"
                + "    ON UPDATE CASCADE,\n"
                + "    CONSTRAINT fk_item"
                + "    FOREIGN KEY(item_id) "
                + "    REFERENCES items(id)\n"
                + "    ON DELETE CASCADE"
                + "    ON UPDATE CASCADE\n"
                + "    CONSTRAINT fk_client"
                + "    FOREIGN KEY(client_id) "
                + "    REFERENCES clients(id)\n"
                + "    ON DELETE CASCADE"
                + "    ON UPDATE CASCADE\n"
                + ");";
        
        String exists0 = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='supplies';";
        String exists1 = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='sales';";
        
        String index0 = "CREATE INDEX supplies_warehouse_id_index ON supplies(warehouse_id);";
        String index1 = "CREATE INDEX supplies_item_id_index ON supplies(item_id);";
        String index2 = "CREATE INDEX sales_warehouse_id_index ON sales(warehouse_id);";
        String index3 = "CREATE INDEX sales_item_id_index ON sales(item_id);";
        String index4 = "CREATE INDEX sales_client_id_index ON sales(client_id);";
        
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
            
            boolean e0 = res0.getInt(1) == 1;
            boolean e1 = res1.getInt(1) == 1;
            
            res0.close();
            res1.close();;
            
            stmt.execute(createItemsTable);
            stmt.execute(createClientsTable);
            stmt.execute(createWarehousesTable);
            stmt.execute(createSuppliesTable);
            stmt.execute(createSalesTable);
            
            if (!e0) {
                stmt.execute(index0);
                stmt.execute(index1);
            }
            if (!e1) {
                stmt.execute(index2);
                stmt.execute(index3);
                stmt.execute(index4);
            }
            conn.commit();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static Client getClient(int id) {
        String sql = "SELECT * FROM clients WHERE id=?;";
    
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
    
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                res.close();
                return null;
            }
            
            Client client = new Client(res.getInt("id"), res.getString("name"));
            res.close();
            
            String sql1 = "SELECT * FROM sales WHERE client_id=?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, id);
            ResultSet res1 = pstmt1.executeQuery();
            
            while(res1.next()) {
                int count = res1.getInt("count");
                int price = res1.getInt("item_price");
                client.setItemsBought(client.getItemsBought() + count);
                client.setTotalSpent(client.getTotalSpent() + count * price);
            }
            
            res1.close();
            pstmt1.close();
            
            return client;
        
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static Item getItem(int id) {
        String sql = "SELECT * FROM items WHERE id=?;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                res.close();
                return null;
            }
            
            Item item = new Item(res.getInt("id"), res.getString("name"));
            res.close();
            return item;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static Warehouse getWarehouse(int id) {
        long datetime = java.time.Instant.now().getEpochSecond();
        return getWarehouse(id, datetime);
    }
    
    public static Warehouse getWarehouse(int id, long datetime) {
        String sql = "SELECT * FROM warehouses WHERE id=?;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                res.close();
                return null;
            }
            
            Warehouse warehouse = new Warehouse(res.getInt("id"), res.getString("name"), res.getString("address"));
            res.close();
            
            String sql1 = "SELECT id FROM supplies WHERE warehouse_id=? AND datetime <= ?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
    
            pstmt1.setInt(1, id);
            pstmt1.setLong(2, datetime);
            ResultSet res1 = pstmt1.executeQuery();
    
            while(res1.next()) {
                Supply supply = getSupply(res1.getInt("id"));
                if (supply == null) {
                    continue;
                }
                supply.put(warehouse);
            }
            res1.close();
            pstmt1.close();
    
    
            String sql2 = "SELECT id FROM sales WHERE warehouse_id=? AND datetime <= ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
    
            pstmt2.setInt(1, id);
            pstmt2.setLong(2, datetime);
            ResultSet res2 = pstmt2.executeQuery();
    
            while(res2.next()) {
                Sale sale = getSale(res2.getInt("id"));
                if (sale == null) {
                    continue;
                }
                try {
                    sale.take(warehouse);
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            res2.close();
            pstmt2.close();
            
            return warehouse;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static Supply getSupply(int id) {
        String sql = "SELECT * FROM supplies WHERE id=?;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                res.close();
                return null;
            }
            
            Item item = getItem(res.getInt("item_id"));
            
            Supply supply = new Supply(item, res.getInt("count"), res.getInt("item_price"), res.getLong("datetime"));
            res.close();
            return supply;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static Sale getSale(int id) {
        String sql = "SELECT * FROM sales WHERE id=?;";
        
        try (Connection conn = DriverManager.getConnection(Utils.url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Statement s = conn.createStatement();
            s.execute(Utils.enableFK);
            s.close();
            
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                res.close();
                return null;
            }
            
            Item item = getItem(res.getInt("item_id"));
            Client client = getClient(res.getInt("client_id"));
            
            Sale sale = new Sale(item, res.getInt("count"), res.getInt("item_price"), res.getLong("datetime"), client);
            res.close();
            return sale;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    public static long parseDatetime(String datetimeString) {
        LocalDateTime datetime;
        if (datetimeString.trim().isEmpty()) {
            datetime = LocalDateTime.now(ZoneId.of("UTC"));
        }
        else {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            datetime = LocalDateTime.parse(datetimeString, format);
        }
        return datetime.toEpochSecond(ZoneOffset.UTC);
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

package model;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {
    private int id;
    private String name;
    private String address;
    private Map<Item, Integer> items = new HashMap<>();
    
    public Warehouse(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    
    public int getCountOf(Item item) {
        return items.getOrDefault(item, 0);
    }
    
    public void putItems(Item item, int count) {
        int current = getCountOf(item);
        items.put(item, current + count);
    }
    
    public void takeItems(Item item, int count) {
        int current = getCountOf(item);
        if (current < count) {
            items.put(item, 0);
            throw new IllegalArgumentException("Warehouse (" + current + " items) doesn't contain the requested amount (" + count + " items)");
        }
        items.put(item, current - count);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Map<Item, Integer> getItems() {
        return items;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}

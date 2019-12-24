package model;

public class Client {
    private int id;
    private String name;
    private int itemsBought;
    private int totalSpent;
    
    public Client(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getItemsBought() {
        return itemsBought;
    }
    
    public void setItemsBought(int itemsBought) {
        this.itemsBought = itemsBought;
    }
    
    public int getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(int totalSpent) {
        this.totalSpent = totalSpent;
    }
}

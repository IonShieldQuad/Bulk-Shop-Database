package model;

public class Sale extends StorageOperation {
    private int itemPrice;
    private Client client;
    
    
    public Sale(Item item, int count, int itemPrice, long datetime, Client client) {
        super(item, count, datetime);
        this.itemPrice = itemPrice;
        this.client = client;
    }
    
    public int getSum() {
        return count * itemPrice;
    }
    
    @Override
    public void put(Warehouse warehouse) {
        super.put(warehouse);
        if (client != null) {
            client.setItemsBought(client.getItemsBought() + count);
            client.setTotalSpent(client.getTotalSpent() + getSum());
        }
    }
    
    @Override
    public void take(Warehouse warehouse) {
        super.take(warehouse);
        if (client != null) {
            client.setItemsBought(client.getItemsBought() + count);
            client.setTotalSpent(client.getTotalSpent() + getSum());
        }
    }
    
    public Client getClient() {
        return client;
    }
    
    public int getItemPrice() {
        return itemPrice;
    }
    
    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
}

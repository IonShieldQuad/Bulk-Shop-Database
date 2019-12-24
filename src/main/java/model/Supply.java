package model;

public class Supply extends StorageOperation {
    private int itemPrice;
    
    public Supply(Item item, int count, int itemPrice, long datetime) {
        super(item, count, datetime);
        this.itemPrice = itemPrice;
    }
    
    public int getSum() {
        return count * itemPrice;
    }
    
    public int getItemPrice() {
        return itemPrice;
    }
    
    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }
}

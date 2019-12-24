package model;

public class StorageOperation {
    protected Item item;
    protected int count;
    protected long datetime;
    
    public StorageOperation(Item item, int count, long datetime) {
        this.item = item;
        this.count = count;
        this.datetime = datetime;
    }
    
    public void put(Warehouse warehouse) {
        warehouse.putItems(item, count);
    }
    
    public void take(Warehouse warehouse) {
        warehouse.takeItems(item, count);
    }
    
    
    public Item getItem() {
        return item;
    }
    
    public void setItem(Item item) {
        this.item = item;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}

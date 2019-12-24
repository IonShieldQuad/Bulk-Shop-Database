package model;

import java.util.Objects;

public class Item {
    private int id;
    private String name;
    
    public Item(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}

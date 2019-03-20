package utilities;

/**
 * Created by 'Santiago on 31/7/2016.
 */
public class Category {
    private String name,id;
    private int resource;

    public Category(String name,String id, int resource) {
        this.name = name;
        this.id = id;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}

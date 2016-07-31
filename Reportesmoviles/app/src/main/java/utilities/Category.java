package utilities;

/**
 * Created by 'Santiago on 31/7/2016.
 */
public class Category {
    private String name;
    private int resource;

    public Category(String name, int resource) {
        this.name = name;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}

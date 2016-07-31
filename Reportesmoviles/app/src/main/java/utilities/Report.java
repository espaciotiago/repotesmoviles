package utilities;

import java.io.Serializable;

/**
 * Created by smartin on 27/06/2016.
 */
public class Report implements Serializable{
    public final static String PUBLISHED="Publicado";
    public final static String IN_PROCESS="En tramite";
    public final static String SOLVED="Solucionado";

    private String id,title,description,address,referencePoint,state,category,date;
    private int latitude,longitude,supports;
    private String[] images;

    public Report(String id, String title, String description,
                  String address, String referencePoint, int latitude,int supports,
                  int longitude, String state, String category,String date,String[] images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.referencePoint = referencePoint;
        this.latitude = latitude;
        this.longitude = longitude;
        this.supports = supports;
        this.state = state;
        this.images = images;
        this.category = category;
        this.date=date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReferencePoint() {
        return referencePoint;
    }

    public void setReferencePoint(String referencePoint) {
        this.referencePoint = referencePoint;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public int getSupports() {
        return supports;
    }

    public void setSupports(int supports) {
        this.supports = supports;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

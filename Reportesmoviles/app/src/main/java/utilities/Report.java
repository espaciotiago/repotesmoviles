package utilities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by smartin on 27/06/2016.
 */
public class Report implements Serializable{
    public final static String PUBLISHED="Publicado";
    public final static String IN_PROCESS="En tramite";
    public final static String SOLVED="Solucionado";

    private String id,title,description,address,status,category,date;
    private int supports;
    private double latitude,longitude;
    private ArrayList<String> images;

    public Report(String id, String title, String description,
                  String address, double latitude,int supports,
                  double longitude, String status, String category,String date,ArrayList<String>  images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.supports = supports;
        this.status = status;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String state) {
        this.status = state;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<String>  getImages() {
        return images;
    }

    public void setImages(ArrayList<String>  images) {
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

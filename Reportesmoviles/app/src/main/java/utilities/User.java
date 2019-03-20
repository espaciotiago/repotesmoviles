package utilities;

import java.io.Serializable;

/**
 * Created by smartin on 27/06/2016.
 */
public class User implements Serializable{
    private String name,mail,password,phone,id,gender,image;

    public User(String name, String mail, String password, String phone, String id,String gender,String image) {
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.phone = phone;
        this.id = id;
        this.gender = gender;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return name + "\n" +
                mail + "\n" +
                password + "\n" +
                phone + "\n" +
                id + "\n" +
                 gender + "\n" +
                image;
    }
}

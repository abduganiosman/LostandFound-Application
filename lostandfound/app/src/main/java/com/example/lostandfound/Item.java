package com.example.lostandfound;

import android.location.Location;
import android.view.View;

import java.util.Date;

public class Item  {

    private String name;
    private String Description;
    private String date;
    private String extra;
    private String location;
    private String image;
    private String id;
    private String username;
    private String Ctime;

        public Item(String name, String Description, String dol, String extra, String location, String image, String id, String username, String Ctime) {

            this.name = name;
            this.Description = Description;
            this.date = dol;
            this.extra = extra;
            this.location = location;
            this.image = image;
            this.id = id;
            this.username = username;
            this.Ctime = Ctime;

        }

    //ID
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    //Username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    //Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    //Description
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }

    //Date
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    //Extra
    public String getExtra() {
        return extra;
    }
    public void setExtra(String extra) {
        this.extra = extra;
    }

    //Location
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    //Image
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    //Current Time
    public String getCtime() {
        return Ctime;
    }
    public void setCtime(String Ctime) {
        this.Ctime = Ctime;
    }

    //

}

package com.kobbi.oujdashop.Models;

import java.io.Serializable;

public class Category implements Serializable {
    private int id;
    private String name, description, image;

    public Category(int id, String name, String description, String pathImg) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = pathImg;
    }

    public Category(String name, String description, String pathImg) {
        this.name = name;
        this.description = description;
        this.image = pathImg;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

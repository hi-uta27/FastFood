package com.tavanhieu.fastfood.my_class;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "MyBuyProduct")
public class BuyProduct implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Integer id;
    private String title, image;
    private int number;
    private float price;

    public BuyProduct() {
    }

    public BuyProduct(Integer id, String title, String image, int number, float price) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.number = number;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

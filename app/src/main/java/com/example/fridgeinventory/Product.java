package com.example.fridgeinventory;

import android.graphics.Bitmap;

public class Product {

    private String product_name;
    private Bitmap product_pic;

    public Product(String product_name, Bitmap product_pic){
        this.product_name=product_name;
        this.product_pic = product_pic;


    }

    public String getProduct_name(){
        return product_name;
    }

    public Bitmap getProduct_pic() {
        return product_pic;
    }


}

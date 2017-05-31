package com.tnmlicitacoes.app.model;

import android.content.Context;

public class Subscription {

    private String mName;

    private String mDescription;

    private String mSku;

    private float mPrice;

    private int mQuantity;

    private int[] mFeatures;

    public Subscription() {}

    public Subscription(Context context, int name, int description, String sku, int quantity, float price, int[] features) {
        this.mName = context.getString(name);
        this.mDescription = context.getString(description);
        this.mSku = sku;
        this.mPrice = price;
        this.mQuantity = quantity;
        this.mFeatures = features;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public float getPrice() {
        return mPrice;
    }

    public void setPrice(float price) {
        this.mPrice = price;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        this.mQuantity = quantity;
    }

    public int[] getFeatures() {
        return mFeatures;
    }

    public void setFeatures(int[] features) {
        this.mFeatures = features;
    }

    public String getSku() {
        return mSku;
    }

    public void setSku(String sku) {
        this.mSku = sku;
    }
}

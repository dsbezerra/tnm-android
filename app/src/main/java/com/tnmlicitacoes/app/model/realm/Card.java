package com.tnmlicitacoes.app.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Card extends RealmObject {

    /* The card id */
    @PrimaryKey
    private String id;

    /* The card brand name */
    private String brand;

    /* The card last 4 digits */
    private int last4;

    /* The card expiry month */
    private int expMonth;

    /* The card expiry year */
    private int expYear;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getLast4() {
        return last4;
    }

    public void setLast4(int last4) {
        this.last4 = last4;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }
}

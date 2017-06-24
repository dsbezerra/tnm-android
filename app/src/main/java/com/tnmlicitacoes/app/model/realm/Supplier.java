package com.tnmlicitacoes.app.model.realm;

import com.tnmlicitacoes.app.SupplierQuery;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Supplier extends RealmObject {

    @PrimaryKey
    private String id;

    /* The supplier phone number */
    private String phone;

    /* The supplier name */
    private String name;

    /* The supplier email */
    private String email;

    /* The supplier subscription city num */
    private int cityNum;

    /* The supplier subscription segment num */
    private int segNum;

    /* Whether the email of the supplier is confirmed or not */
    private boolean activated;

    /* Whether the supplier has an active subscription or not */
    private boolean activeSub;

    /* The default supplier card used in subscription */
    private String defaultCard;

    /* The supplier list of cards */
    private RealmList<Card> cards;

    public Supplier() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getSegNum() {
        return segNum;
    }

    public void setSegNum(int segNum) {
        this.segNum = segNum;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActiveSub() {
        return activeSub;
    }

    public void setActiveSub(boolean activeSub) {
        this.activeSub = activeSub;
    }

    public String getDefaultCard() {
        return defaultCard;
    }

    public void setDefaultCard(String defaultCard) {
        this.defaultCard = defaultCard;
    }

    public RealmList<Card> getCards() {
        return cards;
    }

    public void setCards(RealmList<Card> cards) {
        this.cards = cards;
    }

    public static Supplier copyToRealmFromGraphQL(SupplierQuery.Supplier supplier) {
        Supplier result = new Supplier();
        result.setId(supplier.id());
        result.setPhone(supplier.phone());
        result.setEmail(supplier.email());
        result.setDefaultCard(supplier.defaultCard());

        if (supplier.activated() != null) {
            result.setActivated(supplier.activated());
        }

        if (supplier.cityNum() != null) {
            result.setCityNum(supplier.cityNum());

        }

        if (supplier.segNum() != null) {
            result.setSegNum(supplier.segNum());
        }

        if (supplier.activeSub() != null) {
            result.setActiveSub(supplier.activeSub());
        }

        return result;
    }
}

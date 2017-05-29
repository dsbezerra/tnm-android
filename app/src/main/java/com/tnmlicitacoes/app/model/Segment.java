package com.tnmlicitacoes.app.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Segment extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String icon;
    private String defaultImg;
    private String mqdefault;
    private String hqdefault;

    public Segment() {

    }

    public Segment(String id, String name, String icon, String defaultImg,
                   String mqdefault, String hqdefault) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.defaultImg = defaultImg;
        this.mqdefault = mqdefault;
        this.hqdefault = hqdefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDefaultImg() {
        return defaultImg;
    }

    public void setDefaultImg(String defaultImg) {
        this.defaultImg = defaultImg;
    }

    public String getMqdefault() {
        return mqdefault;
    }

    public void setMqdefault(String mqdefault) {
        this.mqdefault = mqdefault;
    }

    public String getHqdefault() {
        return hqdefault;
    }

    public void setHqdefault(String hqdefault) {
        this.hqdefault = hqdefault;
    }
}

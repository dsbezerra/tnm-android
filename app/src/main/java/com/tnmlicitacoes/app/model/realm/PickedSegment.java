package com.tnmlicitacoes.app.model.realm;

import com.tnmlicitacoes.app.apollo.SegmentsQuery;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class PickedSegment extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    @Ignore
    private String icon;
    @Ignore
    private String defaultImg;
    @Ignore
    private String mqdefault;
    @Ignore
    private String hqdefault;

    public PickedSegment() {

    }

    public PickedSegment(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PickedSegment(String id, String name, String icon, String defaultImg,
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

    public static PickedSegment copyToRealmFromGraphQL(SegmentsQuery.Node segment) {
        return new PickedSegment(segment.id(), segment.name());
    }
}

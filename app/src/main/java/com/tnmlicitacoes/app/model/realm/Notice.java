package com.tnmlicitacoes.app.model.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Notice extends RealmObject {

    @PrimaryKey
    private String id;
    private String object;
    private String number;
    private String link;
    private String url;
    private String modality;
    private double amount;
    private boolean exclusive;
    private String agencyId;
    @Index
    private String segId;
    private Agency agency;
    private Segment segment;
    private Date disputeDate;

    public Notice() {

    }

    public Notice(String id, String object, String number, String link, String url, String modality,
                  boolean exclusive, double amount, Agency agency, Segment segment, Date disputeDate) {
        this.id = id;
        this.object = object;
        this.number = number;
        this.link = link;
        this.url = url;
        this.modality = modality;
        this.exclusive = exclusive;
        this.amount = amount;
        this.agency = agency;
        this.segment = segment;
        this.disputeDate = disputeDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public Date getDisputeDate() {
        return disputeDate;
    }

    public void setDisputeDate(Date disputeDate) {
        this.disputeDate = disputeDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getSegId() {
        return segId;
    }

    public void setSegId(String segId) {
        this.segId = segId;
    }
}
